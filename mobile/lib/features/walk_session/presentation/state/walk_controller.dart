import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

import '../../../../core/location/location_provider.dart';
import '../../../../core/location/location_service.dart';
import '../../../../core/geo/haversine.dart';
import '../../../../core/network/api_exception.dart';
import '../../../auth/presentation/state/session_controller.dart';
import '../../../places/domain/place.dart';
import '../../../routing/data/routing_api.dart';
import '../../../routing/domain/route_option.dart';
import '../../../routing/presentation/state/plan_controller.dart';
import '../../data/location_tracker.dart';
import '../../data/walk_session_api.dart';
import '../../domain/route_distance.dart';
import '../../domain/tracking_policy.dart';
import '../../domain/walk_session.dart';

/// checking: looking for an unfinished walk. none: free to plan one. pending: an unfinished walk needs a decision.
/// active: tracking. summary: the walk just ended.
enum WalkPhase { checking, none, pending, active, summary }

enum StartOutcome { started, noLocation, tooFar, alreadyActive, failed }

class StartResult {
  const StartResult(
    this.outcome, {
    this.distanceMeters,
    this.failure,
    this.message,
  });

  final StartOutcome outcome;
  final double? distanceMeters;
  final LocationFailure? failure;
  final String? message;
}

class WalkState {
  const WalkState({
    this.phase = WalkPhase.checking,
    this.session,
    this.route,
    this.position,
    this.sendFailing = false,
    this.emergencyActive = false,
    this.finished,
    this.message,
  });

  final WalkPhase phase;
  final WalkSession? session;
  final DecodedRoute? route;
  final LatLng? position;

  /// The last location report did not reach the server.
  final bool sendFailing;

  /// The server flagged this walk as an emergency (the emergency screen arrives in M7).
  final bool emergencyActive;
  final WalkSession? finished;
  final String? message;

  WalkState copyWith({
    WalkPhase? phase,
    WalkSession? session,
    DecodedRoute? route,
    LatLng? position,
    bool? sendFailing,
    bool? emergencyActive,
    WalkSession? finished,
    String? Function()? message,
  }) => WalkState(
    phase: phase ?? this.phase,
    session: session ?? this.session,
    route: route ?? this.route,
    position: position ?? this.position,
    sendFailing: sendFailing ?? this.sendFailing,
    emergencyActive: emergencyActive ?? this.emergencyActive,
    finished: finished ?? this.finished,
    message: message != null ? message() : this.message,
  );
}

/// Timers, injectable so tests do not wait.
class WalkTiming {
  const WalkTiming({
    this.heartbeat = TrackingPolicy.heartbeat,
    this.poll = const Duration(seconds: 20),
  });

  final Duration heartbeat;
  final Duration poll;
}

final walkTimingProvider = Provider<WalkTiming>((ref) => const WalkTiming());
final walkClockProvider = Provider<DateTime Function()>((ref) => DateTime.now);

final walkProvider = NotifierProvider<WalkController, WalkState>(
  WalkController.new,
);

class WalkController extends Notifier<WalkState> {
  StreamSubscription<LatLng>? _positions;
  Timer? _heartbeat;
  Timer? _poll;
  DateTime? _lastSentAt;
  LatLng? _lastSentPosition;
  bool _sending = false;

  int? get _userId => ref.read(sessionProvider).value?.id;

  @override
  WalkState build() {
    ref.onDispose(_stopTracking);
    // Look for an unfinished walk whenever someone signs in; forget everything when they sign out.
    ref.listen(sessionProvider.select((s) => s.value?.id), (previous, next) {
      Future.microtask(() => next == null ? _signedOut() : restore());
    }, fireImmediately: true);
    return const WalkState();
  }

  void _signedOut() {
    _stopTracking();
    state = const WalkState(phase: WalkPhase.none);
  }

  /// Finds a walk that was left unfinished (the app was closed or killed) and asks what to do with it.
  Future<void> restore() async {
    if (state.phase == WalkPhase.active) return;
    final userId = _userId;
    if (userId == null) return;
    try {
      final sessions = await ref.read(walkSessionApiProvider).mine(userId);
      sessions.sort((a, b) => b.startTime.compareTo(a.startTime));
      final latest = sessions.isEmpty ? null : sessions.first;
      if (state.phase == WalkPhase.active) return;
      state = (latest != null && !latest.isFinished)
          ? WalkState(phase: WalkPhase.pending, session: latest)
          : const WalkState(phase: WalkPhase.none);
    } on ApiException {
      if (state.phase == WalkPhase.checking) {
        state = const WalkState(phase: WalkPhase.none);
      }
    }
  }

  Future<DecodedRoute?> _loadRoute(int? routeId) async {
    if (routeId == null) return null;
    try {
      final option = await ref.read(routingApiProvider).byId(routeId);
      return decodeRoutes([option]).first;
    } on ApiException {
      return null;
    }
  }

  /// Picks an unfinished walk back up and keeps reporting its location.
  Future<void> resume() async {
    final session = state.session;
    if (session == null || state.phase != WalkPhase.pending) return;
    final route = await _loadRoute(session.routeId);
    state = WalkState(phase: WalkPhase.active, session: session, route: route);
    _startTracking();
  }

  /// Ends an unfinished walk without resuming it, so a new one can be started.
  Future<void> endPending() async {
    final session = state.session;
    final userId = _userId;
    if (session == null || userId == null) return;
    try {
      await ref.read(walkSessionApiProvider).end(session.id, userId);
      state = const WalkState(phase: WalkPhase.none);
    } on ApiException catch (e) {
      state = state.copyWith(message: () => e.message);
    }
  }

  Future<StartResult> start({
    required DecodedRoute route,
    required Place origin,
    required Place destination,
  }) async {
    final userId = _userId;
    if (userId == null) {
      return const StartResult(
        StartOutcome.failed,
        message: 'Please sign in again.',
      );
    }

    final location = await ref.read(locationServiceProvider).current();
    final here = location.point;
    if (here == null) {
      return StartResult(StartOutcome.noLocation, failure: location.failure);
    }

    // Starting far from the route would look like an instant deviation to the server.
    final away = distanceToRouteMeters(here, route.points);
    if (away > maxStartDistanceMeters) {
      return StartResult(StartOutcome.tooFar, distanceMeters: away);
    }

    try {
      final session = await ref
          .read(walkSessionApiProvider)
          .start(
            userId,
            StartWalkRequest(
              originLatitude: origin.latitude,
              originLongitude: origin.longitude,
              destinationLatitude: destination.latitude,
              destinationLongitude: destination.longitude,
              chosenRouteId: route.route.id,
            ),
          );
      state = WalkState(
        phase: WalkPhase.active,
        session: session,
        route: route,
        position: here,
      );
      _startTracking();
      return const StartResult(StartOutcome.started);
    } on ApiException catch (e) {
      if (e.statusCode == 409) {
        await restore();
        return StartResult(StartOutcome.alreadyActive, message: e.message);
      }
      return StartResult(StartOutcome.failed, message: e.message);
    }
  }

  void _startTracking() {
    _stopTracking();
    final timing = ref.read(walkTimingProvider);
    // The server counted the start itself as the first location.
    _lastSentAt = ref.read(walkClockProvider)();
    _lastSentPosition = state.position;
    _positions = ref
        .read(locationTrackerProvider)
        .positions()
        .listen(_onPosition, onError: (Object _) {});
    _heartbeat = Timer.periodic(timing.heartbeat, (_) => _sendLatest());
    _poll = Timer.periodic(timing.poll, (_) => refreshSession());
  }

  void _stopTracking() {
    _positions?.cancel();
    _heartbeat?.cancel();
    _poll?.cancel();
    _positions = null;
    _heartbeat = null;
    _poll = null;
    _lastSentAt = null;
    _lastSentPosition = null;
  }

  void _onPosition(LatLng position) {
    if (state.phase != WalkPhase.active) return;
    state = state.copyWith(position: position);
    final send = shouldSendLocation(
      now: ref.read(walkClockProvider)(),
      position: position,
      lastSentAt: _lastSentAt,
      lastSentPosition: _lastSentPosition,
    );
    if (send) _send(position);
  }

  void _sendLatest() {
    final position = state.position;
    if (position != null) _send(position);
  }

  /// Sends one position. If it fails nothing is queued: the next fix is newer and replaces it.
  Future<void> _send(LatLng position) async {
    final session = state.session;
    final userId = _userId;
    if (_sending ||
        session == null ||
        userId == null ||
        state.phase != WalkPhase.active) {
      return;
    }
    _sending = true;
    try {
      await ref
          .read(walkSessionApiProvider)
          .updateLocation(
            session.id,
            userId,
            position.latitude,
            position.longitude,
          );
      _lastSentAt = ref.read(walkClockProvider)();
      _lastSentPosition = position;
      if (state.sendFailing) state = state.copyWith(sendFailing: false);
    } on ApiException {
      if (state.phase == WalkPhase.active) {
        state = state.copyWith(sendFailing: true);
        // A refused update can mean the server already ended this walk.
        unawaited(refreshSession());
      }
    } finally {
      _sending = false;
    }
  }

  /// Asks the server how the walk is doing: it may have ended on arrival or raised an emergency.
  Future<void> refreshSession() async {
    final session = state.session;
    final userId = _userId;
    if (session == null || userId == null || state.phase != WalkPhase.active) {
      return;
    }
    try {
      final latest = await ref
          .read(walkSessionApiProvider)
          .byId(session.id, userId);
      if (state.phase != WalkPhase.active) return;
      if (latest.isFinished) {
        _finish(latest);
      } else {
        final emergency = latest.status == SessionStatus.emergency;
        if (emergency != state.emergencyActive) {
          state = state.copyWith(emergencyActive: emergency);
        }
      }
    } on ApiException {
      // Keep going; the next poll will try again.
    }
  }

  void _finish(WalkSession finished) {
    _stopTracking();
    state = state.copyWith(phase: WalkPhase.summary, finished: finished);
  }

  /// The user ends the walk. Returns false (and sets a message) if the server refused.
  Future<bool> end() async {
    final session = state.session;
    final userId = _userId;
    if (session == null || userId == null) return false;
    try {
      final ended = await ref
          .read(walkSessionApiProvider)
          .end(session.id, userId);
      _finish(ended);
      return true;
    } on ApiException catch (e) {
      state = state.copyWith(message: () => e.message);
      return false;
    }
  }

  void clearMessage() => state = state.copyWith(message: () => null);

  /// Leaves the summary and returns to planning.
  void dismissSummary() {
    ref.read(planProvider.notifier).reset();
    state = const WalkState(phase: WalkPhase.none);
  }

  /// Meters left to the destination, for the walking screen.
  double? metersToDestination() {
    final position = state.position;
    final session = state.session;
    final lat = session?.destinationLatitude;
    final lng = session?.destinationLongitude;
    if (position == null || lat == null || lng == null) return null;
    return haversineMeters(position, LatLng(lat, lng));
  }
}

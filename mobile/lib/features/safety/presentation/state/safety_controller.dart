import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/network/providers.dart';
import '../../../../core/storage/secure_store.dart';
import '../../../../core/notifications/alert_notifier.dart';
import '../../../auth/presentation/state/session_controller.dart';
import '../../../walk_session/data/walk_session_api.dart';
import '../../../walk_session/domain/walk_session.dart';
import '../../../walk_session/presentation/state/walk_controller.dart';
import '../../data/realtime_client.dart';
import '../../domain/alert_message.dart';
import '../../domain/idle_prompt.dart';

class SafetyState {
  const SafetyState({
    this.idlePrompt,
    this.offRoute = false,
    this.connectionLost = false,
    this.error,
  });

  /// The "Are you safe?" prompt, while the server is waiting for an answer.
  final IdlePrompt? idlePrompt;
  final bool offRoute;

  /// The live connection has been down long enough to tell the walker.
  final bool connectionLost;
  final String? error;

  SafetyState copyWith({
    IdlePrompt? Function()? idlePrompt,
    bool? offRoute,
    bool? connectionLost,
    String? Function()? error,
  }) => SafetyState(
    idlePrompt: idlePrompt != null ? idlePrompt() : this.idlePrompt,
    offRoute: offRoute ?? this.offRoute,
    connectionLost: connectionLost ?? this.connectionLost,
    error: error != null ? error() : this.error,
  );
}

/// How long the connection may be down before the walker is told (brief drops should not flash a banner).
final safetyTimingProvider = Provider<Duration>(
  (ref) => const Duration(seconds: 5),
);

const _idleNotificationId = 1001;
const _deviationNotificationId = 1002;
const _emergencyNotificationId = 1003;
const _connectionNotificationId = 1004;

final safetyProvider = NotifierProvider<SafetyController, SafetyState>(
  SafetyController.new,
);

/// Everything the server tells the walker during a walk: the live connection, the "are you safe?" prompt,
/// the off-route warning, and emergencies.
class SafetyController extends Notifier<SafetyState> {
  Timer? _lostTimer;
  bool _registered = false;
  bool _connected = false;
  int? _sessionId;
  late final RealtimeClient _realtime;
  late final SecureStore _store;

  @override
  SafetyState build() {
    // Held in a field: providers may not be read while this one is being disposed.
    _realtime = ref.read(realtimeClientProvider);
    _store = ref.read(secureStoreProvider);
    ref.onDispose(_teardown);
    ref.listen(
      walkProvider.select(
        (s) => s.phase == WalkPhase.active ? s.session?.id : null,
      ),
      (previous, next) {
        Future.microtask(() => next == null ? _end() : _begin(next));
      },
      fireImmediately: true,
    );
    ref.listen(
      walkProvider.select((s) => s.position),
      (previous, next) => _register(next),
    );
    ref.listen(walkProvider.select((s) => s.latest), (previous, next) {
      if (next != null) _syncFromServer(next);
    });
    return const SafetyState();
  }

  void _begin(int sessionId) {
    if (_sessionId == sessionId) return;
    _teardown();
    _sessionId = sessionId;
    state = const SafetyState();
    _realtime.connect(
      sessionId: sessionId,
      tokenProvider: _store.readToken,
      onAlert: _onAlert,
      onConnection: _onConnection,
    );
  }

  void _end() {
    _teardown();
    state = const SafetyState();
  }

  void _teardown() {
    _lostTimer?.cancel();
    _lostTimer = null;
    _registered = false;
    _connected = false;
    if (_sessionId != null) _realtime.disconnect();
    _sessionId = null;
  }

  void _onConnection(bool connected) {
    _connected = connected;
    if (connected) {
      _lostTimer?.cancel();
      _lostTimer = null;
      _registered = false;
      if (state.connectionLost) state = state.copyWith(connectionLost: false);
      _register(ref.read(walkProvider).position);
    } else {
      _registered = false;
      _lostTimer ??= Timer(ref.read(safetyTimingProvider), () {
        _lostTimer = null;
        if (_sessionId == null || _connected) return;
        state = state.copyWith(connectionLost: true);
        _notify(
          _connectionNotificationId,
          'Connection lost',
          'SafeWalk is trying to reconnect. Keep the app open.',
        );
      });
    }
  }

  /// Once per connection, tells the server which walk this connection belongs to, so it can notice a lost one.
  void _register(LatLng? position) {
    if (position == null || !_connected || _registered || _sessionId == null) {
      return;
    }
    _registered = true;
    _realtime.register(position);
  }

  void _onAlert(AlertMessage alert) {
    if (_sessionId == null) return;
    final walk = ref.read(walkProvider.notifier);
    switch (alert.type) {
      case AlertType.idleWarning:
        _showIdlePrompt();
      case AlertType.routeDeviation:
        if (!state.offRoute) {
          state = state.copyWith(offRoute: true);
          _notify(
            _deviationNotificationId,
            "You're off your route",
            'Return to the route, or open SafeWalk and tap I\'m OK.',
          );
        }
      case AlertType.emergencyTriggered:
        walk.setEmergency(true);
        _notify(
          _emergencyNotificationId,
          'Emergency alert raised',
          'Your contacts are being alerted. Open SafeWalk.',
        );
      case AlertType.emergencyResolved:
        walk.setEmergency(false);
      case AlertType.autocomplete:
        unawaited(walk.refreshSession());
      case AlertType.unknown:
        break;
    }
  }

  void _showIdlePrompt() {
    if (state.idlePrompt != null) return;
    final now = DateTime.now();
    state = state.copyWith(idlePrompt: () => IdlePrompt.startingAt(now));
    _notify(
      _idleNotificationId,
      'Are you safe?',
      'No movement detected. Open SafeWalk and tap I\'m OK.',
    );
  }

  /// The poll of the server is a backup for a missed live message.
  void _syncFromServer(WalkSession latest) {
    if (_sessionId == null) return;
    if (latest.alarmTriggered && latest.status == SessionStatus.active) {
      _showIdlePrompt();
    } else if (!latest.alarmTriggered && state.idlePrompt != null) {
      state = state.copyWith(idlePrompt: () => null);
    }
    if (latest.deviationTriggered != state.offRoute) {
      state = state.copyWith(offRoute: latest.deviationTriggered);
    }
  }

  /// "I'm OK". Returns false (and sets an error) if the server did not accept it.
  Future<bool> imOk() async {
    final sessionId = _sessionId;
    final userId = ref.read(sessionProvider).value?.id;
    if (sessionId == null || userId == null) return false;
    try {
      await ref
          .read(walkSessionApiProvider)
          .resolveIdleWarning(sessionId, userId);
      state = state.copyWith(idlePrompt: () => null, error: () => null);
      return true;
    } on ApiException catch (e) {
      state = state.copyWith(error: () => e.message);
      return false;
    }
  }

  void dismissOffRoute() => state = state.copyWith(offRoute: false);
  void clearError() => state = state.copyWith(error: () => null);

  void _notify(int id, String title, String body) {
    if (ref.read(appInForegroundProvider)()) return;
    unawaited(
      ref.read(alertNotifierProvider).show(id: id, title: title, body: body),
    );
  }
}

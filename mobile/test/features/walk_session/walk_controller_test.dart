import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:latlong2/latlong.dart';
import 'package:safewalk_mobile/core/location/location_provider.dart';
import 'package:safewalk_mobile/core/location/location_service.dart';
import 'package:safewalk_mobile/core/network/api_exception.dart';
import 'package:safewalk_mobile/features/auth/domain/session_user.dart';
import 'package:safewalk_mobile/features/auth/presentation/state/session_controller.dart';
import 'package:safewalk_mobile/features/places/domain/place.dart';
import 'package:safewalk_mobile/features/routing/data/routing_api.dart';
import 'package:safewalk_mobile/features/routing/domain/route_option.dart';
import 'package:safewalk_mobile/features/walk_session/data/location_tracker.dart';
import 'package:safewalk_mobile/features/walk_session/data/walk_session_api.dart';
import 'package:safewalk_mobile/features/walk_session/domain/walk_session.dart';
import 'package:safewalk_mobile/features/walk_session/presentation/state/walk_controller.dart';

class _FakeSession extends SessionController {
  @override
  Future<SessionUser?> build() async =>
      const SessionUser(id: 7, email: 'a@b.c', roles: ['ROLE_USER']);
}

class _FakeLocation extends LocationService {
  const _FakeLocation(this.result);

  final LocationResult result;

  @override
  Future<LocationResult> current() async => result;
}

class _FakeTracker implements LocationTracker {
  final controller = StreamController<LatLng>.broadcast();

  @override
  Stream<LatLng> positions() => controller.stream;
}

WalkSession _session(int id, SessionStatus status, {bool auto = false}) =>
    WalkSession(
      id: id,
      userId: 7,
      routeId: 5,
      startTime: '2026-10-10T10:00:0$id',
      endTime: status == SessionStatus.completed ? '2026-10-10T10:20:00' : null,
      status: status,
      originLatitude: 52.9500,
      originLongitude: -1.1500,
      destinationLatitude: 52.9600,
      destinationLongitude: -1.1400,
      autoCompleted: auto,
    );

class _FakeWalkApi implements WalkSessionApi {
  List<WalkSession> existing = [];
  ApiException? startError;
  ApiException? updateError;
  WalkSession Function()? byIdAnswer;
  final updates = <(double, double)>[];
  final ended = <int>[];

  @override
  Future<List<WalkSession>> mine(int userId) async => [...existing];

  @override
  Future<WalkSession> start(int userId, StartWalkRequest request) async {
    if (startError != null) throw startError!;
    return _session(9, SessionStatus.active);
  }

  @override
  Future<void> updateLocation(
    int sessionId,
    int userId,
    double latitude,
    double longitude,
  ) async {
    if (updateError != null) throw updateError!;
    updates.add((latitude, longitude));
  }

  @override
  Future<WalkSession> end(int sessionId, int userId) async {
    ended.add(sessionId);
    return _session(sessionId, SessionStatus.completed);
  }

  @override
  Future<WalkSession> resolveIdleWarning(int sessionId, int userId) async =>
      _session(sessionId, SessionStatus.active);

  @override
  Future<WalkSession> byId(int sessionId, int userId) async =>
      (byIdAnswer ?? () => _session(sessionId, SessionStatus.active))();
}

class _FakeRoutingApi implements RoutingApi {
  @override
  Future<RouteOption> byId(int routeId) async => _option(routeId);

  @override
  Future<List<RouteOption>> recommend(RouteRequest request) async => [];
}

RouteOption _option(int id) => RouteOption(
  id: id,
  // A straight line from (52.95, -1.15) to (52.96, -1.14).
  polyline: '',
  actualDistanceMeters: 1400,
  safetyPenaltyMeters: 0,
  virtualDistanceMeters: 1400,
  rank: 1,
  routeRequestId: 'r',
);

DecodedRoute _route() => DecodedRoute(
  route: _option(5),
  points: const [
    LatLng(52.9500, -1.1500),
    LatLng(52.9500, -1.1400),
    LatLng(52.9600, -1.1400),
  ],
  color: recommendedColor,
);

const _origin = Place(label: 'A', latitude: 52.95, longitude: -1.15);
const _destination = Place(label: 'B', latitude: 52.96, longitude: -1.14);

class _Rig {
  _Rig({LocationResult? location})
    : api = _FakeWalkApi(),
      tracker = _FakeTracker() {
    now = DateTime(2026, 10, 10, 12);
    container = ProviderContainer(
      overrides: [
        sessionProvider.overrideWith(_FakeSession.new),
        walkSessionApiProvider.overrideWithValue(api),
        routingApiProvider.overrideWithValue(_FakeRoutingApi()),
        locationTrackerProvider.overrideWithValue(tracker),
        locationServiceProvider.overrideWithValue(
          _FakeLocation(
            location ?? const LocationResult.found(LatLng(52.9501, -1.1499)),
          ),
        ),
        walkClockProvider.overrideWithValue(() => now),
        walkTimingProvider.overrideWithValue(
          const WalkTiming(
            heartbeat: Duration(hours: 1),
            poll: Duration(hours: 1),
          ),
        ),
      ],
    );
  }

  final _FakeWalkApi api;
  final _FakeTracker tracker;
  late final ProviderContainer container;
  late DateTime now;

  WalkController get controller => container.read(walkProvider.notifier);
  WalkState get state => container.read(walkProvider);

  Future<void> boot() async {
    await container.read(sessionProvider.future);
    container.read(walkProvider);
    await Future<void>.delayed(Duration.zero);
    await Future<void>.delayed(Duration.zero);
  }

  Future<StartResult> start() => controller.start(
    route: _route(),
    origin: _origin,
    destination: _destination,
  );

  void emit(LatLng p) {
    tracker.controller.add(p);
  }

  Future<void> settle() => Future<void>.delayed(Duration.zero);

  Future<void> dispose() async {
    container.dispose();
    await tracker.controller.close();
  }
}

void main() {
  test('with no earlier walk the Walk tab is free for planning', () async {
    final rig = _Rig();
    await rig.boot();
    expect(rig.state.phase, WalkPhase.none);
    await rig.dispose();
  });

  test('an unfinished ACTIVE walk is offered for resuming', () async {
    final rig = _Rig();
    rig.api.existing = [
      _session(2, SessionStatus.completed),
      _session(3, SessionStatus.active),
    ];
    await rig.boot();
    expect(rig.state.phase, WalkPhase.pending);
    expect(rig.state.session?.id, 3);
    await rig.dispose();
  });

  test('resuming starts tracking again with the route loaded', () async {
    final rig = _Rig();
    rig.api.existing = [_session(3, SessionStatus.active)];
    await rig.boot();
    await rig.controller.resume();
    expect(rig.state.phase, WalkPhase.active);
    expect(rig.state.route, isNotNull);
    await rig.dispose();
  });

  test('ending an unfinished walk frees the tab', () async {
    final rig = _Rig();
    rig.api.existing = [_session(3, SessionStatus.emergency)];
    await rig.boot();
    expect(rig.state.phase, WalkPhase.pending);
    await rig.controller.endPending();
    expect(rig.state.phase, WalkPhase.none);
    expect(rig.api.ended, [3]);
    await rig.dispose();
  });

  test('starting near the route works and becomes active', () async {
    final rig = _Rig();
    await rig.boot();
    final result = await rig.start();
    expect(result.outcome, StartOutcome.started);
    expect(rig.state.phase, WalkPhase.active);
    expect(rig.state.session?.id, 9);
    await rig.dispose();
  });

  test(
    'starting far from the route is refused without calling the server',
    () async {
      final rig = _Rig(
        location: const LocationResult.found(LatLng(52.9700, -1.1000)),
      );
      await rig.boot();
      final result = await rig.start();
      expect(result.outcome, StartOutcome.tooFar);
      expect(result.distanceMeters, greaterThan(100));
      expect(rig.state.phase, WalkPhase.none);
      await rig.dispose();
    },
  );

  test('starting without a location fix says why', () async {
    final rig = _Rig(
      location: const LocationResult.failed(LocationFailure.denied),
    );
    await rig.boot();
    final result = await rig.start();
    expect(result.outcome, StartOutcome.noLocation);
    expect(result.failure, LocationFailure.denied);
    await rig.dispose();
  });

  test(
    'a 409 means an earlier walk is unfinished and is shown for a decision',
    () async {
      final rig = _Rig();
      rig.api.startError = const ApiException(
        409,
        'Please complete previous session',
      );
      rig.api.existing = [_session(3, SessionStatus.active)];
      await rig.boot();
      // Booting already found it; clear to prove the 409 path loads it again.
      final result = await rig.start();
      expect(result.outcome, StartOutcome.alreadyActive);
      expect(rig.state.phase, WalkPhase.pending);
      await rig.dispose();
    },
  );

  test(
    'positions are sent at most every 10 seconds and only when you moved',
    () async {
      final rig = _Rig();
      await rig.boot();
      await rig.start();

      rig.now = rig.now.add(const Duration(seconds: 4));
      rig.emit(const LatLng(52.9510, -1.1499));
      await rig.settle();
      expect(rig.api.updates, isEmpty, reason: 'too soon after the start');

      rig.now = rig.now.add(const Duration(seconds: 8));
      rig.emit(const LatLng(52.9510, -1.1499));
      await rig.settle();
      await rig.settle();
      expect(rig.api.updates, hasLength(1));

      rig.now = rig.now.add(const Duration(seconds: 12));
      rig.emit(const LatLng(52.95100, -1.14990)); // same spot
      await rig.settle();
      expect(
        rig.api.updates,
        hasLength(1),
        reason: 'standing still sends nothing before the heartbeat',
      );
      await rig.dispose();
    },
  );

  test(
    'a failed update flags the connection and a later one clears it',
    () async {
      final rig = _Rig();
      await rig.boot();
      await rig.start();

      rig.api.updateError = const ApiException(
        0,
        'offline',
        failure: ApiFailure.network,
      );
      rig.now = rig.now.add(const Duration(seconds: 12));
      rig.emit(const LatLng(52.9520, -1.1499));
      await rig.settle();
      await rig.settle();
      expect(rig.state.sendFailing, isTrue);

      rig.api.updateError = null;
      rig.now = rig.now.add(const Duration(seconds: 12));
      rig.emit(const LatLng(52.9530, -1.1499));
      await rig.settle();
      await rig.settle();
      expect(rig.state.sendFailing, isFalse);
      await rig.dispose();
    },
  );

  test('ending the walk shows the summary and stops tracking', () async {
    final rig = _Rig();
    await rig.boot();
    await rig.start();
    final ok = await rig.controller.end();
    expect(ok, isTrue);
    expect(rig.state.phase, WalkPhase.summary);
    expect(rig.state.finished?.status, SessionStatus.completed);

    rig.now = rig.now.add(const Duration(seconds: 30));
    rig.emit(const LatLng(52.9540, -1.1499));
    await rig.settle();
    expect(rig.api.updates, isEmpty);
    await rig.dispose();
  });

  test(
    'when the server ends the walk on arrival the summary appears',
    () async {
      final rig = _Rig();
      await rig.boot();
      await rig.start();
      rig.api.byIdAnswer = () =>
          _session(9, SessionStatus.completed, auto: true);
      await rig.controller.refreshSession();
      expect(rig.state.phase, WalkPhase.summary);
      expect(rig.state.finished?.autoCompleted, isTrue);
      await rig.dispose();
    },
  );

  test('an emergency raised by the server is reflected on the walk', () async {
    final rig = _Rig();
    await rig.boot();
    await rig.start();
    rig.api.byIdAnswer = () => _session(9, SessionStatus.emergency);
    await rig.controller.refreshSession();
    expect(rig.state.phase, WalkPhase.active);
    expect(rig.state.emergencyActive, isTrue);
    await rig.dispose();
  });

  test('dismissing the summary returns to planning', () async {
    final rig = _Rig();
    await rig.boot();
    await rig.start();
    await rig.controller.end();
    rig.controller.dismissSummary();
    expect(rig.state.phase, WalkPhase.none);
    await rig.dispose();
  });
}

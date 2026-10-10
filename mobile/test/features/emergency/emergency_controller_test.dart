import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/network/api_exception.dart';
import 'package:safewalk_mobile/features/auth/domain/session_user.dart';
import 'package:safewalk_mobile/features/auth/presentation/state/session_controller.dart';
import 'package:safewalk_mobile/features/emergency/data/emergency_api.dart';
import 'package:safewalk_mobile/features/emergency/domain/emergency.dart';
import 'package:safewalk_mobile/features/emergency/presentation/state/emergency_controller.dart';
import 'package:safewalk_mobile/features/walk_session/domain/walk_session.dart';
import 'package:safewalk_mobile/features/walk_session/presentation/state/walk_controller.dart';

class _FakeSession extends SessionController {
  @override
  Future<SessionUser?> build() async =>
      const SessionUser(id: 7, email: 'a@b.c', roles: ['ROLE_USER']);
}

class _ControllableWalk extends WalkController {
  int refreshes = 0;

  @override
  WalkState build() => const WalkState(phase: WalkPhase.none);

  void set(WalkState next) => state = next;

  @override
  Future<void> refreshSession() async => refreshes++;
}

Emergency _emergency({int id = 5}) => Emergency(
  id: id,
  source: TriggerSource.manualSos,
  latitude: 47.49,
  longitude: 19.07,
  triggeredAt: '2026-10-10T10:00:00',
  resolved: false,
  contacts: const [NotifiedContact(name: 'Mum', phone: '+3630')],
);

class _FakeEmergencyApi implements EmergencyApi {
  Emergency? triggerResult;
  Emergency? activeResult;
  ApiException? triggerError;
  ApiException? resolveError;
  final calls = <String>[];

  @override
  Future<Emergency?> trigger(int sessionId, int userId) async {
    calls.add('trigger $sessionId $userId');
    if (triggerError != null) throw triggerError!;
    return triggerResult;
  }

  @override
  Future<Emergency?> active(int sessionId, int userId) async {
    calls.add('active $sessionId $userId');
    return activeResult;
  }

  @override
  Future<void> resolve(int emergencyId, int sessionId, int userId) async {
    calls.add('resolve $emergencyId $sessionId $userId');
    if (resolveError != null) throw resolveError!;
  }
}

WalkSession _session() => const WalkSession(
  id: 9,
  userId: 7,
  routeId: 1,
  startTime: '2026-10-10T10:00:00',
  endTime: null,
  status: SessionStatus.active,
  originLatitude: null,
  originLongitude: null,
  destinationLatitude: null,
  destinationLongitude: null,
  autoCompleted: false,
);

class _Rig {
  _Rig() {
    container = ProviderContainer(
      overrides: [
        sessionProvider.overrideWith(_FakeSession.new),
        walkProvider.overrideWith(_ControllableWalk.new),
        emergencyApiProvider.overrideWithValue(api),
      ],
    );
  }

  final api = _FakeEmergencyApi();
  late final ProviderContainer container;

  _ControllableWalk get walk =>
      container.read(walkProvider.notifier) as _ControllableWalk;
  EmergencyState get state => container.read(emergencyProvider);
  EmergencyController get controller =>
      container.read(emergencyProvider.notifier);

  Future<void> pump() async {
    await Future<void>.delayed(Duration.zero);
    await Future<void>.delayed(Duration.zero);
  }

  Future<void> boot({bool emergency = false}) async {
    await container.read(sessionProvider.future);
    container.read(emergencyProvider);
    walk.set(
      WalkState(
        phase: WalkPhase.active,
        session: _session(),
        emergencyActive: emergency,
      ),
    );
    await pump();
  }

  void dispose() => container.dispose();
}

void main() {
  test(
    'SOS sends the alert, remembers it, and flags the walk as an emergency',
    () async {
      final rig = _Rig();
      rig.api.triggerResult = _emergency();
      await rig.boot();

      final ok = await rig.controller.triggerSos();

      expect(ok, isTrue);
      expect(rig.api.calls, ['trigger 9 7']);
      expect(rig.state.emergency?.id, 5);
      expect(rig.container.read(walkProvider).emergencyActive, isTrue);
      rig.dispose();
    },
  );

  test(
    'SOS when the walk is already in emergency fetches the active one',
    () async {
      final rig = _Rig();
      rig.api.triggerResult = null;
      rig.api.activeResult = _emergency(id: 8);
      await rig.boot();

      final ok = await rig.controller.triggerSos();
      await rig.pump();

      expect(ok, isTrue);
      expect(rig.state.emergency?.id, 8);
      rig.dispose();
    },
  );

  test(
    'a failed SOS says so and does not pretend the alert went out',
    () async {
      final rig = _Rig();
      rig.api.triggerError = const ApiException(
        0,
        "Can't reach SafeWalk.",
        failure: ApiFailure.network,
      );
      await rig.boot();

      final ok = await rig.controller.triggerSos();

      expect(ok, isFalse);
      expect(rig.state.error, "Can't reach SafeWalk.");
      expect(rig.state.triggering, isFalse);
      expect(rig.container.read(walkProvider).emergencyActive, isFalse);
      rig.dispose();
    },
  );

  test('an emergency raised by the server loads its details', () async {
    final rig = _Rig();
    rig.api.activeResult = _emergency(id: 3);
    await rig.boot(emergency: true);
    await rig.pump();

    expect(rig.api.calls, contains('active 9 7'));
    expect(rig.state.emergency?.id, 3);
    rig.dispose();
  });

  test(
    'I am safe resolves it, clears the state and refreshes the walk',
    () async {
      final rig = _Rig();
      rig.api.activeResult = _emergency(id: 3);
      await rig.boot(emergency: true);
      await rig.pump();

      final ok = await rig.controller.resolve();
      await rig.pump();

      expect(ok, isTrue);
      expect(rig.api.calls, contains('resolve 3 9 7'));
      expect(rig.container.read(walkProvider).emergencyActive, isFalse);
      expect(rig.state.emergency, isNull);
      expect(rig.walk.refreshes, greaterThan(0));
      rig.dispose();
    },
  );

  test(
    'a refused resolve keeps the emergency on screen with the reason',
    () async {
      final rig = _Rig();
      rig.api.activeResult = _emergency(id: 3);
      rig.api.resolveError = const ApiException(
        500,
        'Walk session is not in EMERGENCY status.',
      );
      await rig.boot(emergency: true);
      await rig.pump();

      final ok = await rig.controller.resolve();

      expect(ok, isFalse);
      expect(rig.state.error, 'Walk session is not in EMERGENCY status.');
      expect(rig.state.resolving, isFalse);
      expect(rig.container.read(walkProvider).emergencyActive, isTrue);
      rig.dispose();
    },
  );

  test('when the emergency ends elsewhere the details are cleared', () async {
    final rig = _Rig();
    rig.api.activeResult = _emergency(id: 3);
    await rig.boot(emergency: true);
    await rig.pump();
    expect(rig.state.emergency, isNotNull);

    rig.walk.set(WalkState(phase: WalkPhase.active, session: _session()));
    await rig.pump();

    expect(rig.state.emergency, isNull);
    rig.dispose();
  });
}

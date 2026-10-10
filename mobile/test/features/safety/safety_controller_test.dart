import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:latlong2/latlong.dart';
import 'package:safewalk_mobile/core/network/api_exception.dart';
import 'package:safewalk_mobile/core/network/providers.dart';
import 'package:safewalk_mobile/core/notifications/alert_notifier.dart';
import 'package:safewalk_mobile/core/storage/secure_store.dart';
import 'package:safewalk_mobile/features/auth/domain/session_user.dart';
import 'package:safewalk_mobile/features/auth/presentation/state/session_controller.dart';
import 'package:safewalk_mobile/features/safety/data/realtime_client.dart';
import 'package:safewalk_mobile/features/safety/domain/alert_message.dart';
import 'package:safewalk_mobile/features/safety/presentation/state/safety_controller.dart';
import 'package:safewalk_mobile/features/walk_session/data/walk_session_api.dart';
import 'package:safewalk_mobile/features/walk_session/domain/walk_session.dart';
import 'package:safewalk_mobile/features/walk_session/presentation/state/walk_controller.dart';

class _FakeSession extends SessionController {
  @override
  Future<SessionUser?> build() async =>
      const SessionUser(id: 7, email: 'a@b.c', roles: ['ROLE_USER']);
}

/// A walk whose state the test can set directly.
class _ControllableWalk extends WalkController {
  int refreshes = 0;

  @override
  WalkState build() => const WalkState(phase: WalkPhase.none);

  void set(WalkState next) => state = next;

  @override
  Future<void> refreshSession() async => refreshes++;
}

class _FakeRealtime implements RealtimeClient {
  int? connectedTo;
  Future<String?> Function()? tokenReader;
  int disconnects = 0;
  final registrations = <LatLng>[];
  void Function(AlertMessage)? onAlert;
  void Function(bool)? onConnection;

  @override
  void connect({
    required int sessionId,
    required Future<String?> Function() tokenProvider,
    required void Function(AlertMessage alert) onAlert,
    required void Function(bool connected) onConnection,
  }) {
    connectedTo = sessionId;
    tokenReader = tokenProvider;
    this.onAlert = onAlert;
    this.onConnection = onConnection;
  }

  @override
  void register(LatLng position) => registrations.add(position);

  @override
  void disconnect() {
    disconnects++;
    connectedTo = null;
  }
}

class _FakeStore extends SecureStore {
  _FakeStore() : super(null);

  @override
  Future<String?> readToken() async => 'jwt-123';
}

class _FakeNotifier implements AlertNotifier {
  final shown = <String>[];

  @override
  Future<bool> requestPermission() async => true;

  @override
  Future<void> show({
    required int id,
    required String title,
    required String body,
  }) async => shown.add(title);
}

class _FakeWalkApi implements WalkSessionApi {
  ApiException? failResolve;
  int resolves = 0;

  @override
  Future<WalkSession> resolveIdleWarning(int sessionId, int userId) async {
    if (failResolve != null) throw failResolve!;
    resolves++;
    return _session();
  }

  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw UnimplementedError('${invocation.memberName}');
}

WalkSession _session({
  bool alarm = false,
  bool deviation = false,
  SessionStatus status = SessionStatus.active,
}) => WalkSession(
  id: 9,
  userId: 7,
  routeId: 1,
  startTime: '2026-10-10T10:00:00',
  endTime: null,
  status: status,
  originLatitude: null,
  originLongitude: null,
  destinationLatitude: null,
  destinationLongitude: null,
  autoCompleted: false,
  alarmTriggered: alarm,
  deviationTriggered: deviation,
);

class _Rig {
  _Rig({bool foreground = true}) {
    container = ProviderContainer(
      overrides: [
        sessionProvider.overrideWith(_FakeSession.new),
        secureStoreProvider.overrideWithValue(_FakeStore()),
        walkProvider.overrideWith(_ControllableWalk.new),
        realtimeClientProvider.overrideWithValue(realtime),
        alertNotifierProvider.overrideWithValue(notifier),
        walkSessionApiProvider.overrideWithValue(api),
        appInForegroundProvider.overrideWithValue(() => foreground),
        safetyTimingProvider.overrideWithValue(const Duration(milliseconds: 5)),
      ],
    );
  }

  final realtime = _FakeRealtime();
  final notifier = _FakeNotifier();
  final api = _FakeWalkApi();
  late final ProviderContainer container;

  _ControllableWalk get walk =>
      container.read(walkProvider.notifier) as _ControllableWalk;
  SafetyState get safety => container.read(safetyProvider);

  Future<void> boot() async {
    await container.read(sessionProvider.future);
    container.read(safetyProvider);
    await pump();
  }

  Future<void> pump() async {
    await Future<void>.delayed(Duration.zero);
    await Future<void>.delayed(Duration.zero);
  }

  Future<void> startWalk({LatLng? position}) async {
    walk.set(
      WalkState(
        phase: WalkPhase.active,
        session: _session(),
        position: position,
      ),
    );
    await pump();
  }

  Future<void> alert(AlertType type) async {
    realtime.onAlert!(AlertMessage(sessionId: 9, type: type, message: ''));
    await pump();
  }

  void dispose() => container.dispose();
}

void main() {
  test(
    'connects when a walk becomes active and disconnects when it ends',
    () async {
      final rig = _Rig();
      await rig.boot();
      expect(rig.realtime.connectedTo, isNull);

      await rig.startWalk();
      expect(rig.realtime.connectedTo, 9);

      rig.walk.set(const WalkState(phase: WalkPhase.summary));
      await rig.pump();
      expect(rig.realtime.connectedTo, isNull);
      expect(rig.realtime.disconnects, greaterThan(0));
      rig.dispose();
    },
  );

  test(
    'registers the connection once per connect, when a position is known',
    () async {
      final rig = _Rig();
      await rig.boot();
      await rig.startWalk();

      rig.realtime.onConnection!(true);
      await rig.pump();
      expect(rig.realtime.registrations, isEmpty, reason: 'no position yet');

      rig.walk.set(
        WalkState(
          phase: WalkPhase.active,
          session: _session(),
          position: const LatLng(52.95, -1.15),
        ),
      );
      await rig.pump();
      expect(rig.realtime.registrations, hasLength(1));

      rig.walk.set(
        WalkState(
          phase: WalkPhase.active,
          session: _session(),
          position: const LatLng(52.951, -1.15),
        ),
      );
      await rig.pump();
      expect(
        rig.realtime.registrations,
        hasLength(1),
        reason: 'only once per connection',
      );

      rig.realtime.onConnection!(false);
      rig.realtime.onConnection!(true);
      await rig.pump();
      expect(
        rig.realtime.registrations,
        hasLength(2),
        reason: 'a new connection must register again',
      );
      rig.dispose();
    },
  );

  test('an idle warning opens the 45 second prompt', () async {
    final rig = _Rig();
    await rig.boot();
    await rig.startWalk();

    await rig.alert(AlertType.idleWarning);

    expect(rig.safety.idlePrompt, isNotNull);
    expect(
      rig.safety.idlePrompt!.remaining(DateTime.now()).inSeconds,
      inInclusiveRange(43, 45),
    );
    rig.dispose();
  });

  test('notifications only appear when the app is in the background', () async {
    final foreground = _Rig(foreground: true);
    await foreground.boot();
    await foreground.startWalk();
    await foreground.alert(AlertType.idleWarning);
    expect(foreground.notifier.shown, isEmpty);
    foreground.dispose();

    final background = _Rig(foreground: false);
    await background.boot();
    await background.startWalk();
    await background.alert(AlertType.idleWarning);
    await background.alert(AlertType.routeDeviation);
    expect(background.notifier.shown, [
      'Are you safe?',
      "You're off your route",
    ]);
    background.dispose();
  });

  test("I'm OK clears the prompt after the server accepts it", () async {
    final rig = _Rig();
    await rig.boot();
    await rig.startWalk();
    await rig.alert(AlertType.idleWarning);

    final ok = await rig.container.read(safetyProvider.notifier).imOk();

    expect(ok, isTrue);
    expect(rig.api.resolves, 1);
    expect(rig.safety.idlePrompt, isNull);
    rig.dispose();
  });

  test(
    "I'm OK keeps the prompt and shows the error if the server refuses",
    () async {
      final rig = _Rig();
      await rig.boot();
      await rig.startWalk();
      await rig.alert(AlertType.idleWarning);
      rig.api.failResolve = const ApiException(500, 'Server error');

      final ok = await rig.container.read(safetyProvider.notifier).imOk();

      expect(ok, isFalse);
      expect(rig.safety.idlePrompt, isNotNull);
      expect(rig.safety.error, 'Server error');
      rig.dispose();
    },
  );

  test('a second idle warning does not restart the countdown', () async {
    final rig = _Rig();
    await rig.boot();
    await rig.startWalk();
    await rig.alert(AlertType.idleWarning);
    final first = rig.safety.idlePrompt;
    await rig.alert(AlertType.idleWarning);
    expect(identical(rig.safety.idlePrompt, first), isTrue);
    rig.dispose();
  });

  test('a route deviation shows the banner, which can be dismissed', () async {
    final rig = _Rig();
    await rig.boot();
    await rig.startWalk();

    await rig.alert(AlertType.routeDeviation);
    expect(rig.safety.offRoute, isTrue);

    rig.container.read(safetyProvider.notifier).dismissOffRoute();
    expect(rig.safety.offRoute, isFalse);
    rig.dispose();
  });

  test('the server poll is a backup: it raises and clears the prompt and the banner', () async {
    final rig = _Rig();
    await rig.boot();
    await rig.startWalk();

    rig.walk.set(
      WalkState(
        phase: WalkPhase.active,
        session: _session(),
        latest: _session(alarm: true, deviation: true),
      ),
    );
    await rig.pump();
    expect(rig.safety.idlePrompt, isNotNull);
    expect(rig.safety.offRoute, isTrue);

    rig.walk.set(
      WalkState(
        phase: WalkPhase.active,
        session: _session(),
        latest: _session(),
      ),
    );
    await rig.pump();
    expect(rig.safety.idlePrompt, isNull);
    expect(rig.safety.offRoute, isFalse);
    rig.dispose();
  });

  test('emergency alerts reach the walk, and arrival asks the server for the summary', () async {
    final rig = _Rig();
    await rig.boot();
    await rig.startWalk();

    await rig.alert(AlertType.emergencyTriggered);
    expect(rig.container.read(walkProvider).emergencyActive, isTrue);

    await rig.alert(AlertType.emergencyResolved);
    expect(rig.container.read(walkProvider).emergencyActive, isFalse);

    await rig.alert(AlertType.autocomplete);
    expect(rig.walk.refreshes, 1);
    rig.dispose();
  });

  test(
    'a short connection drop shows nothing, a long one shows the banner',
    () async {
      final rig = _Rig();
      await rig.boot();
      await rig.startWalk();
      rig.realtime.onConnection!(true);
      await rig.pump();

      rig.realtime.onConnection!(false);
      rig.realtime.onConnection!(true);
      await Future<void>.delayed(const Duration(milliseconds: 30));
      expect(rig.safety.connectionLost, isFalse);

      rig.realtime.onConnection!(false);
      await Future<void>.delayed(const Duration(milliseconds: 30));
      expect(rig.safety.connectionLost, isTrue);

      rig.realtime.onConnection!(true);
      await rig.pump();
      expect(rig.safety.connectionLost, isFalse);
      rig.dispose();
    },
  );
}

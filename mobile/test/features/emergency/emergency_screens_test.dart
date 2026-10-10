import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:latlong2/latlong.dart';
import 'package:safewalk_mobile/core/theme/theme.dart';
import 'package:safewalk_mobile/features/auth/domain/session_user.dart';
import 'package:safewalk_mobile/features/auth/presentation/state/session_controller.dart';
import 'package:safewalk_mobile/features/emergency/data/authority_repository.dart';
import 'package:safewalk_mobile/features/emergency/data/dialer.dart';
import 'package:safewalk_mobile/features/emergency/domain/authority_numbers.dart';
import 'package:safewalk_mobile/features/emergency/domain/emergency.dart';
import 'package:safewalk_mobile/features/emergency/presentation/state/emergency_controller.dart';
import 'package:safewalk_mobile/features/emergency/presentation/widgets/emergency_overlay.dart';
import 'package:safewalk_mobile/features/emergency/presentation/widgets/sos_button.dart';
import 'package:safewalk_mobile/features/walk_session/presentation/state/walk_controller.dart';

class _FakeSession extends SessionController {
  @override
  Future<SessionUser?> build() async => null;
}

class _FixedWalk extends WalkController {
  @override
  WalkState build() => const WalkState(
    phase: WalkPhase.active,
    emergencyActive: true,
    position: LatLng(47.49, 19.07),
  );
}

class _FixedEmergency extends EmergencyController {
  _FixedEmergency(this.initial);

  final EmergencyState initial;
  int resolves = 0;

  @override
  EmergencyState build() => initial;

  @override
  Future<bool> resolve() async {
    resolves++;
    return true;
  }
}

class _FakeDialer implements Dialer {
  final dialed = <String>[];

  @override
  Future<bool> dial(String number) async {
    dialed.add(number);
    return true;
  }
}

Emergency _emergency({
  List<NotifiedContact> contacts = const [
    NotifiedContact(name: 'Mum', phone: '+3630'),
  ],
}) => Emergency(
  id: 5,
  source: TriggerSource.idleTimeout,
  latitude: 47.49,
  longitude: 19.07,
  triggeredAt: '2026-10-10T10:00:00',
  resolved: false,
  contacts: contacts,
);

Widget _overlay(
  _FixedEmergency emergency,
  _FakeDialer dialer, {
  AuthorityNumbers? numbers,
}) => ProviderScope(
  overrides: [
    sessionProvider.overrideWith(_FakeSession.new),
    walkProvider.overrideWith(_FixedWalk.new),
    emergencyProvider.overrideWith(() => emergency),
    dialerProvider.overrideWithValue(dialer),
    authorityNumbersProvider.overrideWith(
      (ref, arg) async => numbers ?? AuthorityNumbers.fallback,
    ),
  ],
  child: MaterialApp(
    theme: buildTheme(downloadFont: false),
    home: const Scaffold(body: EmergencyOverlay()),
  ),
);

Future<void> _pump(WidgetTester tester, Widget widget) async {
  // A tall screen, so every button is on screen and can be tapped.
  await tester.binding.setSurfaceSize(const Size(800, 2400));
  addTearDown(() => tester.binding.setSurfaceSize(null));
  await tester.pumpWidget(widget);
}

const _uk = AuthorityNumbers(
  police: '999',
  ambulance: '998',
  general: '112',
  countryName: 'Hungary',
  countryCode: 'HU',
);

void main() {
  group('EmergencyOverlay', () {
    testWidgets(
      'says what happened, who was notified, and offers every number',
      (tester) async {
        final dialer = _FakeDialer();
        await _pump(
          tester,
          _overlay(
            _FixedEmergency(EmergencyState(emergency: _emergency())),
            dialer,
            numbers: _uk,
          ),
        );
        await tester.pumpAndSettle();

        expect(find.text('Emergency alert sent'), findsOneWidget);
        expect(find.textContaining('No movement was detected'), findsOneWidget);
        expect(find.text('Mum'), findsOneWidget);
        expect(find.text('Police'), findsOneWidget);
        expect(find.text('999'), findsOneWidget);
        expect(find.text('Ambulance'), findsOneWidget);
        expect(find.text('Emergency services'), findsOneWidget);
        expect(find.text('Numbers for Hungary.'), findsOneWidget);
      },
    );

    testWidgets('pressing a call button opens the dialer with that number', (
      tester,
    ) async {
      final dialer = _FakeDialer();
      await _pump(
        tester,
        _overlay(
          _FixedEmergency(EmergencyState(emergency: _emergency())),
          dialer,
          numbers: _uk,
        ),
      );
      await tester.pumpAndSettle();

      await tester.tap(find.text('Police'));
      await tester.pump();
      expect(dialer.dialed, ['999']);

      await tester.tap(find.text('Emergency services'));
      await tester.pump();
      expect(dialer.dialed, ['999', '112']);
    });

    testWidgets('with no contacts saved it says nobody was notified', (
      tester,
    ) async {
      await _pump(
        tester,
        _overlay(
          _FixedEmergency(
            EmergencyState(emergency: _emergency(contacts: const [])),
          ),
          _FakeDialer(),
          numbers: _uk,
        ),
      );
      await tester.pumpAndSettle();
      expect(find.textContaining('nobody was notified'), findsOneWidget);
    });

    testWidgets('112 is on screen even when the local numbers could not load', (
      tester,
    ) async {
      await _pump(
        tester,
        _overlay(
          _FixedEmergency(EmergencyState(emergency: _emergency())),
          _FakeDialer(),
        ),
      );
      await tester.pumpAndSettle();
      expect(find.text('112'), findsOneWidget);
      expect(
        find.textContaining('Local numbers could not be loaded'),
        findsOneWidget,
      );
      expect(find.text('Police'), findsNothing);
    });

    testWidgets('I am safe asks first, then resolves', (tester) async {
      final emergency = _FixedEmergency(
        EmergencyState(emergency: _emergency()),
      );
      await _pump(tester, _overlay(emergency, _FakeDialer(), numbers: _uk));
      await tester.pumpAndSettle();

      await tester.ensureVisible(find.text("I'm safe"));
      await tester.tap(find.text("I'm safe"));
      await tester.pumpAndSettle();
      expect(find.text('Are you safe?'), findsOneWidget);
      expect(
        emergency.resolves,
        0,
        reason: 'nothing happens until it is confirmed',
      );

      await tester.tap(find.text("Yes, I'm safe"));
      await tester.pumpAndSettle();
      expect(emergency.resolves, 1);
    });

    testWidgets(
      'while details load it shows a spinner but already offers help',
      (tester) async {
        await _pump(
          tester,
          _overlay(
            _FixedEmergency(const EmergencyState(loading: true)),
            _FakeDialer(),
          ),
        );
        await tester.pump();
        expect(find.byType(CircularProgressIndicator), findsWidgets);
        expect(find.text('Emergency services'), findsOneWidget);
      },
    );
  });

  group('SosButton', () {
    Widget app({
      required VoidCallback onTriggered,
      VoidCallback? onConfirm,
      bool busy = false,
    }) => MaterialApp(
      theme: buildTheme(downloadFont: false),
      home: Scaffold(
        body: Center(
          child: SosButton(
            onTriggered: onTriggered,
            onConfirmRequested: onConfirm ?? () {},
            busy: busy,
          ),
        ),
      ),
    );

    testWidgets('holding for one second sends the SOS', (tester) async {
      var sent = 0;
      await _pump(tester, app(onTriggered: () => sent++));

      final gesture = await tester.startGesture(
        tester.getCenter(find.byType(SosButton)),
      );
      await tester.pump(); // the hold animation starts on the first frame
      await tester.pump(const Duration(milliseconds: 500));
      expect(sent, 0);
      await tester.pump(const Duration(milliseconds: 600));
      expect(sent, 1);
      await gesture.up();
    });

    testWidgets('letting go early cancels it', (tester) async {
      var sent = 0;
      await _pump(tester, app(onTriggered: () => sent++));

      final gesture = await tester.startGesture(
        tester.getCenter(find.byType(SosButton)),
      );
      await tester.pump(); // the hold animation starts on the first frame
      await tester.pump(const Duration(milliseconds: 400));
      await gesture.up();
      await tester.pump(const Duration(seconds: 2));

      expect(sent, 0);
    });

    testWidgets('a single tap does nothing', (tester) async {
      var sent = 0;
      await _pump(tester, app(onTriggered: () => sent++));
      await tester.tap(find.byType(SosButton));
      await tester.pump(const Duration(seconds: 2));
      expect(sent, 0);
    });

    testWidgets('screen readers get a confirmation instead of a hold', (
      tester,
    ) async {
      var confirms = 0;
      await _pump(tester, app(onTriggered: () {}, onConfirm: () => confirms++));
      final handle = tester.ensureSemantics();
      tester.semantics.tap(find.semantics.byLabel(RegExp('SOS')));
      await tester.pump();
      expect(confirms, 1);
      handle.dispose();
    });

    testWidgets('it cannot be started again while one is being sent', (
      tester,
    ) async {
      var sent = 0;
      await _pump(tester, app(onTriggered: () => sent++, busy: true));
      final gesture = await tester.startGesture(
        tester.getCenter(find.byType(SosButton)),
      );
      await tester.pump(); // the hold animation starts on the first frame
      await tester.pump(const Duration(seconds: 2));
      expect(sent, 0);
      await gesture.up();
    });
  });
}

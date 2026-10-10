import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/notifications/alert_notifier.dart';
import 'package:safewalk_mobile/core/theme/theme.dart';
import 'package:safewalk_mobile/features/auth/domain/session_user.dart';
import 'package:safewalk_mobile/features/auth/presentation/state/session_controller.dart';
import 'package:safewalk_mobile/features/profile/data/profile_api.dart';
import 'package:safewalk_mobile/features/profile/domain/user_profile.dart';
import 'package:safewalk_mobile/features/settings/presentation/settings_screen.dart';

class _FakeSession extends SessionController {
  static int signOuts = 0;

  @override
  Future<SessionUser?> build() async =>
      const SessionUser(id: 7, email: 'elena@v.com', roles: ['ROLE_USER']);

  @override
  Future<void> logout() async => signOuts++;
}

Future<void> _pump(WidgetTester tester, {required bool notificationsOn}) async {
  _FakeSession.signOuts = 0;
  await tester.binding.setSurfaceSize(const Size(800, 2000));
  addTearDown(() => tester.binding.setSurfaceSize(null));
  await tester.pumpWidget(
    ProviderScope(
      overrides: [
        sessionProvider.overrideWith(_FakeSession.new),
        profileProvider.overrideWith(
          (ref) async => const UserProfile(
            id: 7,
            email: 'elena@v.com',
            firstName: 'Elena',
            lastName: 'Vance',
            phoneNumber: '+3630',
          ),
        ),
        notificationsEnabledProvider.overrideWith(
          (ref) async => notificationsOn,
        ),
      ],
      child: MaterialApp(
        theme: buildTheme(downloadFont: false),
        home: const SettingsScreen(),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('shows the account, notification state and version', (
    tester,
  ) async {
    await _pump(tester, notificationsOn: true);
    expect(find.text('Elena Vance'), findsOneWidget);
    expect(find.textContaining('elena@v.com'), findsOneWidget);
    expect(find.textContaining('On. Safety alerts reach you'), findsOneWidget);
    expect(find.text('Version 1.0.0'), findsOneWidget);
  });

  testWidgets('says plainly when notifications are off', (tester) async {
    await _pump(tester, notificationsOn: false);
    expect(
      find.textContaining("Off. You won't see safety alerts"),
      findsOneWidget,
    );
  });

  testWidgets('signing out asks first', (tester) async {
    await _pump(tester, notificationsOn: true);

    await tester.ensureVisible(find.text('Sign out'));
    await tester.tap(find.text('Sign out'));
    await tester.pumpAndSettle();
    expect(find.text('Sign out?'), findsOneWidget);
    expect(_FakeSession.signOuts, 0);

    await tester.tap(find.widgetWithText(FilledButton, 'Sign out'));
    await tester.pumpAndSettle();
    expect(_FakeSession.signOuts, 1);
  });

  testWidgets('staying signed in does nothing', (tester) async {
    await _pump(tester, notificationsOn: true);
    await tester.ensureVisible(find.text('Sign out'));
    await tester.tap(find.text('Sign out'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Stay signed in'));
    await tester.pumpAndSettle();
    expect(_FakeSession.signOuts, 0);
  });
}

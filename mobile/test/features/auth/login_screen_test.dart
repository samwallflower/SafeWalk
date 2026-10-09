import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/theme/theme.dart';
import 'package:safewalk_mobile/features/auth/presentation/login_screen.dart';
import 'package:safewalk_mobile/features/auth/presentation/widgets/demo_login_buttons.dart';

Widget _app(List<DemoAccount> accounts) => ProviderScope(
  child: MaterialApp(
    theme: buildTheme(downloadFont: false),
    home: LoginScreen(demoAccounts: accounts),
  ),
);

void main() {
  const accounts = [
    DemoAccount(
      label: 'Use demo user',
      email: 'user@demo.test',
      password: 'Password1',
    ),
    DemoAccount(
      label: 'Use demo admin',
      email: 'admin@demo.test',
      password: 'Password2',
    ),
  ];

  testWidgets('demo buttons fill the fields and do not submit', (tester) async {
    await tester.pumpWidget(_app(accounts));
    await tester.tap(find.text('Use demo admin'));
    await tester.pump();

    expect(find.text('admin@demo.test'), findsOneWidget);
    // Still on the login screen, nothing was sent, no error shown.
    expect(find.text('Sign in'), findsOneWidget);
    expect(find.byType(CircularProgressIndicator), findsNothing);
  });

  testWidgets('hides the demo section when no accounts are configured', (
    tester,
  ) async {
    await tester.pumpWidget(_app(const []));
    expect(find.text('DEMO ACCOUNTS FOR EVALUATION'), findsNothing);
  });

  testWidgets('shows validation messages before calling the server', (
    tester,
  ) async {
    await tester.pumpWidget(_app(const []));
    await tester.tap(find.widgetWithText(FilledButton, 'Sign in'));
    await tester.pump();

    expect(find.text('Email is required'), findsOneWidget);
    expect(find.text('Password is required'), findsOneWidget);
  });
}

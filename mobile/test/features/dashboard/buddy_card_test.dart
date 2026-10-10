import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/theme/theme.dart';
import 'package:safewalk_mobile/features/auth/presentation/login_screen.dart';
import 'package:safewalk_mobile/features/dashboard/presentation/widgets/buddy_card.dart';

void main() {
  testWidgets('the buddy card invites you to start a walk', (tester) async {
    await tester.pumpWidget(
      ProviderScope(
        child: MaterialApp(
          theme: buildTheme(downloadFont: false),
          home: const Scaffold(body: BuddyCard()),
        ),
      ),
    );
    expect(find.text('Walk with SafeWalk'), findsOneWidget);
    expect(find.text('Start a walk'), findsOneWidget);
  });

  testWidgets('the login screen still fits at the largest text size', (
    tester,
  ) async {
    tester.view.physicalSize = const Size(1080, 2200);
    tester.view.devicePixelRatio = 3;
    addTearDown(tester.view.reset);
    await tester.pumpWidget(
      ProviderScope(
        child: MaterialApp(
          theme: buildTheme(downloadFont: false),
          builder: (context, child) => MediaQuery(
            data: MediaQuery.of(context)
                .copyWith(textScaler: const TextScaler.linear(2)),
            child: child!,
          ),
          home: const LoginScreen(demoAccounts: []),
        ),
      ),
    );
    expect(tester.takeException(), isNull);
  });
}

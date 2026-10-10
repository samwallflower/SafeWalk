import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/theme/colors.dart';
import 'package:safewalk_mobile/core/theme/theme.dart';
import 'package:safewalk_mobile/core/widgets/inline_notice.dart';
import 'package:safewalk_mobile/features/safety/domain/idle_prompt.dart';
import 'package:safewalk_mobile/features/safety/presentation/widgets/idle_prompt_overlay.dart';
import 'package:safewalk_mobile/features/safety/presentation/widgets/off_route_banner.dart';

Widget _app(Widget child) => MaterialApp(
  theme: buildTheme(downloadFont: false),
  home: Scaffold(body: child),
);

void main() {
  testWidgets(
    'the idle prompt asks if you are safe and answers with one big button',
    (tester) async {
      var answered = 0;
      final prompt = IdlePrompt(
        deadline: DateTime.now().add(const Duration(seconds: 40)),
      );
      await tester.pumpWidget(
        _app(IdlePromptOverlay(prompt: prompt, onImOk: () async => answered++)),
      );

      expect(find.text('Are you safe?'), findsOneWidget);
      expect(
        find.text('No movement detected. Please confirm you are okay.'),
        findsOneWidget,
      );
      expect(find.text("I'm OK"), findsOneWidget);

      await tester.tap(find.text("I'm OK"));
      await tester.pump();
      expect(answered, 1);
      // The countdown timer is cancelled when the widget goes away.
      await tester.pumpWidget(const SizedBox());
    },
  );

  testWidgets('after the countdown it says the contacts are being alerted', (
    tester,
  ) async {
    final prompt = IdlePrompt(
      deadline: DateTime.now().subtract(const Duration(seconds: 1)),
    );
    await tester.pumpWidget(
      _app(IdlePromptOverlay(prompt: prompt, onImOk: () async {})),
    );
    expect(
      find.textContaining('alerting your emergency contacts'),
      findsOneWidget,
    );
    expect(find.text('00:00'), findsOneWidget);
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets('the off-route banner can be dismissed', (tester) async {
    var dismissed = 0;
    await tester.pumpWidget(_app(OffRouteBanner(onDismiss: () => dismissed++)));
    expect(find.text("You're off your route"), findsOneWidget);
    await tester.tap(find.text("I'm OK"));
    expect(dismissed, 1);
  });

  testWidgets('warning notices are amber', (tester) async {
    await tester.pumpWidget(
      _app(const InlineNotice('Careful', tone: NoticeTone.warning)),
    );
    final text = tester.widget<Text>(find.text('Careful'));
    expect(text.style!.color, AppColors.warning);
  });
}

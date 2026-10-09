import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/theme/colors.dart';
import 'package:safewalk_mobile/core/widgets/inline_notice.dart';

Color _textColor(WidgetTester tester, String text) =>
    tester.widget<Text>(find.text(text)).style!.color!;

void main() {
  testWidgets('success notices are green and errors are red', (tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Column(
          children: [
            InlineNotice('Email verified', tone: NoticeTone.success),
            InlineNotice('Bad credentials'),
          ],
        ),
      ),
    );
    expect(_textColor(tester, 'Email verified'), AppColors.success);
    expect(_textColor(tester, 'Bad credentials'), AppColors.destructive);
  });
}

import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/report/domain/report_rules.dart';

void main() {
  test('description is required and capped at 280', () {
    expect(ReportRules.description('  '), 'Describe what you observed');
    expect(ReportRules.description('x' * 281), 'Keep it under 280 characters');
    expect(ReportRules.description('x' * 280), isNull);
  });

  test('coordinates follow the backend ranges', () {
    expect(ReportRules.latitude(91), 'Latitude must be between -90 and 90');
    expect(ReportRules.latitude(52.95), isNull);
    expect(
      ReportRules.longitude(-181),
      'Longitude must be between -180 and 180',
    );
    expect(ReportRules.longitude(-1.15), isNull);
  });
}

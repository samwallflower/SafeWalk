import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/categories/domain/incident_category.dart';
import 'package:safewalk_mobile/features/report/data/report_api.dart';

void main() {
  test('the request body mirrors AddIncidentReportRequest', () {
    const report = NewReport(
      description: 'Broken streetlight',
      latitude: 52.95,
      longitude: -1.15,
      isAnonymous: true,
      category: IncidentCategory(
        id: 3,
        name: 'Poor lighting',
        severityWeight: 2,
      ),
    );
    expect(report.toJson(), {
      'description': 'Broken streetlight',
      'latitude': 52.95,
      'longitude': -1.15,
      'isAnonymous': true,
      'category': {'id': 3, 'name': 'Poor lighting'},
    });
  });
}

import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/categories/domain/incident_category.dart';
import 'package:safewalk_mobile/features/incidents/domain/incident.dart';
import 'package:safewalk_mobile/features/map/domain/filter_incidents.dart';
import 'package:safewalk_mobile/features/map/domain/map_models.dart';

Incident _incident(int id, int categoryId, String timestamp) => Incident(
  id: id,
  isAnonymous: true,
  reporterName: null,
  description: 'x',
  latitude: 52.95,
  longitude: -1.15,
  timestamp: timestamp,
  upvotes: 0,
  downvotes: 0,
  category: IncidentCategory(
    id: categoryId,
    name: 'c$categoryId',
    severityWeight: 1,
  ),
  status: ReportStatus.active,
);

void main() {
  final now = DateTime(2026, 10, 9, 12);
  final items = [
    _incident(1, 1, '2026-10-09T11:00:00'),
    _incident(2, 2, '2026-10-05T12:00:00'),
    _incident(3, 1, '2026-08-01T12:00:00'),
  ];

  test('no filters keep everything', () {
    expect(filterIncidents(items, const MapFilters(), now: now), hasLength(3));
  });

  test('filters by category', () {
    final result = filterIncidents(
      items,
      const MapFilters(categoryId: 1),
      now: now,
    );
    expect(result.map((i) => i.id), [1, 3]);
  });

  test('filters by time window', () {
    final day = filterIncidents(
      items,
      const MapFilters(range: TimeRange.day),
      now: now,
    );
    expect(day.map((i) => i.id), [1]);
    final week = filterIncidents(
      items,
      const MapFilters(range: TimeRange.week),
      now: now,
    );
    expect(week.map((i) => i.id), [1, 2]);
  });

  test('combines category and time', () {
    final result = filterIncidents(
      items,
      const MapFilters(categoryId: 1, range: TimeRange.month),
      now: now,
    );
    expect(result.map((i) => i.id), [1]);
  });
}

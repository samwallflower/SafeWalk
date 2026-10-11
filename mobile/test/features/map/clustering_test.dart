import 'package:flutter_test/flutter_test.dart';
import 'package:latlong2/latlong.dart';
import 'package:safewalk_mobile/features/categories/domain/incident_category.dart';
import 'package:safewalk_mobile/features/incidents/domain/incident.dart';
import 'package:safewalk_mobile/features/map/domain/clustering.dart';
import 'package:safewalk_mobile/features/map/domain/map_models.dart';

Incident _at(
  int id,
  double lat,
  double lng, {
  String time = '2026-10-09T10:00:00',
}) => Incident(
  id: id,
  isAnonymous: true,
  reporterName: null,
  description: '',
  latitude: lat,
  longitude: lng,
  timestamp: time,
  upvotes: 0,
  downvotes: 0,
  category: const IncidentCategory(id: 1, name: 'c', severityWeight: 1),
  status: ReportStatus.active,
);

void main() {
  const bounds = MapBounds(
    west: -1.16,
    south: 52.95,
    east: -1.15,
    north: 52.96,
  );

  test('reports at the same place share one spot and none is lost', () {
    final spots = groupBySpot([
      for (var i = 0; i < 5; i++) _at(i, 52.955, -1.155),
      _at(9, 52.956, -1.155),
    ]);
    expect(spots, hasLength(2));
    expect(spots.map((s) => s.incidents.length).toList()..sort(), [1, 5]);
  });

  test(
    'a spot sits exactly where its reports were made and lists newest first',
    () {
      final spot = groupBySpot([
        _at(1, 52.955, -1.155, time: '2026-10-09T08:00:00'),
        _at(2, 52.955, -1.155, time: '2026-10-09T18:00:00'),
      ]).single;
      expect(spot.position, const LatLng(52.955, -1.155));
      expect(spot.incidents.map((i) => i.id), [2, 1]);
    },
  );

  test('nearestSpots keeps the closest and caps the count', () {
    final spots = groupBySpot([
      _at(1, 52.9, -1.1),
      _at(2, 52.9548, -1.1581),
      _at(3, 52.9549, -1.1582),
    ]);
    final nearest = nearestSpots(spots, const LatLng(52.9548, -1.1581), 2);
    expect(nearest, hasLength(2));
    expect(nearest.first.incidents.first.id, 2);
  });

  test('reportsInBounds counts every report at the spots on screen', () {
    final spots = groupBySpot([
      for (var i = 0; i < 4; i++) _at(i, 52.955, -1.155),
      _at(9, 52.97, -1.155),
    ]);
    expect(reportsInBounds(spots, bounds), 4);
  });

  test('incidentsInBounds counts only what is on screen', () {
    final inside = _at(1, 52.955, -1.155);
    final outside = _at(2, 52.97, -1.155);
    expect(incidentsInBounds([inside, outside], bounds).map((i) => i.id), [1]);
  });
}

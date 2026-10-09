import 'package:flutter_test/flutter_test.dart';
import 'package:latlong2/latlong.dart';
import 'package:safewalk_mobile/features/categories/domain/incident_category.dart';
import 'package:safewalk_mobile/features/incidents/domain/incident.dart';
import 'package:safewalk_mobile/features/map/domain/sampling.dart';

Incident _at(int id, double lat) => Incident(
  id: id,
  isAnonymous: true,
  reporterName: null,
  description: '',
  latitude: lat,
  longitude: 0,
  timestamp: '2026-10-09T10:00:00',
  upvotes: 0,
  downvotes: 0,
  category: const IncidentCategory(id: 1, name: 'c', severityWeight: 1),
  status: ReportStatus.active,
);

void main() {
  test('sample keeps short lists and thins long ones evenly', () {
    expect(sample([1, 2, 3], 10), [1, 2, 3]);
    final thinned = sample(List.generate(100, (i) => i), 10);
    expect(thinned, hasLength(10));
    expect(thinned.first, 0);
    expect(thinned.last, greaterThan(80));
  });

  test('nearestIncidents returns the closest first and caps the count', () {
    final list = [_at(1, 0.5), _at(2, 0.01), _at(3, 0.2)];
    final result = nearestIncidents(list, const LatLng(0, 0), 2);
    expect(result.map((i) => i.id), [2, 3]);
  });
}

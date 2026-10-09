import 'package:flutter_test/flutter_test.dart';
import 'package:latlong2/latlong.dart';
import 'package:safewalk_mobile/features/categories/domain/incident_category.dart';
import 'package:safewalk_mobile/features/incidents/domain/incident.dart';
import 'package:safewalk_mobile/features/map/domain/clustering.dart';
import 'package:safewalk_mobile/features/map/domain/map_models.dart';

Incident _at(int id, double lat, double lng) => Incident(
  id: id,
  isAnonymous: true,
  reporterName: null,
  description: '',
  latitude: lat,
  longitude: lng,
  timestamp: '2026-10-09T10:00:00',
  upvotes: 0,
  downvotes: 0,
  category: const IncidentCategory(id: 1, name: 'c', severityWeight: 1),
  status: ReportStatus.active,
);

void main() {
  test('reports a few meters apart become one dot with a count', () {
    final clusters = clusterIncidents([
      _at(1, 52.9548, -1.1581),
      _at(2, 52.95481, -1.15811),
      _at(3, 52.95479, -1.15809),
    ], zoom: 16);
    expect(clusters, hasLength(1));
    expect(clusters.single.count, 3);
  });

  test('reports far apart stay separate', () {
    final clusters = clusterIncidents([
      _at(1, 52.9548, -1.1581),
      _at(2, 52.9648, -1.1581),
    ], zoom: 16);
    expect(clusters, hasLength(2));
  });

  test('zooming out merges dots that were separate when zoomed in', () {
    final points = [
      _at(1, 52.9548, -1.1581),
      _at(2, 52.9551, -1.1581),
    ]; // about 33 m apart
    expect(clusterIncidents(points, zoom: 18), hasLength(2));
    expect(clusterIncidents(points, zoom: 14), hasLength(1));
  });

  test('the same incidents are all accounted for', () {
    final list = [
      for (var i = 0; i < 20; i++) _at(i, 52.95 + i * 0.00002, -1.15),
    ];
    final clusters = clusterIncidents(list, zoom: 16);
    expect(clusters.fold<int>(0, (sum, c) => sum + c.count), 20);
  });

  test('incidentsInBounds counts only what is on screen', () {
    const bounds = MapBounds(
      west: -1.16,
      south: 52.95,
      east: -1.15,
      north: 52.96,
    );
    final inside = _at(1, 52.955, -1.155);
    final outside = _at(2, 52.97, -1.155);
    expect(incidentsInBounds([inside, outside], bounds).map((i) => i.id), [1]);
    expect(bounds.contains(const LatLng(52.955, -1.155)), isTrue);
  });
}

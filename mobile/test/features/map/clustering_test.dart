import 'package:flutter_test/flutter_test.dart';
import 'package:latlong2/latlong.dart';
import 'package:safewalk_mobile/core/geo/haversine.dart';
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
  test('every incident keeps its own dot', () {
    final list = [
      for (var i = 0; i < 12; i++) _at(i, 52.95 + (i % 3) * 0.00001, -1.15),
    ];
    final placed = spreadOverlapping(list, zoom: 16);
    expect(placed, hasLength(12));
    expect({for (final p in placed) p.incident.id}, hasLength(12));
  });

  test('a lone incident is drawn exactly where it is', () {
    final placed = spreadOverlapping([_at(1, 52.9548, -1.1581)], zoom: 16);
    expect(placed.single.position, const LatLng(52.9548, -1.1581));
  });

  test('stacked incidents are fanned out so each dot has its own spot', () {
    final stacked = [
      _at(1, 52.9548, -1.1581),
      _at(2, 52.9548, -1.1581),
      _at(3, 52.9548, -1.1581),
    ];
    final placed = spreadOverlapping(stacked, zoom: 17);
    final positions = {
      for (final p in placed)
        '${p.position.latitude.toStringAsFixed(7)},${p.position.longitude.toStringAsFixed(7)}',
    };
    expect(positions, hasLength(3));
  });

  test('spread dots stay close to the original spot', () {
    const origin = LatLng(52.9548, -1.1581);
    final placed = spreadOverlapping([
      for (var i = 0; i < 5; i++) _at(i, origin.latitude, origin.longitude),
    ], zoom: 17);
    expect(
      placed.map((p) => p.position).toSet(),
      hasLength(5),
      reason: 'every dot has its own spot',
    );
    for (final p in placed) {
      final pixels =
          haversineMeters(origin, p.position) /
          metersPerPixel(origin.latitude, 17);
      expect(pixels, lessThanOrEqualTo(100));
    }
  });

  test('dots that are far apart are not moved', () {
    final far = [_at(1, 52.9548, -1.1581), _at(2, 52.9648, -1.1581)];
    final placed = spreadOverlapping(far, zoom: 16);
    expect(placed[0].position, const LatLng(52.9548, -1.1581));
    expect(placed[1].position, const LatLng(52.9648, -1.1581));
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

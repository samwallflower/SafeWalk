import 'package:flutter_test/flutter_test.dart';
import 'package:latlong2/latlong.dart';
import 'package:safewalk_mobile/features/map/domain/area.dart';
import 'package:safewalk_mobile/features/map/domain/map_models.dart';

const _bounds = MapBounds(west: -1.17, south: 52.94, east: -1.14, north: 52.97);

void main() {
  test(
    'the viewport circle is centered on the view and reaches the corners',
    () {
      final circle = viewportCircle(_bounds);
      expect(circle.center.latitude, closeTo(52.955, 1e-9));
      expect(circle.center.longitude, closeTo(-1.155, 1e-9));
      expect(circle.radiusMeters, greaterThan(1000));
      expect(circle.radiusMeters, lessThan(3000));
    },
  );

  test('padding grows the radius', () {
    final view = viewportCircle(_bounds);
    expect(padCircle(view).radiusMeters, greaterThan(view.radiusMeters));
  });

  test('nothing is fetched when the view is larger than the limit', () {
    final view = GeoCircle(const LatLng(52.95, -1.15), 40000);
    expect(nextFetchArea(null, view, 30000), isNull);
  });

  test('a small pan inside the fetched area does not refetch', () {
    final view = viewportCircle(_bounds);
    final first = nextFetchArea(null, view, 30000)!;
    final panned = GeoCircle(
      LatLng(view.center.latitude + 0.001, view.center.longitude),
      view.radiusMeters,
    );
    expect(nextFetchArea(first, panned, 30000), same(first));
  });

  test('moving outside the fetched area fetches a new one', () {
    final view = viewportCircle(_bounds);
    final first = nextFetchArea(null, view, 30000)!;
    final moved = GeoCircle(
      LatLng(view.center.latitude + 0.2, view.center.longitude),
      view.radiusMeters,
    );
    final next = nextFetchArea(first, moved, 30000)!;
    expect(next, isNot(first));
  });

  test('the fetched radius never exceeds the limit', () {
    final view = GeoCircle(const LatLng(52.95, -1.15), 2900);
    expect(nextFetchArea(null, view, 3000)!.radiusMeters, 3000);
  });

  test('rounded areas share a key', () {
    final a = roundedArea(
      GeoCircle(const LatLng(52.954801, -1.158102), 1500.4),
    );
    final b = roundedArea(
      GeoCircle(const LatLng(52.954803, -1.158099), 1499.6),
    );
    expect(a, b);
  });
}

import 'package:flutter_test/flutter_test.dart';
import 'package:latlong2/latlong.dart';
import 'package:safewalk_mobile/features/walk_session/domain/route_distance.dart';

const _route = [
  LatLng(52.9500, -1.1500),
  LatLng(52.9500, -1.1400),
  LatLng(52.9600, -1.1400),
];

void main() {
  test('a point on the route is at distance zero', () {
    expect(
      distanceToRouteMeters(const LatLng(52.9500, -1.1450), _route),
      closeTo(0, 0.5),
    );
  });

  test('a point beside the route is measured to the nearest segment', () {
    // 0.001 degrees of latitude is about 111 m.
    final d = distanceToRouteMeters(const LatLng(52.9510, -1.1450), _route);
    expect(d, closeTo(111, 3));
  });

  test('past the end of the route it is measured to the end point', () {
    final d = distanceToRouteMeters(const LatLng(52.9610, -1.1400), _route);
    expect(d, closeTo(111, 3));
  });

  test('an empty route is infinitely far', () {
    expect(
      distanceToRouteMeters(const LatLng(0, 0), const []),
      double.infinity,
    );
  });

  test('starting needs you to be within 100 m of the route', () {
    expect(
      isNearRoute(const LatLng(52.9505, -1.1450), _route),
      isTrue,
    ); // about 55 m
    expect(
      isNearRoute(const LatLng(52.9520, -1.1450), _route),
      isFalse,
    ); // about 222 m
  });
}

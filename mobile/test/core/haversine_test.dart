import 'package:flutter_test/flutter_test.dart';
import 'package:latlong2/latlong.dart';
import 'package:safewalk_mobile/core/geo/haversine.dart';

void main() {
  test('zero distance to itself', () {
    expect(
      haversineMeters(const LatLng(52.95, -1.15), const LatLng(52.95, -1.15)),
      0,
    );
  });

  test('one degree of latitude is about 111 km', () {
    final d = haversineMeters(const LatLng(0, 0), const LatLng(1, 0));
    expect(d, closeTo(111195, 300));
  });
}

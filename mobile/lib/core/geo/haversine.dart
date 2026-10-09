import 'dart:math' as math;

import 'package:latlong2/latlong.dart';

const _earthRadiusMeters = 6371000.0;

double _rad(double degrees) => degrees * math.pi / 180;

/// Great-circle distance in meters.
double haversineMeters(LatLng a, LatLng b) {
  final dLat = _rad(b.latitude - a.latitude);
  final dLng = _rad(b.longitude - a.longitude);
  final h =
      math.pow(math.sin(dLat / 2), 2) +
      math.cos(_rad(a.latitude)) *
          math.cos(_rad(b.latitude)) *
          math.pow(math.sin(dLng / 2), 2);
  return 2 * _earthRadiusMeters * math.asin(math.min(1, math.sqrt(h)));
}

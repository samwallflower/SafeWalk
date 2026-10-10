import 'dart:math' as math;

import 'package:latlong2/latlong.dart';

const _metersPerDegreeLatitude = 111320.0;

/// Distance from [p] to the segment [a]-[b] in meters, on a local flat projection (fine for city-scale distances).
double distanceToSegmentMeters(LatLng p, LatLng a, LatLng b) {
  final cosLat = math.cos(p.latitude * math.pi / 180);
  double x(LatLng q) =>
      (q.longitude - p.longitude) * _metersPerDegreeLatitude * cosLat;
  double y(LatLng q) => (q.latitude - p.latitude) * _metersPerDegreeLatitude;

  final ax = x(a);
  final ay = y(a);
  final bx = x(b);
  final by = y(b);
  final dx = bx - ax;
  final dy = by - ay;
  final lengthSquared = dx * dx + dy * dy;
  // p is the origin of this projection.
  final t = lengthSquared == 0
      ? 0.0
      : (-(ax * dx + ay * dy) / lengthSquared).clamp(0.0, 1.0);
  final cx = ax + t * dx;
  final cy = ay + t * dy;
  return math.sqrt(cx * cx + cy * cy);
}

/// Distance from [p] to the nearest part of the route line. Infinity for an empty route.
double distanceToRouteMeters(LatLng p, List<LatLng> route) {
  if (route.isEmpty) return double.infinity;
  if (route.length == 1) {
    return distanceToSegmentMeters(p, route.first, route.first);
  }
  var best = double.infinity;
  for (var i = 0; i < route.length - 1; i++) {
    best = math.min(best, distanceToSegmentMeters(p, route[i], route[i + 1]));
  }
  return best;
}

/// You may start a walk only near its route: farther than this and the server would see you off-route at once.
const maxStartDistanceMeters = 100.0;

bool isNearRoute(LatLng p, List<LatLng> route) =>
    distanceToRouteMeters(p, route) <= maxStartDistanceMeters;

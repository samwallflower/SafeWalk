import 'dart:math' as math;

import 'package:latlong2/latlong.dart';

import '../../../core/geo/haversine.dart';
import '../../map/domain/map_models.dart';
import 'route_option.dart';

List<LatLng> allPoints(Iterable<DecodedRoute> routes) => [
  for (final r in routes) ...r.points,
];

/// A circle around every route, used to load the incidents lying under them.
GeoCircle? coveringCircle(List<LatLng> points) {
  if (points.isEmpty) return null;
  var west = double.infinity;
  var south = double.infinity;
  var east = -double.infinity;
  var north = -double.infinity;
  for (final p in points) {
    west = math.min(west, p.longitude);
    east = math.max(east, p.longitude);
    south = math.min(south, p.latitude);
    north = math.max(north, p.latitude);
  }
  final center = LatLng((south + north) / 2, (west + east) / 2);
  final radius = points.map((p) => haversineMeters(center, p)).reduce(math.max);
  return GeoCircle(center, radius.ceilToDouble() + 300);
}

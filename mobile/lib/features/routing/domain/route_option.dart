import 'package:flutter/painting.dart';
import 'package:latlong2/latlong.dart';

import '../../../core/geo/polyline.dart';

/// Mirrors RouteDto.java
class RouteOption {
  const RouteOption({
    required this.id,
    required this.polyline,
    required this.actualDistanceMeters,
    required this.safetyPenaltyMeters,
    required this.virtualDistanceMeters,
    required this.rank,
    required this.routeRequestId,
  });

  factory RouteOption.fromJson(Map<String, Object?> json) => RouteOption(
    id: (json['id'] as num).toInt(),
    polyline: json['polyline'] as String? ?? '',
    actualDistanceMeters:
        (json['actualDistanceMeters'] as num?)?.toDouble() ?? 0,
    safetyPenaltyMeters: (json['safetyPenaltyMeters'] as num?)?.toDouble() ?? 0,
    virtualDistanceMeters:
        (json['virtualDistanceMeters'] as num?)?.toDouble() ?? 0,
    rank: (json['rank'] as num?)?.toInt() ?? 0,
    routeRequestId: json['routeRequestId'] as String? ?? '',
  );

  final int id;
  final String polyline;
  final double actualDistanceMeters;
  final double safetyPenaltyMeters;
  final double virtualDistanceMeters;

  /// 1 = best (lowest virtual distance, which is distance plus the safety penalty).
  final int rank;
  final String routeRequestId;
}

const recommendedColor = Color(0xFF0A6B32);
const _alternativeColors = [
  Color(0xFF0B52B8),
  Color(0xFF9A6700),
  Color(0xFF5B6475),
  Color(0xFF7C3AED),
];

Color routeColor(int rank) => rank == 1
    ? recommendedColor
    : _alternativeColors[(rank - 2) % _alternativeColors.length];

class DecodedRoute {
  const DecodedRoute({
    required this.route,
    required this.points,
    required this.color,
  });

  final RouteOption route;
  final List<LatLng> points;
  final Color color;
}

/// Best first, with each polyline decoded once.
List<DecodedRoute> decodeRoutes(Iterable<RouteOption> routes) {
  final sorted = [...routes]..sort((a, b) => a.rank.compareTo(b.rank));
  return [
    for (final route in sorted)
      DecodedRoute(
        route: route,
        points: decodePolyline(route.polyline),
        color: routeColor(route.rank),
      ),
  ];
}

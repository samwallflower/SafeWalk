import 'dart:math' as math;

import 'package:latlong2/latlong.dart';

import '../../../core/geo/haversine.dart';
import 'map_config.dart';
import 'map_models.dart';

/// Smallest circle (centered on the view) that covers the whole viewport rectangle.
GeoCircle viewportCircle(MapBounds bounds) {
  final center = LatLng(
    (bounds.north + bounds.south) / 2,
    (bounds.east + bounds.west) / 2,
  );
  return GeoCircle(
    center,
    haversineMeters(center, LatLng(bounds.north, bounds.east)),
  );
}

GeoCircle padCircle(GeoCircle circle, [double factor = areaPadding]) =>
    GeoCircle(circle.center, (circle.radiusMeters * factor).ceilToDouble());

bool circleContains(GeoCircle outer, GeoCircle inner) =>
    haversineMeters(outer.center, inner.center) + inner.radiusMeters <=
    outer.radiusMeters;

/// Decides which area to fetch for the current viewport circle.
///
/// - `null`: the viewport is larger than [maxRadiusM], so nothing may be fetched
/// - [previous]: it still covers the view and is not wastefully large (no refetch)
/// - a new padded circle otherwise
GeoCircle? nextFetchArea(
  GeoCircle? previous,
  GeoCircle? view,
  double maxRadiusM,
) {
  if (view == null) return previous;
  if (view.radiusMeters > maxRadiusM) return null;
  if (previous != null &&
      circleContains(previous, view) &&
      previous.radiusMeters <=
          view.radiusMeters * areaPadding * areaMaxOversize) {
    return previous;
  }
  final padded = padCircle(view);
  return GeoCircle(padded.center, math.min(padded.radiusMeters, maxRadiusM));
}

/// Rounded so that areas which look equal share one request.
GeoCircle roundedArea(GeoCircle area) => GeoCircle(
  LatLng(
    double.parse(area.center.latitude.toStringAsFixed(5)),
    double.parse(area.center.longitude.toStringAsFixed(5)),
  ),
  area.radiusMeters.roundToDouble(),
);

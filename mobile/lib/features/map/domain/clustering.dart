import 'package:latlong2/latlong.dart';

import '../../../core/geo/haversine.dart';
import '../../incidents/domain/incident.dart';
import 'map_models.dart';

/// A place where one or more reports were made. Reports made at the same spot share one dot, and the dot opens
/// all of them. (The reports are never moved: the dot is exactly where they happened.)
class Spot {
  const Spot(this.position, this.incidents);

  final LatLng position;

  /// Newest first.
  final List<Incident> incidents;

  String get key => '${position.latitude},${position.longitude}';
}

/// Groups reports that share a place, to about one metre (five decimals).
List<Spot> groupBySpot(List<Incident> incidents) {
  final groups = <String, List<Incident>>{};
  for (final incident in incidents) {
    final key =
        '${incident.latitude.toStringAsFixed(5)},${incident.longitude.toStringAsFixed(5)}';
    (groups[key] ??= []).add(incident);
  }
  return [
    for (final list in groups.values)
      Spot(
        LatLng(list.first.latitude, list.first.longitude),
        [...list]..sort((a, b) => b.timestamp.compareTo(a.timestamp)),
      ),
  ];
}

/// The [max] spots closest to [center], nearest first.
List<Spot> nearestSpots(List<Spot> spots, LatLng center, int max) {
  final ranked = [...spots]
    ..sort(
      (a, b) => haversineMeters(
        center,
        a.position,
      ).compareTo(haversineMeters(center, b.position)),
    );
  return ranked.length <= max ? ranked : ranked.sublist(0, max);
}

/// How many reports lie inside the visible map, so "N incidents in view" counts every report, not just the dots.
int reportsInBounds(List<Spot> spots, MapBounds bounds) {
  var count = 0;
  for (final spot in spots) {
    if (bounds.contains(spot.position)) count += spot.incidents.length;
  }
  return count;
}

/// Only the incidents inside the visible map.
List<Incident> incidentsInBounds(List<Incident> incidents, MapBounds bounds) =>
    [
      for (final incident in incidents)
        if (bounds.contains(LatLng(incident.latitude, incident.longitude)))
          incident,
    ];

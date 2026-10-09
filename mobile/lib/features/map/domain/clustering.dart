import 'dart:math' as math;

import 'package:latlong2/latlong.dart';

import '../../../core/geo/haversine.dart';
import '../../incidents/domain/incident.dart';
import 'map_models.dart';

/// Incidents so close together on screen that their dots would sit on top of each other.
class IncidentCluster {
  IncidentCluster(Incident first)
    : incidents = [first],
      center = LatLng(first.latitude, first.longitude);

  final List<Incident> incidents;
  final LatLng center;

  int get count => incidents.length;
}

/// Meters covered by one screen pixel at [latitude] and [zoom] (256 px tiles).
double metersPerPixel(double latitude, double zoom) =>
    156543.03392 * math.cos(latitude * math.pi / 180) / math.pow(2, zoom);

/// Groups incidents whose dots would be within [pixelRadius] of each other at this zoom, so a stack of
/// reports at one spot shows as one dot with a number instead of hiding behind the top one.
List<IncidentCluster> clusterIncidents(
  List<Incident> incidents, {
  required double zoom,
  double pixelRadius = 22,
}) {
  final clusters = <IncidentCluster>[];
  for (final incident in incidents) {
    final point = LatLng(incident.latitude, incident.longitude);
    final limit = pixelRadius * metersPerPixel(incident.latitude, zoom);
    IncidentCluster? home;
    for (final cluster in clusters) {
      if (haversineMeters(cluster.center, point) <= limit) {
        home = cluster;
        break;
      }
    }
    if (home == null) {
      clusters.add(IncidentCluster(incident));
    } else {
      home.incidents.add(incident);
    }
  }
  return clusters;
}

/// Only the incidents inside the visible map, so "N incidents in view" matches what is on screen.
List<Incident> incidentsInBounds(List<Incident> incidents, MapBounds bounds) =>
    [
      for (final incident in incidents)
        if (bounds.contains(LatLng(incident.latitude, incident.longitude)))
          incident,
    ];

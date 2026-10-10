import 'dart:math' as math;

import 'package:latlong2/latlong.dart';

import '../../../core/geo/haversine.dart';
import '../../incidents/domain/incident.dart';
import 'map_models.dart';

/// An incident and where its dot is drawn. This only differs from the real position when several
/// incidents would sit on top of each other.
class PlacedIncident {
  const PlacedIncident(this.incident, this.position);

  final Incident incident;
  final LatLng position;
}

/// Meters covered by one screen pixel at [latitude] and [zoom] (256 px tiles).
double metersPerPixel(double latitude, double zoom) =>
    156543.03392 * math.cos(latitude * math.pi / 180) / math.pow(2, zoom);

const _metersPerDegreeLatitude = 111320.0;

/// Every incident keeps its own dot. Dots that would overlap on screen at this zoom are fanned out on a small ring
/// around their shared spot, so none of them is hidden behind another and each can still be tapped.
List<PlacedIncident> spreadOverlapping(
  List<Incident> incidents, {
  required double zoom,
  double pixelRadius = 22,
}) {
  final groups = <List<Incident>>[];
  final centers = <LatLng>[];
  for (final incident in incidents) {
    final point = LatLng(incident.latitude, incident.longitude);
    final limit = pixelRadius * metersPerPixel(incident.latitude, zoom);
    var placed = false;
    for (var i = 0; i < groups.length; i++) {
      if (haversineMeters(centers[i], point) <= limit) {
        groups[i].add(incident);
        placed = true;
        break;
      }
    }
    if (!placed) {
      groups.add([incident]);
      centers.add(point);
    }
  }

  final result = <PlacedIncident>[];
  for (var g = 0; g < groups.length; g++) {
    final group = groups[g];
    final center = centers[g];
    if (group.length == 1) {
      result.add(PlacedIncident(group.first, center));
      continue;
    }
    // Ring radius grows a little with the number of dots so they do not touch.
    final ringPixels = math.min(26.0, 11.0 + group.length * 2.5);
    final ringMeters = ringPixels * metersPerPixel(center.latitude, zoom);
    final dLat = ringMeters / _metersPerDegreeLatitude;
    final dLng =
        ringMeters /
        (_metersPerDegreeLatitude * math.cos(center.latitude * math.pi / 180));
    for (var i = 0; i < group.length; i++) {
      final angle = -math.pi / 2 + 2 * math.pi * i / group.length;
      result.add(
        PlacedIncident(
          group[i],
          LatLng(
            center.latitude - dLat * math.sin(angle),
            center.longitude + dLng * math.cos(angle),
          ),
        ),
      );
    }
  }
  return result;
}

/// Only the incidents inside the visible map, so "N incidents in view" matches what is on screen.
List<Incident> incidentsInBounds(List<Incident> incidents, MapBounds bounds) =>
    [
      for (final incident in incidents)
        if (bounds.contains(LatLng(incident.latitude, incident.longitude)))
          incident,
    ];

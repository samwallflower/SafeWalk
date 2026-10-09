import 'package:latlong2/latlong.dart';

import '../../../core/geo/haversine.dart';
import '../../incidents/domain/incident.dart';

/// Evenly thins a long list down to at most [max] items (keeps the spread, drops the density).
List<T> sample<T>(List<T> items, int max) {
  if (items.length <= max) return items;
  final step = items.length / max;
  return [for (var i = 0; i < max; i++) items[(i * step).floor()]];
}

/// The [max] incidents closest to [center], nearest first.
List<Incident> nearestIncidents(
  List<Incident> incidents,
  LatLng center,
  int max,
) {
  final ranked = [...incidents]
    ..sort(
      (a, b) => haversineMeters(
        center,
        LatLng(a.latitude, a.longitude),
      ).compareTo(haversineMeters(center, LatLng(b.latitude, b.longitude))),
    );
  return ranked.length <= max ? ranked : ranked.sublist(0, max);
}

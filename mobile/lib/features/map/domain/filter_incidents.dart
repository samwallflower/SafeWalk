import '../../incidents/domain/incident.dart';
import 'map_models.dart';

/// Timestamps are zone-less; they are compared as local time, the same way they are shown.
List<Incident> filterIncidents(
  Iterable<Incident> incidents,
  MapFilters filters, {
  DateTime? now,
}) {
  final current = now ?? DateTime.now();
  final window = filters.range.window;
  return [
    for (final incident in incidents)
      if ((filters.categoryId == null ||
              incident.category.id == filters.categoryId) &&
          (window == null ||
              (DateTime.tryParse(incident.timestamp) != null &&
                  current.difference(DateTime.parse(incident.timestamp)) <=
                      window)))
        incident,
  ];
}

import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../incidents/data/incidents_api.dart';
import '../../../incidents/domain/incident.dart';
import '../../domain/area.dart';
import '../../domain/map_models.dart';
import '../../domain/filter_incidents.dart';
import 'map_filters_controller.dart';
import 'map_view_controller.dart';

AreaQuery _query(GeoCircle area) {
  final rounded = roundedArea(area);
  return AreaQuery(
    latitude: rounded.center.latitude,
    longitude: rounded.center.longitude,
    radiusMeters: rounded.radiusMeters,
  );
}

/// Re-runs only when the fetch area changes; the previous points stay visible while it loads.
final heatPointsProvider = FutureProvider.autoDispose<List<HeatPoint>>((
  ref,
) async {
  final area = ref.watch(mapViewProvider.select((s) => s.heatArea));
  if (area == null) return const [];
  final cancel = CancelToken();
  ref.onDispose(cancel.cancel);
  return ref
      .watch(incidentsApiProvider)
      .heatmapPoints(_query(area), cancelToken: cancel);
});

final nearbyProvider = FutureProvider.autoDispose<List<Incident>>((ref) async {
  final area = ref.watch(mapViewProvider.select((s) => s.nearArea));
  if (area == null) return const [];
  final cancel = CancelToken();
  ref.onDispose(cancel.cancel);
  return ref
      .watch(incidentsApiProvider)
      .nearby(_query(area), cancelToken: cancel);
});

class MapData {
  const MapData({
    required this.zoomedOut,
    required this.filtersNeedZoom,
    required this.heatPoints,
    required this.incidents,
    required this.isLoading,
    required this.error,
  });

  /// The view is too large to request data safely.
  final bool zoomedOut;

  /// Filters are set but the view is too large to apply them.
  final bool filtersNeedZoom;
  final List<HeatPoint> heatPoints;

  /// Tappable incidents; empty unless zoomed in to street level.
  final List<Incident> incidents;
  final bool isLoading;
  final Object? error;
}

const _noHeat = <HeatPoint>[];
const _noIncidents = <Incident>[];

/// The same decisions as the web map: what to draw, given the view and the filters.
final mapDataProvider = Provider<MapData>((ref) {
  final view = ref.watch(mapViewProvider);
  final filters = ref.watch(mapFiltersProvider);
  final heat = ref.watch(heatPointsProvider);
  final near = ref.watch(nearbyProvider);

  final nearData = (view.wantsPoints || view.heatFromNearby)
      ? filterIncidents(near.value ?? _noIncidents, filters)
      : _noIncidents;
  final heatPoints = view.zoomedOut
      ? _noHeat
      : view.heatFromNearby
      ? [for (final incident in nearData) incident.toHeatPoint()]
      : (heat.value ?? _noHeat);

  final usesHeat = view.heatArea != null;
  final usesNear = view.nearArea != null;
  final failed = (usesHeat && heat.hasError)
      ? heat.error
      : ((usesNear && near.hasError) ? near.error : null);

  return MapData(
    zoomedOut: view.zoomedOut,
    filtersNeedZoom: view.filtersNeedZoom,
    heatPoints: heatPoints,
    incidents: (view.zoomedOut || !view.wantsPoints) ? _noIncidents : nearData,
    isLoading: (usesHeat && heat.isLoading) || (usesNear && near.isLoading),
    error: failed,
  );
});

/// Used by the "try again" button.
void retryMapData(WidgetRef ref) {
  ref.invalidate(heatPointsProvider);
  ref.invalidate(nearbyProvider);
}

import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

import '../../domain/area.dart';
import '../../domain/map_config.dart';
import '../../domain/map_models.dart';
import 'map_filters_controller.dart';

/// What the map decided to fetch for the (debounced) viewport.
class MapViewState {
  const MapViewState({
    this.viewport,
    this.heatArea,
    this.nearArea,
    this.zoomedOut = false,
    this.filtersNeedZoom = false,
    this.heatFromNearby = false,
    this.wantsPoints = false,
  });

  final MapViewport? viewport;

  /// Area for the lightweight heat points, or null when they are not wanted.
  final GeoCircle? heatArea;

  /// Area for full incident records, or null when they are not wanted.
  final GeoCircle? nearArea;
  final bool zoomedOut;
  final bool filtersNeedZoom;

  /// Filters are on and the view is small enough, so the dots are drawn from the filtered full records.
  final bool heatFromNearby;

  /// Zoomed in far enough for individual, tappable incidents.
  final bool wantsPoints;

  LatLng? get center => viewport?.center;
}

final mapViewProvider = NotifierProvider<MapViewController, MapViewState>(
  MapViewController.new,
);

/// Debounces camera moves and turns the settled viewport into fetch areas, so panning a little never refetches.
class MapViewController extends Notifier<MapViewState> {
  Timer? _timer;

  @override
  MapViewState build() {
    ref.onDispose(() => _timer?.cancel());
    ref.listen(mapFiltersProvider, (previous, next) {
      final viewport = state.viewport;
      if (viewport != null) _settle(viewport);
    });
    return const MapViewState();
  }

  void onCameraChanged(MapViewport viewport) {
    _timer?.cancel();
    _timer = Timer(moveDebounce, () => _settle(viewport));
  }

  void _settle(MapViewport viewport) {
    final filters = ref.read(mapFiltersProvider);
    final view = viewportCircle(viewport.bounds);
    final zoomedOut = view.radiusMeters > maxHeatmapRadiusM;
    final canFilter = view.radiusMeters <= maxFilterRadiusM;
    final heatFromNearby = filters.isActive && canFilter;
    final wantsPoints = view.radiusMeters <= maxPointsRadiusM;
    final wantsNear = !zoomedOut && (heatFromNearby || wantsPoints);

    state = MapViewState(
      viewport: viewport,
      zoomedOut: zoomedOut,
      filtersNeedZoom: filters.isActive && !canFilter && !zoomedOut,
      heatFromNearby: heatFromNearby,
      wantsPoints: wantsPoints,
      heatArea: (zoomedOut || heatFromNearby)
          ? null
          : nextFetchArea(state.heatArea, view, maxHeatmapRadiusM),
      nearArea: wantsNear
          ? nextFetchArea(
              state.nearArea,
              view,
              heatFromNearby ? maxFilterRadiusM : maxPointsRadiusM,
            )
          : null,
    );
  }
}

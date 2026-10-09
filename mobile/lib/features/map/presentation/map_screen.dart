import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

import '../../../core/config/env.dart';
import '../../../core/location/location_service.dart';
import '../../../core/widgets/error_state.dart';
import '../../auth/presentation/widgets/account_button.dart';
import '../../incidents/domain/incident.dart';
import '../../places/domain/place.dart';
import '../domain/clustering.dart';
import '../domain/map_config.dart';
import '../domain/map_models.dart';
import '../domain/sampling.dart';
import 'state/map_data_provider.dart';
import 'state/map_filters_controller.dart';
import 'state/map_focus.dart';
import 'state/map_view_controller.dart';
import 'widgets/filter_sheet.dart';
import 'widgets/incident_pager.dart';
import 'widgets/map_layers.dart';
import 'widgets/map_status_chip.dart';
import 'widgets/place_search_bar.dart';

const _tileUrl =
    'https://api.mapbox.com/styles/v1/mapbox/streets-v12/tiles/{z}/{x}/{y}?access_token=${Env.mapboxToken}';

class MapScreen extends ConsumerStatefulWidget {
  const MapScreen({super.key});

  @override
  ConsumerState<MapScreen> createState() => _MapScreenState();
}

class _MapScreenState extends ConsumerState<MapScreen> {
  final _map = MapController();
  List<Incident> _selection = const [];
  LatLng? _me;
  bool _locating = false;
  double _zoom = defaultMapZoom;

  void _reportCamera(MapCamera camera) {
    final b = camera.visibleBounds;
    _zoom = camera.zoom;
    ref
        .read(mapViewProvider.notifier)
        .onCameraChanged(
          MapViewport(
            bounds: MapBounds(
              west: b.west,
              south: b.south,
              east: b.east,
              north: b.north,
            ),
            zoom: camera.zoom,
          ),
        );
  }

  void _goTo(Place place) =>
      _map.move(LatLng(place.latitude, place.longitude), 15);

  void _select(IncidentCluster cluster) {
    // Newest first, so a fresh report is the first card you see at a busy spot.
    final ordered = [...cluster.incidents]
      ..sort((a, b) => b.timestamp.compareTo(a.timestamp));
    setState(() => _selection = ordered);
    _map.move(cluster.center, math.max(_zoom, 15));
  }

  Future<void> _locate() async {
    if (_locating) return;
    setState(() => _locating = true);
    final result = await const LocationService().current();
    if (!mounted) return;
    setState(() => _locating = false);
    final point = result.point;
    if (point != null) {
      setState(() => _me = point);
      _map.move(point, math.max(_zoom, 15));
      return;
    }
    final messenger = ScaffoldMessenger.of(context);
    messenger.hideCurrentSnackBar();
    final failure = result.failure;
    messenger.showSnackBar(
      SnackBar(
        content: Text(switch (failure) {
          LocationFailure.serviceDisabled =>
            'Turn on location services to find yourself on the map.',
          LocationFailure.denied =>
            'Location permission is needed to show where you are.',
          LocationFailure.deniedForever =>
            'Location is blocked. Allow it in the app settings.',
          _ => "Couldn't get your location. Try again.",
        }),
        action: failure == LocationFailure.deniedForever
            ? SnackBarAction(
                label: 'Settings',
                onPressed: () => const LocationService().openSettings(),
              )
            : null,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (Env.mapboxToken.isEmpty) {
      return const Scaffold(
        body: ErrorState(
          message: 'The map needs a Mapbox token. Add MAPBOX_TOKEN to env/dev.json and rebuild.',
        ),
      );
    }

    ref.listen(mapFocusProvider, (previous, next) {
      if (next == null) return;
      _map.move(next.point, next.zoom);
      ref.read(mapFocusProvider.notifier).clear();
    });

    final data = ref.watch(mapDataProvider);
    final view = ref.watch(mapViewProvider);
    final filters = ref.watch(mapFiltersProvider);
    final theme = Theme.of(context);
    final center = view.center ?? defaultMapCenter;

    final nearest = nearestIncidents(
      data.incidents,
      center,
      maxIncidentMarkers,
    );
    final clusters = clusterIncidents(
      nearest,
      zoom: view.viewport?.zoom ?? _zoom,
    );
    final inView = view.viewport == null
        ? nearest
        : incidentsInBounds(nearest, view.viewport!.bounds);
    final showMarkers = clusters.isNotEmpty;

    final String? status;
    IconData? statusIcon;
    if (data.zoomedOut) {
      status = 'Zoom in to see incidents';
      statusIcon = Icons.zoom_in;
    } else if (data.filtersNeedZoom) {
      status = 'Zoom in to apply filters';
      statusIcon = Icons.filter_alt_outlined;
    } else if (data.isLoading && data.heatPoints.isEmpty && nearest.isEmpty) {
      status = 'Loading incidents';
    } else if (view.wantsPoints) {
      status =
          '${inView.length} ${inView.length == 1 ? 'incident' : 'incidents'} in view';
      statusIcon = Icons.place_outlined;
    } else {
      status = '${data.heatPoints.length} reports in this area';
      statusIcon = Icons.place_outlined;
    }

    return Scaffold(
      body: Stack(
        children: [
          FlutterMap(
            mapController: _map,
            options: MapOptions(
              initialCenter: defaultMapCenter,
              initialZoom: defaultMapZoom,
              minZoom: 3,
              maxZoom: 19,
              interactionOptions: const InteractionOptions(
                flags: InteractiveFlag.all & ~InteractiveFlag.rotate,
              ),
              onMapReady: () => _reportCamera(_map.camera),
              onPositionChanged: (camera, hasGesture) => _reportCamera(camera),
              onTap: (tap, point) {
                if (_selection.isNotEmpty) {
                  setState(() => _selection = const []);
                }
              },
            ),
            children: [
              TileLayer(
                urlTemplate: _tileUrl,
                tileDimension: 512,
                zoomOffset: -1,
                userAgentPackageName: 'com.samwallflower.safewalk_mobile',
              ),
              if (!showMarkers && !data.zoomedOut)
                HeatDotsLayer(
                  points: sample(data.heatPoints, maxHeatDots),
                  zoom: _zoom,
                ),
              if (showMarkers)
                IncidentMarkersLayer(
                  clusters: clusters,
                  selectedIds: {for (final i in _selection) i.id},
                  onTap: _select,
                ),
              if (_me != null) MyLocationLayer(point: _me!),
              const RichAttributionWidget(
                alignment: AttributionAlignment.bottomLeft,
                attributions: [
                  TextSourceAttribution('© Mapbox © OpenStreetMap'),
                ],
              ),
            ],
          ),
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(12, 8, 12, 0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  PlaceSearchBar(
                    onSelected: _goTo,
                    trailing: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        IconButton(
                          tooltip: 'Filters',
                          icon: Badge(
                            isLabelVisible: filters.isActive,
                            child: const Icon(Icons.tune),
                          ),
                          onPressed: () => showFilterSheet(context),
                        ),
                        const AccountButton(),
                      ],
                    ),
                  ),
                  const SizedBox(height: 8),
                  if (data.error != null)
                    Material(
                      color: theme.colorScheme.errorContainer,
                      borderRadius: BorderRadius.circular(14),
                      child: Padding(
                        padding: const EdgeInsets.fromLTRB(12, 6, 4, 6),
                        child: Row(
                          children: [
                            Icon(
                              Icons.error_outline,
                              size: 18,
                              color: theme.colorScheme.error,
                            ),
                            const SizedBox(width: 8),
                            Expanded(
                              child: Text(
                                "Couldn't load incidents.",
                                style: TextStyle(
                                  color: theme.colorScheme.error,
                                ),
                              ),
                            ),
                            TextButton(
                              onPressed: () => retryMapData(ref),
                              child: const Text('Retry'),
                            ),
                          ],
                        ),
                      ),
                    )
                  else
                    Align(
                      alignment: Alignment.centerLeft,
                      child: MapStatusChip(
                        text: status,
                        icon: statusIcon,
                        loading: statusIcon == null,
                      ),
                    ),
                ],
              ),
            ),
          ),
          Positioned(
            right: 12,
            bottom: _selection.isEmpty
                ? 24
                : (_selection.length > 1 ? 300 : 250),
            child: FloatingActionButton.small(
              heroTag: 'locate',
              tooltip: 'My location',
              backgroundColor: theme.colorScheme.surface,
              foregroundColor: theme.colorScheme.primary,
              onPressed: _locate,
              child: _locating
                  ? const SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(strokeWidth: 2.5),
                    )
                  : const Icon(Icons.my_location),
            ),
          ),
          if (_selection.isNotEmpty)
            Positioned(
              left: 12,
              right: 12,
              bottom: 12,
              child: IncidentPager(
                incidents: _selection,
                onClose: () => setState(() => _selection = const []),
              ),
            ),
        ],
      ),
    );
  }
}

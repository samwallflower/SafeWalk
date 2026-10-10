import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

import '../../../core/config/env.dart';
import '../../../core/format/format.dart';
import '../../../core/location/location_service.dart';
import '../../walk_session/presentation/state/walk_controller.dart';
import '../../../core/widgets/error_state.dart';
import '../../../core/widgets/inline_notice.dart';
import '../../map/domain/map_config.dart';
import '../../map/domain/sampling.dart';
import '../../map/presentation/state/map_view_controller.dart';
import '../../map/presentation/widgets/map_layers.dart';
import '../../places/domain/place.dart';
import '../../places/presentation/place_field.dart';
import '../data/route_incidents_provider.dart';
import '../domain/route_geometry.dart';
import 'state/plan_controller.dart';
import 'widgets/route_map_layers.dart';
import 'widgets/route_results_sheet.dart';

const _tileUrl =
    'https://api.mapbox.com/styles/v1/mapbox/streets-v12/tiles/{z}/{x}/{y}?access_token=${Env.mapboxToken}';

class PlanScreen extends ConsumerStatefulWidget {
  const PlanScreen({super.key});

  @override
  ConsumerState<PlanScreen> createState() => _PlanScreenState();
}

class _PlanScreenState extends ConsumerState<PlanScreen> {
  final _map = MapController();
  bool _locating = false;
  bool _starting = false;

  @override
  void initState() {
    super.initState();
    _prefillOrigin();
  }

  /// Start from where you are, but only if location is already allowed: opening a tab should never pop a prompt.
  Future<void> _prefillOrigin() async {
    const service = LocationService();
    if (ref.read(planProvider).origin != null ||
        !await service.hasPermission()) {
      return;
    }
    final result = await service.current();
    final point = result.point;
    if (!mounted || point == null || ref.read(planProvider).origin != null) {
      return;
    }
    ref.read(planProvider.notifier).setOrigin(_myLocation(point));
  }

  Place _myLocation(LatLng point) => Place(
    label: 'My location',
    latitude: point.latitude,
    longitude: point.longitude,
  );

  Future<void> _useMyLocation() async {
    if (_locating) return;
    setState(() => _locating = true);
    final result = await const LocationService().current();
    if (!mounted) return;
    setState(() => _locating = false);
    final point = result.point;
    if (point != null) {
      ref.read(planProvider.notifier).setOrigin(_myLocation(point));
      return;
    }
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(
        SnackBar(
          content: Text(switch (result.failure) {
            LocationFailure.serviceDisabled =>
              'Turn on location services to start from where you are.',
            LocationFailure.denied =>
              'Location permission is needed to start from where you are.',
            LocationFailure.deniedForever =>
              'Location is blocked. Allow it in the app settings.',
            _ => "Couldn't get your location. Search for a start instead.",
          }),
        ),
      );
  }

  Future<void> _startWalk() async {
    final plan = ref.read(planProvider);
    final route = plan.selected;
    final origin = plan.origin;
    final destination = plan.destination;
    if (_starting || route == null || origin == null || destination == null) {
      return;
    }
    setState(() => _starting = true);
    final result = await ref
        .read(walkProvider.notifier)
        .start(route: route, origin: origin, destination: destination);
    if (!mounted) return;
    setState(() => _starting = false);

    switch (result.outcome) {
      case StartOutcome.started:
        break; // The Walk tab switches to the walking screen by itself.
      case StartOutcome.tooFar:
        await _info(
          'You are too far from this route',
          "You're ${formatDistance(result.distanceMeters ?? 0)} from it. A walk can only start near its route, so SafeWalk can tell if you leave it. Move closer, or plan the route from \"My location\".",
        );
      case StartOutcome.noLocation:
        await _info('Location is needed', switch (result.failure) {
          LocationFailure.serviceDisabled =>
            'Turn on location services, then try again.',
          LocationFailure.deniedForever => 'Location is blocked for SafeWalk. Allow it in the app settings, then try again.',
          _ => 'SafeWalk needs your location to follow your walk. Allow it and try again.',
        });
      case StartOutcome.alreadyActive:
        break; // The screen now shows the unfinished walk.
      case StartOutcome.failed:
        ScaffoldMessenger.of(context)
          ..hideCurrentSnackBar()
          ..showSnackBar(
            SnackBar(
              content: Text(
                result.message ?? "Couldn't start the walk. Try again.",
              ),
            ),
          );
    }
  }

  Future<void> _info(String title, String message) => showDialog<void>(
    context: context,
    builder: (context) => AlertDialog(
      title: Text(title),
      content: Text(message),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('OK'),
        ),
      ],
    ),
  );

  void _fit(List<LatLng> points) {
    if (points.length < 2) return;
    _map.fitCamera(
      CameraFit.bounds(
        bounds: LatLngBounds.fromPoints(points),
        padding: const EdgeInsets.fromLTRB(40, 280, 40, 380),
        maxZoom: 17,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (Env.mapboxToken.isEmpty) {
      return const Scaffold(
        body: ErrorState(
          message: 'Route planning needs a Mapbox token. Add MAPBOX_TOKEN to env/dev.json and rebuild.',
        ),
      );
    }

    final plan = ref.watch(planProvider);
    final controller = ref.read(planProvider.notifier);
    final theme = Theme.of(context);
    final mapCenter = ref.read(mapViewProvider).center ?? defaultMapCenter;

    ref.listen(planProvider.select((s) => s.status), (previous, next) {
      if (next == PlanStatus.ready) {
        _fit(allPoints(ref.read(planProvider).routes));
      }
    });
    ref.listen(planProvider.select((s) => (s.origin, s.destination)), (
      previous,
      next,
    ) {
      final (origin, destination) = next;
      if (origin != null &&
          destination != null &&
          ref.read(planProvider).status == PlanStatus.idle) {
        _fit([
          LatLng(origin.latitude, origin.longitude),
          LatLng(destination.latitude, destination.longitude),
        ]);
      }
    });

    final originPoint = plan.origin == null
        ? null
        : LatLng(plan.origin!.latitude, plan.origin!.longitude);
    final destinationPoint = plan.destination == null
        ? null
        : LatLng(plan.destination!.latitude, plan.destination!.longitude);
    final incidents = plan.routes.isEmpty
        ? null
        : ref
              .watch(
                routeIncidentsProvider(coveringCircle(allPoints(plan.routes))),
              )
              .value;

    return Scaffold(
      body: Stack(
        children: [
          FlutterMap(
            mapController: _map,
            options: MapOptions(
              initialCenter: mapCenter,
              initialZoom: defaultMapZoom,
              minZoom: 3,
              maxZoom: 19,
              interactionOptions: const InteractionOptions(
                flags: InteractiveFlag.all & ~InteractiveFlag.rotate,
              ),
            ),
            children: [
              TileLayer(
                urlTemplate: _tileUrl,
                tileDimension: 512,
                zoomOffset: -1,
                userAgentPackageName: 'com.samwallflower.safewalk_mobile',
              ),
              if (incidents != null && incidents.isNotEmpty)
                HeatDotsLayer(points: sample(incidents, maxHeatDots), zoom: 14),
              RouteLinesLayer(routes: plan.routes, selectedId: plan.selectedId),
              EndpointsLayer(
                origin: originPoint,
                destination: destinationPoint,
              ),
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
              child: Card(
                elevation: 4,
                shadowColor: Colors.black26,
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: SingleChildScrollView(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        PlaceField(
                          label: 'Start',
                          icon: Icons.trip_origin,
                          place: plan.origin,
                          proximity: mapCenter,
                          onSelected: controller.setOrigin,
                          trailing: IconButton(
                            tooltip: 'Use my location',
                            icon: _locating
                                ? const SizedBox(
                                    width: 18,
                                    height: 18,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                    ),
                                  )
                                : const Icon(Icons.my_location),
                            onPressed: _useMyLocation,
                          ),
                        ),
                        const SizedBox(height: 10),
                        PlaceField(
                          label: 'Where to?',
                          icon: Icons.place_outlined,
                          place: plan.destination,
                          proximity: originPoint ?? mapCenter,
                          onSelected: controller.setDestination,
                        ),
                        const SizedBox(height: 12),
                        if (plan.error != null) ...[
                          InlineNotice(plan.error!),
                          const SizedBox(height: 12),
                        ],
                        Row(
                          children: [
                            IconButton.outlined(
                              tooltip: 'Swap start and destination',
                              icon: const Icon(Icons.swap_vert),
                              onPressed:
                                  (plan.origin == null &&
                                      plan.destination == null)
                                  ? null
                                  : controller.swap,
                            ),
                            const SizedBox(width: 10),
                            Expanded(
                              child: FilledButton.icon(
                                onPressed: plan.canSearch
                                    ? controller.find
                                    : null,
                                icon: plan.status == PlanStatus.loading
                                    ? const SizedBox(
                                        width: 20,
                                        height: 20,
                                        child: CircularProgressIndicator(
                                          strokeWidth: 2.5,
                                          color: Colors.white,
                                        ),
                                      )
                                    : const Icon(Icons.alt_route),
                                label: Text(
                                  plan.status == PlanStatus.loading
                                      ? 'Finding routes...'
                                      : 'Find safer routes',
                                ),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
          if (plan.status == PlanStatus.ready && plan.routes.isEmpty)
            Positioned(
              left: 12,
              right: 12,
              bottom: 12,
              child: Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Text(
                    'No walking routes were found between these places.',
                    style: theme.textTheme.bodyLarge,
                  ),
                ),
              ),
            ),
          if (plan.status == PlanStatus.ready && plan.routes.isNotEmpty)
            RouteResultsSheet(
              routes: plan.routes,
              selectedId: plan.selectedId,
              onStart: _startWalk,
              starting: _starting,
              onSelect: (route) {
                controller.select(route.route.id);
                _fit(route.points);
              },
            ),
        ],
      ),
    );
  }
}

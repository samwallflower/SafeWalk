import 'dart:async';
import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';
import 'package:wakelock_plus/wakelock_plus.dart';

import '../../../core/config/env.dart';
import '../../../core/format/format.dart';
import '../../../core/widgets/error_state.dart';
import '../../../core/widgets/inline_notice.dart';
import '../../map/domain/map_config.dart';
import '../../map/presentation/widgets/map_layers.dart';
import '../../routing/presentation/widgets/route_map_layers.dart';
import '../../../core/location/location_provider.dart';
import '../../../core/notifications/alert_notifier.dart';
import '../../../core/widgets/notifications_off_banner.dart';
import '../../emergency/data/authority_cache.dart';
import '../../emergency/data/best_emergency_number.dart';
import '../../emergency/data/dialer.dart';
import '../../emergency/presentation/state/emergency_controller.dart';
import '../../emergency/presentation/widgets/emergency_overlay.dart';
import '../../emergency/presentation/widgets/sos_button.dart';
import '../../safety/presentation/state/safety_controller.dart';
import '../../safety/presentation/widgets/idle_prompt_overlay.dart';
import '../../safety/presentation/widgets/off_route_banner.dart';
import 'state/walk_controller.dart';

const _tileUrl =
    'https://api.mapbox.com/styles/v1/mapbox/streets-v12/tiles/{z}/{x}/{y}?access_token=${Env.mapboxToken}';

class ActiveWalkScreen extends ConsumerStatefulWidget {
  const ActiveWalkScreen({super.key});

  @override
  ConsumerState<ActiveWalkScreen> createState() => _ActiveWalkScreenState();
}

class _ActiveWalkScreenState extends ConsumerState<ActiveWalkScreen> {
  final _map = MapController();
  Timer? _clock;
  bool _follow = true;
  bool _ending = false;
  late final AppLifecycleListener _lifecycle;

  @override
  void initState() {
    super.initState();
    // Keep the screen on while the walk screen is showing.
    WakelockPlus.enable();
    // Coming back to the app: re-check notifications and ask the server how the walk is doing, right away.
    _lifecycle = AppLifecycleListener(
      onResume: () {
        ref.invalidate(notificationsEnabledProvider);
        ref.read(walkProvider.notifier).refreshSession();
      },
    );
    _clock = Timer.periodic(const Duration(seconds: 1), (_) {
      if (mounted) setState(() {});
    });
  }

  @override
  void dispose() {
    _clock?.cancel();
    _lifecycle.dispose();
    WakelockPlus.disable();
    super.dispose();
  }

  Future<void> _confirmEnd() async {
    if (_ending) return;
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('End your walk?'),
        content: const Text('SafeWalk will stop sharing your location.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Keep walking'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('End walk'),
          ),
        ],
      ),
    );
    if (confirmed != true || !mounted) return;
    setState(() => _ending = true);
    final ok = await ref.read(walkProvider.notifier).end();
    if (!mounted) return;
    setState(() => _ending = false);
    if (!ok) {
      final message =
          ref.read(walkProvider).message ?? "Couldn't end the walk. Try again.";
      ref.read(walkProvider.notifier).clearMessage();
      ScaffoldMessenger.of(context)
        ..hideCurrentSnackBar()
        ..showSnackBar(SnackBar(content: Text(message)));
    }
  }

  Future<void> _sendSos() async {
    final ok = await ref.read(emergencyProvider.notifier).triggerSos();
    if (ok || !mounted) return;
    final error =
        ref.read(emergencyProvider).error ?? "The alert couldn't be sent.";
    ref.read(emergencyProvider.notifier).clearError();
    final number = await bestEmergencyNumber(ref.read(authorityCacheProvider));
    if (!mounted) return;
    await showDialog<void>(
      context: context,
      builder: (context) => AlertDialog(
        icon: const Icon(
          Icons.error_outline,
          color: Color(0xFFC4161C),
          size: 36,
        ),
        title: const Text('SOS was not sent'),
        content: Text('$error\n\nIf you are in danger, call for help now.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Close'),
          ),
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
              _sendSos();
            },
            child: const Text('Try again'),
          ),
          FilledButton.icon(
            icon: const Icon(Icons.call),
            onPressed: () => ref.read(dialerProvider).dial(number),
            label: Text('Call $number'),
          ),
        ],
      ),
    );
  }

  Future<void> _confirmSos() async {
    final yes = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Send an SOS?'),
        content: const Text(
          'Your emergency contacts will be alerted with your location.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            style: FilledButton.styleFrom(
              backgroundColor: const Color(0xFFC4161C),
            ),
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('Send SOS'),
          ),
        ],
      ),
    );
    if (yes == true) await _sendSos();
  }

  @override
  Widget build(BuildContext context) {
    if (Env.mapboxToken.isEmpty) {
      return const Scaffold(
        body: ErrorState(
          message: 'The walk screen needs a Mapbox token. Add MAPBOX_TOKEN to env/dev.json.',
        ),
      );
    }
    final walk = ref.watch(walkProvider);
    final safety = ref.watch(safetyProvider);
    final emergency = ref.watch(emergencyProvider);
    final notificationsOn =
        ref.watch(notificationsEnabledProvider).value ?? true;
    final theme = Theme.of(context);
    final route = walk.route;
    final position = walk.position;
    final session = walk.session;
    final elapsed = DateTime.now().difference(
      DateTime.tryParse(session?.startTime ?? '') ?? DateTime.now(),
    );
    final left = ref.read(walkProvider.notifier).metersToDestination();

    ref.listen(walkProvider.select((s) => s.position), (previous, next) {
      if (next != null && _follow) {
        _map.move(next, math.max(_map.camera.zoom, 17));
      }
    });

    final destination = route?.points.isNotEmpty == true
        ? route!.points.last
        : (session?.destinationLatitude != null &&
                  session?.destinationLongitude != null
              ? LatLng(
                  session!.destinationLatitude!,
                  session.destinationLongitude!,
                )
              : null);
    final start = route?.points.isNotEmpty == true ? route!.points.first : null;

    return Scaffold(
      body: Stack(
        children: [
          FlutterMap(
            mapController: _map,
            options: MapOptions(
              initialCenter: position ?? destination ?? defaultMapCenter,
              initialZoom: 17,
              minZoom: 3,
              maxZoom: 19,
              interactionOptions: const InteractionOptions(
                flags: InteractiveFlag.all & ~InteractiveFlag.rotate,
              ),
              onPositionChanged: (camera, hasGesture) {
                if (hasGesture && _follow) setState(() => _follow = false);
              },
            ),
            children: [
              TileLayer(
                urlTemplate: _tileUrl,
                tileDimension: 512,
                zoomOffset: -1,
                userAgentPackageName: 'com.samwallflower.safewalk_mobile',
              ),
              if (route != null)
                RouteLinesLayer(routes: [route], selectedId: route.route.id),
              EndpointsLayer(origin: start, destination: destination),
              if (position != null) MyLocationLayer(point: position),
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
                  Card(
                    elevation: 4,
                    shadowColor: Colors.black26,
                    child: Padding(
                      padding: const EdgeInsets.all(14),
                      child: Row(
                        children: [
                          Container(
                            width: 10,
                            height: 10,
                            decoration: const BoxDecoration(
                              color: Color(0xFF0A6B32),
                              shape: BoxShape.circle,
                            ),
                          ),
                          const SizedBox(width: 10),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'Walk in progress',
                                  style: theme.textTheme.titleSmall?.copyWith(
                                    fontWeight: FontWeight.w800,
                                  ),
                                ),
                                Text(
                                  left == null
                                      ? 'Finding your position...'
                                      : '${formatDistance(left)} to go',
                                  style: theme.textTheme.bodySmall?.copyWith(
                                    color: theme.colorScheme.onSurfaceVariant,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          Text(
                            formatClock(elapsed),
                            style: theme.textTheme.titleLarge?.copyWith(
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  if (!notificationsOn) ...[
                    const SizedBox(height: 8),
                    NotificationsOffBanner(
                      onOpenSettings: () =>
                          ref.read(locationServiceProvider).openSettings(),
                    ),
                  ],
                  if (safety.connectionLost) ...[
                    const SizedBox(height: 8),
                    const InlineNotice('Connection lost. Reconnecting...'),
                  ],
                  if (safety.offRoute) ...[
                    const SizedBox(height: 8),
                    OffRouteBanner(
                      onDismiss: ref
                          .read(safetyProvider.notifier)
                          .dismissOffRoute,
                    ),
                  ],
                  if (walk.sendFailing) ...[
                    const SizedBox(height: 8),
                    const InlineNotice(
                      "Can't reach SafeWalk. Still trying to share your location.",
                    ),
                  ],
                ],
              ),
            ),
          ),
          if (!_follow && position != null)
            Positioned(
              right: 12,
              bottom: 180,
              child: FloatingActionButton.small(
                heroTag: 'recenter',
                tooltip: 'Follow my position',
                backgroundColor: theme.colorScheme.surface,
                foregroundColor: theme.colorScheme.primary,
                onPressed: () {
                  setState(() => _follow = true);
                  _map.move(position, math.max(_map.camera.zoom, 17));
                },
                child: const Icon(Icons.my_location),
              ),
            ),
          Positioned(
            left: 12,
            right: 12,
            bottom: 12,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                SosButton(
                  busy: emergency.triggering,
                  onTriggered: _sendSos,
                  onConfirmRequested: _confirmSos,
                ),
                const SizedBox(height: 10),
                FilledButton.icon(
                  style: FilledButton.styleFrom(
                    backgroundColor: theme.colorScheme.surface,
                    foregroundColor: theme.colorScheme.error,
                  ),
                  onPressed: _ending ? null : _confirmEnd,
                  icon: _ending
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(strokeWidth: 2.5),
                        )
                      : const Icon(Icons.stop_circle_outlined),
                  label: Text(_ending ? 'Ending...' : 'End walk'),
                ),
              ],
            ),
          ),
          if (safety.idlePrompt != null && !walk.emergencyActive)
            Positioned.fill(
              child: IdlePromptOverlay(
                prompt: safety.idlePrompt!,
                error: safety.error,
                onImOk: () async {
                  await ref.read(safetyProvider.notifier).imOk();
                },
              ),
            ),
          if (walk.emergencyActive)
            const Positioned.fill(child: EmergencyOverlay()),
        ],
      ),
    );
  }
}

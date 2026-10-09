import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

import '../../../../core/config/env.dart';
import '../../../../core/theme/colors.dart';

const _tileUrl =
    'https://api.mapbox.com/styles/v1/mapbox/streets-v12/tiles/{z}/{x}/{y}?access_token=${Env.mapboxToken}';

/// A small map with a fixed pin in the middle: drag the map to move the pin.
class LocationPinPicker extends StatelessWidget {
  const LocationPinPicker({
    super.key,
    required this.controller,
    required this.start,
    required this.onMoved,
    required this.onInteraction,
  });

  final MapController controller;
  final LatLng start;

  /// Called with the pin position whenever the map moves.
  final void Function(LatLng point) onMoved;

  /// True while a finger is on the map, so the page can stop scrolling underneath it.
  final void Function(bool active) onInteraction;

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(16),
      child: SizedBox(
        height: 220,
        child: Listener(
          onPointerDown: (_) => onInteraction(true),
          onPointerUp: (_) => onInteraction(false),
          onPointerCancel: (_) => onInteraction(false),
          child: Stack(
            alignment: Alignment.center,
            children: [
              FlutterMap(
                mapController: controller,
                options: MapOptions(
                  initialCenter: start,
                  initialZoom: 16,
                  minZoom: 5,
                  maxZoom: 19,
                  interactionOptions: const InteractionOptions(
                    flags: InteractiveFlag.all & ~InteractiveFlag.rotate,
                  ),
                  onPositionChanged: (camera, hasGesture) =>
                      onMoved(camera.center),
                ),
                children: [
                  TileLayer(
                    urlTemplate: _tileUrl,
                    tileDimension: 512,
                    zoomOffset: -1,
                    userAgentPackageName: 'com.samwallflower.safewalk_mobile',
                  ),
                  const RichAttributionWidget(
                    alignment: AttributionAlignment.bottomRight,
                    attributions: [
                      TextSourceAttribution('© Mapbox © OpenStreetMap'),
                    ],
                  ),
                ],
              ),
              // The pin's tip sits on the map center.
              const IgnorePointer(
                child: Padding(
                  padding: EdgeInsets.only(bottom: 36),
                  child: Icon(
                    Icons.location_on,
                    size: 44,
                    color: AppColors.destructive,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

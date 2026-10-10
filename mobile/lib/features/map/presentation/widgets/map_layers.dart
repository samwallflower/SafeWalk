import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

import '../../../../core/theme/colors.dart';
import '../../../incidents/domain/incident.dart';
import '../../domain/clustering.dart';

/// Lightweight reports as small red dots with a white border. Radius grows a little as you zoom in.
class HeatDotsLayer extends StatelessWidget {
  const HeatDotsLayer({super.key, required this.points, required this.zoom});

  final List<HeatPoint> points;
  final double zoom;

  @override
  Widget build(BuildContext context) {
    final radius = zoom < 12 ? 3.0 : (zoom < 14 ? 4.0 : 5.5);
    return CircleLayer(
      circles: [
        for (final p in points)
          CircleMarker(
            point: LatLng(p.latitude, p.longitude),
            radius: radius,
            color: AppColors.destructive,
            borderStrokeWidth: 1.2,
            borderColor: Colors.white,
          ),
      ],
    );
  }
}

/// Tappable incidents at street level: one dot each. Dots that would overlap are fanned out a little.
class IncidentMarkersLayer extends StatelessWidget {
  const IncidentMarkersLayer({
    super.key,
    required this.placed,
    required this.selectedId,
    required this.onTap,
  });

  final List<PlacedIncident> placed;
  final int? selectedId;
  final void Function(Incident incident) onTap;

  @override
  Widget build(BuildContext context) {
    return MarkerLayer(
      markers: [
        for (final item in placed)
          Marker(
            point: item.position,
            width: 44,
            height: 44,
            child: Semantics(
              button: true,
              label: '${item.incident.category.name} incident',
              child: GestureDetector(
                behavior: HitTestBehavior.opaque,
                onTap: () => onTap(item.incident),
                child: Center(
                  child: _Dot(selected: item.incident.id == selectedId),
                ),
              ),
            ),
          ),
      ],
    );
  }
}

class _Dot extends StatelessWidget {
  const _Dot({required this.selected});

  final bool selected;

  @override
  Widget build(BuildContext context) {
    final size = selected ? 22.0 : 14.0;
    return Container(
      width: size + 12,
      height: size + 12,
      alignment: Alignment.center,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: AppColors.destructive.withValues(alpha: selected ? 0.25 : 0.14),
      ),
      child: Container(
        width: size,
        height: size,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          color: AppColors.destructive,
          border: Border.all(color: Colors.white, width: selected ? 3 : 2),
        ),
      ),
    );
  }
}

/// The user's own position.
class MyLocationLayer extends StatelessWidget {
  const MyLocationLayer({super.key, required this.point});

  final LatLng point;

  @override
  Widget build(BuildContext context) {
    return CircleLayer(
      circles: [
        CircleMarker(
          point: point,
          radius: 14,
          color: AppColors.primary.withValues(alpha: 0.18),
        ),
        CircleMarker(
          point: point,
          radius: 7,
          color: AppColors.primary,
          borderStrokeWidth: 3,
          borderColor: Colors.white,
        ),
      ],
    );
  }
}

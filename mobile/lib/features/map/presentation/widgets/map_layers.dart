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

/// Tappable incidents at street level. Incidents that overlap show as one dot with a number.
class IncidentMarkersLayer extends StatelessWidget {
  const IncidentMarkersLayer({
    super.key,
    required this.clusters,
    required this.selectedIds,
    required this.onTap,
  });

  final List<IncidentCluster> clusters;
  final Set<int> selectedIds;
  final void Function(IncidentCluster cluster) onTap;

  @override
  Widget build(BuildContext context) {
    return MarkerLayer(
      markers: [
        for (final cluster in clusters)
          Marker(
            point: cluster.center,
            width: 48,
            height: 48,
            child: Semantics(
              button: true,
              label: cluster.count == 1
                  ? '${cluster.incidents.first.category.name} incident'
                  : '${cluster.count} incidents',
              child: GestureDetector(
                behavior: HitTestBehavior.opaque,
                onTap: () => onTap(cluster),
                child: Center(
                  child: _Dot(
                    selected: cluster.incidents.any(
                      (i) => selectedIds.contains(i.id),
                    ),
                    count: cluster.count,
                  ),
                ),
              ),
            ),
          ),
      ],
    );
  }
}

class _Dot extends StatelessWidget {
  const _Dot({required this.selected, required this.count});

  final bool selected;
  final int count;

  @override
  Widget build(BuildContext context) {
    final size = selected ? 22.0 : 14.0;
    final dot = Container(
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
    if (count < 2) return dot;
    return Stack(
      clipBehavior: Clip.none,
      alignment: Alignment.center,
      children: [
        dot,
        Positioned(
          top: -2,
          right: -4,
          child: Container(
            constraints: const BoxConstraints(minWidth: 20, minHeight: 20),
            padding: const EdgeInsets.symmetric(horizontal: 5),
            alignment: Alignment.center,
            decoration: BoxDecoration(
              color: AppColors.foreground,
              borderRadius: BorderRadius.circular(10),
              border: Border.all(color: Colors.white, width: 1.5),
            ),
            child: Text(
              '$count',
              style: const TextStyle(
                color: Colors.white,
                fontSize: 11,
                fontWeight: FontWeight.w800,
              ),
            ),
          ),
        ),
      ],
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

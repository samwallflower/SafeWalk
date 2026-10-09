import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

import '../../../../core/theme/colors.dart';
import '../../domain/route_option.dart';

/// Every route as a line; the selected one is thicker, drawn last, and the others are softened.
class RouteLinesLayer extends StatelessWidget {
  const RouteLinesLayer({
    super.key,
    required this.routes,
    required this.selectedId,
  });

  final List<DecodedRoute> routes;
  final int? selectedId;

  @override
  Widget build(BuildContext context) {
    final ordered = [
      ...routes.where((r) => r.route.id != selectedId),
      ...routes.where((r) => r.route.id == selectedId),
    ].where((r) => r.points.length >= 2);
    return PolylineLayer(
      polylines: [
        for (final r in ordered)
          Polyline(
            points: r.points,
            strokeWidth: r.route.id == selectedId ? 7 : 5,
            color: r.route.id == selectedId
                ? r.color
                : r.color.withValues(alpha: 0.6),
            borderStrokeWidth: 2,
            borderColor: Colors.white,
            strokeCap: StrokeCap.round,
            strokeJoin: StrokeJoin.round,
          ),
      ],
    );
  }
}

/// Start and destination markers.
class EndpointsLayer extends StatelessWidget {
  const EndpointsLayer({super.key, this.origin, this.destination});

  final LatLng? origin;
  final LatLng? destination;

  @override
  Widget build(BuildContext context) {
    return MarkerLayer(
      markers: [
        if (origin != null)
          Marker(
            point: origin!,
            width: 28,
            height: 28,
            child: Container(
              decoration: BoxDecoration(
                color: AppColors.primary,
                shape: BoxShape.circle,
                border: Border.all(color: Colors.white, width: 4),
                boxShadow: const [
                  BoxShadow(color: Colors.black26, blurRadius: 4),
                ],
              ),
            ),
          ),
        if (destination != null)
          Marker(
            point: destination!,
            width: 44,
            height: 52,
            alignment: Alignment.topCenter,
            child: const Icon(
              Icons.location_on,
              size: 44,
              color: AppColors.success,
            ),
          ),
      ],
    );
  }
}

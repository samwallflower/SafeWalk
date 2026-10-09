import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../incidents/data/incidents_api.dart';
import '../../incidents/domain/incident.dart';
import '../../map/domain/area.dart';
import '../../map/domain/map_config.dart';
import '../../map/domain/map_models.dart';

/// Lightweight report points under the routes. Skipped when the routes span more than the 30 km safety limit.
final routeIncidentsProvider = FutureProvider.autoDispose
    .family<List<HeatPoint>, GeoCircle?>((ref, circle) async {
      if (circle == null || circle.radiusMeters > maxHeatmapRadiusM) {
        return const [];
      }
      final rounded = roundedArea(circle);
      final cancel = CancelToken();
      ref.onDispose(cancel.cancel);
      return ref
          .watch(incidentsApiProvider)
          .heatmapPoints(
            AreaQuery(
              latitude: rounded.center.latitude,
              longitude: rounded.center.longitude,
              radiusMeters: rounded.radiusMeters,
            ),
            cancelToken: cancel,
          );
    });

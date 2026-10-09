import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../../../core/network/providers.dart';
import '../domain/incident.dart';

class AreaQuery {
  const AreaQuery({
    required this.latitude,
    required this.longitude,
    required this.radiusMeters,
  });

  final double latitude;
  final double longitude;
  final double radiusMeters;

  Map<String, Object?> toQuery() => {
    'latitude': latitude,
    'longitude': longitude,
    'radiusMeters': radiusMeters,
  };
}

/// Only radius-bounded endpoints are used. The unbounded lists must never be called (about 67k reports).
class IncidentsApi {
  IncidentsApi(this._client);

  final ApiClient _client;

  Future<List<HeatPoint>> heatmapPoints(
    AreaQuery area, {
    CancelToken? cancelToken,
  }) async {
    final list = await _client.getList(
      '/incident-reports/heatmap-points/report',
      query: area.toQuery(),
      cancelToken: cancelToken,
    );
    return list.map(HeatPoint.fromJson).toList();
  }

  Future<List<Incident>> nearby(
    AreaQuery area, {
    CancelToken? cancelToken,
  }) async {
    final list = await _client.getList(
      '/incident-reports/nearby/report',
      query: area.toQuery(),
      cancelToken: cancelToken,
    );
    return list.map(Incident.fromDto).toList();
  }
}

final incidentsApiProvider = Provider<IncidentsApi>(
  (ref) => IncidentsApi(ref.watch(apiClientProvider)),
);

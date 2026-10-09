import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

import '../../../core/config/env.dart';
import '../domain/place.dart';

const _forward = 'https://api.mapbox.com/search/geocode/v6/forward';
const _reverse = 'https://api.mapbox.com/search/geocode/v6/reverse';

String _label(Map<String, Object?> properties) {
  final name = properties['name'] as String? ?? '';
  final context = properties['place_formatted'] as String? ?? '';
  return [name, context].where((part) => part.isNotEmpty).join(', ');
}

/// Mapbox Geocoding v6. This uses its own Dio on purpose: the SafeWalk JWT must never be sent to Mapbox.
class GeocodeApi {
  GeocodeApi(this._dio);

  final Dio _dio;

  /// Results are biased (not restricted) toward [proximity]. Coordinates arrive as [lng, lat].
  Future<List<Place>> search(
    String query, {
    LatLng? proximity,
    CancelToken? cancelToken,
  }) async {
    final response = await _dio.get<Map<String, Object?>>(
      _forward,
      queryParameters: {
        'q': query,
        'limit': 5,
        'autocomplete': true,
        'access_token': Env.mapboxToken,
        if (proximity != null)
          'proximity': '${proximity.longitude},${proximity.latitude}',
      },
      cancelToken: cancelToken,
    );
    final features = (response.data?['features'] as List<Object?>? ?? const [])
        .cast<Map<String, Object?>>();
    return [
      for (final feature in features)
        if (feature['geometry'] case {
          'coordinates': [final num lng, final num lat],
        })
          Place(
            label: _label(feature['properties'] as Map<String, Object?>),
            latitude: lat.toDouble(),
            longitude: lng.toDouble(),
          ),
    ];
  }

  Future<String?> _reverseName(
    double latitude,
    double longitude,
    String type,
  ) async {
    final response = await _dio.get<Map<String, Object?>>(
      _reverse,
      queryParameters: {
        'latitude': latitude,
        'longitude': longitude,
        'limit': 1,
        'types': type,
        'access_token': Env.mapboxToken,
      },
    );
    final features = (response.data?['features'] as List<Object?>? ?? const [])
        .cast<Map<String, Object?>>();
    if (features.isEmpty) return null;
    return _label(features.first['properties'] as Map<String, Object?>)
        .split(',')
        .first;
  }

  /// Street (or nearest address) name only, e.g. "Upper Parliament Street".
  Future<String?> street(double latitude, double longitude) async =>
      await _reverseName(latitude, longitude, 'street') ??
      await _reverseName(latitude, longitude, 'address');
}

final geocodeApiProvider = Provider<GeocodeApi>((ref) => GeocodeApi(Dio()));

/// Cached street name for a rounded coordinate. Failures just mean "no name", never an error screen.
final streetNameProvider =
    FutureProvider.family<String?, ({double lat, double lng})>((
      ref,
      key,
    ) async {
      if (Env.mapboxToken.isEmpty) return null;
      try {
        return await ref.watch(geocodeApiProvider).street(key.lat, key.lng);
      } on DioException {
        return null;
      }
    });

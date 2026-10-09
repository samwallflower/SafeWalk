import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../../../core/network/providers.dart';
import '../domain/route_option.dart';

class RouteRequest {
  const RouteRequest({
    required this.originLatitude,
    required this.originLongitude,
    required this.destinationLatitude,
    required this.destinationLongitude,
  });

  final double originLatitude;
  final double originLongitude;
  final double destinationLatitude;
  final double destinationLongitude;

  /// Mirrors RouteRecommendationRequest.java
  Map<String, Object?> toJson() => {
    'originLatitude': originLatitude,
    'originLongitude': originLongitude,
    'destinationLatitude': destinationLatitude,
    'destinationLongitude': destinationLongitude,
  };
}

class RoutingApi {
  RoutingApi(this._client);

  final ApiClient _client;

  /// Ranked alternatives; rank 1 has the lowest virtual distance (distance plus safety penalty).
  Future<List<RouteOption>> recommend(RouteRequest request) async {
    final list = await _client.postList(
      '/routing/recommend',
      body: request.toJson(),
    );
    return list.map(RouteOption.fromJson).toList();
  }
}

final routingApiProvider = Provider<RoutingApi>(
  (ref) => RoutingApi(ref.watch(apiClientProvider)),
);

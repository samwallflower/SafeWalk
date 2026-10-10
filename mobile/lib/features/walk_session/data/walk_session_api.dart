import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../../../core/network/providers.dart';
import '../domain/walk_session.dart';

class StartWalkRequest {
  const StartWalkRequest({
    required this.originLatitude,
    required this.originLongitude,
    required this.destinationLatitude,
    required this.destinationLongitude,
    required this.chosenRouteId,
  });

  final double originLatitude;
  final double originLongitude;
  final double destinationLatitude;
  final double destinationLongitude;
  final int chosenRouteId;

  /// Mirrors AddWalkSessionRequest.java
  Map<String, Object?> toJson() => {
    'originLatitude': originLatitude,
    'originLongitude': originLongitude,
    'destinationLatitude': destinationLatitude,
    'destinationLongitude': destinationLongitude,
    'chosenRouteId': chosenRouteId,
  };
}

class WalkSessionApi {
  WalkSessionApi(this._client);

  final ApiClient _client;

  /// The backend refuses (409) while an earlier session is not completed.
  Future<WalkSession> start(int userId, StartWalkRequest request) async =>
      WalkSession.fromJson(
        await _client.postObject(
          '/walk-sessions/user/$userId/add',
          body: request.toJson(),
        ),
      );

  Future<void> updateLocation(
    int sessionId,
    int userId,
    double latitude,
    double longitude,
  ) => _client.putObject(
    '/walk-sessions/$sessionId/session/user/$userId/update/location',
    body: {'latitude': latitude, 'longitude': longitude},
  );

  Future<WalkSession> end(int sessionId, int userId) async =>
      WalkSession.fromJson(
        await _client.putObject(
          '/walk-sessions/$sessionId/session/user/$userId/end',
        ),
      );

  /// "I'm OK": answers the idle warning and tells the server the walker has moved.
  Future<WalkSession> resolveIdleWarning(int sessionId, int userId) async =>
      WalkSession.fromJson(
        await _client.putObject(
          '/walk-sessions/$sessionId/user/$userId/resolve-idle-warning/session',
        ),
      );

  Future<WalkSession> byId(int sessionId, int userId) async =>
      WalkSession.fromJson(
        await _client.getObject(
          '/walk-sessions/$sessionId/user/$userId/session',
        ),
      );

  Future<List<WalkSession>> mine(int userId) async {
    final list = await _client.getList('/walk-sessions/user/$userId/session');
    return list.map(WalkSession.fromJson).toList();
  }
}

final walkSessionApiProvider = Provider<WalkSessionApi>(
  (ref) => WalkSessionApi(ref.watch(apiClientProvider)),
);

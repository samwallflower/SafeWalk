import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/network/providers.dart';
import '../domain/emergency.dart';

class EmergencyApi {
  EmergencyApi(this._client);

  final ApiClient _client;

  /// SOS. The server answers with no emergency (null) if the walk is already in emergency, so callers must handle that.
  Future<Emergency?> trigger(int sessionId, int userId) async {
    final json = await _client.postObjectOrNull(
      '/emergency/session/$sessionId/user/$userId/trigger',
    );
    return json == null ? null : Emergency.fromJson(json);
  }

  /// The unresolved emergency of a walk, or null when there is none (the backend answers 404).
  Future<Emergency?> active(int sessionId, int userId) async {
    try {
      return Emergency.fromJson(
        await _client.getObject(
          '/emergency/session/$sessionId/user/$userId/active',
        ),
      );
    } on ApiException catch (e) {
      if (e.isNotFound) return null;
      rethrow;
    }
  }

  /// "I'm safe": puts the walk back to normal.
  Future<void> resolve(
    int emergencyId,
    int sessionId,
    int userId,
  ) => _client.putObject(
    '/emergency/$emergencyId/emergency/session/$sessionId/user/$userId/resolve',
  );
}

final emergencyApiProvider = Provider<EmergencyApi>(
  (ref) => EmergencyApi(ref.watch(apiClientProvider)),
);

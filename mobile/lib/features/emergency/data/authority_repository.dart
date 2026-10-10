import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/network/providers.dart';
import '../domain/authority_numbers.dart';
import 'authority_cache.dart';

/// Emergency numbers for where you are: live from the server, else the last saved ones, else 112.
/// It never fails, because this is needed most when things are going wrong.
class AuthorityRepository {
  AuthorityRepository(this._client, this._cache);

  final ApiClient _client;
  final AuthorityCache _cache;

  Future<AuthorityNumbers> forLocation(
    double latitude,
    double longitude,
  ) async {
    try {
      final json = await _client.getObject(
        '/emergency-authority/authority/by-location',
        query: {'latitude': latitude, 'longitude': longitude},
      );
      final numbers = AuthorityNumbers.fromJson(json);
      await _cache.save(numbers);
      return numbers;
    } on ApiException {
      return (await _cache.readLast()) ?? AuthorityNumbers.fallback;
    } on Object {
      return (await _cache.readLast()) ?? AuthorityNumbers.fallback;
    }
  }
}

final authorityRepositoryProvider = Provider<AuthorityRepository>(
  (ref) => AuthorityRepository(
    ref.watch(apiClientProvider),
    ref.watch(authorityCacheProvider),
  ),
);

/// Numbers for a rounded position (about 1 km), so nearby readings share one lookup.
final authorityNumbersProvider = FutureProvider.autoDispose
    .family<AuthorityNumbers, ({double lat, double lng})>((ref, at) {
      return ref.watch(authorityRepositoryProvider).forLocation(at.lat, at.lng);
    });

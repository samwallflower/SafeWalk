import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../../../core/network/providers.dart';
import '../../auth/presentation/state/session_controller.dart';
import '../domain/user_profile.dart';

class ProfileApi {
  ProfileApi(this._client);

  final ApiClient _client;

  Future<UserProfile> get(int userId) async =>
      UserProfile.fromJson(await _client.getObject('/users/$userId/user'));

  /// Only the fields given are changed.
  Future<UserProfile> update(
    int userId, {
    String? firstName,
    String? lastName,
    String? phoneNumber,
  }) async => UserProfile.fromJson(
    await _client.putObject(
      '/users/$userId/update',
      body: {
        'firstName': ?firstName,
        'lastName': ?lastName,
        'phoneNumber': ?phoneNumber,
      },
    ),
  );
}

final profileApiProvider = Provider<ProfileApi>(
  (ref) => ProfileApi(ref.watch(apiClientProvider)),
);

final profileProvider = FutureProvider.autoDispose<UserProfile>((ref) async {
  final userId = ref.watch(sessionProvider.select((s) => s.value?.id));
  if (userId == null) throw StateError('Not signed in');
  return ref.watch(profileApiProvider).get(userId);
});

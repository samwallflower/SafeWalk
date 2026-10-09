import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../../../core/network/providers.dart';

class RegisterInput {
  const RegisterInput({
    required this.email,
    required this.password,
    required this.firstName,
    required this.lastName,
  });

  final String email;
  final String password;
  final String firstName;
  final String lastName;

  Map<String, Object?> toJson() => {
    'email': email,
    'password': password,
    'firstName': firstName,
    'lastName': lastName,
  };
}

class AuthApi {
  AuthApi(this._client);

  final ApiClient _client;

  /// Returns the JWT. The `JwtResponse` also holds the user id, but that is read from the token itself.
  Future<String?> login(String email, String password) async {
    final data = await _client.postObject(
      '/auth/login',
      body: {'email': email, 'password': password},
    );
    final token = data['token'];
    return token is String ? token : null;
  }

  Future<void> register(RegisterInput input) =>
      _client.postObject('/users/register', body: input.toJson());

  Future<void> verify(String email, String code) => _client.postVoid(
    '/auth/verify',
    body: {'email': email, 'verificationCode': code},
  );

  Future<void> resendVerification(String email) =>
      _client.postVoid('/auth/resend-verification', body: {'email': email});
}

final authApiProvider = Provider<AuthApi>(
  (ref) => AuthApi(ref.watch(apiClientProvider)),
);

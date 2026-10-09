import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../config/env.dart';
import '../storage/secure_store.dart';
import 'api_client.dart';
import 'auth_interceptor.dart';

final secureStoreProvider = Provider<SecureStore>((ref) => SecureStore());

/// Bumped when the backend answers 401; the router listens and returns to login.
final sessionExpiredProvider = NotifierProvider<SessionExpired, int>(
  SessionExpired.new,
);

class SessionExpired extends Notifier<int> {
  @override
  int build() => 0;

  void notify() => state++;
}

final dioProvider = Provider<Dio>((ref) {
  final dio = Dio(
    BaseOptions(
      baseUrl: Env.apiBaseUrl,
      connectTimeout: const Duration(seconds: 15),
      receiveTimeout: const Duration(seconds: 30),
      headers: {'Accept': 'application/json'},
    ),
  );
  dio.interceptors.add(
    AuthInterceptor(
      ref.read(secureStoreProvider),
      onUnauthorized: () => ref.read(sessionExpiredProvider.notifier).notify(),
    ),
  );
  return dio;
});

final apiClientProvider = Provider<ApiClient>(
  (ref) => ApiClient(ref.watch(dioProvider)),
);

import 'package:dio/dio.dart';

import '../storage/secure_store.dart';

/// Adds the bearer token and, on a 401, forgets it and tells the app to go back to login.
class AuthInterceptor extends Interceptor {
  AuthInterceptor(this._store, {required this.onUnauthorized});

  final SecureStore _store;
  final void Function() onUnauthorized;

  @override
  Future<void> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final token = await _store.readToken();
    if (token != null && token.isNotEmpty) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    final isLogin = err.requestOptions.path.contains('/auth/login');
    if (err.response?.statusCode == 401 && !isLogin) {
      await _store.clear();
      onUnauthorized();
    }
    handler.next(err);
  }
}

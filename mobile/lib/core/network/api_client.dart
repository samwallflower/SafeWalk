import 'package:dio/dio.dart';

import 'api_envelope.dart';
import 'error_mapper.dart';

typedef Json = Map<String, Object?>;

/// Typed access to the backend: returns the unwrapped `data` or throws an [ApiException].
class ApiClient {
  ApiClient(this._dio);

  final Dio _dio;

  Future<Object?> _send(
    String method,
    String path, {
    Map<String, Object?>? query,
    Object? body,
    CancelToken? cancelToken,
  }) async {
    try {
      final response = await _dio.request<Object?>(
        path,
        data: body,
        queryParameters: query,
        options: Options(method: method),
        cancelToken: cancelToken,
      );
      return unwrapEnvelope(response.data);
    } on DioException catch (error) {
      throw mapDioError(error);
    }
  }

  Future<Json> getObject(
    String path, {
    Map<String, Object?>? query,
    CancelToken? cancelToken,
  }) async {
    final data = await _send(
      'GET',
      path,
      query: query,
      cancelToken: cancelToken,
    );
    return data as Json;
  }

  Future<List<Json>> getList(
    String path, {
    Map<String, Object?>? query,
    CancelToken? cancelToken,
  }) async {
    final data = await _send(
      'GET',
      path,
      query: query,
      cancelToken: cancelToken,
    );
    return (data as List<Object?>).cast<Json>();
  }

  /// A paged endpoint's `content`, for lists that are short by nature (the backend caps a page at 50).
  Future<List<Json>> getPageContent(
    String path, {
    Map<String, Object?>? query,
    CancelToken? cancelToken,
  }) async {
    final page = await getObject(path, query: query, cancelToken: cancelToken);
    return ((page['content'] as List<Object?>?) ?? const []).cast<Json>();
  }

  Future<Object?> getValue(String path, {Map<String, Object?>? query}) =>
      _send('GET', path, query: query);

  Future<Json> postObject(
    String path, {
    Object? body,
    Map<String, Object?>? query,
  }) async {
    final data = await _send('POST', path, body: body, query: query);
    return data as Json;
  }

  /// A POST that may legitimately answer with no `data` (null instead of an object).
  Future<Json?> postObjectOrNull(String path, {Object? body}) async {
    final data = await _send('POST', path, body: body);
    return data as Json?;
  }

  Future<List<Json>> postList(String path, {Object? body}) async {
    final data = await _send('POST', path, body: body);
    return (data as List<Object?>).cast<Json>();
  }

  /// For endpoints whose response carries no `data` (verify, resend, ...).
  Future<void> postVoid(String path, {Object? body}) =>
      _send('POST', path, body: body);

  Future<Json> putObject(
    String path, {
    Object? body,
    Map<String, Object?>? query,
  }) async {
    final data = await _send('PUT', path, body: body, query: query);
    return data as Json;
  }

  Future<void> delete(String path, {Map<String, Object?>? query}) =>
      _send('DELETE', path, query: query);
}

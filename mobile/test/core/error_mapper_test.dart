import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/network/api_exception.dart';
import 'package:safewalk_mobile/core/network/error_mapper.dart';

DioException _error(int? status, [Object? body]) {
  final options = RequestOptions(path: '/x');
  return DioException(
    requestOptions: options,
    response: status == null
        ? null
        : Response(requestOptions: options, statusCode: status, data: body),
  );
}

void main() {
  test('uses the backend message', () {
    final e = mapDioError(_error(400, {'message': 'Email already used'}));
    expect(e.statusCode, 400);
    expect(e.message, 'Email already used');
    expect(e.failure, ApiFailure.server);
  });

  test('flags 401 and 403', () {
    expect(
      mapDioError(_error(401, {'message': 'Unauthorized'})).failure,
      ApiFailure.unauthorized,
    );
    expect(
      mapDioError(_error(403, {'message': 'Not allowed'})).failure,
      ApiFailure.forbidden,
    );
  });

  test('treats no response as a network failure', () {
    final e = mapDioError(_error(null));
    expect(e.failure, ApiFailure.network);
    expect(e.statusCode, 0);
  });
}

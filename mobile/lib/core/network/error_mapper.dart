import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';

import 'api_envelope.dart';
import 'api_exception.dart';

const _offlineMessage =
    "Can't reach SafeWalk. Check your connection and try again.";

/// Turns any Dio failure into an [ApiException] the UI can show as-is.
ApiException mapDioError(DioException error) {
  final response = error.response;
  if (response == null) {
    if (kDebugMode && error.type != DioExceptionType.cancel) {
      debugPrint('Network failure: ${error.type} ${error.error}');
    }
    return const ApiException(0, _offlineMessage, failure: ApiFailure.network);
  }
  final status = response.statusCode ?? 0;
  final message =
      messageOf(response.data) ?? response.statusMessage ?? 'Request failed';
  return ApiException(
    status,
    message,
    failure: switch (status) {
      401 => ApiFailure.unauthorized,
      403 => ApiFailure.forbidden,
      _ => ApiFailure.server,
    },
  );
}

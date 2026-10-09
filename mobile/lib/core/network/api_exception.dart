enum ApiFailure { server, unauthorized, forbidden, network }

/// Every failure the UI can see from the backend. `message` is safe to show to the user.
class ApiException implements Exception {
  const ApiException(
    this.statusCode,
    this.message, {
    this.failure = ApiFailure.server,
  });

  /// HTTP status, or 0 when there was no response at all.
  final int statusCode;
  final String message;
  final ApiFailure failure;

  bool get isNotFound => statusCode == 404;

  @override
  String toString() => 'ApiException($statusCode, $message)';
}

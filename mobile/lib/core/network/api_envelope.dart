/// Backend responses look like `{"message": "...", "data": ...}`.
Object? unwrapEnvelope(Object? body) {
  if (body is Map<String, Object?> && body.containsKey('data')) {
    return body['data'];
  }
  return null;
}

/// The `message` of an envelope or of a security-handler 401/403 body, when there is one.
String? messageOf(Object? body) {
  if (body is Map<String, Object?>) {
    final message = body['message'];
    if (message is String && message.isNotEmpty) return message;
  }
  return null;
}

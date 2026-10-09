import 'dart:convert';

import 'session_user.dart';

class DecodedToken {
  const DecodedToken({required this.user, required this.expiresAt});

  final SessionUser user;
  final DateTime expiresAt;

  bool isExpiredAt(DateTime now) => !expiresAt.isAfter(now);
}

/// Reads claims from a backend JWT WITHOUT verifying the signature (the server does that on each request).
DecodedToken? decodeToken(String token) {
  try {
    final parts = token.split('.');
    if (parts.length < 2) return null;
    final payload = jsonDecode(
      utf8.decode(base64Url.decode(base64Url.normalize(parts[1]))),
    );
    if (payload is! Map<String, Object?>) return null;
    final id = payload['id'];
    final sub = payload['sub'];
    final exp = payload['exp'];
    if (id is! num || sub is! String || exp is! num) return null;
    final roles = payload['roles'];
    return DecodedToken(
      user: SessionUser(
        id: id.toInt(),
        email: sub,
        roles: roles is List ? roles.whereType<String>().toList() : const [],
      ),
      expiresAt: DateTime.fromMillisecondsSinceEpoch(exp.toInt() * 1000),
    );
  } on FormatException {
    return null;
  }
}

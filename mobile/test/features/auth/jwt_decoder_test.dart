import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/auth/domain/jwt_decoder.dart';

String _token(Map<String, Object?> payload) {
  String part(Object value) =>
      base64Url.encode(utf8.encode(jsonEncode(value))).replaceAll('=', '');
  return '${part({'alg': 'HS256'})}.${part(payload)}.signature';
}

void main() {
  test('reads id, email, roles and expiry', () {
    final decoded = decodeToken(
      _token({
        'id': 7,
        'sub': 'a@b.com',
        'roles': ['ROLE_USER'],
        'exp': 2000000000,
      }),
    );
    expect(decoded, isNotNull);
    expect(decoded!.user.id, 7);
    expect(decoded.user.email, 'a@b.com');
    expect(decoded.user.isAdmin, isFalse);
    expect(
      decoded.expiresAt,
      DateTime.fromMillisecondsSinceEpoch(2000000000 * 1000),
    );
  });

  test('knows an admin', () {
    final decoded = decodeToken(
      _token({
        'id': 1,
        'sub': 'x@y.z',
        'roles': ['ROLE_ADMIN'],
        'exp': 2000000000,
      }),
    );
    expect(decoded!.user.isAdmin, isTrue);
  });

  test('detects expiry', () {
    final decoded = decodeToken(
      _token({'id': 1, 'sub': 'x@y.z', 'roles': [], 'exp': 1000}),
    )!;
    expect(decoded.isExpiredAt(DateTime.now()), isTrue);
  });

  test('rejects malformed tokens', () {
    expect(decodeToken('not-a-token'), isNull);
    expect(decodeToken('a.b.c'), isNull);
    expect(decodeToken(_token({'sub': 'no-id', 'exp': 1})), isNull);
  });
}

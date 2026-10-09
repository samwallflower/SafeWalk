import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/network/api_envelope.dart';

void main() {
  group('unwrapEnvelope', () {
    test('returns data from a normal response', () {
      expect(
        unwrapEnvelope({
          'message': 'ok',
          'data': [1, 2],
        }),
        [1, 2],
      );
    });

    test('returns null when there is no data key', () {
      expect(unwrapEnvelope({'message': 'deleted'}), isNull);
      expect(unwrapEnvelope(null), isNull);
    });
  });

  group('messageOf', () {
    test('reads the message of envelope and security-handler bodies', () {
      expect(messageOf({'message': 'Bad credentials'}), 'Bad credentials');
    });

    test('ignores empty or missing messages', () {
      expect(messageOf({'message': ''}), isNull);
      expect(messageOf('text'), isNull);
    });
  });
}

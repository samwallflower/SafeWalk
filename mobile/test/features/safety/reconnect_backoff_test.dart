import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/safety/domain/idle_prompt.dart';
import 'package:safewalk_mobile/features/safety/domain/reconnect_backoff.dart';

void main() {
  test('reconnect waits 1, 2, 4, 8, 16 and then at most 30 seconds', () {
    expect(
      [for (var i = 0; i < 8; i++) reconnectDelay(i).inSeconds],
      [1, 2, 4, 8, 16, 30, 30, 30],
    );
  });

  test('a huge attempt number does not overflow', () {
    expect(reconnectDelay(1000).inSeconds, 30);
  });

  group('IdlePrompt', () {
    final start = DateTime(2026, 10, 10, 12);

    test('counts down from the 45 second grace period', () {
      final prompt = IdlePrompt.startingAt(start);
      expect(prompt.remaining(start), const Duration(seconds: 45));
      expect(
        prompt.remaining(start.add(const Duration(seconds: 30))),
        const Duration(seconds: 15),
      );
      expect(prompt.expiredAt(start.add(const Duration(seconds: 44))), isFalse);
    });

    test('never goes negative once it has run out', () {
      final prompt = IdlePrompt.startingAt(start);
      final late = start.add(const Duration(seconds: 90));
      expect(prompt.remaining(late), Duration.zero);
      expect(prompt.expiredAt(late), isTrue);
    });
  });
}

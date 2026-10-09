import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/format/format.dart';

void main() {
  final now = DateTime(2026, 10, 9, 12, 0, 0);

  test('formats how long ago a zone-less timestamp was', () {
    expect(formatRelative('2026-10-09T11:59:40', now: now), 'just now');
    expect(formatRelative('2026-10-09T11:36:00', now: now), '24m ago');
    expect(formatRelative('2026-10-09T09:00:00', now: now), '3h ago');
    expect(formatRelative('2026-10-07T12:00:00', now: now), '2d ago');
  });

  test('is empty for garbage', () {
    expect(formatRelative('not a date', now: now), '');
  });
}

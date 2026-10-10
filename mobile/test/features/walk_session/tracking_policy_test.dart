import 'package:flutter_test/flutter_test.dart';
import 'package:latlong2/latlong.dart';
import 'package:safewalk_mobile/features/walk_session/domain/tracking_policy.dart';

void main() {
  final t0 = DateTime(2026, 10, 10, 12);
  const here = LatLng(52.9548, -1.1581);
  const movedFar = LatLng(52.9558, -1.1581); // about 110 m
  const movedLittle = LatLng(52.95481, -1.1581); // about 1 m

  test('the first position is always sent', () {
    expect(
      shouldSendLocation(
        now: t0,
        position: here,
        lastSentAt: null,
        lastSentPosition: null,
      ),
      isTrue,
    );
  });

  test('never more often than every 10 seconds', () {
    expect(
      shouldSendLocation(
        now: t0.add(const Duration(seconds: 5)),
        position: movedFar,
        lastSentAt: t0,
        lastSentPosition: here,
      ),
      isFalse,
    );
  });

  test('sends once you have moved enough and 10 seconds passed', () {
    expect(
      shouldSendLocation(
        now: t0.add(const Duration(seconds: 12)),
        position: movedFar,
        lastSentAt: t0,
        lastSentPosition: here,
      ),
      isTrue,
    );
  });

  test('standing still sends nothing until the heartbeat', () {
    expect(
      shouldSendLocation(
        now: t0.add(const Duration(seconds: 15)),
        position: movedLittle,
        lastSentAt: t0,
        lastSentPosition: here,
      ),
      isFalse,
    );
    expect(
      shouldSendLocation(
        now: t0.add(const Duration(seconds: 31)),
        position: movedLittle,
        lastSentAt: t0,
        lastSentPosition: here,
      ),
      isTrue,
    );
  });

  test(
    'the heartbeat is well inside the 3 minute idle limit of the server',
    () {
      expect(TrackingPolicy.heartbeat, lessThan(const Duration(minutes: 3)));
    },
  );
}

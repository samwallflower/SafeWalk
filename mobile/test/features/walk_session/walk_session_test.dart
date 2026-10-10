import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/walk_session/domain/walk_session.dart';

void main() {
  test('parses a WalkSessionDto', () {
    final session = WalkSession.fromJson({
      'id': 4,
      'userId': 9,
      'routeId': 12,
      'startTime': '2026-10-10T10:00:00',
      'endTime': null,
      'status': 'ACTIVE',
      'originLatitude': 52.95,
      'originLongitude': -1.15,
      'destinationLatitude': 52.97,
      'destinationLongitude': -1.13,
      'autoCompleted': false,
    });
    expect(session.id, 4);
    expect(session.routeId, 12);
    expect(session.status, SessionStatus.active);
    expect(session.isFinished, isFalse);
  });

  test('knows every status and flags completed walks', () {
    expect(SessionStatus.parse('COMPLETED'), SessionStatus.completed);
    expect(SessionStatus.parse('EMERGENCY'), SessionStatus.emergency);
    expect(SessionStatus.parse('ABANDONED'), SessionStatus.abandoned);
    expect(
      WalkSession.fromJson({
        'id': 1,
        'startTime': 'x',
        'status': 'COMPLETED',
        'autoCompleted': true,
      }).autoCompleted,
      isTrue,
    );
  });
}

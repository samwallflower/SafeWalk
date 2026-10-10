import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/dashboard/domain/dashboard_stats.dart';
import 'package:safewalk_mobile/features/dashboard/domain/walked_distance.dart';
import 'package:safewalk_mobile/features/walk_session/domain/walk_session.dart';

WalkSession _walk(String start, SessionStatus status, {int? route = 1}) =>
    WalkSession(
      id: 1,
      userId: 1,
      routeId: route,
      startTime: start,
      endTime: null,
      status: status,
      originLatitude: null,
      originLongitude: null,
      destinationLatitude: null,
      destinationLongitude: null,
      autoCompleted: false,
    );

void main() {
  final now = DateTime(2026, 10, 10, 15);

  group('walksPerDay', () {
    test('has one entry per day, today last, with zeros for quiet days', () {
      final days = walksPerDay(
        [_walk('2026-10-10T09:00:00', SessionStatus.completed)],
        days: 7,
        now: now,
      );
      expect(days, hasLength(7));
      expect(days.last.day, DateTime(2026, 10, 10));
      expect(days.last.count, 1);
      expect(days.first.day, DateTime(2026, 10, 4));
      expect(days.take(6).every((d) => d.count == 0), isTrue);
    });

    test(
      'counts several walks on one day and ignores abandoned or old ones',
      () {
        final days = walksPerDay(
          [
            _walk('2026-10-09T08:00:00', SessionStatus.completed),
            _walk('2026-10-09T18:00:00', SessionStatus.emergency),
            _walk('2026-10-09T19:00:00', SessionStatus.abandoned),
            _walk('2026-09-01T09:00:00', SessionStatus.completed),
          ],
          days: 7,
          now: now,
        );
        expect(days.firstWhere((d) => d.day == DateTime(2026, 10, 9)).count, 2);
        expect(days.fold<int>(0, (s, d) => s + d.count), 2);
      },
    );
  });

  test('completedWalks counts only walks that ended normally', () {
    expect(
      completedWalks([
        _walk('2026-10-10T09:00:00', SessionStatus.completed),
        _walk('2026-10-10T10:00:00', SessionStatus.emergency),
        _walk('2026-10-10T11:00:00', SessionStatus.completed),
      ]),
      2,
    );
  });

  group('walked distance', () {
    test('only completed walks with a route count, newest first', () {
      final walked = completedRouteIds([
        _walk('2026-10-08T09:00:00', SessionStatus.completed, route: 3),
        _walk('2026-10-10T09:00:00', SessionStatus.completed, route: 1),
        _walk('2026-10-09T09:00:00', SessionStatus.active, route: 2),
        _walk('2026-10-07T09:00:00', SessionStatus.completed, route: null),
      ]);
      expect(walked.routeIds, [1, 3]);
      expect(walked.capped, isFalse);
    });

    test('only the most recent 50 walks are looked up', () {
      final many = [
        for (var i = 0; i < 55; i++)
          _walk(
            '2026-09-${(i % 28) + 1}T09:00:00',
            SessionStatus.completed,
            route: i + 1,
          ),
      ];
      final walked = completedRouteIds(many);
      expect(walked.routeIds, hasLength(maxWalksCounted));
      expect(walked.capped, isTrue);
    });

    test('the total adds each walk, counts a repeated route twice, and skips routes that did not load', () {
      expect(totalWalkedMeters([1, 2, 1], {1: 1200, 2: 800}), 3200);
      expect(totalWalkedMeters([9], {}), 0);
    });
  });
}

import '../../walk_session/domain/walk_session.dart';

int completedWalks(Iterable<WalkSession> sessions) =>
    sessions.where((s) => s.status == SessionStatus.completed).length;

class DayCount {
  const DayCount({required this.day, required this.count});

  final DateTime day;
  final int count;
}

/// Walks started on each of the last [days] days (today last), counting every walk that was not abandoned.
List<DayCount> walksPerDay(
  Iterable<WalkSession> sessions, {
  int days = 7,
  DateTime? now,
}) {
  final today = now ?? DateTime.now();
  final end = DateTime(today.year, today.month, today.day);
  final counts = <DateTime, int>{
    for (var i = days - 1; i >= 0; i--) end.subtract(Duration(days: i)): 0,
  };
  for (final session in sessions) {
    if (session.status == SessionStatus.abandoned) continue;
    final started = DateTime.tryParse(session.startTime);
    if (started == null) continue;
    final day = DateTime(started.year, started.month, started.day);
    if (counts.containsKey(day)) counts[day] = counts[day]! + 1;
  }
  return [
    for (final entry in counts.entries)
      DayCount(day: entry.key, count: entry.value),
  ];
}

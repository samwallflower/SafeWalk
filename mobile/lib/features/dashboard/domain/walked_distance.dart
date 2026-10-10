import '../../walk_session/domain/walk_session.dart';

/// Each completed walk's route is one request, so only the most recent walks are looked up.
const maxWalksCounted = 50;

class WalkedRoutes {
  const WalkedRoutes({required this.routeIds, required this.capped});

  final List<int> routeIds;

  /// There were more completed walks than [maxWalksCounted].
  final bool capped;
}

/// The route ids of completed walks, newest first (one entry per walk, so a repeated route counts again).
WalkedRoutes completedRouteIds(Iterable<WalkSession> sessions) {
  final completed =
      sessions
          .where(
            (s) => s.status == SessionStatus.completed && s.routeId != null,
          )
          .toList()
        ..sort((a, b) => b.startTime.compareTo(a.startTime));
  final ids = [for (final s in completed) s.routeId!];
  return WalkedRoutes(
    routeIds: ids.take(maxWalksCounted).toList(),
    capped: ids.length > maxWalksCounted,
  );
}

/// The sum of route lengths. A route that could not be loaded counts as zero.
double totalWalkedMeters(List<int> routeIds, Map<int, double> lengthByRoute) =>
    routeIds.fold(0.0, (sum, id) => sum + (lengthByRoute[id] ?? 0));

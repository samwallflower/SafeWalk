import 'route_option.dart';

enum RiskLevel { none, lowest, higher }

class RouteRisk {
  const RouteRisk({
    required this.level,
    required this.label,
    required this.barFraction,
  });

  final RiskLevel level;
  final String label;

  /// 0..1 width of the risk bar.
  final double barFraction;
}

/// How risky a route is compared with the other options. It is shown relatively on purpose: the penalty
/// numbers themselves mean little to people, but "+40% vs lowest" is clear.
RouteRisk riskOf(
  RouteOption route, {
  required double minPenalty,
  required double maxPenalty,
}) {
  final penalty = route.safetyPenaltyMeters;
  if (penalty <= 0) {
    return const RouteRisk(
      level: RiskLevel.none,
      label: 'None nearby',
      barFraction: 0.04,
    );
  }
  final fraction = maxPenalty <= 0
      ? 0.04
      : (penalty / maxPenalty).clamp(0.08, 1.0);
  if (penalty == minPenalty) {
    return RouteRisk(
      level: RiskLevel.lowest,
      label: 'Lowest of these routes',
      barFraction: fraction,
    );
  }
  final percent =
      ((penalty - minPenalty) / (minPenalty < 1 ? 1 : minPenalty) * 100)
          .round();
  return RouteRisk(
    level: RiskLevel.higher,
    label: '+$percent% vs lowest',
    barFraction: fraction,
  );
}

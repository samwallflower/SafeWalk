import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/routing/domain/route_option.dart';
import 'package:safewalk_mobile/features/routing/domain/route_risk.dart';

RouteOption _route(int rank, double penalty, {double distance = 1000}) =>
    RouteOption(
      id: rank,
      polyline: '',
      actualDistanceMeters: distance,
      safetyPenaltyMeters: penalty,
      virtualDistanceMeters: distance + penalty,
      rank: rank,
      routeRequestId: 'r',
    );

void main() {
  test('no penalty means no incidents nearby', () {
    final risk = riskOf(_route(1, 0), minPenalty: 0, maxPenalty: 300);
    expect(risk.level, RiskLevel.none);
    expect(risk.label, 'None nearby');
  });

  test('the smallest penalty is the lowest of the options', () {
    final risk = riskOf(_route(1, 100), minPenalty: 100, maxPenalty: 300);
    expect(risk.level, RiskLevel.lowest);
  });

  test('a higher penalty is shown as a percentage over the lowest', () {
    final risk = riskOf(_route(2, 150), minPenalty: 100, maxPenalty: 300);
    expect(risk.level, RiskLevel.higher);
    expect(risk.label, '+50% vs lowest');
    expect(risk.barFraction, closeTo(0.5, 1e-9));
  });

  test('the worst route fills the bar', () {
    expect(
      riskOf(_route(3, 300), minPenalty: 100, maxPenalty: 300).barFraction,
      1,
    );
  });
}

import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/routing/domain/route_option.dart';

Map<String, Object?> _json(int id, int rank) => {
  'id': id,
  'polyline': r'_p~iF~ps|U_ulLnnqC_mqNvxq`@',
  'actualDistanceMeters': 1200.5,
  'safetyPenaltyMeters': 40,
  'virtualDistanceMeters': 1240.5,
  'rank': rank,
  'routeRequestId': 'req-1',
};

void main() {
  test('parses a RouteDto', () {
    final route = RouteOption.fromJson(_json(5, 1));
    expect(route.id, 5);
    expect(route.actualDistanceMeters, 1200.5);
    expect(route.safetyPenaltyMeters, 40);
    expect(route.rank, 1);
  });

  test('decodeRoutes sorts best first and decodes each polyline', () {
    final decoded = decodeRoutes([
      RouteOption.fromJson(_json(2, 2)),
      RouteOption.fromJson(_json(1, 1)),
    ]);
    expect(decoded.map((r) => r.route.rank), [1, 2]);
    expect(decoded.first.points, hasLength(3));
    expect(decoded.first.color, recommendedColor);
    expect(decoded.last.color, isNot(recommendedColor));
  });
}

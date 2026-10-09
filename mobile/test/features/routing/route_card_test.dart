import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/theme/theme.dart';
import 'package:safewalk_mobile/features/routing/domain/route_option.dart';
import 'package:safewalk_mobile/features/routing/presentation/widgets/route_card.dart';

DecodedRoute _decoded(int id, int rank, double distance, double penalty) =>
    decodeRoutes([
      RouteOption(
        id: id,
        polyline: '',
        actualDistanceMeters: distance,
        safetyPenaltyMeters: penalty,
        virtualDistanceMeters: distance + penalty,
        rank: rank,
        routeRequestId: 'r',
      ),
    ]).single;

Widget _app(Widget child) => MaterialApp(
  theme: buildTheme(downloadFont: false),
  home: Scaffold(
    body: Padding(padding: const EdgeInsets.all(16), child: child),
  ),
);

void main() {
  testWidgets(
    'the recommended card shows distance, walking estimate and lowest risk',
    (tester) async {
      final best = _decoded(1, 1, 2400, 50);
      await tester.pumpWidget(
        _app(
          RouteCard(
            item: best,
            recommended: best,
            minPenalty: 50,
            maxPenalty: 200,
            selected: true,
            onSelect: () {},
          ),
        ),
      );
      expect(find.text('Route 1'), findsOneWidget);
      expect(find.text('Recommended'), findsOneWidget);
      expect(find.text('2.4 km'), findsOneWidget);
      expect(find.text('about 29 min walk (estimate)'), findsOneWidget);
      expect(find.text('Lowest of these routes'), findsOneWidget);
    },
  );

  testWidgets('an alternative shows the extra distance and relative risk', (
    tester,
  ) async {
    final best = _decoded(1, 1, 2000, 50);
    final other = _decoded(2, 2, 2300, 100);
    var tapped = false;
    await tester.pumpWidget(
      _app(
        RouteCard(
          item: other,
          recommended: best,
          minPenalty: 50,
          maxPenalty: 100,
          selected: false,
          onSelect: () => tapped = true,
        ),
      ),
    );
    expect(find.text('Alternative'), findsOneWidget);
    expect(find.text('+300 m vs Route 1'), findsOneWidget);
    expect(find.text('+100% vs lowest'), findsOneWidget);

    await tester.tap(find.text('Route 2'));
    expect(tapped, isTrue);
  });
}

import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/network/api_exception.dart';
import 'package:safewalk_mobile/features/places/domain/place.dart';
import 'package:safewalk_mobile/features/routing/data/routing_api.dart';
import 'package:safewalk_mobile/features/routing/domain/route_option.dart';
import 'package:safewalk_mobile/features/routing/presentation/state/plan_controller.dart';

class _FakeRoutingApi implements RoutingApi {
  _FakeRoutingApi(this.handler);

  final Future<List<RouteOption>> Function(RouteRequest request) handler;
  int calls = 0;

  @override
  Future<List<RouteOption>> recommend(RouteRequest request) {
    calls++;
    return handler(request);
  }
}

RouteOption _route(int id, int rank) => RouteOption(
  id: id,
  polyline: r'_p~iF~ps|U_ulLnnqC_mqNvxq`@',
  actualDistanceMeters: 1000.0 + rank * 100,
  safetyPenaltyMeters: rank * 10.0,
  virtualDistanceMeters: 1000,
  rank: rank,
  routeRequestId: 'r',
);

const _a = Place(label: 'A', latitude: 52.95, longitude: -1.15);
const _b = Place(label: 'B', latitude: 52.97, longitude: -1.13);

ProviderContainer _container(_FakeRoutingApi api) {
  final container = ProviderContainer(
    overrides: [routingApiProvider.overrideWithValue(api)],
  );
  addTearDown(container.dispose);
  return container;
}

void main() {
  test('finds routes, sorts them, and selects the recommended one', () async {
    final api = _FakeRoutingApi((_) async => [_route(2, 2), _route(1, 1)]);
    final c = _container(api);
    final plan = c.read(planProvider.notifier);
    plan.setOrigin(_a);
    plan.setDestination(_b);

    await plan.find();

    final state = c.read(planProvider);
    expect(state.status, PlanStatus.ready);
    expect(state.routes.map((r) => r.route.rank), [1, 2]);
    expect(state.selectedId, 1);
  });

  test('cannot search without both places', () {
    final c = _container(_FakeRoutingApi((_) async => []));
    expect(c.read(planProvider).canSearch, isFalse);
    c.read(planProvider.notifier).setOrigin(_a);
    expect(c.read(planProvider).canSearch, isFalse);
    c.read(planProvider.notifier).setDestination(_b);
    expect(c.read(planProvider).canSearch, isTrue);
  });

  test(
    'refuses the same start and destination without calling the server',
    () async {
      final api = _FakeRoutingApi((_) async => []);
      final c = _container(api);
      final plan = c.read(planProvider.notifier);
      plan.setOrigin(_a);
      plan.setDestination(
        const Place(label: 'A again', latitude: 52.95001, longitude: -1.15001),
      );

      await plan.find();

      expect(api.calls, 0);
      expect(c.read(planProvider).status, PlanStatus.error);
      expect(c.read(planProvider).error, contains('same place'));
    },
  );

  test('shows the server message when the request fails', () async {
    final api = _FakeRoutingApi(
      (_) async => throw const ApiException(400, 'No walking route found'),
    );
    final c = _container(api);
    final plan = c.read(planProvider.notifier)
      ..setOrigin(_a)
      ..setDestination(_b);

    await plan.find();

    expect(c.read(planProvider).status, PlanStatus.error);
    expect(c.read(planProvider).error, 'No walking route found');
  });

  test('changing a place throws away stale results', () async {
    final api = _FakeRoutingApi((_) async => [_route(1, 1)]);
    final c = _container(api);
    final plan = c.read(planProvider.notifier)
      ..setOrigin(_a)
      ..setDestination(_b);
    await plan.find();
    expect(c.read(planProvider).routes, isNotEmpty);

    plan.setDestination(
      const Place(label: 'C', latitude: 52.99, longitude: -1.1),
    );

    expect(c.read(planProvider).routes, isEmpty);
    expect(c.read(planProvider).status, PlanStatus.idle);
  });

  test('a slow answer for an old search is ignored', () async {
    final slow = Completer<List<RouteOption>>();
    final api = _FakeRoutingApi((_) => slow.future);
    final c = _container(api);
    final plan = c.read(planProvider.notifier)
      ..setOrigin(_a)
      ..setDestination(_b);

    final pending = plan.find();
    plan.setDestination(
      const Place(label: 'C', latitude: 52.99, longitude: -1.1),
    );
    slow.complete([_route(1, 1)]);
    await pending;

    expect(c.read(planProvider).routes, isEmpty);
    expect(c.read(planProvider).status, PlanStatus.idle);
  });

  test('swap exchanges start and destination', () {
    final c = _container(_FakeRoutingApi((_) async => []));
    final plan = c.read(planProvider.notifier)
      ..setOrigin(_a)
      ..setDestination(_b);
    plan.swap();
    expect(c.read(planProvider).origin?.label, 'B');
    expect(c.read(planProvider).destination?.label, 'A');
  });

  test('selecting another route changes the selection', () async {
    final api = _FakeRoutingApi((_) async => [_route(1, 1), _route(2, 2)]);
    final c = _container(api);
    final plan = c.read(planProvider.notifier)
      ..setOrigin(_a)
      ..setDestination(_b);
    await plan.find();
    plan.select(2);
    expect(c.read(planProvider).selected?.route.rank, 2);
  });
}

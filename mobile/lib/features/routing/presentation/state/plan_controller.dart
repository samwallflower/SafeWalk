import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/geo/haversine.dart';
import '../../../../core/network/api_exception.dart';
import '../../../places/domain/place.dart';

import 'package:latlong2/latlong.dart';

import '../../data/routing_api.dart';
import '../../domain/route_option.dart';

enum PlanStatus { idle, loading, ready, error }

class PlanState {
  const PlanState({
    this.origin,
    this.destination,
    this.status = PlanStatus.idle,
    this.routes = const [],
    this.selectedId,
    this.error,
  });

  final Place? origin;
  final Place? destination;
  final PlanStatus status;
  final List<DecodedRoute> routes;
  final int? selectedId;
  final String? error;

  bool get canSearch =>
      origin != null && destination != null && status != PlanStatus.loading;

  DecodedRoute? get selected {
    for (final r in routes) {
      if (r.route.id == selectedId) return r;
    }
    return null;
  }

  PlanState copyWith({
    Place? Function()? origin,
    Place? Function()? destination,
    PlanStatus? status,
    List<DecodedRoute>? routes,
    int? Function()? selectedId,
    String? Function()? error,
  }) => PlanState(
    origin: origin != null ? origin() : this.origin,
    destination: destination != null ? destination() : this.destination,
    status: status ?? this.status,
    routes: routes ?? this.routes,
    selectedId: selectedId != null ? selectedId() : this.selectedId,
    error: error != null ? error() : this.error,
  );
}

final planProvider = NotifierProvider<PlanController, PlanState>(
  PlanController.new,
);

class PlanController extends Notifier<PlanState> {
  int _requestCounter = 0;

  @override
  PlanState build() => const PlanState();

  /// Changing a place throws away old results, which no longer match.
  void setOrigin(Place? place) => _setPlaces(origin: () => place);
  void setDestination(Place? place) => _setPlaces(destination: () => place);

  void swap() {
    state = PlanState(origin: state.destination, destination: state.origin);
  }

  void _setPlaces({Place? Function()? origin, Place? Function()? destination}) {
    _requestCounter++;
    state = PlanState(
      origin: origin != null ? origin() : state.origin,
      destination: destination != null ? destination() : state.destination,
    );
  }

  void select(int routeId) => state = state.copyWith(selectedId: () => routeId);

  void reset() {
    _requestCounter++;
    state = const PlanState();
  }

  Future<void> find() async {
    final origin = state.origin;
    final destination = state.destination;
    if (origin == null ||
        destination == null ||
        state.status == PlanStatus.loading) {
      return;
    }

    final apart = haversineMeters(
      LatLng(origin.latitude, origin.longitude),
      LatLng(destination.latitude, destination.longitude),
    );
    if (apart < 30) {
      state = state.copyWith(
        status: PlanStatus.error,
        error: () => 'The start and the destination are the same place.',
      );
      return;
    }

    final ticket = ++_requestCounter;
    state = state.copyWith(
      status: PlanStatus.loading,
      routes: const [],
      selectedId: () => null,
      error: () => null,
    );
    try {
      final options = await ref
          .read(routingApiProvider)
          .recommend(
            RouteRequest(
              originLatitude: origin.latitude,
              originLongitude: origin.longitude,
              destinationLatitude: destination.latitude,
              destinationLongitude: destination.longitude,
            ),
          );
      if (ticket != _requestCounter) return;
      final decoded = decodeRoutes(options);
      state = state.copyWith(
        status: PlanStatus.ready,
        routes: decoded,
        selectedId: () => decoded.isEmpty ? null : decoded.first.route.id,
      );
    } on ApiException catch (e) {
      if (ticket != _requestCounter) return;
      state = state.copyWith(status: PlanStatus.error, error: () => e.message);
    }
  }
}

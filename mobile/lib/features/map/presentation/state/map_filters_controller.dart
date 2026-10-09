import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/map_models.dart';

final mapFiltersProvider = NotifierProvider<MapFiltersController, MapFilters>(
  MapFiltersController.new,
);

class MapFiltersController extends Notifier<MapFilters> {
  @override
  MapFilters build() => const MapFilters();

  void setCategory(int? id) => state = state.copyWith(categoryId: () => id);
  void setRange(TimeRange range) => state = state.copyWith(range: range);
  void clear() => state = const MapFilters();
}

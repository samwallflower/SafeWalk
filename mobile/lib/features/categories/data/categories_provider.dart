import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/providers.dart';
import '../domain/incident_category.dart';

/// Categories are public and change rarely, so they are kept for the whole session.
final categoriesProvider = FutureProvider<List<IncidentCategory>>((ref) async {
  final list = await ref
      .watch(apiClientProvider)
      .getList('/incident-categories/all');
  return list.map(IncidentCategory.fromJson).toList();
});

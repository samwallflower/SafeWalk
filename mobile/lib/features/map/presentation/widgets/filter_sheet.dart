import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/widgets/error_state.dart';
import '../../../../core/widgets/loading.dart';
import '../../../categories/data/categories_provider.dart';
import '../../domain/map_models.dart';
import '../state/map_filters_controller.dart';

Future<void> showFilterSheet(BuildContext context) =>
    showModalBottomSheet<void>(
      context: context,
      showDragHandle: true,
      isScrollControlled: true,
      builder: (context) => const FilterSheet(),
    );

class FilterSheet extends ConsumerWidget {
  const FilterSheet({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final filters = ref.watch(mapFiltersProvider);
    final controller = ref.read(mapFiltersProvider.notifier);
    final categories = ref.watch(categoriesProvider);
    final theme = Theme.of(context);

    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 4, 20, 20),
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                children: [
                  Expanded(
                    child: Text(
                      'Filter incidents',
                      style: theme.textTheme.titleLarge,
                    ),
                  ),
                  TextButton(
                    onPressed: filters.isActive ? controller.clear : null,
                    child: const Text('Clear'),
                  ),
                ],
              ),
              const SizedBox(height: 4),
              Text(
                'Filters apply when you are zoomed in to a neighbourhood.',
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: 20),
              Text('Category', style: theme.textTheme.titleSmall),
              const SizedBox(height: 8),
              categories.when(
                loading: () => const SizedBox(
                  height: 80,
                  child: LoadingView(label: 'Loading categories'),
                ),
                error: (error, _) => ErrorState(
                  message: 'Categories failed to load.',
                  onRetry: () => ref.invalidate(categoriesProvider),
                ),
                data: (items) => Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: [
                    ChoiceChip(
                      label: const Text('All'),
                      selected: filters.categoryId == null,
                      onSelected: (_) => controller.setCategory(null),
                    ),
                    for (final category in items)
                      ChoiceChip(
                        label: Text(category.name),
                        selected: filters.categoryId == category.id,
                        onSelected: (_) => controller.setCategory(category.id),
                      ),
                  ],
                ),
              ),
              const SizedBox(height: 20),
              Text('Reported', style: theme.textTheme.titleSmall),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: [
                  for (final range in TimeRange.values)
                    ChoiceChip(
                      label: Text(range.label),
                      selected: filters.range == range,
                      onSelected: (_) => controller.setRange(range),
                    ),
                ],
              ),
              const SizedBox(height: 24),
              FilledButton(
                onPressed: () => Navigator.of(context).pop(),
                child: const Text('Done'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

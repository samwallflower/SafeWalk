import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/error_state.dart';
import '../../../../core/widgets/empty_state.dart';
import '../../../../core/widgets/loading.dart';
import '../../../categories/data/categories_provider.dart';

class CategoryPicker extends ConsumerWidget {
  const CategoryPicker({
    super.key,
    required this.selectedId,
    required this.onChanged,
    this.errorText,
  });

  final int? selectedId;
  final void Function(int id) onChanged;
  final String? errorText;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final categories = ref.watch(categoriesProvider);
    final theme = Theme.of(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        categories.when(
          loading: () => const SizedBox(
            height: 120,
            child: LoadingView(label: 'Loading categories'),
          ),
          error: (error, _) => ErrorState(
            message: "Couldn't load the categories.",
            onRetry: () => ref.invalidate(categoriesProvider),
          ),
          data: (items) => items.isEmpty
              ? const EmptyState(
                  title: 'No categories available',
                  message: 'An administrator needs to add incident categories.',
                )
              : LayoutBuilder(
                  builder: (context, constraints) {
                    final width = (constraints.maxWidth - 12) / 2;
                    return Wrap(
                      spacing: 12,
                      runSpacing: 12,
                      children: [
                        for (final category in items)
                          SizedBox(
                            width: width,
                            child: _CategoryCard(
                              name: category.name,
                              description: category.description,
                              selected: category.id == selectedId,
                              onTap: () => onChanged(category.id),
                            ),
                          ),
                      ],
                    );
                  },
                ),
        ),
        if (errorText != null) ...[
          const SizedBox(height: 8),
          Text(
            errorText!,
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.error,
            ),
          ),
        ],
      ],
    );
  }
}

class _CategoryCard extends StatelessWidget {
  const _CategoryCard({
    required this.name,
    required this.description,
    required this.selected,
    required this.onTap,
  });

  final String name;
  final String? description;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Semantics(
      button: true,
      selected: selected,
      label: name,
      child: Material(
        color: selected ? AppColors.infoSoft : AppColors.muted,
        borderRadius: BorderRadius.circular(14),
        child: InkWell(
          borderRadius: BorderRadius.circular(14),
          onTap: onTap,
          child: Container(
            constraints: const BoxConstraints(minHeight: 64),
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(14),
              border: Border.all(
                color: selected ? AppColors.primary : Colors.transparent,
                width: 1.5,
              ),
            ),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        name,
                        style: theme.textTheme.titleSmall?.copyWith(
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                      if (description != null && description!.isNotEmpty) ...[
                        const SizedBox(height: 2),
                        Text(
                          description!,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style: theme.textTheme.bodySmall?.copyWith(
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
                if (selected)
                  const Padding(
                    padding: EdgeInsets.only(left: 6),
                    child: Icon(
                      Icons.check_circle,
                      size: 20,
                      color: AppColors.primary,
                    ),
                  ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

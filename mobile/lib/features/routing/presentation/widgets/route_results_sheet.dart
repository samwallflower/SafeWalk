import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../domain/route_option.dart';
import 'route_card.dart';
import 'safety_explainer.dart';

/// The ranked route cards in a sheet you can drag up or down.
class RouteResultsSheet extends StatelessWidget {
  const RouteResultsSheet({
    super.key,
    required this.routes,
    required this.selectedId,
    required this.onSelect,
    this.onStart,
    this.starting = false,
  });

  final List<DecodedRoute> routes;
  final int? selectedId;
  final void Function(DecodedRoute route) onSelect;

  /// Starts a walk on the selected route. Null hides the button.
  final VoidCallback? onStart;
  final bool starting;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final penalties = routes.map((r) => r.route.safetyPenaltyMeters).toList();
    final minPenalty = penalties.reduce(math.min);
    final maxPenalty = penalties.reduce(math.max);
    final selectedRank = routes
        .where((r) => r.route.id == selectedId)
        .firstOrNull
        ?.route
        .rank;

    return DraggableScrollableSheet(
      initialChildSize: 0.4,
      minChildSize: 0.15,
      maxChildSize: 0.88,
      snap: true,
      snapSizes: const [0.15, 0.4, 0.88],
      builder: (context, scroll) => Material(
        elevation: 8,
        shadowColor: Colors.black38,
        color: theme.colorScheme.surfaceContainerHighest,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(24)),
        clipBehavior: Clip.antiAlias,
        child: ListView(
          controller: scroll,
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
          children: [
            Center(
              child: Container(
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: theme.colorScheme.outline,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            const SizedBox(height: 12),
            Text(
              routes.length == 1
                  ? '1 walking route'
                  : '${routes.length} walking routes',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w800,
              ),
            ),
            const SizedBox(height: 12),
            if (onStart != null) ...[
              FilledButton.icon(
                onPressed: starting ? null : onStart,
                icon: starting
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(
                          strokeWidth: 2.5,
                          color: Colors.white,
                        ),
                      )
                    : const Icon(Icons.directions_walk),
                label: Text(
                  starting
                      ? 'Starting...'
                      : 'Start walking on Route ${selectedRank ?? 1}',
                ),
              ),
              const SizedBox(height: 12),
            ],
            for (final route in routes) ...[
              RouteCard(
                item: route,
                recommended: routes.first,
                minPenalty: minPenalty,
                maxPenalty: maxPenalty,
                selected: route.route.id == selectedId,
                onSelect: () => onSelect(route),
              ),
              const SizedBox(height: 12),
            ],
            const SafetyExplainer(),
          ],
        ),
      ),
    );
  }
}

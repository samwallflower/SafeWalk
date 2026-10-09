import 'package:flutter/material.dart';

import '../../../../core/format/format.dart';
import '../../../../core/theme/colors.dart';
import '../../domain/route_option.dart';
import '../../domain/route_risk.dart';

class RouteCard extends StatelessWidget {
  const RouteCard({
    super.key,
    required this.item,
    required this.recommended,
    required this.minPenalty,
    required this.maxPenalty,
    required this.selected,
    required this.onSelect,
  });

  final DecodedRoute item;
  final DecodedRoute recommended;
  final double minPenalty;
  final double maxPenalty;
  final bool selected;
  final VoidCallback onSelect;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final route = item.route;
    final isRecommended = route.rank == 1;
    final risk = riskOf(route, minPenalty: minPenalty, maxPenalty: maxPenalty);
    final extra =
        route.actualDistanceMeters - recommended.route.actualDistanceMeters;
    final riskColor = switch (risk.level) {
      RiskLevel.none => AppColors.success,
      RiskLevel.lowest => AppColors.foreground,
      RiskLevel.higher => AppColors.destructive,
    };

    return Semantics(
      button: true,
      selected: selected,
      label:
          'Route ${route.rank}, ${formatDistance(route.actualDistanceMeters)}, ${risk.label}',
      child: Material(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        child: InkWell(
          borderRadius: BorderRadius.circular(16),
          onTap: onSelect,
          child: Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(16),
              border: Border.all(
                color: selected ? AppColors.primary : AppColors.border,
                width: selected ? 2 : 1,
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Container(
                      width: 12,
                      height: 12,
                      decoration: BoxDecoration(
                        color: item.color,
                        shape: BoxShape.circle,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Text(
                      'Route ${route.rank}',
                      style: theme.textTheme.titleSmall?.copyWith(
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                    const Spacer(),
                    _Tag(
                      text: isRecommended ? 'Recommended' : 'Alternative',
                      background: isRecommended
                          ? AppColors.successSoft
                          : AppColors.muted,
                      foreground: isRecommended
                          ? AppColors.success
                          : AppColors.mutedForeground,
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                Row(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            formatDistance(route.actualDistanceMeters),
                            style: theme.textTheme.headlineSmall?.copyWith(
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                          Text(
                            'about ${estimateWalkingMinutes(route.actualDistanceMeters)} min walk (estimate)',
                            style: theme.textTheme.bodySmall?.copyWith(
                              color: theme.colorScheme.onSurfaceVariant,
                            ),
                          ),
                        ],
                      ),
                    ),
                    if (!isRecommended)
                      Text(
                        '${formatDistanceDelta(extra)} vs Route 1',
                        style: theme.textTheme.labelMedium?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                  ],
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Text(
                      'Incident risk',
                      style: theme.textTheme.bodyMedium?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                    const Spacer(),
                    Text(
                      risk.label,
                      style: theme.textTheme.bodyMedium?.copyWith(
                        fontWeight: FontWeight.w700,
                        color: riskColor,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                ClipRRect(
                  borderRadius: BorderRadius.circular(6),
                  child: Container(
                    height: 8,
                    color: AppColors.muted,
                    alignment: Alignment.centerLeft,
                    child: FractionallySizedBox(
                      widthFactor: risk.barFraction,
                      child: Container(color: item.color),
                    ),
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

class _Tag extends StatelessWidget {
  const _Tag({
    required this.text,
    required this.background,
    required this.foreground,
  });

  final String text;
  final Color background;
  final Color foreground;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: background,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        text,
        style: TextStyle(
          color: foreground,
          fontWeight: FontWeight.w700,
          fontSize: 12,
        ),
      ),
    );
  }
}

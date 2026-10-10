import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/widgets/section_header.dart';
import '../../data/walk_stats_providers.dart';
import '../../domain/dashboard_stats.dart';
import 'walks_chart.dart';

class WalksChartCard extends ConsumerWidget {
  const WalksChartCard({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sessions = ref.watch(mySessionsProvider);
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const SectionHeader('Walks, last 7 days'),
        const SizedBox(height: 8),
        Card(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 12),
            child: sessions.when(
              loading: () => const SizedBox(
                height: 140,
                child: Center(child: CircularProgressIndicator()),
              ),
              error: (error, _) => SizedBox(
                height: 140,
                child: Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        error is ApiException
                            ? error.message
                            : "Couldn't load your walks.",
                      ),
                      TextButton(
                        onPressed: () => ref.invalidate(mySessionsProvider),
                        child: const Text('Try again'),
                      ),
                    ],
                  ),
                ),
              ),
              data: (items) {
                final days = walksPerDay(items);
                if (days.every((d) => d.count == 0)) {
                  return SizedBox(
                    height: 140,
                    child: Center(
                      child: Text(
                        'No walks in the last 7 days.',
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ),
                  );
                }
                return WalksChart(days: days);
              },
            ),
          ),
        ),
      ],
    );
  }
}

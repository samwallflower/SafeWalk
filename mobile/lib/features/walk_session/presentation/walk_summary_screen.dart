import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/format/format.dart';
import '../../../core/theme/colors.dart';
import 'state/walk_controller.dart';

/// Shown when a walk ends, by you or automatically on arrival.
class WalkSummaryScreen extends ConsumerWidget {
  const WalkSummaryScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final walk = ref.watch(walkProvider);
    final finished = walk.finished;
    final theme = Theme.of(context);
    final arrived = finished?.autoCompleted ?? false;
    final time = formatTimeBetween(finished?.startTime, finished?.endTime);
    final distance = walk.route?.route.actualDistanceMeters;

    Widget row(String label, String value) => Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        children: [
          Text(
            label,
            style: theme.textTheme.bodyLarge?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
          const Spacer(),
          Text(
            value,
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w800,
            ),
          ),
        ],
      ),
    );

    return Scaffold(
      appBar: AppBar(title: const Text('Walk')),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 440),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const Icon(
                    Icons.check_circle,
                    size: 56,
                    color: AppColors.success,
                  ),
                  const SizedBox(height: 16),
                  Text(
                    arrived ? "You've arrived" : 'Walk ended',
                    textAlign: TextAlign.center,
                    style: theme.textTheme.headlineMedium?.copyWith(
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    arrived
                        ? 'SafeWalk ended your walk when you reached your destination.'
                        : 'Your location is no longer being shared.',
                    textAlign: TextAlign.center,
                    style: theme.textTheme.bodyLarge?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                  const SizedBox(height: 24),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 8,
                      ),
                      child: Column(
                        children: [
                          if (time.isNotEmpty) row('Time', time),
                          if (distance != null)
                            row('Planned route', formatDistance(distance)),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 24),
                  FilledButton(
                    onPressed: () =>
                        ref.read(walkProvider.notifier).dismissSummary(),
                    child: const Text('Done'),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

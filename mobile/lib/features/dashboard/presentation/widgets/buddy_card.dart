import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/routing/app_routes.dart';
import '../../../walk_session/presentation/state/walk_controller.dart';

/// Invites you to walk with SafeWalk as a buddy: it watches the walk and raises the alarm if something looks wrong.
class BuddyCard extends ConsumerWidget {
  const BuddyCard({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final walking = ref.watch(
      walkProvider.select((s) => s.phase == WalkPhase.active),
    );
    final scheme = theme.colorScheme;
    return Card(
      color: scheme.primary,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.shield_outlined, color: scheme.onPrimary),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    walking
                        ? 'SafeWalk is walking with you'
                        : 'Walk with SafeWalk',
                    style: theme.textTheme.titleMedium?.copyWith(
                      color: scheme.onPrimary,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              'Start a walk and SafeWalk acts as your buddy. It follows your '
              'route, notices if you stop for too long or wander off it, and '
              'alerts your emergency contacts if something looks wrong.',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: scheme.onPrimary.withValues(alpha: 0.92),
              ),
            ),
            const SizedBox(height: 12),
            FilledButton(
              onPressed: () => context.go(AppRoutes.walk),
              style: FilledButton.styleFrom(
                backgroundColor: scheme.onPrimary,
                foregroundColor: scheme.primary,
              ),
              child: Text(walking ? 'Back to my walk' : 'Start a walk'),
            ),
          ],
        ),
      ),
    );
  }
}

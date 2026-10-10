import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/routing/app_routes.dart';
import '../../../../core/widgets/empty_state.dart';
import '../../../../core/widgets/error_state.dart';
import '../../../../core/widgets/section_header.dart';
import '../../../walk_history/presentation/walk_tile.dart';
import '../../data/walk_stats_providers.dart';

class RecentWalksSection extends ConsumerWidget {
  const RecentWalksSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sessions = ref.watch(mySessionsProvider);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        SectionHeader(
          'Recent walks',
          trailing: TextButton(
            onPressed: () => context.push(AppRoutes.walkHistory),
            child: const Text('View all'),
          ),
        ),
        const SizedBox(height: 4),
        Card(
          child: sessions.when(
            loading: () => const Padding(
              padding: EdgeInsets.all(24),
              child: Center(child: CircularProgressIndicator()),
            ),
            error: (error, _) => ErrorState(
              message: error is ApiException
                  ? error.message
                  : "Couldn't load your walks.",
              onRetry: () => ref.invalidate(mySessionsProvider),
            ),
            data: (items) => items.isEmpty
                ? const EmptyState(
                    title: 'No walks yet',
                    message: 'Your walks will show up here.',
                    icon: Icons.directions_walk,
                  )
                : Column(
                    children: [
                      for (final (index, session) in items.take(3).indexed) ...[
                        if (index > 0) const Divider(height: 1),
                        WalkTile(session: session),
                      ],
                    ],
                  ),
          ),
        ),
      ],
    );
  }
}

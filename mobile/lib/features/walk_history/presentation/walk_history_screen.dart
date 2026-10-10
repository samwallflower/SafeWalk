import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/widgets/empty_state.dart';
import '../../../core/widgets/error_state.dart';
import '../../../core/widgets/loading.dart';
import '../../dashboard/data/walk_stats_providers.dart';
import 'walk_tile.dart';

class WalkHistoryScreen extends ConsumerWidget {
  const WalkHistoryScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sessions = ref.watch(mySessionsProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Walk history')),
      body: sessions.when(
        loading: () => const LoadingView(label: 'Loading your walks'),
        error: (error, _) => ErrorState(
          message: error is ApiException
              ? error.message
              : "Couldn't load your walks.",
          onRetry: () => ref.invalidate(mySessionsProvider),
        ),
        data: (items) => items.isEmpty
            ? const EmptyState(
                title: 'No walks yet',
                message: 'Plan a route in the Walk tab and your walks will appear here.',
                icon: Icons.directions_walk,
              )
            : RefreshIndicator(
                onRefresh: () => ref.refresh(mySessionsProvider.future),
                child: ListView.separated(
                  itemCount: items.length,
                  separatorBuilder: (context, index) =>
                      const Divider(height: 1),
                  itemBuilder: (context, index) =>
                      WalkTile(session: items[index]),
                ),
              ),
      ),
    );
  }
}

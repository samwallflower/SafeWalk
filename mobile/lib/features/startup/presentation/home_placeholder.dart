import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/widgets/error_state.dart';
import '../../../core/widgets/loading.dart';
import '../../auth/presentation/state/session_controller.dart';
import '../data/backend_status_provider.dart';

/// Stand-in for the map screen (Phase M2).
class HomePlaceholder extends ConsumerWidget {
  const HomePlaceholder({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final status = ref.watch(categoryCountProvider);
    final user = ref.watch(sessionProvider).value;
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: const Text('SafeWalk'),
        actions: [
          IconButton(
            tooltip: 'Sign out',
            icon: const Icon(Icons.logout),
            onPressed: () => ref.read(sessionProvider.notifier).logout(),
          ),
        ],
      ),
      body: status.when(
        loading: () => const LoadingView(label: 'Checking the SafeWalk server'),
        error: (error, _) => ErrorState(
          message: error is ApiException
              ? error.message
              : 'Something went wrong.',
          onRetry: () => ref.invalidate(categoryCountProvider),
        ),
        data: (count) => Center(
          child: Padding(
            padding: const EdgeInsets.all(32),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(
                  Icons.verified_outlined,
                  size: 48,
                  color: theme.colorScheme.primary,
                ),
                const SizedBox(height: 16),
                Text('Signed in', style: theme.textTheme.titleLarge),
                const SizedBox(height: 4),
                Text(user?.email ?? '', style: theme.textTheme.bodyMedium),
                Text(
                  user?.isAdmin ?? false ? 'Admin account' : 'Walker account',
                  style: theme.textTheme.bodySmall,
                ),
                const SizedBox(height: 16),
                Text(
                  '$count incident categories loaded',
                  style: theme.textTheme.bodyMedium,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

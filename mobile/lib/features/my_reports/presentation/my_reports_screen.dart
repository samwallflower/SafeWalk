import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/widgets/empty_state.dart';
import '../../../core/widgets/error_state.dart';
import '../../../core/widgets/loading.dart';
import '../../auth/presentation/state/session_controller.dart';
import '../../incidents/domain/incident.dart';
import '../data/my_reports_api.dart';
import 'report_tile.dart';

class MyReportsScreen extends ConsumerWidget {
  const MyReportsScreen({super.key});

  Future<void> _delete(
    BuildContext context,
    WidgetRef ref,
    Incident report,
  ) async {
    final yes = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete this report?'),
        content: const Text(
          'It will be removed from the map for everyone. This cannot be undone.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Keep'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('Delete'),
          ),
        ],
      ),
    );
    final userId = ref.read(sessionProvider).value?.id;
    if (yes != true || userId == null) return;
    try {
      await ref.read(myReportsApiProvider).remove(userId, report.id);
      ref.invalidate(myReportsProvider);
      ref.invalidate(myReportCountProvider);
    } on ApiException catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context)
          ..hideCurrentSnackBar()
          ..showSnackBar(SnackBar(content: Text(e.message)));
      }
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final reports = ref.watch(myReportsProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('My reports')),
      body: reports.when(
        loading: () => const LoadingView(label: 'Loading your reports'),
        error: (error, _) => ErrorState(
          message: error is ApiException
              ? error.message
              : "Couldn't load your reports.",
          onRetry: () => ref.invalidate(myReportsProvider),
        ),
        data: (items) => items.isEmpty
            ? const EmptyState(
                title: 'No reports yet',
                message:
                    'Reports you publish from the Report tab will appear here.',
                icon: Icons.warning_amber_rounded,
              )
            : RefreshIndicator(
                onRefresh: () => ref.refresh(myReportsProvider.future),
                child: ListView.separated(
                  itemCount: items.length,
                  separatorBuilder: (context, index) =>
                      const Divider(height: 1),
                  itemBuilder: (context, index) => ReportTile(
                    report: items[index],
                    onDelete: () => _delete(context, ref, items[index]),
                  ),
                ),
              ),
      ),
    );
  }
}

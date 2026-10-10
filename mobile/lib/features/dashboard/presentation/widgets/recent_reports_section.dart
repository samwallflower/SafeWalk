import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/routing/app_routes.dart';
import '../../../../core/widgets/empty_state.dart';
import '../../../../core/widgets/error_state.dart';
import '../../../../core/widgets/section_header.dart';
import '../../../my_reports/data/my_reports_api.dart';
import '../../../my_reports/presentation/report_tile.dart';

class RecentReportsSection extends ConsumerWidget {
  const RecentReportsSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final reports = ref.watch(myReportsProvider);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        SectionHeader(
          'My reports',
          trailing: TextButton(
            onPressed: () => context.push(AppRoutes.myReports),
            child: const Text('View all'),
          ),
        ),
        const SizedBox(height: 4),
        Card(
          child: reports.when(
            loading: () => const Padding(
              padding: EdgeInsets.all(24),
              child: Center(child: CircularProgressIndicator()),
            ),
            error: (error, _) => ErrorState(
              message: error is ApiException
                  ? error.message
                  : "Couldn't load your reports.",
              onRetry: () => ref.invalidate(myReportsProvider),
            ),
            data: (items) => items.isEmpty
                ? const EmptyState(
                    title: 'No reports yet',
                    message: 'Reports you publish will show up here.',
                    icon: Icons.warning_amber_rounded,
                  )
                : Column(
                    children: [
                      for (final (index, report) in items.take(3).indexed) ...[
                        if (index > 0) const Divider(height: 1),
                        ReportTile(report: report),
                      ],
                    ],
                  ),
          ),
        ),
      ],
    );
  }
}

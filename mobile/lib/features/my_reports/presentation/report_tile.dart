import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/format/format.dart';
import '../../../core/widgets/status_chip.dart';
import '../../incidents/domain/incident.dart';
import '../../places/data/geocode_api.dart';

(String, ChipTone) reportStatusLabel(ReportStatus status) => switch (status) {
  ReportStatus.active => ('Active', ChipTone.success),
  ReportStatus.underReview => ('Under review', ChipTone.warning),
  ReportStatus.hidden => ('Hidden', ChipTone.neutral),
};

/// One of the person's own reports.
class ReportTile extends ConsumerWidget {
  const ReportTile({super.key, required this.report, this.onDelete});

  final Incident report;
  final VoidCallback? onDelete;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final street = ref
        .watch(
          streetNameProvider((
            lat: double.parse(report.latitude.toStringAsFixed(5)),
            lng: double.parse(report.longitude.toStringAsFixed(5)),
          )),
        )
        .value;
    final (statusText, tone) = reportStatusLabel(report.status);

    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 14, 4, 14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              StatusChip(report.category.name, tone: ChipTone.danger),
              const SizedBox(width: 8),
              StatusChip(statusText, tone: tone),
              const Spacer(),
              if (onDelete != null)
                IconButton(
                  tooltip: 'Delete report',
                  icon: const Icon(Icons.delete_outline),
                  onPressed: onDelete,
                ),
            ],
          ),
          const SizedBox(height: 6),
          Padding(
            padding: const EdgeInsets.only(right: 12),
            child: Text(
              report.description,
              maxLines: 3,
              overflow: TextOverflow.ellipsis,
              style: theme.textTheme.bodyMedium,
            ),
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              const Icon(Icons.thumb_up_outlined, size: 16),
              const SizedBox(width: 4),
              Text(
                '${report.upvotes}',
                style: const TextStyle(fontWeight: FontWeight.w700),
              ),
              const SizedBox(width: 14),
              const Icon(Icons.thumb_down_outlined, size: 16),
              const SizedBox(width: 4),
              Text(
                '${report.downvotes}',
                style: const TextStyle(fontWeight: FontWeight.w700),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Text(
                  [
                    ?street,
                    formatRelative(report.timestamp),
                  ].where((e) => e.isNotEmpty).join(' • '),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

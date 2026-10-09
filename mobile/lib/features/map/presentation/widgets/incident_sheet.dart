import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/format/format.dart';
import '../../../../core/theme/colors.dart';
import '../../../incidents/domain/incident.dart';
import '../../../places/data/geocode_api.dart';
import '../../../votes/presentation/vote_buttons.dart';

/// The detail card for a tapped incident, with upvote and downvote.
class IncidentSheet extends ConsumerWidget {
  const IncidentSheet({
    super.key,
    required this.incident,
    required this.onClose,
  });

  final Incident incident;
  final VoidCallback onClose;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final street = ref.watch(
      streetNameProvider((
        lat: double.parse(incident.latitude.toStringAsFixed(5)),
        lng: double.parse(incident.longitude.toStringAsFixed(5)),
      )),
    );
    final where =
        street.value ??
        '${incident.latitude.toStringAsFixed(4)}, ${incident.longitude.toStringAsFixed(4)}';
    final reporter = incident.isAnonymous
        ? 'Reported anonymously'
        : 'Reported by ${incident.reporterName ?? 'a community member'}';

    return Card(
      elevation: 6,
      shadowColor: Colors.black38,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 16, 12, 20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 10,
                    vertical: 4,
                  ),
                  decoration: BoxDecoration(
                    color: AppColors.destructiveSoft,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(
                        Icons.warning_amber_rounded,
                        size: 16,
                        color: AppColors.destructive,
                      ),
                      const SizedBox(width: 4),
                      Text(
                        incident.category.name.toUpperCase(),
                        style: theme.textTheme.labelSmall?.copyWith(
                          color: AppColors.destructive,
                          fontWeight: FontWeight.w700,
                          letterSpacing: 0.5,
                        ),
                      ),
                    ],
                  ),
                ),
                const Spacer(),
                Text(
                  formatRelative(incident.timestamp),
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
                IconButton(
                  tooltip: 'Close',
                  icon: const Icon(Icons.close),
                  onPressed: onClose,
                ),
              ],
            ),
            const SizedBox(height: 4),
            Padding(
              padding: const EdgeInsets.only(right: 8),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Icon(
                        Icons.place_outlined,
                        size: 18,
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                      const SizedBox(width: 4),
                      Expanded(
                        child: Text(
                          where,
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w700,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Text(
                    incident.description,
                    style: theme.textTheme.bodyMedium,
                    maxLines: 6,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 14),
                  VoteButtons(
                    reportId: incident.id,
                    upvotes: incident.upvotes,
                    downvotes: incident.downvotes,
                  ),
                  const SizedBox(height: 10),
                  Text(
                    reporter,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

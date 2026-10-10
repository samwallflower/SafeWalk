import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/format/format.dart';
import '../../../core/widgets/status_chip.dart';
import '../../places/data/geocode_api.dart';
import '../../walk_session/domain/walk_session.dart';

(String, ChipTone) walkStatusLabel(WalkSession session) =>
    switch (session.status) {
      SessionStatus.completed => ('Completed', ChipTone.success),
      SessionStatus.active => ('In progress', ChipTone.info),
      SessionStatus.emergency => ('Emergency', ChipTone.danger),
      SessionStatus.abandoned => ('Not finished', ChipTone.neutral),
    };

String? _street(AsyncValue<String?> value) => value.value;

/// One walk: where from and to, when, how long, and how it ended.
class WalkTile extends ConsumerWidget {
  const WalkTile({super.key, required this.session});

  final WalkSession session;

  AsyncValue<String?> _name(WidgetRef ref, double? lat, double? lng) {
    if (lat == null || lng == null) return const AsyncData(null);
    return ref.watch(
      streetNameProvider((
        lat: double.parse(lat.toStringAsFixed(5)),
        lng: double.parse(lng.toStringAsFixed(5)),
      )),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final from =
        _street(_name(ref, session.originLatitude, session.originLongitude)) ??
        'Start';
    final to =
        _street(
          _name(ref, session.destinationLatitude, session.destinationLongitude),
        ) ??
        'Destination';
    final (statusText, tone) = walkStatusLabel(session);
    final duration = formatTimeBetween(session.startTime, session.endTime);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  formatDateTime(session.startTime),
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ),
              StatusChip(statusText, tone: tone),
            ],
          ),
          const SizedBox(height: 6),
          Row(
            children: [
              const Icon(Icons.directions_walk, size: 20),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  '$from  →  $to',
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: theme.textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
            ],
          ),
          if (duration.isNotEmpty) ...[
            const SizedBox(height: 4),
            Text(
              duration,
              style: theme.textTheme.bodySmall?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ],
        ],
      ),
    );
  }
}

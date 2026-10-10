import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/format/format.dart';
import '../../../contacts/domain/contact_rules.dart';
import '../../../contacts/presentation/state/contacts_controller.dart';
import '../../../my_reports/data/my_reports_api.dart';
import '../../data/walk_stats_providers.dart';
import '../../domain/dashboard_stats.dart';
import 'stat_tile.dart';

class StatsGrid extends ConsumerWidget {
  const StatsGrid({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sessions = ref.watch(mySessionsProvider);
    final distance = ref.watch(distanceWalkedProvider);
    final reports = ref.watch(myReportCountProvider);
    final contacts = ref.watch(contactsProvider);

    final tiles = [
      StatTile(
        label: 'Safe walks',
        value: sessions.whenData((s) => '${completedWalks(s)}'),
      ),
      StatTile(
        label: 'Distance walked',
        value: distance.whenData((d) => formatDistance(d.meters)),
        detail: distance.value?.capped == true
            ? 'Most recent ${distance.value!.walks} walks'
            : null,
      ),
      StatTile(label: 'Reports', value: reports.whenData((n) => '$n')),
      StatTile(
        label: 'Contacts',
        value: contacts.whenData((c) => '${c.length} of $maxEmergencyContacts'),
      ),
    ];

    // two per row, and both tiles in a row are as tall as the taller one
    return Column(
      children: [
        for (var i = 0; i < tiles.length; i += 2) ...[
          if (i > 0) const SizedBox(height: 12),
          IntrinsicHeight(
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Expanded(child: tiles[i]),
                const SizedBox(width: 12),
                Expanded(
                  child: i + 1 < tiles.length ? tiles[i + 1] : const SizedBox(),
                ),
              ],
            ),
          ),
        ],
      ],
    );
  }
}

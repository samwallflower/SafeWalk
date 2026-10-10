import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/routing/app_routes.dart';
import '../../contacts/presentation/contacts_section.dart';
import '../../contacts/presentation/state/contacts_controller.dart';
import '../../my_reports/data/my_reports_api.dart';
import '../../profile/data/profile_api.dart';
import '../data/walk_stats_providers.dart';
import 'widgets/buddy_card.dart';
import 'widgets/profile_card.dart';
import 'widgets/recent_reports_section.dart';
import 'widgets/recent_walks_section.dart';
import 'widgets/stats_grid.dart';
import 'widgets/walks_chart_card.dart';

/// The My Safety tab: who you are, your numbers, your contacts, and your history.
class SafetyHubScreen extends ConsumerWidget {
  const SafetyHubScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('My Safety'),
        actions: [
          IconButton(
            tooltip: 'Settings',
            icon: const Icon(Icons.settings_outlined),
            onPressed: () => context.push(AppRoutes.settings),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          ref
            ..invalidate(profileProvider)
            ..invalidate(mySessionsProvider)
            ..invalidate(distanceWalkedProvider)
            ..invalidate(myReportsProvider)
            ..invalidate(myReportCountProvider)
            ..invalidate(contactsProvider);
          await ref
              .read(mySessionsProvider.future)
              .then((_) {}, onError: (Object _) {});
        },
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
          children: const [
            ProfileCard(),
            SizedBox(height: 16),
            StatsGrid(),
            SizedBox(height: 16),
            BuddyCard(),
            SizedBox(height: 24),
            WalksChartCard(),
            SizedBox(height: 24),
            ContactsSection(),
            SizedBox(height: 24),
            RecentWalksSection(),
            SizedBox(height: 24),
            RecentReportsSection(),
          ],
        ),
      ),
    );
  }
}

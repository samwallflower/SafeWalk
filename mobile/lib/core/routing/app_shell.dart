import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/walk_session/presentation/state/walk_controller.dart';

class _Tab {
  const _Tab(this.label, this.icon, this.selectedIcon);

  final String label;
  final IconData icon;
  final IconData selectedIcon;
}

const _tabs = [
  _Tab('Map', Icons.explore_outlined, Icons.explore),
  _Tab('Walk', Icons.directions_walk_outlined, Icons.directions_walk),
  _Tab('Report', Icons.warning_amber_outlined, Icons.warning_amber_rounded),
  _Tab('My Safety', Icons.shield_outlined, Icons.shield),
];

/// The bottom navigation shared by the signed-in screens. Each tab keeps its state when you switch away.
class AppShell extends ConsumerWidget {
  const AppShell({super.key, required this.shell});

  final StatefulNavigationShell shell;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // An unfinished walk found at launch needs a decision, so bring the Walk tab forward.
    ref.listen(walkProvider.select((s) => s.phase), (previous, next) {
      if (next == WalkPhase.pending && shell.currentIndex != 1) {
        shell.goBranch(1);
      }
    });
    return Scaffold(
      body: shell,
      bottomNavigationBar: NavigationBar(
        selectedIndex: shell.currentIndex,
        onDestinationSelected: (i) =>
            shell.goBranch(i, initialLocation: i == shell.currentIndex),
        destinations: [
          for (final tab in _tabs)
            NavigationDestination(
              icon: Icon(tab.icon),
              selectedIcon: Icon(tab.selectedIcon),
              label: tab.label,
            ),
        ],
      ),
    );
  }
}

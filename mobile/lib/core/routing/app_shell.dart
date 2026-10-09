import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import 'app_routes.dart';

class _Tab {
  const _Tab(this.path, this.label, this.icon, this.selectedIcon);

  final String path;
  final String label;
  final IconData icon;
  final IconData selectedIcon;
}

const _tabs = [
  _Tab(AppRoutes.home, 'Map', Icons.explore_outlined, Icons.explore),
  _Tab(
    AppRoutes.walk,
    'Walk',
    Icons.directions_walk_outlined,
    Icons.directions_walk,
  ),
  _Tab(
    AppRoutes.report,
    'Report',
    Icons.warning_amber_outlined,
    Icons.warning_amber_rounded,
  ),
  _Tab(AppRoutes.safety, 'My Safety', Icons.shield_outlined, Icons.shield),
];

/// The bottom navigation shared by the signed-in screens.
class AppShell extends StatelessWidget {
  const AppShell({super.key, required this.location, required this.child});

  final String location;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    final index = _tabs
        .indexWhere((t) => t.path == location)
        .clamp(0, _tabs.length - 1);
    return Scaffold(
      body: child,
      bottomNavigationBar: NavigationBar(
        selectedIndex: index,
        onDestinationSelected: (i) => context.go(_tabs[i].path),
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

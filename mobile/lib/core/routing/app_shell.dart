import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/safety/presentation/state/safety_controller.dart';
import '../theme/colors.dart';
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
    // "Are you safe?" must be seen, whatever tab you are on.
    ref.listen(safetyProvider.select((s) => s.idlePrompt != null), (
      previous,
      next,
    ) {
      if (next && shell.currentIndex != 1) shell.goBranch(1);
    });
    return Scaffold(
      body: shell,
      bottomNavigationBar: _TabBar(
        selectedIndex: shell.currentIndex,
        onSelected: (i) =>
            shell.goBranch(i, initialLocation: i == shell.currentIndex),
      ),
    );
  }
}

/// The bottom tabs. The chosen tab sits on a square leaf-green tile rather than a pill.
class _TabBar extends StatelessWidget {
  const _TabBar({required this.selectedIndex, required this.onSelected});

  final int selectedIndex;
  final ValueChanged<int> onSelected;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return DecoratedBox(
      decoration: const BoxDecoration(
        color: AppColors.surface,
        border: Border(top: BorderSide(color: AppColors.border)),
      ),
      child: SafeArea(
        top: false,
        child: SizedBox(
          height: 84,
          child: Row(
            children: [
              for (var i = 0; i < _tabs.length; i++)
                Expanded(
                  child: Semantics(
                    button: true,
                    selected: i == selectedIndex,
                    label: _tabs[i].label,
                    excludeSemantics: true,
                    child: InkResponse(
                      onTap: () => onSelected(i),
                      radius: 40,
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Container(
                            width: 48,
                            height: 48,
                            decoration: BoxDecoration(
                              color: i == selectedIndex
                                  ? AppColors.leaf
                                  : Colors.transparent,
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: Icon(
                              i == selectedIndex
                                  ? _tabs[i].selectedIcon
                                  : _tabs[i].icon,
                              color: i == selectedIndex
                                  ? AppColors.primary
                                  : AppColors.mutedForeground,
                            ),
                          ),
                          const SizedBox(height: 2),
                          Text(
                            _tabs[i].label,
                            style: theme.textTheme.labelMedium?.copyWith(
                              fontWeight: i == selectedIndex
                                  ? FontWeight.w800
                                  : FontWeight.w600,
                              color: i == selectedIndex
                                  ? AppColors.primary
                                  : AppColors.mutedForeground,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

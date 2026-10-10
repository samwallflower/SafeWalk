import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/widgets/loading.dart';
import '../../routing/presentation/plan_screen.dart';
import 'active_walk_screen.dart';
import 'pending_walk_screen.dart';
import 'state/walk_controller.dart';
import 'walk_summary_screen.dart';

/// The Walk tab shows whatever fits the walk's state: planning, a decision about an unfinished walk,
/// the walk itself, or its summary.
class WalkTab extends ConsumerWidget {
  const WalkTab({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final phase = ref.watch(walkProvider.select((s) => s.phase));
    return switch (phase) {
      WalkPhase.checking => const Scaffold(
        body: LoadingView(label: 'Checking for a walk in progress'),
      ),
      WalkPhase.pending => const PendingWalkScreen(),
      WalkPhase.active => const ActiveWalkScreen(),
      WalkPhase.summary => const WalkSummaryScreen(),
      WalkPhase.none => const PlanScreen(),
    };
  }
}

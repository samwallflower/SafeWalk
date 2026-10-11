import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/routing/app_routes.dart';
import '../../../../core/theme/colors.dart';
import '../../../walk_session/presentation/state/walk_controller.dart';

/// Invites you to walk with SafeWalk as a buddy: it watches the walk and raises the alarm if something looks wrong.
class BuddyCard extends ConsumerWidget {
  const BuddyCard({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final walking = ref.watch(
      walkProvider.select((s) => s.phase == WalkPhase.active),
    );
    return Card(
      color: AppColors.primary,
      margin: EdgeInsets.zero,
      clipBehavior: Clip.antiAlias,
      child: Stack(
        children: [
          const Positioned(
            right: 0,
            top: 0,
            child: ExcludeSemantics(
              child: SizedBox(
                width: 160,
                height: 100,
                child: CustomPaint(painter: _TrailPainter()),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 210),
                  child: Text(
                    walking
                        ? 'SafeWalk is walking with you'
                        : 'Walk with SafeWalk',
                    style: theme.textTheme.titleLarge?.copyWith(
                      color: Colors.white,
                      fontWeight: FontWeight.w800,
                      height: 1.15,
                    ),
                  ),
                ),
                const SizedBox(height: 12),
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 300),
                  child: Text(
                    walking
                        ? 'It is following your route and will tell your contacts if something looks wrong.'
                        : 'Your buddy for the walk home. It follows your route and tells your contacts if something looks wrong.',
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: const Color(0xFFD6EFD3),
                      height: 1.45,
                    ),
                  ),
                ),
                const SizedBox(height: 12),
                FilledButton(
                  onPressed: () => context.go(AppRoutes.walk),
                  style: FilledButton.styleFrom(
                    backgroundColor: AppColors.lantern,
                    foregroundColor: AppColors.foreground,
                    minimumSize: const Size(0, 48),
                    padding: const EdgeInsets.symmetric(horizontal: 22),
                  ),
                  child: Text(walking ? 'Back to my walk' : 'Start a walk'),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

/// A dotted trail that ends in a glowing lantern, the SafeWalk signature.
class _TrailPainter extends CustomPainter {
  const _TrailPainter();

  @override
  void paint(Canvas canvas, Size size) {
    final trail = Path()
      ..moveTo(10, 90)
      ..cubicTo(50, 95, 60, 40, 100, 55)
      ..cubicTo(120, 62, 140, 20, 152, 18);
    final dot = Paint()..color = AppColors.lantern;
    for (final metric in trail.computeMetrics()) {
      for (var d = 0.0; d < metric.length - 6; d += 11) {
        canvas.drawCircle(metric.getTangentForOffset(d)!.position, 2, dot);
      }
    }
    const end = Offset(152, 18);
    canvas.drawCircle(
      end,
      19,
      Paint()..color = AppColors.lantern.withValues(alpha: 0.25),
    );
    canvas.drawCircle(end, 10, dot);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

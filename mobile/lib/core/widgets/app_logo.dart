import 'package:flutter/material.dart';

import '../theme/colors.dart';

/// The SafeWalk mark: a forest-green pin holding a lantern.
class LanternPin extends StatelessWidget {
  const LanternPin({super.key, this.size = 36});

  final double size;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: size,
    height: size,
    child: const CustomPaint(painter: _PinPainter()),
  );
}

class _PinPainter extends CustomPainter {
  const _PinPainter();

  @override
  void paint(Canvas canvas, Size size) {
    final k = size.width / 48;
    Offset p(double x, double y) => Offset(x * k, y * k);
    final pin = Path()
      ..moveTo(24 * k, 3 * k)
      ..cubicTo(14.6 * k, 3 * k, 7 * k, 10.4 * k, 7 * k, 19.6 * k)
      ..cubicTo(7 * k, 31.8 * k, 24 * k, 45 * k, 24 * k, 45 * k)
      ..cubicTo(24 * k, 45 * k, 41 * k, 31.8 * k, 41 * k, 19.6 * k)
      ..cubicTo(41 * k, 10.4 * k, 33.4 * k, 3 * k, 24 * k, 3 * k)
      ..close();
    canvas.drawPath(pin, Paint()..color = AppColors.primary);
    canvas.drawCircle(p(24, 19.5), 9 * k, Paint()..color = AppColors.lantern);
    canvas.drawCircle(
      p(24, 19.5),
      4 * k,
      Paint()..color = AppColors.warningSoft,
    );
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class AppLogo extends StatelessWidget {
  const AppLogo({super.key});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        const LanternPin(size: 36),
        const SizedBox(width: 10),
        Flexible(
          child: Text(
            'SafeWalk',
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: Theme.of(context).textTheme.titleLarge
                ?.copyWith(fontWeight: FontWeight.w800),
          ),
        ),
      ],
    );
  }
}

import 'package:flutter/material.dart';

import '../../../../core/theme/colors.dart';

/// "You're off your route", with a way to dismiss it. It also clears by itself once you are back on the route.
class OffRouteBanner extends StatelessWidget {
  const OffRouteBanner({super.key, required this.onDismiss});

  final VoidCallback onDismiss;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      liveRegion: true,
      child: Container(
        padding: const EdgeInsets.fromLTRB(12, 6, 4, 6),
        decoration: BoxDecoration(
          color: AppColors.warningSoft,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(
          children: [
            const Icon(Icons.alt_route, size: 20, color: AppColors.warning),
            const SizedBox(width: 8),
            const Expanded(
              child: Text(
                "You're off your route",
                style: TextStyle(
                  color: AppColors.warning,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
            TextButton(onPressed: onDismiss, child: const Text("I'm OK")),
          ],
        ),
      ),
    );
  }
}

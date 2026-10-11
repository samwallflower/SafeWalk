import 'package:flutter/material.dart';

import '../theme/colors.dart';

enum ChipTone { neutral, success, danger, info, warning }

/// A small coloured label, e.g. "Completed" or "Under review".
class StatusChip extends StatelessWidget {
  const StatusChip(this.text, {super.key, this.tone = ChipTone.neutral});

  final String text;
  final ChipTone tone;

  @override
  Widget build(BuildContext context) {
    final (background, foreground) = switch (tone) {
      ChipTone.success => (AppColors.successSoft, AppColors.success),
      ChipTone.danger => (AppColors.destructiveSoft, const Color(0xFF8E2A19)),
      ChipTone.info => (AppColors.infoSoft, AppColors.primary),
      ChipTone.warning => (AppColors.warningSoft, AppColors.warning),
      ChipTone.neutral => (AppColors.muted, AppColors.mutedForeground),
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: background,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        text,
        style: TextStyle(
          color: foreground,
          fontWeight: FontWeight.w700,
          fontSize: 12,
        ),
      ),
    );
  }
}

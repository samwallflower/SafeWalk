import 'package:flutter/material.dart';

import '../theme/colors.dart';

enum NoticeTone { error, success, info, warning }

/// An inline banner: red for errors, green for success, blue for information, amber for warnings.
class InlineNotice extends StatelessWidget {
  const InlineNotice(this.message, {super.key, this.tone = NoticeTone.error});

  final String message;
  final NoticeTone tone;

  @override
  Widget build(BuildContext context) {
    final (background, foreground, icon) = switch (tone) {
      NoticeTone.error => (
        AppColors.destructiveSoft,
        AppColors.destructive,
        Icons.error_outline,
      ),
      NoticeTone.success => (
        AppColors.successSoft,
        AppColors.success,
        Icons.check_circle_outline,
      ),
      NoticeTone.info => (
        AppColors.infoSoft,
        AppColors.primary,
        Icons.info_outline,
      ),
      NoticeTone.warning => (
        AppColors.warningSoft,
        AppColors.warning,
        Icons.warning_amber_rounded,
      ),
    };
    return Semantics(
      liveRegion: true,
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: background,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Icon(icon, size: 20, color: foreground),
            const SizedBox(width: 8),
            Expanded(
              child: Text(message, style: TextStyle(color: foreground)),
            ),
          ],
        ),
      ),
    );
  }
}

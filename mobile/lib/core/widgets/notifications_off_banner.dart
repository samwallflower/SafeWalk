import 'package:flutter/material.dart';

import '../theme/colors.dart';

/// Alerts can only reach you in the background if notifications are on, so say so plainly.
class NotificationsOffBanner extends StatelessWidget {
  const NotificationsOffBanner({super.key, required this.onOpenSettings});

  final VoidCallback onOpenSettings;

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
            const Icon(
              Icons.notifications_off_outlined,
              size: 20,
              color: AppColors.warning,
            ),
            const SizedBox(width: 8),
            const Expanded(
              child: Text(
                "Notifications are off, so you won't see alerts while the app is in the background.",
                style: TextStyle(
                  color: AppColors.warning,
                  fontWeight: FontWeight.w600,
                  fontSize: 13,
                ),
              ),
            ),
            TextButton(
              onPressed: onOpenSettings,
              child: const Text('Settings'),
            ),
          ],
        ),
      ),
    );
  }
}

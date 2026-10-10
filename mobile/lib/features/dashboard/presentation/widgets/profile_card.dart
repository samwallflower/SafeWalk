import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../auth/presentation/state/session_controller.dart';
import '../../../profile/data/profile_api.dart';
import '../../../profile/presentation/profile_edit_sheet.dart';

class ProfileCard extends ConsumerWidget {
  const ProfileCard({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final profile = ref.watch(profileProvider);
    final email =
        ref.watch(sessionProvider.select((s) => s.value?.email)) ?? '';
    final theme = Theme.of(context);

    final initials =
        profile.value?.initials ??
        (email.isEmpty ? '?' : email[0].toUpperCase());
    final name = profile.value?.fullName ?? email;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            CircleAvatar(
              radius: 30,
              backgroundColor: theme.colorScheme.primary,
              foregroundColor: theme.colorScheme.onPrimary,
              child: Text(
                initials,
                style: const TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w800,
                ),
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    name,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: theme.textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  Text(
                    email,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                  if (profile.hasError)
                    Text(
                      "Couldn't load your name.",
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.error,
                      ),
                    ),
                ],
              ),
            ),
            IconButton.filledTonal(
              tooltip: 'Edit profile',
              icon: const Icon(Icons.edit_outlined),
              onPressed: profile.value == null
                  ? null
                  : () => showProfileEditSheet(context, profile.value!),
            ),
          ],
        ),
      ),
    );
  }
}

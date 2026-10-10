import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/config/app_info.dart';
import '../../../core/location/location_provider.dart';
import '../../../core/notifications/alert_notifier.dart';
import '../../../core/widgets/section_header.dart';
import '../../auth/presentation/state/session_controller.dart';
import '../../profile/data/profile_api.dart';
import '../../profile/presentation/profile_edit_sheet.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  Future<void> _signOut(BuildContext context, WidgetRef ref) async {
    final yes = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Sign out?'),
        content: const Text(
          'You will need to sign in again to plan a walk or report an incident.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Stay signed in'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('Sign out'),
          ),
        ],
      ),
    );
    if (yes == true) await ref.read(sessionProvider.notifier).logout();
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final profile = ref.watch(profileProvider);
    final email =
        ref.watch(sessionProvider.select((s) => s.value?.email)) ?? '';
    final notifications = ref.watch(notificationsEnabledProvider);
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
        children: [
          const SectionHeader('Account'),
          const SizedBox(height: 8),
          Card(
            child: Column(
              children: [
                ListTile(
                  leading: const Icon(Icons.person_outline),
                  title: Text(profile.value?.fullName ?? email),
                  subtitle: Text(
                    profile.value?.phoneNumber?.isNotEmpty == true
                        ? '$email\n${profile.value!.phoneNumber}'
                        : email,
                  ),
                  isThreeLine: profile.value?.phoneNumber?.isNotEmpty == true,
                  trailing: TextButton(
                    onPressed: profile.value == null
                        ? null
                        : () => showProfileEditSheet(context, profile.value!),
                    child: const Text('Edit'),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),
          const SectionHeader('Alerts'),
          const SizedBox(height: 8),
          Card(
            child: Column(
              children: [
                ListTile(
                  leading: Icon(
                    notifications.value == false
                        ? Icons.notifications_off_outlined
                        : Icons.notifications_active_outlined,
                    color: notifications.value == false
                        ? theme.colorScheme.error
                        : null,
                  ),
                  title: const Text('Notifications'),
                  subtitle: Text(
                    notifications.value == false
                        ? "Off. You won't see safety alerts while the app is in the background."
                        : 'On. Safety alerts reach you when the app is in the background.',
                  ),
                  trailing: TextButton(
                    onPressed: () =>
                        ref.read(locationServiceProvider).openSettings(),
                    child: const Text('Open settings'),
                  ),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.battery_saver_outlined),
                  title: const Text('Battery'),
                  subtitle: const Text(
                    'For alerts to arrive with the screen off, set SafeWalk to "Unrestricted" in the app\'s battery settings.',
                  ),
                  trailing: TextButton(
                    onPressed: () =>
                        ref.read(locationServiceProvider).openSettings(),
                    child: const Text('Open settings'),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),
          const SectionHeader('About'),
          const SizedBox(height: 8),
          const Card(
            child: ListTile(
              leading: Icon(Icons.info_outline),
              title: Text('SafeWalk'),
              subtitle: Text('Version $appVersion'),
            ),
          ),
          const SizedBox(height: 24),
          OutlinedButton.icon(
            style: OutlinedButton.styleFrom(
              foregroundColor: theme.colorScheme.error,
            ),
            onPressed: () => _signOut(context, ref),
            icon: const Icon(Icons.logout),
            label: const Text('Sign out'),
          ),
        ],
      ),
    );
  }
}

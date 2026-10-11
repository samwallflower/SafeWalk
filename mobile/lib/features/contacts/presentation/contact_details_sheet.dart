import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/theme/colors.dart';
import '../../emergency/data/dialer.dart';
import '../domain/emergency_contact.dart';

/// Everything about one emergency contact, with the things you can do: call, edit, remove.
Future<void> showContactDetailsSheet(
  BuildContext context, {
  required EmergencyContact contact,
  required VoidCallback onEdit,
  required VoidCallback onDelete,
}) => showModalBottomSheet<void>(
  context: context,
  isScrollControlled: true,
  useSafeArea: true,
  showDragHandle: true,
  builder: (sheetContext) => ContactDetailsSheet(
    contact: contact,
    onEdit: () {
      Navigator.of(sheetContext).pop();
      onEdit();
    },
    onDelete: () {
      Navigator.of(sheetContext).pop();
      onDelete();
    },
  ),
);

class ContactDetailsSheet extends ConsumerWidget {
  const ContactDetailsSheet({
    super.key,
    required this.contact,
    required this.onEdit,
    required this.onDelete,
  });

  final EmergencyContact contact;
  final VoidCallback onEdit;
  final VoidCallback onDelete;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final phone = contact.phone;
    // scrolls, so the buttons stay reachable with a long email or at a large text size
    return SafeArea(
      child: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(20, 4, 20, 20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                Container(
                  width: 56,
                  height: 56,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                    color: AppColors.leaf,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    contact.initials,
                    style: theme.textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        contact.name,
                        style: theme.textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                      Text(
                        'Emergency contact',
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: AppColors.mutedForeground,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),
            Card(
              child: Column(
                children: [
                  _DetailRow(
                    icon: Icons.phone_outlined,
                    label: 'Phone',
                    value: phone ?? 'No phone number saved',
                    muted: phone == null,
                  ),
                  const Divider(height: 1),
                  _DetailRow(
                    icon: Icons.mail_outline,
                    label: 'Email',
                    value: contact.email,
                  ),
                ],
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'They get an email with your location if an emergency alert is raised.',
              style: theme.textTheme.bodySmall?.copyWith(
                color: AppColors.mutedForeground,
              ),
            ),
            const SizedBox(height: 20),
            if (phone != null) ...[
              FilledButton.icon(
                icon: const Icon(Icons.call),
                onPressed: () => ref.read(dialerProvider).dial(phone),
                label: Text('Call ${contact.name}'),
              ),
              const SizedBox(height: 12),
            ],
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    icon: const Icon(Icons.edit_outlined),
                    onPressed: onEdit,
                    label: const Text('Edit'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: OutlinedButton.icon(
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppColors.destructive,
                      side: const BorderSide(
                        color: AppColors.destructive,
                        width: 1.5,
                      ),
                    ),
                    icon: const Icon(Icons.delete_outline),
                    onPressed: onDelete,
                    label: const Text('Remove'),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _DetailRow extends StatelessWidget {
  const _DetailRow({
    required this.icon,
    required this.label,
    required this.value,
    this.muted = false,
  });

  final IconData icon;
  final String label;
  final String value;
  final bool muted;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Icon(icon, color: AppColors.primary),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: theme.textTheme.labelMedium?.copyWith(
                    color: AppColors.mutedForeground,
                  ),
                ),
                Text(
                  value,
                  style: theme.textTheme.bodyLarge?.copyWith(
                    fontWeight: FontWeight.w600,
                    color: muted ? AppColors.mutedForeground : null,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

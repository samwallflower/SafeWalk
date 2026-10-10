import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/widgets/empty_state.dart';
import '../../../core/widgets/error_state.dart';
import '../../../core/widgets/section_header.dart';
import '../../emergency/data/dialer.dart';
import '../domain/contact_rules.dart';
import '../domain/emergency_contact.dart';
import 'contact_form_sheet.dart';
import 'state/contacts_controller.dart';

/// The emergency contacts: who gets emailed if an emergency alert is raised.
class ContactsSection extends ConsumerWidget {
  const ContactsSection({super.key});

  Future<void> _delete(
    BuildContext context,
    WidgetRef ref,
    EmergencyContact contact,
  ) async {
    final yes = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Remove ${contact.name}?'),
        content: const Text(
          'They will no longer be told if you have an emergency.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Keep'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('Remove'),
          ),
        ],
      ),
    );
    if (yes != true) return;
    try {
      await ref.read(contactsProvider.notifier).remove(contact.id);
    } on ApiException catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context)
          ..hideCurrentSnackBar()
          ..showSnackBar(
            SnackBar(
              content: Text(
                e.failure == ApiFailure.network
                    ? e.message
                    : "Couldn't remove ${contact.name} right now. Please try again.",
              ),
            ),
          );
      }
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final contacts = ref.watch(contactsProvider);
    final theme = Theme.of(context);
    final count = contacts.value?.length ?? 0;
    final full = count >= maxEmergencyContacts;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        SectionHeader(
          'Emergency contacts',
          trailing: contacts.hasValue
              ? Text(
                  '$count of $maxEmergencyContacts',
                  style: theme.textTheme.labelMedium?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                )
              : null,
        ),
        const SizedBox(height: 8),
        Card(
          child: contacts.when(
            loading: () => const Padding(
              padding: EdgeInsets.all(24),
              child: Center(child: CircularProgressIndicator()),
            ),
            error: (error, _) => ErrorState(
              message: error is ApiException
                  ? error.message
                  : "Couldn't load your contacts.",
              onRetry: () => ref.invalidate(contactsProvider),
            ),
            data: (items) => Column(
              children: [
                if (items.isEmpty)
                  const EmptyState(
                    title: 'No emergency contacts yet',
                    message: 'Add someone who should be told if you ever need help on a walk.',
                    icon: Icons.group_outlined,
                  ),
                for (final contact in items) ...[
                  _ContactTile(
                    contact: contact,
                    onEdit: () =>
                        showContactFormSheet(context, contact: contact),
                    onDelete: () => _delete(context, ref, contact),
                  ),
                  const Divider(height: 1),
                ],
                TextButton.icon(
                  onPressed: full ? null : () => showContactFormSheet(context),
                  icon: const Icon(Icons.person_add_alt_1),
                  label: Text(
                    full ? 'Contact limit reached' : 'Add emergency contact',
                  ),
                  style: TextButton.styleFrom(
                    minimumSize: const Size.fromHeight(52),
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class _ContactTile extends ConsumerWidget {
  const _ContactTile({
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
    return ListTile(
      leading: CircleAvatar(
        backgroundColor: theme.colorScheme.primaryContainer,
        foregroundColor: theme.colorScheme.primary,
        child: Text(
          contact.initials,
          style: const TextStyle(fontWeight: FontWeight.w800),
        ),
      ),
      title: Text(
        contact.name,
        style: const TextStyle(fontWeight: FontWeight.w700),
      ),
      subtitle: Text(
        phone ?? contact.email,
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
      ),
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (phone != null)
            IconButton(
              tooltip: 'Call ${contact.name}',
              icon: const Icon(Icons.call),
              color: theme.colorScheme.primary,
              onPressed: () => ref.read(dialerProvider).dial(phone),
            ),
          PopupMenuButton<String>(
            tooltip: 'More',
            onSelected: (value) => value == 'edit' ? onEdit() : onDelete(),
            itemBuilder: (context) => const [
              PopupMenuItem(value: 'edit', child: Text('Edit')),
              PopupMenuItem(value: 'delete', child: Text('Remove')),
            ],
          ),
        ],
      ),
    );
  }
}

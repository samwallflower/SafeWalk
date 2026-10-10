import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/widgets/inline_notice.dart';
import '../domain/contact_rules.dart';
import '../domain/emergency_contact.dart';
import 'state/contacts_controller.dart';

Future<void> showContactFormSheet(
  BuildContext context, {
  EmergencyContact? contact,
}) => showModalBottomSheet<void>(
  context: context,
  isScrollControlled: true,
  showDragHandle: true,
  builder: (context) => ContactFormSheet(contact: contact),
);

/// Add a contact, or edit one when [contact] is given.
class ContactFormSheet extends ConsumerStatefulWidget {
  const ContactFormSheet({super.key, this.contact});

  final EmergencyContact? contact;

  @override
  ConsumerState<ContactFormSheet> createState() => _ContactFormSheetState();
}

class _ContactFormSheetState extends ConsumerState<ContactFormSheet> {
  final _formKey = GlobalKey<FormState>();
  late final _name = TextEditingController(text: widget.contact?.name ?? '');
  late final _email = TextEditingController(text: widget.contact?.email ?? '');
  late final _phone = TextEditingController(text: widget.contact?.phone ?? '');
  bool _saving = false;
  String? _error;

  bool get _editing => widget.contact != null;

  @override
  void dispose() {
    _name.dispose();
    _email.dispose();
    _phone.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (_saving || !(_formKey.currentState?.validate() ?? false)) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    final input = ContactInput(
      name: _name.text,
      email: _email.text,
      phone: _phone.text,
    );
    try {
      final controller = ref.read(contactsProvider.notifier);
      if (_editing) {
        await controller.edit(widget.contact!.id, input);
      } else {
        await controller.add(input);
      }
      if (mounted) Navigator.of(context).pop();
    } on ApiException catch (e) {
      if (mounted) setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Padding(
      padding: EdgeInsets.fromLTRB(
        20,
        4,
        20,
        20 + MediaQuery.viewInsetsOf(context).bottom,
      ),
      child: SingleChildScrollView(
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                _editing ? 'Edit contact' : 'Add an emergency contact',
                style: theme.textTheme.titleLarge,
              ),
              const SizedBox(height: 4),
              Text(
                'They are emailed with your location if an emergency alert is raised on a walk.',
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: 16),
              if (_error != null) ...[
                InlineNotice(_error!),
                const SizedBox(height: 12),
              ],
              TextFormField(
                controller: _name,
                validator: ContactRules.name,
                textCapitalization: TextCapitalization.words,
                textInputAction: TextInputAction.next,
                decoration: const InputDecoration(labelText: 'Name'),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _email,
                validator: ContactRules.email,
                keyboardType: TextInputType.emailAddress,
                textInputAction: TextInputAction.next,
                decoration: const InputDecoration(labelText: 'Email'),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _phone,
                validator: ContactRules.phone,
                keyboardType: TextInputType.phone,
                textInputAction: TextInputAction.done,
                onFieldSubmitted: (_) => _save(),
                decoration: const InputDecoration(
                  labelText: 'Phone (optional)',
                  hintText: '+36301234567',
                ),
              ),
              const SizedBox(height: 20),
              FilledButton(
                onPressed: _saving ? null : _save,
                child: _saving
                    ? const SizedBox(
                        width: 22,
                        height: 22,
                        child: CircularProgressIndicator(
                          strokeWidth: 2.5,
                          color: Colors.white,
                        ),
                      )
                    : Text(_editing ? 'Save changes' : 'Add contact'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/widgets/inline_notice.dart';
import '../../auth/presentation/state/session_controller.dart';
import '../data/profile_api.dart';
import '../domain/profile_rules.dart';
import '../domain/user_profile.dart';

Future<void> showProfileEditSheet(BuildContext context, UserProfile profile) =>
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (context) => ProfileEditSheet(profile: profile),
    );

class ProfileEditSheet extends ConsumerStatefulWidget {
  const ProfileEditSheet({super.key, required this.profile});

  final UserProfile profile;

  @override
  ConsumerState<ProfileEditSheet> createState() => _ProfileEditSheetState();
}

class _ProfileEditSheetState extends ConsumerState<ProfileEditSheet> {
  final _formKey = GlobalKey<FormState>();
  late final _first = TextEditingController(text: widget.profile.firstName);
  late final _last = TextEditingController(text: widget.profile.lastName);
  late final _phone = TextEditingController(
    text: widget.profile.phoneNumber ?? '',
  );
  bool _saving = false;
  String? _error;

  @override
  void dispose() {
    _first.dispose();
    _last.dispose();
    _phone.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (_saving || !(_formKey.currentState?.validate() ?? false)) return;
    final userId = ref.read(sessionProvider).value?.id;
    if (userId == null) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      final phone = _phone.text.trim();
      await ref
          .read(profileApiProvider)
          .update(
            userId,
            firstName: _first.text.trim(),
            lastName: _last.text.trim(),
            phoneNumber: phone.isEmpty ? null : phone,
          );
      ref.invalidate(profileProvider);
      if (mounted) Navigator.of(context).pop();
    } on ApiException catch (e) {
      if (mounted) setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
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
                'Edit profile',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 16),
              if (_error != null) ...[
                InlineNotice(_error!),
                const SizedBox(height: 12),
              ],
              TextFormField(
                controller: _first,
                validator: (v) => ProfileRules.name(v, 'First name'),
                textCapitalization: TextCapitalization.words,
                textInputAction: TextInputAction.next,
                decoration: const InputDecoration(labelText: 'First name'),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _last,
                validator: (v) => ProfileRules.name(v, 'Last name'),
                textCapitalization: TextCapitalization.words,
                textInputAction: TextInputAction.next,
                decoration: const InputDecoration(labelText: 'Last name'),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _phone,
                validator: ProfileRules.phone,
                keyboardType: TextInputType.phone,
                textInputAction: TextInputAction.done,
                onFieldSubmitted: (_) => _save(),
                decoration: const InputDecoration(
                  labelText: 'Phone number (optional)',
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
                    : const Text('Save'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/routing/app_routes.dart';
import '../../../core/widgets/form_error.dart';
import '../data/auth_api.dart';
import '../domain/validators.dart';
import 'state/session_controller.dart';
import 'widgets/auth_scaffold.dart';

class VerifyScreen extends ConsumerStatefulWidget {
  const VerifyScreen({super.key, this.email = ''});

  final String email;

  @override
  ConsumerState<VerifyScreen> createState() => _VerifyScreenState();
}

class _VerifyScreenState extends ConsumerState<VerifyScreen> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _email = TextEditingController(
    text: widget.email,
  );
  final _code = TextEditingController();
  bool _submitting = false;
  bool _resending = false;
  String? _error;
  String? _info;

  @override
  void dispose() {
    _email.dispose();
    _code.dispose();
    super.dispose();
  }

  Future<void> _verify() async {
    if (_submitting || !(_formKey.currentState?.validate() ?? false)) return;
    setState(() {
      _submitting = true;
      _error = null;
      _info = null;
    });
    try {
      await ref
          .read(authApiProvider)
          .verify(_email.text.trim(), _code.text.trim());
      ref
          .read(sessionNoticeProvider.notifier)
          .show('Email verified. You can sign in now.');
      if (mounted) context.go(AppRoutes.login);
    } on ApiException catch (e) {
      if (mounted) setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  Future<void> _resend() async {
    final emailError = AuthValidators.email(_email.text);
    if (_resending || emailError != null) {
      setState(() => _error = emailError);
      return;
    }
    setState(() {
      _resending = true;
      _error = null;
      _info = null;
    });
    try {
      await ref.read(authApiProvider).resendVerification(_email.text.trim());
      if (mounted) setState(() => _info = 'A new code is on its way.');
    } on ApiException catch (e) {
      if (mounted) setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _resending = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return AuthScaffold(
      title: 'Verify your email',
      subtitle: 'Enter the code we sent to your inbox to finish creating your account.',
      children: [
        Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (_error != null) ...[
                FormError(_error!),
                const SizedBox(height: 16),
              ],
              if (_info != null) ...[
                Text(
                  _info!,
                  style: TextStyle(
                    color: Theme.of(context).colorScheme.primary,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: 16),
              ],
              TextFormField(
                controller: _email,
                validator: AuthValidators.email,
                keyboardType: TextInputType.emailAddress,
                textInputAction: TextInputAction.next,
                decoration: const InputDecoration(labelText: 'Email address'),
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _code,
                validator: AuthValidators.verificationCode,
                textInputAction: TextInputAction.done,
                onFieldSubmitted: (_) => _verify(),
                decoration: const InputDecoration(
                  labelText: 'Verification code',
                ),
              ),
              const SizedBox(height: 24),
              FilledButton(
                onPressed: _submitting ? null : _verify,
                child: _submitting
                    ? const SizedBox(
                        width: 22,
                        height: 22,
                        child: CircularProgressIndicator(strokeWidth: 2.5),
                      )
                    : const Text('Verify email'),
              ),
              const SizedBox(height: 8),
              TextButton(
                onPressed: _resending ? null : _resend,
                child: Text(_resending ? 'Sending...' : 'Send a new code'),
              ),
              TextButton(
                onPressed: () => context.go(AppRoutes.login),
                child: const Text('Back to sign in'),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

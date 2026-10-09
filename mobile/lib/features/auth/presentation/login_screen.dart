import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/routing/app_routes.dart';
import '../../../core/widgets/inline_notice.dart';
import '../domain/validators.dart';
import 'state/session_controller.dart';
import 'widgets/auth_scaffold.dart';
import 'widgets/demo_login_buttons.dart';
import 'widgets/password_field.dart';

class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key, this.demoAccounts});

  /// Overridable for tests; defaults to the accounts compiled into the build.
  final List<DemoAccount>? demoAccounts;

  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _email = TextEditingController();
  final _password = TextEditingController();
  bool _submitting = false;
  String? _error;

  @override
  void dispose() {
    _email.dispose();
    _password.dispose();
    super.dispose();
  }

  void _fill(DemoAccount account) {
    _email.text = account.email;
    _password.text = account.password;
    setState(() => _error = null);
  }

  Future<void> _submit() async {
    if (_submitting || !(_formKey.currentState?.validate() ?? false)) return;
    setState(() {
      _submitting = true;
      _error = null;
    });
    try {
      await ref
          .read(sessionProvider.notifier)
          .login(_email.text, _password.text);
      // The router sees the new session and moves on.
    } on ApiException catch (e) {
      if (mounted) setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final notice = ref.watch(sessionNoticeProvider);
    final banner = _error != null ? SessionMessage(_error!) : notice;
    return AuthScaffold(
      title: 'Welcome back',
      subtitle: 'Sign in to report incidents and plan safer walks.',
      children: [
        Form(
          key: _formKey,
          child: AutofillGroup(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                if (banner != null) ...[
                  InlineNotice(banner.text, tone: banner.tone),
                  const SizedBox(height: 16),
                ],
                TextFormField(
                  controller: _email,
                  validator: AuthValidators.email,
                  keyboardType: TextInputType.emailAddress,
                  autofillHints: const [AutofillHints.email],
                  textInputAction: TextInputAction.next,
                  decoration: const InputDecoration(labelText: 'Email address'),
                ),
                const SizedBox(height: 16),
                PasswordField(
                  controller: _password,
                  validator: AuthValidators.loginPassword,
                  onSubmitted: _submit,
                ),
                const SizedBox(height: 24),
                FilledButton(
                  onPressed: _submitting ? null : _submit,
                  child: _submitting
                      ? const SizedBox(
                          width: 22,
                          height: 22,
                          child: CircularProgressIndicator(strokeWidth: 2.5),
                        )
                      : const Text('Sign in'),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 24),
        DemoLoginButtons(
          accounts: widget.demoAccounts ?? configuredDemoAccounts(),
          onFill: _fill,
        ),
        const SizedBox(height: 24),
        Wrap(
          alignment: WrapAlignment.center,
          crossAxisAlignment: WrapCrossAlignment.center,
          children: [
            const Text('New to SafeWalk?'),
            TextButton(
              onPressed: () => context.push(AppRoutes.register),
              child: const Text('Create an account'),
            ),
          ],
        ),
      ],
    );
  }
}

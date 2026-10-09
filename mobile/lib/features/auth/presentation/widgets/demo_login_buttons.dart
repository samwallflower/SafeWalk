import 'package:flutter/material.dart';

import '../../../../core/config/env.dart';

class DemoAccount {
  const DemoAccount({
    required this.label,
    required this.email,
    required this.password,
  });

  final String label;
  final String email;
  final String password;
}

/// The demo accounts from `--dart-define`. Empty unless demo login is enabled for this build.
List<DemoAccount> configuredDemoAccounts() {
  if (!Env.demoLoginEnabled) return const [];
  return [
    if (Env.demoUserEmail.isNotEmpty)
      const DemoAccount(
        label: 'Use demo user',
        email: Env.demoUserEmail,
        password: Env.demoUserPassword,
      ),
    if (Env.demoAdminEmail.isNotEmpty)
      const DemoAccount(
        label: 'Use demo admin',
        email: Env.demoAdminEmail,
        password: Env.demoAdminPassword,
      ),
  ];
}

/// Fills the form only; it never submits.
class DemoLoginButtons extends StatelessWidget {
  const DemoLoginButtons({
    super.key,
    required this.accounts,
    required this.onFill,
  });

  final List<DemoAccount> accounts;
  final void Function(DemoAccount account) onFill;

  @override
  Widget build(BuildContext context) {
    if (accounts.isEmpty) return const SizedBox.shrink();
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Row(
          children: [
            const Expanded(child: Divider()),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 12),
              child: Text(
                'DEMO ACCOUNTS FOR EVALUATION',
                style: theme.textTheme.labelSmall?.copyWith(
                  letterSpacing: 1,
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
            ),
            const Expanded(child: Divider()),
          ],
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            for (final (index, account) in accounts.indexed) ...[
              if (index > 0) const SizedBox(width: 12),
              Expanded(
                child: OutlinedButton(
                  onPressed: () => onFill(account),
                  child: Text(account.label, textAlign: TextAlign.center),
                ),
              ),
            ],
          ],
        ),
      ],
    );
  }
}

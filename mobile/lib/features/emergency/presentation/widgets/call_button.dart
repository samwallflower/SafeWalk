import 'package:flutter/material.dart';

/// One call button: shows who it is for and the number. Pressing it opens the dialer; it never places the call.
class CallButton extends StatelessWidget {
  const CallButton({
    super.key,
    required this.label,
    required this.number,
    required this.onPressed,
    this.emphasis = false,
  });

  final String label;
  final String number;
  final VoidCallback onPressed;
  final bool emphasis;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Semantics(
      button: true,
      label: 'Call $label, $number',
      excludeSemantics: true,
      child: FilledButton(
        style: FilledButton.styleFrom(
          minimumSize: const Size.fromHeight(64),
          backgroundColor: emphasis
              ? theme.colorScheme.error
              : theme.colorScheme.primary,
        ),
        onPressed: onPressed,
        child: Row(
          children: [
            const Icon(Icons.call, size: 26),
            const SizedBox(width: 14),
            Expanded(
              child: Text(
                label,
                style: const TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
            Text(
              number,
              style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w800),
            ),
          ],
        ),
      ),
    );
  }
}

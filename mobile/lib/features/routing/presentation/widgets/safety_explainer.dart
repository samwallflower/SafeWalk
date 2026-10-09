import 'package:flutter/material.dart';

/// A short, general explanation. It deliberately has no formula.
class SafetyExplainer extends StatelessWidget {
  const SafetyExplainer({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      child: Theme(
        data: theme.copyWith(dividerColor: Colors.transparent),
        child: ExpansionTile(
          title: const Text(
            'How routes are ranked',
            style: TextStyle(fontWeight: FontWeight.w700),
          ),
          childrenPadding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
          expandedCrossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Routes are ranked by how safe they are overall, based on the severity of the incidents reported near them. '
              'The recommended route may be slightly longer, but it passes fewer risky spots.',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

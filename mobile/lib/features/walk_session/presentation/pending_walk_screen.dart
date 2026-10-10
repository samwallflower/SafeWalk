import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/format/format.dart';
import '../../../core/widgets/inline_notice.dart';
import '../domain/walk_session.dart';
import 'state/walk_controller.dart';

/// An earlier walk was never finished (the app was closed or killed). Resume it or end it.
class PendingWalkScreen extends ConsumerStatefulWidget {
  const PendingWalkScreen({super.key});

  @override
  ConsumerState<PendingWalkScreen> createState() => _PendingWalkScreenState();
}

class _PendingWalkScreenState extends ConsumerState<PendingWalkScreen> {
  bool _busy = false;

  Future<void> _run(Future<void> Function() action) async {
    if (_busy) return;
    setState(() => _busy = true);
    try {
      await action();
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final walk = ref.watch(walkProvider);
    final controller = ref.read(walkProvider.notifier);
    final session = walk.session;
    final theme = Theme.of(context);
    final isEmergency = session?.status == SessionStatus.emergency;
    final canResume =
        session?.status == SessionStatus.active ||
        session?.status == SessionStatus.emergency;

    return Scaffold(
      appBar: AppBar(title: const Text('Walk')),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 440),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Icon(
                    Icons.directions_walk,
                    size: 48,
                    color: theme.colorScheme.primary,
                  ),
                  const SizedBox(height: 16),
                  Text(
                    isEmergency
                        ? 'Your walk is in an emergency'
                        : canResume
                        ? 'You have a walk in progress'
                        : 'An earlier walk was not finished',
                    textAlign: TextAlign.center,
                    style: theme.textTheme.headlineSmall?.copyWith(
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    isEmergency
                        ? 'Resume to see the alert, call for help, or mark yourself safe. You can also end the walk.'
                        : canResume
                        ? 'It started ${formatRelative(session?.startTime ?? '')}. Resume to keep sharing your location, or end it if you are done.'
                        : 'End it to plan a new walk.',
                    textAlign: TextAlign.center,
                    style: theme.textTheme.bodyLarge?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                  const SizedBox(height: 24),
                  if (walk.message != null) ...[
                    InlineNotice(walk.message!),
                    const SizedBox(height: 16),
                  ],
                  if (canResume) ...[
                    FilledButton.icon(
                      onPressed: _busy ? null : () => _run(controller.resume),
                      icon: const Icon(Icons.play_arrow),
                      label: const Text('Resume walk'),
                    ),
                    const SizedBox(height: 12),
                  ],
                  OutlinedButton(
                    onPressed: _busy ? null : () => _run(controller.endPending),
                    child: Text(canResume ? 'End this walk' : 'End it'),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

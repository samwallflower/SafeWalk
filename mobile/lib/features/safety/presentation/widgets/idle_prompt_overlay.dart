import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../../core/format/format.dart';
import '../../../../core/theme/colors.dart';
import '../../domain/idle_prompt.dart';

/// Full-screen "Are you safe?" with a countdown and one huge button.
class IdlePromptOverlay extends StatefulWidget {
  const IdlePromptOverlay({
    super.key,
    required this.prompt,
    required this.onImOk,
    this.error,
  });

  final IdlePrompt prompt;
  final Future<void> Function() onImOk;
  final String? error;

  @override
  State<IdlePromptOverlay> createState() => _IdlePromptOverlayState();
}

class _IdlePromptOverlayState extends State<IdlePromptOverlay> {
  Timer? _tick;
  bool _busy = false;

  @override
  void initState() {
    super.initState();
    SystemSound.play(SystemSoundType.alert);
    _tick = Timer.periodic(const Duration(seconds: 1), (_) {
      if (!mounted) return;
      if (!widget.prompt.expiredAt(DateTime.now())) {
        HapticFeedback.heavyImpact();
      }
      setState(() {});
    });
  }

  @override
  void dispose() {
    _tick?.cancel();
    super.dispose();
  }

  Future<void> _answer() async {
    if (_busy) return;
    setState(() => _busy = true);
    try {
      await widget.onImOk();
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final left = widget.prompt.remaining(DateTime.now());
    final expired = widget.prompt.expiredAt(DateTime.now());
    return Material(
      color: AppColors.foreground,
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Spacer(),
              Text(
                'Are you safe?',
                textAlign: TextAlign.center,
                style: theme.textTheme.displaySmall?.copyWith(
                  color: Colors.white,
                  fontWeight: FontWeight.w800,
                ),
              ),
              const SizedBox(height: 12),
              Text(
                expired
                    ? "We haven't heard from you. SafeWalk is alerting your emergency contacts."
                    : 'No movement detected. Please confirm you are okay.',
                textAlign: TextAlign.center,
                style: theme.textTheme.titleMedium?.copyWith(
                  color: Colors.white70,
                ),
              ),
              const SizedBox(height: 28),
              Text(
                expired ? '00:00' : formatClock(left),
                textAlign: TextAlign.center,
                style: theme.textTheme.displayMedium?.copyWith(
                  color: expired ? AppColors.destructiveSoft : Colors.white,
                  fontWeight: FontWeight.w800,
                  fontFeatures: const [FontFeature.tabularFigures()],
                ),
              ),
              const Spacer(),
              if (widget.error != null) ...[
                Text(
                  widget.error!,
                  textAlign: TextAlign.center,
                  style: const TextStyle(color: AppColors.destructiveSoft),
                ),
                const SizedBox(height: 12),
              ],
              SizedBox(
                height: 96,
                child: FilledButton(
                  style: FilledButton.styleFrom(
                    backgroundColor: AppColors.success,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(28),
                    ),
                  ),
                  onPressed: _busy ? null : _answer,
                  child: _busy
                      ? const SizedBox(
                          width: 30,
                          height: 30,
                          child: CircularProgressIndicator(
                            strokeWidth: 3,
                            color: Colors.white,
                          ),
                        )
                      : const Text(
                          "I'm OK",
                          style: TextStyle(
                            fontSize: 32,
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                ),
              ),
              const SizedBox(height: 12),
            ],
          ),
        ),
      ),
    );
  }
}

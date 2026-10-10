import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../../core/theme/colors.dart';

/// How long SOS must be held. Long enough to avoid pocket presses, short enough for a real emergency.
const sosHoldDuration = Duration(seconds: 1);

/// SOS: press and hold for one second. A bar fills while you hold, and letting go early cancels.
/// For screen readers, a double tap asks for confirmation instead (a hold is not possible there).
class SosButton extends StatefulWidget {
  const SosButton({
    super.key,
    required this.onTriggered,
    required this.onConfirmRequested,
    this.busy = false,
  });

  final VoidCallback onTriggered;

  /// The accessible alternative to holding: the caller shows a confirmation, then triggers.
  final VoidCallback onConfirmRequested;
  final bool busy;

  @override
  State<SosButton> createState() => _SosButtonState();
}

class _SosButtonState extends State<SosButton>
    with SingleTickerProviderStateMixin {
  late final AnimationController _hold =
      AnimationController(vsync: this, duration: sosHoldDuration)
        ..addStatusListener((status) {
          if (status == AnimationStatus.completed) {
            HapticFeedback.heavyImpact();
            widget.onTriggered();
            _hold.reset();
          }
        });

  @override
  void dispose() {
    _hold.dispose();
    super.dispose();
  }

  void _press() {
    if (widget.busy) return;
    HapticFeedback.mediumImpact();
    _hold.forward();
  }

  void _release() {
    if (_hold.status != AnimationStatus.completed) _hold.reverse();
  }

  @override
  Widget build(BuildContext context) {
    return Semantics(
      button: true,
      label: 'SOS. Press and hold for one second to alert your emergency contacts.',
      onTap: widget.busy ? null : widget.onConfirmRequested,
      excludeSemantics: true,
      child: Listener(
        onPointerDown: (_) => _press(),
        onPointerUp: (_) => _release(),
        onPointerCancel: (_) => _release(),
        child: AnimatedBuilder(
          animation: _hold,
          builder: (context, _) => ClipRRect(
            borderRadius: BorderRadius.circular(18),
            child: Container(
              height: 68,
              color: AppColors.destructive.withValues(
                alpha: widget.busy ? 0.6 : 1,
              ),
              child: Stack(
                children: [
                  FractionallySizedBox(
                    widthFactor: _hold.value,
                    child: Container(
                      color: Colors.black.withValues(alpha: 0.3),
                    ),
                  ),
                  Center(
                    child: widget.busy
                        ? const SizedBox(
                            width: 28,
                            height: 28,
                            child: CircularProgressIndicator(
                              strokeWidth: 3,
                              color: Colors.white,
                            ),
                          )
                        : Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              const Icon(
                                Icons.sos,
                                color: Colors.white,
                                size: 30,
                              ),
                              const SizedBox(width: 10),
                              Text(
                                _hold.value > 0
                                    ? 'Keep holding...'
                                    : 'Hold for SOS',
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontSize: 20,
                                  fontWeight: FontWeight.w800,
                                ),
                              ),
                            ],
                          ),
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

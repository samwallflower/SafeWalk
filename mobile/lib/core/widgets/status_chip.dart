import 'package:flutter/material.dart';

enum ChipTone { neutral, success, danger, info, warning }

/// A small coloured label, e.g. "Completed" or "Under review".
class StatusChip extends StatelessWidget {
  const StatusChip(this.text, {super.key, this.tone = ChipTone.neutral});

  final String text;
  final ChipTone tone;

  @override
  Widget build(BuildContext context) {
    final (background, foreground) = switch (tone) {
      ChipTone.success => (const Color(0xFFE2F3E8), const Color(0xFF0A6B32)),
      ChipTone.danger => (const Color(0xFFFBE7E8), const Color(0xFFC4161C)),
      ChipTone.info => (const Color(0xFFE3EDFB), const Color(0xFF0B52B8)),
      ChipTone.warning => (const Color(0xFFFFF1D1), const Color(0xFF8A5A00)),
      ChipTone.neutral => (const Color(0xFFEEF2FA), const Color(0xFF5B6475)),
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: background,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        text,
        style: TextStyle(
          color: foreground,
          fontWeight: FontWeight.w700,
          fontSize: 12,
        ),
      ),
    );
  }
}

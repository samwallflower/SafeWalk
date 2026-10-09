import 'package:flutter/material.dart';

class MapStatusChip extends StatelessWidget {
  const MapStatusChip({
    super.key,
    required this.text,
    this.loading = false,
    this.icon,
  });

  final String text;
  final bool loading;
  final IconData? icon;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Semantics(
      liveRegion: true,
      child: Material(
        elevation: 2,
        shadowColor: Colors.black26,
        color: theme.colorScheme.surface,
        borderRadius: BorderRadius.circular(20),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 7),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (loading)
                const SizedBox(
                  width: 14,
                  height: 14,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              else if (icon != null)
                Icon(icon, size: 16, color: theme.colorScheme.primary),
              if (loading || icon != null) const SizedBox(width: 8),
              Flexible(
                child: Text(
                  text,
                  style: theme.textTheme.labelLarge,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

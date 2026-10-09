/// "just now", "24m ago", "3h ago", "2d ago". Backend timestamps are zone-less, so they are read as written.
String formatRelative(String timestamp, {DateTime? now}) {
  final time = DateTime.tryParse(timestamp);
  if (time == null) return '';
  final diff = (now ?? DateTime.now()).difference(time);
  if (diff.inMinutes < 1) return 'just now';
  if (diff.inMinutes < 60) return '${diff.inMinutes}m ago';
  if (diff.inHours < 24) return '${diff.inHours}h ago';
  return '${diff.inDays}d ago';
}

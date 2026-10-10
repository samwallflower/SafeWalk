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

/// 850 -> "850 m", 2430 -> "2.4 km"
String formatDistance(double meters) => meters < 1000
    ? '${meters.round()} m'
    : '${(meters / 1000).toStringAsFixed(1)} km';

/// Signed difference, e.g. "+120 m" or "-0.4 km".
String formatDistanceDelta(double meters) =>
    '${meters >= 0 ? '+' : '-'}${formatDistance(meters.abs())}';

const _walkingMetersPerMinute = 5000 / 60;

/// Rough walking time at 5 km/h. An estimate only: the backend does not return durations.
int estimateWalkingMinutes(double meters) {
  final minutes = (meters / _walkingMetersPerMinute).round();
  return minutes < 1 ? 1 : minutes;
}

/// Minutes between two zone-less timestamps as "24 min" or "1 h 5 min". Empty when either is missing or invalid.
String formatTimeBetween(String? start, String? end) {
  final a = start == null ? null : DateTime.tryParse(start);
  final b = end == null ? null : DateTime.tryParse(end);
  if (a == null || b == null) return '';
  final minutes = b.difference(a).inMinutes;
  if (minutes < 0) return '';
  if (minutes < 60) return '$minutes min';
  return '${minutes ~/ 60} h ${minutes % 60} min';
}

/// A running clock like "12:05" or "1:02:09".
String formatClock(Duration elapsed) {
  final total = elapsed.inSeconds < 0 ? 0 : elapsed.inSeconds;
  final h = total ~/ 3600;
  final m = (total % 3600) ~/ 60;
  final s = total % 60;
  String two(int n) => n.toString().padLeft(2, '0');
  return h > 0 ? '$h:${two(m)}:${two(s)}' : '${two(m)}:${two(s)}';
}

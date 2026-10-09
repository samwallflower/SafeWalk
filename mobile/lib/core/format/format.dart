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

import 'package:latlong2/latlong.dart';

import '../../../core/geo/haversine.dart';

/// How often the phone reports its position. The server treats a session with no update for 3 minutes as idle,
/// so even a person standing still must send something well inside that.
abstract final class TrackingPolicy {
  /// Never send more often than this.
  static const minInterval = Duration(seconds: 10);

  /// Send at least this often, moving or not.
  static const heartbeat = Duration(seconds: 30);

  /// Moving less than this counts as standing still.
  static const minMoveMeters = 3.0;
}

/// Should [now]'s [position] be sent, given what was last sent?
bool shouldSendLocation({
  required DateTime now,
  required LatLng position,
  required DateTime? lastSentAt,
  required LatLng? lastSentPosition,
}) {
  if (lastSentAt == null || lastSentPosition == null) return true;
  final elapsed = now.difference(lastSentAt);
  if (elapsed < TrackingPolicy.minInterval) return false;
  if (elapsed >= TrackingPolicy.heartbeat) return true;
  return haversineMeters(lastSentPosition, position) >=
      TrackingPolicy.minMoveMeters;
}

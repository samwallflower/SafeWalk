import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:geolocator/geolocator.dart';
import 'package:latlong2/latlong.dart';

/// Continuous position updates. An interface so the walk logic can be tested without a GPS.
abstract class LocationTracker {
  Stream<LatLng> positions();
}

/// Uses an Android foreground service, so tracking keeps running with the screen off or the app in the background.
class GeolocatorTracker implements LocationTracker {
  const GeolocatorTracker();

  @override
  Stream<LatLng> positions() {
    final settings = AndroidSettings(
      accuracy: LocationAccuracy.high,
      distanceFilter: 0,
      intervalDuration: const Duration(seconds: 5),
      foregroundNotificationConfig: const ForegroundNotificationConfig(
        notificationTitle: 'SafeWalk walk in progress',
        notificationText: 'Sharing your location while you walk. Open SafeWalk to end the walk.',
        enableWakeLock: true,
        notificationIcon: AndroidResource(
          name: 'ic_stat_safewalk',
          defType: 'drawable',
        ),
        setOngoing: true,
      ),
    );
    return Geolocator.getPositionStream(locationSettings: settings)
        .map((position) => LatLng(position.latitude, position.longitude));
  }
}

final locationTrackerProvider = Provider<LocationTracker>(
  (ref) => const GeolocatorTracker(),
);

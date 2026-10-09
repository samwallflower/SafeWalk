import 'package:geolocator/geolocator.dart';
import 'package:latlong2/latlong.dart';

enum LocationFailure { serviceDisabled, denied, deniedForever, unavailable }

class LocationResult {
  const LocationResult.found(LatLng this.point) : failure = null;
  const LocationResult.failed(LocationFailure this.failure) : point = null;

  final LatLng? point;
  final LocationFailure? failure;
}

/// One-shot location with the permission flow. Continuous tracking arrives with walk sessions (M5).
class LocationService {
  const LocationService();

  Future<LocationResult> current() async {
    if (!await Geolocator.isLocationServiceEnabled()) {
      return const LocationResult.failed(LocationFailure.serviceDisabled);
    }
    var permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }
    if (permission == LocationPermission.denied) {
      return const LocationResult.failed(LocationFailure.denied);
    }
    if (permission == LocationPermission.deniedForever) {
      return const LocationResult.failed(LocationFailure.deniedForever);
    }
    try {
      final position = await Geolocator.getCurrentPosition(
        locationSettings: const LocationSettings(
          accuracy: LocationAccuracy.high,
          timeLimit: Duration(seconds: 15),
        ),
      );
      return LocationResult.found(
        LatLng(position.latitude, position.longitude),
      );
    } on Exception {
      return const LocationResult.failed(LocationFailure.unavailable);
    }
  }

  Future<bool> openSettings() => Geolocator.openAppSettings();
}

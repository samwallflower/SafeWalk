import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

typedef MapFocus = ({LatLng point, double zoom});

/// Another screen (e.g. after reporting) asks the map to fly somewhere. The map clears it once handled.
final mapFocusProvider = NotifierProvider<MapFocusController, MapFocus?>(
  MapFocusController.new,
);

class MapFocusController extends Notifier<MapFocus?> {
  @override
  MapFocus? build() => null;

  void request(LatLng point, {double zoom = 16}) =>
      state = (point: point, zoom: zoom);
  void clear() => state = null;
}

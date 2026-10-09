import 'package:latlong2/latlong.dart';

class GeoCircle {
  const GeoCircle(this.center, this.radiusMeters);

  final LatLng center;
  final double radiusMeters;

  @override
  bool operator ==(Object other) =>
      other is GeoCircle &&
      other.center.latitude == center.latitude &&
      other.center.longitude == center.longitude &&
      other.radiusMeters == radiusMeters;

  @override
  int get hashCode =>
      Object.hash(center.latitude, center.longitude, radiusMeters);
}

class MapBounds {
  const MapBounds({
    required this.west,
    required this.south,
    required this.east,
    required this.north,
  });

  final double west;
  final double south;
  final double east;
  final double north;
}

class MapViewport {
  const MapViewport({required this.bounds, required this.zoom});

  final MapBounds bounds;
  final double zoom;

  LatLng get center => LatLng(
    (bounds.north + bounds.south) / 2,
    (bounds.east + bounds.west) / 2,
  );
}

enum TimeRange {
  all('Any time', null),
  day('24 hours', Duration(hours: 24)),
  week('7 days', Duration(days: 7)),
  month('30 days', Duration(days: 30));

  const TimeRange(this.label, this.window);

  final String label;
  final Duration? window;
}

class MapFilters {
  const MapFilters({this.categoryId, this.range = TimeRange.all});

  final int? categoryId;
  final TimeRange range;

  bool get isActive => categoryId != null || range != TimeRange.all;

  MapFilters copyWith({int? Function()? categoryId, TimeRange? range}) =>
      MapFilters(
        categoryId: categoryId != null ? categoryId() : this.categoryId,
        range: range ?? this.range,
      );
}

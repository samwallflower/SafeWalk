import 'package:latlong2/latlong.dart';

/// Default view: Nottingham, where the seeded reports are. Users can search or use their location.
const defaultMapCenter = LatLng(52.9548, -1.1581);
const defaultMapZoom = 12.0;

/// Above this viewport radius no incident data is requested (payload size guard, about 67k reports).
const maxHeatmapRadiusM = 30000.0;

/// Individual tappable incidents (full records) are only fetched at street level.
const maxPointsRadiusM = 3000.0;

/// Category and time filters need full records, so they only apply at neighbourhood level.
const maxFilterRadiusM = 5000.0;

/// The fetched area is the viewport circle times this, so small pans do not refetch.
const areaPadding = 1.4;

/// Refetch with a tighter area once the cached one is this much bigger than needed.
const areaMaxOversize = 2.5;

const moveDebounce = Duration(milliseconds: 400);

/// Drawing limits that keep the map smooth on a phone.
const maxHeatDots = 2500;
const maxIncidentMarkers = 300;

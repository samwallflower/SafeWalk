import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

import '../domain/place.dart';
import 'geocode_api.dart';

typedef PlaceQuery = ({String text, double? lat, double? lng});

final placeSearchProvider = FutureProvider.autoDispose
    .family<List<Place>, PlaceQuery>((ref, query) async {
      final cancel = CancelToken();
      ref.onDispose(cancel.cancel);
      final near = (query.lat != null && query.lng != null)
          ? LatLng(query.lat!, query.lng!)
          : null;
      return ref
          .watch(geocodeApiProvider)
          .search(query.text, proximity: near, cancelToken: cancel);
    });

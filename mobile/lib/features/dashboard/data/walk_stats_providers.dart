import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../auth/presentation/state/session_controller.dart';
import '../../routing/data/routing_api.dart';
import '../../walk_session/data/walk_session_api.dart';
import '../../walk_session/domain/walk_session.dart';
import '../domain/walked_distance.dart';

/// All of the person's walks, newest first. The list is short, so it is read whole.
final mySessionsProvider = FutureProvider.autoDispose<List<WalkSession>>((
  ref,
) async {
  final userId = ref.watch(sessionProvider.select((s) => s.value?.id));
  if (userId == null) return const [];
  final sessions = await ref.watch(walkSessionApiProvider).mine(userId);
  return [...sessions]..sort((a, b) => b.startTime.compareTo(a.startTime));
});

/// A route never changes once it exists, so its length is kept for the whole session.
final routeLengthProvider = FutureProvider.family<double?, int>((
  ref,
  routeId,
) async {
  try {
    return (await ref.watch(routingApiProvider).byId(routeId))
        .actualDistanceMeters;
  } on Object {
    return null;
  }
});

class WalkedDistance {
  const WalkedDistance({
    required this.meters,
    required this.walks,
    required this.capped,
  });

  final double meters;
  final int walks;
  final bool capped;
}

/// Distance walked = the length of the route behind each completed walk.
final distanceWalkedProvider = FutureProvider.autoDispose<WalkedDistance>((
  ref,
) async {
  final sessions = await ref.watch(mySessionsProvider.future);
  final walked = completedRouteIds(sessions);
  final distinct = walked.routeIds.toSet();
  final lengths = <int, double>{};
  await Future.wait([
    for (final id in distinct)
      ref.watch(routeLengthProvider(id).future).then((length) {
        if (length != null) lengths[id] = length;
      }),
  ]);
  return WalkedDistance(
    meters: totalWalkedMeters(walked.routeIds, lengths),
    walks: walked.routeIds.length,
    capped: walked.capped,
  );
});

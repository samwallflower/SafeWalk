import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/vote_counts.dart';

/// Counts changed by this user's votes. They win over the numbers in the loaded incident list, so
/// reopening an incident shows the vote you just cast without refetching the whole map.
final voteCountsOverridesProvider =
    NotifierProvider<VoteCountsOverrides, Map<int, VoteCounts>>(
      VoteCountsOverrides.new,
    );

class VoteCountsOverrides extends Notifier<Map<int, VoteCounts>> {
  @override
  Map<int, VoteCounts> build() => const {};

  void set(int reportId, VoteCounts counts) =>
      state = {...state, reportId: counts};
}

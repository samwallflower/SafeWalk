import 'vote_type.dart';

class VoteCounts {
  const VoteCounts({required this.upvotes, required this.downvotes});

  final int upvotes;
  final int downvotes;
}

/// The vote after [clicked] is pressed: pressing your current vote removes it, otherwise cast or switch.
VoteType? nextVote(VoteType? current, VoteType clicked) =>
    current == clicked ? null : clicked;

/// The counts after a user's vote moves from [from] to [to] (either may be null, meaning no vote).
VoteCounts applyVoteChange(VoteCounts counts, VoteType? from, VoteType? to) {
  var up = counts.upvotes;
  var down = counts.downvotes;
  if (from == VoteType.upvote) up -= 1;
  if (from == VoteType.downvote) down -= 1;
  if (to == VoteType.upvote) up += 1;
  if (to == VoteType.downvote) down += 1;
  return VoteCounts(upvotes: up < 0 ? 0 : up, downvotes: down < 0 ? 0 : down);
}

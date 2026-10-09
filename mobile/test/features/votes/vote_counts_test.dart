import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/votes/domain/vote_counts.dart';
import 'package:safewalk_mobile/features/votes/domain/vote_type.dart';

void main() {
  const counts = VoteCounts(upvotes: 5, downvotes: 2);

  group('nextVote', () {
    test('pressing your current vote removes it', () {
      expect(nextVote(VoteType.upvote, VoteType.upvote), isNull);
    });

    test(
      'pressing the other one switches, and pressing with no vote casts',
      () {
        expect(nextVote(VoteType.upvote, VoteType.downvote), VoteType.downvote);
        expect(nextVote(null, VoteType.upvote), VoteType.upvote);
      },
    );
  });

  group('applyVoteChange', () {
    test('a new upvote adds one', () {
      final next = applyVoteChange(counts, null, VoteType.upvote);
      expect((next.upvotes, next.downvotes), (6, 2));
    });

    test('switching moves one vote across', () {
      final next = applyVoteChange(counts, VoteType.upvote, VoteType.downvote);
      expect((next.upvotes, next.downvotes), (4, 3));
    });

    test('removing takes one away', () {
      final next = applyVoteChange(counts, VoteType.downvote, null);
      expect((next.upvotes, next.downvotes), (5, 1));
    });

    test('never goes below zero', () {
      final next = applyVoteChange(
        const VoteCounts(upvotes: 0, downvotes: 0),
        VoteType.upvote,
        null,
      );
      expect(next.upvotes, 0);
    });
  });

  test('parses and writes the backend values', () {
    expect(VoteType.fromApi('UPVOTE'), VoteType.upvote);
    expect(VoteType.fromApi('nope'), isNull);
    expect(VoteType.downvote.apiValue, 'DOWNVOTE');
  });
}

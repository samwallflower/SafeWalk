enum VoteType {
  upvote('UPVOTE'),
  downvote('DOWNVOTE');

  const VoteType(this.apiValue);

  /// The value the backend expects in `?voteType=`.
  final String apiValue;

  static VoteType? fromApi(Object? value) => switch (value) {
    'UPVOTE' => VoteType.upvote,
    'DOWNVOTE' => VoteType.downvote,
    _ => null,
  };
}

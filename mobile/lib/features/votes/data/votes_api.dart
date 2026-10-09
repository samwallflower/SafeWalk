import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/network/providers.dart';
import '../domain/vote_type.dart';

class VotesApi {
  VotesApi(this._client);

  final ApiClient _client;

  String _base(int userId, int reportId) =>
      '/incident-votes/user/$userId/report/$reportId';

  /// The signed-in user's vote on a report, or null when they have not voted (the backend answers 404).
  Future<VoteType?> mine(int userId, int reportId) async {
    try {
      final vote = await _client.getObject('${_base(userId, reportId)}/vote');
      return VoteType.fromApi(vote['voteType']);
    } on ApiException catch (e) {
      if (e.isNotFound) return null;
      rethrow;
    }
  }

  Future<void> cast(int userId, int reportId, VoteType type) =>
      _client.postObject(
        '${_base(userId, reportId)}/cast',
        query: {'voteType': type.apiValue},
      );

  Future<void> update(int userId, int reportId, VoteType type) =>
      _client.putObject(
        '${_base(userId, reportId)}/update',
        query: {'voteType': type.apiValue},
      );

  Future<void> remove(int userId, int reportId) =>
      _client.delete('${_base(userId, reportId)}/remove');
}

final votesApiProvider = Provider<VotesApi>(
  (ref) => VotesApi(ref.watch(apiClientProvider)),
);

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/network/api_exception.dart';
import 'package:safewalk_mobile/core/theme/theme.dart';
import 'package:safewalk_mobile/features/auth/domain/session_user.dart';
import 'package:safewalk_mobile/features/auth/presentation/state/session_controller.dart';
import 'package:safewalk_mobile/features/votes/data/votes_api.dart';
import 'package:safewalk_mobile/features/votes/domain/vote_type.dart';
import 'package:safewalk_mobile/features/votes/presentation/vote_buttons.dart';

class _FakeSession extends SessionController {
  @override
  Future<SessionUser?> build() async =>
      const SessionUser(id: 7, email: 'a@b.c', roles: ['ROLE_USER']);
}

class _FakeVotesApi implements VotesApi {
  _FakeVotesApi({this.mineVote, this.failWith});

  final VoteType? mineVote;
  final ApiException? failWith;
  final calls = <String>[];

  @override
  Future<VoteType?> mine(int userId, int reportId) async => mineVote;

  @override
  Future<void> cast(int userId, int reportId, VoteType type) async {
    calls.add('cast ${type.apiValue}');
    if (failWith != null) throw failWith!;
  }

  @override
  Future<void> update(int userId, int reportId, VoteType type) async {
    calls.add('update ${type.apiValue}');
    if (failWith != null) throw failWith!;
  }

  @override
  Future<void> remove(int userId, int reportId) async {
    calls.add('remove');
    if (failWith != null) throw failWith!;
  }
}

Widget _app(_FakeVotesApi api) => ProviderScope(
  overrides: [
    sessionProvider.overrideWith(_FakeSession.new),
    votesApiProvider.overrideWithValue(api),
  ],
  child: MaterialApp(
    theme: buildTheme(downloadFont: false),
    home: const Scaffold(
      body: Center(child: VoteButtons(reportId: 1, upvotes: 5, downvotes: 2)),
    ),
  ),
);

Future<void> _settle(WidgetTester tester) async {
  await tester.pump();
  await tester.pump();
}

void main() {
  testWidgets('shows the counts and casts an upvote', (tester) async {
    final api = _FakeVotesApi();
    await tester.pumpWidget(_app(api));
    await _settle(tester);
    expect(find.text('Upvote'), findsOneWidget);
    expect(find.text('Downvote'), findsOneWidget);

    await tester.tap(find.text('Upvote'));
    await _settle(tester);

    expect(find.text('6'), findsOneWidget); // 5 -> 6
    expect(api.calls, ['cast UPVOTE']);
  });

  testWidgets('pressing your own upvote again removes it', (tester) async {
    final api = _FakeVotesApi(mineVote: VoteType.upvote);
    await tester.pumpWidget(_app(api));
    await _settle(tester);

    await tester.tap(find.text('Upvote'));
    await _settle(tester);

    expect(find.text('4'), findsOneWidget); // 5 -> 4
    expect(api.calls, ['remove']);
  });

  testWidgets('switching to the other vote updates both counts', (
    tester,
  ) async {
    final api = _FakeVotesApi(mineVote: VoteType.upvote);
    await tester.pumpWidget(_app(api));
    await _settle(tester);

    await tester.tap(find.text('Downvote'));
    await _settle(tester);

    expect(find.text('4'), findsOneWidget); // upvotes 5 -> 4
    expect(find.text('3'), findsOneWidget); // downvotes 2 -> 3
    expect(api.calls, ['update DOWNVOTE']);
  });

  testWidgets('a refused vote rolls the count back and says why', (
    tester,
  ) async {
    final api = _FakeVotesApi(
      failWith: const ApiException(400, 'You cannot vote on your own report'),
    );
    await tester.pumpWidget(_app(api));
    await _settle(tester);

    await tester.tap(find.text('Upvote'));
    await _settle(tester);

    expect(find.text('5'), findsOneWidget); // back to 5
    expect(find.text('You cannot vote on your own report'), findsOneWidget);
  });
}

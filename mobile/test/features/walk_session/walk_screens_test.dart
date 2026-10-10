import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/theme/theme.dart';
import 'package:safewalk_mobile/features/auth/domain/session_user.dart';
import 'package:safewalk_mobile/features/auth/presentation/state/session_controller.dart';
import 'package:safewalk_mobile/features/walk_session/domain/walk_session.dart';
import 'package:safewalk_mobile/features/walk_session/presentation/pending_walk_screen.dart';
import 'package:safewalk_mobile/features/walk_session/presentation/state/walk_controller.dart';
import 'package:safewalk_mobile/features/walk_session/presentation/walk_summary_screen.dart';

class _FakeSession extends SessionController {
  @override
  Future<SessionUser?> build() async => null;
}

class _FixedWalk extends WalkController {
  _FixedWalk(this.initial);

  final WalkState initial;
  int resumed = 0;
  int ended = 0;
  int dismissed = 0;

  @override
  WalkState build() => initial;

  @override
  Future<void> resume() async => resumed++;

  @override
  Future<void> endPending() async => ended++;

  @override
  void dismissSummary() => dismissed++;
}

WalkSession _session(SessionStatus status, {bool auto = false}) => WalkSession(
  id: 1,
  userId: 1,
  routeId: 1,
  startTime: '2026-10-10T10:00:00',
  endTime: '2026-10-10T10:24:00',
  status: status,
  originLatitude: null,
  originLongitude: null,
  destinationLatitude: null,
  destinationLongitude: null,
  autoCompleted: auto,
);

Widget _app(_FixedWalk walk, Widget screen) => ProviderScope(
  overrides: [
    sessionProvider.overrideWith(_FakeSession.new),
    walkProvider.overrideWith(() => walk),
  ],
  child: MaterialApp(theme: buildTheme(downloadFont: false), home: screen),
);

void main() {
  testWidgets('an active unfinished walk can be resumed or ended', (
    tester,
  ) async {
    final walk = _FixedWalk(
      WalkState(
        phase: WalkPhase.pending,
        session: _session(SessionStatus.active),
      ),
    );
    await tester.pumpWidget(_app(walk, const PendingWalkScreen()));

    expect(find.text('You have a walk in progress'), findsOneWidget);
    await tester.tap(find.text('Resume walk'));
    await tester.pump();
    expect(walk.resumed, 1);

    await tester.tap(find.text('End this walk'));
    await tester.pump();
    expect(walk.ended, 1);
  });

  testWidgets('a stuck emergency walk can only be ended', (tester) async {
    final walk = _FixedWalk(
      WalkState(
        phase: WalkPhase.pending,
        session: _session(SessionStatus.emergency),
      ),
    );
    await tester.pumpWidget(_app(walk, const PendingWalkScreen()));

    expect(find.text('An earlier walk was not finished'), findsOneWidget);
    expect(find.text('Resume walk'), findsNothing);
    expect(find.text('End it'), findsOneWidget);
  });

  testWidgets('arriving shows the summary with the time taken', (tester) async {
    final walk = _FixedWalk(
      WalkState(
        phase: WalkPhase.summary,
        finished: _session(SessionStatus.completed, auto: true),
      ),
    );
    await tester.pumpWidget(_app(walk, const WalkSummaryScreen()));

    expect(find.text("You've arrived"), findsOneWidget);
    expect(find.text('24 min'), findsOneWidget);

    await tester.tap(find.text('Done'));
    expect(walk.dismissed, 1);
  });

  testWidgets('ending a walk yourself says the walk ended', (tester) async {
    final walk = _FixedWalk(
      WalkState(
        phase: WalkPhase.summary,
        finished: _session(SessionStatus.completed),
      ),
    );
    await tester.pumpWidget(_app(walk, const WalkSummaryScreen()));
    expect(find.text('Walk ended'), findsOneWidget);
  });
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/network/api_exception.dart';
import 'package:safewalk_mobile/core/theme/theme.dart';
import 'package:safewalk_mobile/features/auth/domain/session_user.dart';
import 'package:safewalk_mobile/features/auth/presentation/state/session_controller.dart';
import 'package:safewalk_mobile/features/categories/domain/incident_category.dart';
import 'package:safewalk_mobile/features/contacts/domain/emergency_contact.dart';
import 'package:safewalk_mobile/features/contacts/presentation/state/contacts_controller.dart';
import 'package:safewalk_mobile/features/dashboard/data/walk_stats_providers.dart';
import 'package:safewalk_mobile/features/dashboard/domain/dashboard_stats.dart';
import 'package:safewalk_mobile/features/dashboard/presentation/safety_hub_screen.dart';
import 'package:safewalk_mobile/features/dashboard/presentation/widgets/walks_chart.dart';
import 'package:safewalk_mobile/features/incidents/domain/incident.dart';
import 'package:safewalk_mobile/features/my_reports/data/my_reports_api.dart';
import 'package:safewalk_mobile/features/my_reports/presentation/my_reports_screen.dart';
import 'package:safewalk_mobile/features/profile/data/profile_api.dart';
import 'package:safewalk_mobile/features/profile/domain/user_profile.dart';
import 'package:safewalk_mobile/features/walk_history/presentation/walk_history_screen.dart';
import 'package:safewalk_mobile/features/walk_history/presentation/walk_tile.dart';
import 'package:safewalk_mobile/features/walk_session/domain/walk_session.dart';

class _FakeSession extends SessionController {
  int signOuts = 0;

  @override
  Future<SessionUser?> build() async =>
      const SessionUser(id: 7, email: 'elena@v.com', roles: ['ROLE_USER']);

  @override
  Future<void> logout() async => signOuts++;
}

class _FakeContacts extends ContactsController {
  @override
  Future<List<EmergencyContact>> build() async => const [
    EmergencyContact(
      id: 1,
      name: 'Marcus Vance',
      email: 'm@v.com',
      phone: '+3630',
    ),
  ];
}

WalkSession _walk(int id, String start, SessionStatus status, {String? end}) =>
    WalkSession(
      id: id,
      userId: 7,
      routeId: 1,
      startTime: start,
      endTime: end,
      status: status,
      originLatitude: 47.49,
      originLongitude: 19.07,
      destinationLatitude: 47.5,
      destinationLongitude: 19.08,
      autoCompleted: false,
    );

Incident _report(int id, String description, ReportStatus status) => Incident(
  id: id,
  isAnonymous: true,
  reporterName: null,
  description: description,
  latitude: 47.49,
  longitude: 19.07,
  timestamp: '2026-10-09T10:00:00',
  upvotes: 4,
  downvotes: 1,
  category: const IncidentCategory(
    id: 1,
    name: 'Poor lighting',
    severityWeight: 2,
  ),
  status: status,
);

Future<void> _pump(
  WidgetTester tester,
  Widget screen, {
  List<dynamic> overrides = const [],
}) async {
  await tester.binding.setSurfaceSize(const Size(800, 3200));
  addTearDown(() => tester.binding.setSurfaceSize(null));
  await tester.pumpWidget(
    ProviderScope(
      overrides: [
        sessionProvider.overrideWith(_FakeSession.new),
        ...overrides.cast(),
      ],
      child: MaterialApp(theme: buildTheme(downloadFont: false), home: screen),
    ),
  );
  await tester.pumpAndSettle();
}

const _profile = UserProfile(
  id: 7,
  email: 'elena@v.com',
  firstName: 'Elena',
  lastName: 'Vance',
);

void main() {
  testWidgets(
    'the My Safety hub shows the profile, numbers, contacts, walks and reports',
    (tester) async {
      final sessions = [
        _walk(
          2,
          '2026-10-10T09:00:00',
          SessionStatus.completed,
          end: '2026-10-10T09:24:00',
        ),
        _walk(1, '2026-10-09T09:00:00', SessionStatus.emergency),
      ];
      await _pump(
        tester,
        const SafetyHubScreen(),
        overrides: [
          profileProvider.overrideWith((ref) async => _profile),
          mySessionsProvider.overrideWith((ref) async => sessions),
          distanceWalkedProvider.overrideWith(
            (ref) async =>
                const WalkedDistance(meters: 2450, walks: 1, capped: false),
          ),
          myReportCountProvider.overrideWith((ref) async => 5),
          myReportsProvider.overrideWith(
            (ref) async => [
              _report(1, 'Street lamp is out', ReportStatus.active),
            ],
          ),
          contactsProvider.overrideWith(_FakeContacts.new),
        ],
      );

      expect(find.text('Elena Vance'), findsOneWidget);
      expect(find.text('EV'), findsOneWidget);
      expect(find.text('1'), findsWidgets, reason: 'one completed walk');
      expect(find.text('2.5 km'), findsOneWidget);
      expect(find.text('5'), findsOneWidget);
      expect(find.text('Marcus Vance'), findsOneWidget);
      expect(find.text('Street lamp is out'), findsOneWidget);
      expect(find.text('Completed'), findsOneWidget);
      expect(find.text('Emergency'), findsOneWidget);
    },
  );

  testWidgets('walk history lists every walk with its outcome', (tester) async {
    await _pump(
      tester,
      const WalkHistoryScreen(),
      overrides: [
        mySessionsProvider.overrideWith(
          (ref) async => [
            _walk(
              2,
              '2026-10-10T09:00:00',
              SessionStatus.completed,
              end: '2026-10-10T09:24:00',
            ),
            _walk(1, '2026-10-09T09:00:00', SessionStatus.abandoned),
          ],
        ),
      ],
    );
    expect(find.text('Completed'), findsOneWidget);
    expect(find.text('Not finished'), findsOneWidget);
    expect(find.text('24 min'), findsOneWidget);
    expect(find.text('10 Oct 2026, 09:00'), findsOneWidget);
  });

  testWidgets('walk history says so when there are no walks', (tester) async {
    await _pump(
      tester,
      const WalkHistoryScreen(),
      overrides: [
        mySessionsProvider.overrideWith((ref) async => <WalkSession>[]),
      ],
    );
    expect(find.text('No walks yet'), findsOneWidget);
  });

  testWidgets('walk history shows the error with a retry', (tester) async {
    await _pump(
      tester,
      const WalkHistoryScreen(),
      overrides: [
        mySessionsProvider.overrideWith(
          (ref) async => throw const ApiException(0, "Can't reach SafeWalk."),
        ),
      ],
    );
    expect(find.text("Can't reach SafeWalk."), findsOneWidget);
    expect(find.text('Try again'), findsOneWidget);
  });

  test('every walk outcome has a plain label', () {
    expect(
      walkStatusLabel(_walk(1, 'x', SessionStatus.active)).$1,
      'In progress',
    );
    expect(
      walkStatusLabel(_walk(1, 'x', SessionStatus.emergency)).$1,
      'Emergency',
    );
  });

  group('MyReportsScreen', () {
    testWidgets('lists reports with status and votes', (tester) async {
      await _pump(
        tester,
        const MyReportsScreen(),
        overrides: [
          myReportsProvider.overrideWith(
            (ref) async => [
              _report(1, 'Street lamp is out', ReportStatus.active),
              _report(2, 'Loud group', ReportStatus.underReview),
            ],
          ),
        ],
      );
      expect(find.text('Street lamp is out'), findsOneWidget);
      expect(find.text('Under review'), findsOneWidget);
      expect(find.text('Active'), findsOneWidget);
      expect(find.text('4'), findsWidgets);
    });

    testWidgets('an empty list says so', (tester) async {
      await _pump(
        tester,
        const MyReportsScreen(),
        overrides: [
          myReportsProvider.overrideWith((ref) async => <Incident>[]),
        ],
      );
      expect(find.text('No reports yet'), findsOneWidget);
    });
  });

  group('WalksChart', () {
    testWidgets('draws one bar per day', (tester) async {
      final days = [
        for (var i = 0; i < 7; i++)
          DayCount(
            day: DateTime(2026, 10, 1).add(Duration(days: i)),
            count: i % 3,
          ),
      ];
      await tester.pumpWidget(
        MaterialApp(
          theme: buildTheme(downloadFont: false),
          home: Scaffold(body: WalksChart(days: days)),
        ),
      );
      expect(find.byType(WalksChart), findsOneWidget);
    });
  });
}

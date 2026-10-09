import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/theme/theme.dart';
import 'package:safewalk_mobile/features/auth/domain/session_user.dart';
import 'package:safewalk_mobile/features/auth/presentation/state/session_controller.dart';
import 'package:safewalk_mobile/features/categories/domain/incident_category.dart';
import 'package:safewalk_mobile/features/incidents/domain/incident.dart';
import 'package:safewalk_mobile/features/map/presentation/widgets/incident_pager.dart';

class _NoSession extends SessionController {
  @override
  Future<SessionUser?> build() async => null;
}

Incident _incident(int id, String description) => Incident(
  id: id,
  isAnonymous: true,
  reporterName: null,
  description: description,
  latitude: 52.95,
  longitude: -1.15,
  timestamp: '2026-10-09T10:00:00',
  upvotes: 0,
  downvotes: 0,
  category: const IncidentCategory(
    id: 1,
    name: 'Poor lighting',
    severityWeight: 1,
  ),
  status: ReportStatus.active,
);

Widget _app(List<Incident> incidents) => ProviderScope(
  overrides: [sessionProvider.overrideWith(_NoSession.new)],
  child: MaterialApp(
    theme: buildTheme(downloadFont: false),
    home: Scaffold(
      body: SingleChildScrollView(
        child: IncidentPager(incidents: incidents, onClose: () {}),
      ),
    ),
  ),
);

void main() {
  testWidgets('a single incident shows no pager', (tester) async {
    await tester.pumpWidget(_app([_incident(1, 'First')]));
    expect(find.text('First'), findsOneWidget);
    expect(find.textContaining('at this spot'), findsNothing);
  });

  testWidgets('several incidents can be stepped through', (tester) async {
    await tester.pumpWidget(
      _app([
        _incident(1, 'First'),
        _incident(2, 'Second'),
        _incident(3, 'Third'),
      ]),
    );
    expect(find.text('1 of 3 at this spot'), findsOneWidget);
    expect(find.text('First'), findsOneWidget);

    await tester.tap(find.byTooltip('Next incident'));
    await tester.pump();
    expect(find.text('2 of 3 at this spot'), findsOneWidget);
    expect(find.text('Second'), findsOneWidget);

    await tester.tap(find.byTooltip('Previous incident'));
    await tester.pump();
    expect(find.text('First'), findsOneWidget);
  });
}

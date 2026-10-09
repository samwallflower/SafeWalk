import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/theme/theme.dart';
import 'package:safewalk_mobile/features/categories/domain/incident_category.dart';
import 'package:safewalk_mobile/features/incidents/domain/incident.dart';
import 'package:safewalk_mobile/features/map/presentation/widgets/incident_sheet.dart';

Incident _incident({required bool anonymous, String? name}) => Incident(
  id: 1,
  isAnonymous: anonymous,
  reporterName: name,
  description: 'Streetlamps are dark on the north crosswalk.',
  latitude: 52.95,
  longitude: -1.15,
  timestamp: '2026-10-09T10:00:00',
  upvotes: 18,
  downvotes: 2,
  category: const IncidentCategory(
    id: 1,
    name: 'Poor lighting',
    severityWeight: 3,
  ),
  status: ReportStatus.active,
);

Widget _app(Incident incident, VoidCallback onClose) => ProviderScope(
  child: MaterialApp(
    theme: buildTheme(downloadFont: false),
    home: Scaffold(
      body: IncidentSheet(incident: incident, onClose: onClose),
    ),
  ),
);

void main() {
  testWidgets('shows category, description and vote counts', (tester) async {
    await tester.pumpWidget(_app(_incident(anonymous: true), () {}));
    expect(find.text('POOR LIGHTING'), findsOneWidget);
    expect(find.textContaining('Streetlamps are dark'), findsOneWidget);
    expect(find.text('18 Confirm'), findsOneWidget);
    expect(find.text('2 All clear'), findsOneWidget);
    expect(find.text('Reported anonymously'), findsOneWidget);
  });

  testWidgets('shows the display name for named reports', (tester) async {
    await tester.pumpWidget(
      _app(_incident(anonymous: false, name: 'Elena V.'), () {}),
    );
    expect(find.text('Reported by Elena V.'), findsOneWidget);
  });

  testWidgets('the close button calls back', (tester) async {
    var closed = false;
    await tester.pumpWidget(
      _app(_incident(anonymous: true), () => closed = true),
    );
    await tester.tap(find.byTooltip('Close'));
    expect(closed, isTrue);
  });
}

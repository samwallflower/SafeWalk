import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/incidents/domain/incident.dart';

Map<String, Object?> _dto({required bool anonymous}) => {
  'id': 9,
  'description': 'Broken light',
  'latitude': 52.95,
  'longitude': -1.15,
  'timestamp': '2026-10-09T10:00:00',
  'isAnonymous': anonymous,
  'upvotes': 3,
  'downvotes': 1,
  'status': 'ACTIVE',
  'user': {
    'firstName': 'Elena',
    'lastName': 'Vasquez',
    'email': 'secret@mail.com',
    'phoneNumber': '+441234',
  },
  'category': {
    'id': 2,
    'name': 'Poor lighting',
    'severityWeight': 3,
    'description': null,
  },
};

void main() {
  test('anonymous reports carry no name at all', () {
    final incident = Incident.fromDto(_dto(anonymous: true));
    expect(incident.isAnonymous, isTrue);
    expect(incident.reporterName, isNull);
  });

  test('named reports keep only "First L."', () {
    final incident = Incident.fromDto(_dto(anonymous: false));
    expect(incident.reporterName, 'Elena V.');
  });

  test('a missing isAnonymous flag is treated as anonymous', () {
    final dto = _dto(anonymous: false)..remove('isAnonymous');
    expect(Incident.fromDto(dto).isAnonymous, isTrue);
  });

  test('maps votes, category and status', () {
    final incident = Incident.fromDto(_dto(anonymous: true));
    expect(incident.upvotes, 3);
    expect(incident.downvotes, 1);
    expect(incident.category.name, 'Poor lighting');
    expect(incident.status, ReportStatus.active);
  });
}

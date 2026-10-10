import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/safety/data/realtime_client.dart';
import 'package:safewalk_mobile/features/safety/domain/alert_message.dart';

void main() {
  test('parses an alert pushed by the server', () {
    final alert = AlertMessage.tryParse(
      '{"sessionId":4,"emergencyId":null,"type":"IDLE_WARNING","message":"No movement detected."}',
    );
    expect(alert, isNotNull);
    expect(alert!.sessionId, 4);
    expect(alert.type, AlertType.idleWarning);
    expect(alert.message, 'No movement detected.');
  });

  test('knows every alert type from the backend enum', () {
    expect(AlertType.parse('IDLE_WARNING'), AlertType.idleWarning);
    expect(
      AlertType.parse('EMERGENCY_TRIGGERED'),
      AlertType.emergencyTriggered,
    );
    expect(AlertType.parse('ROUTE_DEVIATION'), AlertType.routeDeviation);
    expect(AlertType.parse('AUTOCOMPLETE'), AlertType.autocomplete);
    expect(AlertType.parse('EMERGENCY_RESOLVED'), AlertType.emergencyResolved);
  });

  test('an unknown type is kept as unknown, not a crash', () {
    expect(
      AlertMessage.tryParse(
        '{"sessionId":1,"type":"SOMETHING_NEW","message":""}',
      )!.type,
      AlertType.unknown,
    );
  });

  test('garbage is ignored', () {
    expect(AlertMessage.tryParse(null), isNull);
    expect(AlertMessage.tryParse(''), isNull);
    expect(AlertMessage.tryParse('not json'), isNull);
    expect(AlertMessage.tryParse('[1,2,3]'), isNull);
  });

  test('the connection is signed in with the JWT as a bearer token', () {
    expect(stompConnectHeaders('abc.def.ghi'), {
      'Authorization': 'Bearer abc.def.ghi',
    });
  });
}

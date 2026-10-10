import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/emergency/domain/authority_numbers.dart';
import 'package:safewalk_mobile/features/emergency/domain/dial_uri.dart';
import 'package:safewalk_mobile/features/emergency/domain/emergency.dart';

void main() {
  group('Emergency', () {
    test('parses the dto with the contacts that were notified', () {
      final emergency = Emergency.fromJson({
        'id': 12,
        'triggerSource': 'MANUAL_SOS',
        'triggerLatitude': 47.49,
        'triggerLongitude': 19.07,
        'triggerTimestamp': '2026-10-10T10:00:00',
        'resolved': false,
        'notifiedEmergencyContacts': [
          {
            'id': 1,
            'contactName': 'Mum',
            'contactPhone': '+36 30 111 2222',
            'contactEmail': 'mum@mail.com',
          },
          {
            'id': 2,
            'contactName': '  ',
            'contactPhone': null,
            'contactEmail': 'x@y.z',
          },
        ],
      });
      expect(emergency.id, 12);
      expect(emergency.source, TriggerSource.manualSos);
      expect(emergency.contacts, hasLength(2));
      expect(emergency.contacts.first.name, 'Mum');
      expect(emergency.contacts.first.phone, '+36 30 111 2222');
      expect(
        emergency.contacts.last.name,
        'Contact',
        reason: 'a blank name falls back to a generic one',
      );
    });

    test('works with no contacts at all', () {
      final emergency = Emergency.fromJson({
        'id': 1,
        'triggerSource': 'IDLE_TIMEOUT',
        'resolved': false,
      });
      expect(emergency.contacts, isEmpty);
      expect(emergency.latitude, isNull);
    });

    test('every trigger source reads in plain words', () {
      expect(
        TriggerSource.parse('IDLE_TIMEOUT').description,
        contains('No movement'),
      );
      expect(
        TriggerSource.parse('ROUTE_DEVIATION').description,
        contains('route'),
      );
      expect(
        TriggerSource.parse('CONNECTION_LOST').description,
        contains('connection'),
      );
      expect(TriggerSource.parse('SOMETHING_NEW'), TriggerSource.unknown);
    });
  });

  group('AuthorityNumbers', () {
    test('parses the dto and trims blanks to null', () {
      final n = AuthorityNumbers.fromJson({
        'policeNumber': ' 107 ',
        'ambulanceNumber': '',
        'generalEmergencyNumber': '112',
        'countryName': 'Hungary',
        'countryCode': 'HU',
      });
      expect(n.police, '107');
      expect(n.ambulance, isNull);
      expect(n.general, '112');
      expect(n.countryName, 'Hungary');
    });

    test('a missing general number falls back to 112', () {
      expect(AuthorityNumbers.fromJson({'policeNumber': '999'}).general, '112');
    });

    test('survives a save and load', () {
      const original = AuthorityNumbers(
        police: '999',
        ambulance: '999',
        general: '112',
        countryName: 'UK',
        countryCode: 'GB',
      );
      final copy = AuthorityNumbers.fromJson(original.toJson());
      expect(
        (copy.police, copy.ambulance, copy.general, copy.countryCode),
        ('999', '999', '112', 'GB'),
      );
    });
  });

  group('dialUri', () {
    test('keeps digits, plus, star and hash only', () {
      expect(dialUri('+36 (1) 112-0')!.path, '+3611120');
      expect(dialUri('112')!.toString(), 'tel:112');
      expect(
        dialUri('*31#')!.toString(),
        'tel:*31%23',
        reason: 'a # is percent-encoded, which the dialer decodes',
      );
    });

    test('an empty or junk number gives nothing to dial', () {
      expect(dialUri(''), isNull);
      expect(dialUri('call me'), isNull);
    });

    test('it is always the dialer scheme, never a direct call', () {
      expect(dialUri('999')!.scheme, 'tel');
    });
  });
}

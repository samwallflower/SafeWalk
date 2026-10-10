import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/contacts/domain/contact_rules.dart';
import 'package:safewalk_mobile/features/contacts/domain/emergency_contact.dart';

void main() {
  group('ContactRules', () {
    test('name is required and capped', () {
      expect(ContactRules.name(' '), 'Contact name is required');
      expect(
        ContactRules.name('a' * 101),
        'Contact name must be under 100 characters',
      );
      expect(ContactRules.name('Marcus'), isNull);
    });

    test('email is required and must look valid', () {
      expect(ContactRules.email(''), 'Contact email is required');
      expect(
        ContactRules.email('nope'),
        'Contact email must be a valid email address',
      );
      expect(ContactRules.email('m@x.com'), isNull);
    });

    test('phone is optional but must be international format', () {
      expect(ContactRules.phone(''), isNull);
      expect(ContactRules.phone('+36301234567'), isNull);
      expect(
        ContactRules.phone('0630123'),
        'Use international format, e.g. +36301234567',
      );
      expect(
        ContactRules.phone('+0123456789'),
        isNotNull,
        reason: 'a country code never starts with 0',
      );
    });

    test('five contacts at most, like the backend', () {
      expect(maxEmergencyContacts, 5);
    });
  });

  group('EmergencyContact', () {
    test('parses the dto and builds initials', () {
      final c = EmergencyContact.fromJson({
        'id': 2,
        'contactName': 'Chloe Zhang',
        'contactEmail': 'c@z.com',
        'contactPhone': ' +3630 ',
      });
      expect(c.name, 'Chloe Zhang');
      expect(c.phone, '+3630');
      expect(c.initials, 'CZ');
    });

    test('a missing or blank phone is null', () {
      expect(
        EmergencyContact.fromJson({
          'id': 1,
          'contactName': 'A',
          'contactEmail': 'a@b.c',
          'contactPhone': '  ',
        }).phone,
        isNull,
      );
      expect(
        EmergencyContact.fromJson({
          'id': 1,
          'contactName': 'A',
          'contactEmail': 'a@b.c',
        }).phone,
        isNull,
      );
    });

    test('the request body leaves out a blank phone', () {
      expect(
        const ContactInput(
          name: ' Mum ',
          email: ' m@x.com ',
          phone: '',
        ).toJson(),
        {'contactName': 'Mum', 'contactEmail': 'm@x.com'},
      );
      expect(
        const ContactInput(
          name: 'Mum',
          email: 'm@x.com',
          phone: '+3630',
        ).toJson(),
        {
          'contactName': 'Mum',
          'contactEmail': 'm@x.com',
          'contactPhone': '+3630',
        },
      );
    });
  });
}

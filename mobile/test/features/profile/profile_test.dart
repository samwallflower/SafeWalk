import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/profile/domain/profile_rules.dart';
import 'package:safewalk_mobile/features/profile/domain/user_profile.dart';

void main() {
  group('UserProfile', () {
    test('parses the dto and shows the full name and initials', () {
      final p = UserProfile.fromJson({
        'id': 3,
        'email': 'e@v.com',
        'firstName': 'Elena',
        'lastName': 'Vance',
        'phoneNumber': '+3630',
      });
      expect(p.fullName, 'Elena Vance');
      expect(p.initials, 'EV');
      expect(p.phoneNumber, '+3630');
    });

    test('without a name it falls back to the email', () {
      final p = UserProfile.fromJson({
        'id': 1,
        'email': 'sam@x.com',
        'firstName': '',
        'lastName': ' ',
      });
      expect(p.fullName, 'sam@x.com');
      expect(p.initials, 'S');
    });
  });

  group('ProfileRules', () {
    test('names are required and capped at 100', () {
      expect(ProfileRules.name(' ', 'First name'), 'First name is required');
      expect(
        ProfileRules.name('a' * 101, 'Last name'),
        'Last name must be under 100 characters',
      );
      expect(ProfileRules.name('Elena', 'First name'), isNull);
    });

    test('a phone number is optional but must look valid when given', () {
      expect(ProfileRules.phone(''), isNull);
      expect(ProfileRules.phone('+36 30 123 4567'), isNull);
      expect(ProfileRules.phone('(555) 123-4567'), isNull);
      expect(ProfileRules.phone('12'), 'Enter a valid phone number');
      expect(ProfileRules.phone('call me'), 'Enter a valid phone number');
    });
  });
}

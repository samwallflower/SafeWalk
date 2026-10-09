import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/features/auth/domain/validators.dart';

void main() {
  group('email', () {
    test('requires a value and a valid shape', () {
      expect(AuthValidators.email(''), 'Email is required');
      expect(
        AuthValidators.email('nope'),
        'Email must be a valid email address',
      );
      expect(AuthValidators.email(' a@b.co '), isNull);
    });
  });

  group('newPassword mirrors the Java rules', () {
    test('length 8 to 72', () {
      expect(
        AuthValidators.newPassword('abc1'),
        'Password must be between 8 and 72 characters',
      );
      expect(
        AuthValidators.newPassword('a1${'x' * 80}'),
        'Password must be between 8 and 72 characters',
      );
    });

    test('needs a letter and a number', () {
      expect(
        AuthValidators.newPassword('abcdefgh'),
        'Password must contain at least one letter and one number',
      );
      expect(
        AuthValidators.newPassword('12345678'),
        'Password must contain at least one letter and one number',
      );
      expect(AuthValidators.newPassword('abcdefg1'), isNull);
    });
  });

  test('login only needs a password', () {
    expect(AuthValidators.loginPassword(''), 'Password is required');
    expect(AuthValidators.loginPassword('x'), isNull);
  });

  test('names are required and capped', () {
    expect(AuthValidators.name(' ', 'First name'), 'First name is required');
    expect(
      AuthValidators.name('a' * 101, 'Last name'),
      'Last name must be under 100 characters',
    );
  });
}

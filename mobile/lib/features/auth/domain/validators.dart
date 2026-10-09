/// Mirror the Java validation (UserLoginRequest, UserRegisterRequest). Each returns an error message or null.
abstract final class AuthValidators {
  static final _email = RegExp(r'^[^@\s]+@[^@\s]+\.[^@\s]+$');
  static final _letterAndDigit = RegExp(r'^(?=.*[A-Za-z])(?=.*\d).+$');

  static String? email(String? value) {
    final v = value?.trim() ?? '';
    if (v.isEmpty) return 'Email is required';
    if (v.length > 255) return 'Email must be under 255 characters';
    if (!_email.hasMatch(v)) return 'Email must be a valid email address';
    return null;
  }

  /// Sign-in only checks presence; the server decides whether the password is right.
  static String? loginPassword(String? value) =>
      (value == null || value.isEmpty) ? 'Password is required' : null;

  static String? newPassword(String? value) {
    final v = value ?? '';
    if (v.isEmpty) return 'Password is required';
    if (v.length < 8 || v.length > 72) {
      return 'Password must be between 8 and 72 characters';
    }
    if (!_letterAndDigit.hasMatch(v)) {
      return 'Password must contain at least one letter and one number';
    }
    return null;
  }

  static String? name(String? value, String label) {
    final v = value?.trim() ?? '';
    if (v.isEmpty) return '$label is required';
    if (v.length > 100) return '$label must be under 100 characters';
    return null;
  }

  static String? verificationCode(String? value) =>
      (value == null || value.trim().isEmpty)
      ? 'Verification code is required'
      : null;
}

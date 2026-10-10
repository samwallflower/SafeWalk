/// Mirrors UserUpdateRequest.java
abstract final class ProfileRules {
  static final _phone = RegExp(r'^\+?[0-9\s\-()]{7,20}$');

  static String? name(String? value, String label) {
    final v = value?.trim() ?? '';
    if (v.isEmpty) return '$label is required';
    if (v.length > 100) return '$label must be under 100 characters';
    return null;
  }

  /// Optional, but valid when given.
  static String? phone(String? value) {
    final v = value?.trim() ?? '';
    if (v.isEmpty) return null;
    return _phone.hasMatch(v) ? null : 'Enter a valid phone number';
  }
}

/// The backend allows at most this many contacts per person (app.emergency-contact.add.max-count).
const maxEmergencyContacts = 5;

/// Mirrors AddEmergencyContactRequest.java
abstract final class ContactRules {
  static final _email = RegExp(r'^[^@\s]+@[^@\s]+\.[^@\s]+$');
  static final _e164 = RegExp(r'^\+[1-9]\d{6,14}$');

  static String? name(String? value) {
    final v = value?.trim() ?? '';
    if (v.isEmpty) return 'Contact name is required';
    if (v.length > 100) return 'Contact name must be under 100 characters';
    return null;
  }

  static String? email(String? value) {
    final v = value?.trim() ?? '';
    if (v.isEmpty) return 'Contact email is required';
    if (v.length > 255) return 'Contact email must be under 255 characters';
    if (!_email.hasMatch(v)) {
      return 'Contact email must be a valid email address';
    }
    return null;
  }

  /// Optional, but in international format when given.
  static String? phone(String? value) {
    final v = value?.trim() ?? '';
    if (v.isEmpty) return null;
    return _e164.hasMatch(v)
        ? null
        : 'Use international format, e.g. +36301234567';
  }
}

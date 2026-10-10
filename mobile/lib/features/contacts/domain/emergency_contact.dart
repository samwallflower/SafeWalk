/// Mirrors EmergencyContactDto.java
class EmergencyContact {
  const EmergencyContact({
    required this.id,
    required this.name,
    required this.email,
    this.phone,
  });

  factory EmergencyContact.fromJson(Map<String, Object?> json) =>
      EmergencyContact(
        id: (json['id'] as num).toInt(),
        name: (json['contactName'] as String? ?? '').trim(),
        email: json['contactEmail'] as String? ?? '',
        phone: (json['contactPhone'] as String?)?.trim().isEmpty ?? true
            ? null
            : (json['contactPhone'] as String).trim(),
      );

  final int id;
  final String name;
  final String email;
  final String? phone;

  String get initials {
    final parts = name
        .split(RegExp(r'\s+'))
        .where((p) => p.isNotEmpty)
        .toList();
    if (parts.isEmpty) return '?';
    final first = parts.first[0];
    final last = parts.length > 1 ? parts.last[0] : '';
    return '$first$last'.toUpperCase();
  }
}

/// What the user types into the contact form.
class ContactInput {
  const ContactInput({required this.name, required this.email, this.phone});

  final String name;
  final String email;
  final String? phone;

  /// A blank phone is left out, so the backend treats it as "not provided".
  Map<String, Object?> toJson() => {
    'contactName': name.trim(),
    'contactEmail': email.trim(),
    if (phone != null && phone!.trim().isNotEmpty)
      'contactPhone': phone!.trim(),
  };
}

/// Mirrors UserDto.java
class UserProfile {
  const UserProfile({
    required this.id,
    required this.email,
    required this.firstName,
    required this.lastName,
    this.phoneNumber,
  });

  factory UserProfile.fromJson(Map<String, Object?> json) => UserProfile(
    id: (json['id'] as num).toInt(),
    email: json['email'] as String? ?? '',
    firstName: (json['firstName'] as String? ?? '').trim(),
    lastName: (json['lastName'] as String? ?? '').trim(),
    phoneNumber: json['phoneNumber'] as String?,
  );

  final int id;
  final String email;
  final String firstName;
  final String lastName;
  final String? phoneNumber;

  String get fullName {
    final name = '$firstName $lastName'.trim();
    return name.isEmpty ? email : name;
  }

  /// "EV" for Elena Vance, or the first letter of the email when there is no name.
  String get initials {
    final first = firstName.isEmpty ? '' : firstName[0];
    final last = lastName.isEmpty ? '' : lastName[0];
    final both = '$first$last'.toUpperCase();
    if (both.isNotEmpty) return both;
    return email.isEmpty ? '?' : email[0].toUpperCase();
  }
}

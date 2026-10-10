/// Where a set of numbers came from, so the screen can be honest about how fresh they are.
enum NumbersSource { live, saved, fallback }

/// Mirrors EmergencyAuthorityDto.java
class AuthorityNumbers {
  const AuthorityNumbers({
    required this.general,
    this.police,
    this.ambulance,
    this.countryName,
    this.countryCode,
    this.source = NumbersSource.live,
  });

  factory AuthorityNumbers.fromJson(Map<String, Object?> json) =>
      AuthorityNumbers(
        police: _number(json['policeNumber']),
        ambulance: _number(json['ambulanceNumber']),
        general: _number(json['generalEmergencyNumber']) ?? fallbackNumber,
        countryName: json['countryName'] as String?,
        countryCode: json['countryCode'] as String?,
      );

  /// 112 works across the EU and from most phones elsewhere, so it is the last resort.
  static const fallbackNumber = '112';

  static const fallback = AuthorityNumbers(
    general: fallbackNumber,
    source: NumbersSource.fallback,
  );

  final String? police;
  final String? ambulance;
  final String general;
  final String? countryName;
  final String? countryCode;
  final NumbersSource source;

  AuthorityNumbers withSource(NumbersSource value) => AuthorityNumbers(
    police: police,
    ambulance: ambulance,
    general: general,
    countryName: countryName,
    countryCode: countryCode,
    source: value,
  );

  Map<String, Object?> toJson() => {
    'policeNumber': police,
    'ambulanceNumber': ambulance,
    'generalEmergencyNumber': general,
    'countryName': countryName,
    'countryCode': countryCode,
  };

  static String? _number(Object? value) {
    final text = value is String ? value.trim() : '';
    return text.isEmpty ? null : text;
  }
}

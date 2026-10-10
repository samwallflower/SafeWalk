enum TriggerSource {
  manualSos('You pressed SOS'),
  idleTimeout('No movement was detected and there was no answer'),
  routeDeviation('You left your route'),
  connectionLost('Your connection to SafeWalk dropped'),
  system('SafeWalk raised the alert'),
  unknown('An alert was raised');

  const TriggerSource(this.description);

  final String description;

  static TriggerSource parse(Object? value) => switch (value) {
    'MANUAL_SOS' => TriggerSource.manualSos,
    'IDLE_TIMEOUT' => TriggerSource.idleTimeout,
    'ROUTE_DEVIATION' => TriggerSource.routeDeviation,
    'CONNECTION_LOST' => TriggerSource.connectionLost,
    'SYSTEM' => TriggerSource.system,
    _ => TriggerSource.unknown,
  };
}

/// A contact that was told about the emergency (name and phone only: their email is not shown).
class NotifiedContact {
  const NotifiedContact({required this.name, this.phone});

  final String name;
  final String? phone;
}

/// Mirrors EmergencyDto.java
class Emergency {
  const Emergency({
    required this.id,
    required this.source,
    required this.latitude,
    required this.longitude,
    required this.triggeredAt,
    required this.resolved,
    required this.contacts,
  });

  factory Emergency.fromJson(Map<String, Object?> json) {
    final contacts =
        (json['notifiedEmergencyContacts'] as List<Object?>? ?? const [])
            .whereType<Map<String, Object?>>()
            .map(
              (c) => NotifiedContact(
                name: (c['contactName'] as String?)?.trim().isNotEmpty == true
                    ? (c['contactName'] as String).trim()
                    : 'Contact',
                phone: c['contactPhone'] as String?,
              ),
            )
            .toList();
    return Emergency(
      id: (json['id'] as num).toInt(),
      source: TriggerSource.parse(json['triggerSource']),
      latitude: (json['triggerLatitude'] as num?)?.toDouble(),
      longitude: (json['triggerLongitude'] as num?)?.toDouble(),
      triggeredAt: json['triggerTimestamp'] as String? ?? '',
      resolved: json['resolved'] == true,
      contacts: contacts,
    );
  }

  final int id;
  final TriggerSource source;
  final double? latitude;
  final double? longitude;
  final String triggeredAt;
  final bool resolved;
  final List<NotifiedContact> contacts;
}

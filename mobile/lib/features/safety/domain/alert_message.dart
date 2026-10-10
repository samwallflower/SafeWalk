import 'dart:convert';

enum AlertType {
  idleWarning,
  emergencyTriggered,
  routeDeviation,
  autocomplete,
  emergencyResolved,
  unknown;

  static AlertType parse(Object? value) => switch (value) {
    'IDLE_WARNING' => AlertType.idleWarning,
    'EMERGENCY_TRIGGERED' => AlertType.emergencyTriggered,
    'ROUTE_DEVIATION' => AlertType.routeDeviation,
    'AUTOCOMPLETE' => AlertType.autocomplete,
    'EMERGENCY_RESOLVED' => AlertType.emergencyResolved,
    _ => AlertType.unknown,
  };
}

/// Mirrors AlertMessage.java, pushed to /topic/alert/{sessionId}.
class AlertMessage {
  const AlertMessage({
    required this.sessionId,
    required this.type,
    required this.message,
    this.emergencyId,
  });

  final int? sessionId;
  final int? emergencyId;
  final AlertType type;
  final String message;

  /// Null when [body] is not an alert (a malformed frame must never crash the walk).
  static AlertMessage? tryParse(String? body) {
    if (body == null || body.isEmpty) return null;
    try {
      final json = jsonDecode(body);
      if (json is! Map<String, Object?>) return null;
      return AlertMessage(
        sessionId: (json['sessionId'] as num?)?.toInt(),
        emergencyId: (json['emergencyId'] as num?)?.toInt(),
        type: AlertType.parse(json['type']),
        message: json['message'] as String? ?? '',
      );
    } on FormatException {
      return null;
    }
  }
}

enum SessionStatus {
  active,
  completed,
  emergency,
  abandoned;

  static SessionStatus parse(Object? value) => switch (value) {
    'COMPLETED' => SessionStatus.completed,
    'EMERGENCY' => SessionStatus.emergency,
    'ABANDONED' => SessionStatus.abandoned,
    _ => SessionStatus.active,
  };
}

/// Mirrors WalkSessionDto.java
class WalkSession {
  const WalkSession({
    required this.id,
    required this.userId,
    required this.routeId,
    required this.startTime,
    required this.endTime,
    required this.status,
    required this.originLatitude,
    required this.originLongitude,
    required this.destinationLatitude,
    required this.destinationLongitude,
    required this.autoCompleted,
    this.alarmTriggered = false,
    this.deviationTriggered = false,
  });

  factory WalkSession.fromJson(Map<String, Object?> json) => WalkSession(
    id: (json['id'] as num).toInt(),
    userId: (json['userId'] as num?)?.toInt() ?? 0,
    routeId: (json['routeId'] as num?)?.toInt(),
    startTime: json['startTime'] as String? ?? '',
    endTime: json['endTime'] as String?,
    status: SessionStatus.parse(json['status']),
    originLatitude: (json['originLatitude'] as num?)?.toDouble(),
    originLongitude: (json['originLongitude'] as num?)?.toDouble(),
    destinationLatitude: (json['destinationLatitude'] as num?)?.toDouble(),
    destinationLongitude: (json['destinationLongitude'] as num?)?.toDouble(),
    autoCompleted: json['autoCompleted'] == true,
    alarmTriggered: json['alarmTriggered'] == true,
    deviationTriggered: json['deviationTriggered'] == true,
  );

  final int id;
  final int userId;
  final int? routeId;
  final String startTime;
  final String? endTime;
  final SessionStatus status;
  final double? originLatitude;
  final double? originLongitude;
  final double? destinationLatitude;
  final double? destinationLongitude;

  /// The server ended it because the walker reached the destination.
  final bool autoCompleted;

  /// The server sent the "are you safe?" warning and is waiting for an answer.
  final bool alarmTriggered;

  /// The walker is off the planned route.
  final bool deviationTriggered;

  bool get isFinished => status == SessionStatus.completed;
}

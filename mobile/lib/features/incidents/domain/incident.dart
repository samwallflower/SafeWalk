import '../../categories/domain/incident_category.dart';

enum ReportStatus { active, hidden, underReview }

ReportStatus _status(Object? value) => switch (value) {
  'HIDDEN' => ReportStatus.hidden,
  'UNDER_REVIEW' => ReportStatus.underReview,
  _ => ReportStatus.active,
};

/// Mirrors HeatMapPointDto.java
class HeatPoint {
  const HeatPoint({
    required this.latitude,
    required this.longitude,
    required this.severityWeight,
  });

  factory HeatPoint.fromJson(Map<String, Object?> json) => HeatPoint(
    latitude: (json['latitude'] as num).toDouble(),
    longitude: (json['longitude'] as num).toDouble(),
    severityWeight: (json['severityWeight'] as num?)?.toDouble() ?? 0,
  );

  final double latitude;
  final double longitude;
  final double severityWeight;
}

/// The UI model of a report.
///
/// This is the privacy boundary: the backend attaches the whole reporter to every report, but only a display
/// name ("Elena V.") is ever kept here, and only when the report is not anonymous. Email and phone are dropped.
class Incident {
  const Incident({
    required this.id,
    required this.isAnonymous,
    required this.reporterName,
    required this.description,
    required this.latitude,
    required this.longitude,
    required this.timestamp,
    required this.upvotes,
    required this.downvotes,
    required this.category,
    required this.status,
  });

  factory Incident.fromDto(Map<String, Object?> dto) {
    final anonymous = dto['isAnonymous'] != false;
    return Incident(
      id: (dto['id'] as num).toInt(),
      isAnonymous: anonymous,
      reporterName: anonymous ? null : _displayName(dto['user']),
      description: dto['description'] as String? ?? '',
      latitude: (dto['latitude'] as num).toDouble(),
      longitude: (dto['longitude'] as num).toDouble(),
      timestamp: dto['timestamp'] as String? ?? '',
      upvotes: (dto['upvotes'] as num?)?.toInt() ?? 0,
      downvotes: (dto['downvotes'] as num?)?.toInt() ?? 0,
      category: IncidentCategory.fromJson(
        dto['category'] as Map<String, Object?>,
      ),
      status: _status(dto['status']),
    );
  }

  final int id;
  final bool isAnonymous;
  final String? reporterName;
  final String description;
  final double latitude;
  final double longitude;
  final String timestamp;
  final int upvotes;
  final int downvotes;
  final IncidentCategory category;
  final ReportStatus status;

  HeatPoint toHeatPoint() => HeatPoint(
    latitude: latitude,
    longitude: longitude,
    severityWeight: category.severityWeight,
  );
}

String? _displayName(Object? user) {
  if (user is! Map<String, Object?>) return null;
  final first = (user['firstName'] as String?)?.trim() ?? '';
  final last = (user['lastName'] as String?)?.trim() ?? '';
  if (first.isEmpty) return null;
  return last.isEmpty ? first : '$first ${last[0].toUpperCase()}.';
}

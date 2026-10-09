const descriptionMax = 280;

/// Mirrors AddIncidentReportRequest (the backend has no description rules; 280 comes from the design).
abstract final class ReportRules {
  static String? description(String? value) {
    final v = value?.trim() ?? '';
    if (v.isEmpty) return 'Describe what you observed';
    if (v.length > descriptionMax) {
      return 'Keep it under $descriptionMax characters';
    }
    return null;
  }

  static String? latitude(double value) => (value < -90 || value > 90)
      ? 'Latitude must be between -90 and 90'
      : null;

  static String? longitude(double value) => (value < -180 || value > 180)
      ? 'Longitude must be between -180 and 180'
      : null;
}

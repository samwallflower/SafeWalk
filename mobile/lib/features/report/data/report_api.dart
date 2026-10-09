import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../../../core/network/providers.dart';
import '../../categories/domain/incident_category.dart';

class NewReport {
  const NewReport({
    required this.description,
    required this.latitude,
    required this.longitude,
    required this.isAnonymous,
    required this.category,
  });

  final String description;
  final double latitude;
  final double longitude;
  final bool isAnonymous;
  final IncidentCategory category;

  /// Mirrors AddIncidentReportRequest. The backend resolves the category by name.
  Map<String, Object?> toJson() => {
    'description': description,
    'latitude': latitude,
    'longitude': longitude,
    'isAnonymous': isAnonymous,
    'category': {'id': category.id, 'name': category.name},
  };
}

class ReportApi {
  ReportApi(this._client);

  final ApiClient _client;

  /// [userId] must come from the session. The backend rate-limits (429) and sends a readable message.
  Future<void> create(int userId, NewReport report) => _client.postObject(
    '/incident-reports/$userId/report/add',
    body: report.toJson(),
  );
}

final reportApiProvider = Provider<ReportApi>(
  (ref) => ReportApi(ref.watch(apiClientProvider)),
);

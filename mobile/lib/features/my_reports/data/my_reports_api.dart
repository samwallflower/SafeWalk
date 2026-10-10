import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../../../core/network/providers.dart';
import '../../auth/presentation/state/session_controller.dart';
import '../../incidents/domain/incident.dart';

class MyReportsApi {
  MyReportsApi(this._client);

  final ApiClient _client;

  /// The person's own reports, newest first. One page of up to 50 (the backend's limit).
  Future<List<Incident>> list(int userId) async {
    final content = await _client.getPageContent(
      '/incident-reports/user/$userId/report',
      query: {'page': 0, 'size': 50},
    );
    return content.map(Incident.fromDto).toList();
  }

  Future<int> count(int userId) async => ((await _client.getValue(
    '/incident-reports/count/user/$userId/report',
  )) as num).toInt();

  Future<void> remove(int userId, int reportId) =>
      _client.delete('/incident-reports/$userId/report/$reportId/delete');
}

final myReportsApiProvider = Provider<MyReportsApi>(
  (ref) => MyReportsApi(ref.watch(apiClientProvider)),
);

final myReportsProvider = FutureProvider.autoDispose<List<Incident>>((
  ref,
) async {
  final userId = ref.watch(sessionProvider.select((s) => s.value?.id));
  if (userId == null) return const [];
  return ref.watch(myReportsApiProvider).list(userId);
});

final myReportCountProvider = FutureProvider.autoDispose<int>((ref) async {
  final userId = ref.watch(sessionProvider.select((s) => s.value?.id));
  if (userId == null) return 0;
  return ref.watch(myReportsApiProvider).count(userId);
});

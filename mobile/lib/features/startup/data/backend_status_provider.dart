import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/providers.dart';

/// Phase M0 smoke test: the public categories endpoint proves the URL, Dio and the envelope all work.
final categoryCountProvider = FutureProvider.autoDispose<int>((ref) async {
  final list = await ref
      .watch(apiClientProvider)
      .getList('/incident-categories/all');
  return list.length;
});

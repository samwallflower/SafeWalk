import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'core/routing/app_router.dart';
import 'features/safety/presentation/state/safety_controller.dart';
import 'core/theme/theme.dart';

class SafeWalkApp extends ConsumerWidget {
  const SafeWalkApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // Keep the safety logic alive for the whole app so it reacts to alerts on any screen.
    ref.watch(safetyProvider);
    return MaterialApp.router(
      title: 'SafeWalk',
      debugShowCheckedModeBanner: false,
      theme: buildTheme(),
      routerConfig: ref.watch(routerProvider),
    );
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/auth/presentation/login_screen.dart';
import '../../features/auth/presentation/register_screen.dart';
import '../../features/auth/presentation/state/session_controller.dart';
import '../../features/auth/presentation/verify_screen.dart';
import '../../features/startup/presentation/home_placeholder.dart';
import '../widgets/loading.dart';
import 'app_routes.dart';
import 'route_guard.dart';

final routerProvider = Provider<GoRouter>((ref) {
  // Re-run the redirect whenever the session changes (login, logout, expiry).
  final refresh = ValueNotifier<int>(0);
  ref.listen(sessionProvider, (previous, next) => refresh.value++);
  ref.onDispose(refresh.dispose);

  return GoRouter(
    initialLocation: AppRoutes.splash,
    refreshListenable: refresh,
    redirect: (context, state) {
      final session = ref.read(sessionProvider);
      return decideRedirect(
        location: state.uri.toString(),
        sessionLoading: session.isLoading,
        user: session.value,
      );
    },
    routes: [
      GoRoute(
        path: AppRoutes.splash,
        builder: (context, state) =>
            const Scaffold(body: LoadingView(label: 'Starting SafeWalk')),
      ),
      GoRoute(
        path: AppRoutes.login,
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: AppRoutes.register,
        builder: (context, state) => const RegisterScreen(),
      ),
      GoRoute(
        path: AppRoutes.verify,
        builder: (context, state) =>
            VerifyScreen(email: state.uri.queryParameters['email'] ?? ''),
      ),
      GoRoute(
        path: AppRoutes.home,
        builder: (context, state) => const HomePlaceholder(),
      ),
    ],
  );
});

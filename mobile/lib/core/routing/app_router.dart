import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/auth/presentation/login_screen.dart';
import '../../features/auth/presentation/register_screen.dart';
import '../../features/auth/presentation/state/session_controller.dart';
import '../../features/auth/presentation/verify_screen.dart';
import '../../features/map/presentation/map_screen.dart';
import '../widgets/coming_soon_screen.dart';
import '../widgets/loading.dart';
import 'app_routes.dart';
import 'app_shell.dart';
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
      ShellRoute(
        builder: (context, state, child) =>
            AppShell(location: state.uri.path, child: child),
        routes: [
          GoRoute(
            path: AppRoutes.home,
            builder: (context, state) => const MapScreen(),
          ),
          GoRoute(
            path: AppRoutes.walk,
            builder: (context, state) => const ComingSoonScreen(
              title: 'Walk',
              icon: Icons.directions_walk,
            ),
          ),
          GoRoute(
            path: AppRoutes.report,
            builder: (context, state) => const ComingSoonScreen(
              title: 'Report an incident',
              icon: Icons.warning_amber_rounded,
            ),
          ),
          GoRoute(
            path: AppRoutes.safety,
            builder: (context, state) => const ComingSoonScreen(
              title: 'My Safety',
              icon: Icons.shield_outlined,
            ),
          ),
        ],
      ),
    ],
  );
});

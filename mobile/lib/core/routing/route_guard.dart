import '../../features/auth/domain/session_user.dart';
import 'app_routes.dart';

/// Returns where to send the user, or null to stay. Routing-only; the backend still authorizes each call.
String? decideRedirect({
  required String location,
  required bool sessionLoading,
  required SessionUser? user,
}) {
  if (sessionLoading) {
    return location == AppRoutes.splash ? null : AppRoutes.splash;
  }
  final path = Uri.parse(location).path;
  if (user == null) {
    return AppRoutes.guestOnly.contains(path) ? null : AppRoutes.login;
  }
  if (AppRoutes.guestOnly.contains(path) || path == AppRoutes.splash) {
    return AppRoutes.home;
  }
  return null;
}

abstract final class AppRoutes {
  static const splash = '/splash';
  static const login = '/login';
  static const register = '/register';
  static const verify = '/verify';
  static const home = '/';

  static const guestOnly = {login, register, verify};
}

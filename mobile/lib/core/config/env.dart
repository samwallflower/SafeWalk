/// Compile-time configuration. Supplied with `--dart-define-from-file=env/dev.json`.
abstract final class Env {
  static const baseUrl = String.fromEnvironment(
    'BASE_URL',
    defaultValue: 'http://127.0.0.1:8080',
  );
  static const apiPrefix = String.fromEnvironment(
    'API_PREFIX',
    defaultValue: '/api/v1',
  );
  static const mapboxToken = String.fromEnvironment('MAPBOX_TOKEN');
  static const demoLoginEnabled = bool.fromEnvironment('DEMO_LOGIN_ENABLED');
  static const demoUserEmail = String.fromEnvironment('DEMO_USER_EMAIL');
  static const demoUserPassword = String.fromEnvironment('DEMO_USER_PASSWORD');
  static const demoAdminEmail = String.fromEnvironment('DEMO_ADMIN_EMAIL');
  static const demoAdminPassword = String.fromEnvironment(
    'DEMO_ADMIN_PASSWORD',
  );

  static String get apiBaseUrl => '$baseUrl$apiPrefix';
}

/// The signed-in user, read from the JWT. Used for routing and display only; the backend authorizes every call.
class SessionUser {
  const SessionUser({
    required this.id,
    required this.email,
    required this.roles,
  });

  final int id;
  final String email;
  final List<String> roles;

  bool get isAdmin => roles.contains('ROLE_ADMIN');
}

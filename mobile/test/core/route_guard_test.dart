import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/routing/route_guard.dart';
import 'package:safewalk_mobile/features/auth/domain/session_user.dart';

const _user = SessionUser(id: 1, email: 'a@b.c', roles: ['ROLE_USER']);

void main() {
  test('shows the splash while the session is loading', () {
    expect(
      decideRedirect(location: '/', sessionLoading: true, user: null),
      '/splash',
    );
    expect(
      decideRedirect(location: '/splash', sessionLoading: true, user: null),
      isNull,
    );
  });

  test('sends guests to login but lets them reach the auth screens', () {
    expect(
      decideRedirect(location: '/', sessionLoading: false, user: null),
      '/login',
    );
    expect(
      decideRedirect(location: '/login', sessionLoading: false, user: null),
      isNull,
    );
    expect(
      decideRedirect(location: '/register', sessionLoading: false, user: null),
      isNull,
    );
    expect(
      decideRedirect(
        location: '/verify?email=a@b.c',
        sessionLoading: false,
        user: null,
      ),
      isNull,
    );
  });

  test('keeps signed-in users out of the auth screens', () {
    expect(
      decideRedirect(location: '/login', sessionLoading: false, user: _user),
      '/',
    );
    expect(
      decideRedirect(location: '/splash', sessionLoading: false, user: _user),
      '/',
    );
    expect(
      decideRedirect(location: '/', sessionLoading: false, user: _user),
      isNull,
    );
  });
}

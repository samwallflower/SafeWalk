import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/network/providers.dart';
import '../../../../core/widgets/inline_notice.dart';
import '../../data/auth_api.dart';
import '../../domain/jwt_decoder.dart';
import '../../domain/session_user.dart';

/// A one-off message for the login screen, e.g. "Your session expired".
class SessionMessage {
  const SessionMessage(this.text, {this.tone = NoticeTone.error});

  final String text;
  final NoticeTone tone;
}

final sessionNoticeProvider = NotifierProvider<SessionNotice, SessionMessage?>(
  SessionNotice.new,
);

class SessionNotice extends Notifier<SessionMessage?> {
  @override
  SessionMessage? build() => null;

  void show(String message, {NoticeTone tone = NoticeTone.error}) =>
      state = SessionMessage(message, tone: tone);
  void clear() => state = null;
}

/// `null` data means signed out. While the stored token is being read the state is loading.
final sessionProvider = AsyncNotifierProvider<SessionController, SessionUser?>(
  SessionController.new,
);

class SessionController extends AsyncNotifier<SessionUser?> {
  @override
  Future<SessionUser?> build() async {
    ref.listen(sessionExpiredProvider, (previous, next) => _expire());

    final store = ref.read(secureStoreProvider);
    final token = await store.readToken();
    if (token == null) return null;
    final decoded = decodeToken(token);
    if (decoded == null || decoded.isExpiredAt(DateTime.now())) {
      await store.clear();
      return null;
    }
    return decoded.user;
  }

  Future<void> login(String email, String password) async {
    final token = await ref.read(authApiProvider).login(email.trim(), password);
    final decoded = token == null ? null : decodeToken(token);
    if (token == null || decoded == null) {
      throw const ApiException(
        0,
        'Unexpected response from the server. Please try again.',
      );
    }
    await ref.read(secureStoreProvider).writeToken(token);
    ref.read(sessionNoticeProvider.notifier).clear();
    state = AsyncData(decoded.user);
  }

  Future<void> logout() async {
    await ref.read(secureStoreProvider).clear();
    state = const AsyncData(null);
  }

  void _expire() {
    if (state.value == null) return;
    state = const AsyncData(null);
    ref
        .read(sessionNoticeProvider.notifier)
        .show('Your session expired. Please sign in again.');
  }
}

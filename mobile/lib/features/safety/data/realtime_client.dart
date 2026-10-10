import 'dart:async';
import 'dart:convert';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';

import '../../../core/config/env.dart';
import '../domain/alert_message.dart';
import '../domain/reconnect_backoff.dart';

/// The live connection to the server for one walk: it delivers that walk's alerts and tells the server the phone is
/// still connected. An interface so the safety logic can be tested without a server.
abstract class RealtimeClient {
  /// Connects and keeps reconnecting (with growing delays) until [disconnect].
  void connect({
    required int sessionId,
    required Future<String?> Function() tokenProvider,
    required void Function(AlertMessage alert) onAlert,
    required void Function(bool connected) onConnection,
  });

  /// Tells the server this connection belongs to the walk. Until the first call the server does not watch the
  /// connection, and after a reconnect it must be called again.
  void register(LatLng position);

  void disconnect();
}

/// The server checks this header on every connect and refuses a connection without a valid token.
Map<String, String> stompConnectHeaders(String token) => {
  'Authorization': 'Bearer $token',
};

/// STOMP over SockJS at `/ws`, signed in with the JWT. Regular location reports go through REST (it confirms each one);
/// the socket carries the alerts and, through one message per connection, the "I am connected" signal.
class StompRealtimeClient implements RealtimeClient {
  StompClient? _client;
  Timer? _retry;
  int _attempt = 0;
  int? _sessionId;
  bool _active = false;
  Future<String?> Function()? _token;
  void Function(AlertMessage)? _onAlert;
  void Function(bool)? _onConnection;

  @override
  void connect({
    required int sessionId,
    required Future<String?> Function() tokenProvider,
    required void Function(AlertMessage alert) onAlert,
    required void Function(bool connected) onConnection,
  }) {
    disconnect();
    _sessionId = sessionId;
    _token = tokenProvider;
    _onAlert = onAlert;
    _onConnection = onConnection;
    _active = true;
    _attempt = 0;
    _open();
  }

  void _scheduleRetry() {
    if (!_active || _retry != null) return;
    _onConnection?.call(false);
    _retry = Timer(reconnectDelay(_attempt++), () {
      _retry = null;
      _open();
    });
  }

  Future<void> _open() async {
    if (!_active) return;
    final sessionId = _sessionId;
    if (sessionId == null) return;
    _closeClient();

    // A fresh token every time, so a reconnect after a long walk does not use an old one.
    final token = await _token?.call();
    if (!_active) return;
    if (token == null || token.isEmpty) {
      _scheduleRetry();
      return;
    }

    StompClient? self;
    void lost() {
      // Several callbacks fire for one drop; only react for the client that is still current.
      if (!identical(_client, self)) return;
      _scheduleRetry();
    }

    self = StompClient(
      config: StompConfig.sockJS(
        url: '${Env.baseUrl}/ws',
        stompConnectHeaders: stompConnectHeaders(token),
        // Reconnecting is handled here, with backoff.
        reconnectDelay: Duration.zero,
        onConnect: (frame) {
          if (!_active || !identical(_client, self)) return;
          _attempt = 0;
          self?.subscribe(
            destination: '/topic/alert/$sessionId',
            callback: (frame) {
              final alert = AlertMessage.tryParse(frame.body);
              if (alert != null) _onAlert?.call(alert);
            },
          );
          _onConnection?.call(true);
        },
        onWebSocketDone: lost,
        onWebSocketError: (dynamic error) => lost(),
        onStompError: (frame) => lost(),
        onDisconnect: (frame) => lost(),
      ),
    );
    _client = self;
    self.activate();
  }

  @override
  void register(LatLng position) {
    final client = _client;
    final sessionId = _sessionId;
    if (client == null || !client.connected || sessionId == null) return;
    client.send(
      destination: '/app/session.location',
      body: jsonEncode({
        'sessionId': sessionId,
        'latitude': position.latitude,
        'longitude': position.longitude,
      }),
    );
  }

  void _closeClient() {
    final client = _client;
    _client = null;
    client?.deactivate();
  }

  @override
  void disconnect() {
    _active = false;
    _retry?.cancel();
    _retry = null;
    _closeClient();
    _sessionId = null;
    _token = null;
    _onAlert = null;
    _onConnection = null;
  }
}

final realtimeClientProvider = Provider<RealtimeClient>((ref) {
  final client = StompRealtimeClient();
  ref.onDispose(client.disconnect);
  return client;
});

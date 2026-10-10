import 'package:flutter/widgets.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Heads-up notifications for safety alerts that arrive while the app is not on screen.
/// An interface so the safety logic can be tested without a phone.
abstract class AlertNotifier {
  /// Asks for the Android 13+ notification permission. False if the user refuses.
  Future<bool> requestPermission();

  Future<void> show({
    required int id,
    required String title,
    required String body,
  });
}

class LocalAlertNotifier implements AlertNotifier {
  final _plugin = FlutterLocalNotificationsPlugin();
  bool _ready = false;

  static const _channel = AndroidNotificationChannel(
    'safewalk_alerts',
    'Safety alerts',
    description:
        'Alerts about your walk: are you safe, off route, emergencies.',
    importance: Importance.max,
  );

  Future<void> _init() async {
    if (_ready) return;
    await _plugin.initialize(
      settings: const InitializationSettings(
        android: AndroidInitializationSettings('@mipmap/ic_launcher'),
      ),
    );
    await _plugin
        .resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin
        >()
        ?.createNotificationChannel(_channel);
    _ready = true;
  }

  @override
  Future<bool> requestPermission() async {
    await _init();
    final granted = await _plugin
        .resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin
        >()
        ?.requestNotificationsPermission();
    return granted ?? false;
  }

  @override
  Future<void> show({
    required int id,
    required String title,
    required String body,
  }) async {
    await _init();
    await _plugin.show(
      id: id,
      title: title,
      body: body,
      notificationDetails: NotificationDetails(
        android: AndroidNotificationDetails(
          _channel.id,
          _channel.name,
          channelDescription: _channel.description,
          importance: Importance.max,
          priority: Priority.high,
          category: AndroidNotificationCategory.alarm,
          visibility: NotificationVisibility.public,
          enableVibration: true,
          playSound: true,
        ),
      ),
    );
  }
}

final alertNotifierProvider = Provider<AlertNotifier>(
  (ref) => LocalAlertNotifier(),
);

/// True while the app is on screen. Alerts then show in the app itself instead of as notifications.
final appInForegroundProvider = Provider<bool Function()>(
  (ref) => () {
    final state = WidgetsBinding.instance.lifecycleState;
    return state == null || state == AppLifecycleState.resumed;
  },
);

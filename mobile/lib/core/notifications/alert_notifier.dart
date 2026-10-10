import 'dart:typed_data';

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
    bool alarm = false,
  });

  /// Takes a notification away, which also silences an alarm that is still sounding.
  Future<void> cancel(int id);

  /// False when the user (or the system) has switched this app's notifications off.
  Future<bool> areEnabled();
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

  /// Rings like an alarm clock: on the alarm volume, so it is heard with the phone on silent.
  static const _alarmChannel = AndroidNotificationChannel(
    'safewalk_alarm',
    'Safety alarm',
    description:
        'Rings loudly when you may be in danger: an emergency, or no movement.',
    importance: Importance.max,
    sound: UriAndroidNotificationSound('content://settings/system/alarm_alert'),
    audioAttributesUsage: AudioAttributesUsage.alarm,
  );

  Future<void> _init() async {
    if (_ready) return;
    await _plugin.initialize(
      settings: const InitializationSettings(
        android: AndroidInitializationSettings('ic_stat_safewalk'),
      ),
    );
    await _plugin
        .resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin
        >()
        ?.createNotificationChannel(_channel);
    await _plugin
        .resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin
        >()
        ?.createNotificationChannel(_alarmChannel);
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
  Future<bool> areEnabled() async {
    try {
      await _init();
      final enabled = await _plugin
          .resolvePlatformSpecificImplementation<
            AndroidFlutterLocalNotificationsPlugin
          >()
          ?.areNotificationsEnabled();
      return enabled ?? true;
    } on Object {
      return true;
    }
  }

  @override
  Future<void> show({
    required int id,
    required String title,
    required String body,
    bool alarm = false,
  }) async {
    try {
      await _show(id, title, body, alarm);
    } on Object catch (error) {
      debugPrint('Could not show notification: $error');
    }
  }

  @override
  Future<void> cancel(int id) async {
    try {
      await _init();
      await _plugin.cancel(id: id);
    } on Object catch (error) {
      debugPrint('Could not cancel notification: $error');
    }
  }

  Future<void> _show(int id, String title, String body, bool alarm) async {
    await _init();
    final channel = alarm ? _alarmChannel : _channel;
    await _plugin.show(
      id: id,
      title: title,
      body: body,
      notificationDetails: NotificationDetails(
        android: AndroidNotificationDetails(
          channel.id,
          channel.name,
          channelDescription: channel.description,
          importance: Importance.max,
          priority: Priority.high,
          category: AndroidNotificationCategory.alarm,
          visibility: NotificationVisibility.public,
          enableVibration: true,
          playSound: true,
          sound: alarm ? channel.sound : null,
          audioAttributesUsage: alarm
              ? AudioAttributesUsage.alarm
              : AudioAttributesUsage.notification,
          vibrationPattern: alarm
              ? Int64List.fromList([0, 800, 400, 800, 400, 800])
              : null,
          // insistent: the sound repeats until the notification is opened or dismissed
          additionalFlags: alarm ? Int32List.fromList([4]) : null,
          fullScreenIntent: alarm,
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

/// Whether this app may show notifications right now. Checked when the walk screen opens and when the app returns.
final notificationsEnabledProvider = FutureProvider.autoDispose<bool>(
  (ref) => ref.watch(alertNotifierProvider).areEnabled(),
);

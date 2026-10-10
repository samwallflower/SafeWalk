import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:url_launcher/url_launcher.dart';

import '../domain/dial_uri.dart';

/// Opens the phone's dialer with a number filled in. It never places the call itself.
abstract class Dialer {
  Future<bool> dial(String number);
}

class PhoneDialer implements Dialer {
  const PhoneDialer();

  @override
  Future<bool> dial(String number) async {
    final uri = dialUri(number);
    if (uri == null) return false;
    try {
      return await launchUrl(uri);
    } on Object {
      return false;
    }
  }
}

final dialerProvider = Provider<Dialer>((ref) => const PhoneDialer());

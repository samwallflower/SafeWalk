import 'dart:convert';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../domain/authority_numbers.dart';

/// The last numbers that were looked up, kept on the phone so they are still there without a connection.
abstract class AuthorityCache {
  Future<void> save(AuthorityNumbers numbers);
  Future<AuthorityNumbers?> readLast();
}

class PrefsAuthorityCache implements AuthorityCache {
  static const _key = 'authority_numbers_last';

  @override
  Future<void> save(AuthorityNumbers numbers) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_key, jsonEncode(numbers.toJson()));
  }

  @override
  Future<AuthorityNumbers?> readLast() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final raw = prefs.getString(_key);
      if (raw == null) return null;
      final json = jsonDecode(raw);
      return json is Map<String, Object?>
          ? AuthorityNumbers.fromJson(json).withSource(NumbersSource.saved)
          : null;
    } on FormatException {
      return null;
    }
  }
}

final authorityCacheProvider = Provider<AuthorityCache>(
  (ref) => PrefsAuthorityCache(),
);

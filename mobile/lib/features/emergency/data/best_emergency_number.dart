import '../domain/authority_numbers.dart';
import 'authority_cache.dart';

/// The number to offer when SOS could not be sent: the one saved from the last lookup, otherwise 112.
Future<String> bestEmergencyNumber(AuthorityCache cache) async =>
    (await cache.readLast())?.general ?? AuthorityNumbers.fallbackNumber;

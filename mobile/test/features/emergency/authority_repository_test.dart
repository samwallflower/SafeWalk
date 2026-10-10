import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/network/api_client.dart';
import 'package:safewalk_mobile/features/emergency/data/authority_cache.dart';
import 'package:safewalk_mobile/features/emergency/data/authority_repository.dart';
import 'package:safewalk_mobile/features/emergency/data/best_emergency_number.dart';
import 'package:safewalk_mobile/features/emergency/domain/authority_numbers.dart';

class _Adapter implements HttpClientAdapter {
  _Adapter({this.status = 200, this.body, this.offline = false});

  final int status;
  final Object? body;
  final bool offline;

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    if (offline) {
      throw DioException.connectionError(
        requestOptions: options,
        reason: 'offline',
      );
    }
    return ResponseBody.fromString(
      jsonEncode(body),
      status,
      headers: {
        Headers.contentTypeHeader: ['application/json'],
      },
    );
  }

  @override
  void close({bool force = false}) {}
}

class _MemoryCache implements AuthorityCache {
  AuthorityNumbers? stored;

  @override
  Future<AuthorityNumbers?> readLast() async =>
      stored?.withSource(NumbersSource.saved);

  @override
  Future<void> save(AuthorityNumbers numbers) async => stored = numbers;
}

ApiClient _client(_Adapter adapter) {
  final dio = Dio(BaseOptions(baseUrl: 'http://test.invalid/api/v1'));
  dio.httpClientAdapter = adapter;
  return ApiClient(dio);
}

const _hungary = {
  'message': 'ok',
  'data': {
    'policeNumber': '107',
    'ambulanceNumber': '104',
    'generalEmergencyNumber': '112',
    'countryName': 'Hungary',
    'countryCode': 'HU',
  },
};

void main() {
  test('live numbers are used and saved for later', () async {
    final cache = _MemoryCache();
    final repo = AuthorityRepository(_client(_Adapter(body: _hungary)), cache);

    final numbers = await repo.forLocation(47.49, 19.07);

    expect(numbers.source, NumbersSource.live);
    expect(numbers.police, '107');
    expect(cache.stored?.countryCode, 'HU');
  });

  test('offline, the saved numbers are used', () async {
    final cache = _MemoryCache()
      ..stored = AuthorityNumbers.fromJson(
        (_hungary['data']! as Map<String, Object?>),
      );
    final repo = AuthorityRepository(_client(_Adapter(offline: true)), cache);

    final numbers = await repo.forLocation(47.49, 19.07);

    expect(numbers.source, NumbersSource.saved);
    expect(numbers.ambulance, '104');
  });

  test('a server error also falls back to the saved numbers', () async {
    final cache = _MemoryCache()
      ..stored = AuthorityNumbers.fromJson(
        (_hungary['data']! as Map<String, Object?>),
      );
    final repo = AuthorityRepository(
      _client(_Adapter(status: 500, body: {'message': 'boom', 'data': null})),
      cache,
    );
    expect((await repo.forLocation(1, 1)).source, NumbersSource.saved);
  });

  test('with nothing saved and no connection it is 112', () async {
    final repo = AuthorityRepository(
      _client(_Adapter(offline: true)),
      _MemoryCache(),
    );

    final numbers = await repo.forLocation(47.49, 19.07);

    expect(numbers.source, NumbersSource.fallback);
    expect(numbers.general, '112');
    expect(numbers.police, isNull);
  });

  test('a place with no authority on file (404) is 112 too', () async {
    final repo = AuthorityRepository(
      _client(
        _Adapter(
          status: 404,
          body: {'message': 'No authority found', 'data': null},
        ),
      ),
      _MemoryCache(),
    );
    expect((await repo.forLocation(0, 0)).general, '112');
  });

  test(
    'when SOS fails the offered number is the saved local one, else 112',
    () async {
      final cache = _MemoryCache();
      expect(await bestEmergencyNumber(cache), '112');
      cache.stored = const AuthorityNumbers(general: '999');
      expect(await bestEmergencyNumber(cache), '999');
    },
  );
}

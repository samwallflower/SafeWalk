import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../../../core/network/providers.dart';
import '../domain/emergency_contact.dart';

class ContactsApi {
  ContactsApi(this._client);

  final ApiClient _client;

  String _base(int userId) => '/emergency-contacts/$userId';

  Future<List<EmergencyContact>> list(int userId) async {
    final list = await _client.getList('${_base(userId)}/contacts');
    return list.map(EmergencyContact.fromJson).toList();
  }

  Future<void> add(int userId, ContactInput input) =>
      _client.postObject('${_base(userId)}/add', body: input.toJson());

  Future<void> update(int userId, int contactId, ContactInput input) =>
      _client.putObject(
        '${_base(userId)}/contacts/$contactId/update',
        body: input.toJson(),
      );

  Future<void> remove(int userId, int contactId) =>
      _client.delete('${_base(userId)}/contacts/$contactId/delete');
}

final contactsApiProvider = Provider<ContactsApi>(
  (ref) => ContactsApi(ref.watch(apiClientProvider)),
);

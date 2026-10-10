import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../auth/presentation/state/session_controller.dart';
import '../../data/contacts_api.dart';
import '../../domain/emergency_contact.dart';

final contactsProvider =
    AsyncNotifierProvider<ContactsController, List<EmergencyContact>>(
      ContactsController.new,
    );

/// The signed-in person's emergency contacts. Changes refresh the list from the server, so what is shown is what is
/// saved. A refused change throws an `ApiException` for the form to show.
class ContactsController extends AsyncNotifier<List<EmergencyContact>> {
  /// Waits for the session if it is still loading, so a change is never dropped.
  Future<int?> _currentUserId() async =>
      (await ref.read(sessionProvider.future))?.id;

  @override
  Future<List<EmergencyContact>> build() async {
    final userId = ref.watch(sessionProvider.select((s) => s.value?.id));
    if (userId == null) return const [];
    return ref.read(contactsApiProvider).list(userId);
  }

  Future<void> _reload(int userId) async {
    state = AsyncData(await ref.read(contactsApiProvider).list(userId));
  }

  Future<void> add(ContactInput input) async {
    final userId = await _currentUserId();
    if (userId == null) return;
    await ref.read(contactsApiProvider).add(userId, input);
    await _reload(userId);
  }

  Future<void> edit(int contactId, ContactInput input) async {
    final userId = await _currentUserId();
    if (userId == null) return;
    await ref.read(contactsApiProvider).update(userId, contactId, input);
    await _reload(userId);
  }

  Future<void> remove(int contactId) async {
    final userId = await _currentUserId();
    if (userId == null) return;
    await ref.read(contactsApiProvider).remove(userId, contactId);
    await _reload(userId);
  }
}

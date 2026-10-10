import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/network/api_exception.dart';
import 'package:safewalk_mobile/core/theme/theme.dart';
import 'package:safewalk_mobile/features/auth/domain/session_user.dart';
import 'package:safewalk_mobile/features/auth/presentation/state/session_controller.dart';
import 'package:safewalk_mobile/features/contacts/data/contacts_api.dart';
import 'package:safewalk_mobile/features/contacts/domain/emergency_contact.dart';
import 'package:safewalk_mobile/features/contacts/presentation/contact_form_sheet.dart';
import 'package:safewalk_mobile/features/contacts/presentation/contacts_section.dart';
import 'package:safewalk_mobile/features/contacts/presentation/state/contacts_controller.dart';
import 'package:safewalk_mobile/features/emergency/data/dialer.dart';

class _FakeSession extends SessionController {
  @override
  Future<SessionUser?> build() async =>
      const SessionUser(id: 7, email: 'a@b.c', roles: ['ROLE_USER']);
}

class _FakeContactsApi implements ContactsApi {
  _FakeContactsApi(List<EmergencyContact> start) : contacts = [...start];

  final List<EmergencyContact> contacts;
  ApiException? addError;
  final calls = <String>[];

  @override
  Future<List<EmergencyContact>> list(int userId) async => [...contacts];

  @override
  Future<void> add(int userId, ContactInput input) async {
    calls.add('add ${input.name}');
    if (addError != null) throw addError!;
    contacts.add(
      EmergencyContact(
        id: contacts.length + 1,
        name: input.name,
        email: input.email,
        phone: input.phone,
      ),
    );
  }

  @override
  Future<void> update(int userId, int contactId, ContactInput input) async {
    calls.add('update $contactId ${input.name}');
    final i = contacts.indexWhere((c) => c.id == contactId);
    contacts[i] = EmergencyContact(
      id: contactId,
      name: input.name,
      email: input.email,
      phone: input.phone,
    );
  }

  @override
  Future<void> remove(int userId, int contactId) async {
    calls.add('remove $contactId');
    contacts.removeWhere((c) => c.id == contactId);
  }
}

class _FakeDialer implements Dialer {
  final dialed = <String>[];

  @override
  Future<bool> dial(String number) async {
    dialed.add(number);
    return true;
  }
}

EmergencyContact _c(int id, String name, {String? phone}) =>
    EmergencyContact(id: id, name: name, email: '$name@x.com', phone: phone);

Future<void> _pump(
  WidgetTester tester,
  Widget body, {
  required _FakeContactsApi api,
  _FakeDialer? dialer,
}) async {
  await tester.binding.setSurfaceSize(const Size(800, 2000));
  addTearDown(() => tester.binding.setSurfaceSize(null));
  await tester.pumpWidget(
    ProviderScope(
      overrides: [
        sessionProvider.overrideWith(_FakeSession.new),
        contactsApiProvider.overrideWithValue(api),
        dialerProvider.overrideWithValue(dialer ?? _FakeDialer()),
      ],
      child: MaterialApp(
        theme: buildTheme(downloadFont: false),
        home: Scaffold(body: SingleChildScrollView(child: body)),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

void main() {
  group('ContactsSection', () {
    testWidgets('lists contacts with how many of five are used', (
      tester,
    ) async {
      await _pump(
        tester,
        const ContactsSection(),
        api: _FakeContactsApi([_c(1, 'Mum', phone: '+3630'), _c(2, 'Dad')]),
      );
      expect(find.text('Mum'), findsOneWidget);
      expect(find.text('Dad'), findsOneWidget);
      expect(find.text('2 of 5'), findsOneWidget);
      expect(find.text('Add emergency contact'), findsOneWidget);
    });

    testWidgets('an empty list invites you to add someone', (tester) async {
      await _pump(tester, const ContactsSection(), api: _FakeContactsApi([]));
      expect(find.text('No emergency contacts yet'), findsOneWidget);
    });

    testWidgets('at five contacts adding is switched off and says why', (
      tester,
    ) async {
      await _pump(
        tester,
        const ContactsSection(),
        api: _FakeContactsApi([for (var i = 1; i <= 5; i++) _c(i, 'C$i')]),
      );
      expect(find.text('Contact limit reached'), findsOneWidget);
      final button = tester.widget<TextButton>(
        find.widgetWithText(TextButton, 'Contact limit reached'),
      );
      expect(button.onPressed, isNull);
    });

    testWidgets('the call button opens the dialer for contacts with a phone', (
      tester,
    ) async {
      final dialer = _FakeDialer();
      await _pump(
        tester,
        const ContactsSection(),
        api: _FakeContactsApi([_c(1, 'Mum', phone: '+3630'), _c(2, 'Dad')]),
        dialer: dialer,
      );
      expect(
        find.byTooltip('Call Dad'),
        findsNothing,
        reason: 'no phone, no call button',
      );
      await tester.tap(find.byTooltip('Call Mum'));
      await tester.pump();
      expect(dialer.dialed, ['+3630']);
    });

    testWidgets('removing asks first, then deletes', (tester) async {
      final api = _FakeContactsApi([_c(1, 'Mum')]);
      await _pump(tester, const ContactsSection(), api: api);

      await tester.tap(find.byTooltip('More'));
      await tester.pumpAndSettle();
      await tester.tap(find.text('Remove'));
      await tester.pumpAndSettle();
      expect(find.text('Remove Mum?'), findsOneWidget);
      expect(api.calls, isEmpty);

      await tester.tap(find.widgetWithText(FilledButton, 'Remove'));
      await tester.pumpAndSettle();
      expect(api.calls, ['remove 1']);
      expect(find.text('Mum'), findsNothing);
    });
  });

  group('ContactFormSheet', () {
    Future<void> openForm(
      WidgetTester tester,
      _FakeContactsApi api, {
      EmergencyContact? contact,
    }) async {
      await _pump(
        tester,
        Builder(
          builder: (context) => TextButton(
            onPressed: () => showContactFormSheet(context, contact: contact),
            child: const Text('open'),
          ),
        ),
        api: api,
      );
      await tester.tap(find.text('open'));
      await tester.pumpAndSettle();
    }

    testWidgets('checks the fields before calling the server', (tester) async {
      final api = _FakeContactsApi([]);
      await openForm(tester, api);

      await tester.tap(find.text('Add contact'));
      await tester.pump();

      expect(find.text('Contact name is required'), findsOneWidget);
      expect(find.text('Contact email is required'), findsOneWidget);
      expect(api.calls, isEmpty);
    });

    testWidgets('rejects a phone that is not in international format', (
      tester,
    ) async {
      final api = _FakeContactsApi([]);
      await openForm(tester, api);
      await tester.enterText(find.widgetWithText(TextFormField, 'Name'), 'Mum');
      await tester.enterText(
        find.widgetWithText(TextFormField, 'Email'),
        'm@x.com',
      );
      await tester.enterText(
        find.widgetWithText(TextFormField, 'Phone (optional)'),
        '0630123',
      );

      await tester.tap(find.text('Add contact'));
      await tester.pump();

      expect(find.textContaining('international format'), findsOneWidget);
      expect(api.calls, isEmpty);
    });

    testWidgets('saves a valid contact and closes', (tester) async {
      final api = _FakeContactsApi([]);
      await openForm(tester, api);
      await tester.enterText(find.widgetWithText(TextFormField, 'Name'), 'Mum');
      await tester.enterText(
        find.widgetWithText(TextFormField, 'Email'),
        'm@x.com',
      );

      await tester.tap(find.text('Add contact'));
      await tester.pumpAndSettle();

      expect(api.calls, ['add Mum']);
      expect(
        find.text('Add an emergency contact'),
        findsNothing,
        reason: 'the sheet closed',
      );
    });

    testWidgets('shows the server message when it refuses', (tester) async {
      final api = _FakeContactsApi([])
        ..addError = const ApiException(
          400,
          'You can only have 5 emergency contacts',
        );
      await openForm(tester, api);
      await tester.enterText(find.widgetWithText(TextFormField, 'Name'), 'Mum');
      await tester.enterText(
        find.widgetWithText(TextFormField, 'Email'),
        'm@x.com',
      );

      await tester.tap(find.text('Add contact'));
      await tester.pumpAndSettle();

      expect(
        find.text('You can only have 5 emergency contacts'),
        findsOneWidget,
      );
      expect(
        find.text('Add an emergency contact'),
        findsOneWidget,
        reason: 'the sheet stays open to fix it',
      );
    });

    testWidgets('editing is prefilled and saves changes', (tester) async {
      final api = _FakeContactsApi([_c(4, 'Mum', phone: '+36301234567')]);
      await openForm(tester, api, contact: _c(4, 'Mum', phone: '+36301234567'));
      expect(find.text('Edit contact'), findsOneWidget);
      expect(find.text('Mum'), findsWidgets);

      await tester.enterText(
        find.widgetWithText(TextFormField, 'Name'),
        'Mother',
      );
      await tester.tap(find.text('Save changes'));
      await tester.pumpAndSettle();

      expect(api.calls, ['update 4 Mother']);
    });
  });

  test('the controller reloads the list after each change', () async {
    final api = _FakeContactsApi([_c(1, 'Mum')]);
    final container = ProviderContainer(
      overrides: [
        sessionProvider.overrideWith(_FakeSession.new),
        contactsApiProvider.overrideWithValue(api),
      ],
    );
    addTearDown(container.dispose);
    await container.read(sessionProvider.future);
    expect((await container.read(contactsProvider.future)).map((c) => c.name), [
      'Mum',
    ]);

    await container
        .read(contactsProvider.notifier)
        .add(const ContactInput(name: 'Dad', email: 'd@x.com'));
    expect(container.read(contactsProvider).value!.map((c) => c.name), [
      'Mum',
      'Dad',
    ]);

    await container.read(contactsProvider.notifier).remove(1);
    expect(container.read(contactsProvider).value!.map((c) => c.name), ['Dad']);
  });
}

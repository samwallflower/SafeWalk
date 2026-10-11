import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/format/format.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/inline_notice.dart';
import '../../../places/data/geocode_api.dart';
import '../../../walk_session/presentation/state/walk_controller.dart';
import '../../data/authority_repository.dart';
import '../../data/dialer.dart';
import '../../domain/authority_numbers.dart';
import '../../domain/emergency.dart';
import '../state/emergency_controller.dart';
import 'call_button.dart';

/// Shown over the walk while an emergency is active: what was sent, who knows, how to call for help,
/// and "I'm safe" to end the alert.
class EmergencyOverlay extends ConsumerWidget {
  const EmergencyOverlay({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(emergencyProvider);
    final position = ref.watch(walkProvider.select((s) => s.position));
    final emergency = state.emergency;
    final theme = Theme.of(context);

    final lat = emergency?.latitude ?? position?.latitude;
    final lng = emergency?.longitude ?? position?.longitude;

    return Material(
      color: theme.scaffoldBackgroundColor,
      child: SafeArea(
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 20, 20, 32),
          children: [
            Row(
              children: [
                Container(
                  width: 56,
                  height: 56,
                  decoration: BoxDecoration(
                    color: AppColors.destructiveSoft,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: const Icon(
                    Icons.warning_amber_rounded,
                    size: 30,
                    color: AppColors.destructive,
                  ),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Alert sent',
                        style: theme.textTheme.headlineMedium?.copyWith(
                          fontWeight: FontWeight.w800,
                          color: AppColors.destructive,
                          height: 1.1,
                        ),
                      ),
                      Text(
                        emergency?.source.description ??
                            'An emergency alert is active for your walk.',
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: AppColors.mutedForeground,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),
            if (state.error != null) ...[
              InlineNotice(state.error!),
              const SizedBox(height: 8),
              if (emergency == null && !state.loading)
                Align(
                  alignment: Alignment.centerLeft,
                  child: TextButton(
                    onPressed: () =>
                        ref.read(emergencyProvider.notifier).load(),
                    child: const Text('Try again'),
                  ),
                ),
              const SizedBox(height: 8),
            ],
            if (state.loading && emergency == null)
              const Padding(
                padding: EdgeInsets.symmetric(vertical: 16),
                child: Center(child: CircularProgressIndicator()),
              ),
            _ContactsCard(emergency: emergency, loading: state.loading),
            const SizedBox(height: 12),
            _LocationCard(
              latitude: lat,
              longitude: lng,
              triggeredAt: emergency?.triggeredAt,
            ),
            const SizedBox(height: 24),
            Text(
              'Call for help',
              style: theme.textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.w800,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              'This opens your phone app with the number ready. You press call.',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 12),
            _CallSection(latitude: lat, longitude: lng),
            const SizedBox(height: 24),
            _SafeButton(resolving: state.resolving),
          ],
        ),
      ),
    );
  }
}

class _ContactsCard extends StatelessWidget {
  const _ContactsCard({required this.emergency, required this.loading});

  final Emergency? emergency;
  final bool loading;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final contacts = emergency?.contacts ?? const <NotifiedContact>[];
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Who was told',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w800,
              ),
            ),
            const SizedBox(height: 8),
            if (emergency == null)
              Text(
                loading ? 'Loading...' : 'No details available.',
                style: theme.textTheme.bodyMedium,
              )
            else if (contacts.isEmpty)
              Text(
                'No emergency contacts are saved, so nobody was notified. Add contacts in the SafeWalk web app, and call for help below.',
                style: theme.textTheme.bodyMedium,
              )
            else
              for (final contact in contacts)
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 4),
                  child: Row(
                    children: [
                      const Icon(
                        Icons.check_circle_outline,
                        size: 20,
                        color: AppColors.success,
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Text(
                          contact.name,
                          style: theme.textTheme.bodyLarge?.copyWith(
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                      if (contact.phone != null && contact.phone!.isNotEmpty)
                        Text(
                          contact.phone!,
                          style: theme.textTheme.bodyMedium?.copyWith(
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
                        ),
                    ],
                  ),
                ),
          ],
        ),
      ),
    );
  }
}

class _LocationCard extends ConsumerWidget {
  const _LocationCard({
    required this.latitude,
    required this.longitude,
    required this.triggeredAt,
  });

  final double? latitude;
  final double? longitude;
  final String? triggeredAt;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    String where = 'Location not available yet';
    if (latitude != null && longitude != null) {
      final street = ref.watch(
        streetNameProvider((
          lat: double.parse(latitude!.toStringAsFixed(5)),
          lng: double.parse(longitude!.toStringAsFixed(5)),
        )),
      );
      where =
          street.value ??
          '${latitude!.toStringAsFixed(4)}, ${longitude!.toStringAsFixed(4)}';
    }
    final when = triggeredAt == null || triggeredAt!.isEmpty
        ? ''
        : formatRelative(triggeredAt!);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            const Icon(
              Icons.place_outlined,
              size: 26,
              color: AppColors.destructive,
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Your location was shared',
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  Text(
                    when.isEmpty ? where : '$where, $when',
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _CallSection extends ConsumerWidget {
  const _CallSection({required this.latitude, required this.longitude});

  final double? latitude;
  final double? longitude;

  Future<void> _call(BuildContext context, WidgetRef ref, String number) async {
    final opened = await ref.read(dialerProvider).dial(number);
    if (!opened && context.mounted) {
      ScaffoldMessenger.of(context)
        ..hideCurrentSnackBar()
        ..showSnackBar(
          SnackBar(
            content: Text(
              "Couldn't open the phone app. Dial $number yourself.",
            ),
          ),
        );
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final AsyncValue<AuthorityNumbers> numbers =
        (latitude == null || longitude == null)
        ? const AsyncData(AuthorityNumbers.fallback)
        : ref.watch(
            authorityNumbersProvider((
              lat: double.parse(latitude!.toStringAsFixed(2)),
              lng: double.parse(longitude!.toStringAsFixed(2)),
            )),
          );

    // While the lookup runs, 112 is already there: the screen must never wait to offer a number.
    final data = numbers.value ?? AuthorityNumbers.fallback;
    final note = switch (data.source) {
      NumbersSource.live =>
        data.countryName == null ? null : 'Numbers for ${data.countryName}.',
      NumbersSource.saved => 'Saved numbers from your last lookup. Check that they match where you are.',
      NumbersSource.fallback =>
        numbers.isLoading
            ? 'Looking up local numbers...'
            : 'Default emergency number. Local numbers could not be loaded.',
    };

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (data.police != null) ...[
          CallButton(
            label: 'Police',
            number: data.police!,
            onPressed: () => _call(context, ref, data.police!),
          ),
          const SizedBox(height: 10),
        ],
        if (data.ambulance != null) ...[
          CallButton(
            label: 'Ambulance',
            number: data.ambulance!,
            onPressed: () => _call(context, ref, data.ambulance!),
          ),
          const SizedBox(height: 10),
        ],
        CallButton(
          label: 'Emergency services',
          number: data.general,
          emphasis: true,
          onPressed: () => _call(context, ref, data.general),
        ),
        if (note != null) ...[
          const SizedBox(height: 8),
          Text(
            note,
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ],
      ],
    );
  }
}

class _SafeButton extends ConsumerWidget {
  const _SafeButton({required this.resolving});

  final bool resolving;

  Future<void> _confirm(BuildContext context, WidgetRef ref) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Are you safe?"),
        content: const Text(
          'This ends the emergency alert and puts your walk back to normal.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Not yet'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text("Yes, I'm safe"),
          ),
        ],
      ),
    );
    if (ok == true) await ref.read(emergencyProvider.notifier).resolve();
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // same height, corners and type size as the call buttons above, so the screen reads as one set
    return SizedBox(
      height: 64,
      child: OutlinedButton.icon(
        style: OutlinedButton.styleFrom(
          side: const BorderSide(color: AppColors.primary, width: 2),
        ),
        onPressed: resolving ? null : () => _confirm(context, ref),
        icon: resolving
            ? const SizedBox(
                width: 24,
                height: 24,
                child: CircularProgressIndicator(
                  strokeWidth: 3,
                  color: AppColors.primary,
                ),
              )
            : const Icon(Icons.verified_user_outlined, size: 26),
        label: Text(
          resolving ? 'Updating...' : "I'm safe",
          style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w700),
        ),
      ),
    );
  }
}

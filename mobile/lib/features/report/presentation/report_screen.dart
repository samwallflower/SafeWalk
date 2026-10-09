import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:latlong2/latlong.dart';

import '../../../core/config/env.dart';
import '../../../core/location/location_service.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/routing/app_routes.dart';
import '../../../core/theme/colors.dart';
import '../../../core/widgets/error_state.dart';
import '../../../core/widgets/inline_notice.dart';
import '../../auth/presentation/state/session_controller.dart';
import '../../categories/data/categories_provider.dart';
import '../../map/domain/map_config.dart';
import '../../map/presentation/state/map_data_provider.dart';
import '../../map/presentation/state/map_focus.dart';
import '../../map/presentation/state/map_view_controller.dart';
import '../../places/data/geocode_api.dart';
import '../data/report_api.dart';
import '../domain/report_rules.dart';
import 'widgets/category_picker.dart';
import 'widgets/location_pin_picker.dart';
import 'widgets/report_section.dart';

class ReportScreen extends ConsumerStatefulWidget {
  const ReportScreen({super.key});

  @override
  ConsumerState<ReportScreen> createState() => _ReportScreenState();
}

class _ReportScreenState extends ConsumerState<ReportScreen> {
  final _formKey = GlobalKey<FormState>();
  final _description = TextEditingController();
  final _map = MapController();

  late LatLng _pin = ref.read(mapViewProvider).center ?? defaultMapCenter;
  late LatLng _streetPoint = _pin;
  Timer? _streetTimer;

  int? _categoryId;
  String? _categoryError;
  bool _anonymous = true;
  bool _scrollLocked = false;
  bool _locating = false;
  bool _submitting = false;
  String? _error;

  @override
  void dispose() {
    _streetTimer?.cancel();
    _description.dispose();
    super.dispose();
  }

  void _onPinMoved(LatLng point) {
    _pin = point;
    // Look the street up only once the map settles, not on every frame of a drag.
    _streetTimer?.cancel();
    _streetTimer = Timer(const Duration(milliseconds: 600), () {
      if (mounted) setState(() => _streetPoint = point);
    });
  }

  Future<void> _useMyLocation() async {
    if (_locating) return;
    setState(() => _locating = true);
    final result = await const LocationService().current();
    if (!mounted) return;
    setState(() => _locating = false);
    final point = result.point;
    if (point != null) {
      _map.move(point, 17);
      return;
    }
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(
        SnackBar(
          content: Text(switch (result.failure) {
            LocationFailure.serviceDisabled =>
              'Turn on location services to use your position.',
            LocationFailure.denied =>
              'Location permission is needed to use your position.',
            LocationFailure.deniedForever =>
              'Location is blocked. Allow it in the app settings.',
            _ => "Couldn't get your location. Drag the map to place the pin instead.",
          }),
        ),
      );
  }

  Future<void> _submit() async {
    if (_submitting) return;
    final formOk = _formKey.currentState?.validate() ?? false;
    final categoryOk = _categoryId != null;
    setState(() => _categoryError = categoryOk ? null : 'Select a category');
    if (!formOk || !categoryOk) return;

    final user = ref.read(sessionProvider).value;
    final categories = ref.read(categoriesProvider).value;
    final category = categories?.where((c) => c.id == _categoryId).firstOrNull;
    if (user == null || category == null) {
      setState(() => _error = 'Please sign in again and pick a category.');
      return;
    }
    final latError =
        ReportRules.latitude(_pin.latitude) ??
        ReportRules.longitude(_pin.longitude);
    if (latError != null) {
      setState(() => _error = latError);
      return;
    }

    setState(() {
      _submitting = true;
      _error = null;
    });
    try {
      await ref
          .read(reportApiProvider)
          .create(
            user.id,
            NewReport(
              description: _description.text.trim(),
              latitude: _pin.latitude,
              longitude: _pin.longitude,
              isAnonymous: _anonymous,
              category: category,
            ),
          );
      if (!mounted) return;
      ref.invalidate(heatPointsProvider);
      ref.invalidate(nearbyProvider);
      ref.read(mapFocusProvider.notifier).request(_pin);
      _description.clear();
      setState(() {
        _categoryId = null;
        _anonymous = true;
      });
      ScaffoldMessenger.of(context)
        ..hideCurrentSnackBar()
        ..showSnackBar(
          const SnackBar(content: Text('Incident published to the map')),
        );
      context.go(AppRoutes.home);
    } on ApiException catch (e) {
      if (mounted) setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (Env.mapboxToken.isEmpty) {
      return const Scaffold(
        body: ErrorState(
          message: 'Reporting needs a Mapbox token. Add MAPBOX_TOKEN to env/dev.json and rebuild.',
        ),
      );
    }
    final theme = Theme.of(context);
    final street = ref.watch(
      streetNameProvider((
        lat: double.parse(_streetPoint.latitude.toStringAsFixed(5)),
        lng: double.parse(_streetPoint.longitude.toStringAsFixed(5)),
      )),
    );
    final where =
        street.value ??
        '${_streetPoint.latitude.toStringAsFixed(4)}, ${_streetPoint.longitude.toStringAsFixed(4)}';

    return Scaffold(
      appBar: AppBar(title: const Text('Report an incident')),
      body: SafeArea(
        child: Form(
          key: _formKey,
          child: ListView(
            physics: _scrollLocked
                ? const NeverScrollableScrollPhysics()
                : null,
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
            children: [
              Text(
                'Help keep fellow pedestrians safe in real time.',
                style: theme.textTheme.bodyLarge?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: 20),
              ReportSection(
                title: 'Incident location',
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    LocationPinPicker(
                      controller: _map,
                      start: _pin,
                      onMoved: _onPinMoved,
                      onInteraction: (active) =>
                          setState(() => _scrollLocked = active),
                    ),
                    const SizedBox(height: 10),
                    Row(
                      children: [
                        const Icon(Icons.place_outlined, size: 18),
                        const SizedBox(width: 6),
                        Expanded(
                          child: Text(
                            where,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: theme.textTheme.bodyMedium?.copyWith(
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                        TextButton.icon(
                          onPressed: _useMyLocation,
                          icon: _locating
                              ? const SizedBox(
                                  width: 16,
                                  height: 16,
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                  ),
                                )
                              : const Icon(Icons.my_location, size: 18),
                          label: const Text('Use my location'),
                        ),
                      ],
                    ),
                    Text(
                      'Drag the map to place the pin exactly where it happened.',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              ReportSection(
                title: 'Hazard category',
                child: CategoryPicker(
                  selectedId: _categoryId,
                  errorText: _categoryError,
                  onChanged: (id) => setState(() {
                    _categoryId = id;
                    _categoryError = null;
                  }),
                ),
              ),
              const SizedBox(height: 24),
              ReportSection(
                title: 'Situation details',
                trailing: ValueListenableBuilder<TextEditingValue>(
                  valueListenable: _description,
                  builder: (context, value, _) => Text(
                    '${value.text.length} / $descriptionMax',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: value.text.length > descriptionMax
                          ? theme.colorScheme.error
                          : theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                ),
                child: TextFormField(
                  controller: _description,
                  validator: ReportRules.description,
                  minLines: 4,
                  maxLines: 6,
                  textCapitalization: TextCapitalization.sentences,
                  decoration: const InputDecoration(
                    hintText: 'What did you see? Be specific and factual.',
                  ),
                ),
              ),
              const SizedBox(height: 24),
              Card(
                child: SwitchListTile(
                  value: _anonymous,
                  onChanged: (value) => setState(() => _anonymous = value),
                  secondary: const CircleAvatar(
                    backgroundColor: AppColors.successSoft,
                    child: Icon(
                      Icons.visibility_off_outlined,
                      color: AppColors.success,
                    ),
                  ),
                  title: const Text(
                    'Post anonymously',
                    style: TextStyle(fontWeight: FontWeight.w700),
                  ),
                  subtitle: Text(
                    _anonymous
                        ? 'Your name is not shown on this report.'
                        : 'Your name is shown as first name and last initial.',
                  ),
                ),
              ),
              const SizedBox(height: 24),
              if (_error != null) ...[
                InlineNotice(_error!),
                const SizedBox(height: 16),
              ],
              FilledButton.icon(
                onPressed: _submitting ? null : _submit,
                icon: _submitting
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(strokeWidth: 2.5),
                      )
                    : const Icon(Icons.add_location_alt_outlined),
                label: Text(
                  _submitting ? 'Publishing...' : 'Publish to the map',
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

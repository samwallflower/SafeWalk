import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

import '../data/place_search_provider.dart';
import '../domain/place.dart';

/// A labelled place input with Mapbox suggestions listed right under it.
class PlaceField extends ConsumerStatefulWidget {
  const PlaceField({
    super.key,
    required this.label,
    required this.icon,
    required this.place,
    required this.onSelected,
    this.proximity,
    this.trailing,
  });

  final String label;
  final IconData icon;

  /// The chosen place. The field shows its label.
  final Place? place;
  final void Function(Place place) onSelected;
  final LatLng? proximity;
  final Widget? trailing;

  @override
  ConsumerState<PlaceField> createState() => _PlaceFieldState();
}

class _PlaceFieldState extends ConsumerState<PlaceField> {
  late final TextEditingController _controller = TextEditingController(
    text: widget.place?.label ?? '',
  );
  final _focus = FocusNode();
  Timer? _debounce;
  String _query = '';

  @override
  void didUpdateWidget(PlaceField oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Show a place chosen from outside the field (swap, "my location").
    final label = widget.place?.label ?? '';
    if (!_focus.hasFocus && label != _controller.text) _controller.text = label;
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _controller.dispose();
    _focus.dispose();
    super.dispose();
  }

  void _onChanged(String value) {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 300), () {
      if (mounted) setState(() => _query = value.trim());
    });
  }

  void _select(Place place) {
    _controller.text = place.label;
    _focus.unfocus();
    setState(() => _query = '');
    widget.onSelected(place);
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        TextField(
          controller: _controller,
          focusNode: _focus,
          onChanged: _onChanged,
          textInputAction: TextInputAction.search,
          decoration: InputDecoration(
            labelText: widget.label,
            prefixIcon: Icon(widget.icon),
            suffixIcon: widget.trailing,
          ),
        ),
        if (_query.length >= 2 && _focus.hasFocus)
          _Suggestions(
            query: _query,
            proximity: widget.proximity,
            onSelected: _select,
          ),
      ],
    );
  }
}

class _Suggestions extends ConsumerWidget {
  const _Suggestions({
    required this.query,
    required this.proximity,
    required this.onSelected,
  });

  final String query;
  final LatLng? proximity;
  final void Function(Place place) onSelected;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final results = ref.watch(
      placeSearchProvider((
        text: query,
        lat: proximity?.latitude,
        lng: proximity?.longitude,
      )),
    );
    Widget message(String text) =>
        Padding(padding: const EdgeInsets.all(14), child: Text(text));
    return Padding(
      padding: const EdgeInsets.only(top: 4),
      child: Material(
        color: Theme.of(context).colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(12),
        clipBehavior: Clip.antiAlias,
        child: results.when(
          loading: () => const Padding(
            padding: EdgeInsets.all(14),
            child: Center(
              child: SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(strokeWidth: 2.5),
              ),
            ),
          ),
          error: (error, _) => message('Search is unavailable right now.'),
          data: (places) => places.isEmpty
              ? message('No places found.')
              : Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    for (final place in places)
                      ListTile(
                        dense: true,
                        leading: const Icon(Icons.place_outlined, size: 20),
                        title: Text(
                          place.label,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                        onTap: () => onSelected(place),
                      ),
                  ],
                ),
        ),
      ),
    );
  }
}

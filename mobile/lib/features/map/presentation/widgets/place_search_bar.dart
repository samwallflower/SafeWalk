import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../places/data/place_search_provider.dart';
import '../../../places/domain/place.dart';
import '../state/map_view_controller.dart';

/// A search field with a drop-down of Mapbox results. Results lean toward where the map is looking.
class PlaceSearchBar extends ConsumerStatefulWidget {
  const PlaceSearchBar({
    super.key,
    required this.onSelected,
    required this.trailing,
  });

  final void Function(Place place) onSelected;
  final Widget trailing;

  @override
  ConsumerState<PlaceSearchBar> createState() => _PlaceSearchBarState();
}

class _PlaceSearchBarState extends ConsumerState<PlaceSearchBar> {
  final _controller = TextEditingController();
  final _focus = FocusNode();
  Timer? _debounce;
  String _query = '';

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

  void _clear() {
    _controller.clear();
    setState(() => _query = '');
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Material(
          elevation: 3,
          shadowColor: Colors.black26,
          color: theme.colorScheme.surface,
          borderRadius: BorderRadius.circular(16),
          child: Padding(
            padding: const EdgeInsets.only(left: 14, right: 6),
            child: Row(
              children: [
                Icon(Icons.search, color: theme.colorScheme.onSurfaceVariant),
                const SizedBox(width: 8),
                Expanded(
                  child: TextField(
                    controller: _controller,
                    focusNode: _focus,
                    onChanged: _onChanged,
                    textInputAction: TextInputAction.search,
                    decoration: InputDecoration(
                      hintText: 'Search a place or street',
                      border: InputBorder.none,
                      enabledBorder: InputBorder.none,
                      focusedBorder: InputBorder.none,
                      filled: false,
                      suffixIcon: _controller.text.isEmpty
                          ? null
                          : IconButton(
                              tooltip: 'Clear search',
                              icon: const Icon(Icons.close),
                              onPressed: _clear,
                            ),
                    ),
                  ),
                ),
                widget.trailing,
              ],
            ),
          ),
        ),
        if (_query.length >= 2) _Results(query: _query, onSelected: _select),
      ],
    );
  }
}

class _Results extends ConsumerWidget {
  const _Results({required this.query, required this.onSelected});

  final String query;
  final void Function(Place place) onSelected;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final center = ref.watch(mapViewProvider.select((s) => s.center));
    final results = ref.watch(
      placeSearchProvider((
        text: query,
        lat: center?.latitude,
        lng: center?.longitude,
      )),
    );
    final theme = Theme.of(context);

    Widget message(String text) =>
        Padding(padding: const EdgeInsets.all(16), child: Text(text));

    return Padding(
      padding: const EdgeInsets.only(top: 6),
      child: Material(
        elevation: 3,
        shadowColor: Colors.black26,
        color: theme.colorScheme.surface,
        borderRadius: BorderRadius.circular(16),
        clipBehavior: Clip.antiAlias,
        child: results.when(
          loading: () => const Padding(
            padding: EdgeInsets.all(16),
            child: Center(
              child: SizedBox(
                width: 22,
                height: 22,
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
                        leading: const Icon(Icons.place_outlined),
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

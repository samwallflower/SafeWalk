import 'package:flutter/material.dart';

import '../../../incidents/domain/incident.dart';
import 'incident_sheet.dart';

/// One incident card, or several you can step through when reports overlap on the map.
class IncidentPager extends StatefulWidget {
  const IncidentPager({
    super.key,
    required this.incidents,
    required this.onClose,
  });

  final List<Incident> incidents;
  final VoidCallback onClose;

  @override
  State<IncidentPager> createState() => _IncidentPagerState();
}

class _IncidentPagerState extends State<IncidentPager> {
  int _index = 0;

  @override
  void didUpdateWidget(IncidentPager oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (_index >= widget.incidents.length) _index = 0;
    if (!identical(oldWidget.incidents, widget.incidents) &&
        (oldWidget.incidents.isEmpty ||
            widget.incidents.isEmpty ||
            oldWidget.incidents.first.id != widget.incidents.first.id)) {
      _index = 0;
    }
  }

  @override
  Widget build(BuildContext context) {
    final incidents = widget.incidents;
    final theme = Theme.of(context);
    final incident = incidents[_index];
    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (incidents.length > 1)
          Padding(
            padding: const EdgeInsets.only(bottom: 6),
            child: Material(
              elevation: 2,
              shadowColor: Colors.black26,
              color: theme.colorScheme.surface,
              borderRadius: BorderRadius.circular(20),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  IconButton(
                    tooltip: 'Previous incident',
                    icon: const Icon(Icons.chevron_left),
                    onPressed: _index > 0
                        ? () => setState(() => _index--)
                        : null,
                  ),
                  Text(
                    '${_index + 1} of ${incidents.length} at this spot',
                    style: theme.textTheme.labelLarge,
                  ),
                  IconButton(
                    tooltip: 'Next incident',
                    icon: const Icon(Icons.chevron_right),
                    onPressed: _index < incidents.length - 1
                        ? () => setState(() => _index++)
                        : null,
                  ),
                ],
              ),
            ),
          ),
        IncidentSheet(
          key: ValueKey(incident.id),
          incident: incident,
          onClose: widget.onClose,
        ),
      ],
    );
  }
}

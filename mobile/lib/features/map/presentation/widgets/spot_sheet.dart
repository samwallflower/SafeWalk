import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/theme/colors.dart';
import '../../../../core/theme/theme.dart' show sheetRadius;
import '../../../places/data/geocode_api.dart';
import '../../domain/clustering.dart';
import 'incident_sheet.dart';

/// Everything reported at one spot. One report shows as the usual card; several show as a scrolling list,
/// newest first, each with its own votes.
class SpotSheet extends ConsumerWidget {
  const SpotSheet({super.key, required this.spot, required this.onClose});

  final Spot spot;
  final VoidCallback onClose;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    if (spot.incidents.length == 1) {
      return IncidentSheet(incident: spot.incidents.first, onClose: onClose);
    }
    final theme = Theme.of(context);
    final street = ref.watch(
      streetNameProvider((
        lat: double.parse(spot.position.latitude.toStringAsFixed(5)),
        lng: double.parse(spot.position.longitude.toStringAsFixed(5)),
      )),
    );
    final where =
        street.value ??
        '${spot.position.latitude.toStringAsFixed(4)}, ${spot.position.longitude.toStringAsFixed(4)}';
    final maxHeight = MediaQuery.sizeOf(context).height * 0.55;

    return Card(
      elevation: 6,
      shadowColor: Colors.black38,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(sheetRadius),
      ),
      child: ConstrainedBox(
        constraints: BoxConstraints(maxHeight: maxHeight),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 12, 8, 4),
              child: Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          '${spot.incidents.length} reports here',
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                        Text(
                          where,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: theme.textTheme.bodySmall?.copyWith(
                            color: AppColors.mutedForeground,
                          ),
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    tooltip: 'Close',
                    icon: const Icon(Icons.close),
                    onPressed: onClose,
                  ),
                ],
              ),
            ),
            const Divider(height: 1),
            Flexible(
              child: ListView.separated(
                shrinkWrap: true,
                padding: const EdgeInsets.fromLTRB(20, 14, 20, 16),
                itemCount: spot.incidents.length,
                separatorBuilder: (context, index) => const Padding(
                  padding: EdgeInsets.symmetric(vertical: 14),
                  child: Divider(height: 1),
                ),
                itemBuilder: (context, index) => IncidentDetails(
                  incident: spot.incidents[index],
                  showLocation: false,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

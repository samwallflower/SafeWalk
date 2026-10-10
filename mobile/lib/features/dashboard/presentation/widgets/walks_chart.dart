import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';

import '../../domain/dashboard_stats.dart';

/// Walks per day, as bars. Days with none are empty bars, so the rhythm of your walking is easy to see.
class WalksChart extends StatelessWidget {
  const WalksChart({super.key, required this.days});

  final List<DayCount> days;

  static const _weekdays = ['M', 'T', 'W', 'T', 'F', 'S', 'S'];

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final top = days.fold<int>(0, (m, d) => d.count > m ? d.count : m);
    final maxY = (top < 2 ? 2 : top + 1).toDouble();

    return Semantics(
      label:
          'Walks per day for the last ${days.length} days. ${days.fold<int>(0, (s, d) => s + d.count)} walks in total.',
      child: SizedBox(
        height: 140,
        child: BarChart(
          BarChartData(
            maxY: maxY,
            alignment: BarChartAlignment.spaceAround,
            gridData: const FlGridData(show: false),
            borderData: FlBorderData(show: false),
            barTouchData: BarTouchData(enabled: false),
            titlesData: FlTitlesData(
              leftTitles: const AxisTitles(),
              topTitles: const AxisTitles(),
              rightTitles: const AxisTitles(),
              bottomTitles: AxisTitles(
                sideTitles: SideTitles(
                  showTitles: true,
                  reservedSize: 22,
                  getTitlesWidget: (value, meta) {
                    final i = value.toInt();
                    if (i < 0 || i >= days.length) {
                      return const SizedBox.shrink();
                    }
                    return Padding(
                      padding: const EdgeInsets.only(top: 6),
                      child: Text(
                        _weekdays[days[i].day.weekday - 1],
                        style: theme.textTheme.labelSmall?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    );
                  },
                ),
              ),
            ),
            barGroups: [
              for (var i = 0; i < days.length; i++)
                BarChartGroupData(
                  x: i,
                  barRods: [
                    BarChartRodData(
                      toY: days[i].count == 0 ? 0.06 : days[i].count.toDouble(),
                      width: 24,
                      borderRadius: BorderRadius.circular(4),
                      color: days[i].count == 0
                          ? theme.colorScheme.outline
                          : theme.colorScheme.primary,
                    ),
                  ],
                ),
            ],
          ),
        ),
      ),
    );
  }
}

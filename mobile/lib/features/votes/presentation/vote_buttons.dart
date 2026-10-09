import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/colors.dart';
import '../../auth/presentation/state/session_controller.dart';
import '../data/votes_api.dart';
import '../domain/vote_counts.dart';
import '../domain/vote_type.dart';
import 'state/vote_counts_provider.dart';

/// Upvote / downvote with instant feedback: the count moves right away and rolls back if the server refuses.
class VoteButtons extends ConsumerStatefulWidget {
  const VoteButtons({
    super.key,
    required this.reportId,
    required this.upvotes,
    required this.downvotes,
  });

  final int reportId;
  final int upvotes;
  final int downvotes;

  @override
  ConsumerState<VoteButtons> createState() => _VoteButtonsState();
}

class _VoteButtonsState extends ConsumerState<VoteButtons> {
  VoteType? _mine;
  bool _loadingMine = true;
  bool _busy = false;

  @override
  void initState() {
    super.initState();
    _loadMine();
  }

  @override
  void didUpdateWidget(VoteButtons oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.reportId != widget.reportId) {
      setState(() {
        _mine = null;
        _loadingMine = true;
      });
      _loadMine();
    }
  }

  int? get _userId => ref.read(sessionProvider).value?.id;

  Future<void> _loadMine() async {
    final userId = _userId;
    final reportId = widget.reportId;
    if (userId == null) {
      // The session is still loading; build() retries as soon as it is ready.
      return;
    }
    try {
      final vote = await ref.read(votesApiProvider).mine(userId, reportId);
      if (mounted && reportId == widget.reportId) setState(() => _mine = vote);
    } on ApiException {
      // Unknown own vote: leave both buttons unselected; casting still works.
    } finally {
      if (mounted && reportId == widget.reportId) {
        setState(() => _loadingMine = false);
      }
    }
  }

  VoteCounts get _counts =>
      ref.read(voteCountsOverridesProvider)[widget.reportId] ??
      VoteCounts(upvotes: widget.upvotes, downvotes: widget.downvotes);

  Future<void> _vote(VoteType clicked) async {
    final userId = _userId;
    if (userId == null || _busy || _loadingMine) return;
    final reportId = widget.reportId;
    final api = ref.read(votesApiProvider);
    final overrides = ref.read(voteCountsOverridesProvider.notifier);

    final from = _mine;
    final to = nextVote(from, clicked);
    final before = _counts;

    setState(() {
      _busy = true;
      _mine = to;
    });
    overrides.set(reportId, applyVoteChange(before, from, to));
    try {
      if (to == null) {
        await api.remove(userId, reportId);
      } else if (from == null) {
        await api.cast(userId, reportId, to);
      } else {
        await api.update(userId, reportId, to);
      }
    } on ApiException catch (e) {
      overrides.set(reportId, before);
      if (mounted) {
        setState(() => _mine = from);
        ScaffoldMessenger.of(context)
          ..hideCurrentSnackBar()
          ..showSnackBar(SnackBar(content: Text(e.message)));
      }
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    ref.listen(sessionProvider.select((s) => s.value?.id), (previous, next) {
      if (next != null && previous != next) _loadMine();
    });
    final counts =
        ref.watch(voteCountsOverridesProvider)[widget.reportId] ??
        VoteCounts(upvotes: widget.upvotes, downvotes: widget.downvotes);
    final disabled = _busy || _loadingMine;
    return Row(
      children: [
        _VoteButton(
          label: 'Upvote',
          count: counts.upvotes,
          icon: Icons.thumb_up_outlined,
          selectedIcon: Icons.thumb_up,
          selected: _mine == VoteType.upvote,
          selectedBackground: AppColors.infoSoft,
          selectedForeground: AppColors.primary,
          onPressed: disabled ? null : () => _vote(VoteType.upvote),
        ),
        const SizedBox(width: 8),
        _VoteButton(
          label: 'Downvote',
          count: counts.downvotes,
          icon: Icons.thumb_down_outlined,
          selectedIcon: Icons.thumb_down,
          selected: _mine == VoteType.downvote,
          selectedBackground: AppColors.destructiveSoft,
          selectedForeground: AppColors.destructive,
          onPressed: disabled ? null : () => _vote(VoteType.downvote),
        ),
      ],
    );
  }
}

class _VoteButton extends StatelessWidget {
  const _VoteButton({
    required this.label,
    required this.count,
    required this.icon,
    required this.selectedIcon,
    required this.selected,
    required this.selectedBackground,
    required this.selectedForeground,
    required this.onPressed,
  });

  final String label;
  final int count;
  final IconData icon;
  final IconData selectedIcon;
  final bool selected;
  final Color selectedBackground;
  final Color selectedForeground;
  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) {
    final foreground = selected ? selectedForeground : AppColors.foreground;
    return Semantics(
      button: true,
      selected: selected,
      label: '$label, $count',
      child: Material(
        color: selected ? selectedBackground : AppColors.muted,
        borderRadius: BorderRadius.circular(12),
        child: InkWell(
          borderRadius: BorderRadius.circular(12),
          onTap: onPressed,
          child: ConstrainedBox(
            constraints: const BoxConstraints(minHeight: 44),
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 14),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(
                    selected ? selectedIcon : icon,
                    size: 18,
                    color: foreground,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    label,
                    style: TextStyle(
                      fontWeight: FontWeight.w600,
                      fontSize: 13,
                      color: foreground,
                    ),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    '$count',
                    style: TextStyle(
                      fontWeight: FontWeight.w800,
                      fontSize: 14,
                      color: foreground,
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_exception.dart';
import '../../../auth/presentation/state/session_controller.dart';
import '../../../walk_session/presentation/state/walk_controller.dart';
import '../../data/emergency_api.dart';
import '../../domain/emergency.dart';

class EmergencyState {
  const EmergencyState({
    this.emergency,
    this.loading = false,
    this.triggering = false,
    this.resolving = false,
    this.error,
  });

  /// The details of the active emergency. Null while loading, or if the server has none to show.
  final Emergency? emergency;
  final bool loading;
  final bool triggering;
  final bool resolving;
  final String? error;

  EmergencyState copyWith({
    Emergency? Function()? emergency,
    bool? loading,
    bool? triggering,
    bool? resolving,
    String? Function()? error,
  }) => EmergencyState(
    emergency: emergency != null ? emergency() : this.emergency,
    loading: loading ?? this.loading,
    triggering: triggering ?? this.triggering,
    resolving: resolving ?? this.resolving,
    error: error != null ? error() : this.error,
  );
}

final emergencyProvider = NotifierProvider<EmergencyController, EmergencyState>(
  EmergencyController.new,
);

/// SOS, the active emergency's details, and "I'm safe".
class EmergencyController extends Notifier<EmergencyState> {
  int? _sessionId;

  int? get _userId => ref.read(sessionProvider).value?.id;

  @override
  EmergencyState build() {
    // Whenever the walk is in an emergency (by SOS, by the server, or found when resuming), load its details.
    ref.listen(
      walkProvider.select(
        (s) => s.phase == WalkPhase.active && s.emergencyActive
            ? s.session?.id
            : null,
      ),
      (previous, next) {
        Future.microtask(() {
          if (!ref.mounted) return;
          if (next == null) {
            _sessionId = null;
            state = const EmergencyState();
          } else if (state.emergency == null && !state.triggering) {
            unawaited(load(next));
          }
        });
      },
      fireImmediately: true,
    );
    return const EmergencyState();
  }

  Future<void> load([int? sessionId]) async {
    final id = sessionId ?? ref.read(walkProvider).session?.id;
    final userId = _userId;
    if (id == null || userId == null) return;
    _sessionId = id;
    state = state.copyWith(loading: true, error: () => null);
    try {
      final emergency = await ref.read(emergencyApiProvider).active(id, userId);
      state = state.copyWith(emergency: () => emergency, loading: false);
    } on ApiException catch (e) {
      state = state.copyWith(loading: false, error: () => e.message);
    }
  }

  /// SOS. Returns true if the alert went out. On failure [EmergencyState.error] says why, and nothing is hidden:
  /// the screen must then point the person to calling for help themselves.
  Future<bool> triggerSos() async {
    final walk = ref.read(walkProvider);
    final sessionId = walk.session?.id;
    final userId = _userId;
    if (sessionId == null || userId == null || state.triggering) return false;
    _sessionId = sessionId;
    state = state.copyWith(triggering: true, error: () => null);
    try {
      final emergency = await ref
          .read(emergencyApiProvider)
          .trigger(sessionId, userId);
      ref.read(walkProvider.notifier).setEmergency(true);
      state = state.copyWith(emergency: () => emergency, triggering: false);
      // Already in emergency: the server sent no new one, so fetch the one that is active.
      if (emergency == null) unawaited(load(sessionId));
      return true;
    } on ApiException catch (e) {
      state = state.copyWith(triggering: false, error: () => e.message);
      return false;
    }
  }

  /// "I'm safe". Returns true once the server put the walk back to normal.
  Future<bool> resolve() async {
    final sessionId = _sessionId ?? ref.read(walkProvider).session?.id;
    final userId = _userId;
    if (sessionId == null || userId == null || state.resolving) return false;
    state = state.copyWith(resolving: true, error: () => null);
    try {
      var emergency = state.emergency;
      emergency ??= await ref
          .read(emergencyApiProvider)
          .active(sessionId, userId);
      if (emergency != null) {
        await ref
            .read(emergencyApiProvider)
            .resolve(emergency.id, sessionId, userId);
      }
      ref.read(walkProvider.notifier).setEmergency(false);
      state = const EmergencyState();
      unawaited(ref.read(walkProvider.notifier).refreshSession());
      return true;
    } on ApiException catch (e) {
      state = state.copyWith(resolving: false, error: () => e.message);
      return false;
    }
  }

  void clearError() => state = state.copyWith(error: () => null);
}

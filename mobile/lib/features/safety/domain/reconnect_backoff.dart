import 'dart:math' as math;

/// Wait before the next reconnect attempt: 1, 2, 4, 8, 16, then 30 seconds at most.
Duration reconnectDelay(int attempt) {
  final seconds = math.min(30, 1 << math.min(attempt, 5));
  return Duration(seconds: seconds);
}

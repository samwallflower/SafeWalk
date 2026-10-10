/// How long the "Are you safe?" prompt counts down. The server waits 45 seconds after its warning before it raises an
/// emergency, so this matches its grace period.
const idleGracePeriod = Duration(seconds: 45);

class IdlePrompt {
  const IdlePrompt({required this.deadline});

  factory IdlePrompt.startingAt(DateTime now) =>
      IdlePrompt(deadline: now.add(idleGracePeriod));

  final DateTime deadline;

  Duration remaining(DateTime now) {
    final left = deadline.difference(now);
    return left.isNegative ? Duration.zero : left;
  }

  bool expiredAt(DateTime now) => !deadline.isAfter(now);
}

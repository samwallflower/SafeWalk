/// A `tel:` link for [number]. It only opens the dialer with the number filled in: the person presses call.
/// Anything that is not a digit, `+`, `*` or `#` is dropped (spaces, dashes, brackets).
Uri? dialUri(String number) {
  final cleaned = number.replaceAll(RegExp(r'[^0-9+*#]'), '');
  if (cleaned.isEmpty) return null;
  return Uri(scheme: 'tel', path: cleaned);
}

import 'package:flutter/material.dart';

import 'inline_notice.dart';

/// An inline red banner for server errors on forms.
class FormError extends StatelessWidget {
  const FormError(this.message, {super.key});

  final String message;

  @override
  Widget build(BuildContext context) => InlineNotice(message);
}

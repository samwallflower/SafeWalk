import 'package:flutter/material.dart';

import 'empty_state.dart';

class ComingSoonScreen extends StatelessWidget {
  const ComingSoonScreen({super.key, required this.title, required this.icon});

  final String title;
  final IconData icon;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: EmptyState(
        title: 'Coming soon',
        message: 'This part of the app is being built.',
        icon: icon,
      ),
    );
  }
}

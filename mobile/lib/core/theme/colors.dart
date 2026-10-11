import 'package:flutter/material.dart';

/// The SafeWalk palette. Leaf is the soft green for surfaces you touch, forest is the green for text and icons,
/// lantern is the warm accent, and coral appears only when something needs you right now.
abstract final class AppColors {
  static const background = Color(0xFFF5FCF4);
  static const surface = Color(0xFFFFFFFF);
  static const foreground = Color(0xFF16301A);
  static const mutedForeground = Color(0xFF566B63);
  static const muted = Color(0xFFE9F7E6);
  static const border = Color(0xFFDAEFD6);

  /// Soft green fill for buttons and the selected tab. Text on it is [foreground].
  static const leaf = Color(0xFFB4F2AE);

  /// Deep green for text, icons, links and focus.
  static const primary = Color(0xFF1E6A1A);
  static const lantern = Color(0xFFFBCB62);
  static const brand = lantern;
  static const destructive = Color(0xFFCC4631);
  static const success = Color(0xFF1E6A1A);
  static const infoSoft = Color(0xFFE3FADF);
  static const destructiveSoft = Color(0xFFFDE7E2);
  static const successSoft = Color(0xFFE3FADF);
  static const warning = Color(0xFF6B4500);
  static const warningSoft = Color(0xFFFFF3D1);

  /// Strong red used for the SOS and alarm surfaces, where the label is white.
  static const alarm = Color(0xFFC4361F);
}

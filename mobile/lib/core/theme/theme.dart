import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import 'colors.dart';

const _radius = 12.0;

/// One set of corners: controls 12, cards 20, sheets 28.
const cardRadius = 20.0;
const sheetRadius = 28.0;

TextTheme _textTheme(TextTheme base, bool downloadFont) {
  final body = (downloadFont ? GoogleFonts.figtreeTextTheme(base) : base).apply(
    bodyColor: AppColors.foreground,
    displayColor: AppColors.foreground,
  );
  if (!downloadFont) return body;
  TextStyle? head(TextStyle? style) => style == null
      ? null
      : GoogleFonts.bricolageGrotesque(
          textStyle: style,
          fontWeight: FontWeight.w800,
          letterSpacing: -0.5,
        );
  return body.copyWith(
    displayLarge: head(body.displayLarge),
    displayMedium: head(body.displayMedium),
    displaySmall: head(body.displaySmall),
    headlineLarge: head(body.headlineLarge),
    headlineMedium: head(body.headlineMedium),
    headlineSmall: head(body.headlineSmall),
    titleLarge: head(body.titleLarge),
  );
}

/// [downloadFont] is turned off in tests, where google_fonts cannot fetch Inter.
ThemeData buildTheme({bool downloadFont = true}) {
  const scheme = ColorScheme.light(
    primary: AppColors.primary,
    onPrimary: Colors.white,
    primaryContainer: AppColors.leaf,
    onPrimaryContainer: AppColors.foreground,
    secondary: AppColors.lantern,
    onSecondary: AppColors.foreground,
    error: AppColors.destructive,
    onError: Colors.white,
    errorContainer: AppColors.destructiveSoft,
    surface: AppColors.surface,
    onSurface: AppColors.foreground,
    onSurfaceVariant: AppColors.mutedForeground,
    surfaceContainerHighest: AppColors.muted,
    outline: AppColors.border,
    outlineVariant: AppColors.border,
  );
  final base = ThemeData(
    useMaterial3: true,
    colorScheme: scheme,
    scaffoldBackgroundColor: AppColors.background,
  );
  final shape = RoundedRectangleBorder(
    borderRadius: BorderRadius.circular(_radius),
  );
  const minSize = Size(64, 52);

  return base.copyWith(
    textTheme: _textTheme(base.textTheme, downloadFont),
    appBarTheme: const AppBarTheme(
      backgroundColor: AppColors.surface,
      foregroundColor: AppColors.foreground,
      elevation: 0,
      scrolledUnderElevation: 0,
      centerTitle: false,
    ),
    cardTheme: CardThemeData(
      color: AppColors.surface,
      elevation: 0,
      margin: EdgeInsets.zero,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(cardRadius),
        side: const BorderSide(color: AppColors.border),
      ),
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        minimumSize: minSize,
        backgroundColor: AppColors.leaf,
        foregroundColor: AppColors.foreground,
        disabledBackgroundColor: AppColors.muted,
        shape: shape,
        textStyle: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16),
      ),
    ),
    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        minimumSize: minSize,
        shape: shape,
        foregroundColor: AppColors.primary,
        side: const BorderSide(color: AppColors.primary, width: 1.5),
        textStyle: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16),
      ),
    ),
    textButtonTheme: TextButtonThemeData(
      style: TextButton.styleFrom(
        minimumSize: const Size(48, 48),
        shape: shape,
      ),
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: AppColors.surface,
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(_radius),
        borderSide: const BorderSide(color: AppColors.border),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(_radius),
        borderSide: const BorderSide(color: AppColors.border),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(_radius),
        borderSide: const BorderSide(color: AppColors.primary, width: 2),
      ),
    ),
    snackBarTheme: SnackBarThemeData(
      behavior: SnackBarBehavior.floating,
      shape: shape,
    ),
    dividerTheme: const DividerThemeData(
      color: AppColors.border,
      space: 1,
      thickness: 1,
    ),
  );
}

import 'package:flutter_test/flutter_test.dart';
import 'package:safewalk_mobile/core/theme/colors.dart';
import 'package:safewalk_mobile/core/theme/theme.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('theme uses the web palette', () {
    final theme = buildTheme(downloadFont: false);
    expect(theme.colorScheme.primary, AppColors.primary);
    expect(theme.colorScheme.error, AppColors.destructive);
    expect(theme.scaffoldBackgroundColor, AppColors.background);
  });

  test('buttons meet the 48 dp touch target', () {
    final size = buildTheme(downloadFont: false)
        .filledButtonTheme
        .style
        ?.minimumSize
        ?.resolve({});
    expect(size?.height, greaterThanOrEqualTo(48));
  });
}

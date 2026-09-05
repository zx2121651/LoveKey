import 'package:flutter/material.dart';

/// LoveKey 对标设计系统：恋爱粉色系设计令牌
class AppColors {
  AppColors._();

  // 主色：LoveKey 标志性粉红
  static const Color primary = Color(0xFFFF4D6D);
  static const Color primaryDark = Color(0xFFE63E5D);
  static const Color primaryLight = Color(0xFFFF8FA8);

  // 辅助色
  static const Color accent = Color(0xFF8B5CF6); // 紫
  static const Color gold = Color(0xFFFFB020); // 会员金
  static const Color goldDeep = Color(0xFFB45309);

  // 背景
  static const Color background = Color(0xFFF7F8FA);
  static const Color cardBg = Color(0xFFFFFFFF);
  static const Color inputBg = Color(0xFFF1F2F5);

  // 文字
  static const Color textPrimary = Color(0xFF1F2126);
  static const Color textSecondary = Color(0xFF6B7280);
  static const Color textHint = Color(0xFFB0B4BC);

  // 功能色
  static const Color success = Color(0xFF22C55E);
  static const Color divider = Color(0xFFEEF0F3);

  // 浅色功能卡底色
  static const Color pinkBg = Color(0xFFFFEBF0);
  static const Color blueBg = Color(0xFFEBF4FF);
  static const Color purpleBg = Color(0xFFF3EFFF);
  static const Color orangeBg = Color(0xFFFFF3E6);
  static const Color greenBg = Color(0xFFE8FAF0);
}

class AppGradients {
  AppGradients._();

  /// 主品牌渐变（按钮、强调）
  static const LinearGradient brand = LinearGradient(
    begin: Alignment.centerLeft,
    end: Alignment.centerRight,
    colors: [Color(0xFFFF6B8F), Color(0xFFFF4D6D)],
  );

  /// 首页头部渐变
  static const LinearGradient header = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [Color(0xFFFFE9F0), Color(0xFFFFF6EE)],
  );

  /// 会员金渐变
  static const LinearGradient vip = LinearGradient(
    begin: Alignment.centerLeft,
    end: Alignment.centerRight,
    colors: [Color(0xFF3A2E1E), Color(0xFF1F1710)],
  );

  /// 会员金色文字/边框
  static const LinearGradient vipGold = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [Color(0xFFF6D08A), Color(0xFFE5A84B)],
  );

  /// 紫色 AI 渐变
  static const LinearGradient ai = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [Color(0xFF8B5CF6), Color(0xFFFF4D6D)],
  );
}

class AppTheme {
  AppTheme._();

  static ThemeData build() {
    return ThemeData(
      useMaterial3: true,
      scaffoldBackgroundColor: AppColors.background,
      colorScheme: ColorScheme.fromSeed(
        seedColor: AppColors.primary,
        primary: AppColors.primary,
        secondary: AppColors.accent,
        surface: Colors.white,
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: Colors.white,
        elevation: 0,
        centerTitle: true,
        titleTextStyle: TextStyle(
          color: AppColors.textPrimary,
          fontSize: 17,
          fontWeight: FontWeight.bold,
        ),
        iconTheme: IconThemeData(color: AppColors.textPrimary),
      ),
      snackBarTheme: SnackBarThemeData(
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        backgroundColor: AppColors.textPrimary,
      ),
    );
  }
}

/// 通用小组件：品牌渐变按钮
class GradientButton extends StatelessWidget {
  final String text;
  final VoidCallback? onPressed;
  final double height;
  final Gradient gradient;
  final IconData? icon;

  const GradientButton({
    super.key,
    required this.text,
    this.onPressed,
    this.height = 52,
    this.gradient = AppGradients.brand,
    this.icon,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: double.infinity,
      height: height,
      child: DecoratedBox(
        decoration: BoxDecoration(
          gradient: onPressed == null ? null : gradient,
          color: onPressed == null ? AppColors.textHint : null,
          borderRadius: BorderRadius.circular(height / 2),
          boxShadow: onPressed == null
              ? null
              : [
                  BoxShadow(
                    color: AppColors.primary.withValues(alpha: 0.35),
                    blurRadius: 12,
                    offset: const Offset(0, 6),
                  ),
                ],
        ),
        child: Material(
          color: Colors.transparent,
          child: InkWell(
            borderRadius: BorderRadius.circular(height / 2),
            onTap: onPressed,
            child: Center(
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  if (icon != null) ...[
                    Icon(icon, color: Colors.white, size: 20),
                    const SizedBox(width: 8),
                  ],
                  Text(
                    text,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
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

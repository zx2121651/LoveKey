import re
import os

# Define the global color and style palette for Luminous Jelly
# Primary Blue: #586AFE
# Background: #F4F6FE
# Cards/Surface: #FFFFFF
# Text Dark: #2B2F35
# Text Light: #585C62
# Heart Liquid Pink: #FF85A2

def process_file(filepath, replacements):
    with open(filepath, 'r') as f:
        content = f.read()
    for old, new in replacements:
        content = content.replace(old, new)
    with open(filepath, 'w') as f:
        f.write(content)

def main():
    # 1. Update home_screen.dart
    home_replacements = [
        # Background
        ("backgroundColor: const Color(0xFF0C0916)", "backgroundColor: const Color(0xFFF4F6FE)"),
        # Top Text (Lovekey Title)
        ("color: Colors.white,", "color: const Color(0xFF2B2F35),"),
        ("color: Colors.white.withValues(alpha: 0.7)", "color: const Color(0xFF585C62)"),
        # Tag background / text
        ("color: const Color(0xFFFF2E54).withOpacity(0.2)", "color: const Color(0xFF586AFE).withValues(alpha: 0.15)"),
        ("border: Border.all(color: const Color(0xFFFF2E54).withOpacity(0.5))", "border: Border.all(color: const Color(0xFF586AFE).withValues(alpha: 0.3))"),
        ("color: Color(0xFFFF2E54)", "color: Color(0xFF586AFE)"),
        # Heart Glow (Neon to Soft Blue/White)
        ("color: const Color(0xFFFF2E54).withOpacity(0.4)", "color: const Color(0xFF586AFE).withValues(alpha: 0.15)"),
        # Heart Text '30%'
        ("color: Colors.white,\n                              shadows: [\n                                Shadow(\n                                  color: const Color(0xFFFF2E54).withOpacity(0.8),",
         "color: const Color(0xFF2B2F35),\n                              shadows: [\n                                Shadow(\n                                  color: Colors.white,"),
        ("color: Colors.white70", "color: const Color(0xFF585C62)"),
        # Cards Background & Borders
        ("color: const Color(0xFF1A1528).withOpacity(0.8)", "color: Colors.white"),
        ("border: Border.all(\n            color: const Color(0xFFFF2E54).withOpacity(0.3),\n            width: 1,\n          )",
         "border: Border.all(\n            color: Colors.white,\n            width: 0,\n          )"),
        ("color: const Color(0xFFFF2E54).withOpacity(0.05)", "color: const Color(0xFF586AFE).withValues(alpha: 0.08)"),
        # Primary accent / button
        ("color: const Color(0xFFFF2E54)", "color: const Color(0xFF586AFE)"),
        # Card Titles
        ("color: Colors.white,", "color: const Color(0xFF2B2F35),"),
        ("color: Colors.white.withOpacity(0.6)", "color: const Color(0xFF585C62)"),
        # Search Bar
        ("color: const Color(0xFF1A1528)", "color: Colors.white"),
        ("border: Border.all(color: Colors.white.withOpacity(0.1))", "border: Border.all(color: Colors.white)"),
        ("style: const TextStyle(color: Colors.white)", "style: const TextStyle(color: Color(0xFF2B2F35))"),
        ("hintStyle: TextStyle(color: Colors.white.withOpacity(0.4))", "hintStyle: TextStyle(color: const Color(0xFF9A9DA4))"),
        ("color: Colors.white.withOpacity(0.4)", "color: const Color(0xFF9A9DA4)"),
        # Bottom Sheet background
        ("backgroundColor: const Color(0xFF0C0916)", "backgroundColor: const Color(0xFFF4F6FE)"),
        ("color: const Color(0xFF1A1528)", "color: Colors.white"),
        ("border: Border.all(color: const Color(0xFFFF2E54).withOpacity(0.2))", "border: Border.all(color: Colors.transparent)"),
        # Fluid Heart Painter Colors
        ("const Color(0xFFFF2E54).withOpacity(0.3)", "const Color(0xFFFFFFFF).withValues(alpha: 0.8)"), # Glass outline
        ("const Color(0xFFFF2E54).withOpacity(0.1)", "const Color(0xFF586AFE).withValues(alpha: 0.1)"), # Glass inner glow
        ("const Color(0xFFFF2E54), // Neon Pink\n          const Color(0xFF9B2EFF), // Neon Purple", "const Color(0xFFFF85A2), // Soft Pink Liquid\n          const Color(0xFFFFB3C6), // Lighter Pink"),
        ("const Color(0xFFFF2E54).withOpacity(0.5)", "const Color(0xFFFF85A2).withValues(alpha: 0.6)"), # Liquid depth
    ]
    process_file('lib/screens/home_screen.dart', home_replacements)

    # 2. Update main.dart (Bottom Nav)
    main_replacements = [
        ("colorScheme: const ColorScheme.dark(primary: Color(0xFFFF2E54), surface: Color(0xFF0C0916))",
         "colorScheme: const ColorScheme.light(primary: Color(0xFF586AFE), surface: Color(0xFFF4F6FE))"),
        ("scaffoldBackgroundColor: const Color(0xFF0C0916)", "scaffoldBackgroundColor: const Color(0xFFF4F6FE)"),
        ("border: Border(top: BorderSide(color: const Color(0xFFFF2E54).withValues(alpha: 0.2)))", "border: Border(top: BorderSide.none)"),
        ("color: const Color(0xFFFF2E54).withValues(alpha: 0.05)", "color: const Color(0xFF586AFE).withValues(alpha: 0.08)"),
        ("backgroundColor: const Color(0xFF1A1528)", "backgroundColor: Colors.white"),
        ("selectedItemColor: const Color(0xFFFF2E54)", "selectedItemColor: const Color(0xFF586AFE)"),
        ("unselectedItemColor: Colors.white.withValues(alpha: 0.4)", "unselectedItemColor: const Color(0xFF9A9DA4)"),
    ]
    process_file('lib/main.dart', main_replacements)

    # 3. counselor_screen.dart
    counselor_replacements = [
        ("backgroundColor: const Color(0xFF0C0916)", "backgroundColor: const Color(0xFFF4F6FE)"),
        ("color: Colors.white", "color: const Color(0xFF2B2F35)"),
        ("color: Colors.white70", "color: const Color(0xFF585C62)"),
        ("color: const Color(0xFF1A1528)", "color: Colors.white"),
        ("color: const Color(0xFFFF2E54).withValues(alpha: 0.1)", "color: const Color(0xFF586AFE).withValues(alpha: 0.08)"),
        ("color: const Color(0xFFFF2E54).withValues(alpha: 0.2)", "color: const Color(0xFFEEF0F9)"),
        ("color: Colors.white.withValues(alpha: 0.4)", "color: const Color(0xFF9A9DA4)"),
        ("? Colors.grey.shade800\n                  : const Color(0xFFFF2E54)", "? const Color(0xFFDFE2EC)\n                  : const Color(0xFF586AFE)"),
        ("Color(0xFF1A1528) // Solid dark purple for user\n              : const Color(0xFF1A1528).withValues(alpha: 0.8)", "Color(0xFF586AFE) // Solid blue for user\n              : Colors.white // White card for AI"),
        ("border: isMe \n              ? null \n              : Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.3), width: 1)",
         "border: null"),
        ("color: const Color(0xFFFF2E54).withValues(alpha: 0.05)", "color: const Color(0xFF586AFE).withValues(alpha: 0.08)"),
        ("color: isError ? Colors.redAccent : Colors.white", "color: isMe ? Colors.white : (isError ? const Color(0xFFB41340) : const Color(0xFF2B2F35))"),
        ("color: const Color(0xFFFF2E54)", "color: const Color(0xFF586AFE)"),
        ("color: const Color(0xFFFF2E54).withValues(alpha: 0.3)", "color: const Color(0xFF586AFE).withValues(alpha: 0.1)"),
        ("Colors.white.withValues(alpha: 0.1)", "const Color(0xFFDFE2EC)"),
    ]
    process_file('lib/screens/counselor_screen.dart', counselor_replacements)

    # 4. scripts_screen.dart
    scripts_replacements = [
        ("backgroundColor: const Color(0xFF0C0916)", "backgroundColor: const Color(0xFFF4F6FE)"),
        ("color: Colors.white", "color: const Color(0xFF2B2F35)"),
        ("color: Colors.white70", "color: const Color(0xFF585C62)"),
        ("color: Colors.white.withValues(alpha: 0.7)", "color: const Color(0xFF585C62)"),
        ("color: const Color(0xFF1A1528)", "color: Colors.white"),
        ("border: Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.3))", "border: Border.all(color: Colors.transparent)"),
        ("color: const Color(0xFFFF2E54).withValues(alpha: 0.05)", "color: const Color(0xFF586AFE).withValues(alpha: 0.08)"),
        ("color: Colors.white.withValues(alpha: 0.4)", "color: const Color(0xFF9A9DA4)"),
        ("color: isSelected ? const Color(0xFFFF2E54) : const Color(0xFF1A1528)", "color: isSelected ? const Color(0xFF586AFE) : Colors.white"),
        ("border: isSelected ? Border.all(color: const Color(0xFFFF2E54)) : Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.3))", "border: null"),
        ("boxShadow: isSelected ? [BoxShadow(color: const Color(0xFFFF2E54).withValues(alpha: 0.5), blurRadius: 8)] : null",
         "boxShadow: isSelected ? [BoxShadow(color: const Color(0xFF586AFE).withValues(alpha: 0.3), blurRadius: 8)] : [BoxShadow(color: const Color(0xFF586AFE).withValues(alpha: 0.05), blurRadius: 5)]"),
        ("color: const Color(0xFFFF2E54)", "color: const Color(0xFF586AFE)"),
        ("color: const Color(0xFFFF2E54).withValues(alpha: 0.1)", "color: const Color(0xFF586AFE).withValues(alpha: 0.1)"),
        ("color: isSelected ? Colors.white : Colors.white70", "color: isSelected ? Colors.white : const Color(0xFF585C62)"),
    ]
    process_file('lib/screens/scripts_screen.dart', scripts_replacements)

    # 5. keyboard_screen.dart
    keyboard_replacements = [
        ("backgroundColor: const Color(0xFF0C0916)", "backgroundColor: const Color(0xFFF4F6FE)"),
        ("color: Colors.white", "color: const Color(0xFF2B2F35)"),
        ("color: Colors.white70", "color: const Color(0xFF585C62)"),
        ("color: Colors.white.withValues(alpha: 0.7)", "color: const Color(0xFF585C62)"),
        ("backgroundColor: const Color(0xFFFF2E54)", "backgroundColor: const Color(0xFF586AFE)"),
        ("shadowColor: const Color(0xFFFF2E54)", "shadowColor: const Color(0xFF586AFE).withValues(alpha: 0.4)"),
        ("color: isSelected ? const Color(0xFFFF2E54).withValues(alpha: 0.2) : const Color(0xFF1A1528)",
         "color: isSelected ? const Color(0xFF586AFE).withValues(alpha: 0.1) : Colors.white"),
        ("border: Border.all(color: isSelected ? const Color(0xFFFF2E54) : const Color(0xFFFF2E54).withValues(alpha: 0.3), width: isSelected ? 2 : 1)",
         "border: isSelected ? Border.all(color: const Color(0xFF586AFE), width: 2) : null"),
        ("color: isSelected ? const Color(0xFFFF2E54).withValues(alpha: 0.2) : const Color(0xFFFF2E54).withValues(alpha: 0.05)",
         "color: const Color(0xFF586AFE).withValues(alpha: 0.08)"),
        ("color: const Color(0xFFFF2E54)", "color: const Color(0xFF586AFE)"),
        ("color: const Color(0xFF1A1528)", "color: Colors.white"),
        ("border: Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.3))", "border: null"),
        ("color: const Color(0xFF0C0916)", "color: const Color(0xFFF4F6FE)"),
    ]
    process_file('lib/screens/keyboard_screen.dart', keyboard_replacements)

    # 6. profile_screen.dart
    profile_replacements = [
        ("backgroundColor: const Color(0xFF0C0916)", "backgroundColor: const Color(0xFFF4F6FE)"),
        ("color: Colors.white", "color: const Color(0xFF2B2F35)"),
        ("color: Colors.white70", "color: const Color(0xFF585C62)"),
        ("color: Colors.white.withValues(alpha: 0.6)", "color: const Color(0xFF585C62)"),
        ("color: const Color(0xFF1A1528)", "color: Colors.white"),
        ("border: Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.5))", "border: Border.none"),
        ("color: const Color(0xFFFF2E54).withValues(alpha: 0.2)", "color: const Color(0xFF586AFE).withValues(alpha: 0.15)"),
        ("colors: [Color(0xFF2D0A1F), Color(0xFF0F0514)]", "colors: [Color(0xFF7786FF), Color(0xFF586AFE)]"),
        ("backgroundColor: const Color(0xFFFF2E54)", "backgroundColor: Colors.white"),
        ("shadowColor: const Color(0xFFFF2E54)", "shadowColor: Colors.transparent"),
        ("border: Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.2))", "border: Border.none"),
        ("color: const Color(0xFFFF2E54).withValues(alpha: 0.05)", "color: const Color(0xFF586AFE).withValues(alpha: 0.08)"),
        ("color: const Color(0xFFFF2E54)", "color: const Color(0xFF586AFE)"),
        ("color: Colors.white.withValues(alpha: 0.1)", "color: const Color(0xFFEEF0F9)"),
        ("color: const Color(0xFFFF2E54).withValues(alpha: 0.4)", "color: const Color(0xFF586AFE).withValues(alpha: 0.2)"),
        ("border: Border.all(color: const Color(0xFFFF2E54), width: 2)", "border: Border.all(color: Colors.white, width: 3)"),
    ]
    process_file('lib/screens/profile_screen.dart', profile_replacements)

    # 7. onboarding_screen.dart
    onboarding_replacements = [
        ("backgroundColor: const Color(0xFF0C0916)", "backgroundColor: Colors.white"),
        ("color: Colors.white", "color: const Color(0xFF2B2F35)"),
        ("color: Colors.white.withValues(alpha: 0.7)", "color: const Color(0xFF585C62)"),
        ("color: _currentPage == index ? const Color(0xFFFF2E54) : Colors.white.withValues(alpha: 0.2)", "color: _currentPage == index ? const Color(0xFF586AFE) : const Color(0xFFEEF0F9)"),
        ("color: const Color(0xFFFF2E54).withValues(alpha: 0.5)", "color: const Color(0xFF586AFE).withValues(alpha: 0.3)"),
        ("color: const Color(0xFFFF2E54).withValues(alpha: 0.05)", "color: Colors.white"),
        ("border: Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.3))", "border: null"),
        ("color: const Color(0xFFFF2E54).withValues(alpha: 0.2)", "color: const Color(0xFF586AFE).withValues(alpha: 0.1)"),
        ("color: const Color(0xFFFF2E54)", "color: const Color(0xFF586AFE)"),
        ("backgroundColor: const Color(0xFF1A1528)", "backgroundColor: Colors.white"),
        ("side: BorderSide(color: const Color(0xFFFF2E54).withValues(alpha: 0.3))", "side: BorderSide(color: const Color(0xFFDFE2EC))"),
        ("backgroundColor: const Color(0xFFFF2E54)", "backgroundColor: const Color(0xFF586AFE)"),
        ("shadowColor: const Color(0xFFFF2E54)", "shadowColor: const Color(0xFF586AFE).withValues(alpha: 0.4)"),
    ]
    process_file('lib/screens/onboarding_screen.dart', onboarding_replacements)

if __name__ == '__main__':
    main()

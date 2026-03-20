import re

def main():
    # Fix profile_screen.dart
    with open('lib/screens/profile_screen.dart', 'r') as f:
        content = f.read()

    # 34:77 `const Color(0xFFFF2E54)[400]` -> `const Color(0xFFFF2E54)`
    content = content.replace("const Color(0xFFFF2E54)[400]", "const Color(0xFFFF2E54)")
    with open('lib/screens/profile_screen.dart', 'w') as f:
        f.write(content)

    # Fix onboarding_screen.dart
    with open('lib/screens/onboarding_screen.dart', 'r') as f:
        content = f.read()

    # 273:64 `const Color(0xFFFF2E54)[100]!` -> `const Color(0xFFFF2E54)`
    content = content.replace("const Color(0xFFFF2E54)[100]!", "const Color(0xFFFF2E54)")

    # 66:19 `boxShadow` inside AnimatedContainer
    # AnimatedContainer takes `decoration: BoxDecoration(boxShadow: ...)` not a direct `boxShadow` argument.
    # The original was just an AnimatedContainer with color, margin, width, height, duration.
    # Let's fix that.

    # Regex to find the broken AnimatedContainer and wrap it properly in a decoration
    broken_container_regex = r'AnimatedContainer\(\n\s*duration: const Duration\(milliseconds: 300\),\n\s*margin: const EdgeInsets\.symmetric\(horizontal: 4\),\n\s*height: 8,\n\s*width: _currentPage == index \? 20 : 8,\n\s*boxShadow: _currentPage == index \? \[\n\s*BoxShadow\(color: const Color\(0xFFFF2E54\)\.withValues\(alpha: 0\.5\), blurRadius: 10\)\n\s*\] : null,\n\s*color: _currentPage == index \? const Color\(0xFFFF2E54\) : Colors\.white\.withValues\(alpha: 0\.2\),\n\s*\)'

    fixed_container = '''AnimatedContainer(
                  duration: const Duration(milliseconds: 300),
                  margin: const EdgeInsets.symmetric(horizontal: 4),
                  height: 8,
                  width: _currentPage == index ? 20 : 8,
                  decoration: BoxDecoration(
                    color: _currentPage == index ? const Color(0xFFFF2E54) : Colors.white.withValues(alpha: 0.2),
                    borderRadius: BorderRadius.circular(4),
                    boxShadow: _currentPage == index ? [
                      BoxShadow(color: const Color(0xFFFF2E54).withValues(alpha: 0.5), blurRadius: 10)
                    ] : null,
                  ),
                )'''

    # It seems originally it used `decoration: BoxDecoration` with borderRadius. Let's see original onboarding_screen.dart

    content = re.sub(r'AnimatedContainer\(.*?color: _currentPage == index \? const Color\(0xFFFF2E54\) : Colors\.white\.withValues\(alpha: 0\.2\),\n\s*borderRadius: BorderRadius\.circular\(4\),\n\s*\)', fixed_container, content, flags=re.DOTALL)

    # Let's just do a simpler replace because my regex above might fail if it originally had decoration
    content = content.replace(
        "width: _currentPage == index ? 20 : 8,\n                  boxShadow: _currentPage == index ? [\n                    BoxShadow(color: const Color(0xFFFF2E54).withValues(alpha: 0.5), blurRadius: 10)\n                  ] : null,\n                  decoration: BoxDecoration(\n                    color: _currentPage == index ? const Color(0xFFFF2E54) : Colors.white.withValues(alpha: 0.2),",
        "width: _currentPage == index ? 20 : 8,\n                  decoration: BoxDecoration(\n                    boxShadow: _currentPage == index ? [\n                      BoxShadow(color: const Color(0xFFFF2E54).withValues(alpha: 0.5), blurRadius: 10)\n                    ] : null,\n                    color: _currentPage == index ? const Color(0xFFFF2E54) : Colors.white.withValues(alpha: 0.2),"
    )

    with open('lib/screens/onboarding_screen.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

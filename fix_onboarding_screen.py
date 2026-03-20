import re

def main():
    with open('lib/screens/onboarding_screen.dart', 'r') as f:
        content = f.read()

    # 1. Backgrounds
    content = content.replace("backgroundColor: Colors.white", "backgroundColor: const Color(0xFF0C0916)")

    # 2. Texts
    content = content.replace("color: Colors.black87", "color: Colors.white")
    content = content.replace("color: Colors.black54", "color: Colors.white.withValues(alpha: 0.7)")

    # 3. Dots
    content = content.replace(
        "color: _currentPage == index ? Colors.pink : Colors.grey.shade300,",
        "color: _currentPage == index ? const Color(0xFFFF2E54) : Colors.white.withValues(alpha: 0.2),"
    )
    content = content.replace(
        "width: _currentPage == index ? 20 : 8,",
        "width: _currentPage == index ? 20 : 8,\n                  boxShadow: _currentPage == index ? [\n                    BoxShadow(color: const Color(0xFFFF2E54).withValues(alpha: 0.5), blurRadius: 10)\n                  ] : null,"
    )

    # 4. Image Container Glow
    content = content.replace(
        "color: Colors.pink.shade50,",
        "color: const Color(0xFFFF2E54).withValues(alpha: 0.05),\n              border: Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.3)),\n              boxShadow: [\n                BoxShadow(\n                  color: const Color(0xFFFF2E54).withValues(alpha: 0.2),\n                  blurRadius: 30,\n                  spreadRadius: 5,\n                )\n              ],"
    )

    # Icons in Container
    content = content.replace("color: Colors.pink", "color: const Color(0xFFFF2E54)")

    # 5. Buttons
    content = content.replace(
        "backgroundColor: Colors.white,",
        "backgroundColor: const Color(0xFF1A1528),\n                        side: BorderSide(color: const Color(0xFFFF2E54).withValues(alpha: 0.3)),"
    )
    content = content.replace("color: Colors.black", "color: Colors.white")

    # The main Next/Start button
    content = content.replace(
        "backgroundColor: Colors.pink,",
        "backgroundColor: const Color(0xFFFF2E54),\n                        shadowColor: const Color(0xFFFF2E54),\n                        elevation: 10,"
    )

    with open('lib/screens/onboarding_screen.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

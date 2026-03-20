import re

def main():
    with open('lib/screens/onboarding_screen.dart', 'r') as f:
        content = f.read()

    # The issue is still the boxShadow in AnimatedContainer which needs to be inside BoxDecoration
    content = re.sub(
        r'width: _currentPage == index \? 20 : 8,\n\s*boxShadow: _currentPage == index \? \[\n\s*BoxShadow\(color: const Color\(0xFFFF2E54\)\.withValues\(alpha: 0\.5\), blurRadius: 10\)\n\s*\] : null,\n\s*decoration: BoxDecoration\(',
        r'width: _currentPage == index ? 20 : 8,\n                decoration: BoxDecoration(\n                  boxShadow: _currentPage == index ? [\n                    BoxShadow(color: const Color(0xFFFF2E54).withValues(alpha: 0.5), blurRadius: 10)\n                  ] : null,',
        content,
        flags=re.DOTALL
    )

    # Let's verify what the actual text is:
    print(content[content.find("AnimatedContainer"):content.find("AnimatedContainer")+500])

    with open('lib/screens/onboarding_screen.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

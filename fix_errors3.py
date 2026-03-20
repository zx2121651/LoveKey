import re

def main():
    with open('lib/screens/onboarding_screen.dart', 'r') as f:
        content = f.read()

    # The issue is near line 66:
    # boxShadow: _currentPage == index ? [ BoxShadow(...) ] : null,
    # height: 8,
    # decoration: BoxDecoration( ... )

    # We need to move the boxShadow inside the BoxDecoration and remove it from the Container level

    # Let's replace the whole block
    bad_block = '''                  boxShadow: _currentPage == index ? [
                    BoxShadow(color: const Color(0xFFFF2E54).withValues(alpha: 0.5), blurRadius: 10)
                  ] : null,
                        height: 8,
                        decoration: BoxDecoration(
                          color: _currentPage == index
                              ? const Color(0xFFFF4D85)
                              : Colors.grey[300],
                          borderRadius: BorderRadius.circular(4),
                        ),'''

    good_block = '''                        height: 8,
                        decoration: BoxDecoration(
                          color: _currentPage == index
                              ? const Color(0xFFFF2E54)
                              : Colors.white.withValues(alpha: 0.2),
                          borderRadius: BorderRadius.circular(4),
                          boxShadow: _currentPage == index ? [
                            BoxShadow(color: const Color(0xFFFF2E54).withValues(alpha: 0.5), blurRadius: 10)
                          ] : null,
                        ),'''

    content = content.replace(bad_block, good_block)

    with open('lib/screens/onboarding_screen.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

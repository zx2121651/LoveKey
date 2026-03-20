import re

def main():
    with open('lib/screens/scripts_screen.dart', 'r') as f:
        content = f.read()

    # 1. Backgrounds
    content = content.replace("backgroundColor: const Color(0xFFF9FAFB)", "backgroundColor: const Color(0xFF0C0916)")
    content = content.replace("backgroundColor: Colors.white", "backgroundColor: const Color(0xFF1A1528)")

    # 2. Texts
    content = content.replace("color: Colors.black87", "color: Colors.white")
    content = content.replace("color: Colors.black54", "color: Colors.white70")
    content = content.replace("color: Colors.grey[700]", "color: Colors.white.withValues(alpha: 0.7)")

    # 3. Search Bar
    content = content.replace(
        "color: Colors.grey.shade100,",
        "color: const Color(0xFF1A1528),"
    )
    content = content.replace(
        "border: Border.all(color: Colors.grey.shade300),",
        "border: Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.3)),\n          boxShadow: [BoxShadow(color: const Color(0xFFFF2E54).withValues(alpha: 0.05), blurRadius: 10)],"
    )
    content = content.replace(
        "hintStyle: TextStyle(color: Colors.grey.shade500),",
        "hintStyle: TextStyle(color: Colors.white.withValues(alpha: 0.4)),"
    )
    content = content.replace(
        "color: Colors.grey.shade500",
        "color: Colors.white.withValues(alpha: 0.4)"
    )

    # 4. Categories Tabs (Pills)
    content = content.replace(
        "color: isSelected ? Colors.pink : Colors.white,",
        "color: isSelected ? const Color(0xFFFF2E54) : const Color(0xFF1A1528),"
    )
    content = content.replace(
        "border: isSelected ? null : Border.all(color: Colors.grey.shade300),",
        "border: isSelected ? Border.all(color: const Color(0xFFFF2E54)) : Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.3)),"
    )
    content = content.replace(
        "color: isSelected ? Colors.white : Colors.black87,",
        "color: isSelected ? Colors.white : Colors.white70,"
    )

    # Add neon glow to selected tab
    content = content.replace(
        "borderRadius: BorderRadius.circular(20),",
        "borderRadius: BorderRadius.circular(20),\n            boxShadow: isSelected ? [BoxShadow(color: const Color(0xFFFF2E54).withValues(alpha: 0.5), blurRadius: 8)] : null,"
    )

    # 5. Cards (Glassmorphism)
    content = content.replace(
        "boxShadow: [\n        BoxShadow(",
        "border: Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.3)),\n      boxShadow: [\n        BoxShadow("
    )
    content = content.replace(
        "color: Colors.grey.withOpacity(0.05),",
        "color: const Color(0xFFFF2E54).withValues(alpha: 0.05),"
    )

    # 6. Icons
    content = content.replace("color: Colors.pink", "color: const Color(0xFFFF2E54)")
    content = content.replace("color: Colors.pink[50]", "color: const Color(0xFFFF2E54).withValues(alpha: 0.1)")

    # Write changes
    with open('lib/screens/scripts_screen.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

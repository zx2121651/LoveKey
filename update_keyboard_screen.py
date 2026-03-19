import re

def main():
    with open('lib/screens/keyboard_screen.dart', 'r') as f:
        content = f.read()

    # 1. Backgrounds
    content = content.replace("backgroundColor: const Color(0xFFF9FAFB)", "backgroundColor: const Color(0xFF0C0916)")
    content = content.replace("backgroundColor: Colors.white", "backgroundColor: const Color(0xFF1A1528)")

    # 2. Texts
    content = content.replace("color: Colors.black87", "color: Colors.white")
    content = content.replace("color: Colors.black54", "color: Colors.white70")
    content = content.replace("color: Colors.grey.shade600", "color: Colors.white70")
    content = content.replace("color: Colors.grey[700]", "color: Colors.white.withValues(alpha: 0.7)")

    # 3. Apply Button
    content = content.replace(
        "backgroundColor: Colors.pink,",
        "backgroundColor: const Color(0xFFFF2E54),\n                shadowColor: const Color(0xFFFF2E54),\n                elevation: 10,"
    )

    # 4. Persona Cards
    content = content.replace(
        "color: isSelected ? Colors.pink.shade50 : Colors.white,",
        "color: isSelected ? const Color(0xFFFF2E54).withValues(alpha: 0.2) : const Color(0xFF1A1528),"
    )
    content = content.replace(
        "border: Border.all(color: isSelected ? Colors.pink : Colors.grey.shade300, width: isSelected ? 2 : 1),",
        "border: Border.all(color: isSelected ? const Color(0xFFFF2E54) : const Color(0xFFFF2E54).withValues(alpha: 0.3), width: isSelected ? 2 : 1),\n            boxShadow: [BoxShadow(color: isSelected ? const Color(0xFFFF2E54).withValues(alpha: 0.2) : const Color(0xFFFF2E54).withValues(alpha: 0.05), blurRadius: 10)],"
    )
    content = content.replace("color: Colors.black,", "color: Colors.white,")

    # 5. Icons and Checkmarks
    content = content.replace(
        "Icon(Icons.check_circle, color: Colors.pink",
        "Icon(Icons.check_circle, color: const Color(0xFFFF2E54)"
    )

    # 6. Mockup Container
    content = content.replace(
        "decoration: BoxDecoration(\n              color: Colors.white,",
        "decoration: BoxDecoration(\n              color: const Color(0xFF1A1528),"
    )
    content = content.replace(
        "color: Colors.grey.withOpacity(0.1),",
        "color: const Color(0xFFFF2E54).withValues(alpha: 0.05),"
    )
    content = content.replace(
        "border: Border.all(color: Colors.grey.shade200)",
        "border: Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.3))"
    )

    # Keyboard Mockup key colors
    content = content.replace(
        "color: Colors.grey.shade100,",
        "color: const Color(0xFF0C0916),"
    )

    # Write changes
    with open('lib/screens/keyboard_screen.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

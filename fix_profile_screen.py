import re

def main():
    with open('lib/screens/profile_screen.dart', 'r') as f:
        content = f.read()

    # 1. Backgrounds
    content = content.replace("backgroundColor: const Color(0xFFF9FAFB)", "backgroundColor: const Color(0xFF0C0916)")
    content = content.replace("backgroundColor: Colors.white", "backgroundColor: const Color(0xFF1A1528)")

    # 2. Texts
    content = content.replace("color: Colors.black87", "color: Colors.white")
    content = content.replace("color: Colors.black54", "color: Colors.white70")
    content = content.replace("color: Colors.grey[600]", "color: Colors.white.withValues(alpha: 0.6)")
    content = content.replace("color: Colors.grey.shade600", "color: Colors.white.withValues(alpha: 0.6)")

    # 3. AppBar
    content = content.replace("backgroundColor: Colors.transparent", "backgroundColor: const Color(0xFF0C0916)")

    # 4. VIP Card Container
    # Find gradient and change to neon dark theme
    content = content.replace(
        "gradient: const LinearGradient(\n            colors: [Color(0xFFFFB6C1), Color(0xFFFF69B4)],",
        "color: const Color(0xFF1A1528),\n          border: Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.5)),\n          boxShadow: [\n            BoxShadow(\n              color: const Color(0xFFFF2E54).withValues(alpha: 0.2),\n              blurRadius: 15,\n              spreadRadius: 2,\n            ),\n          ],\n          gradient: const LinearGradient(\n            colors: [Color(0xFF2D0A1F), Color(0xFF0F0514)],"
    )

    # VIP Button
    content = content.replace(
        "backgroundColor: Colors.white.withOpacity(0.2),",
        "backgroundColor: const Color(0xFFFF2E54),\n                    shadowColor: const Color(0xFFFF2E54),\n                    elevation: 10,"
    )
    # VIP Button text color from white back to white is fine, but previously it was white on light pink.
    # Text in VIP Button was white, keep it.

    # 5. Menu List Container (Glassmorphism)
    content = content.replace(
        "boxShadow: [\n          BoxShadow(\n            color: Colors.grey.withOpacity(0.05),",
        "border: Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.2)),\n        boxShadow: [\n          BoxShadow(\n            color: const Color(0xFFFF2E54).withValues(alpha: 0.05),"
    )

    # Icon colors in menu
    content = content.replace("color: Colors.pink", "color: const Color(0xFFFF2E54)")
    content = content.replace("color: Colors.orange", "color: const Color(0xFFFF2E54)")
    content = content.replace("color: Colors.blue", "color: const Color(0xFFFF2E54)")
    content = content.replace("color: Colors.purple", "color: const Color(0xFFFF2E54)")

    # Divider colors in menu list
    content = content.replace("color: Colors.grey.shade200", "color: Colors.white.withValues(alpha: 0.1)")

    # 6. Avatar Glow
    content = content.replace(
        "CircleAvatar(\n                radius: 40,\n                backgroundColor: Colors.pink.shade50,",
        "Container(\n                decoration: BoxDecoration(\n                  shape: BoxShape.circle,\n                  boxShadow: [\n                    BoxShadow(\n                      color: const Color(0xFFFF2E54).withValues(alpha: 0.4),\n                      blurRadius: 15,\n                    ),\n                  ],\n                  border: Border.all(color: const Color(0xFFFF2E54), width: 2),\n                ),\n                child: CircleAvatar(\n                  radius: 38,\n                  backgroundColor: const Color(0xFF1A1528),"
    )
    # Add closing ) for Container
    content = content.replace(
        "child: Icon(Icons.person, size: 40, color: Colors.pink),\n              ),",
        "child: const Icon(Icons.person, size: 40, color: Color(0xFFFF2E54)),\n                ),\n              ),"
    )

    with open('lib/screens/profile_screen.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

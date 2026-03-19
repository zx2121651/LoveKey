import re

def main():
    with open('lib/screens/counselor_screen.dart', 'r') as f:
        content = f.read()

    # 1. Backgrounds
    content = content.replace("backgroundColor: const Color(0xFFFDF8F9)", "backgroundColor: const Color(0xFF0C0916)")
    content = content.replace("backgroundColor: Colors.white", "backgroundColor: const Color(0xFF1A1528)")

    # 2. Text Colors
    content = content.replace("color: Colors.black87", "color: Colors.white")
    content = content.replace("color: Colors.black54", "color: Colors.white70")
    content = content.replace("color: Colors.black", "color: Colors.white")
    content = content.replace("color: Colors.grey.shade600", "color: Colors.white70")

    # 3. Input Bar Container
    content = content.replace(
        "decoration: BoxDecoration(\n        color: Colors.white,",
        "decoration: BoxDecoration(\n        color: const Color(0xFF1A1528),"
    )
    content = content.replace(
        "color: Colors.pink.withOpacity(0.05),",
        "color: const Color(0xFFFF2E54).withValues(alpha: 0.1),"
    )
    content = content.replace(
        "border: Border(top: BorderSide(color: Colors.grey.shade200)),",
        "border: Border(top: BorderSide(color: const Color(0xFFFF2E54).withValues(alpha: 0.2))),"
    )

    # 4. Input TextField
    content = content.replace(
        "fillColor: const Color(0xFFF9FAFB),",
        "fillColor: const Color(0xFF0C0916),"
    )
    content = content.replace(
        "hintStyle: TextStyle(color: Colors.grey.shade400),",
        "hintStyle: TextStyle(color: Colors.white.withValues(alpha: 0.4)),"
    )
    content = content.replace(
        "child: TextField(",
        "child: TextField(\n                style: const TextStyle(color: Colors.white),"
    )

    # 5. Send Button
    content = content.replace(
        "backgroundColor: _textController.text.trim().isEmpty\n                  ? Colors.grey.shade300\n                  : Colors.pink,",
        "backgroundColor: _textController.text.trim().isEmpty\n                  ? Colors.grey.shade800\n                  : const Color(0xFFFF2E54),"
    )

    # 6. Chat Bubbles replace
    bubble_regex = r'  Widget _buildMessageBubble\(Map<String, dynamic> message\) \{.*?return Align\(.*?\}\);.*?\}'

    new_bubble = '''  Widget _buildMessageBubble(Map<String, dynamic> message) {
    final isMe = message['isMe'] as bool;
    final text = message['text'] as String;
    final isError = message['isError'] == true;

    return Align(
      alignment: isMe ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: EdgeInsets.only(
          left: isMe ? 50 : 0,
          right: isMe ? 0 : 50,
          bottom: 4,
        ),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        decoration: BoxDecoration(
          color: isMe
              ? const Color(0xFF1A1528) // Solid dark purple for user
              : const Color(0xFF1A1528).withValues(alpha: 0.8), // Glassmorphism for AI
          borderRadius: BorderRadius.only(
            topLeft: const Radius.circular(20),
            topRight: const Radius.circular(20),
            bottomLeft: Radius.circular(isMe ? 20 : 5),
            bottomRight: Radius.circular(isMe ? 5 : 20),
          ),
          border: isMe
              ? null
              : Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.3), width: 1),
          boxShadow: isMe
              ? null
              : [
                  BoxShadow(
                    color: const Color(0xFFFF2E54).withValues(alpha: 0.05),
                    blurRadius: 10,
                  )
                ],
        ),
        child: Text(
          text,
          style: TextStyle(
            color: isError ? Colors.redAccent : Colors.white,
            fontSize: 15,
            height: 1.4,
          ),
        ),
      ),
    );
  }'''

    content = re.sub(bubble_regex, new_bubble, content, flags=re.DOTALL)

    # 7. Avatar colors
    content = content.replace("color: Colors.pink", "color: const Color(0xFFFF2E54)")
    content = content.replace("backgroundColor: Colors.pink.shade50", "backgroundColor: const Color(0xFFFF2E54).withValues(alpha: 0.1)")

    # Add a glowing Container explicitly wrapping the CircleAvatar in AppBar
    # ONLY replace specific CircleAvatars that are easy
    # Instead of regex, let's just make the CircleAvatar itself have a neon pink background since we can't easily add box-shadows without nesting properly.
    content = content.replace(
        "CircleAvatar(\n                radius: 18,\n                backgroundColor: Colors.pink.shade50,\n                child: Icon(Icons.favorite, size: 20, color: Colors.pink),\n              )",
        "Container(\n                decoration: BoxDecoration(\n                  shape: BoxShape.circle,\n                  boxShadow: [\n                    BoxShadow(\n                      color: const Color(0xFFFF2E54).withValues(alpha: 0.3),\n                      blurRadius: 10,\n                    ),\n                  ],\n                ),\n                child: CircleAvatar(\n                  radius: 18,\n                  backgroundColor: const Color(0xFFFF2E54).withValues(alpha: 0.1),\n                  child: const Icon(Icons.favorite, size: 20, color: Color(0xFFFF2E54)),\n                ),\n              )"
    )

    # In _buildBotGreeting
    content = content.replace(
        "CircleAvatar(\n              backgroundColor: Colors.pink.shade50,\n              child: Icon(Icons.favorite, color: Colors.pink, size: 20),\n            )",
        "Container(\n              decoration: BoxDecoration(\n                shape: BoxShape.circle,\n                boxShadow: [\n                  BoxShadow(\n                    color: const Color(0xFFFF2E54).withValues(alpha: 0.3),\n                    blurRadius: 10,\n                  ),\n                ],\n              ),\n              child: CircleAvatar(\n                backgroundColor: const Color(0xFFFF2E54).withValues(alpha: 0.1),\n                child: const Icon(Icons.favorite, color: Color(0xFFFF2E54), size: 20),\n              ),\n            )"
    )

    # Divider colors
    content = content.replace("color: Colors.grey.shade100", "color: Colors.white.withValues(alpha: 0.1)")
    content = content.replace("color: Colors.grey.shade200", "color: Colors.white.withValues(alpha: 0.1)")

    # BottomSheet UI
    content = content.replace(
        "const CircleAvatar(\n                        backgroundColor: Colors.white,",
        "const CircleAvatar(\n                        backgroundColor: Color(0xFF1A1528),"
    )

    with open('lib/screens/counselor_screen.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

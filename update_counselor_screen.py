import re

def main():
    with open('lib/screens/counselor_screen.dart', 'r') as f:
        content = f.read()

    # 1. Scaffold background and AppBar
    content = content.replace("backgroundColor: const Color(0xFFFDF8F9)", "backgroundColor: const Color(0xFF0C0916)")
    content = content.replace("backgroundColor: Colors.white", "backgroundColor: const Color(0xFF1A1528)")

    # 2. Update AppBar Text
    content = content.replace("color: Colors.black87", "color: Colors.white")
    content = content.replace("color: Colors.black54", "color: Colors.white70")

    # 3. Update Input Bar Container
    content = content.replace(
        "decoration: BoxDecoration(\n        color: Colors.white,",
        "decoration: BoxDecoration(\n        color: const Color(0xFF1A1528),"
    )
    content = content.replace(
        "boxShadow: [\n          BoxShadow(\n            color: Colors.pink.withOpacity(0.05),",
        "boxShadow: [\n          BoxShadow(\n            color: const Color(0xFFFF2E54).withValues(alpha: 0.1),"
    )
    content = content.replace(
        "border: Border(top: BorderSide(color: Colors.grey.shade200)),",
        "border: Border(top: BorderSide(color: const Color(0xFFFF2E54).withValues(alpha: 0.2))),"
    )

    # 4. Update Input TextField
    content = content.replace(
        "fillColor: const Color(0xFFF9FAFB),",
        "fillColor: const Color(0xFF0C0916),"
    )
    content = content.replace(
        "hintStyle: TextStyle(color: Colors.grey.shade400),",
        "hintStyle: TextStyle(color: Colors.white.withValues(alpha: 0.4)),"
    )
    # TextField Text Style
    content = content.replace(
        "child: TextField(",
        "child: TextField(\n                style: const TextStyle(color: Colors.white),"
    )

    # 5. Update Send Button
    content = content.replace(
        "backgroundColor: _textController.text.trim().isEmpty\n                  ? Colors.grey.shade300\n                  : Colors.pink,",
        "backgroundColor: _textController.text.trim().isEmpty\n                  ? Colors.grey.shade800\n                  : const Color(0xFFFF2E54),"
    )

    # 6. Chat Bubbles
    # Find _buildMessageBubble method
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

    # Update avatar and bot text color
    content = content.replace("color: Colors.pink", "color: const Color(0xFFFF2E54)")
    content = content.replace("backgroundColor: Colors.pink.shade50", "backgroundColor: const Color(0xFFFF2E54).withValues(alpha: 0.1)")

    # Add neon effect to AI avatar
    content = content.replace(
        "CircleAvatar(",
        "Container(\n                decoration: BoxDecoration(\n                  shape: BoxShape.circle,\n                  boxShadow: [\n                    BoxShadow(\n                      color: const Color(0xFFFF2E54).withValues(alpha: 0.3),\n                      blurRadius: 10,\n                    ),\n                  ],\n                ),\n                child: CircleAvatar("
    )

    # Bottom Sheet Background
    content = content.replace("backgroundColor: Colors.white", "backgroundColor: const Color(0xFF1A1528)")

    # Divider colors
    content = content.replace("color: Colors.grey.shade100", "color: Colors.white.withValues(alpha: 0.1)")
    content = content.replace("color: Colors.grey.shade200", "color: Colors.white.withValues(alpha: 0.1)")

    # Misc text colors
    content = content.replace("color: Colors.black", "color: Colors.white")
    content = content.replace("color: Colors.grey.shade600", "color: Colors.white70")

    with open('lib/screens/counselor_screen.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

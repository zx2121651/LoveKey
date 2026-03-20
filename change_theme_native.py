import re

def main():
    with open('android/app/src/main/kotlin/com/example/lovekey_clone/LoveKeyIME.kt', 'r') as f:
        content = f.read()

    # Since the previous run might have failed or been partial due to missing imports or syntax:
    # Let's ensure BorderStroke is imported.
    if "import androidx.compose.foundation.BorderStroke" not in content:
        content = content.replace("import androidx.compose.foundation.background", "import androidx.compose.foundation.background\nimport androidx.compose.foundation.BorderStroke")

    # Change the gradient brush to SolidColor brush for background
    content = content.replace(
        "Brush.verticalGradient(\n                    colors = listOf(Color(0xFFE8E8FF), Color(0xFFF3E5F5))\n                )",
        "SolidColor(Color(0xFF0C0916))"
    )

    # Convert remaining white buttons and shapes to dark glass style
    content = content.replace(
        "colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),",
        "colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1A1528)),\n                    border = BorderStroke(1.dp, Color(0xFFFF2E54).copy(alpha = 0.3f)),"
    )
    content = content.replace("Color(0xFF333333)", "Color.White")
    content = content.replace("Color(0xFF666666)", "Color.LightGray")
    content = content.replace("Color(0xFF999999)", "Color.Gray")

    content = content.replace("background(Color(0xFF4285F4))", "background(Color(0xFF1A1528)).border(1.dp, Color(0xFFFF2E54), CircleShape)")

    # "✨换个说法" button color
    content = content.replace("backgroundColor = Color(0xFF8A9CFF)", "backgroundColor = Color(0xFFFF2E54)")

    # Overlay backgrounds
    content = content.replace("background(Color(0xFFF3F4F6))", "background(Color(0xFF1A1528))")
    content = content.replace(
        "background(Color.White, RoundedCornerShape(12.dp))",
        "background(Color(0xFF0C0916), RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFFF2E54).copy(alpha = 0.3f), RoundedCornerShape(12.dp))"
    )

    # Pills active state
    content = content.replace("Color(0xFF8A9CFF) else Color.White", "Color(0xFFFF2E54) else Color(0xFF0C0916)")

    # T9 Keyboard keys
    content = content.replace("backgroundColor = Color.White", "backgroundColor = Color(0xFF1A1528)")
    content = content.replace("backgroundColor = Color(0xFF4285F4)", "backgroundColor = Color(0xFFFF2E54)")
    content = content.replace("color = Color.Black", "color = Color.White")
    content = content.replace("color = Color.Gray", "color = Color.LightGray")

    with open('android/app/src/main/kotlin/com/example/lovekey_clone/LoveKeyIME.kt', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

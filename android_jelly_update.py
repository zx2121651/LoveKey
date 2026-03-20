import re

def process_file(filepath, replacements):
    with open(filepath, 'r') as f:
        content = f.read()
    for old, new in replacements:
        content = content.replace(old, new)
    with open(filepath, 'w') as f:
        f.write(content)

def main():
    android_replacements = [
        # Undo dark mode
        ("SolidColor(Color(0xFF0C0916))", "SolidColor(Color(0xFFF4F6FE))"),
        ("backgroundColor = Color(0xFF1A1528)", "backgroundColor = Color(0xFFFFFFFF)"),
        ("border = BorderStroke(1.dp, Color(0xFFFF2E54).copy(alpha = 0.3f))", "border = BorderStroke(1.dp, Color(0xFFDFE2EC))"),
        ("color = Color.White", "color = Color(0xFF2B2F35)"),
        ("color = Color.LightGray", "color = Color(0xFF585C62)"),
        ("background(Color(0xFF1A1528)).border(1.dp, Color(0xFFFF2E54), CircleShape)", "background(Color(0xFF586AFE), CircleShape)"),

        # Primary Action Button
        ("backgroundColor = Color(0xFFFF2E54)", "backgroundColor = Color(0xFF586AFE)"),
        ("Text(\"✨换个说法\", color = Color.White", "Text(\"✨换个说法\", color = Color.White"),

        # Draft pill
        ("background(Color(0xFF1A1528)).border(1.dp, Color(0xFFFF2E54).copy(alpha=0.3f), RoundedCornerShape(18.dp))", "background(Color.White, RoundedCornerShape(18.dp)).border(1.dp, Color(0xFFDFE2EC), RoundedCornerShape(18.dp))"),
        ("color = Color(0xFF333333)", "color = Color(0xFF2B2F35)"),

        # Overlay Backgrounds
        ("background(Color(0xFF1A1528))", "background(Color(0xFFF4F6FE))"),
        ("background(Color(0xFF0C0916), RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFFF2E54).copy(alpha = 0.3f), RoundedCornerShape(12.dp))", "background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFEEF0F9), RoundedCornerShape(12.dp))"),

        # Overlay Category Pills
        ("Color(0xFFFF2E54) else Color(0xFF0C0916)", "Color(0xFF586AFE) else Color(0xFFFFFFFF)"),
        ("Color.White else Color.LightGray", "Color.White else Color(0xFF585C62)"),
    ]
    process_file('android/app/src/main/kotlin/com/example/lovekey_clone/LoveKeyIME.kt', android_replacements)

if __name__ == '__main__':
    main()

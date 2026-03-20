import re
import os

def process_file(filepath, replacements):
    with open(filepath, 'r') as f:
        content = f.read()
    for old, new in replacements:
        content = content.replace(old, new)
    with open(filepath, 'w') as f:
        f.write(content)

def main():
    keyboard_reps = [
        ("color: const Color(0xFF2B2F35)70", "color: const Color(0xFF585C62)"),
    ]
    process_file('lib/screens/keyboard_screen.dart', keyboard_reps)

    profile_reps = [
        ("color: const Color(0xFF2B2F35)70", "color: const Color(0xFF585C62)"),
    ]
    process_file('lib/screens/profile_screen.dart', profile_reps)

if __name__ == '__main__':
    main()

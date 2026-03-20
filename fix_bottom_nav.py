import re

def main():
    with open('lib/main.dart', 'r') as f:
        content = f.read()

    # We need to change ThemeData to support the dark theme properly without breaking too much
    content = content.replace(
        "colorScheme: ColorScheme.fromSeed(seedColor: Colors.pink),",
        "colorScheme: const ColorScheme.dark(primary: Color(0xFFFF2E54), surface: Color(0xFF0C0916)),\n        scaffoldBackgroundColor: const Color(0xFF0C0916),"
    )

    # Change BottomNavigationBar directly
    new_nav = '''      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          border: Border(top: BorderSide(color: const Color(0xFFFF2E54).withValues(alpha: 0.2))),
          boxShadow: [
            BoxShadow(
              color: const Color(0xFFFF2E54).withValues(alpha: 0.05),
              blurRadius: 10,
              offset: const Offset(0, -5),
            )
          ]
        ),
        child: BottomNavigationBar(
          backgroundColor: const Color(0xFF1A1528),
          currentIndex: _currentIndex,
          onTap: (index) {
            setState(() {
              _currentIndex = index;
            });
          },
          type: BottomNavigationBarType.fixed,
          selectedItemColor: const Color(0xFFFF2E54),
          unselectedItemColor: Colors.white.withValues(alpha: 0.4),
          items: const ['''

    content = re.sub(r'      bottomNavigationBar: BottomNavigationBar\(\n        currentIndex: _currentIndex,\n        onTap: \(index\) \{\n          setState\(\(\) \{\n            _currentIndex = index;\n          \}\);\n        \},\n        type: BottomNavigationBarType.fixed,\n        selectedItemColor: Theme.of\(context\).colorScheme.primary,\n        unselectedItemColor: Colors.grey,\n        items: const \[', new_nav, content, flags=re.DOTALL)

    # Add closing for the Container wrap around BottomNavigationBar
    content = re.sub(r'        \],\n      \),\n    \);\n  \}\n\}', r'        ],\n      ),\n      ),\n    );\n  }\n}', content)

    with open('lib/main.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

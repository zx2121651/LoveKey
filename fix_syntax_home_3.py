import re

def main():
    with open('lib/screens/home_screen.dart', 'r') as f:
        content = f.read()

    # The missing parenthesis was in Transform.translate, so let's manually write out the full replacement

    # Identify the exact AnimatedBuilder block bounds
    # Since regex fails because of multiline and greedy matching, let's use string operations
    start_idx = content.find('          AnimatedBuilder(\n            animation: _heartAnimationController')
    end_idx = content.find('          // Search Bar or something after', start_idx)
    if end_idx == -1:
        end_idx = content.find('        ],\n      ),\n    );\n  }\n\n  Widget _buildSearchBar', start_idx)

    if start_idx != -1 and end_idx != -1:
        # Actually, let's just restore the file completely and ONLY replace the FluidHeartPainter class and the two lines for Transform
        pass

if __name__ == '__main__':
    main()

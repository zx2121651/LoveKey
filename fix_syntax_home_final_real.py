import re

def main():
    with open('lib/screens/home_screen.dart', 'r') as f:
        content = f.read()

    # I'll just find the exact block from `AnimatedBuilder` to the closing `),` and replace it
    # Find start:
    start_str = "          AnimatedBuilder(\n            animation: _heartAnimationController,\n            builder: (context, child) {"
    end_str = "              );\n            },\n          ),"

    # We can also just read lines and replace the exact lines 130 to 200
    lines = content.split('\n')
    start_idx = -1
    end_idx = -1
    for i, line in enumerate(lines):
        if "AnimatedBuilder(" in line and "_heartAnimationController" in lines[i+1]:
            start_idx = i
        if start_idx != -1 and "Widget _buildSearchBar" in line:
            end_idx = i
            break

    if start_idx != -1 and end_idx != -1:
        # The block ends just before `Widget _buildSearchBar`
        # Let's find the `          ),` that closes the AnimatedBuilder
        # It's better to just replace the broken part
        pass

    # The previous regex `r'              return Transform\.translate\([\s\S]*?\}\s*,\s*\)\s*;\s*\}\s*,\s*\),' ` failed because it didn't match the ending correctly.
    # The ending in the current file is:
    #                       ],
    #                     ),
    #                   ],
    #                 ),
    #               ),
    #             );
    #           },
    #         ),

    fixed_block = '''          AnimatedBuilder(
            animation: _heartAnimationController,
            builder: (context, child) {
              return Transform.translate(
                offset: Offset(0, _heartHoverAnimation.value),
                child: Transform.scale(
                  scale: _heartScaleAnimation.value,
                  child: SizedBox(
                    width: 160,
                    height: 160,
                    child: Stack(
                      alignment: Alignment.center,
                      children: [
                        Container(
                          width: 90,
                          height: 90,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            boxShadow: [
                              BoxShadow(
                                color: const Color(0xFF586AFE).withValues(alpha: 0.15),
                                blurRadius: 30,
                                spreadRadius: 10,
                              ),
                            ],
                          ),
                        ),
                        CustomPaint(
                          size: const Size(160, 160),
                          painter: FluidHeartPainter(
                            wavePhase: _fluidWaveAnimation.value,
                            fillPercentage: 0.3,
                          ),
                        ),
                        Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: const [
                            Text(
                              '当前情绪值',
                              style: TextStyle(
                                fontSize: 10,
                                color: Color(0xFF585C62),
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                            Text(
                              '30%',
                              style: TextStyle(
                                fontSize: 26,
                                fontWeight: FontWeight.w900,
                                color: Color(0xFF2B2F35),
                                shadows: [
                                  Shadow(
                                    color: Colors.white,
                                    blurRadius: 10,
                                  )
                                ],
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ),
              );
            },
          ),'''

    content = re.sub(r'          AnimatedBuilder\([\s\S]*?\}\s*,\s*\)\s*;\s*\}\s*,\s*\),', fixed_block, content)

    with open('lib/screens/home_screen.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

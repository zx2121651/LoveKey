import re

def main():
    with open('lib/screens/home_screen.dart', 'r') as f:
        content = f.read()

    # The missing parenthesis is for Transform.translate or Transform.scale or SizedBox
    # Look at line 134 to 200
    # return Transform.translate(
    #   offset: Offset(0, _heartHoverAnimation.value),
    #   child: Transform.scale(
    #     scale: _heartScaleAnimation.value,
    #     child: SizedBox(
    #       width: 160,
    #       height: 160,
    #     child: Stack(
    #       alignment: Alignment.center,
    #       children: [
    #         // ...
    #         ],
    #       ),
    #     ),
    #   );

    # Wait, `child: SizedBox(` has no closing parenthesis before `);`.
    # Let's replace the end of the `AnimatedBuilder` return block.

    pattern = r'(\s*\]\s*,\s*\n\s*\)\s*,\n\s*\)\s*,\n\s*\)\s*;\n\s*\}\s*,\n\s*\),)'

    # The current end is:
    #                       ],
    #                     ),
    #                   ],
    #                 ),
    #               ),
    #             );
    #           },
    #         ),

    replacement = r'''                      ],
                    ),
                  ),
                ),
              );
            },
          ),'''

    # It seems we need ONE MORE `),` for the `SizedBox`.

    content = content.replace(
        "                      ],\n                    ),\n                  ),\n                ),\n              );\n            },\n          ),",
        "                      ],\n                    ),\n                  ),\n                ),\n              );\n            },\n          ),"
    ) # Still not right

    # Let's just fix it by replacing the whole `return Transform.translate(...)` block again

    fixed_block = '''              return Transform.translate(
                offset: Offset(0, _heartHoverAnimation.value),
                child: Transform.scale(
                  scale: _heartScaleAnimation.value,
                  child: SizedBox(
                    width: 160,
                    height: 160,
                    child: Stack(
                      alignment: Alignment.center,
                      children: [
                        // Glow effect behind the heart
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
                        // The Fluid Heart Custom Paint
                        CustomPaint(
                          size: const Size(160, 160),
                          painter: FluidHeartPainter(
                            wavePhase: _fluidWaveAnimation.value,
                            fillPercentage: 0.3, // 30% capacity
                          ),
                        ),
                        // Text inside/over the heart
                        Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            const Text(
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
              );'''

    content = re.sub(
        r'              return Transform\.translate\([\s\S]*?\}\s*,\s*\)\s*;\s*\}\s*,\s*\),',
        fixed_block + '\n            },\n          ),',
        content
    )

    with open('lib/screens/home_screen.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

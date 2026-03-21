import re

def main():
    with open('lib/screens/home_screen.dart', 'r') as f:
        content = f.read()

    # The previous script might not have perfectly nested the Transform.translate
    # Let's fix the AnimatedBuilder return block manually to ensure correct syntax
    # Search for: return Transform.translate(

    if "child: Transform.scale(" in content:
        # Let's use a very specific replace
        bad_block = '''              return Transform.translate(
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
                                color: const Color(0xFF2B2F35),
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

        # Actually it looks like `Transform.scale` didn't have its closing parenthesis missing in the previous step if it just replaced `return Transform.scale(` with `return Transform.translate(... child: Transform.scale(`
        # Wait, if we replaced `return Transform.scale(...)` with `return Transform.translate(... child: Transform.scale(...)`, then we missed a closing parenthesis at the very end of the return statement.

        fixed_block = bad_block.replace(");\n              );", ");") # Ensure no double parenthesis if already there
        # We need exactly 2 closing parenthesis at the end for the two Transforms
        # Let's just rewrite the whole return block cleanly
        clean_block = '''              return Transform.translate(
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
                                color: const Color(0xFF2B2F35),
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

        # Use regex to replace the entire builder body
        content = re.sub(
            r'              return Transform\.translate\([\s\S]*?\}\s*,\s*\)\s*;\s*\}\s*,',
            clean_block + '\n            },\n          ),',
            content
        )

    with open('lib/screens/home_screen.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

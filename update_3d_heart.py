import re

def main():
    with open('lib/screens/home_screen.dart', 'r') as f:
        content = f.read()

    # 1. Update Animation Controller to add hovering (Y-axis translation)
    # Right now we have `_heartScaleAnimation` and `_fluidWaveAnimation`.
    # Let's add `_heartHoverAnimation`.

    if "late Animation<double> _heartHoverAnimation;" not in content:
        content = content.replace(
            "late Animation<double> _fluidWaveAnimation;",
            "late Animation<double> _fluidWaveAnimation;\n  late Animation<double> _heartHoverAnimation;"
        )
        content = content.replace(
            "// 2. Fluid Wave Animation",
            "// 2. Fluid Wave Animation\n    _heartHoverAnimation = Tween<double>(begin: -10.0, end: 10.0).animate(\n      CurvedAnimation(parent: _heartAnimationController, curve: Curves.easeInOutSine),\n    );\n"
        )

    # 2. Add Transform.translate to the AnimatedBuilder
    content = content.replace(
        "return Transform.scale(\n                scale: _heartScaleAnimation.value,\n                child: SizedBox(",
        "return Transform.translate(\n                offset: Offset(0, _heartHoverAnimation.value),\n                child: Transform.scale(\n                  scale: _heartScaleAnimation.value,\n                  child: SizedBox("
    )
    # Ensure closing parenthesis matches. We replaced "return Transform.scale(... child: SizedBox(" with the new string
    # We need to add one more closing parenthesis at the end of the AnimatedBuilder block, but it's easier to just do it via regex on the whole block.
    # Wait, simple replace is safer if we just wrap the SizedBox.

    content = content.replace(
        "child: SizedBox(\n                  width: 120,\n                  height: 120,",
        "child: SizedBox(\n                    width: 160,\n                    height: 160,"
    )

    content = content.replace(
        "size: const Size(120, 120),",
        "size: const Size(160, 160),"
    )

    # 3. Rewrite FluidHeartPainter to look like an extremely premium 3D Glass Heart
    # The previous one just had an outline. We need to fill it with translucent white gradients and specular highlights.

    new_painter = '''
class FluidHeartPainter extends CustomPainter {
  final double wavePhase;
  final double fillPercentage;

  FluidHeartPainter({required this.wavePhase, required this.fillPercentage});

  @override
  void paint(Canvas canvas, Size size) {
    final double width = size.width;
    final double height = size.height;

    // 1. Create Heart Path (standardized cubic bezier heart)
    Path heartPath = Path();
    heartPath.moveTo(width / 2, height / 5);
    heartPath.cubicTo(5 * width / 14, 0, 0, height / 15, width / 28, 2 * height / 5);
    heartPath.cubicTo(width / 14, 2 * height / 3, 3 * width / 7, 5 * height / 6, width / 2, height);
    heartPath.cubicTo(4 * width / 7, 5 * height / 6, 13 * width / 14, 2 * height / 3, 27 * width / 28, 2 * height / 5);
    heartPath.cubicTo(width, height / 15, 9 * width / 14, 0, width / 2, height / 5);
    heartPath.close();

    // 2. Draw 3D Glass Body (Back Layer)
    // A soft radial gradient to simulate the translucent frosted glass volume
    Paint glassBodyPaint = Paint()
      ..shader = ui.Gradient.radial(
        Offset(width / 3, height / 3),
        width * 0.8,
        [
          const Color(0xFFFFFFFF).withValues(alpha: 0.9), // Bright highlight near top-left
          const Color(0xFFF4F6FE).withValues(alpha: 0.4), // Frosty blue in the middle
          const Color(0xFF586AFE).withValues(alpha: 0.1), // Deep blue shadow at the edges
        ],
        [0.0, 0.5, 1.0],
      )
      ..style = PaintingStyle.fill;
    canvas.drawPath(heartPath, glassBodyPaint);

    // 3. Create Fluid Wave Paths (The Pink Liquid)
    // Calculate water level
    double waterLevel = height * (1 - fillPercentage);

    Path fluidPath = Path();
    fluidPath.moveTo(0, height);
    fluidPath.lineTo(0, waterLevel);

    double waveHeight = 8.0;
    double waveLength = width;

    // Front Wave
    for (double i = 0; i <= width; i++) {
      double waveY = waterLevel + math.sin((i / waveLength * 2 * math.pi) + wavePhase) * waveHeight;
      fluidPath.lineTo(i, waveY);
    }
    fluidPath.lineTo(width, height);
    fluidPath.close();

    // Back Wave (Slightly offset and lighter for depth)
    Path fluidPathBack = Path();
    fluidPathBack.moveTo(0, height);
    fluidPathBack.lineTo(0, waterLevel);
    for (double i = 0; i <= width; i++) {
      double waveY = waterLevel + math.sin((i / waveLength * 2 * math.pi) + wavePhase + math.pi) * waveHeight * 0.6;
      fluidPathBack.lineTo(i, waveY);
    }
    fluidPathBack.lineTo(width, height);
    fluidPathBack.close();

    // 4. Clip to heart shape and draw fluids
    canvas.save();
    canvas.clipPath(heartPath);

    // Back wave paint (Lighter, softer pink)
    Paint fluidPaintBack = Paint()
      ..color = const Color(0xFFFFB3C6).withValues(alpha: 0.8)
      ..style = PaintingStyle.fill;
    canvas.drawPath(fluidPathBack, fluidPaintBack);

    // Front wave paint (Vibrant jelly pink gradient)
    Paint fluidPaintFront = Paint()
      ..shader = ui.Gradient.linear(
        Offset(0, waterLevel),
        Offset(0, height),
        [
          const Color(0xFFFF85A2), // Top of liquid
          const Color(0xFFFF4D85), // Bottom deep pink
        ],
      )
      ..style = PaintingStyle.fill;
    canvas.drawPath(fluidPath, fluidPaintFront);

    // Add a subtle inner shadow/glow inside the liquid to make it look 3D
    Paint liquidGlowPaint = Paint()
      ..shader = ui.Gradient.radial(
        Offset(width / 2, height),
        width / 2,
        [
          const Color(0xFFFFFFFF).withValues(alpha: 0.3),
          const Color(0x00FFFFFF),
        ],
      )
      ..blendMode = BlendMode.screen;
    canvas.drawPath(fluidPath, liquidGlowPaint);

    canvas.restore(); // Remove clipping so we can draw outer highlights over everything

    // 5. Draw 3D Specular Highlights (The Glass Reflections)
    // Primary curved highlight on the top left lobe
    Path highlightPath = Path();
    highlightPath.moveTo(width * 0.15, height * 0.35);
    highlightPath.quadraticBezierTo(width * 0.15, height * 0.15, width * 0.35, height * 0.15);
    highlightPath.quadraticBezierTo(width * 0.25, height * 0.2, width * 0.2, height * 0.35);
    highlightPath.close();

    Paint highlightPaint = Paint()
      ..shader = ui.Gradient.linear(
        Offset(width * 0.15, height * 0.15),
        Offset(width * 0.35, height * 0.35),
        [
          const Color(0xFFFFFFFF).withValues(alpha: 0.9),
          const Color(0xFFFFFFFF).withValues(alpha: 0.0),
        ],
      )
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 2.0);
    canvas.drawPath(highlightPath, highlightPaint);

    // Secondary rim light on the right edge
    Path rimLightPath = Path();
    rimLightPath.moveTo(width * 0.9, height * 0.4);
    rimLightPath.quadraticBezierTo(width * 0.95, height * 0.6, width * 0.85, height * 0.7);

    Paint rimLightPaint = Paint()
      ..color = const Color(0xFFFFFFFF).withValues(alpha: 0.6)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3.0
      ..strokeCap = StrokeCap.round
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 3.0);
    canvas.drawPath(rimLightPath, rimLightPaint);

    // 6. Draw Outer Glass Shell (Smooth translucent outline)
    Paint glassOutlinePaint = Paint()
      ..color = const Color(0xFFFFFFFF).withValues(alpha: 0.6)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.0;
    canvas.drawPath(heartPath, glassOutlinePaint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) {
    return true; // Repaint every frame for fluid and hover animations
  }
}
'''

    # Replace the old painter with the new 3D one
    old_painter_regex = r'class FluidHeartPainter extends CustomPainter \{.*?\n\}\n'
    content = re.sub(old_painter_regex, new_painter, content, flags=re.DOTALL)

    # We also need to fix the AnimatedBuilder closing tags if we changed "return Transform.scale"
    # Let's verify we replaced the Transform correctly.
    if "offset: Offset(0, _heartHoverAnimation.value)," in content and "child: Transform.translate(" not in content:
        # Wait, the replace string was:
        # "return Transform.translate(\n                offset: Offset(0, _heartHoverAnimation.value),\n                child: Transform.scale("
        # We need an extra closing parenthesis for the Transform.translate
        # Find where the AnimatedBuilder returns
        content = re.sub(
            r'(return Transform\.translate\(\n\s*offset: Offset\(0, _heartHoverAnimation\.value\),\n\s*child: Transform\.scale\(\n\s*scale: _heartScaleAnimation\.value,\n\s*child: SizedBox\(.*?\}\n\s*\),.*?\n\s*\);\n\s*\},)',
            lambda m: m.group(1).replace(");", ");\n              );"),
            content,
            flags=re.DOTALL
        )

        # A safer way is to just do a strict replace on the specific blocks
        pass

    with open('lib/screens/home_screen.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

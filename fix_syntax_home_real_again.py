import re

def main():
    with open('lib/screens/home_screen.dart', 'r') as f:
        content = f.read()

    # Apply ONLY the painter replacement
    new_painter = '''class FluidHeartPainter extends CustomPainter {
  final double wavePhase;
  final double fillPercentage;

  FluidHeartPainter({required this.wavePhase, required this.fillPercentage});

  @override
  void paint(Canvas canvas, Size size) {
    final double width = size.width;
    final double height = size.height;

    Path heartPath = Path();
    heartPath.moveTo(width / 2, height / 5);
    heartPath.cubicTo(5 * width / 14, 0, 0, height / 15, width / 28, 2 * height / 5);
    heartPath.cubicTo(width / 14, 2 * height / 3, 3 * width / 7, 5 * height / 6, width / 2, height);
    heartPath.cubicTo(4 * width / 7, 5 * height / 6, 13 * width / 14, 2 * height / 3, 27 * width / 28, 2 * height / 5);
    heartPath.cubicTo(width, height / 15, 9 * width / 14, 0, width / 2, height / 5);
    heartPath.close();

    Paint glassBodyPaint = Paint()
      ..shader = ui.Gradient.radial(
        Offset(width / 3, height / 3),
        width * 0.8,
        [
          const Color(0xFFFFFFFF).withValues(alpha: 0.9),
          const Color(0xFFF4F6FE).withValues(alpha: 0.4),
          const Color(0xFF586AFE).withValues(alpha: 0.1),
        ],
        [0.0, 0.5, 1.0],
      )
      ..style = PaintingStyle.fill;
    canvas.drawPath(heartPath, glassBodyPaint);

    double waterLevel = height * (1 - fillPercentage);

    Path fluidPath = Path();
    fluidPath.moveTo(0, height);
    fluidPath.lineTo(0, waterLevel);

    double waveHeight = 8.0;
    double waveLength = width;

    for (double i = 0; i <= width; i++) {
      double waveY = waterLevel + math.sin((i / waveLength * 2 * math.pi) + wavePhase) * waveHeight;
      fluidPath.lineTo(i, waveY);
    }
    fluidPath.lineTo(width, height);
    fluidPath.close();

    Path fluidPathBack = Path();
    fluidPathBack.moveTo(0, height);
    fluidPathBack.lineTo(0, waterLevel);
    for (double i = 0; i <= width; i++) {
      double waveY = waterLevel + math.sin((i / waveLength * 2 * math.pi) + wavePhase + math.pi) * waveHeight * 0.6;
      fluidPathBack.lineTo(i, waveY);
    }
    fluidPathBack.lineTo(width, height);
    fluidPathBack.close();

    canvas.save();
    canvas.clipPath(heartPath);

    Paint fluidPaintBack = Paint()
      ..color = const Color(0xFFFFB3C6).withValues(alpha: 0.8)
      ..style = PaintingStyle.fill;
    canvas.drawPath(fluidPathBack, fluidPaintBack);

    Paint fluidPaintFront = Paint()
      ..shader = ui.Gradient.linear(
        Offset(0, waterLevel),
        Offset(0, height),
        [
          const Color(0xFFFF85A2),
          const Color(0xFFFF4D85),
        ],
      )
      ..style = PaintingStyle.fill;
    canvas.drawPath(fluidPath, fluidPaintFront);

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

    canvas.restore();

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

    Paint glassOutlinePaint = Paint()
      ..color = const Color(0xFFFFFFFF).withValues(alpha: 0.6)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.0;
    canvas.drawPath(heartPath, glassOutlinePaint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) {
    return true;
  }
}'''

    content = re.sub(r'class FluidHeartPainter extends CustomPainter \{.*?\n\}\n', new_painter + '\n', content, flags=re.DOTALL)

    # Re-apply the _heartHoverAnimation and size changes only. DO NOT mess with AnimatedBuilder's tree structure.

    if "late Animation<double> _heartHoverAnimation;" not in content:
        content = content.replace(
            "late Animation<double> _fluidWaveAnimation;",
            "late Animation<double> _fluidWaveAnimation;\n  late Animation<double> _heartHoverAnimation;"
        )
        content = content.replace(
            "// 2. Fluid Wave Animation",
            "// 2. Fluid Wave Animation\n    _heartHoverAnimation = Tween<double>(begin: -10.0, end: 10.0).animate(\n      CurvedAnimation(parent: _heartAnimationController, curve: Curves.easeInOutSine),\n    );\n"
        )

        # Modify the single line returning Transform.scale
        content = content.replace(
            "return Transform.scale(",
            "return Transform.translate(offset: Offset(0, _heartHoverAnimation.value), child: Transform.scale("
        )

        # Now we need ONE closing parenthesis for the new Transform.translate.
        # Find where it ends: it ends at: `);\n            },\n          ),`
        # Let's replace:
        content = re.sub(
            r'(\s*\]\s*,\s*\n\s*\)\s*,\n\s*\)\s*,\n\s*\)\s*;\n\s*\}\s*,\n\s*\),)',
            r'\1'.replace(");", ");\n              );"),
            content
        )

        # Increase size
        content = content.replace("width: 120,\n                  height: 120,", "width: 160,\n                  height: 160,")
        content = content.replace("size: const Size(120, 120),", "size: const Size(160, 160),")

    with open('lib/screens/home_screen.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

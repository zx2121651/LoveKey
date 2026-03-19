import re

def main():
    with open('lib/screens/home_screen.dart', 'r') as f:
        content = f.read()

    # 1. Update Imports and Class properties
    content = content.replace(
        '''  late Animation<double> _heartScaleAnimation;
  late Animation<double> _heartRotationAnimation;''',
        '''  late Animation<double> _heartScaleAnimation;
  late Animation<double> _fluidWaveAnimation;'''
    )

    # 2. Update initState
    content = content.replace(
        '''    // 2. 3D Rotation Animation (Y-axis flip, like a coin spinning)
    // We animate from 0 to Pi (180 degrees)
    _heartRotationAnimation = Tween<double>(begin: 0.0, end: math.pi).animate(
      CurvedAnimation(parent: _heartAnimationController, curve: Curves.easeInOutSine),
    );''',
        '''    // 2. Fluid Wave Animation
    _fluidWaveAnimation = Tween<double>(begin: 0.0, end: 2 * math.pi).animate(
      CurvedAnimation(parent: _heartAnimationController, curve: Curves.linear),
    );'''
    )

    # 3. Update build background
    content = content.replace(
        '''  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,''',
        '''  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0C0916), // Dark Mode Cyber background'''
    )

    # 4. Update Header (Top section with Heart)
    new_header = '''  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 30),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Lovekey',
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                  letterSpacing: 1.2,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'AI恋爱辅助输入法',
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.white.withOpacity(0.7),
                ),
              ),
              const SizedBox(height: 12),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: const Color(0xFFFF2E54).withOpacity(0.2),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: const Color(0xFFFF2E54).withOpacity(0.5)),
                ),
                child: const Text(
                  '你们的情绪共鸣正在升温',
                  style: TextStyle(
                    fontSize: 12,
                    color: Color(0xFFFF2E54),
                  ),
                ),
              ),
            ],
          ),
          // Fluid 3D Heart Animation (30% capacity)
          AnimatedBuilder(
            animation: _heartAnimationController,
            builder: (context, child) {
              return Transform.scale(
                scale: _heartScaleAnimation.value,
                child: SizedBox(
                  width: 120,
                  height: 120,
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
                              color: const Color(0xFFFF2E54).withOpacity(0.4),
                              blurRadius: 30,
                              spreadRadius: 10,
                            ),
                          ],
                        ),
                      ),
                      // The Fluid Heart Custom Paint
                      CustomPaint(
                        size: const Size(120, 120),
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
                              color: Colors.white70,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                          Text(
                            '30%',
                            style: TextStyle(
                              fontSize: 26,
                              fontWeight: FontWeight.w900,
                              color: Colors.white,
                              shadows: [
                                Shadow(
                                  color: const Color(0xFFFF2E54).withOpacity(0.8),
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
              );
            },
          ),
        ],
      ),
    );
  }'''

    # Use regex to replace the old _buildHeader method
    content = re.sub(r'  Widget _buildHeader\(\) \{.*?(?=  Widget _buildSearchBar)', new_header + '\n\n', content, flags=re.DOTALL)


    # 5. Update Feature Cards
    new_feature_cards = '''  Widget _buildFeatureCards(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      child: Column(
        children: [
          Row(
            children: [
              Expanded(
                child: _buildCyberCard(
                  context,
                  '键盘人设',
                  '幽默 / 高冷',
                  Icons.psychology,
                ),
              ),
              const SizedBox(width: 15),
              Expanded(
                child: _buildCyberCard(
                  context,
                  '话术库',
                  '如何高情商回复...',
                  Icons.menu_book,
                ),
              ),
            ],
          ),
          const SizedBox(height: 15),
          _buildCyberCard(
            context,
            '恋爱咨询室',
            'AI情感导师24H在线',
            Icons.favorite_border,
            isLarge: true,
            isCounselor: true,
          ),
        ],
      ),
    );
  }

  Widget _buildCyberCard(BuildContext context, String title, String subtitle, IconData icon, {bool isLarge = false, bool isCounselor = false}) {
    return GestureDetector(
      onTap: () {
        if (isCounselor) {
          Navigator.of(context).push(
            MaterialPageRoute(
              builder: (context) => const CounselorScreen(),
            ),
          );
        }
      },
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: const Color(0xFF1A1528).withOpacity(0.8),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: const Color(0xFFFF2E54).withOpacity(0.3),
            width: 1,
          ),
          boxShadow: [
            BoxShadow(
              color: const Color(0xFFFF2E54).withOpacity(0.05),
              blurRadius: 10,
              spreadRadius: 2,
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Icon(icon, color: const Color(0xFFFF2E54), size: isLarge ? 32 : 24),
                if (isLarge)
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: const Color(0xFFFF2E54),
                      borderRadius: BorderRadius.circular(20),
                      boxShadow: [
                        BoxShadow(
                          color: const Color(0xFFFF2E54).withOpacity(0.5),
                          blurRadius: 8,
                        )
                      ],
                    ),
                    child: const Text(
                      '开始咨询',
                      style: TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold),
                    ),
                  ),
              ],
            ),
            SizedBox(height: isLarge ? 16 : 12),
            Text(
              title,
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 16,
                color: Colors.white,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              subtitle,
              style: TextStyle(
                fontSize: 12,
                color: Colors.white.withOpacity(0.6),
              ),
            ),
          ],
        ),
      ),
    );
  }'''

    # Replace _buildFeatureCards and _buildCard
    content = re.sub(r'  Widget _buildFeatureCards\(\) \{.*?(?=  Widget _buildMyKeyboardSection)', new_feature_cards + '\n\n', content, flags=re.DOTALL)
    content = re.sub(r'  Widget _buildFeatureCards\(BuildContext context\) \{.*?(?=  Widget _buildMyKeyboardSection)', new_feature_cards + '\n\n', content, flags=re.DOTALL)


    # 6. Make search bar cyber
    new_search_bar = '''  Widget _buildSearchBar(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(20),
      child: Container(
        decoration: BoxDecoration(
          color: const Color(0xFF1A1528),
          borderRadius: BorderRadius.circular(25),
          border: Border.all(color: Colors.white.withOpacity(0.1)),
        ),
        child: TextField(
          controller: _searchController,
          style: const TextStyle(color: Colors.white),
          decoration: InputDecoration(
            hintText: '输入对方发来的消息，AI帮你回...',
            hintStyle: TextStyle(color: Colors.white.withOpacity(0.4)),
            prefixIcon: Icon(Icons.search, color: Colors.white.withOpacity(0.4)),
            suffixIcon: IconButton(
              icon: const Icon(Icons.send, color: Color(0xFFFF2E54)),
              onPressed: () => _showGenerateDialog(context),
            ),
            border: InputBorder.none,
            contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 15),
          ),
          onSubmitted: (_) => _showGenerateDialog(context),
        ),
      ),
    );
  }'''
    content = re.sub(r'  Widget _buildSearchBar\(BuildContext context\) \{.*?(?=  Widget _buildFeatureCards)', new_search_bar + '\n\n', content, flags=re.DOTALL)


    # 7. Update bottom sheets and other colors to dark theme
    content = content.replace("backgroundColor: Colors.white,", "backgroundColor: const Color(0xFF0C0916),")
    content = content.replace("color: Colors.black87,", "color: Colors.white,")
    content = content.replace("color: Colors.black,", "color: Colors.white,")
    content = content.replace("color: const Color(0xFFF9FAFB),", "color: const Color(0xFF1A1528),")
    content = content.replace("border: Border.all(color: Colors.grey.shade200)", "border: Border.all(color: const Color(0xFFFF2E54).withOpacity(0.2))")

    # Add FluidHeartPainter class at the end
    fluid_painter = '''
class FluidHeartPainter extends CustomPainter {
  final double wavePhase;
  final double fillPercentage;

  FluidHeartPainter({required this.wavePhase, required this.fillPercentage});

  @override
  void paint(Canvas canvas, Size size) {
    final double width = size.width;
    final double height = size.height;

    // 1. Create Heart Path
    Path heartPath = Path();
    heartPath.moveTo(width / 2, height / 5);
    heartPath.cubicTo(5 * width / 14, 0, 0, height / 15, width / 28, 2 * height / 5);
    heartPath.cubicTo(width / 14, 2 * height / 3, 3 * width / 7, 5 * height / 6, width / 2, height);
    heartPath.cubicTo(4 * width / 7, 5 * height / 6, 13 * width / 14, 2 * height / 3, 27 * width / 28, 2 * height / 5);
    heartPath.cubicTo(width, height / 15, 9 * width / 14, 0, width / 2, height / 5);

    // 2. Draw glowing outline (empty part)
    Paint outlinePaint = Paint()
      ..color = const Color(0xFFFF2E54).withOpacity(0.3)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.0;
    canvas.drawPath(heartPath, outlinePaint);

    // Add a slight inner glow to the outline
    Paint innerGlowPaint = Paint()
      ..color = const Color(0xFFFF2E54).withOpacity(0.1)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 6.0
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 3);
    canvas.drawPath(heartPath, innerGlowPaint);

    // 3. Create Fluid Wave Path
    Path fluidPath = Path();

    // Calculate the water level based on percentage (0.0 to 1.0)
    // Since 0 is top and height is bottom, we invert the percentage
    double waterLevel = height * (1 - fillPercentage);

    fluidPath.moveTo(0, height);
    fluidPath.lineTo(0, waterLevel);

    // Sine wave parameters
    double waveHeight = 5.0;
    double waveLength = width;

    for (double i = 0; i <= width; i++) {
      double waveY = waterLevel + math.sin((i / waveLength * 2 * math.pi) + wavePhase) * waveHeight;
      fluidPath.lineTo(i, waveY);
    }

    fluidPath.lineTo(width, height);
    fluidPath.close();

    // 4. Clip the fluid to the heart shape
    canvas.clipPath(heartPath);

    // 5. Draw the fluid
    Paint fluidPaint = Paint()
      ..shader = ui.Gradient.linear(
        Offset(0, waterLevel),
        Offset(0, height),
        [
          const Color(0xFFFF2E54), // Neon Pink
          const Color(0xFF9B2EFF), // Neon Purple
        ],
      );

    canvas.drawPath(fluidPath, fluidPaint);

    // Add a secondary wave for depth
    Path fluidPath2 = Path();
    fluidPath2.moveTo(0, height);
    fluidPath2.lineTo(0, waterLevel);
    for (double i = 0; i <= width; i++) {
      double waveY = waterLevel + math.sin((i / waveLength * 2 * math.pi) + wavePhase + math.pi) * waveHeight * 0.8;
      fluidPath2.lineTo(i, waveY);
    }
    fluidPath2.lineTo(width, height);
    fluidPath2.close();

    Paint fluidPaint2 = Paint()
      ..color = const Color(0xFFFF2E54).withOpacity(0.5);
    canvas.drawPath(fluidPath2, fluidPaint2);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) {
    return true; // Repaint every frame for animation
  }
}
'''

    # Add dart:ui import if not present
    if "import 'dart:ui' as ui;" not in content:
        content = content.replace("import 'dart:math' as math;", "import 'dart:math' as math;\nimport 'dart:ui' as ui;")

    # Append the class
    content += fluid_painter

    with open('lib/screens/home_screen.dart', 'w') as f:
        f.write(content)

if __name__ == '__main__':
    main()

import 'package:flutter/material.dart';
import 'counselor_screen.dart';

import 'package:flutter/services.dart';
import 'dart:math' as math;
import 'dart:ui' as ui;

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with TickerProviderStateMixin {
  final TextEditingController _searchController = TextEditingController();
  late AnimationController _heartAnimationController;
  late Animation<double> _heartScaleAnimation;
  late Animation<double> _fluidWaveAnimation;

  @override
  void initState() {
    super.initState();

    // Heartbeat & Rotation Controller
    _heartAnimationController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1500),
    )..repeat(reverse: true); // repeats endlessly

    // 1. Scale Animation (2D heart beat)
    _heartScaleAnimation = Tween<double>(begin: 1.0, end: 1.15).animate(
      CurvedAnimation(parent: _heartAnimationController, curve: Curves.easeInOutCubic),
    );

    // 2. Fluid Wave Animation
    _fluidWaveAnimation = Tween<double>(begin: 0.0, end: 2 * math.pi).animate(
      CurvedAnimation(parent: _heartAnimationController, curve: Curves.linear),
    );
  }

  @override
  void dispose() {
    _heartAnimationController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  void _showGenerateDialog(BuildContext context) {
    if (_searchController.text.trim().isEmpty) return;

    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (context) => _BuildGenerateResultSheet(query: _searchController.text),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0C0916), // Dark Mode Cyber background
      body: SafeArea(
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildHeader(),
              _buildSearchBar(context),
              _buildFeatureCards(context),
              _buildMyKeyboardSection(),
              _buildLoveQuotesSection(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeader() {
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
                  color: Colors.white.withValues(alpha: 0.7),
                ),
              ),
              const SizedBox(height: 12),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: const Color(0xFFFF2E54).withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.5)),
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
                              color: const Color(0xFFFF2E54).withValues(alpha: 0.4),
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
                                  color: const Color(0xFFFF2E54).withValues(alpha: 0.8),
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
  }

  Widget _buildSearchBar(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(20),
      child: Container(
        decoration: BoxDecoration(
          color: const Color(0xFF1A1528),
          borderRadius: BorderRadius.circular(25),
          border: Border.all(color: Colors.white.withValues(alpha: 0.1)),
        ),
        child: TextField(
          controller: _searchController,
          style: const TextStyle(color: Colors.white),
          decoration: InputDecoration(
            hintText: '输入对方发来的消息，AI帮你回...',
            hintStyle: TextStyle(color: Colors.white.withValues(alpha: 0.4)),
            prefixIcon: Icon(Icons.search, color: Colors.white.withValues(alpha: 0.4)),
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
  }

  Widget _buildFeatureCards(BuildContext context) {
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
          color: const Color(0xFF1A1528).withValues(alpha: 0.8),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: const Color(0xFFFF2E54).withValues(alpha: 0.3),
            width: 1,
          ),
          boxShadow: [
            BoxShadow(
              color: const Color(0xFFFF2E54).withValues(alpha: 0.05),
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
                          color: const Color(0xFFFF2E54).withValues(alpha: 0.5),
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
                color: Colors.white.withValues(alpha: 0.6),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMyKeyboardSection() {
    final tags = [
      {'label': '1. 高情商', 'color': Colors.pink},
      {'label': '2. 撩女生', 'color': Colors.purple},
      {'label': '3. 情场高手', 'color': Colors.blue},
      {'label': '4. 暧昧拉扯', 'color': Colors.purpleAccent},
      {'label': '5. 幽默', 'color': Colors.orange},
      {'label': '6. 夸夸 TA', 'color': Colors.teal},
      {'label': '7. 贴心暖男', 'color': Colors.orangeAccent},
      {'label': '8. 风流浪子', 'color': Colors.redAccent},
      {'label': '9. 逗比', 'color': Colors.deepPurple},
    ];

    return Padding(
      padding: const EdgeInsets.all(20),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                '我的键盘',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
              ),
              Row(
                children: [
                  Text('更多键盘', style: TextStyle(color: Colors.grey[600], fontSize: 12)),
                  Icon(Icons.chevron_right, size: 16, color: Colors.grey[600]),
                ],
              )
            ],
          ),
          const SizedBox(height: 15),
          Wrap(
            spacing: 10,
            runSpacing: 10,
            children: tags.map((tag) {
              return Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                decoration: BoxDecoration(
                  color: Colors.grey[50],
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: Colors.grey[200]!),
                ),
                child: Text(
                  tag['label'] as String,
                  style: TextStyle(
                    color: tag['color'] as Color,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              );
            }).toList(),
          ),
        ],
      ),
    );
  }

  Widget _buildLoveQuotesSection() {
    return Padding(
      padding: const EdgeInsets.all(20),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                '土味情话',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
              ),
              Row(
                children: [
                  Text('查看更多', style: TextStyle(color: Colors.grey[600], fontSize: 12)),
                  Icon(Icons.chevron_right, size: 16, color: Colors.grey[600]),
                ],
              )
            ],
          ),
          const SizedBox(height: 15),
          Container(
            padding: const EdgeInsets.all(15),
            decoration: BoxDecoration(
              color: const Color(0xFFF8F9FA),
              borderRadius: BorderRadius.circular(15),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(4),
                      decoration: const BoxDecoration(
                        color: Colors.white,
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(Icons.male, size: 12, color: Colors.white),
                    ),
                    const SizedBox(width: 8),
                    const Text('你的脸上有点东西。'),
                  ],
                ),
                const SizedBox(height: 10),
                Align(
                  alignment: Alignment.centerRight,
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 15, vertical: 10),
                    decoration: BoxDecoration(
                      color: const Color(0xFFEBF0FF),
                      borderRadius: BorderRadius.circular(15),
                    ),
                    child: const Text('有点帅。'),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 40), // Bottom padding
        ],
      ),
    );
  }
}

class _BuildGenerateResultSheet extends StatefulWidget {
  final String query;
  const _BuildGenerateResultSheet({required this.query});

  @override
  State<_BuildGenerateResultSheet> createState() => _BuildGenerateResultSheetState();
}

class _BuildGenerateResultSheetState extends State<_BuildGenerateResultSheet> {
  bool _isLoading = true;
  final List<String> _mockReplies = [
    '这是我听过最有趣的想法了，你真有意思！',
    '哈哈，你这么说我会骄傲的哦～',
    '那你想不想知道我是怎么想的？😏',
  ];

  @override
  void initState() {
    super.initState();
    Future.delayed(const Duration(seconds: 2), () {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    });
  }

  void _copyToClipboard(String text) {
    Clipboard.setData(ClipboardData(text: text)).then((_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('已复制到剪贴板，快去回复吧！')),
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      height: MediaQuery.of(context).size.height * 0.6,
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.only(
          topLeft: Radius.circular(20),
          topRight: Radius.circular(20),
        ),
      ),
      child: Column(
        children: [
          Container(
            height: 4,
            width: 40,
            margin: const EdgeInsets.symmetric(vertical: 12),
            decoration: BoxDecoration(
              color: Colors.grey[300],
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          const Text('AI 高情商回复', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
          const SizedBox(height: 20),
          Expanded(
            child: _isLoading
                ? const Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        CircularProgressIndicator(),
                        SizedBox(height: 16),
                        Text('AI 正在为你生成完美的回复...', style: TextStyle(color: Colors.grey)),
                      ],
                    ),
                  )
                : ListView.builder(
                    padding: const EdgeInsets.all(20),
                    itemCount: _mockReplies.length,
                    itemBuilder: (context, index) {
                      return Container(
                        margin: const EdgeInsets.only(bottom: 16),
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: const Color(0xFF1A1528),
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(color: const Color(0xFFFF2E54).withValues(alpha: 0.2)),
                        ),
                        child: Row(
                          children: [
                            Expanded(
                              child: Text(_mockReplies[index], style: const TextStyle(fontSize: 14)),
                            ),
                            IconButton(
                              icon: const Icon(Icons.copy, color: Colors.pink, size: 20),
                              onPressed: () => _copyToClipboard(_mockReplies[index]),
                            ),
                          ],
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}

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
      ..color = const Color(0xFFFF2E54).withValues(alpha: 0.3)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.0;
    canvas.drawPath(heartPath, outlinePaint);

    // Add a slight inner glow to the outline
    Paint innerGlowPaint = Paint()
      ..color = const Color(0xFFFF2E54).withValues(alpha: 0.1)
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
      ..color = const Color(0xFFFF2E54).withValues(alpha: 0.5);
    canvas.drawPath(fluidPath2, fluidPaint2);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) {
    return true; // Repaint every frame for animation
  }
}

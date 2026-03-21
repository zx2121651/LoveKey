import 'package:flutter/material.dart';
import 'counselor_screen.dart';

import 'package:flutter/services.dart';
import 'dart:math' as math;

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with TickerProviderStateMixin {
  final TextEditingController _searchController = TextEditingController();
  late AnimationController _heartAnimationController;
  late Animation<double> _heartScaleAnimation;
  late Animation<double> _heartRotationAnimation;

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
      CurvedAnimation(
        parent: _heartAnimationController,
        curve: Curves.easeInOutCubic,
      ),
    );

    // 2. 3D Rotation Animation (Y-axis flip, like a coin spinning)
    // We animate from 0 to Pi (180 degrees)
    _heartRotationAnimation = Tween<double>(begin: 0.0, end: math.pi).animate(
      CurvedAnimation(
        parent: _heartAnimationController,
        curve: Curves.easeInOutSine,
      ),
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
      builder: (context) =>
          _BuildGenerateResultSheet(query: _searchController.text),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
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
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20),
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            Color(0xFFE6FFEA), // Light green-ish
            Color(0xFFFFF0F5), // Light pink-ish
          ],
        ),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'AI恋爱键盘',
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                  color: Colors.black87,
                ),
              ),
              const SizedBox(height: 8),
              const Text(
                'AI帮你撩，脱单没烦恼',
                style: TextStyle(fontSize: 14, color: Colors.black54),
              ),
              const SizedBox(height: 4),
              Text(
                'love',
                style: TextStyle(
                  fontSize: 24,
                  fontFamily:
                      'cursive', // Assuming a cursive font would be used
                  color: Color(0xFF8A9CFF),
                ),
              ),
            ],
          ),
          // Beating 3D Emotion Value Coin flip animation
          AnimatedBuilder(
            animation: _heartAnimationController,
            builder: (context, child) {
              final angle = _heartRotationAnimation.value;
              final isFront = angle <= math.pi / 2;

              // Create a 3D perspective matrix
              final transform = Matrix4.identity()
                ..setEntry(3, 2, 0.0015) // depth perspective
                ..rotateY(angle); // flip on Y axis

              // Scale matrix
              transform.scale(
                _heartScaleAnimation.value,
                _heartScaleAnimation.value,
              );

              return Transform(
                transform: transform,
                alignment: Alignment.center,
                child: Container(
                  width: 120,
                  height: 120,
                  child: isFront
                      ? CustomPaint(
                          painter: FluidHeartPainter(_heartAnimationController.value),
                        )
                      : Transform(
                          transform: Matrix4.rotationY(math.pi),
                          alignment: Alignment.center,
                          child: Container(
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              color: Colors.white,
                              boxShadow: [
                                BoxShadow(
                                  color: const Color(0xFF586AFE).withValues(alpha: 0.3),
                                  blurRadius: 15,
                                  spreadRadius: 2,
                                ),
                              ],
                            ),
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                const Text(
                                  '99%',
                                  style: TextStyle(
                                    fontSize: 24,
                                    fontWeight: FontWeight.bold,
                                    color: Color(0xFF586AFE),
                                  ),
                                ),
                                const Text(
                                  '心动值',
                                  style: TextStyle(
                                    fontSize: 12,
                                    color: Color(0xFF8A9CFF),
                                  ),
                                ),
                              ],
                            ),
                          ),
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
          color: Colors.white,
          borderRadius: BorderRadius.circular(30),
          boxShadow: [
            BoxShadow(
              color: Colors.grey.withValues(alpha: 0.2),
              spreadRadius: 2,
              blurRadius: 10,
              offset: const Offset(0, 3),
            ),
          ],
        ),
        child: TextField(
          controller: _searchController,
          textInputAction: TextInputAction.search,
          onSubmitted: (_) => _showGenerateDialog(context),
          decoration: InputDecoration(
            hintText: '输入 TA 说的话，获得高情商回复',
            hintStyle: TextStyle(color: Colors.grey[400], fontSize: 14),
            border: InputBorder.none,
            contentPadding: const EdgeInsets.symmetric(
              horizontal: 20,
              vertical: 15,
            ),
            suffixIcon: Padding(
              padding: const EdgeInsets.all(4.0),
              child: ElevatedButton(
                onPressed: () => _showGenerateDialog(context),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.black87,
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(25),
                  ),
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                ),
                child: const Text('搜索'),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildFeatureCards(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      child: GridView.count(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        crossAxisCount: 2,
        mainAxisSpacing: 15,
        crossAxisSpacing: 15,
        childAspectRatio: 1.8,
        children: [
          _buildCard(
            context,
            '恋爱键盘',
            '教你高情商聊天，不\n管怎么聊都有料',
            const Color(0xFFFFEBF0),
            Icons.keyboard,
          ),
          _buildCard(
            context,
            '情感导师',
            '24小时在线的情感\n专家',
            const Color(0xFFEBF7FF),
            Icons.support_agent,
            isCounselor: true,
          ),
          _buildCard(
            context,
            '话术生成器',
            '表达心意的魔法道具',
            const Color(0xFFEBF0FF),
            Icons.chat,
          ),
          _buildCard(
            context,
            '图片识人',
            '上传照片或聊天截图\nAI快速解析',
            const Color(0xFFF6EBFF),
            Icons.camera_alt,
          ),
        ],
      ),
    );
  }

  Widget _buildCard(
    BuildContext context,
    String title,
    String subtitle,
    Color bgColor,
    IconData icon, {
    bool isCounselor = false,
  }) {
    return GestureDetector(
      onTap: () {
        if (isCounselor) {
          Navigator.of(context).push(
            MaterialPageRoute(builder: (context) => const CounselorScreen()),
          );
        }
      },
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: bgColor,
          borderRadius: BorderRadius.circular(15),
        ),
        child: Stack(
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 16,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  subtitle,
                  style: TextStyle(fontSize: 10, color: Colors.grey[700]),
                ),
              ],
            ),
            Positioned(
              right: 0,
              bottom: 0,
              child: Icon(icon, size: 40, color: Colors.black12),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMyKeyboardSection() {
    final tags = [
      {'label': '1. 高情商', 'color': Color(0xFF586AFE)},
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
                  Text(
                    '更多键盘',
                    style: TextStyle(color: Colors.grey[600], fontSize: 12),
                  ),
                  Icon(Icons.chevron_right, size: 16, color: Colors.grey[600]),
                ],
              ),
            ],
          ),
          const SizedBox(height: 15),
          Wrap(
            spacing: 10,
            runSpacing: 10,
            children: tags.map((tag) {
              return Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 8,
                ),
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
                  Text(
                    '查看更多',
                    style: TextStyle(color: Colors.grey[600], fontSize: 12),
                  ),
                  Icon(Icons.chevron_right, size: 16, color: Colors.grey[600]),
                ],
              ),
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
                        color: Colors.black,
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.male,
                        size: 12,
                        color: Colors.white,
                      ),
                    ),
                    const SizedBox(width: 8),
                    const Text('你的脸上有点东西。'),
                  ],
                ),
                const SizedBox(height: 10),
                Align(
                  alignment: Alignment.centerRight,
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 15,
                      vertical: 10,
                    ),
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
  State<_BuildGenerateResultSheet> createState() =>
      _BuildGenerateResultSheetState();
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
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('已复制到剪贴板，快去回复吧！')));
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
          const Text(
            'AI 高情商回复',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 20),
          Expanded(
            child: _isLoading
                ? const Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        CircularProgressIndicator(),
                        SizedBox(height: 16),
                        Text(
                          'AI 正在为你生成完美的回复...',
                          style: TextStyle(color: Colors.grey),
                        ),
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
                          color: const Color(0xFFF9FAFB),
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(color: Colors.grey.shade200),
                        ),
                        child: Row(
                          children: [
                            Expanded(
                              child: Text(
                                _mockReplies[index],
                                style: const TextStyle(fontSize: 14),
                              ),
                            ),
                            IconButton(
                              icon: const Icon(
                                Icons.copy,
                                color: Color(0xFF586AFE),
                                size: 20,
                              ),
                              onPressed: () =>
                                  _copyToClipboard(_mockReplies[index]),
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
  final double animationValue;
  FluidHeartPainter(this.animationValue);

  @override
  void paint(Canvas canvas, Size size) {
    final double width = size.width;
    final double height = size.height;

    Path getHeartPath(double w, double h) {
      Path path = Path();
      path.moveTo(w / 2, h / 5);
      path.cubicTo(5 * w / 14, 0, 0, h / 15, w / 28, 2 * h / 5);
      path.cubicTo(w / 14, 2 * h / 3, 3 * w / 7, 5 * h / 6, w / 2, h);
      path.cubicTo(4 * w / 7, 5 * h / 6, 13 * w / 14, 2 * h / 3, 27 * w / 28, 2 * h / 5);
      path.cubicTo(w, h / 15, 9 * w / 14, 0, w / 2, h / 5);
      path.close();
      return path;
    }

    Path heartPath = getHeartPath(width, height);

    Paint glassPaint = Paint()
      ..color = const Color(0xFF586AFE).withValues(alpha: 0.1)
      ..style = PaintingStyle.fill;

    Paint glassBorderPaint = Paint()
      ..color = const Color(0xFF586AFE).withValues(alpha: 0.3)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.0;

    canvas.drawPath(heartPath, glassPaint);
    canvas.drawPath(heartPath, glassBorderPaint);

    canvas.save();
    canvas.clipPath(heartPath);

    Paint liquidPaint = Paint()
      ..shader = const LinearGradient(
        colors: [Color(0xFF586AFE), Color(0xFF8A9CFF)],
        begin: Alignment.bottomCenter,
        end: Alignment.topCenter,
      ).createShader(Rect.fromLTWH(0, 0, width, height));

    Path wavePath = Path();
    double liquidLevel = height * 0.6;

    wavePath.moveTo(0, height);
    wavePath.lineTo(0, liquidLevel);

    for (double x = 0; x <= width; x++) {
      double y = liquidLevel + math.sin((x / width * 2 * math.pi) + (animationValue * 2 * math.pi)) * 10;
      wavePath.lineTo(x, y);
    }

    wavePath.lineTo(width, height);
    wavePath.close();

    canvas.drawPath(wavePath, liquidPaint);

    Paint liquidPaint2 = Paint()
      ..color = const Color(0xFF586AFE).withValues(alpha: 0.5);

    Path wavePath2 = Path();
    wavePath2.moveTo(0, height);
    wavePath2.lineTo(0, liquidLevel);
    for (double x = 0; x <= width; x++) {
      double y = liquidLevel + math.sin((x / width * 2 * math.pi) + (animationValue * 2 * math.pi) + math.pi) * 8;
      wavePath2.lineTo(x, y);
    }
    wavePath2.lineTo(width, height);
    wavePath2.close();
    canvas.drawPath(wavePath2, liquidPaint2);

    canvas.restore();
  }

  @override
  bool shouldRepaint(covariant FluidHeartPainter oldDelegate) {
    return oldDelegate.animationValue != animationValue;
  }
}

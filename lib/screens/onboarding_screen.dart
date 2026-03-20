import 'package:flutter/material.dart';
import '../main.dart';

class OnboardingScreen extends StatefulWidget {
  const OnboardingScreen({super.key});

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen> {
  final PageController _pageController = PageController();
  int _currentPage = 0;

  void _nextPage() {
    if (_currentPage < 2) {
      _pageController.nextPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    } else {
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (context) => const MainScreen()),
      );
    }
  }

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  // Back button or invisible placeholder to keep spacing
                  _currentPage > 0
                      ? IconButton(
                          icon: const Icon(Icons.arrow_back_ios, size: 20),
                          onPressed: () {
                            _pageController.previousPage(
                              duration: const Duration(milliseconds: 300),
                              curve: Curves.easeInOut,
                            );
                          },
                        )
                      : const SizedBox(width: 48),

                  // Dots Indicator
                  Row(
                    children: List.generate(
                      3,
                      (index) => Container(
                        margin: const EdgeInsets.symmetric(horizontal: 4),
                        width: _currentPage == index ? 20 : 8,
                        height: 8,
                        decoration: BoxDecoration(
                          color: _currentPage == index
                              ? const Color(0xFFFF2E54)
                              : Colors.white.withValues(alpha: 0.2),
                          borderRadius: BorderRadius.circular(4),
                          boxShadow: _currentPage == index ? [
                            BoxShadow(color: const Color(0xFF586AFE).withValues(alpha: 0.3), blurRadius: 10)
                          ] : null,
                        ),
                      ),
                    ),
                  ),

                  // Skip button
                  TextButton(
                    onPressed: () {
                      Navigator.of(context).pushReplacement(
                        MaterialPageRoute(builder: (context) => const MainScreen()),
                      );
                    },
                    child: const Text('跳过', style: TextStyle(color: Colors.grey)),
                  ),
                ],
              ),
            ),
            Expanded(
              child: PageView(
                controller: _pageController,
                physics: const NeverScrollableScrollPhysics(), // Force using buttons
                onPageChanged: (index) {
                  setState(() {
                    _currentPage = index;
                  });
                },
                children: [
                  _buildStep1(),
                  _buildStep2(),
                  _buildStep3(),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  // Step 1: Enable Keyboard in Settings
  Widget _buildStep1() {
    return Padding(
      padding: const EdgeInsets.all(30.0),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text(
            '第一步：开启键盘',
            style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 10),
          const Text(
            '为了让 AI 助手帮您回复，请先在系统中开启 LoveKey。',
            textAlign: TextAlign.center,
            style: TextStyle(fontSize: 14, color: Colors.grey),
          ),
          const SizedBox(height: 40),

          // Mock Settings UI
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: const Color(0xFFF8F9FA),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: Colors.grey[200]!),
            ),
            child: Column(
              children: [
                _buildMockSettingRow('键盘', 'LoveKey', true),
                const Divider(),
                _buildMockSettingRow('允许完全访问', '', true, highlight: true),
              ],
            ),
          ),
          const SizedBox(height: 20),
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Icon(Icons.info_outline, size: 16, color: Colors.orange),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  '系统提示“可能收集密码等信息”为常规提示，我们承诺绝不收集您的任何私人数据。',
                  style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                ),
              ),
            ],
          ),

          const Spacer(),
          _buildActionButton('去设置开启', Icons.settings),
        ],
      ),
    );
  }

  // Step 2: Switch Keyboard
  Widget _buildStep2() {
    return Padding(
      padding: const EdgeInsets.all(30.0),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text(
            '第二步：切换输入法',
            style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 10),
          const Text(
            '长按输入法界面的“地球”或“键盘”图标，选择 LoveKey。',
            textAlign: TextAlign.center,
            style: TextStyle(fontSize: 14, color: Colors.grey),
          ),
          const SizedBox(height: 40),

          // Mock Keyboard Switch UI
          Stack(
            alignment: Alignment.center,
            children: [
              Container(
                height: 200,
                width: double.infinity,
                decoration: BoxDecoration(
                  color: Colors.grey[200],
                  borderRadius: BorderRadius.circular(16),
                ),
                child: const Align(
                  alignment: Alignment.bottomLeft,
                  child: Padding(
                    padding: EdgeInsets.all(16.0),
                    child: Icon(Icons.language, size: 30, color: Colors.grey), // Earth icon
                  ),
                ),
              ),
              // Popup menu simulation
              Positioned(
                bottom: 50,
                left: 10,
                child: Container(
                  width: 200,
                  padding: const EdgeInsets.symmetric(vertical: 8),
                  decoration: BoxDecoration(
                    color: const Color(0xFF2B2F35),
                    borderRadius: BorderRadius.circular(12),
                    boxShadow: [
                      BoxShadow(color: const Color(0xFF2B2F35).withValues(alpha: 0.1), blurRadius: 10, offset: const Offset(0, 5)),
                    ],
                  ),
                  child: Column(
                    children: [
                      _buildMockMenuRow('系统默认键盘', false),
                      const Divider(height: 1),
                      _buildMockMenuRow('LoveKey (AI 恋爱键盘)', true),
                    ],
                  ),
                ),
              ),
              // Pointing finger simulation
              Positioned(
                bottom: 5,
                left: 45,
                child: Icon(Icons.touch_app, size: 40, color: const Color(0xFFFF4D85).withValues(alpha: 0.8)),
              ),
            ],
          ),

          const Spacer(),
          _buildActionButton('点击切换输入法', Icons.keyboard),
        ],
      ),
    );
  }

  // Step 3: Try it out
  Widget _buildStep3() {
    return Padding(
      padding: const EdgeInsets.all(30.0),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text(
            '大功告成！',
            style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 10),
          const Text(
            '现在，点这里试试你的新键盘吧，高情商回复立刻拥有。',
            textAlign: TextAlign.center,
            style: TextStyle(fontSize: 14, color: Colors.grey),
          ),
          const SizedBox(height: 40),

          // Mock Chat Input UI
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: const Color(0xFFF3F4F6),
              borderRadius: BorderRadius.circular(24),
              border: Border.all(color: const Color(0xFF586AFE), width: 2),
            ),
            child: const Row(
              children: [
                Icon(Icons.mic, color: Colors.grey),
                SizedBox(width: 12),
                Expanded(
                  child: Text(
                    '点击这里打字试试...',
                    style: TextStyle(color: Colors.grey, fontSize: 16),
                  ),
                ),
                Icon(Icons.tag_faces, color: Colors.grey),
              ],
            ),
          ),

          const Spacer(),
          _buildActionButton('完成，进入应用', Icons.check_circle),
        ],
      ),
    );
  }

  Widget _buildMockSettingRow(String title, String subtitle, bool isOn, {bool highlight = false}) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(title, style: const TextStyle(fontSize: 16)),
        Row(
          children: [
            if (subtitle.isNotEmpty) Text(subtitle, style: const TextStyle(color: Colors.grey)),
            const SizedBox(width: 8),
            Container(
              width: 50,
              height: 30,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(15),
                color: isOn ? (highlight ? const Color(0xFFFF4D85) : Colors.green) : Colors.grey[300],
              ),
              child: Align(
                alignment: isOn ? Alignment.centerRight : Alignment.centerLeft,
                child: Container(
                  margin: const EdgeInsets.all(2),
                  width: 26,
                  height: 26,
                  decoration: const BoxDecoration(shape: BoxShape.circle, color: const Color(0xFF2B2F35)),
                ),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildMockMenuRow(String title, bool isSelected) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      color: isSelected ? Colors.pink[50] : Colors.transparent,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(title, style: TextStyle(color: isSelected ? const Color(0xFFFF4D85) : Colors.black, fontWeight: isSelected ? FontWeight.bold : FontWeight.normal)),
          if (isSelected) const Icon(Icons.check, color: Color(0xFFFF4D85), size: 18),
        ],
      ),
    );
  }

  Widget _buildActionButton(String text, IconData icon) {
    return SizedBox(
      width: double.infinity,
      height: 56,
      child: ElevatedButton.icon(
        onPressed: _nextPage,
        icon: Icon(icon, color: const Color(0xFF2B2F35)),
        label: Text(
          text,
          style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: const Color(0xFF2B2F35)),
        ),
        style: ElevatedButton.styleFrom(
          backgroundColor: const Color(0xFFFF4D85),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(28),
          ),
          elevation: 4,
          shadowColor: const Color(0xFFFF4D85).withValues(alpha: 0.5),
        ),
      ),
    );
  }
}

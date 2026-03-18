import 'package:flutter/material.dart';
import 'counselor_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

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
              _buildSearchBar(),
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
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.black54,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                'love',
                style: TextStyle(
                  fontSize: 24,
                  fontFamily: 'cursive', // Assuming a cursive font would be used
                  color: Colors.pink[300],
                ),
              ),
            ],
          ),
          // Placeholder for the 3D heart icon
          Icon(
            Icons.favorite,
            size: 80,
            color: Colors.pink[400],
          ),
        ],
      ),
    );
  }

  Widget _buildSearchBar() {
    return Padding(
      padding: const EdgeInsets.all(20),
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: Colors.pink.withOpacity(0.3), width: 1.5),
          boxShadow: [
            BoxShadow(
              color: Colors.pink.withOpacity(0.05),
              spreadRadius: 2,
              blurRadius: 10,
              offset: const Offset(0, 3),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            TextField(
              maxLines: 3,
              minLines: 1,
              decoration: InputDecoration(
                hintText: '在此粘贴对方的话，AI帮你生成高情商回复...',
                hintStyle: TextStyle(color: Colors.grey[400], fontSize: 14),
                border: InputBorder.none,
                contentPadding: const EdgeInsets.all(16),
              ),
            ),
            Padding(
              padding: const EdgeInsets.only(left: 16, right: 16, bottom: 12),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  OutlinedButton.icon(
                    onPressed: () {
                      // TODO: Implement wording polishing
                    },
                    icon: const Icon(Icons.edit_note, size: 18),
                    label: const Text('帮你措辞'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.pink[400],
                      side: BorderSide(color: Colors.pink[200]!),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(20),
                      ),
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 0),
                      minimumSize: const Size(0, 32),
                    ),
                  ),
                  const SizedBox(width: 10),
                  ElevatedButton.icon(
                    onPressed: () {
                      // TODO: Implement AI reply generation mock
                    },
                    icon: const Icon(Icons.auto_awesome, size: 18),
                    label: const Text('恋爱帮回'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.pink[400],
                      foregroundColor: Colors.white,
                      elevation: 0,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(20),
                      ),
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 0),
                      minimumSize: const Size(0, 32),
                    ),
                  ),
                ],
              ),
            ),
          ],
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
          _buildCard(context, '恋爱键盘', '教你高情商聊天，不\n管怎么聊都有料', const Color(0xFFFFEBF0), Icons.keyboard),
          _buildCard(context, '情感导师', '24小时在线的情感\n专家', const Color(0xFFEBF7FF), Icons.support_agent, isCounselor: true),
          _buildCard(context, '话术生成器', '表达心意的魔法道具', const Color(0xFFEBF0FF), Icons.chat),
          _buildCard(context, '图片识人', '上传照片或聊天截图\nAI快速解析', const Color(0xFFF6EBFF), Icons.camera_alt),
        ],
      ),
    );
  }

  Widget _buildCard(BuildContext context, String title, String subtitle, Color bgColor, IconData icon, {bool isCounselor = false}) {
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
                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
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
    return Padding(
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                '如何启用恋爱键盘',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
              ),
            ],
          ),
          const SizedBox(height: 15),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: const Color(0xFFF9FAFB),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: const Color(0xFFF0F0F0)),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildKeyboardStep(
                  stepNumber: '1',
                  title: '启用恋爱键盘',
                  subtitle: '前往设置开启',
                  icon: Icons.settings_suggest,
                  color: Colors.pink,
                  onTap: () {
                    // TODO: Navigate to Android input settings
                  },
                ),
                const Padding(
                  padding: EdgeInsets.symmetric(vertical: 8.0),
                  child: Divider(height: 1, color: Color(0xFFEEEEEE)),
                ),
                _buildKeyboardStep(
                  stepNumber: '2',
                  title: '切换到恋爱键盘',
                  subtitle: '在聊天时使用',
                  icon: Icons.keyboard,
                  color: Colors.blue,
                  onTap: () {
                    // TODO: Show input method picker
                  },
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildKeyboardStep({
    required String stepNumber,
    required String title,
    required String subtitle,
    required IconData icon,
    required Color color,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      child: Row(
        children: [
          Container(
            width: 32,
            height: 32,
            decoration: BoxDecoration(
              color: color.withOpacity(0.1),
              shape: BoxShape.circle,
            ),
            child: Center(
              child: Text(
                stepNumber,
                style: TextStyle(
                  color: color,
                  fontWeight: FontWeight.bold,
                  fontSize: 16,
                ),
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
                ),
                Text(
                  subtitle,
                  style: TextStyle(color: Colors.grey[600], fontSize: 12),
                ),
              ],
            ),
          ),
          Icon(icon, color: color, size: 24),
          const SizedBox(width: 8),
          const Icon(Icons.chevron_right, color: Colors.grey, size: 20),
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
                        color: Colors.black,
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

import 'package:flutter/material.dart';

class KeyboardScreen extends StatefulWidget {
  const KeyboardScreen({super.key});

  @override
  State<KeyboardScreen> createState() => _KeyboardScreenState();
}

class _KeyboardScreenState extends State<KeyboardScreen> {
  final List<Map<String, dynamic>> personas = [
    {'title': '恋爱大师', 'subtitle': '精通恋爱技巧了解人心', 'icon': Icons.favorite_border, 'added': true},
    {'title': '情场高手', 'subtitle': '深谙情场之道，游刃有余', 'icon': Icons.psychology, 'added': true},
    {'title': '土味情话', 'subtitle': '用接地气的情话，打动她', 'icon': Icons.chat, 'added': true},
    {'title': '情绪稳定', 'subtitle': '星级安慰人，给人力量', 'icon': Icons.spa, 'added': false},
    {'title': '花式撩人', 'subtitle': '风趣浪漫，俘获芳心', 'icon': Icons.local_florist, 'added': false},
    {'title': '暧昧拉扯', 'subtitle': '若即若离，让人欲罢不能', 'icon': Icons.all_inclusive, 'added': true},
    {'title': '撩女生', 'subtitle': '擅长用各种方式吸引女生', 'icon': Icons.girl, 'added': false},
    {'title': '贴心暖男', 'subtitle': '心思细腻，总是给予关怀', 'icon': Icons.wb_sunny, 'added': true},
  ];

  void _togglePersonaStatus(int index) {
    setState(() {
      personas[index]['added'] = !personas[index]['added'];
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text('话术', style: TextStyle(color: Colors.black)), // Following App Store image style for top tab
        backgroundColor: Colors.white,
        elevation: 0,
        centerTitle: true,
      ),
      body: SafeArea(
        child: Column(
          children: [
            _buildTopBanners(),
            _buildTabBar(context),
            Expanded(child: _buildPersonaGrid()),
          ],
        ),
      ),
    );
  }

  Widget _buildTopBanners() {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Row(
        children: [
          Expanded(
            flex: 6,
            child: Container(
              height: 100,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [Color(0xFFE6E6FA), Color(0xFFFFD1DC)], // Purple to pink gradient
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                borderRadius: BorderRadius.circular(15),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('定制专属人设', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                  const Text('为你量身打造', style: TextStyle(fontSize: 12, color: Colors.black54)),
                  const Spacer(),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                    decoration: const BoxDecoration(
                      color: Colors.black,
                      borderRadius: BorderRadius.all(Radius.circular(12)),
                    ),
                    child: const Text('去定制', style: TextStyle(color: Colors.white, fontSize: 12)),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            flex: 4,
            child: Column(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: const Color(0xFFEBF0FF),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: const Row(
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text('我的键盘', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
                            Text('查看已配置', style: TextStyle(fontSize: 9, color: Colors.black54)),
                          ],
                        ),
                      ),
                      Icon(Icons.keyboard, size: 20, color: Colors.blueAccent),
                    ],
                  ),
                ),
                const SizedBox(height: 12),
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: const Color(0xFFFFF5E6),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: const Row(
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text('恋爱教程', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
                            Text('经典恋爱案例', style: TextStyle(fontSize: 9, color: Colors.black54)),
                          ],
                        ),
                      ),
                      Icon(Icons.book, size: 20, color: Colors.orangeAccent),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTabBar(BuildContext context) {
    return DefaultTabController(
      length: 5,
      child: TabBar(
        isScrollable: true,
        labelColor: Theme.of(context).colorScheme.primary,
        unselectedLabelColor: Colors.grey,
        indicatorColor: Theme.of(context).colorScheme.primary,
        indicatorSize: TabBarIndicatorSize.label,
        tabs: const [
          Tab(text: '恋爱人设', icon: Icon(Icons.favorite, size: 20)),
          Tab(text: '聊天必备', icon: Icon(Icons.chat_bubble_outline, size: 20)),
          Tab(text: '职场人设', icon: Icon(Icons.work_outline, size: 20)),
          Tab(text: '十二星座', icon: Icon(Icons.star_border, size: 20)),
          Tab(text: 'MBTI', icon: Icon(Icons.person_outline, size: 20)),
        ],
      ),
    );
  }

  Widget _buildPersonaGrid() {
    return GridView.builder(
      padding: const EdgeInsets.all(16),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        mainAxisSpacing: 16,
        crossAxisSpacing: 16,
        childAspectRatio: 1.5,
      ),
      itemCount: personas.length,
      itemBuilder: (context, index) {
        final persona = personas[index];
        final isAdded = persona['added'] as bool;
        return Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(12),
            boxShadow: [
              BoxShadow(
                color: Colors.grey.withOpacity(0.1),
                spreadRadius: 1,
                blurRadius: 5,
                offset: const Offset(0, 2),
              ),
            ],
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    persona['title'] as String,
                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    persona['subtitle'] as String,
                    style: TextStyle(fontSize: 10, color: Colors.grey[600]),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  CircleAvatar(
                    radius: 14,
                    backgroundColor: Colors.grey[200],
                    child: Icon(persona['icon'] as IconData, size: 16, color: Colors.black54),
                  ),
                  GestureDetector(
                    onTap: () => _togglePersonaStatus(index),
                    child: AnimatedContainer(
                      duration: const Duration(milliseconds: 300),
                      curve: Curves.easeOutBack,
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                      decoration: BoxDecoration(
                        color: isAdded ? Colors.grey[200] : const Color(0xFFFF6B4A),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: AnimatedSwitcher(
                        duration: const Duration(milliseconds: 200),
                        transitionBuilder: (child, animation) {
                          return ScaleTransition(
                            scale: animation,
                            child: child,
                          );
                        },
                        child: Icon(
                          isAdded ? Icons.check : Icons.add,
                          key: ValueKey<bool>(isAdded),
                          size: 14,
                          color: isAdded ? Colors.grey[600] : Colors.white,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        );
      },
    );
  }
}

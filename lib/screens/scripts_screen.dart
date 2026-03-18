import 'package:flutter/material.dart';

class ScriptsScreen extends StatelessWidget {
  const ScriptsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text('话术生成器', style: TextStyle(color: Colors.black)),
        backgroundColor: Colors.white,
        elevation: 0,
        centerTitle: false,
      ),
      body: SafeArea(
        child: Column(
          children: [
            _buildTabBar(context),
            Expanded(child: _buildScriptsGrid()),
          ],
        ),
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
          Tab(text: '开场'),
          Tab(text: '约会'),
          Tab(text: '告白'),
          Tab(text: '道歉'),
          Tab(text: '夸赞'),
        ],
      ),
    );
  }

  Widget _buildScriptsGrid() {
    final scripts = [
      {'title': '早起问候', 'subtitle': '温柔问候，甜心一天', 'iconColor': Colors.blue[100]},
      {'title': '晚安问候', 'subtitle': '温柔情话伴入梦', 'iconColor': Colors.purple[100]},
      {'title': '午休闲聊', 'subtitle': '用美食与爱陪伴', 'iconColor': Colors.orange[100]},
      {'title': '半夜失眠', 'subtitle': '深夜里的温暖陪伴', 'iconColor': Colors.indigo[100]},
      {'title': '下班关心', 'subtitle': '为疲惫的Ta加鼓打气', 'iconColor': Colors.green[100]},
      {'title': '加班问候', 'subtitle': '辛苦了，有你陪着Ta', 'iconColor': Colors.red[100]},
      {'title': '周末约会', 'subtitle': '创造专属美好时光', 'iconColor': Colors.teal[100]},
      {'title': '天气提醒', 'subtitle': '贴心提醒护Ta周全', 'iconColor': Colors.cyan[100]},
      {'title': '趣事分享', 'subtitle': '分享快乐拉近距离', 'iconColor': Colors.amber[100]},
      {'title': '考试打气', 'subtitle': '为Ta加油赢得未来', 'iconColor': Colors.pink[100]},
      {'title': '生病关怀', 'subtitle': '暖心呵护，早日康复', 'iconColor': Colors.lime[100]},
      {'title': '突然想念', 'subtitle': '情不自禁的爱意表达', 'iconColor': Colors.lightBlue[100]},
    ];

    return GridView.builder(
      padding: const EdgeInsets.all(16),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        mainAxisSpacing: 12,
        crossAxisSpacing: 12,
        childAspectRatio: 2.2, // Rectangular cards
      ),
      itemCount: scripts.length,
      itemBuilder: (context, index) {
        final script = scripts[index];
        return Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(12),
            boxShadow: [
              BoxShadow(
                color: Colors.grey.withOpacity(0.08),
                spreadRadius: 1,
                blurRadius: 4,
                offset: const Offset(0, 1),
              ),
            ],
            border: Border.all(color: Colors.grey.shade100),
          ),
          child: Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      script['title'] as String,
                      style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      script['subtitle'] as String,
                      style: TextStyle(fontSize: 10, color: Colors.grey[500]),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              CircleAvatar(
                radius: 16,
                backgroundColor: script['iconColor'] as Color,
                child: const Icon(Icons.person, size: 20, color: Colors.white), // Placeholder for image
              ),
            ],
          ),
        );
      },
    );
  }
}

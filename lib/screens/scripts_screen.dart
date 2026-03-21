import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class ScriptsScreen extends StatelessWidget {
  const ScriptsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFFFFFFF),
      appBar: AppBar(
        title: const Text('话术生成器', style: TextStyle(color: Colors.black)),
        backgroundColor: const Color(0xFFFFFFFF),
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
      {
        'title': '半夜失眠',
        'subtitle': '深夜里的温暖陪伴',
        'iconColor': Colors.indigo[100],
      },
      {
        'title': '下班关心',
        'subtitle': '为疲惫的Ta加鼓打气',
        'iconColor': Colors.green[100],
      },
      {'title': '加班问候', 'subtitle': '辛苦了，有你陪着Ta', 'iconColor': Colors.red[100]},
      {'title': '周末约会', 'subtitle': '创造专属美好时光', 'iconColor': Colors.teal[100]},
      {'title': '天气提醒', 'subtitle': '贴心提醒护Ta周全', 'iconColor': Colors.cyan[100]},
      {'title': '趣事分享', 'subtitle': '分享快乐拉近距离', 'iconColor': Colors.amber[100]},
      {'title': '考试打气', 'subtitle': '为Ta加油赢得未来', 'iconColor': Color(0xFFE4E7F2)},
      {'title': '生病关怀', 'subtitle': '暖心呵护，早日康复', 'iconColor': Colors.lime[100]},
      {
        'title': '突然想念',
        'subtitle': '情不自禁的爱意表达',
        'iconColor': Colors.lightBlue[100],
      },
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
        return GestureDetector(
          onTap: () {
            showModalBottomSheet(
              context: context,
              backgroundColor: Colors.transparent,
              isScrollControlled: true,
              builder: (context) =>
                  _BuildScriptDetailSheet(title: script['title'] as String),
            );
          },
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: BoxDecoration(
              color: const Color(0xFFFFFFFF),
              borderRadius: BorderRadius.circular(12),
              boxShadow: [
                BoxShadow(
                  color: Colors.grey.withValues(alpha: 0.08),
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
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 14,
                        ),
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
                  child: const Icon(
                    Icons.person,
                    size: 20,
                    color: const Color(0xFF2B2F35),
                  ), // Placeholder for image
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}

class _BuildScriptDetailSheet extends StatelessWidget {
  final String title;
  const _BuildScriptDetailSheet({required this.title});

  void _copyToClipboard(BuildContext context, String text) {
    Clipboard.setData(ClipboardData(text: text)).then((_) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('话术已复制！')));
    });
  }

  @override
  Widget build(BuildContext context) {
    final List<String> mockSentences = List.generate(
      10,
      (index) => '这是关于【$title】的第 ${index + 1} 条高情商话术示例，可以直接复制使用哦～',
    );

    return Container(
      height: MediaQuery.of(context).size.height * 0.7,
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
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
            child: Text(
              title,
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
          ),
          Expanded(
            child: ListView.separated(
              padding: const EdgeInsets.all(20),
              itemCount: mockSentences.length,
              separatorBuilder: (context, index) => const SizedBox(height: 12),
              itemBuilder: (context, index) {
                return Container(
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
                          mockSentences[index],
                          style: const TextStyle(fontSize: 14),
                        ),
                      ),
                      const SizedBox(width: 10),
                      IconButton(
                        icon: const Icon(
                          Icons.copy,
                          color: const Color(0xFF586AFE),
                          size: 20,
                        ),
                        onPressed: () =>
                            _copyToClipboard(context, mockSentences[index]),
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

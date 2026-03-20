import 'package:flutter/material.dart';

class KeyboardScreen extends StatefulWidget {
  const KeyboardScreen({super.key});

  @override
  State<KeyboardScreen> createState() => _KeyboardScreenState();
}

class _KeyboardScreenState extends State<KeyboardScreen> with TickerProviderStateMixin {
  final GlobalKey _myKeyboardKey = GlobalKey();
  late final List<GlobalKey> _iconKeys;

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

  @override
  void initState() {
    super.initState();
    _iconKeys = List.generate(personas.length, (index) => GlobalKey());
  }

  void _togglePersonaStatus(int index, GlobalKey iconKey) {
    bool isCurrentlyAdded = personas[index]['added'];
    setState(() {
      personas[index]['added'] = !isCurrentlyAdded;
    });

    if (!isCurrentlyAdded) {
      _runSuckAnimation(iconKey, personas[index]['icon'] as IconData);
    }
  }

  void _runSuckAnimation(GlobalKey sourceKey, IconData iconData) {
    final RenderBox? sourceBox = sourceKey.currentContext?.findRenderObject() as RenderBox?;
    final RenderBox? targetBox = _myKeyboardKey.currentContext?.findRenderObject() as RenderBox?;

    if (sourceBox == null || targetBox == null) return;

    final sourcePosition = sourceBox.localToGlobal(Offset.zero);
    final targetPosition = targetBox.localToGlobal(targetBox.size.center(Offset.zero));

    final overlayEntry = OverlayEntry(
      builder: (context) => _SuckAnimationWidget(
        startPosition: sourcePosition,
        endPosition: targetPosition,
        iconData: iconData,
      ),
    );

    Overlay.of(context).insert(overlayEntry);

    // Remove the overlay after animation completes
    Future.delayed(const Duration(milliseconds: 600), () {
      overlayEntry.remove();
      // Optionally trigger a bounce on the target here
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF1A1528),
      appBar: AppBar(
        title: const Text('话术', style: TextStyle(color: Colors.black)), // Following App Store image style for top tab
        backgroundColor: const Color(0xFF1A1528),
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
                  const Text('为你量身打造', style: TextStyle(fontSize: 12, color: const Color(0xFF585C62))),
                  const Spacer(),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                    decoration: const BoxDecoration(
                      color: const Color(0xFF2B2F35),
                      borderRadius: BorderRadius.all(Radius.circular(12)),
                    ),
                    child: const Text('去定制', style: TextStyle(color: const Color(0xFF2B2F35), fontSize: 12)),
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
                  child: Row(
                    children: [
                      const Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text('我的键盘', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
                            Text('查看已配置', style: TextStyle(fontSize: 9, color: const Color(0xFF585C62))),
                          ],
                        ),
                      ),
                      Icon(Icons.keyboard, key: _myKeyboardKey, size: 20, color: Colors.blueAccent),
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
                            Text('经典恋爱案例', style: TextStyle(fontSize: 9, color: const Color(0xFF585C62))),
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
        final GlobalKey iconKey = _iconKeys[index];

        return Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: const Color(0xFF2B2F35),
            borderRadius: BorderRadius.circular(12),
            boxShadow: [
              BoxShadow(
                color: Colors.grey.withValues(alpha: 0.1),
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
                    key: iconKey,
                    radius: 14,
                    backgroundColor: Colors.grey[200],
                    child: Icon(persona['icon'] as IconData, size: 16, color: const Color(0xFF585C62)),
                  ),
                  GestureDetector(
                    onTap: () => _togglePersonaStatus(index, iconKey),
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

class _SuckAnimationWidget extends StatefulWidget {
  final Offset startPosition;
  final Offset endPosition;
  final IconData iconData;

  const _SuckAnimationWidget({
    required this.startPosition,
    required this.endPosition,
    required this.iconData,
  });

  @override
  State<_SuckAnimationWidget> createState() => _SuckAnimationWidgetState();
}

class _SuckAnimationWidgetState extends State<_SuckAnimationWidget> with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _scaleAnimation;
  late Animation<double> _opacityAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 600),
    );

    _scaleAnimation = Tween<double>(begin: 1.0, end: 0.1).animate(
      CurvedAnimation(
        parent: _controller,
        curve: const Interval(0.5, 1.0, curve: Curves.easeIn), // Shrink at the end
      ),
    );

    _opacityAnimation = Tween<double>(begin: 1.0, end: 0.0).animate(
      CurvedAnimation(
        parent: _controller,
        curve: const Interval(0.7, 1.0, curve: Curves.easeOut),
      ),
    );

    _controller.forward();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Positioned(
      left: widget.startPosition.dx,
      top: widget.startPosition.dy,
      child: AnimatedBuilder(
        animation: _controller,
        builder: (context, child) {
          // Manually translating instead of SlideTransition for absolute pixels
          return Transform.translate(
            offset: Offset(
              (widget.endPosition.dx - widget.startPosition.dx) * _controller.value,
              (widget.endPosition.dy - widget.startPosition.dy) * _controller.value,
            ),
            child: Transform.scale(
              scale: _scaleAnimation.value,
              child: Opacity(
                opacity: _opacityAnimation.value,
                child: Material(
                  color: Colors.transparent,
                  child: CircleAvatar(
                    radius: 14,
                    backgroundColor: Colors.pink[100],
                    child: Icon(widget.iconData, size: 16, color: Colors.pink),
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}

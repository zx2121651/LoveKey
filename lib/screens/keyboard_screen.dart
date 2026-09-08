import 'package:flutter/material.dart';

import '../models/persona.dart';
import '../services/settings_service.dart';

class KeyboardScreen extends StatefulWidget {
  const KeyboardScreen({super.key});

  @override
  State<KeyboardScreen> createState() => _KeyboardScreenState();
}

class _KeyboardScreenState extends State<KeyboardScreen>
    with SingleTickerProviderStateMixin {
  final GlobalKey _myKeyboardKey = GlobalKey();
  final Map<String, GlobalKey> _iconKeys = {};
  late final TabController _tabController;

  int _intimacy = 50;
  String _activePersona = '通用';
  late Set<String> _downloaded;
  List<Persona> _customPersonas = [];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(
      length: kPersonaCategories.length + 1, // 5 个分类 + 我的
      vsync: this,
    )..addListener(() {
        if (!_tabController.indexIsChanging) setState(() {});
      });

    _intimacy = SettingsService.instance.intimacy;
    _activePersona = SettingsService.instance.persona;
    _customPersonas = SettingsService.instance.customPersonas
        .map(Persona.fromJson)
        .toList();
    // 默认已下载首批人设，其余需要从市场下载
    _downloaded = {
      for (final p in kMarketPersonas.take(5)) p.title,
    };
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  // ------------------------------------------------------------------
  // 交互逻辑
  // ------------------------------------------------------------------

  void _selectPersona(String title) {
    if (_activePersona == title) return;
    setState(() => _activePersona = title);
    SettingsService.instance.setPersona(title);
  }

  void _toggleDownload(Persona persona, GlobalKey iconKey) {
    final isDownloaded = _downloaded.contains(persona.title);
    setState(() {
      if (isDownloaded) {
        _downloaded.remove(persona.title);
      } else {
        _downloaded.add(persona.title);
      }
    });

    if (!isDownloaded) {
      _runSuckAnimation(iconKey, persona.icon);
    }
  }

  Future<void> _openCustomize() async {
    final nameCtrl = TextEditingController();
    final descCtrl = TextEditingController();

    final result = await showModalBottomSheet<Map<String, String>>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (ctx) => Padding(
        padding: EdgeInsets.only(
          left: 20,
          right: 20,
          top: 16,
          bottom: MediaQuery.of(ctx).viewInsets.bottom + 24,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              '定制专属人设',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
            ),
            const SizedBox(height: 4),
            const Text(
              '定义你想要的聊天风格，AI 会按照这个口吻回复',
              style: TextStyle(fontSize: 12, color: Colors.grey),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: nameCtrl,
              maxLength: 12,
              decoration: InputDecoration(
                labelText: '人设名称',
                hintText: '例如：温柔御姐 / 霸道学长',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: descCtrl,
              maxLines: 3,
              maxLength: 60,
              decoration: InputDecoration(
                labelText: '人设描述',
                hintText: '性格特点、说话口吻、语气偏好…',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              height: 48,
              child: FilledButton(
                style: FilledButton.styleFrom(
                  backgroundColor: const Color(0xFF586AFE),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(24),
                  ),
                ),
                onPressed: () {
                  final name = nameCtrl.text.trim();
                  if (name.isEmpty) {
                    ScaffoldMessenger.of(ctx).showSnackBar(
                      const SnackBar(content: Text('请先填写人设名称')),
                    );
                    return;
                  }
                  Navigator.of(ctx).pop({
                    'title': name,
                    'subtitle': descCtrl.text.trim().isEmpty
                        ? '自定义人设'
                        : descCtrl.text.trim(),
                  });
                },
                child: const Text(
                  '保存并使用',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
              ),
            ),
          ],
        ),
      ),
    );

    if (result != null) {
      final persona = Persona(
        title: result['title']!,
        subtitle: result['subtitle']!,
        category: '我的',
        icon: Icons.person,
        isCustom: true,
      );
      await SettingsService.instance.saveCustomPersona(persona.toJson());
      if (!mounted) return;
      setState(() => _customPersonas.add(persona));
      _selectPersona(persona.title);
    }
  }

  void _runSuckAnimation(GlobalKey sourceKey, IconData iconData) {
    final RenderBox? sourceBox =
        sourceKey.currentContext?.findRenderObject() as RenderBox?;
    final RenderBox? targetBox =
        _myKeyboardKey.currentContext?.findRenderObject() as RenderBox?;

    if (sourceBox == null || targetBox == null) return;

    final sourcePosition = sourceBox.localToGlobal(Offset.zero);
    final targetPosition = targetBox.localToGlobal(
      targetBox.size.center(Offset.zero),
    );

    final overlayEntry = OverlayEntry(
      builder: (context) => _SuckAnimationWidget(
        startPosition: sourcePosition,
        endPosition: targetPosition,
        iconData: iconData,
      ),
    );

    Overlay.of(context).insert(overlayEntry);

    Future.delayed(const Duration(milliseconds: 600), () {
      overlayEntry.remove();
    });
  }

  // ------------------------------------------------------------------
  // UI
  // ------------------------------------------------------------------

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFFFFFFF),
      appBar: AppBar(
        title: const Text(
          '话术',
          style: TextStyle(color: Colors.black),
        ),
        backgroundColor: const Color(0xFFFFFFFF),
        elevation: 0,
        centerTitle: true,
      ),
      body: SafeArea(
        child: Column(
          children: [
            _buildIntimacyCard(),
            _buildTopBanners(),
            _buildTabBar(context),
            Expanded(child: _buildMarketGrid()),
          ],
        ),
      ),
    );
  }

  Widget _buildIntimacyCard() {
    const accent = Color(0xFF586AFE);
    final label = SettingsService.instance.intimacyLabel;
    final emoji = _intimacy < 20
        ? '😶'
        : _intimacy < 40
            ? '🙋'
            : _intimacy < 60
                ? '🙂'
                : _intimacy < 80
                    ? '😊'
                    : '😍';

    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 4, 16, 12),
      child: Container(
        padding: const EdgeInsets.fromLTRB(16, 14, 16, 8),
        decoration: BoxDecoration(
          gradient: const LinearGradient(
            colors: [Color(0xFFFFEFF5), Color(0xFFF3F0FF)],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: accent.withValues(alpha: 0.1)),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    const Text(
                      '聊天亲密度',
                      style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                    ),
                    const SizedBox(width: 8),
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 10,
                        vertical: 3,
                      ),
                      decoration: BoxDecoration(
                        color: accent,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text(
                        '$emoji $label',
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 11,
                        ),
                      ),
                    ),
                  ],
                ),
                Text(
                  '$_intimacy%',
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 16,
                    color: accent,
                  ),
                ),
              ],
            ),
            SliderTheme(
              data: SliderTheme.of(context).copyWith(
                trackHeight: 4,
                thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 9),
                overlayShape: const RoundSliderOverlayShape(overlayRadius: 16),
              ),
              child: Slider(
                value: _intimacy.toDouble(),
                min: 0,
                max: 100,
                divisions: 20,
                activeColor: accent,
                inactiveColor: accent.withValues(alpha: 0.15),
                onChanged: (v) {
                  setState(() => _intimacy = v.round());
                  SettingsService.instance.setIntimacy(_intimacy);
                },
              ),
            ),
            const Text(
              '从陌生人到灵魂伴侣，AI 回复的分寸感会随之自动调节',
              style: TextStyle(fontSize: 11, color: Colors.grey),
            ),
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
            child: GestureDetector(
              onTap: _openCustomize,
              child: Container(
                height: 100,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [
                      Color(0xFFE6E6FA),
                      Color(0xFFFFD1DC),
                    ],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                  borderRadius: BorderRadius.circular(15),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      '定制专属人设',
                      style:
                          TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                    ),
                    const Text(
                      '为你量身打造',
                      style: TextStyle(
                        fontSize: 12,
                        color: Color(0xFF585C62),
                      ),
                    ),
                    const Spacer(),
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 12,
                        vertical: 4,
                      ),
                      decoration: const BoxDecoration(
                        color: Color(0xFF586AFE),
                        borderRadius: BorderRadius.all(Radius.circular(12)),
                      ),
                      child: const Text(
                        '去定制',
                        style: TextStyle(color: Colors.white, fontSize: 12),
                      ),
                    ),
                  ],
                ),
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
                            Text(
                              '我的键盘',
                              style: TextStyle(
                                fontWeight: FontWeight.bold,
                                fontSize: 12,
                              ),
                            ),
                            Text(
                              '查看已配置',
                              style: TextStyle(
                                fontSize: 9,
                                color: Color(0xFF585C62),
                              ),
                            ),
                          ],
                        ),
                      ),
                      Icon(
                        Icons.keyboard,
                        key: _myKeyboardKey,
                        size: 20,
                        color: Colors.blueAccent,
                      ),
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
                            Text(
                              '恋爱教程',
                              style: TextStyle(
                                fontWeight: FontWeight.bold,
                                fontSize: 12,
                              ),
                            ),
                            Text(
                              '经典恋爱案例',
                              style: TextStyle(
                                fontSize: 9,
                                color: Color(0xFF585C62),
                              ),
                            ),
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
    return TabBar(
      controller: _tabController,
      isScrollable: true,
      labelColor: Theme.of(context).colorScheme.primary,
      unselectedLabelColor: Colors.grey,
      indicatorColor: Theme.of(context).colorScheme.primary,
      indicatorSize: TabBarIndicatorSize.label,
      tabs: [
        const Tab(text: '恋爱人设', icon: Icon(Icons.favorite, size: 20)),
        const Tab(text: '聊天必备', icon: Icon(Icons.chat_bubble_outline, size: 20)),
        const Tab(text: '职场人设', icon: Icon(Icons.work_outline, size: 20)),
        const Tab(text: '十二星座', icon: Icon(Icons.star_border, size: 20)),
        const Tab(text: 'MBTI', icon: Icon(Icons.person_outline, size: 20)),
        const Tab(text: '我的', icon: Icon(Icons.person, size: 20)),
      ],
    );
  }

  /// 当前 Tab 对应的人设列表（最后一个是自定义的「我的」）
  List<Persona> get _currentPersonas {
    if (_tabController.index == kPersonaCategories.length) {
      return _customPersonas;
    }
    final category = kPersonaCategories[_tabController.index];
    return kMarketPersonas.where((p) => p.category == category).toList();
  }

  Widget _buildMarketGrid() {
    final personas = _currentPersonas;

    if (personas.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.person_add_alt, size: 48, color: Colors.grey),
            const SizedBox(height: 12),
            const Text(
              '还没有自定义人设',
              style: TextStyle(fontSize: 14, color: Colors.grey),
            ),
            const SizedBox(height: 16),
            FilledButton(
              style: FilledButton.styleFrom(
                backgroundColor: const Color(0xFF586AFE),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(20),
                ),
              ),
              onPressed: _openCustomize,
              child: const Text('去定制'),
            ),
          ],
        ),
      );
    }

    return GridView.builder(
      padding: const EdgeInsets.all(16),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        mainAxisSpacing: 16,
        crossAxisSpacing: 16,
        childAspectRatio: 1.4,
      ),
      itemCount: personas.length,
      itemBuilder: (context, index) => _buildPersonaCard(personas[index]),
    );
  }

  Widget _buildPersonaCard(Persona persona) {
    const accent = Color(0xFF586AFE);
    final isActive = persona.title == _activePersona;
    final isDownloaded =
        persona.isCustom || _downloaded.contains(persona.title);
    final iconKey =
        _iconKeys.putIfAbsent(persona.title, () => GlobalKey());

    return GestureDetector(
      onTap: () {
        if (isDownloaded) _selectPersona(persona.title);
      },
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: isActive ? accent : Colors.transparent,
            width: 1.5,
          ),
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
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        persona.title,
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 14,
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    if (isActive)
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 6,
                          vertical: 2,
                        ),
                        decoration: BoxDecoration(
                          color: accent.withValues(alpha: 0.12),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Text(
                          '使用中',
                          style: const TextStyle(
                            fontSize: 10,
                            color: accent,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                  ],
                ),
                const SizedBox(height: 4),
                Text(
                  persona.subtitle,
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
                  backgroundColor:
                      isActive ? accent.withValues(alpha: 0.15) : Colors.grey[200],
                  child: Icon(
                    persona.icon,
                    size: 16,
                    color: isActive ? accent : const Color(0xFF585C62),
                  ),
                ),
                GestureDetector(
                  onTap: () {
                    if (!isDownloaded) {
                      _toggleDownload(persona, iconKey);
                    } else if (!isActive) {
                      _selectPersona(persona.title);
                    }
                  },
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 300),
                    curve: Curves.easeOutBack,
                    padding: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: !isDownloaded
                          ? accent
                          : isActive
                              ? accent
                              : Colors.grey[200],
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
                      child: Row(
                        key: ValueKey<String>(
                            '$isDownloaded-$isActive'),
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(
                            isActive
                                ? Icons.check_circle
                                : isDownloaded
                                    ? Icons.check
                                    : Icons.add,
                            size: 14,
                            color: isDownloaded ? accent : Colors.white,
                          ),
                          const SizedBox(width: 4),
                          Text(
                            isActive
                                ? '使用中'
                                : isDownloaded
                                    ? '已下载'
                                    : '下载',
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.bold,
                              color:
                                  isDownloaded ? accent : Colors.white,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
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

class _SuckAnimationWidgetState extends State<_SuckAnimationWidget>
    with SingleTickerProviderStateMixin {
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
        curve: const Interval(
          0.5,
          1.0,
          curve: Curves.easeIn,
        ),
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
          return Transform.translate(
            offset: Offset(
              (widget.endPosition.dx - widget.startPosition.dx) *
                  _controller.value,
              (widget.endPosition.dy - widget.startPosition.dy) *
                  _controller.value,
            ),
            child: Transform.scale(
              scale: _scaleAnimation.value,
              child: Opacity(
                opacity: _opacityAnimation.value,
                child: Material(
                  color: Colors.transparent,
                  child: CircleAvatar(
                    radius: 14,
                    backgroundColor: const Color(0xFFE4E7F2),
                    child: Icon(
                      widget.iconData,
                      size: 16,
                      color: const Color(0xFF586AFE),
                    ),
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

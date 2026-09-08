import 'package:flutter/material.dart';

import '../models/membership.dart';
import '../services/float_ball_service.dart';

class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFFFFFFF),
      body: SafeArea(
        child: SingleChildScrollView(
          child: Column(
            children: [
              _buildUserInfo(),
              _buildVipBanner(),
              _buildFeatureIcons(),
              const SizedBox(height: 20),
              _buildMenuSection(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildUserInfo() {
    return Padding(
      padding: const EdgeInsets.all(20.0),
      child: Row(
        children: [
          CircleAvatar(
            radius: 35,
            backgroundColor: Colors.orange[100],
            child: Icon(
              Icons.pets,
              size: 40,
              color: const Color(0xFF586AFE),
            ), // Placeholder dog avatar
          ),
          const SizedBox(width: 16),
          const Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Looper',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                ),
                SizedBox(height: 4),
                Text(
                  'ID: 9793685',
                  style: TextStyle(fontSize: 12, color: Colors.grey),
                ),
                SizedBox(height: 4),
                Text(
                  '普通用户 · 点击上方卡片开通会员',
                  style: TextStyle(fontSize: 12, color: Colors.grey),
                ),
              ],
            ),
          ),
          TextButton(
            onPressed: () {},
            child: const Text(
              '编辑主页',
              style: TextStyle(color: Colors.grey, fontSize: 12),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildVipBanner() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20.0),
      child: GestureDetector(
        onTap: () {
          showModalBottomSheet(
            context: context,
            backgroundColor: Colors.transparent,
            isScrollControlled: true,
            builder: (context) => const _MembershipPaywallSheet(),
          );
        },
        child: Container(
          width: double.infinity,
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            gradient: const LinearGradient(
              colors: [
                Color(0xFFFF8C00),
                Color(0xFFFF4500),
              ], // Orange to Red gradient
              begin: Alignment.centerLeft,
              end: Alignment.centerRight,
            ),
            borderRadius: BorderRadius.circular(16),
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '恋爱键盘 会员开通',
                    style: TextStyle(
                      color: Color(0xFF2B2F35),
                      fontWeight: FontWeight.bold,
                      fontSize: 18,
                    ),
                  ),
                  SizedBox(height: 8),
                  Text(
                    '首单 7 天免费试用 · ¥48/月 · ¥98/季',
                    style: TextStyle(
                      color: Color(0xFF585C62),
                      fontSize: 12,
                    ),
                  ),
                ],
              ),
              Icon(
                Icons.payment,
                size: 40,
                color: Colors.yellow[200],
              ), // Placeholder for VIP Card image
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildFeatureIcons() {
    return Padding(
      padding: const EdgeInsets.all(20.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceAround,
        children: [
          _buildIconItem('恋爱键盘', Icons.keyboard, Color(0xFF586AFE)),
          _buildIconItem('情感导师', Icons.support_agent, Colors.blue),
          _buildIconItem('话术生成', Icons.chat, Colors.purple),
          _buildIconItem('图片识人', Icons.camera_alt, Colors.orange),
        ],
      ),
    );
  }

  Widget _buildIconItem(String title, IconData icon, Color color) {
    return Column(
      children: [
        CircleAvatar(
          radius: 25,
          backgroundColor: color.withValues(alpha: 0.1),
          child: Icon(icon, color: color, size: 24),
        ),
        const SizedBox(height: 8),
        Text(title, style: const TextStyle(fontSize: 12)),
      ],
    );
  }

  Widget _buildMenuSection() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20.0),
      child: Column(
        children: [
          _buildMenuItem(Icons.person_outline, '个人信息'),
          _buildMenuItem(Icons.receipt_long, '消费记录'),
          _buildMenuItem(Icons.info_outline, '关于'),
          _buildMenuItem(Icons.headset_mic_outlined, '联系客服'),
          _buildMenuItem(
            Icons.settings_outlined,
            '设置',
            onTap: () => _openSettings(context),
          ),
          _buildMenuItem(Icons.gavel_outlined, '摘要投诉'),
        ],
      ),
    );
  }

  void _openSettings(BuildContext context) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (ctx) => const _SettingsSheet(),
    );
  }

  Widget _buildMenuItem(IconData icon, String title, {VoidCallback? onTap}) {
    return ListTile(
      contentPadding: EdgeInsets.zero,
      leading: Icon(icon, color: Colors.grey[700]),
      title: Text(title, style: const TextStyle(fontSize: 14)),
      trailing: const Icon(Icons.chevron_right, color: Colors.grey),
      onTap: onTap,
    );
  }
}

/// 设置弹层：全局悬浮球开关
class _SettingsSheet extends StatefulWidget {
  const _SettingsSheet();

  @override
  State<_SettingsSheet> createState() => _SettingsSheetState();
}

class _SettingsSheetState extends State<_SettingsSheet> {
  bool _floatBallOn = false;

  @override
  void initState() {
    super.initState();
    FloatBallService.instance.isShowing().then((v) {
      if (mounted) setState(() => _floatBallOn = v);
    });
  }

  Future<void> _toggleFloatBall(bool value) async {
    if (value) {
      final started = await FloatBallService.instance.start();
      if (!started && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('请在系统设置中开启「显示在其他应用上层」权限')),
        );
      }
    } else {
      await FloatBallService.instance.stop();
    }
    if (mounted) setState(() => _floatBallOn = value);
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Column(
        mainAxisSize: MainAxisSize.min,
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
          const Padding(
            padding: EdgeInsets.symmetric(horizontal: 20),
            child: Align(
              alignment: Alignment.centerLeft,
              child: Text(
                '设置',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
            ),
          ),
          SwitchListTile(
            value: _floatBallOn,
            activeTrackColor: const Color(0xFF586AFE),
            onChanged: _toggleFloatBall,
            title: const Text('全局悬浮球', style: TextStyle(fontSize: 14)),
            subtitle: const Text(
              '任意界面点击悬浮球，快速唤起恋爱键盘',
              style: TextStyle(fontSize: 12, color: Colors.grey),
            ),
          ),
          const SizedBox(height: 12),
        ],
      ),
    );
  }
}

/// 会员开通弹层：7天试用 / ¥48月 / ¥98季
class _MembershipPaywallSheet extends StatefulWidget {
  const _MembershipPaywallSheet();

  @override
  State<_MembershipPaywallSheet> createState() => _MembershipPaywallSheetState();
}

class _MembershipPaywallSheetState extends State<_MembershipPaywallSheet> {
  String _selectedId = kMembershipPlans.first.id;

  @override
  Widget build(BuildContext context) {
    final selected = kMembershipPlans.firstWhere((p) => p.id == _selectedId);

    return Container(
      height: MediaQuery.of(context).size.height * 0.75,
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.only(
          topLeft: Radius.circular(24),
          topRight: Radius.circular(24),
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
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  '解锁恋爱键盘全部功能',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
                GestureDetector(
                  onTap: () => Navigator.of(context).pop(),
                  child: const Icon(Icons.close, color: Colors.grey),
                ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 4, 20, 0),
            child: Align(
              alignment: Alignment.centerLeft,
              child: Text(
                '首单 $kTrialDays 天免费试用 · 随时取消',
                style: TextStyle(fontSize: 12, color: Colors.grey[600]),
              ),
            ),
          ),
          const SizedBox(height: 12),
          // 权益列表
          Expanded(
            child: ListView(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              children: [
                for (final benefit in kMembershipBenefits)
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 6),
                    child: Row(
                      children: [
                        const Icon(
                          Icons.check_circle,
                          color: Color(0xFF586AFE),
                          size: 20,
                        ),
                        const SizedBox(width: 10),
                        Text(benefit, style: const TextStyle(fontSize: 14)),
                      ],
                    ),
                  ),
                const SizedBox(height: 16),
                // 计划选择
                for (final plan in kMembershipPlans)
                  _buildPlanCard(plan),
                const SizedBox(height: 16),
                SizedBox(
                  width: double.infinity,
                  height: 50,
                  child: FilledButton(
                    style: FilledButton.styleFrom(
                      backgroundColor: const Color(0xFFFFA000),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(25),
                      ),
                    ),
                    onPressed: () {
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text(
                            selected.isTrial
                                ? '已开始 $kTrialDays 天免费试用！'
                                : '已开通${selected.name}（${selected.price}${selected.period}）',
                          ),
                        ),
                      );
                      Navigator.of(context).pop();
                    },
                    child: Text(
                      selected.isTrial
                          ? '开始 $kTrialDays 天免费试用'
                          : '立即开通 ${selected.price}${selected.period}',
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: Color(0xFF2B2F35),
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 10),
                const Center(
                  child: Text(
                    '订阅自动续费，可随时在系统设置中取消',
                    style: TextStyle(fontSize: 11, color: Colors.grey),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPlanCard(MembershipPlan plan) {
    final isSelected = plan.id == _selectedId;
    return GestureDetector(
      onTap: () => setState(() => _selectedId = plan.id),
      child: Container(
        margin: const EdgeInsets.only(bottom: 10),
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: isSelected
              ? const Color(0xFF586AFE).withValues(alpha: 0.06)
              : Colors.white,
          borderRadius: BorderRadius.circular(14),
          border: Border.all(
            color: isSelected
                ? const Color(0xFF586AFE)
                : Colors.grey.shade200,
            width: isSelected ? 1.5 : 1,
          ),
        ),
        child: Row(
          children: [
            Icon(
              isSelected
                  ? Icons.radio_button_checked
                  : Icons.radio_button_off,
              color: isSelected
                  ? const Color(0xFF586AFE)
                  : Colors.grey[400],
              size: 20,
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Text(
                        plan.name,
                        style: const TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      if (plan.recommended) ...[
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 6,
                            vertical: 2,
                          ),
                          decoration: BoxDecoration(
                            color: const Color(0xFFFFA000),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: const Text(
                            '推荐',
                            style: TextStyle(
                              fontSize: 10,
                              color: Color(0xFF2B2F35),
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                      ],
                    ],
                  ),
                  const SizedBox(height: 2),
                  Text(
                    plan.tagline,
                    style: TextStyle(fontSize: 11, color: Colors.grey[600]),
                  ),
                ],
              ),
            ),
            Text(
              '${plan.price}${plan.period}',
              style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Color(0xFF586AFE),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

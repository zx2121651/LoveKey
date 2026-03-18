import 'package:flutter/material.dart';

class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
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
            child: Icon(Icons.pets, size: 40, color: Colors.orange[400]), // Placeholder dog avatar
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
                  '终身会员',
                  style: TextStyle(fontSize: 12, color: Colors.grey),
                ),
              ],
            ),
          ),
          TextButton(
            onPressed: () {},
            child: const Text('编辑主页', style: TextStyle(color: Colors.grey, fontSize: 12)),
          ),
        ],
      ),
    );
  }

  Widget _buildVipBanner() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20.0),
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          gradient: const LinearGradient(
            colors: [Color(0xFFFF8C00), Color(0xFFFF4500)], // Orange to Red gradient
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
                  style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 18),
                ),
                SizedBox(height: 8),
                Text(
                  'VIP会员体验聊天达人',
                  style: TextStyle(color: Colors.white70, fontSize: 12),
                ),
              ],
            ),
            Icon(Icons.payment, size: 40, color: Colors.yellow[200]), // Placeholder for VIP Card image
          ],
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
          _buildIconItem('恋爱键盘', Icons.keyboard, Colors.pink),
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
          backgroundColor: color.withOpacity(0.1),
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
          _buildMenuItem(Icons.settings_outlined, '设置'),
          _buildMenuItem(Icons.gavel_outlined, '摘要投诉'),
        ],
      ),
    );
  }

  Widget _buildMenuItem(IconData icon, String title) {
    return ListTile(
      contentPadding: EdgeInsets.zero,
      leading: Icon(icon, color: Colors.grey[700]),
      title: Text(title, style: const TextStyle(fontSize: 14)),
      trailing: const Icon(Icons.chevron_right, color: Colors.grey),
      onTap: () {},
    );
  }
}

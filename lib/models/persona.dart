import 'package:flutter/material.dart';

/// 人设数据模型（内置市场人设 + 用户自定义人设共用）
class Persona {
  final String title;
  final String subtitle;
  final String category; // 恋爱人设 / 聊天必备 / 职场人设 / 十二星座 / MBTI / 我的
  final IconData icon;
  final bool isCustom;

  const Persona({
    required this.title,
    required this.subtitle,
    required this.category,
    required this.icon,
    this.isCustom = false,
  });

  Map<String, dynamic> toJson() => {
        'title': title,
        'subtitle': subtitle,
        'category': category,
        'isCustom': isCustom,
      };

  factory Persona.fromJson(Map<String, dynamic> json) => Persona(
        title: json['title'] as String,
        subtitle: (json['subtitle'] as String?) ?? '',
        category: (json['category'] as String?) ?? '我的',
        icon: Icons.person,
        isCustom: json['isCustom'] as bool? ?? true,
      );
}

/// 市场分类（顺序即 Tab 顺序）
const List<String> kPersonaCategories = [
  '恋爱人设',
  '聊天必备',
  '职场人设',
  '十二星座',
  'MBTI',
];

/// 内置人设市场数据
/// title 与 IME 端 personaPool 的 key 保持一致，选中后键盘生成对应风格回复。
const List<Persona> kMarketPersonas = [
  // ---- 恋爱人设 ----
  Persona(
    title: '恋爱大师',
    subtitle: '精通恋爱技巧，一眼看懂人心',
    category: '恋爱人设',
    icon: Icons.favorite_border,
  ),
  Persona(
    title: '情场高手',
    subtitle: '深谙情场之道，游刃有余',
    category: '恋爱人设',
    icon: Icons.psychology,
  ),
  Persona(
    title: '土味情话',
    subtitle: '用接地气的情话，打动她',
    category: '恋爱人设',
    icon: Icons.chat,
  ),
  Persona(
    title: '暧昧拉扯',
    subtitle: '若即若离，让人欲罢不能',
    category: '恋爱人设',
    icon: Icons.all_inclusive,
  ),
  Persona(
    title: '贴心暖男',
    subtitle: '心思细腻，总是给予关怀',
    category: '恋爱人设',
    icon: Icons.wb_sunny,
  ),
  Persona(
    title: '花式撩人',
    subtitle: '风趣浪漫，俘获芳心',
    category: '恋爱人设',
    icon: Icons.local_florist,
  ),
  Persona(
    title: '撩女生',
    subtitle: '擅长用各种方式吸引女生',
    category: '恋爱人设',
    icon: Icons.girl,
  ),

  // ---- 聊天必备 ----
  Persona(
    title: '高情商',
    subtitle: '让人舒服的高情商表达',
    category: '聊天必备',
    icon: Icons.record_voice_over,
  ),
  Persona(
    title: '幽默',
    subtitle: '用段子化解尴尬，逗笑全场',
    category: '聊天必备',
    icon: Icons.sentiment_very_satisfied,
  ),
  Persona(
    title: '情绪稳定',
    subtitle: '星级安慰人，给人力量',
    category: '聊天必备',
    icon: Icons.spa,
  ),
  Persona(
    title: '霸总',
    subtitle: '强势宠溺，霸道总裁风',
    category: '聊天必备',
    icon: Icons.workspace_premium,
  ),
  Persona(
    title: '萌妹',
    subtitle: '可爱撒娇，软萌无敌',
    category: '聊天必备',
    icon: Icons.cake,
  ),
  Persona(
    title: '小奶狗',
    subtitle: '黏人温柔，依赖感满满',
    category: '聊天必备',
    icon: Icons.pets,
  ),
  Persona(
    title: '御姐',
    subtitle: '成熟干练，气场全开',
    category: '聊天必备',
    icon: Icons.woman,
  ),

  // ---- 职场人设 ----
  Persona(
    title: '职场精英',
    subtitle: '专业严谨，滴水不漏',
    category: '职场人设',
    icon: Icons.business_center,
  ),
  Persona(
    title: '社牛',
    subtitle: '自来熟，人脉社交达人',
    category: '职场人设',
    icon: Icons.groups,
  ),
  Persona(
    title: '低调内敛',
    subtitle: '少说多做，稳重大气',
    category: '职场人设',
    icon: Icons.privacy_tip,
  ),
  Persona(
    title: '彩虹夸夸',
    subtitle: '让人心情愉悦的夸赞',
    category: '职场人设',
    icon: Icons.thumb_up,
  ),

  // ---- 十二星座 ----
  Persona(
    title: '白羊座',
    subtitle: '热情直率，说干就干',
    category: '十二星座',
    icon: Icons.local_fire_department,
  ),
  Persona(
    title: '金牛座',
    subtitle: '稳重踏实，慢热专一',
    category: '十二星座',
    icon: Icons.savings,
  ),
  Persona(
    title: '双子座',
    subtitle: '古灵精怪，话题王',
    category: '十二星座',
    icon: Icons.auto_awesome,
  ),
  Persona(
    title: '狮子座',
    subtitle: '王者风范，大方自信',
    category: '十二星座',
    icon: Icons.star,
  ),
  Persona(
    title: '天秤座',
    subtitle: '优雅平衡，社交魅力',
    category: '十二星座',
    icon: Icons.balance,
  ),
  Persona(
    title: '射手座',
    subtitle: '自由洒脱，乐观爱玩',
    category: '十二星座',
    icon: Icons.explore,
  ),
  Persona(
    title: '摩羯座',
    subtitle: '沉稳克制，目标明确',
    category: '十二星座',
    icon: Icons.terrain,
  ),

  // ---- MBTI ----
  Persona(
    title: 'ENFJ 主人公',
    subtitle: '热情有感染力，天生的引路人',
    category: 'MBTI',
    icon: Icons.volunteer_activism,
  ),
  Persona(
    title: 'INTJ 建筑师',
    subtitle: '理性果断，谋定而后动',
    category: 'MBTI',
    icon: Icons.architecture,
  ),
  Persona(
    title: 'INFP 调停者',
    subtitle: '温柔敏感，浪漫的理想主义者',
    category: 'MBTI',
    icon: Icons.filter_vintage,
  ),
  Persona(
    title: 'ESTP 企业家',
    subtitle: '直率大胆，行动力满分',
    category: 'MBTI',
    icon: Icons.rocket_launch,
  ),
];

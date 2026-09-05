import 'package:flutter/material.dart';

/// AI 生成的一条回复建议
class AiReply {
  final String style; // 风格标签，如「高情商」「幽默」
  final String text;

  const AiReply({required this.style, required this.text});
}

/// 键盘人设
class Persona {
  final String id;
  final String name;
  final String desc;
  final IconData icon;
  final Color color;
  bool added;

  Persona({
    required this.id,
    required this.name,
    required this.desc,
    required this.icon,
    required this.color,
    this.added = false,
  });
}

/// 话术场景卡片
class ScriptScene {
  final String title;
  final String subtitle;
  final IconData icon;
  final Color color;
  final List<String> lines;

  const ScriptScene({
    required this.title,
    required this.subtitle,
    required this.icon,
    required this.color,
    required this.lines,
  });
}

/// 话术分类
class ScriptCategory {
  final String name;
  final List<ScriptScene> scenes;

  const ScriptCategory({required this.name, required this.scenes});
}

/// 聊天消息（咨询师）
class ChatMessage {
  final String text;
  final bool isUser;

  ChatMessage({required this.text, required this.isUser});
}

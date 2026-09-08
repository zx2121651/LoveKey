import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'settings_service.dart';

/// AI 回复生成服务。
///
/// 优先调用火山引擎豆包（方舟 Ark，OpenAI 兼容协议）API 生成高情商回复；
/// 未配置 API Key 或请求失败时，自动回退到本地亲密度 Mock，保证功能可用。
///
/// 配置方式：构建时通过 `--dart-define=DOUBAO_API_KEY=xxx` 注入，
/// 或直接修改本文件顶部的 [_apiKey]。
class AIService {
  AIService._();

  static final AIService instance = AIService._();

  /// 豆包 API Key。为空时始终走本地 Mock 回退。
  static const String _apiKey = String.fromEnvironment('DOUBAO_API_KEY');

  /// 方舟模型 ID（以方舟控制台创建的接入点为准）
  static const String _model = 'doubao-1-5-pro-32k-250115';

  static const String _endpoint =
      'https://ark.cn-beijing.volces.com/api/v3/chat/completions';

  static const Duration _timeout = Duration(seconds: 15);

  bool get hasApiKey => _apiKey.isNotEmpty;

  /// 生成多条候选回复。
  ///
  /// [query]：TA 说的话。
  /// [intimacy]：0-100 亲密度，缺省读取 SettingsService。
  /// [persona]：人设名，缺省读取 SettingsService。
  Future<List<String>> generateReplies(
    String query, {
    int? intimacy,
    String? persona,
  }) async {
    if (hasApiKey) {
      try {
        final replies = await _requestFromApi(query, intimacy, persona);
        if (replies.isNotEmpty) return replies;
      } catch (_) {
        // 网络/解析失败，静默回退到本地 Mock
      }
    }
    return _mockByIntimacy(intimacy ?? SettingsService.instance.intimacy);
  }

  /// 调用豆包 API，返回最多 3 条候选回复。
  Future<List<String>> _requestFromApi(
    String query,
    int? intimacy,
    String? persona,
  ) async {
    final level = intimacy ?? SettingsService.instance.intimacy;
    final name = persona ?? SettingsService.instance.persona;

    // 优先一次请求 3 条；若服务端不支持 n 参数（非 200），降级为 n=1 重试。
    var replies = await _post(query, level, name, n: 3);
    if (replies.isEmpty) {
      replies = await _post(query, level, name, n: 1);
    }
    return replies;
  }

  Future<List<String>> _post(
    String query,
    int level,
    String persona, {
    required int n,
  }) async {
    final client = HttpClient()..connectionTimeout = _timeout;
    try {
      final request = await client
          .postUrl(Uri.parse(_endpoint))
          .timeout(_timeout);
      request.headers.contentType = ContentType.json;
      request.headers.set(
        HttpHeaders.authorizationHeader,
        'Bearer $_apiKey',
      );
      request.write(jsonEncode({
        'model': _model,
        'messages': [
          {'role': 'system', 'content': _buildSystemPrompt(level, persona)},
          {'role': 'user', 'content': query},
        ],
        'temperature': 0.9,
        'max_tokens': 300,
        'n': n,
      }));
      final response = await request.close().timeout(_timeout);
      final text =
          await response.transform(utf8.decoder).join().timeout(_timeout);
      if (response.statusCode != 200) return const [];

      final data = jsonDecode(text) as Map<String, dynamic>;
      final choices = (data['choices'] as List?) ?? const [];
      final replies = <String>[];
      for (final choice in choices) {
        final message = (choice as Map<String, dynamic>)['message'];
        final content =
            (message as Map<String, dynamic>?)?['content'] as String?;
        final cleaned = _clean(content ?? '');
        if (cleaned.isNotEmpty) replies.add(cleaned);
      }
      return replies.take(3).toList();
    } finally {
      client.close();
    }
  }

  /// 组装系统提示词：亲密度决定分寸，人设决定口吻。
  String _buildSystemPrompt(int level, String persona) {
    final stage = _stageLabel(level);
    String rule;
    if (level < 20) {
      rule = '克制礼貌，保持距离，不过度热情';
    } else if (level < 40) {
      rule = '友好自然，适度热情，可适当幽默';
    } else if (level < 60) {
      rule = '轻松亲近，可调侃、可展现关心';
    } else if (level < 80) {
      rule = '暧昧亲密，可撒娇、可暗示好感';
    } else {
      rule = '极度亲密，大胆表达爱意与思念';
    }
    return '你是「AI恋爱键盘」的恋爱参谋。当前关系阶段：$stage'
        '（亲密度 $level/100），此阶段的分寸：$rule。'
        '你的人设：$persona，请始终以该人设的口吻说话。'
        '针对用户给出的对方消息，生成 3 条风格不同的高情商回复，'
        '每条单独一行，不要编号，不要任何解释或前缀。';
  }

  /// 清理模型输出：去编号、项目符号、首尾引号。
  String _clean(String raw) {
    final lines = raw
        .split('\n')
        .map((line) => line
            .trim()
            .replaceFirst(RegExp(r'^[\d一二三四五六七八九十]+[.、)）]'), '')
            .replaceFirst(RegExp(r'^[-*•·]'), '')
            .replaceFirst(RegExp(r'^["“]'), '')
            .replaceFirst(RegExp(r'["”]$'), '')
            .trim())
        .where((line) => line.isNotEmpty)
        .toList();
    return lines.isEmpty ? '' : lines.join(' ');
  }

  String _stageLabel(int level) {
    if (level < 20) return '陌生人';
    if (level < 40) return '刚认识';
    if (level < 60) return '普通朋友';
    if (level < 80) return '暧昧期';
    return '灵魂伴侣';
  }

  /// 本地 Mock：按亲密度给出不同分寸感的回复（API 不可用时的兜底）。
  List<String> _mockByIntimacy(int level) {
    if (level < 20) {
      return [
        '好的，我看到了，谢谢你告诉我。',
        '收到～我这边还有点事，晚点再详细回复你。',
        '嗯嗯，明白你的意思了。',
      ];
    } else if (level < 40) {
      return [
        '哈哈，你这么说挺有意思的，具体说说？',
        '我也有类似的感觉，可以多聊聊。',
        '原来如此，看来你是个很有趣的人。',
      ];
    } else if (level < 60) {
      return [
        '哈哈，你这话说到我心坎里了！',
        '跟你聊天总是很轻松，很开心。',
        '你懂我，这种感觉真好～',
      ];
    } else if (level < 80) {
      return [
        '你这样说，我会忍不住多想的哦～',
        '只有你才会让我这么开心。',
        '跟你聊天，时间总是过得太快。',
      ];
    }
    return [
      '有你在，我什么都不怕。',
      '你就是我每天最期待的那个人～',
      '想你了，此刻尤其想。',
    ];
  }
}

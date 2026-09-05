import '../models/models.dart';

/// AI 服务抽象层。
/// 当前使用 [MockAiService]，后续接入真实大模型（如豆包 API）时，
/// 只需新增一个实现类并替换 [aiService] 的实例即可。
abstract class AiService {
  /// 帮你回：根据对方说的话生成多种风格的回复
  Future<List<AiReply>> generateReplies(String input, {String persona = '通用'});

  /// 换个说法：润色用户的草稿
  Future<List<AiReply>> refineDraft(String draft);

  /// 自定义指令：按用户意图生成一段文案
  Future<String> generateFromPrompt(String prompt);

  /// 情感导师：多轮对话
  Future<String> chat(String message, List<ChatMessage> history);
}

/// 全局服务定位（简单单例，便于后续替换为依赖注入）
AiService aiService = MockAiService();

class MockAiService implements AiService {
  static const Duration _latency = Duration(milliseconds: 1400);

  @override
  Future<List<AiReply>> generateReplies(String input,
      {String persona = '通用'}) async {
    await Future.delayed(_latency);
    return [
      AiReply(style: '高情商', text: '你这么说，是不是偷偷在心里练习过很多遍啦？'),
      AiReply(style: '幽默', text: '收到！已自动加入今日开心循环播放清单～'),
      AiReply(style: '暧昧拉扯', text: '你猜我看到这句话的时候，第一个想到的人是谁？'),
      AiReply(style: '贴心暖男', text: '听你说这些我真的挺开心的，今天过得还好吗？'),
      AiReply(style: '直球心动', text: '怎么办，你随便一句话都能让我心动一下。'),
    ];
  }

  @override
  Future<List<AiReply>> refineDraft(String draft) async {
    await Future.delayed(_latency);
    return [
      AiReply(style: '高情商', text: '原来你是这么想的呀，那我可得好好回应一下这份心意了～'),
      AiReply(style: '幽默', text: '这段话我反复看了三遍，确认是被可爱到了。'),
      AiReply(style: '拉扯感', text: '你这么说，是想让我更在意你一点吗？'),
    ];
  }

  @override
  Future<String> generateFromPrompt(String prompt) async {
    await Future.delayed(_latency);
    return '我理解你的意思了。这样吧，换个角度表达会更打动人：你的用心我都感受得到，也希望你能看到我的认真。';
  }

  @override
  Future<String> chat(String message, List<ChatMessage> history) async {
    await Future.delayed(_latency);
    if (message.contains('暧昧') || message.contains('天天聊天')) {
      return '天天聊天确实容易产生依赖感。如果关系一直没有突破，可以试着稍微放慢节奏，观察 TA 的反应；也可以找个自然的契机，比如周末，主动约 TA 出来吃个饭，把线上好感落到线下。';
    } else if (message.contains('吵架') || message.contains('和好')) {
      return '情侣之间吵架很正常，先给彼此一点冷静的空间。破冰时可以主动递台阶，比如说：「其实我刚才语气也有点急，我们好好聊聊可以吗？」先谈感受，再谈对错，会更容易和好。';
    } else if (message.contains('异地')) {
      return '异地恋最重要的是信任和分享欲。建议约定固定的视频时间，平时多分享生活里的小确幸，条件允许的话定期见面。距离不是问题，失联感才是。';
    }
    return '我理解你的感受，感情的事有时确实复杂。你可以再多跟我说说具体的细节吗？比如你们是怎么认识的、最近发生了什么变化，这样我能更准确地帮你分析。';
  }
}

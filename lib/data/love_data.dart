import 'package:flutter/material.dart';
import '../models/models.dart';

/// 键盘人设库
final List<Persona> kPersonas = [
  Persona(
    id: 'master',
    name: '恋爱大师',
    desc: '精通恋爱技巧，懂人心',
    icon: Icons.favorite,
    color: const Color(0xFFFF4D6D),
    added: true,
  ),
  Persona(
    id: 'player',
    name: '情场高手',
    desc: '深谙情场之道，游刃有余',
    icon: Icons.psychology,
    color: const Color(0xFF8B5CF6),
    added: true,
  ),
  Persona(
    id: 'earthy',
    name: '土味情话',
    desc: '接地气的情话最动人',
    icon: Icons.chat_bubble,
    color: const Color(0xFFF59E0B),
    added: true,
  ),
  Persona(
    id: 'stable',
    name: '情绪稳定',
    desc: '星级安慰人，给人力量',
    icon: Icons.spa,
    color: const Color(0xFF10B981),
  ),
  Persona(
    id: 'flirty',
    name: '花式撩人',
    desc: '风趣浪漫，俘获芳心',
    icon: Icons.local_florist,
    color: const Color(0xFFEC4899),
  ),
  Persona(
    id: 'push_pull',
    name: '暧昧拉扯',
    desc: '若即若离，让人上头',
    icon: Icons.all_inclusive,
    color: const Color(0xFF6366F1),
    added: true,
  ),
  Persona(
    id: 'girl',
    name: '撩女生',
    desc: '擅长吸引女生的注意',
    icon: Icons.face_3,
    color: const Color(0xFFF97316),
  ),
  Persona(
    id: 'warm',
    name: '贴心暖男',
    desc: '心思细腻，总给关怀',
    icon: Icons.wb_sunny,
    color: const Color(0xFFEAB308),
    added: true,
  ),
  Persona(
    id: 'humor',
    name: '幽默逗比',
    desc: '快乐源泉，聊天不冷场',
    icon: Icons.emoji_emotions,
    color: const Color(0xFF06B6D4),
  ),
  Persona(
    id: 'direct',
    name: '直球心动',
    desc: '坦率真诚，一击即中',
    icon: Icons.bolt,
    color: const Color(0xFFEF4444),
  ),
];

/// 话术库分类数据
final List<ScriptCategory> kScriptCategories = [
  ScriptCategory(
    name: '开场',
    scenes: [
      ScriptScene(
        title: '初次搭讪',
        subtitle: '第一句话就抓住 TA',
        icon: Icons.waving_hand,
        color: const Color(0xFFFFEBF0),
        lines: [
          '你好呀，刚刚刷到你的动态，感觉你是个很有趣的人～',
          '冒昧打扰啦，你的头像很有品味，忍不住想认识一下。',
          ' hi～看你也喜欢旅行，最近有想去的地方吗？',
        ],
      ),
      ScriptScene(
        title: '早起问候',
        subtitle: '温柔问候，甜一整天',
        icon: Icons.wb_twilight,
        color: const Color(0xFFFFF3E6),
        lines: [
          '早安，今天的第一个念头就是你。',
          '醒了吗？新的一天开始了，记得吃早餐哦～',
          '早上好呀，希望你今天遇到的都是温柔的事。',
        ],
      ),
      ScriptScene(
        title: '晚安问候',
        subtitle: '温柔情话伴入梦',
        icon: Icons.nightlight_round,
        color: const Color(0xFFF3EFFF),
        lines: [
          '晚安，做个好梦，梦里有我的话记得加分。',
          '今天辛苦啦，早点休息，明天又是元气满满的一天。',
          '睡前最后一条消息想发给你，晚安，好梦～',
        ],
      ),
      ScriptScene(
        title: '冷场救急',
        subtitle: '聊天不再尴尬冷场',
        icon: Icons.ac_unit,
        color: const Color(0xFFEBF4FF),
        lines: [
          '刚刚看到一个超好笑的段子，第一个就想分享给你。',
          '突然好奇，你最近在听什么歌呀？',
          '考你一个问题：如果中了五百万，你第一件事想做什么？',
        ],
      ),
    ],
  ),
  ScriptCategory(
    name: '约会',
    scenes: [
      ScriptScene(
        title: '周末约会',
        subtitle: '自然发出邀约',
        icon: Icons.weekend,
        color: const Color(0xFFE8FAF0),
        lines: [
          '这周末有空吗？发现一家评价超棒的店，想带你去试试。',
          '天气预报说周末放晴，要不要出去走走？',
          '新上映的电影据说很好看，一起去看吗？我请爆米花。',
        ],
      ),
      ScriptScene(
        title: '吃饭邀约',
        subtitle: '用美食拉近距离',
        icon: Icons.restaurant,
        color: const Color(0xFFFFF3E6),
        lines: [
          '你最喜欢吃什么呀？我正好知道一家宝藏餐厅。',
          '工作再忙也要好好吃饭，今晚一起吃个饭？',
          '听说城西那家火锅排队两小时，敢不敢跟我去挑战？',
        ],
      ),
    ],
  ),
  ScriptCategory(
    name: '告白',
    scenes: [
      ScriptScene(
        title: '含蓄试探',
        subtitle: '进可攻退可守',
        icon: Icons.favorite_border,
        color: const Color(0xFFFFEBF0),
        lines: [
          '我最近好像养成一个习惯，什么事都想第一时间告诉你。',
          '你说，两个聊得来的人，是不是应该试试更进一步？',
          '跟你聊天的时候，嘴角总是不自觉上扬。',
        ],
      ),
      ScriptScene(
        title: '正式表白',
        subtitle: '认真又浪漫',
        icon: Icons.favorite,
        color: const Color(0xFFFFE0E6),
        lines: [
          '我不想再只和你做朋友了，我想做那个可以光明正大关心你的人。',
          '遇见你之后，我才知道原来心动是这种感觉。做我女朋友/男朋友好吗？',
          '往后余生，风雪是你，平淡是你，目光所至都是你。',
        ],
      ),
    ],
  ),
  ScriptCategory(
    name: '道歉',
    scenes: [
      ScriptScene(
        title: '吵架和好',
        subtitle: '主动递出台阶',
        icon: Icons.handshake,
        color: const Color(0xFFEBF4FF),
        lines: [
          '对不起，我刚才语气太重了，其实我比谁都舍不得让你难过。',
          '气消了吗？我买了你爱吃的零食，和好好不好？',
          '吵架归吵架，又不是不爱你了。我们好好聊聊吧。',
        ],
      ),
    ],
  ),
  ScriptCategory(
    name: '夸赞',
    scenes: [
      ScriptScene(
        title: '花式夸人',
        subtitle: '夸到 TA 心坎里',
        icon: Icons.auto_awesome,
        color: const Color(0xFFF3EFFF),
        lines: [
          '你今天有点怪哦……怪好看的。',
          '你是不是偷偷充了会员？怎么笑起来比昨天还好看。',
          '别人是好看，你是耐看，越看越喜欢的那种。',
        ],
      ),
    ],
  ),
  ScriptCategory(
    name: '关心',
    scenes: [
      ScriptScene(
        title: '生病关怀',
        subtitle: '暖心呵护，早日康复',
        icon: Icons.healing,
        color: const Color(0xFFE8FAF0),
        lines: [
          '听说你不舒服，记得多喝热水多休息，需要我陪你去医院吗？',
          '生病了就别硬撑着，好好休息，工作的事先放一放。',
          '给你点了热粥，记得趁热喝，快点好起来～',
        ],
      ),
      ScriptScene(
        title: '天气提醒',
        subtitle: '贴心提醒护周全',
        icon: Icons.umbrella,
        color: const Color(0xFFEBF4FF),
        lines: [
          '明天降温，出门记得多穿一件，别感冒了。',
          '今天下午有雨，你带伞了吗？没带的话我去接你。',
        ],
      ),
      ScriptScene(
        title: '下班关心',
        subtitle: '为疲惫的 TA 打气',
        icon: Icons.night_shelter,
        color: const Color(0xFFFFF3E6),
        lines: [
          '下班了吗？辛苦一天啦，晚上想吃点什么？',
          '今天工作累不累？早点回家休息，我等你报平安。',
        ],
      ),
    ],
  ),
];

/// 土味情话轮播
const List<Map<String, String>> kLoveQuotes = [
  {'q': '你的脸上有点东西。', 'a': '有点帅。'},
  {'q': '你知道我为什么感冒了吗？', 'a': '因为我对你完全没有抵抗力。'},
  {'q': '你是什么血型？', 'a': '不，你是我的理想型。'},
  {'q': '最近有谣言说我喜欢你。', 'a': '我要澄清一下，那不是谣言。'},
  {'q': '我觉得你今天有点怪。', 'a': '怪可爱的。'},
];

/// 帮你回：场景化快捷短语
const Map<String, List<String>> kQuickReplies = {
  '高情商': ['你说的对，我怎么没想到呢', '跟你聊天总能学到东西', '你这个想法很有意思'],
  '幽默': ['哈哈哈哈你也太可爱了吧', '这话我没法接，除非请我喝奶茶', '笑死，你怎么这么有梗'],
  '暖心': ['辛苦啦，抱抱你', '有我在呢，别担心', '你好好休息，其他的交给我'],
  '暧昧': ['你猜我现在在想什么', '再说这种话我可就当真了', '你这是在撩我吗？'],
};

/// 咨询师开场快捷问题
const List<String> kQuickQuestions = [
  '我们天天聊天，关系很暧昧，但就是没有进展怎么办？',
  '异地恋如何维持感情？总觉得没有安全感',
  '我们吵架了，不知道该怎么开口和好',
];

import 'dart:convert';

import 'package:flutter/services.dart';

/// 与 Android 原生端（MainActivity / IME）共享的偏好设置桥。
/// 亲密度与人设保存在 SharedPreferences 中，Flutter 与原生 IME 实时共享。
class SettingsService {
  SettingsService._();

  static final SettingsService instance = SettingsService._();

  static const MethodChannel _channel = MethodChannel('lovekey/settings');
  static const EventChannel _eventsChannel = EventChannel('lovekey/settings/events');

  int _intimacy = 50;
  String _persona = '通用';
  List<Map<String, dynamic>> _customPersonas = [];
  bool _channelAvailable = true;

  /// 设置被外部（如键盘 IME 内调整亲密度）修改时回调，用于刷新 UI
  VoidCallback? onChanged;

  /// AI 回复状态机多订阅方集合（键盘面板 / 生成结果页 / 后续扩展页各自独立消费，互不覆盖）
  final List<void Function(Map<String, dynamic>)> _airReplyListeners = [];

  /// 订阅 AI 回复状态机事件，返回取消订阅函数。
  void Function() addAIReplyListener(void Function(Map<String, dynamic>) listener) {
    _airReplyListeners.add(listener);
    return () => _airReplyListeners.remove(listener);
  }

  int get intimacy => _intimacy;
  String get persona => _persona;
  List<Map<String, dynamic>> get customPersonas => List.unmodifiable(_customPersonas);

  /// 亲密度对应的关系阶段文案
  String get intimacyLabel {
    if (_intimacy < 20) return '陌生人';
    if (_intimacy < 40) return '刚认识';
    if (_intimacy < 60) return '普通朋友';
    if (_intimacy < 80) return '暧昧期';
    return '灵魂伴侣';
  }

  Future<void> init() async {
    try {
      _intimacy = await _channel.invokeMethod<int>('getIntimacy') ?? 50;
      _persona = await _channel.invokeMethod<String>('getPersona') ?? '通用';
      final rawList =
          await _channel.invokeListMethod<String>('getCustomPersonas') ?? [];
      _customPersonas = rawList
          .map((e) => jsonDecode(e) as Map<String, dynamic>)
          .toList();
    } catch (_) {
      _channelAvailable = false;
    }
    _listenForExternalChanges();
  }

  /// 订阅原生事件流：IME 键盘内修改亲密度/人设时，Flutter 侧实时刷新；
  /// AI 回复状态机阶段流转也经同一通道广播（type == 'airReply'）。
  void _listenForExternalChanges() {
    _eventsChannel.receiveBroadcastStream().listen((event) {
      if (event is! Map) return;
      // AI 回复状态机事件：分发给所有订阅方，不参与设置字段合并
      if (event['type'] == 'airReply') {
        final payload = Map<String, dynamic>.from(event);
        for (final listener in List.of(_airReplyListeners)) {
          listener(payload);
        }
        return;
      }
      final settings = event['settings'];
      if (settings is String) {
        try {
          final data = jsonDecode(settings) as Map<String, dynamic>;
          _intimacy = (data['intimacy_level'] as num?)?.toInt() ?? _intimacy;
          _persona = data['selected_persona'] as String? ?? _persona;
          final rawList = data['custom_personas'] as List? ?? [];
          _customPersonas = rawList
              .map((e) => e is String ? jsonDecode(e) as Map<String, dynamic> : e as Map<String, dynamic>)
              .toList();
          onChanged?.call();
        } catch (_) {}
      }
    });
  }

  Future<void> setIntimacy(int level) async {
    _intimacy = level.clamp(0, 100).toInt();
    if (!_channelAvailable) return;
    try {
      await _channel.invokeMethod('setIntimacy', {'level': _intimacy});
    } catch (_) {
      _channelAvailable = false;
    }
  }

  Future<void> setPersona(String name) async {
    _persona = name;
    if (!_channelAvailable) return;
    try {
      await _channel.invokeMethod('setPersona', {'name': name});
    } catch (_) {
      _channelAvailable = false;
    }
  }

  Future<void> saveCustomPersona(Map<String, dynamic> persona) async {
    // 按 id 去重：已存在则覆盖，否则追加
    final index = _customPersonas.indexWhere((p) => p['id'] == persona['id']);
    if (index >= 0) {
      _customPersonas = [..._customPersonas]..[index] = persona;
    } else {
      _customPersonas = [..._customPersonas, persona];
    }
    if (!_channelAvailable) return;
    try {
      await _channel.invokeMethod('saveCustomPersona', {'json': jsonEncode(persona)});
    } catch (_) {
      _channelAvailable = false;
    }
  }

  /// 编辑已存在的自定义人设（id 相同即覆盖）
  Future<void> updateCustomPersona(Map<String, dynamic> persona) =>
      saveCustomPersona(persona);

  Future<void> deleteCustomPersona(String id) async {
    _customPersonas = _customPersonas.where((p) => p['id'] != id).toList();
    if (!_channelAvailable) return;
    try {
      await _channel.invokeMethod('deleteCustomPersona', {'id': id});
    } catch (_) {
      _channelAvailable = false;
    }
  }

  /// 恢复全部默认设置（亲密度 / 人设 / 自定义人设）
  Future<void> resetSettings() async {
    _intimacy = 50;
    _persona = '通用';
    _customPersonas = [];
    if (!_channelAvailable) return;
    try {
      await _channel.invokeMethod('resetSettings');
      onChanged?.call();
    } catch (_) {
      _channelAvailable = false;
    }
  }

  // ------------------------------------------------------------------
  // AI 回复流程状态机桥接（原生 AIReplyScheduler）
  // ------------------------------------------------------------------

  /// 发起一次 AI 回复生成，返回原生会话 JSON（含 batch），失败返回 null。
  /// 生成结果经 addAIReplyListener 订阅的监听器异步回传，勿在此同步等待。
  Future<Map<String, dynamic>?> startAIReply(
    String scene, {
    String? contextText,
  }) async {
    if (!_channelAvailable) return null;
    try {
      final raw = await _channel.invokeMethod<String>('startAIReply', {
        'scene': scene,
        'contextText': contextText ?? '',
      });
      return raw == null ? null : jsonDecode(raw) as Map<String, dynamic>;
    } catch (_) {
      return null;
    }
  }

  Future<void> cancelAIReply(int batch) async {
    if (!_channelAvailable) return;
    try {
      await _channel.invokeMethod('cancelAIReply', {'batch': batch});
    } catch (_) {}
  }

  /// LLM 生成完成后回填结果；成功返回 true（批次不匹配或已终态则 false）
  Future<bool> succeedAIReply(int batch, String text) async {
    if (!_channelAvailable) return false;
    try {
      return await _channel.invokeMethod<bool>('succeedAIReply', {
            'batch': batch,
            'text': text,
          }) ??
          false;
    } catch (_) {
      return false;
    }
  }

  Future<bool> failAIReply(int batch, String error) async {
    if (!_channelAvailable) return false;
    try {
      return await _channel.invokeMethod<bool>('failAIReply', {
            'batch': batch,
            'error': error,
          }) ??
          false;
    } catch (_) {
      return false;
    }
  }

  /// 查询当前会话快照（无活跃会话返回 null）
  Future<Map<String, dynamic>?> getAIReplyState() async {
    if (!_channelAvailable) return null;
    try {
      final raw = await _channel.invokeMethod<String>('getAIReplyState');
      return raw == null ? null : jsonDecode(raw) as Map<String, dynamic>;
    } catch (_) {
      return null;
    }
  }

  /// 读取 AI 回复历史（最新在前，每条为 JSON 字符串）
  Future<List<Map<String, dynamic>>> getAIReplyHistory() async {
    if (!_channelAvailable) return const [];
    try {
      final rawList = await _channel.invokeListMethod<String>('getAIReplyHistory') ?? const [];
      return rawList
          .map((e) => jsonDecode(e) as Map<String, dynamic>)
          .toList();
    } catch (_) {
      return const [];
    }
  }

  Future<void> clearAIReplyHistory() async {
    if (!_channelAvailable) return;
    try {
      await _channel.invokeMethod('clearAIReplyHistory');
    } catch (_) {}
  }

  /// 手动把一条回复写入历史（如 Flutter 页内选中 LLM 结果），供键盘"最近"行复用
  Future<void> addAIReplyHistory(String scene, String text) async {
    if (!_channelAvailable) return;
    try {
      await _channel.invokeMethod('addAIReplyHistory', {
        'scene': scene,
        'text': text,
      });
    } catch (_) {}
  }
}

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

  /// 订阅原生事件流：IME 键盘内修改亲密度/人设时，Flutter 侧实时刷新
  void _listenForExternalChanges() {
    _eventsChannel.receiveBroadcastStream().listen((event) {
      if (event is Map) {
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
}

import 'package:flutter/services.dart';

/// 悬浮球控制桥（Android 原生 FloatingBallService）
class FloatBallService {
  FloatBallService._();

  static final FloatBallService instance = FloatBallService._();

  static const MethodChannel _channel = MethodChannel('lovekey/floatball');

  /// 启动悬浮球；若未授予悬浮窗权限，会先跳转系统设置并返回 false
  Future<bool> start() async {
    try {
      return await _channel.invokeMethod<bool>('start') ?? false;
    } catch (_) {
      return false;
    }
  }

  Future<void> stop() async {
    try {
      await _channel.invokeMethod('stop');
    } catch (_) {}
  }

  Future<bool> isShowing() async {
    try {
      return await _channel.invokeMethod<bool>('isShowing') ?? false;
    } catch (_) {
      return false;
    }
  }
}

# LoveKey release 混淆规则（商业打包基线）

# ------------------------------------------------------------------
# Rime 引擎 JNI 绑定：native 方法由 C++ 侧按类名/方法名解析，
# 混淆会导致 UnsatisfiedLinkError，整包保留（含 Structs/RimeProto 等）
# ------------------------------------------------------------------
-keep class com.yuyan.inputmethod.core.** { *; }
-keep class com.osfans.trime.core.** { *; }

# ------------------------------------------------------------------
# 输入法服务与悬浮球入口：系统通过 manifest 全类名实例化，保留入口
# ------------------------------------------------------------------
-keep class com.example.lovekey_clone.LoveKeyIME { *; }
-keep class com.example.lovekey_clone.FloatingBallService { *; }
-keep class com.example.lovekey_clone.KeyboardInvokerActivity { *; }
-keep class com.example.lovekey_clone.MainActivity { *; }

# ------------------------------------------------------------------
# Compose / Lifecycle / Flutter embedding 均自带 consumer rules，
# 无需在此重复配置。以下供排查时参考：
# -keep class androidx.compose.** { *; }
# ------------------------------------------------------------------
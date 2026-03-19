# Lovekey Clone (AI 恋爱辅助输入法)

这是一款基于 **Flutter (前端宿主 App)** + **Android Kotlin/Jetpack Compose (原生输入法服务)** 开发的混合应用项目。

该项目复刻并扩展了流行的“AI 恋爱聊天助手/高情商回复键盘”的核心理念，为用户提供从应用内的情感咨询，到应用外的全局输入拦截与话术润色的完整闭环体验。

## 🚀 核心架构与功能

### 1. 宿主 App (Flutter 端)
主要负责用户配置、内容社区展示以及沉浸式的 AI 交互体验。
- **新手引导 (Onboarding):** 介绍高情商聊天助手的使用方式。
- **首页概览 (Home):** 包含一个独特的 **3D 情绪硬币翻转动画**（结合缩放与 `Matrix4` 空间翻转，正面显示爱心，背面正向显示情绪值“99%”）。
- **键盘人设 (Keyboard):** 采用炫酷的卡片“吸入 (Suck Animation)”动画配置 AI 生成风格。
- **话术库 (Scripts):** 类似百科的话术分类列表，供用户平时学习和复制。
- **恋爱咨询师 (Counselor):** 一个支持长对话的 AI 聊天界面，提供详尽的情感问题分析。
- **个人中心 (Profile):** 会员状态与订阅拦截管理的入口。

### 2. 全局原生键盘 (Android Native 端)
基于 `InputMethodService` 和最新的 `Jetpack Compose` 构建，这是整个应用的灵魂模块，可在微信、QQ 等任何聊天界面悬浮调用。
- **定制 T9 布局:** 精确复刻的高颜值九宫格基础键盘，包含搜索和中英切换功能。
- **动态顶栏 (Dynamic Top Area):**
  - *默认状态:* 显示“帮你回”（场景化快捷短语）与“超会说”（开启通用话题建议）的快捷按钮。
  - *打字状态 (草稿拦截):* 通过重写 `onUpdateSelection` 实时监听用户输入。当草稿不为空时，顶栏自动切换为“候选词区域”，并在右侧浮现带有**智能呼吸灯动画**的 `✨换个说法` 按钮。
- **AI 生成与上屏:** 点击 `✨换个说法` 后，展示带有加载骨架的“AI 建议列表”。用户选中某条高情商回复后，键盘会自动 `deleteSurroundingText` 抹除直白草稿，并一键 `commitText` 上屏润色后的话术。
- **商业化拦截网 (Paywall):** 内置免费额度与 VIP 状态校验。额度耗尽时，键盘底层弹出一个带遮罩的阻断式付费引导弹窗。

## 🛠️ 技术栈
- **跨平台 UI:** Flutter, Dart
- **Android 原生:** Kotlin, Android SDK 34
- **原生 UI 框架:** Jetpack Compose, Lifecycle-Runtime-KTX

## 📦 如何运行与测试

### 1. 运行 Flutter 宿主应用
确保您已连接 Android/iOS 模拟器或真机。
```bash
# 获取依赖
flutter pub get

# 运行主应用
flutter run
```

### 2. 编译并使用原生自定义键盘 (仅限 Android)
要测试原生的 AI 辅助键盘，您必须编译 Android 模块并在系统设置中手动启用该输入法。

```bash
# 进入 android 目录
cd android

# 执行 Debug 编译
./gradlew assembleDebug
```
*编译成功后，将生成的 APK 安装到设备。然后在设备的“设置 -> 系统 -> 语言和输入法 -> 虚拟键盘 -> 管理键盘”中，勾选开启 `LoveKey Clone`，并将其切换为默认输入法。*

## 🔮 未来规划 (To-Do)
- 接入真实的 **火山引擎 (Volcengine) 豆包大模型 API**，替换掉现有的本地假数据 (Mock Data) 和 `delay` 协程，实现真正的 AI 智能回复。
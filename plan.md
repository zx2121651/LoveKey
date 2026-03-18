1. **分析与官网查找总结**
   - **官网**: 经搜索，"恋爱键盘"相关应用主要上架于各大应用商店（如App Store的“恋爱键盘-AI聊天回复高情商输入法”），其相关域名（如 keyboard.boyastarring.com）多用于托管隐私政策和用户协议，并没有一个传统的展示型官网主页。
   - **功能分析**: 这是一类通过AI辅助聊天的工具。核心功能包括：
     - **恋爱帮回**：根据对方发来的话，AI智能生成多条高情商回复供选择。
     - **帮你措辞**：把用户直白的输入润色得更生动、幽默或浪漫。
     - **多重人设**：可以选择不同的回复风格（如暖男、霸总、幽默等）。
     - **键盘集成**：作为系统输入法（IME）直接在微信、QQ等聊天软件中使用，免去来回切换App的麻烦。
     - **注意**: 本仓库已经包含了一个 Flutter 项目用于实现部分应用UI界面。但由于用户的要求是“安卓原生开发”，因此我需要从头构建一个 Android 模块，或是将 Flutter 结合 Android 原生开发（实现系统输入法这一核心只能由原生 Android 实现，Flutter 很难直接作为 Android系统键盘）。结合现有仓库和要求，我们将在 `android/` 目录下创建一个原生键盘服务（InputMethodService）。

2. **安卓原生键盘开发实施计划**
   由于仓库已经是Flutter工程，但Flutter不支持直接开发Android输入法（IME），因此我们需要用 Android 原生代码在 `android/` 目录下编写一个自定义的 `InputMethodService`，这是“恋爱键盘”最核心的功能。

   - **步骤一：配置 Android 原生模块清单 (AndroidManifest.xml)**
     - 编辑 `android/app/src/main/AndroidManifest.xml`。
     - 增加 `<service>` 标签声明我们的原生键盘服务 `com.lovekey.clone.LoveKeyInputMethodService`。
     - 添加必需的 `<intent-filter>` ( `android.view.InputMethod` )。
     - 添加必需的权限 `android.permission.BIND_INPUT_METHOD`。
     - 提供对应的 XML 资源配置文件 `android/app/src/main/res/xml/method.xml`。

   - **步骤二：开发自定义键盘服务类 (LoveKeyInputMethodService)**
     - 创建 `android/app/src/main/java/com/lovekey/clone/LoveKeyInputMethodService.kt`（或者根据实际包名创建）。
     - 继承 `android.inputmethodservice.InputMethodService`。
     - 重写 `onCreateInputView()` 方法。
     - 由于我们要使用现代的 UI 开发，我们可以使用原生的 `Compose` 或是传统的 Android View (XML) 来搭建键盘的界面。由于是基础集成，我们使用一个简单的 Android `LinearLayout` 和原生的 `Button` 来实现核心输入。

   - **步骤三：开发键盘的UI与模拟AI回复逻辑 (原生 Android)**
     - 创建 `android/app/src/main/res/layout/keyboard_view.xml`。
     - 布局中包含一个文本输入框（模拟用户要回复的话），以及几个按钮（如“AI帮回”、“发出去”）。
     - 在 `LoveKeyInputMethodService.kt` 中编写逻辑：点击“发出去”或者选中某条AI回复时，通过 `currentInputConnection.commitText(text, 1)` 将内容输入到当前聊天的软件中。
     - 包含一段基础的Mock模拟数据，模拟AI提供的高情商回复。

   - **步骤四：校验 Android 项目的配置和构建 (Verify)**
     - 运行 `./gradlew tasks` 在 `android/` 目录下检查 Gradle 构建是否正常工作。
     - 运行 `./gradlew assembleDebug` 确保我们的原生键盘服务代码没有编译错误。

   - **步骤五：Pre-commit 检查**
     - Complete pre commit steps to make sure proper testing, verifications, reviews and reflections are done.

   - **步骤六：提交代码 (Submit)**
     - 以规范的中文 commit message 提交代码并请求审查。

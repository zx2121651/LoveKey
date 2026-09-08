package com.example.lovekey_clone

import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import java.util.concurrent.atomic.AtomicBoolean
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.yuyan.inputmethod.core.Rime
import java.io.File

class LoveKeyIME : InputMethodService() {

    private lateinit var lifecycleOwner: IMELifecycleOwner

    // Mutable state to hold the text before the cursor
    private val currentDraftText = mutableStateOf("")

    // State backed by the C++ Rime Engine
    private val currentComposingText = mutableStateOf("")
    private val currentCandidates = mutableStateOf<List<String>>(emptyList())

    // Shared settings with the Flutter host app (SharedPreferences bridge)
    private val intimacyLevel = mutableStateOf(50)
    private val personaName = mutableStateOf("通用")

    /** 设置变更监听注销句柄，避免泄漏 */
    private var settingsListenerUnregister: Runnable? = null

    /** Rime 引擎是否就绪（异步初始化） */
    private val rimeReady = AtomicBoolean(false)

    /** 初始化期间缓冲的按键，引擎就绪后回放 */
    private val pendingKeys = mutableListOf<String>()

    private val mainHandler = Handler(Looper.getMainLooper())

    /** 中/英文模式（引擎 ascii_mode 开关） */
    private val isAsciiMode = mutableStateOf(false)

    /** 上屏后的联想词（引擎支持时非空） */
    private val associateCandidates = mutableStateOf<List<String>>(emptyList())

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner = IMELifecycleOwner()
        lifecycleOwner.onCreate()

        val rimePath = filesDir.absolutePath + "/rime"

        // 1. Deploy assets to file system
        RimeDeployer.deployAssets(this, rimePath)

        // 2. Initialize YuyanIme Engine (Sogou Wrapper)
        //    后台线程初始化，避免词库加载阻塞主线程、卡住首弹键盘；
        //    就绪前输入的按键先缓冲，就绪后回放。
        rimeReady.set(false)
        Thread {
            runCatching { Rime.startup(this, false) }
            rimeReady.set(true)
            mainHandler.post { onRimeReady() }
        }.start()

        // 3. 实时同步 Flutter 侧设置：亲密度 / 人设 / 中英模式变化即时生效，无需等键盘重开
        settingsListenerUnregister = SettingsStore.registerChangeListener(
            this,
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    "intimacy_level" -> intimacyLevel.value = SettingsStore.getIntimacy(this)
                    "selected_persona" -> personaName.value = SettingsStore.getPersona(this)
                    // 悬浮球 / Flutter 侧切换中英：幂等应用目标值
                    "ascii_mode" -> applyAsciiMode(SettingsStore.getAsciiMode(this))
                }
            }
        )
    }

    // ------------------------------------------------------------------
    // Rime 增强：异步就绪 / 按键缓冲 / 翻页 / 中英切换 / 联想
    // ------------------------------------------------------------------

    /** 引擎就绪：同步西文模式状态，并回放初始化期间缓冲的按键 */
    private fun onRimeReady() {
        isAsciiMode.value = Rime.isAsciiMode
        // 以引擎实际状态为准，初始化设置桥（避免悬浮球菜单显示与键盘不一致）
        SettingsStore.setAsciiMode(this, Rime.isAsciiMode)
        val buffered = synchronized(pendingKeys) {
            pendingKeys.toList().also { pendingKeys.clear() }
        }
        buffered.forEach { handleKeyPress(it) }
    }

    private fun handleKeyPress(key: String) {
        hapticTick()
        if (!rimeReady.get()) {
            synchronized(pendingKeys) { pendingKeys.add(key) }
            return
        }
        // 多字符文本（颜文字 / 表情等）直接上屏，避免首字符被引擎拆进拼音态
        if (key.length > 1) {
            currentInputConnection?.commitText(key, 1)
            return
        }
        // 西文模式：按键直接上屏，不走引擎
        if (isAsciiMode.value) {
            currentInputConnection?.commitText(key, 1)
            return
        }
        val keycode = key.firstOrNull()?.code ?: 0
        val handled = Rime.processKey(keycode, 0)
        if (handled) {
            currentComposingText.value = Rime.compositionText
            currentInputConnection?.setComposingText(currentComposingText.value, 1)
            currentCandidates.value = Rime.mContext?.candidates?.map { it.text } ?: emptyList()
        } else {
            currentInputConnection?.commitText(key, 1)
        }
    }

    /** 中/英切换：拨动引擎 ascii_mode 开关 */
    private fun toggleAsciiMode() {
        hapticTick()
        if (!rimeReady.get()) return
        applyAsciiMode(!Rime.isAsciiMode)
    }

    /** 幂等设置中/英模式，并写回设置桥（悬浮球 / Flutter 侧可见、可联动） */
    private fun applyAsciiMode(target: Boolean) {
        if (!rimeReady.get()) return
        if (Rime.isAsciiMode == target) {
            // 状态已一致：仅同步 UI 与设置桥，不重复拨引擎
            isAsciiMode.value = target
            SettingsStore.setAsciiMode(this, target)
            return
        }
        Rime.setOption("ascii_mode", target)
        Rime.updateStatus()
        isAsciiMode.value = Rime.isAsciiMode
        SettingsStore.setAsciiMode(this, isAsciiMode.value)
        if (isAsciiMode.value) {
            // 切到西文时清空拼音输入态
            Rime.clearComposition()
            currentComposingText.value = ""
            currentCandidates.value = emptyList()
        }
    }

    private fun pageUpCandidates() {
        hapticTick()
        if (Rime.pageUp()) refreshCandidatesAfterPage()
    }

    private fun pageDownCandidates() {
        hapticTick()
        if (Rime.pageDown()) refreshCandidatesAfterPage()
    }

    /** 按键 / 候选等操作的反馈：触觉震动 + 按键音，均跟随设置开关 */
    private fun hapticTick() {
        val view = currentInputView ?: return
        if (SettingsStore.getKeySoundEnabled(this)) {
            // 按键音（系统 Click 音效，音量跟随系统设置）
            runCatching { view.playSoundEffect(android.view.SoundEffectConstants.CLICK) }
        }
        if (!SettingsStore.getHapticEnabled(this)) return
        val feedback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            HapticFeedbackConstants.KEYBOARD_TAP
        } else {
            HapticFeedbackConstants.VIRTUAL_KEY
        }
        runCatching { view.performHapticFeedback(feedback) }
    }

    private fun refreshCandidatesAfterPage() {
        currentCandidates.value = Rime.mContext?.candidates?.map { it.text } ?: emptyList()
        currentComposingText.value = Rime.compositionText
    }

    /** 候选上屏后：取联想词，并把上屏内容收录进剪贴板历史（供快捷插入） */
    private fun afterCommit(committedText: String) {
        if (committedText.isEmpty()) {
            associateCandidates.value = emptyList()
            return
        }
        SettingsStore.addClipboardItem(this, committedText)
        val associates = runCatching { Rime.getAssociateList(committedText.takeLast(1)) }
            .getOrNull()?.filterNotNull()?.filter { it.isNotBlank() } ?: emptyList()
        associateCandidates.value = associates
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        // Extract up to 100 characters before the cursor as the draft
        val textBefore = currentInputConnection?.getTextBeforeCursor(100, 0)?.toString() ?: ""
        currentDraftText.value = textBefore
    }

    override fun onCreateInputView(): View {
        val rootLayout = FrameLayout(this)
        rootLayout.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val draft = currentDraftText.value
                val composing = currentComposingText.value
                val candidates = currentCandidates.value

                // 候选翻页状态（仅拼音输入中有效）
                val rimeMenu = Rime.mContext?.menu
                val pageNo = rimeMenu?.pageNo ?: 0
                val canPrev = pageNo > 0
                val canNext = rimeMenu?.isLastPage == false

                MaterialTheme {
                    LoveKeyKeyboardUI(
                        draftText = draft,
                        composingText = composing,
                        candidates = candidates,
                        pageNo = pageNo,
                        canPrev = canPrev,
                        canNext = canNext,
                        asciiMode = isAsciiMode.value,
                        associates = associateCandidates.value,
                        intimacy = intimacyLevel.value,
                        personaName = personaName.value,
                        onToggleAscii = { toggleAsciiMode() },
                        onPageUp = { pageUpCandidates() },
                        onPageDown = { pageDownCandidates() },
                        onCommitAssociate = { word ->
                            hapticTick()
                            currentInputConnection?.commitText(word, 1)
                            associateCandidates.value = emptyList()
                        },
                        onSetIntimacy = { level ->
                            intimacyLevel.value = level
                            SettingsStore.setIntimacy(this@LoveKeyIME, level)
                        },
                        onSelectPersona = { name ->
                            personaName.value = name
                            SettingsStore.setPersona(this@LoveKeyIME, name)
                        },
                        onKeyPress = { key ->
                            handleKeyPress(key)
                        },
                        onCommitCandidate = { candidate ->
                            hapticTick()
                            val index = currentCandidates.value.indexOf(candidate)
                            if (index != -1) {
                                Rime.selectCandidate(index)

                                val commit = Rime.getRimeCommit()
                                val committed = if (commit?.commitText != null && commit.commitText.isNotEmpty()) {
                                    currentInputConnection?.commitText(commit.commitText, 1)
                                    commit.commitText
                                } else ""

                                currentComposingText.value = Rime.compositionText
                                if (currentComposingText.value.isEmpty()) {
                                    currentCandidates.value = emptyList()
                                } else {
                                    currentInputConnection?.setComposingText(currentComposingText.value, 1)
                                    val candidates = Rime.mContext?.candidates ?: emptyArray()
                                    currentCandidates.value = candidates.map { it.text }
                                }
                                afterCommit(committed)
                            } else {
                                currentInputConnection?.commitText(candidate, 1)
                                Rime.clearComposition()
                                currentComposingText.value = ""
                                currentCandidates.value = emptyList()
                                afterCommit(candidate)
                            }
                        },
                        onDelete = {
                            hapticTick()
                            if (currentComposingText.value.isNotEmpty()) {
                                Rime.processKey(0xff08, 0)

                                currentComposingText.value = Rime.compositionText

                                if (currentComposingText.value.isEmpty()) {
                                    currentInputConnection?.commitText("", 1)
                                    currentCandidates.value = emptyList()
                                } else {
                                    currentInputConnection?.setComposingText(currentComposingText.value, 1)
                                    val candidates = Rime.mContext?.candidates ?: emptyArray()
                                    currentCandidates.value = candidates.map { it.text }
                                }
                            } else {
                                currentInputConnection?.deleteSurroundingText(1, 0)
                            }
                        },
                        onReplaceDraft = { replacement ->
                            Rime.clearComposition()
                            currentComposingText.value = ""
                            currentCandidates.value = emptyList()

                            currentInputConnection?.deleteSurroundingText(draft.length, 0)
                            currentInputConnection?.commitText(replacement, 1)
                        },
                        onPerformAction = {
                            if (currentComposingText.value.isNotEmpty()) {
                                Rime.clearComposition()
                                currentInputConnection?.commitText(currentComposingText.value, 1)
                                currentComposingText.value = ""
                                currentCandidates.value = emptyList()
                            }
                            currentInputConnection?.performEditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH)
                        }
                    )
                }
            }
        }

        composeView.setViewTreeLifecycleOwner(lifecycleOwner)
        composeView.setViewTreeViewModelStoreOwner(lifecycleOwner)
        composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)

        rootLayout.addView(composeView)
        return rootLayout
    }

    override fun onWindowShown() {
        super.onWindowShown()
        lifecycleOwner.onResume()
        // Refresh shared settings when the keyboard is opened,
        // so changes made in the Flutter host app take effect immediately.
        intimacyLevel.value = SettingsStore.getIntimacy(this)
        personaName.value = SettingsStore.getPersona(this)
        if (rimeReady.get()) {
            isAsciiMode.value = Rime.isAsciiMode
        }
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        lifecycleOwner.onPause()
    }

    override fun onDestroy() {
        settingsListenerUnregister?.run()
        settingsListenerUnregister = null
        // 退出引擎：flush 用户词典（学习记录落盘到 rime_user/build）
        runCatching { Rime.destroy() }
        lifecycleOwner.onDestroy()
        super.onDestroy()
    }
}

@Composable
fun LoveKeyKeyboardUI(
    draftText: String,
    composingText: String,
    candidates: List<String>,
    pageNo: Int,
    canPrev: Boolean,
    canNext: Boolean,
    asciiMode: Boolean,
    associates: List<String>,
    intimacy: Int,
    personaName: String,
    onSetIntimacy: (Int) -> Unit,
    onSelectPersona: (String) -> Unit,
    onKeyPress: (String) -> Unit,
    onCommitCandidate: (String) -> Unit,
    onDelete: () -> Unit,
    onReplaceDraft: (String) -> Unit,
    onPerformAction: () -> Unit,
    onToggleAscii: () -> Unit,
    onPageUp: () -> Unit,
    onPageDown: () -> Unit,
    onCommitAssociate: (String) -> Unit
) {
    var isGenerating by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("keyboard") } // keyboard, ai_reply, quick_reply, custom_prompt, refine_draft, emoji, symbols
    var customPromptText by remember { mutableStateOf("") }
    var copiedText by remember { mutableStateOf("") }

    // 帮你回：当前选中的话术场景（通用/撩人/安慰/日常/吵架）
    var quickScene by rememberSaveable { mutableStateOf(SettingsStore.SCENE_GENERAL) }
    // 自定义话术输入框（长按"帮你回"面板内自定义项时弹出）
    var customPhraseText by remember { mutableStateOf("") }
    var showCustomPhraseInput by remember { mutableStateOf(false) }

    val context = LocalContext.current
    // 剪贴板面板：系统剪贴板当前内容 + 历史（打开时刷新）
    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
    var clipboardItems by remember { mutableStateOf<List<String>>(emptyList()) }
    fun refreshClipboard() {
        val sysClip = clipboardManager.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)?.text?.toString()
        clipboardItems = (listOfNotNull(sysClip?.takeIf { it.isNotBlank() }) + SettingsStore.getClipboardItems(context))
            .distinct()
            .take(12)
    }

    // VIP and Usage logic
    var isVip by remember { mutableStateOf(false) }
    var freeUsageCount by remember { mutableStateOf(3) }
    var showPaywall by remember { mutableStateOf(false) }

    fun checkAndUseFeature(action: () -> Unit) {
        if (isVip) {
            action()
        } else if (freeUsageCount > 0) {
            freeUsageCount--
            action()
        } else {
            showPaywall = true
        }
    }

    val coroutineScope = rememberCoroutineScope()

    // 键盘特殊键转发：@# / 123 / 表情 / 剪贴板键在 UI 层拦截，切换到对应面板，不进入 IME 引擎
    val innerOnKeyPress: (String) -> Unit = { key ->
        when (key) {
            "emoji" -> activeTab = "emoji"
            "@#", "123" -> activeTab = "symbols"
            "clipboard" -> {
                refreshClipboard()
                activeTab = "clipboard"
            }
            else -> onKeyPress(key)
        }
    }

    // Intimacy-aware reply pool (recomputed when intimacy / persona changes)
    val aiReplies = remember(intimacy, personaName) { buildReplies(intimacy, personaName) }
    // 场景话术：自定义话术优先 + 场景池 + 亲密度 + 人设
    val quickReplies = remember(context, quickScene, intimacy, personaName) {
        buildSceneQuickReplies(context, quickScene, intimacy, personaName)
    }

    // 快捷切换 chips：内置常用人设 + 当前使用的人设（可能来自 Flutter 人设市场/自定义）
    val personaChips =
        (listOf("通用", "高情商", "幽默", "温柔暖男", "暧昧拉扯", "土味情话", "霸总", "萌妹") + listOf(personaName))
            .distinct()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                SolidColor(Color(0xFFF4F6FE))
            )
            .padding(bottom = 8.dp) // Slight bottom padding
    ) {
        // Dynamic Top Area: Toolbar OR Candidate/Refine View
        if (draftText.isEmpty() && composingText.isEmpty()) {
            // Top Toolbar (Default state when no draft)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar / Logo Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF586AFE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("☺️", fontSize = 18.sp) // Simplified icon
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 帮你回
                Button(
                    onClick = {
                        if (activeTab != "quick_reply") {
                            checkAndUseFeature {
                                // Read the copied message as context (TA 的话)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                copiedText = clipboard?.primaryClip
                                    ?.takeIf { it.itemCount > 0 }
                                    ?.getItemAt(0)?.coerceToText(context)?.toString() ?: draftText
                                activeTab = "quick_reply"
                                isGenerating = true
                            }
                        } else {
                            activeTab = "keyboard"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFFFFF)),
                    border = BorderStroke(1.dp, Color(0xFFDFE2EC)),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp),
                    elevation = ButtonDefaults.elevation(0.dp)
                ) {
                    Text("帮你回", color = Color(0xFF2B2F35), fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 超会说
                Button(
                    onClick = {
                        if (activeTab != "ai_reply") {
                            checkAndUseFeature { activeTab = "ai_reply" }
                        } else {
                            activeTab = "keyboard"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFFFFF)),
                    border = BorderStroke(1.dp, Color(0xFFDFE2EC)),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp),
                    elevation = ButtonDefaults.elevation(0.dp)
                ) {
                    Text("超会说", color = Color(0xFF2B2F35), fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                // History Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White, RoundedCornerShape(18.dp)).border(1.dp, Color(0xFFDFE2EC), RoundedCornerShape(18.dp))
                        .clickable { /* History */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.List, contentDescription = "History", tint = Color(0xFF888888), modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Apps Grid Icon / AI Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White, RoundedCornerShape(18.dp)).border(1.dp, Color(0xFFDFE2EC), RoundedCornerShape(18.dp))
                        .clickable {
                            if (activeTab != "custom_prompt") {
                                checkAndUseFeature { activeTab = "custom_prompt" }
                            } else {
                                activeTab = "keyboard"
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "AI Prompt", tint = Color(0xFF888888), modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Heart / Quota
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFCDD2))
                        .clickable { showPaywall = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isVip) "VIP" else "$freeUsageCount", color = Color(0xFF2B2F35), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Candidate / Refine View (When draft exists)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Candidates from the Mini T9 Dictionary
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(candidates) { word ->
                        Text(
                            text = word,
                            color = Color(0xFF2B2F35),
                            fontSize = 16.sp,
                            modifier = Modifier.clickable { onCommitCandidate(word) }.padding(vertical = 8.dp)
                        )
                    }
                }

                // 候选翻页：有候选且存在上一页/下一页时显示翻页箭头
                if (candidates.isNotEmpty() && (canPrev || canNext)) {
                    IconButton(
                        onClick = onPageUp,
                        enabled = canPrev,
                        modifier = Modifier.size(28.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "上一页",
                            tint = if (canPrev) Color(0xFF586AFE) else Color(0xFFCCCCCC),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text("${pageNo + 1}", color = Color(0xFF888888), fontSize = 11.sp)
                    IconButton(
                        onClick = onPageDown,
                        enabled = canNext,
                        modifier = Modifier.size(28.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "下一页",
                            tint = if (canNext) Color(0xFF586AFE) else Color(0xFFCCCCCC),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Breathing Animation logic for long drafts
                val isLongDraft = draftText.length >= 10
                val infiniteTransition = rememberInfiniteTransition()
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isLongDraft) 1.08f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                // ✨换个说法 (The single entry point)
                Button(
                    onClick = {
                        checkAndUseFeature {
                            activeTab = "refine_draft"
                            isGenerating = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF586AFE)),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale
                        ),
                    elevation = ButtonDefaults.elevation(2.dp)
                ) {
                    Text("✨换个说法", color = Color(0xFFFFFFFF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 联想词行：上屏后由引擎联想（不支持时为空，整行隐藏）
        if (associates.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFECEEFF))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Text(
                        text = "联想",
                        color = Color(0xFF8A9CFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(associates) { word ->
                    Text(
                        text = word,
                        color = Color(0xFF2B2F35),
                        fontSize = 15.sp,
                        modifier = Modifier
                            .clickable { onCommitAssociate(word) }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }

        if (activeTab == "ai_reply") {
            // AI Reply Overlay Mode
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(0xFFF4F6FE))
                    .height(300.dp)
            ) {
                // Header of AI Reply
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "超会说✨",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF2B2F35)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Intimacy indicator
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFE4EC), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${intimacyLabel(intimacy)} ${intimacy}%",
                            fontSize = 11.sp,
                            color = Color(0xFFE05A86),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF888888),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { activeTab = "keyboard" }
                    )
                }

                // Persona chips (switch style on the fly)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(personaChips) { chip ->
                        val selected = chip == personaName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (selected) Color(0xFF586AFE) else Color.White,
                                    RoundedCornerShape(14.dp)
                                )
                                .border(
                                    1.dp,
                                    if (selected) Color(0xFF586AFE) else Color(0xFFDFE2EC),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    if (personaName != chip) onSelectPersona(chip)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chip,
                                color = if (selected) Color.White else Color(0xFF2B2F35),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // List of Replies
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(aiReplies) { replyPair ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFEEF0F9), RoundedCornerShape(12.dp))
                                .clickable {
                                    onCommitCandidate(replyPair.second)
                                    activeTab = "keyboard"
                                }
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = replyPair.first,
                                    color = Color(0xFF8A9CFF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = replyPair.second,
                                        color = Color(0xFF2B2F35),
                                        fontSize = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = Color(0xFF888888),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "More", tint = Color(0xFF888888))
                            }
                        }
                    }
                }
            }
        } else if (activeTab == "quick_reply") {
            // 帮你回：基于复制的对方消息 + 亲密度 + 人设生成回复
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(0xFFF4F6FE))
                    .height(320.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "帮你回 ✨",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF2B2F35)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // 亲密度调节（键盘内直接调节回复分寸）
                    Text(
                        text = "亲密度",
                        fontSize = 11.sp,
                        color = Color(0xFF585C62)
                    )
                    Slider(
                        value = intimacy.toFloat(),
                        onValueChange = { onSetIntimacy(it.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier.width(110.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF586AFE),
                            activeTrackColor = Color(0xFF586AFE),
                            inactiveTrackColor = Color(0xFF586AFE).copy(alpha = 0.15f)
                        )
                    )
                    Text(
                        text = "${intimacyLabel(intimacy)} ${intimacy}%",
                        fontSize = 11.sp,
                        color = Color(0xFFE05A86),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF888888),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { activeTab = "keyboard" }
                    )
                }

                if (copiedText.isNotBlank()) {
                    Text(
                        text = "TA 说：$copiedText",
                        color = Color(0xFF8A8F99),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 场景切换 chips：通用 / 撩人 / 安慰 / 日常 / 吵架 + 自定义话术入口
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingsStore.ALL_SCENES.forEach { scene ->
                            val selected = quickScene == scene
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (selected) Color(0xFF586AFE) else Color.White,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .border(1.dp, if (selected) Color(0xFF586AFE) else Color(0xFFDFE2EC), RoundedCornerShape(14.dp))
                                    .clickable { quickScene = scene }
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = scene,
                                    color = if (selected) Color.White else Color(0xFF585C62),
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // 自定义话术入口：添加到当前场景
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0xFFDFE2EC), CircleShape)
                            .clickable {
                                customPhraseText = ""
                                showCustomPhraseInput = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加自定义话术",
                            tint = Color(0xFF586AFE),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 自定义话术输入区
                if (showCustomPhraseInput) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customPhraseText,
                            onValueChange = { customPhraseText = it },
                            modifier = Modifier.weight(1f).height(44.dp),
                            placeholder = { Text("输入自定义话术…", color = Color(0xFF9AA0AC), fontSize = 13.sp) },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                backgroundColor = Color.White,
                                unfocusedBorderColor = Color(0xFFDFE2EC),
                                focusedBorderColor = Color(0xFF8A9CFF)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val text = customPhraseText.trim()
                                if (text.isNotEmpty()) {
                                    SettingsStore.saveCustomPhrase(
                                        context,
                                        org.json.JSONObject().apply {
                                            put("id", "custom_" + System.currentTimeMillis())
                                            put("text", text)
                                            put("label", "自定义")
                                            put("scene", quickScene)
                                        }
                                    )
                                    customPhraseText = ""
                                    showCustomPhraseInput = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF586AFE)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(44.dp),
                            enabled = customPhraseText.isNotBlank()
                        ) {
                            Text("添加", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 回复列表（随场景 / 亲密度 / 人设变化）
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(quickReplies) { replyPair ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFEEF0F9), RoundedCornerShape(12.dp))
                                .clickable {
                                    onCommitCandidate(replyPair.second)
                                    activeTab = "keyboard"
                                }
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = replyPair.first,
                                    color = Color(0xFF8A9CFF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = replyPair.second,
                                        color = Color(0xFF2B2F35),
                                        fontSize = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint = Color(0xFF586AFE),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (activeTab == "refine_draft") {
            // Refine Draft Mode
            // Simulate generation (intimacy-aware)
            var refinedResults by remember { mutableStateOf(emptyList<Pair<String, String>>()) }

            LaunchedEffect(Unit) {
                if (isGenerating) {
                    refinedResults = emptyList()
                    delay(1200) // fake delay
                    refinedResults = buildReplies(intimacy, personaName)
                    isGenerating = false
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(0xFFF4F6FE))
                    .height(300.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✨ 为你润色草稿",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF2B2F35)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF888888),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { activeTab = "keyboard" }
                    )
                }

                if (isGenerating) {
                    // Loading State
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF8A9CFF), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("AI 恋爱专家正在分析...", color = Color(0xFF888888), fontSize = 14.sp)
                        }
                    }
                } else {
                    // List of Refined Replies
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(refinedResults) { replyPair ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFEEF0F9), RoundedCornerShape(12.dp))
                                    .clickable {
                                        onReplaceDraft(replyPair.second)
                                        activeTab = "keyboard"
                                    }
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = replyPair.first,
                                        color = Color(0xFFFFA000),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = replyPair.second,
                                        color = Color(0xFF2B2F35),
                                        fontSize = 15.sp,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (activeTab == "custom_prompt") {
            // Custom Prompt Mode ("AI 图标")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color.White, RoundedCornerShape(18.dp)).border(1.dp, Color(0xFFDFE2EC), RoundedCornerShape(18.dp))
                    .height(300.dp)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "写下你的草稿或意图",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF2B2F35)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF888888),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { activeTab = "keyboard" }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = customPromptText,
                    onValueChange = { customPromptText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = { Text("例如：告诉老板我今天病了请假一天，语气委婉一点", color = Color(0xFF585C62), fontSize = 14.sp) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color(0xFFF3F4F6),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color(0xFF8A9CFF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (customPromptText.isNotEmpty()) {
                            isGenerating = true
                            // Simulate network request
                            coroutineScope.launch {
                                delay(1500) // fake delay
                                onCommitCandidate("老板您好，非常抱歉打扰您。我今天身体很不舒服，需要请假一天去医院检查。希望能得到您的批准，手头的工作我已经和同事交接好了。")
                                isGenerating = false
                                activeTab = "keyboard"
                                customPromptText = ""
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF586AFE)),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = Color(0xFF2B2F35), modifier = Modifier.size(24.dp))
                    } else {
                        Text("帮我润色", color = Color(0xFFFFFFFF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (activeTab == "emoji") {
            EmojiPanel(
                onKeyPress = { innerOnKeyPress(it) },
                onClose = { activeTab = "keyboard" }
            )
        } else if (activeTab == "symbols") {
            SymbolPanel(
                onKeyPress = { innerOnKeyPress(it) },
                onClose = { activeTab = "keyboard" }
            )
        } else if (activeTab == "clipboard") {
            ClipboardPanel(
                items = clipboardItems,
                onKeyPress = { innerOnKeyPress(it) },
                onDelete = { text ->
                    SettingsStore.deleteClipboardItem(context, text)
                    refreshClipboard()
                },
                onClear = {
                    SettingsStore.clearClipboard(context)
                    refreshClipboard()
                },
                onClose = { activeTab = "keyboard" }
            )
        } else {
            // T9 Keyboard Mode
            T9KeyboardGrid(
                asciiMode = asciiMode,
                onToggleAscii = onToggleAscii,
                onKeyPress = innerOnKeyPress,
                onDelete = onDelete,
                onPerformAction = onPerformAction
            )
        }

        // Paywall Overlay
        if (showPaywall) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // matches the height of the keyboard overlays
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(enabled = true, onClick = { /* consume click to prevent interactions underneath */ }),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(18.dp)).border(1.dp, Color(0xFFDFE2EC), RoundedCornerShape(18.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF888888),
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.End)
                            .clickable { showPaywall = false }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "免费次数已用完",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF2B2F35)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "升级高级版，解锁无限次 AI 对话、100+高情商人设及专属自定义指令。",
                        color = Color(0xFF585C62),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            isVip = true
                            showPaywall = false
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFA000)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("首单 7 天免费试用 · ¥48 / 月  立即解锁", color = Color(0xFF2B2F35), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "¥98 / 季 · 随时取消",
                        color = Color(0xFF585C62),
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            isVip = true
                            showPaywall = false
                        }
                    )
                }
            }
        }
        } // End of Box Stack
    }

@Composable
fun T9KeyboardGrid(
    asciiMode: Boolean,
    onToggleAscii: () -> Unit,
    onKeyPress: (String) -> Unit,
    onDelete: () -> Unit,
    onPerformAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // We'll hardcode the grid layout to match the screenshot precisely for the 4 rows

        // Row 1
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            KeyButton(text = ",", modifier = Modifier.weight(1f), bgColor = Color(0xFFB0B3BE), onClick = { onKeyPress(",") })
            KeyButton(text = "@#", modifier = Modifier.weight(1.5f), onClick = { onKeyPress("@#") })
            KeyButton(text = "ABC\n2", modifier = Modifier.weight(1.5f), onClick = { onKeyPress("2") })
            KeyButton(text = "DEF\n3", modifier = Modifier.weight(1.5f), onClick = { onKeyPress("3") })
            KeyButton(text = "⌫", modifier = Modifier.weight(1.2f), bgColor = Color(0xFFB0B3BE), onClick = onDelete)
        }

        // Row 2
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            KeyButton(text = "。", modifier = Modifier.weight(1f), bgColor = Color(0xFFB0B3BE), onClick = { onKeyPress("。") })
            KeyButton(text = "GHI\n4", modifier = Modifier.weight(1.5f), onClick = { onKeyPress("4") })
            KeyButton(text = "JKL\n5", modifier = Modifier.weight(1.5f), onClick = { onKeyPress("5") })
            KeyButton(text = "MNO\n6", modifier = Modifier.weight(1.5f), onClick = { onKeyPress("6") })
            KeyButton(text = "换行", modifier = Modifier.weight(1.2f), bgColor = Color(0xFFB0B3BE), onClick = { onKeyPress("\n") })
        }

        // Row 3 & 4 need special handling for the tall "搜索" (Search) button
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().height(102.dp)) {
            // Left Column (rows 3 and 4)
            Column(modifier = Modifier.weight(5.5f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Row 3 left
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    KeyButton(text = "?", modifier = Modifier.weight(1f), bgColor = Color(0xFFB0B3BE), onClick = { onKeyPress("?") })
                    KeyButton(text = "PQRS\n7", modifier = Modifier.weight(1.5f), onClick = { onKeyPress("7") })
                    KeyButton(text = "TUV\n8", modifier = Modifier.weight(1.5f), onClick = { onKeyPress("8") })
                    KeyButton(text = "WXYZ\n9", modifier = Modifier.weight(1.5f), onClick = { onKeyPress("9") })
                }
                // Row 4 left
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    KeyButton(text = "123", modifier = Modifier.weight(1.2f), onClick = { onKeyPress("123") })
                    KeyButton(text = "😊", modifier = Modifier.weight(1.2f), onClick = { onKeyPress("emoji") })
                    KeyButton(text = "📋", modifier = Modifier.weight(1.2f), onClick = { onKeyPress("clipboard") })
                    KeyButton(text = "␣", modifier = Modifier.weight(2.6f), onClick = { onKeyPress(" ") }) // Spacebar spans 2 columns
                    KeyButton(text = if (asciiMode) "EN" else "中/英", modifier = Modifier.weight(1.2f), onClick = onToggleAscii)
                }
            }

            // Right Column (Tall Search button)
            KeyButton(
                text = "搜索",
                modifier = Modifier.weight(1.2f).fillMaxHeight(),
                bgColor = Color(0xFFB0B3BE),
                onClick = onPerformAction
            )
        }
    }
}

@Composable
fun KeyButton(
    text: String,
    modifier: Modifier = Modifier,
    bgColor: Color = Color.White,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = bgColor),
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.elevation(0.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.height(48.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF2B2F35),
            fontSize = if (text.length > 3) 14.sp else 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ---------------------------------------------------------------------------
// 表情 / 颜文字面板 & 符号面板
// ---------------------------------------------------------------------------

/** 恋爱场景常用 Emoji */
private val LOVE_EMOJIS = listOf(
    "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍",
    "💖", "💘", "💝", "💞", "💕", "💗", "💓", "💋",
    "😘", "😍", "🥰", "😊", "😉", "😜", "🤪", "😏",
    "😳", "🥺", "😢", "😭", "🤗", "😎", "🥳", "😴",
    "🙈", "🙉", "🙊", "👀", "🔥", "✨", "⭐", "🌹",
    "🌙", "🎁", "💍", "🌸", "💫", "🍓", "🧸", "🌈"
)

/** 恋爱场景常用颜文字 */
private val LOVE_KAOMOJI = listOf(
    "(≧▽≦)", "(●'◡'●)", "(◕‿◕)", "(*≧ω≦)",
    "(´▽`)", "(๑˃ᴗ˂)ﻭ", "ヾ(≧▽≦*)o", "(*´∀`*)",
    "( ˘ ³˘)♥", "♡(ŐωŐ人)", "(づ￣3￣)づ", "｡ﾟ(ﾟ´ω`ﾟ)ﾟ｡",
    "(^・ω・^ )", "(｡♥‿♥｡)", "✧(≖ ◡ ≖✿)", "₍₍◝(・ω・)◟₎₎",
    "(๑•̀ㅂ•́)و✧", "٩(๑❛ᴗ❛๑)۶", "(づ｡◕‿‿◕｡)づ", "♡(∩o∩)♡"
)

/** 符号面板内容：每 4 行为一页（常用标点 / 数字运算 / 特殊符号） */
private val SYMBOL_PAGES = listOf(
    listOf(",", "。", "、", "；", "：", "？", "！", "…"),
    listOf("“", "”", "‘", "’", "（", "）", "【", "】"),
    listOf("《", "》", "—", "·", "～", "@", "#", "&"),
    listOf("0", "1", "2", "3", "4", "5", "6", "7"),
    listOf("8", "9", "+", "-", "×", "÷", "=", "%"),
    listOf("$", "€", "£", "¥", "^", "*", "_", "|"),
    listOf("\\", "/", "~", "`", "<", ">", "[", "]"),
    listOf("{", "}", "(", ")", "☆", "★", "☀", "☾")
)

@Composable
private fun EmojiPanel(
    onKeyPress: (String) -> Unit,
    onClose: () -> Unit
) {
    var emojiTab by rememberSaveable { mutableStateOf(0) } // 0=表情 1=颜文字
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color(0xFFF4F6FE))
            .height(320.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (emojiTab == 0) "表情" else "颜文字",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color(0xFF2B2F35)
            )
            Spacer(modifier = Modifier.weight(1f))
            // 表情 / 颜文字 tab
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFFDFE2EC), RoundedCornerShape(14.dp)),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf("表情", "颜文字").forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (emojiTab == index) Color(0xFF586AFE) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { emojiTab = index }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (emojiTab == index) Color.White else Color(0xFF585C62),
                            fontSize = 12.sp,
                            fontWeight = if (emojiTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF888888),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onClose() }
            )
        }

        if (emojiTab == 0) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(LOVE_EMOJIS) { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .clickable {
                                onKeyPress(emoji)
                                onClose()
                            }
                            .padding(top = 6.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(LOVE_KAOMOJI) { kaomoji ->
                    Text(
                        text = kaomoji,
                        color = Color(0xFF2B2F35),
                        fontSize = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .clickable {
                                onKeyPress(kaomoji)
                                onClose()
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SymbolPanel(
    onKeyPress: (String) -> Unit,
    onClose: () -> Unit
) {
    val totalPages = (SYMBOL_PAGES.size + 3) / 4
    var symbolPage by rememberSaveable { mutableStateOf(0) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color(0xFFF4F6FE))
            .height(320.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "符号",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color(0xFF2B2F35)
            )
            Spacer(modifier = Modifier.width(8.dp))
            // 页码指示
            repeat(totalPages) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == symbolPage) 18.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == symbolPage) Color(0xFF586AFE) else Color(0xFFC9CDD8)
                        )
                        .clickable { symbolPage = index }
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF888888),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onClose() }
            )
        }

        val start = symbolPage * 4
        val end = (start + 4).coerceAtMost(SYMBOL_PAGES.size)
        for (rowIndex in start until end) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SYMBOL_PAGES[rowIndex].forEach { symbol ->
                    Text(
                        text = symbol,
                        color = Color(0xFF2B2F35),
                        fontSize = 18.sp,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .clickable {
                                onKeyPress(symbol)
                            }
                            .padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF586AFE)),
            shape = RoundedCornerShape(22.dp)
        ) {
            Text("完成", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ClipboardPanel(
    items: List<String>,
    onKeyPress: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color(0xFFF4F6FE))
            .height(320.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "剪贴板",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color(0xFF2B2F35)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "最近上屏内容自动收录",
                color = Color(0xFF9AA0AC),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            if (items.isNotEmpty()) {
                Text(
                    text = "清空",
                    color = Color(0xFFE05A86),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable { onClear() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF888888),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onClose() }
            )
        }

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "暂无内容，上屏的文字会自动收录到这里",
                        color = Color(0xFF9AA0AC),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(items) { index, text ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .clickable {
                                onKeyPress(text)
                                onClose()
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (index == 0) "当前剪贴板" else "历史 $index",
                                color = Color(0xFF9AA0AC),
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = text,
                                color = Color(0xFF2B2F35),
                                fontSize = 14.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "删除",
                            tint = Color(0xFFC9CDD8),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onDelete(text) }
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 亲密度 & 人设 -> 回复生成（本地 Mock，后续由真 AI 接口替换）
// ---------------------------------------------------------------------------

private fun intimacyLabel(level: Int): String = when {
    level < 20 -> "陌生人"
    level < 40 -> "刚认识"
    level < 60 -> "普通朋友"
    level < 80 -> "暧昧期"
    else -> "灵魂伴侣"
}

/** 人设专属话术池 */
private fun personaPool(persona: String): List<Pair<String, String>> = when (persona) {
    "幽默" -> listOf(
        Pair("幽默", "我掐指一算，咱们今天适合多聊两句~"),
        Pair("幽默", "你这句要是晚发两分钟，我就要开始想你了。"),
        Pair("幽默", "哦？这个展开方式有点可爱。"),
        Pair("幽默", "大师说你这消息自带好运buff，我先接住了。")
    )
    "土味情话" -> listOf(
        Pair("土味情话", "你知道你和星星的区别吗？星星在天上，你在我心里。"),
        Pair("土味情话", "近朱者赤，近你者甜。"),
        Pair("土味情话", "我想买一块地，你的死心塌地。"),
        Pair("土味情话", "你累不累？你在我脑子里跑一天了。")
    )
    "霸总" -> listOf(
        Pair("霸总", "嗯，这件事我来安排，你不用操心。"),
        Pair("霸总", "记住，你只需要做自己想做的事。"),
        Pair("霸总", "我的字典里，没有拒绝你这一项。"),
        Pair("霸总", "跟了我，就没人敢让你受委屈。")
    )
    "萌妹" -> listOf(
        Pair("萌妹", "呜呜呜你太好啦，人家好感动~"),
        Pair("萌妹", "真的嘛！那我可要开心一整天啦~"),
        Pair("萌妹", "和你聊天小鹿乱撞，你负责扶稳它！"),
        Pair("萌妹", "拉勾勾，说好了要一直这么聊下去哦~")
    )
    "温柔暖男" -> listOf(
        Pair("温柔暖男", "别急，有我在呢，慢慢说。"),
        Pair("温柔暖男", "今天辛苦啦，记得好好休息。"),
        Pair("温柔暖男", "不管发生什么，我都会站在你这边。"),
        Pair("温柔暖男", "你先忙，我等你，随时都在。")
    )
    "暧昧拉扯" -> listOf(
        Pair("暧昧拉扯", "你这样说，我可要当真了哦～"),
        Pair("暧昧拉扯", "那就要看你表现咯～"),
        Pair("暧昧拉扯", "只有你能让我秒回消息。"),
        Pair("暧昧拉扯", "你猜我现在在想谁？")
    )
    "高情商" -> listOf(
        Pair("高情商", "和你聊天总是很舒服，一点都不累。"),
        Pair("高情商", "你说得对，这个角度我之前还真没想过。"),
        Pair("高情商", "哈哈，你总是能让我心情变好。")
    )
    // ---- 恋爱人设 ----
    "恋爱大师" -> listOf(
        Pair("恋爱大师", "别急，感情讲究的是节奏，稳一点反而更动人。"),
        Pair("恋爱大师", "这种时候不用解释太多，真诚比套路更有杀伤力。"),
        Pair("恋爱大师", "记住，让她开心不是你的事，是你唯一的事。"),
        Pair("恋爱大师", "适当的留白，比一直追问更有吸引力。")
    )
    "情场高手" -> listOf(
        Pair("情场高手", "你的眼光不错，不过我更在意你怎么想。"),
        Pair("情场高手", "气氛到这儿了，不说点什么多可惜。"),
        Pair("情场高手", "先别急着表态，让对方多好奇一会儿。"),
        Pair("情场高手", "越轻松，越有魅力。")
    )
    "贴心暖男" -> listOf(
        Pair("贴心暖男", "别急，有我在呢，慢慢说。"),
        Pair("贴心暖男", "今天辛苦啦，记得好好休息。"),
        Pair("贴心暖男", "不管发生什么，我都会站在你这边。"),
        Pair("贴心暖男", "你先忙，我等你，随时都在。")
    )
    "花式撩人" -> listOf(
        Pair("花式撩人", "你今天是不是偷偷喷了香水？我隔着屏幕都心动了。"),
        Pair("花式撩人", "巧了，我刚好在想你，你就发消息来了。"),
        Pair("花式撩人", "我有个超能力，见到你就会开心。"),
        Pair("花式撩人", "月亮不睡我不睡，你不回我我失眠。")
    )
    "撩女生" -> listOf(
        Pair("撩女生", "你笑起来的样子，一定很好看。"),
        Pair("撩女生", "别老夸我，我会当真然后赖上你的。"),
        Pair("撩女生", "这条消息，我可是斟酌了三分钟才发的。"),
        Pair("撩女生", "跟你聊天，输赢都行，开心就行。")
    )
    // ---- 聊天必备 ----
    "情绪稳定" -> listOf(
        Pair("情绪稳定", "先冷静下来，我们一起看看怎么解决。"),
        Pair("情绪稳定", "我知道你现在很难受，我陪你。"),
        Pair("情绪稳定", "这件事不怪你，别太自责。"),
        Pair("情绪稳定", "先深呼吸，天塌下来有我帮你扛一半。")
    )
    "小奶狗" -> listOf(
        Pair("小奶狗", "姐姐～你今天有想我一点点嘛～"),
        Pair("小奶狗", "抱抱～我刚刚一直盯着手机等你消息。"),
        Pair("小奶狗", "你不理我，我就要委屈巴巴了。"),
        Pair("小奶狗", "拉钩钩，你要一直这么宠我哦～")
    )
    "御姐" -> listOf(
        Pair("御姐", "小朋友，有事说事，别吞吞吐吐的。"),
        Pair("御姐", "嗯？想我直说，不用绕弯子。"),
        Pair("御姐", "乖，听话的人才有奖励。"),
        Pair("御姐", "我的时间很贵，但给你的不算。")
    )
    // ---- 职场人设 ----
    "职场精英" -> listOf(
        Pair("职场精英", "这个需求我梳理一下，稍后给你一份结论。"),
        Pair("职场精英", "我建议先对齐目标，再定执行方案。"),
        Pair("职场精英", "收到，我会在截止前同步进度。"),
        Pair("职场精英", "数据我看过了，按这个方向推进没问题。")
    )
    "社牛" -> listOf(
        Pair("社牛", "哈哈这个局必须算我一个！"),
        Pair("社牛", "回头介绍个朋友给你，超有意思。"),
        Pair("社牛", "改天组个局，把大家都叫上。"),
        Pair("社牛", "跟你聊天真对味，下次继续。")
    )
    "低调内敛" -> listOf(
        Pair("低调内敛", "嗯，我了解了。"),
        Pair("低调内敛", "这件事我心里有数，你放心。"),
        Pair("低调内敛", "先不声张，等有结果了再聊。"),
        Pair("低调内敛", "做好自己该做的，其他的交给时间。")
    )
    "彩虹夸夸" -> listOf(
        Pair("彩虹夸夸", "你今天的思路也太清晰了吧！"),
        Pair("彩虹夸夸", "这个方案细节，全组就你想到。"),
        Pair("彩虹夸夸", "跟你合作，效率直接拉满。"),
        Pair("彩虹夸夸", "你这状态，就是传说中的发光体。")
    )
    // ---- 十二星座 ----
    "白羊座" -> listOf(
        Pair("白羊座", "别纠结了，喜欢就上，犹豫就是输。"),
        Pair("白羊座", "这事儿我拍板，现在就去办！"),
        Pair("白羊座", "直球永远是最好用的武器。"),
        Pair("白羊座", "我要是喜欢一个人，藏不住的。")
    )
    "金牛座" -> listOf(
        Pair("金牛座", "慢慢来，好感情不怕等。"),
        Pair("金牛座", "认定的人，我会一直坚持。"),
        Pair("金牛座", "比起花言巧语，我更信实际行动。"),
        Pair("金牛座", "今天也辛苦你啦，记得犒劳自己。")
    )
    "双子座" -> listOf(
        Pair("双子座", "诶我跟你说，我今天有个超好玩的事！"),
        Pair("双子座", "这个话题我能跟你聊一整天不重样。"),
        Pair("双子座", "想法嘛，我脑子里随时有一打。"),
        Pair("双子座", "你猜我是认真的还是逗你的？")
    )
    "狮子座" -> listOf(
        Pair("狮子座", "有我在，你只管往前走。"),
        Pair("狮子座", "喜欢就要大大方方，藏着掖着多没意思。"),
        Pair("狮子座", "我认定的，就一定要护到底。"),
        Pair("狮子座", "别担心，天塌下来我顶着。")
    )
    "天秤座" -> listOf(
        Pair("天秤座", "和你在一起，氛围总是刚刚好。"),
        Pair("天秤座", "这件事咱们商量着来，都开心最重要。"),
        Pair("天秤座", "优雅一点，事情会顺利很多。"),
        Pair("天秤座", "你的每个选择，我都支持。")
    )
    "射手座" -> listOf(
        Pair("射手座", "走啊，趁年轻，想干嘛就干嘛！"),
        Pair("射手座", "别被规矩困住了，开心最重要。"),
        Pair("射手座", "我这个人，喜欢就直说。"),
        Pair("射手座", "明天的事交给明天，今晚先痛快聊。")
    )
    "摩羯座" -> listOf(
        Pair("摩羯座", "别急，我在规划，给你一个稳妥的答案。"),
        Pair("摩羯座", "承诺的事，我会一步步做到。"),
        Pair("摩羯座", "理性一点，感情才能走得更远。"),
        Pair("摩羯座", "你愿意等，我就不会让你失望。")
    )
    // ---- MBTI ----
    "ENFJ 主人公" -> listOf(
        Pair("ENFJ", "你有这种想法，说明你特别有想法，我支持你！"),
        Pair("ENFJ", "别怕，你的背后有我。"),
        Pair("ENFJ", "和你在一起，我感觉整个人都被点亮了。"),
        Pair("ENFJ", "走，咱们一起把这件事做成。")
    )
    "INTJ 建筑师" -> listOf(
        Pair("INTJ", "想清楚目标，再谈别的。"),
        Pair("INTJ", "计划我已经列好了，按步骤来就行。"),
        Pair("INTJ", "我话不多，但说出口的都会兑现。"),
        Pair("INTJ", "你的优势很明显，别自己看不见。")
    )
    "INFP 调停者" -> listOf(
        Pair("INFP", "我觉得你内心深处，一定是个很温柔的人。"),
        Pair("INFP", "世界很大，但懂你的没几个，我算一个。"),
        Pair("INFP", "慢慢来，按照你的节奏生活就好。"),
        Pair("INFP", "你值得被认真对待。")
    )
    "ESTP 企业家" -> listOf(
        Pair("ESTP", "别分析来分析去了，先干了再说！"),
        Pair("ESTP", "这局交给我，稳赢。"),
        Pair("ESTP", "心动不如行动，现在就去。"),
        Pair("ESTP", "输赢都痛快，别留遗憾。")
    )
    else -> emptyList()
}

/** 亲密度话术池：越低越克制疏离，越高越暧昧亲密 */
private fun intimacyPool(level: Int): List<Pair<String, String>> = when {
    level < 20 -> listOf(
        Pair("得体", "好的，我看到了，谢谢你告诉我。"),
        Pair("自然", "收到～我这边还有点事，晚点再详细回复你。"),
        Pair("礼貌", "嗯嗯，明白你的意思了。")
    )
    level < 40 -> listOf(
        Pair("友好", "哈哈，你这么说挺有意思的，具体说说？"),
        Pair("自然", "我也有类似的感觉，可以多聊聊。"),
        Pair("得体温和", "原来如此，看来你是个很有趣的人。")
    )
    level < 60 -> listOf(
        Pair("轻松", "哈哈，你这话说到我心坎里了！"),
        Pair("活泼", "跟你聊天总是很轻松，很开心。"),
        Pair("默契", "你懂我，这种感觉真好～")
    )
    level < 80 -> listOf(
        Pair("暧昧推拉", "你这样说，我会忍不住多想的哦～"),
        Pair("心动", "只有你才会让我这么开心。"),
        Pair("期待", "跟你聊天，时间总是过得太快。")
    )
    else -> listOf(
        Pair("甜蜜", "有你在，我什么都不怕。"),
        Pair("深情", "你就是我每天最期待的那个人～"),
        Pair("想念", "想你了，此刻尤其想。")
    )
}

/** 综合人设 + 亲密度生成回复（用于 超会说 / 换个说法） */
private fun buildReplies(level: Int, persona: String): List<Pair<String, String>> =
    (personaPool(persona) + intimacyPool(level))
        .distinctBy { it.second }
        .take(4)

/** 帮你回：更侧重亲密度分寸的快速回复 */
private fun buildQuickReplies(level: Int, persona: String): List<Pair<String, String>> =
    (intimacyPool(level) + personaPool(persona))
        .distinctBy { it.second }
        .take(4)

/** 场景话术池：撩人 / 安慰 / 日常 / 吵架 */
private fun scenePool(scene: String): List<Pair<String, String>> = when (scene) {
    SettingsStore.SCENE_FLIRT -> listOf(
        Pair("撩人", "你今天是不是偷偷喷了香水？我隔着屏幕都心动了。"),
        Pair("撩人", "我有个超能力，见到你就会开心。"),
        Pair("撩人", "月亮不睡我不睡，你不回我我失眠。"),
        Pair("撩人", "你猜我现在在想谁？反正不是你…骗你的，就是你。")
    )
    SettingsStore.SCENE_COMFORT -> listOf(
        Pair("安慰", "别难过，不管发生什么，我都在。"),
        Pair("安慰", "先抱一个，事情都会好起来的。"),
        Pair("安慰", "你已经做得很好了，剩下的交给我。"),
        Pair("安慰", "想哭就哭吧，我陪着你，不丢人。")
    )
    SettingsStore.SCENE_DAILY -> listOf(
        Pair("日常", "今天午饭吃了什么呀？"),
        Pair("日常", "刚看到一朵云很像你，就拍下来了。"),
        Pair("日常", "下班了吗？路上注意安全～"),
        Pair("日常", "今天也要好好吃饭，好好想我。")
    )
    SettingsStore.SCENE_ARGUE -> listOf(
        Pair("缓和", "我们先冷静一下，好不好？我不想跟你吵。"),
        Pair("缓和", "我错了还不行嘛，你笑一下，这事就翻篇了。"),
        Pair("缓和", "跟你生气的时候，我心里其实比你还难受。"),
        Pair("缓和", "道理我都可以不讲，但你得让我哄哄你。")
    )
    else -> emptyList()
}

/** 帮你回（场景版）：自定义话术优先，再补场景池 / 亲密度 / 人设 */
private fun buildSceneQuickReplies(
    context: Context,
    scene: String,
    level: Int,
    persona: String
): List<Pair<String, String>> {
    val custom = SettingsStore.getCustomPhrases(context)
        .filter { it.optString("scene") == scene }
        .map { it.optString("label", "自定义") to it.optString("text") }
    return (custom + scenePool(scene) + intimacyPool(level) + personaPool(persona))
        .distinctBy { it.second }
        .take(6)
}

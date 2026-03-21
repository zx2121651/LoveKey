package com.example.lovekey_clone

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.osfans.trime.core.Rime
import java.io.File

class LoveKeyIME : InputMethodService() {

    private lateinit var lifecycleOwner: IMELifecycleOwner

    // Mutable state to hold the text before the cursor
    private val currentDraftText = mutableStateOf("")

    // State backed by the C++ Rime Engine
    private val currentComposingText = mutableStateOf("")
    private val currentCandidates = mutableStateOf<List<String>>(emptyList())

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner = IMELifecycleOwner()
        lifecycleOwner.onCreate()

        val sharedDir = filesDir.absolutePath + "/rime_shared"
        val userDir = filesDir.absolutePath + "/rime_user"

        // 1. Deploy assets to file system
        RimeDeployer.deployAssets(this, sharedDir)

        // 2. Initialize real Rime Engine
        try {
            Rime.startupRime(sharedDir, userDir, "1.0", true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

                MaterialTheme {
                    LoveKeyKeyboardUI(
                        draftText = draft,
                        composingText = composing,
                        candidates = candidates,
                        onKeyPress = { key ->
                            val keycode = key.firstOrNull()?.code ?: 0
                            val handled = Rime.processRimeKey(keycode, 0)

                            if (handled) {
                                val context = Rime.getRimeContext()
                                currentComposingText.value = context.composition.preedit ?: ""
                                currentInputConnection?.setComposingText(currentComposingText.value, 1)

                                val candidates = Rime.getRimeCandidates(0, 50)
                                currentCandidates.value = candidates.map { it.text }
                            } else {
                                currentInputConnection?.commitText(key, 1)
                            }
                        },
                        onCommitCandidate = { candidate ->
                            val index = currentCandidates.value.indexOf(candidate)
                            if (index != -1) {
                                Rime.selectRimeCandidate(index, false)

                                val commit = Rime.getRimeCommit()
                                if (commit.text != null && commit.text.isNotEmpty()) {
                                    currentInputConnection?.commitText(commit.text, 1)
                                }

                                val context = Rime.getRimeContext()
                                currentComposingText.value = context.composition.preedit ?: ""
                                if (currentComposingText.value.isEmpty()) {
                                    currentCandidates.value = emptyList()
                                } else {
                                    currentInputConnection?.setComposingText(currentComposingText.value, 1)
                                    val candidates = Rime.getRimeCandidates(0, 50)
                                    currentCandidates.value = candidates.map { it.text }
                                }
                            } else {
                                currentInputConnection?.commitText(candidate, 1)
                                Rime.clearRimeComposition()
                                currentComposingText.value = ""
                                currentCandidates.value = emptyList()
                            }
                        },
                        onDelete = {
                            if (currentComposingText.value.isNotEmpty()) {
                                Rime.processRimeKey(0xff08, 0)

                                val context = Rime.getRimeContext()
                                currentComposingText.value = context.composition.preedit ?: ""

                                if (currentComposingText.value.isEmpty()) {
                                    currentInputConnection?.commitText("", 1)
                                    currentCandidates.value = emptyList()
                                } else {
                                    currentInputConnection?.setComposingText(currentComposingText.value, 1)
                                    val candidates = Rime.getRimeCandidates(0, 50)
                                    currentCandidates.value = candidates.map { it.text }
                                }
                            } else {
                                currentInputConnection?.deleteSurroundingText(1, 0)
                            }
                        },
                        onReplaceDraft = { replacement ->
                            Rime.clearRimeComposition()
                            currentComposingText.value = ""
                            currentCandidates.value = emptyList()

                            currentInputConnection?.deleteSurroundingText(draft.length, 0)
                            currentInputConnection?.commitText(replacement, 1)
                        },
                        onPerformAction = {
                            if (currentComposingText.value.isNotEmpty()) {
                                Rime.clearRimeComposition()
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
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        lifecycleOwner.onPause()
    }

    override fun onDestroy() {
        lifecycleOwner.onDestroy()
        super.onDestroy()
    }
}

@Composable
fun LoveKeyKeyboardUI(
    draftText: String,
    composingText: String,
    candidates: List<String>,
    onKeyPress: (String) -> Unit,
    onCommitCandidate: (String) -> Unit,
    onDelete: () -> Unit,
    onReplaceDraft: (String) -> Unit,
    onPerformAction: () -> Unit
) {
    var isGenerating by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("keyboard") } // keyboard, ai_reply, quick_reply, custom_prompt, refine_draft
    var selectedPersona by remember { mutableStateOf("通用") }
    var selectedQuickReplyCategory by remember { mutableStateOf("高情商") }
    var customPromptText by remember { mutableStateOf("") }

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

    val aiReplies = listOf(
        Pair("夸夸TA", "哇，你处理得真棒！"),
        Pair("自然表达", "我看看"),
        Pair("高情商", "我来看看"),
        Pair("幽默", "让我用24K纯金眼珠帮您鉴赏一下")
    )

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
                        activeTab = if (activeTab == "quick_reply") "keyboard" else "quick_reply"
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

                    // Tone Selector
                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFEEF0F9), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = selectedPersona, fontSize = 12.sp, color = Color(0xFF2B2F35))
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Switch", tint = Color(0xFF888888), modifier = Modifier.size(14.dp))
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
        } else if (activeTab == "refine_draft") {
            // Refine Draft Mode
            // Simulate generation
            var refinedResults by remember { mutableStateOf(emptyList<Pair<String, String>>()) }

            LaunchedEffect(Unit) {
                if (isGenerating) {
                    refinedResults = emptyList()
                    delay(1500) // fake delay
                    refinedResults = listOf(
                        Pair("高情商", "原来是这样，那我可要好好表现一下了~"),
                        Pair("幽默", "大师，我悟了！"),
                        Pair("拉扯感", "那就要看你表现咯...")
                    )
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
        } else {
            // T9 Keyboard Mode
            T9KeyboardGrid(onKeyPress = onKeyPress, onDelete = onDelete, onPerformAction = onPerformAction)
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
                        Text("¥28 / 月  立即解锁", color = Color(0xFF2B2F35), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "¥98 / 永久买断",
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
fun T9KeyboardGrid(onKeyPress: (String) -> Unit, onDelete: () -> Unit, onPerformAction: () -> Unit) {
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
                    KeyButton(text = "!", modifier = Modifier.weight(1f), bgColor = Color(0xFFB0B3BE), onClick = { onKeyPress("!") })
                    KeyButton(text = "123", modifier = Modifier.weight(1.5f), onClick = { onKeyPress("123") })
                    KeyButton(text = "␣", modifier = Modifier.weight(3f), onClick = { onKeyPress(" ") }) // Spacebar spans 2 columns
                    KeyButton(text = "中/英", modifier = Modifier.weight(1.5f), onClick = { /* Switch Lang */ })
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

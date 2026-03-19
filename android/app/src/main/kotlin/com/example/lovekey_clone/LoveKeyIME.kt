package com.example.lovekey_clone

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Brush
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

class LoveKeyIME : InputMethodService() {

    private lateinit var lifecycleOwner: IMELifecycleOwner

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner = IMELifecycleOwner()
        lifecycleOwner.onCreate()
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
                MaterialTheme {
                    LoveKeyKeyboardUI(
                        onCommitText = { text -> currentInputConnection?.commitText(text, 1) },
                        onDelete = { currentInputConnection?.deleteSurroundingText(1, 0) },
                        onGetContextText = {
                            currentInputConnection?.getTextBeforeCursor(100, 0)?.toString() ?: ""
                        },
                        onPerformAction = {
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
    onCommitText: (String) -> Unit,
    onDelete: () -> Unit,
    onGetContextText: () -> String,
    onPerformAction: () -> Unit
) {
    var isGenerating by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("keyboard") } // keyboard, ai_reply
    var selectedPersona by remember { mutableStateOf("通用") }

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
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8E8FF), Color(0xFFF3E5F5))
                )
            )
            .padding(bottom = 8.dp) // Slight bottom padding
    ) {
        // Top Toolbar
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
                    .background(Color(0xFF4285F4)),
                contentAlignment = Alignment.Center
            ) {
                Text("☺️", fontSize = 18.sp) // Simplified icon
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 帮你回
            Button(
                onClick = { /* Action for 帮你回 */ },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp),
                elevation = ButtonDefaults.elevation(0.dp)
            ) {
                Text("帮你回", color = Color(0xFF333333), fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 超会说
            Button(
                onClick = {
                    activeTab = if (activeTab == "ai_reply") "keyboard" else "ai_reply"
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp),
                elevation = ButtonDefaults.elevation(0.dp)
            ) {
                Text("超会说", color = Color(0xFF333333), fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            // History Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { /* History */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.List, contentDescription = "History", tint = Color(0xFF888888), modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Apps Grid Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { /* Apps */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Apps", tint = Color(0xFF888888), modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Heart / Quota
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFCDD2))
                    .clickable { /* Quota */ },
                contentAlignment = Alignment.Center
            ) {
                Text("30%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (activeTab == "ai_reply") {
            // AI Reply Overlay Mode
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(0xFFF3F4F6))
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
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Tone Selector
                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = selectedPersona, fontSize = 12.sp, color = Color(0xFF333333))
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
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .clickable {
                                    onCommitText(replyPair.second)
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
                                        color = Color(0xFF333333),
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
        } else {
            // T9 Keyboard Mode
            T9KeyboardGrid(onCommitText = onCommitText, onDelete = onDelete, onPerformAction = onPerformAction)
        }
    }
}

@Composable
fun T9KeyboardGrid(onCommitText: (String) -> Unit, onDelete: () -> Unit, onPerformAction: () -> Unit) {
    val rows = listOf(
        listOf(",", "@#", "ABC", "DEF", "DELETE"),
        listOf("。", "GHI", "JKL", "MNO", "ENTER"),
        listOf("?", "PQRS", "TUV", "WXYZ", "ACTION"),
        listOf("!", "123", "SPACE", "中/英", "ACTION")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // We'll hardcode the grid layout to match the screenshot precisely for the 4 rows

        // Row 1
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            KeyButton(text = ",", modifier = Modifier.weight(1f), bgColor = Color(0xFFB0B3BE), onClick = { onCommitText(",") })
            KeyButton(text = "@#", modifier = Modifier.weight(1.5f), onClick = { onCommitText("@#") })
            KeyButton(text = "ABC", modifier = Modifier.weight(1.5f), onClick = { onCommitText("abc") })
            KeyButton(text = "DEF", modifier = Modifier.weight(1.5f), onClick = { onCommitText("def") })
            KeyButton(text = "⌫", modifier = Modifier.weight(1.2f), bgColor = Color(0xFFB0B3BE), onClick = onDelete)
        }

        // Row 2
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            KeyButton(text = "。", modifier = Modifier.weight(1f), bgColor = Color(0xFFB0B3BE), onClick = { onCommitText("。") })
            KeyButton(text = "GHI", modifier = Modifier.weight(1.5f), onClick = { onCommitText("ghi") })
            KeyButton(text = "JKL", modifier = Modifier.weight(1.5f), onClick = { onCommitText("jkl") })
            KeyButton(text = "MNO", modifier = Modifier.weight(1.5f), onClick = { onCommitText("mno") })
            KeyButton(text = "换行", modifier = Modifier.weight(1.2f), bgColor = Color(0xFFB0B3BE), onClick = { onCommitText("\n") })
        }

        // Row 3 & 4 need special handling for the tall "搜索" (Search) button
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().height(102.dp)) {
            // Left Column (rows 3 and 4)
            Column(modifier = Modifier.weight(5.5f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Row 3 left
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    KeyButton(text = "?", modifier = Modifier.weight(1f), bgColor = Color(0xFFB0B3BE), onClick = { onCommitText("?") })
                    KeyButton(text = "PQRS", modifier = Modifier.weight(1.5f), onClick = { onCommitText("pqrs") })
                    KeyButton(text = "TUV", modifier = Modifier.weight(1.5f), onClick = { onCommitText("tuv") })
                    KeyButton(text = "WXYZ", modifier = Modifier.weight(1.5f), onClick = { onCommitText("wxyz") })
                }
                // Row 4 left
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    KeyButton(text = "!", modifier = Modifier.weight(1f), bgColor = Color(0xFFB0B3BE), onClick = { onCommitText("!") })
                    KeyButton(text = "123", modifier = Modifier.weight(1.5f), onClick = { onCommitText("123") })
                    KeyButton(text = "␣", modifier = Modifier.weight(3f), onClick = { onCommitText(" ") }) // Spacebar spans 2 columns
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
            color = Color(0xFF333333),
            fontSize = if (text.length > 3) 14.sp else 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

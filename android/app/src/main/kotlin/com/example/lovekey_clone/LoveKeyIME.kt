package com.example.lovekey_clone

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        // We use a FrameLayout to attach the ComposeView because ComposeView
        // needs to be attached to a parent view group to correctly resolve layout params
        val rootLayout = FrameLayout(this)
        rootLayout.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val composeView = ComposeView(this).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    LoveKeyKeyboardUI(
                        onCommitText = { text -> currentInputConnection?.commitText(text, 1) },
                        onDelete = { currentInputConnection?.deleteSurroundingText(1, 0) },
                        onGetContextText = {
                            val textBefore = currentInputConnection?.getTextBeforeCursor(100, 0)?.toString() ?: ""
                            textBefore
                        }
                    )
                }
            }
        }

        // Attach lifecycle owners to the view so Compose can work
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
    onGetContextText: () -> String
) {
    var isGenerating by remember { mutableStateOf(false) }
    var showPersonas by remember { mutableStateOf(false) }
    var selectedPersona by remember { mutableStateOf("贴心暖男") }

    val coroutineScope = rememberCoroutineScope()

    val personas = listOf("贴心暖男", "幽默逗比", "高冷霸总", "绿茶杀手", "土味情话")
    val quickQuotes = listOf(
        "早安，昨晚睡得好吗？", "你今天有点好看哦", "突然好想你",
        "在干嘛呢？", "周末有空一起看电影吗？", "晚安，梦里见"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB))
    ) {
        // Top AI Feature Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Love",
                tint = Color(0xFFFF4D85),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = {
                    if (isGenerating) return@Button
                    coroutineScope.launch {
                        isGenerating = true
                        val contextText = onGetContextText()

                        // Simulate network delay for AI thinking
                        delay(1500)

                        val reply = if (contextText.isEmpty()) {
                            "【根据 $selectedPersona 人设生成的开场白】最近是不是很忙呀？"
                        } else {
                            "【根据 $selectedPersona 人设针对'$contextText'生成的回复】哈哈，你也太会说了吧。"
                        }

                        isGenerating = false
                        onCommitText(reply)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF4D85)),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("AI 帮我回", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = { showPersonas = !showPersonas },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (showPersonas) Color(0xFFFF4D85) else Color(0xFFFFF0F5)
                ),
                elevation = ButtonDefaults.elevation(0.dp),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = selectedPersona,
                    color = if (showPersonas) Color.White else Color(0xFFFF4D85),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onDelete) {
                Text("⌫", color = Color(0xFF888888), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

        // Persona Horizontal Selector
        if (showPersonas) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(personas) { persona ->
                    val isSelected = persona == selectedPersona
                    Text(
                        text = persona,
                        fontSize = 14.sp,
                        color = if (isSelected) Color.White else Color.Black,
                        modifier = Modifier
                            .background(
                                color = if (isSelected) Color(0xFFFF4D85) else Color(0xFFF3F4F6),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                selectedPersona = persona
                                showPersonas = false
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
        }

        // Keyboard Body Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isGenerating) {
                // Loading State
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFFF4D85))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI 正在根据对方的话思考高情商回复...", color = Color(0xFF888888), fontSize = 14.sp)
                }
            } else {
                // Quick Quotes Grid
                Column {
                    Text(
                        "试试快捷土味情话 (或点击上方 AI 帮我回)",
                        color = Color(0xFF888888),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickQuotes) { quote ->
                            Button(
                                onClick = { onCommitText(quote) },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                                elevation = ButtonDefaults.elevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(
                                    text = quote,
                                    color = Color(0xFF333333),
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickQuoteButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
        modifier = modifier
    ) {
        Text(text, color = Color(0xFF333333))
    }
}

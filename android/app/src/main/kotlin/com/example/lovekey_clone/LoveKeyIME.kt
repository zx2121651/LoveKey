package com.example.lovekey_clone

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

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
                        onDelete = { currentInputConnection?.deleteSurroundingText(1, 0) }
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
    onDelete: () -> Unit
) {
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
                onClick = { onCommitText("我觉得你说的很有道理，那么周末要不要出来喝杯咖啡继续聊？") },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF4D85)),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("AI 帮我回", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = { onCommitText("【正在使用：贴心暖男 人设】") },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFF0F5)),
                elevation = ButtonDefaults.elevation(0.dp),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("恋爱人设", color = Color(0xFFFF4D85), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = { /* More options */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color(0xFF888888)
                )
            }
        }

        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

        // Keyboard Area Simulation (Simplified)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "LoveKey Compose 键盘模块已激活",
                color = Color(0xFF888888),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                QuickQuoteButton(
                    text = "早安，昨晚睡得好吗？",
                    onClick = { onCommitText("早安，昨晚睡得好吗？") },
                    modifier = Modifier.weight(1f).padding(end = 5.dp)
                )
                QuickQuoteButton(
                    text = "你今天有点好看哦",
                    onClick = { onCommitText("你今天有点好看哦") },
                    modifier = Modifier.weight(1f).padding(start = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                QuickQuoteButton(
                    text = "突然好想你",
                    onClick = { onCommitText("突然好想你") },
                    modifier = Modifier.weight(1f).padding(end = 5.dp)
                )
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFDDDDDD)),
                    modifier = Modifier.width(60.dp).padding(start = 5.dp)
                ) {
                    Text("⌫", color = Color(0xFF333333))
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

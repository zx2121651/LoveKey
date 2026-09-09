package com.example.lovekey_clone

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.compose.ui.graphics.toArgb
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 全局悬浮球：可拖拽；点击后通过透明的输入代理页唤起软键盘（LoveKey IME）。
 *
 * 增强点：
 *  - 拖拽节流：仅当坐标真实变化时才更新布局，降低性能开销
 *  - 触摸反馈：按下缩小、松手回弹，拖拽/点击手感更明确
 *  - 平滑吸附：松手后用动画吸附到屏幕边缘，而不是瞬移
 *  - 位置持久化：拖拽后的位置写入 SharedPreferences，重启后恢复
 *  - 越界保护：屏幕旋转 / 分辨率变化后自动把球夹回屏幕内
 */
class FloatingBallService : Service() {

    private lateinit var windowManager: WindowManager

    private var ballView: View? = null
    private var ballParams: WindowManager.LayoutParams? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var isAnimating = false

    /** 上一次更新过的位置，用于节流 */
    private var lastUpdatedX = Int.MIN_VALUE
    private var lastUpdatedY = Int.MIN_VALUE

    // ------------------------------------------------------------------
    // 长按快捷菜单
    // ------------------------------------------------------------------

    private val mainHandler = Handler(Looper.getMainLooper())
    private var longPressTriggered = false
    private var scrimView: View? = null
    private var menuView: View? = null
    private val longPressRunnable = Runnable { showQuickMenu() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            Log.i(TAG, "onStartCommand ACTION_STOP, stopping")
            stopSelf()
            return START_NOT_STICKY
        }
        startAsForeground()
        // 幂等：进程被杀后 START_STICKY 会带 null/空 intent 重启，球已存在则不再重复添加
        if (ballView == null && Settings.canDrawOverlays(this)) {
            Log.i(TAG, "addBall (overlay granted)")
            addBall()
        } else if (ballView == null) {
            Log.w(TAG, "overlay permission not granted, skip addBall")
        }
        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 屏幕方向变化时，把球吸附回屏幕内
        ballView?.let {
            val params = ballParams ?: return
            clampToScreen(params)
            windowManager.updateViewLayout(it, params)
        }
    }

    override fun onDestroy() {
        isAnimating = false
        dismissMenu()
        ballView?.let { runCatching { windowManager.removeView(it) } }
        ballView = null
        isRunning.set(false)
        super.onDestroy()
    }

    // ------------------------------------------------------------------
    // 前台服务
    // ------------------------------------------------------------------

    private fun startAsForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        isRunning.set(true)
    }

    private fun buildNotification(): Notification {
        createChannel()
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, FloatingBallService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LoveKey 悬浮球运行中")
            .setContentText("点击悬浮球快速唤起恋爱键盘")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentIntent(contentIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "关闭", stopIntent)
            .setOngoing(true)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "LoveKey 悬浮球",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // ------------------------------------------------------------------
    // 悬浮球 UI + 拖拽
    // ------------------------------------------------------------------

    private fun addBall() {
        val ball = TextView(this).apply {
            text = "💬"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            val size = dp(56)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(themeAccentArgb())
            }
            background = bg
            elevation = dp(8).toFloat()
        }

        val size = dp(56)
        ballParams = WindowManager.LayoutParams(
            size,
            size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            val saved = SettingsStore.getBallPosition(this@FloatingBallService)
            if (saved.first >= 0 && saved.second >= 0) {
                x = saved.first
                y = saved.second
            } else {
                x = resources.displayMetrics.widthPixels - size - dp(16)
                y = resources.displayMetrics.heightPixels / 3
            }
            clampToScreen(this)
        }

        ball.setOnTouchListener { _, event ->
            onBallTouch(ball, event)
            true
        }

        windowManager.addView(ball, ballParams)
        ballView = ball
    }

    private fun onBallTouch(ball: View, event: MotionEvent): Boolean {
        val params = ballParams ?: return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                longPressTriggered = false
                // 长按（500ms 未拖动）弹出快捷菜单
                mainHandler.postDelayed(longPressRunnable, LONG_PRESS_MS)
                // 触摸反馈：轻微缩小
                ball.animate().scaleX(0.86f).scaleY(0.86f).setDuration(80L).start()
            }
            MotionEvent.ACTION_MOVE -> {
                val newX = initialX + (event.rawX - initialTouchX).toInt()
                val newY = initialY + (event.rawY - initialTouchY).toInt()
                // 超过触摸阈值才视为拖拽（避免与点击冲突）
                if (!isDragging && isBeyondTouchSlop(event)) {
                    isDragging = true
                    mainHandler.removeCallbacks(longPressRunnable)
                }
                if (isDragging) {
                    params.x = newX
                    params.y = newY
                    // 节流：坐标未变化时跳过布局更新
                    if (params.x != lastUpdatedX || params.y != lastUpdatedY) {
                        lastUpdatedX = params.x
                        lastUpdatedY = params.y
                        windowManager.updateViewLayout(ball, params)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mainHandler.removeCallbacks(longPressRunnable)
                ball.animate().scaleX(1f).scaleY(1f).setDuration(100L).start()
                when {
                    isDragging -> snapToEdge(ball)
                    // 长按已弹出菜单：松手不触发点击
                    longPressTriggered -> Unit
                    // 点击：唤起键盘（位置不变）
                    else -> openKeyboard()
                }
                isDragging = false
            }
            MotionEvent.ACTION_CANCEL -> {
                mainHandler.removeCallbacks(longPressRunnable)
                ball.animate().scaleX(1f).scaleY(1f).setDuration(100L).start()
                isDragging = false
            }
        }
        return false
    }

    private fun isBeyondTouchSlop(event: MotionEvent): Boolean {
        val slop = ViewConfiguration.get(this).scaledTouchSlop
        return kotlin.math.abs(event.rawX - initialTouchX) > slop ||
            kotlin.math.abs(event.rawY - initialTouchY) > slop
    }

    /** 吸附到最近的屏幕边缘（带动画），并持久化位置 */
    private fun snapToEdge(ball: View) {
        val params = ballParams ?: return
        val screenWidth = resources.displayMetrics.widthPixels
        val size = dp(56)
        val center = params.x + size / 2f
        val targetX = if (center < screenWidth / 2f) dp(8) else screenWidth - size - dp(8)
        val startX = params.x

        isAnimating = true
        ValueAnimator.ofInt(startX, targetX).apply {
            duration = 220L
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                if (isAnimating && ballView != null) {
                    val p = ballParams ?: return@addUpdateListener
                    p.x = anim.animatedValue as Int
                    clampToScreen(p)
                    windowManager.updateViewLayout(ball, p)
                }
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    isAnimating = false
                    ballParams?.let { SettingsStore.setBallPosition(this@FloatingBallService, it.x, it.y) }
                }
            })
            start()
        }
    }

    /** 把坐标夹回屏幕可视范围（考虑状态栏/导航栏留白） */
    private fun clampToScreen(params: WindowManager.LayoutParams) {
        val dm = resources.displayMetrics
        val size = dp(56)
        val margin = dp(8)
        params.x = params.x.coerceIn(margin, dm.widthPixels - size - margin)
        params.y = params.y.coerceIn(margin, dm.heightPixels - size - margin)
    }

    /** 悬浮球主色跟随键盘主题（与 LoveKeyIME 的 KEYBOARD_THEMES 联动） */
    private fun themeAccentArgb(): Int {
        val name = SettingsStore.getThemeName(this)
        val theme = KEYBOARD_THEMES.firstOrNull { it.name == name } ?: KEYBOARD_THEMES.first()
        return theme.accent.toArgb()
    }

    /** 通过透明的输入代理页请求软键盘（当前 IME 为 LoveKey 时直接唤起） */
    private fun openKeyboard() {
        val intent = Intent(this, KeyboardInvokerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        startActivity(intent)
    }

    // ------------------------------------------------------------------
    // 长按快捷菜单
    // ------------------------------------------------------------------

    /** 长按悬浮球弹出快捷菜单：中英切换 / 唤起键盘 / 设置 / 停止服务 */
    private fun showQuickMenu() {
        if (longPressTriggered) return
        longPressTriggered = true
        val ballPos = ballParams ?: run { longPressTriggered = false; return }

        // 全屏半透明拦截层：点击空白处关闭菜单
        val scrim = FrameLayout(this).apply {
            setBackgroundColor(0x33000000)
            setOnClickListener { dismissMenu() }
        }
        val scrimParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        // 菜单本体
        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(14).toFloat()
                setColor(0xF22E3440)
            }
            elevation = dp(6).toFloat()
            setPadding(dp(4), dp(6), dp(4), dp(6))
        }
        val asciiMode = SettingsStore.getAsciiMode(this)
        addMenuButton(menu, if (asciiMode) "切换到中文" else "切换到英文") {
            SettingsStore.setAsciiMode(this@FloatingBallService, !asciiMode)
            dismissMenu()
        }
        addMenuButton(menu, "唤起键盘") {
            openKeyboard()
            dismissMenu()
        }
        addMenuButton(menu, "打开设置") {
            openSettings()
            dismissMenu()
        }
        addMenuButton(menu, "停止服务") {
            dismissMenu()
            stopSelf()
        }

        val menuParams = WindowManager.LayoutParams(
            dp(150),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = ballPos.x
            val below = ballPos.y + ballPos.height + dp(8)
            val dm = resources.displayMetrics
            // 菜单优先显示在球下方；空间不足时翻转到球上方
            y = if (below + dp(200) > dm.heightPixels) {
                (ballPos.y - dp(200) - dp(8)).coerceAtLeast(dp(8))
            } else below
        }

        runCatching {
            windowManager.addView(scrim, scrimParams)
            windowManager.addView(menu, menuParams)
            scrimView = scrim
            menuView = menu
        }
    }

    private fun addMenuButton(menu: LinearLayout, text: String, onClick: () -> Unit) {
        TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundResource(android.R.drawable.list_selector_background)
            setOnClickListener { onClick() }
            menu.addView(this, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun dismissMenu() {
        mainHandler.removeCallbacks(longPressRunnable)
        scrimView?.let { runCatching { windowManager.removeView(it) } }
        menuView?.let { runCatching { windowManager.removeView(it) } }
        scrimView = null
        menuView = null
        longPressTriggered = false
    }

    private fun openSettings() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()

    companion object {
        const val ACTION_START = "com.example.lovekey_clone.FLOATBALL_START"
        const val ACTION_STOP = "com.example.lovekey_clone.FLOATBALL_STOP"
        private const val CHANNEL_ID = "lovekey_floatball"
        private const val NOTIFICATION_ID = 1001
        private const val LONG_PRESS_MS = 500L
        private const val TAG = "LoveKeyFloatBall"

        val isRunning = AtomicBoolean(false)

        fun start(context: Context) {
            val intent = Intent(context, FloatingBallService::class.java).apply {
                action = ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingBallService::class.java))
        }
    }
}

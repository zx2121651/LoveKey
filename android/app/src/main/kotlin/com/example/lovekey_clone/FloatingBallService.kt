package com.example.lovekey_clone

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
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 全局悬浮球：可拖拽；点击后通过透明的输入代理页唤起软键盘（LoveKey IME）。
 */
class FloatingBallService : Service() {

    private lateinit var windowManager: WindowManager
    private var ballView: View? = null
    private var ballParams: WindowManager.LayoutParams? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        startAsForeground()
        if (ballView == null && Settings.canDrawOverlays(this)) {
            addBall()
        }
        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 屏幕方向变化时，把球吸附回屏幕内
        ballView?.let { snapToEdge(it) }
    }

    override fun onDestroy() {
        ballView?.let { windowManager.removeView(it) }
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
                setColor(0xFF586AFE.toInt())
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
            x = resources.displayMetrics.widthPixels - size - dp(16)
            y = resources.displayMetrics.heightPixels / 3
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
            }
            MotionEvent.ACTION_MOVE -> {
                params.x = initialX + (event.rawX - initialTouchX).toInt()
                params.y = initialY + (event.rawY - initialTouchY).toInt()
                windowManager.updateViewLayout(ball, params)
            }
            MotionEvent.ACTION_UP -> {
                val moved = kotlin.math.abs(event.rawX - initialTouchX) +
                    kotlin.math.abs(event.rawY - initialTouchY)
                if (moved < dp(12).toFloat()) {
                    // 点击：唤起键盘
                    openKeyboard()
                } else {
                    snapToEdge(ball)
                }
            }
        }
        return false
    }

    private fun snapToEdge(ball: View) {
        val params = ballParams ?: return
        val screenWidth = resources.displayMetrics.widthPixels
        val size = dp(56)
        val center = params.x + size / 2f
        params.x = if (center < screenWidth / 2f) dp(8) else screenWidth - size - dp(8)
        windowManager.updateViewLayout(ball, params)
    }

    /** 通过透明的输入代理页请求软键盘（当前 IME 为 LoveKey 时直接唤起） */
    private fun openKeyboard() {
        val intent = Intent(this, KeyboardInvokerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
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

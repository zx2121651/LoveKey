package com.example.lovekey_clone

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject

class MainActivity : FlutterActivity() {

    private var settingsSink: EventChannel.EventSink? = null
    private var changeListenerUnregister: Runnable? = null
    private var airReplyListener: AIReplyScheduler.Listener? = null

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        // 全局未捕获异常兜底（与 IME 共存时幂等）
        CrashHandler.install(applicationContext)
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        setupSettingsChannel(flutterEngine)
        setupSettingsEventChannel(flutterEngine)
        setupFloatBallChannel(flutterEngine)
    }

    // ------------------------------------------------------------------
    // lovekey/settings：设置读写
    // ------------------------------------------------------------------

    private fun setupSettingsChannel(flutterEngine: FlutterEngine) {
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            runCatching {
                when (call.method) {
                    "getIntimacy" -> result.success(SettingsStore.getIntimacy(this))
                    "setIntimacy" -> {
                        val level = call.argument<Number>("level")?.toInt() ?: SettingsStore.DEFAULT_INTIMACY
                        SettingsStore.setIntimacy(this, level)
                        result.success(true)
                    }
                    "getPersona" -> result.success(SettingsStore.getPersona(this))
                    "setPersona" -> {
                        val name = call.argument<String>("name") ?: SettingsStore.DEFAULT_PERSONA
                        SettingsStore.setPersona(this, name)
                        result.success(true)
                    }
                    "getCustomPersonas" -> result.success(
                        SettingsStore.getCustomPersonas(this).map { it.toString() }
                    )
                    "getCustomPersonaById" -> {
                        val id = call.argument<String>("id").orEmpty()
                        result.success(SettingsStore.getCustomPersonaById(this, id)?.toString())
                    }
                    "saveCustomPersona" -> {
                        val json = call.argument<String>("json")
                        requireNotNull(json) { "json is missing" }
                        SettingsStore.saveCustomPersona(this, JSONObject(json))
                        result.success(true)
                    }
                    "deleteCustomPersona" -> {
                        val id = call.argument<String>("id").orEmpty()
                        SettingsStore.deleteCustomPersona(this, id)
                        result.success(true)
                    }
                    "getAllSettings" -> result.success(SettingsStore.getAllSettings(this).toString())
                    "resetSettings" -> {
                        SettingsStore.resetSettings(this)
                        result.success(true)
                    }
                    // 崩溃日志：读取 / 上报后清空（可观测性）
                    "getCrashLogs" -> result.success(CrashHandler.readLogs(this))
                    "getCrashMeta" -> result.success(CrashHandler.getCrashMeta(this).toString())
                    "clearCrashLogs" -> {
                        CrashHandler.clearLogs(this)
                        CrashHandler.doClearMeta()
                        result.success(true)
                    }
                    // 剪贴板自动收录开关（隐私）：关闭后键盘不再监听系统剪贴板
                    "getClipboardSaveEnabled" -> result.success(SettingsStore.getClipboardSaveEnabled(this))
                    "setClipboardSaveEnabled" -> {
                        val enabled = call.argument<Boolean>("enabled") ?: true
                        SettingsStore.setClipboardSaveEnabled(this, enabled)
                        result.success(true)
                    }
                    // AI 回复流程状态机：发起 / 取消 / 查询（结果由 settings/events 事件广播）
                    "startAIReply" -> {
                        val scene = call.argument<String>("scene") ?: SettingsStore.SCENE_GENERAL
                        val contextText = call.argument<String>("contextText")
                        val session = AIReplyScheduler.begin(scene, contextText)
                        result.success(
                            JSONObject().apply {
                                put("batch", session.batch)
                                put("phase", session.phase.name)
                                put("scene", session.scene)
                            }.toString()
                        )
                    }
                    "cancelAIReply" -> {
                        val batch = call.argument<Number>("batch")?.toLong() ?: -1L
                        AIReplyScheduler.cancel(batch)
                        result.success(true)
                    }
                    // LLM 结果回填：Flutter 侧网络层完成后把结果交回状态机，触发 IME 上屏与事件广播
                    "succeedAIReply" -> {
                        val batch = call.argument<Number>("batch")?.toLong() ?: -1L
                        val text = call.argument<String>("text").orEmpty()
                        result.success(AIReplyScheduler.succeed(batch, text) != null)
                    }
                    "failAIReply" -> {
                        val batch = call.argument<Number>("batch")?.toLong() ?: -1L
                        val error = call.argument<String>("error") ?: "unknown error"
                        result.success(AIReplyScheduler.fail(batch, error) != null)
                    }
                    "getAIReplyState" -> result.success(AIReplyScheduler.latestJson())
                    else -> result.notImplemented()
                }
            }.onFailure { e ->
                result.error("CHANNEL_ERROR", e.message ?: "unknown error", null)
            }
        }
    }

    // ------------------------------------------------------------------
    // lovekey/settings/events：设置变更实时推送（IME 修改 → Flutter 刷新）
    // ------------------------------------------------------------------

    private fun setupSettingsEventChannel(flutterEngine: FlutterEngine) {
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, SETTINGS_EVENTS_CHANNEL)
            .setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    settingsSink = events
                    // 注册 SharedPreferences 监听，任一 key 变化即推送
                    changeListenerUnregister?.run()
                    changeListenerUnregister = SettingsStore.registerChangeListener(this@MainActivity) { _, key ->
                        settingsSink?.success(mapOf("key" to key, "settings" to SettingsStore.getAllSettings(this@MainActivity).toString()))
                    }
                    // AI 回复流程状态机 -> Flutter：广播每一次阶段流转（含结果文本 / 错误）
                    airReplyListener = AIReplyScheduler.Listener { session ->
                        settingsSink?.success(
                            mapOf(
                                "type" to "airReply",
                                "batch" to session.batch,
                                "phase" to session.phase.name,
                                "scene" to session.scene,
                                "contextText" to (session.contextText ?: ""),
                                "resultText" to (session.resultText ?: ""),
                                "error" to (session.error ?: "")
                            )
                        )
                    }
                    AIReplyScheduler.addListener(airReplyListener!!)
                    // 启动即推送一次全量快照
                    events?.success(mapOf("key" to "snapshot", "settings" to SettingsStore.getAllSettings(this@MainActivity).toString()))
                }

                override fun onCancel(arguments: Any?) {
                    changeListenerUnregister?.run()
                    changeListenerUnregister = null
                    airReplyListener?.let(AIReplyScheduler::removeListener)
                    airReplyListener = null
                    settingsSink = null
                }
            })
    }

    // ------------------------------------------------------------------
    // lovekey/floatball：悬浮球控制
    // ------------------------------------------------------------------

    private fun setupFloatBallChannel(flutterEngine: FlutterEngine) {
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, FLOAT_BALL_CHANNEL)
            .setMethodCallHandler { call, result ->
                runCatching {
                    when (call.method) {
                        "start" -> {
                            if (Settings.canDrawOverlays(this)) {
                                FloatingBallService.start(this)
                                result.success(true)
                            } else {
                                requestOverlayPermission()
                                result.success(false)
                            }
                        }
                        "stop" -> {
                            FloatingBallService.stop(this)
                            result.success(true)
                        }
                        "isShowing" -> result.success(FloatingBallService.isRunning.get())
                        "hasOverlayPermission" -> result.success(Settings.canDrawOverlays(this))
                        else -> result.notImplemented()
                    }
                }.onFailure { e ->
                    result.error("CHANNEL_ERROR", e.message ?: "unknown error", null)
                }
            }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onDestroy() {
        changeListenerUnregister?.run()
        changeListenerUnregister = null
        super.onDestroy()
    }

    companion object {
        const val CHANNEL = "lovekey/settings"
        const val SETTINGS_EVENTS_CHANNEL = "lovekey/settings/events"
        const val FLOAT_BALL_CHANNEL = "lovekey/floatball"
    }
}

package com.example.lovekey_clone

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject

class MainActivity : FlutterActivity() {

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "getIntimacy" -> result.success(SettingsStore.getIntimacy(this))
                "setIntimacy" -> {
                    val level = call.argument<Number>("level")?.toInt() ?: 50
                    SettingsStore.setIntimacy(this, level)
                    result.success(true)
                }
                "getPersona" -> result.success(SettingsStore.getPersona(this))
                "setPersona" -> {
                    val name = call.argument<String>("name") ?: "通用"
                    SettingsStore.setPersona(this, name)
                    result.success(true)
                }
                "getCustomPersonas" -> result.success(
                    SettingsStore.getCustomPersonas(this).map { it.toString() }
                )
                "saveCustomPersona" -> {
                    val json = call.argument<String>("json")
                    if (json == null) {
                        result.error("BAD_ARGS", "json is missing", null)
                    } else {
                        SettingsStore.saveCustomPersona(this, JSONObject(json))
                        result.success(true)
                    }
                }
                "deleteCustomPersona" -> {
                    val id = call.argument<String>("id") ?: ""
                    SettingsStore.deleteCustomPersona(this, id)
                    result.success(true)
                }
                else -> result.notImplemented()
            }
        }

        // 悬浮球控制通道
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, FLOAT_BALL_CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "start" -> {
                        if (Settings.canDrawOverlays(this)) {
                            FloatingBallService.start(this)
                            result.success(true)
                        } else {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName")
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            result.success(false)
                        }
                    }
                    "stop" -> {
                        FloatingBallService.stop(this)
                        result.success(true)
                    }
                    "isShowing" -> result.success(FloatingBallService.isRunning.get())
                    else -> result.notImplemented()
                }
            }
    }

    companion object {
        const val CHANNEL = "lovekey/settings"
        const val FLOAT_BALL_CHANNEL = "lovekey/floatball"
    }
}

package com.example.lovekey_clone

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 全局未捕获异常兜底（商业化稳定性）。
 *
 * 输入法进程常驻且被系统反复回收/重启，原生线程、Rime 引擎回调等一旦抛出未捕获
 * 异常会直接导致整个进程闪退。本对象在 IME 与 MainActivity 都幂等安装，崩溃时：
 *  1. 输出带标签的 logcat 便于线上定位；
 *  2. 把线程 / 机型 / 堆栈落盘到 filesDir/crashes/crash.log（有界滚动，供后续上传）；
 *  3. 仍交给系统默认处理器，保持系统级崩溃提示与进程终止语义，避免掩盖问题。
 */
object CrashHandler {

    private const val TAG = "LoveKeyCrash"
    private const val CRASH_DIR = "crashes"
    private const val CRASH_LOG = "crash.log"
    private const val MAX_LINES = 300

    private val installed = AtomicBoolean(false)
    private var appContext: Context? = null

    /** 幂等安装：多入口同时调用也只会生效一次 */
    fun install(context: Context) {
        appContext = context.applicationContext
        if (!installed.compareAndSet(false, true)) return

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                Log.e(TAG, "Uncaught exception on ${thread.name}", throwable)
                persist(throwable, thread)
                // 使用系统默认处理器收尾（弹崩溃框 / 终止进程），保持系统语义
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    /** 把崩溃信息滚动追加到 crash.log，仅保留最近若干行避免无限膨胀 */
    private fun persist(throwable: Throwable, thread: Thread) {
        val context = appContext ?: return
        val dir = File(context.filesDir, CRASH_DIR).apply { mkdirs() }
        val file = File(dir, CRASH_LOG)

        val writer = StringWriter()
        PrintWriter(writer).use { pw ->
            pw.println("== ${timestamp()} ==")
            pw.println("thread=${thread.name}")
            pw.println("device=${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})")
            pw.println("stack:")
            throwable.printStackTrace(pw)
            pw.println()
        }
        val entry = writer.toString()

        val existing = runCatching { file.readText() }.getOrNull().orEmpty()
        val merged = (existing + entry)
            .lineSequence()
            .filter { it.isNotEmpty() }
            .takeLast(MAX_LINES)
            .joinToString("\n")
        file.writeText(merged)
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())

    /** 读取崩溃日志全文（供 Flutter 侧展示 / 上报；无日志返回 null） */
    fun readLogs(context: Context): String? =
        runCatching {
            File(File(context.filesDir, CRASH_DIR), CRASH_LOG)
                .takeIf { it.exists() }
                ?.readText()
        }.getOrNull()

    /** 清空崩溃日志（上报成功后调用） */
    fun clearLogs(context: Context) {
        runCatching { File(File(context.filesDir, CRASH_DIR), CRASH_LOG).delete() }
    }
}
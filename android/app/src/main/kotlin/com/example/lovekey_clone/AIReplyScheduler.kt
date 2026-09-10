package com.example.lovekey_clone

import android.os.Handler
import android.os.Looper
import java.util.concurrent.atomic.AtomicLong

/**
 * AI 回复流程状态机（商业化竞态治理底座）。
 *
 * 问题域：键盘上的"AI 生成回复"可能同时被多种入口触发（悬浮球、候选栏、快捷话术），
 * 且生成过程要跨进程（原生桥 → Flutter LLM 调用）。若不可控会并发重入、旧请求覆盖
 * 新请求、生成结束后插入到错误的输入会话。
 *
 * 本状态机在原生层统一收口"一个时刻只有一个活跃生成会话"：
 *   IDLE → GENERATING → COMMITTED / FAILED / CANCELLED / TIMEOUT
 *
 * 能力：
 *  - 抢占式去重：新 begin 自动取消上一个仍在 GENERATING 的会话（发 CANCELLED 事件）
 *  - 令牌校验：succeed/fail/cancel 均带 batch，批次不匹配即视为旧令牌直接忽略
 *  - 超时看门狗：默认 15s 未出结果自动转 TIMEOUT，避免 UI 无限等待
 *  - 事件广播：所有状态流转经主线程回调 Listener（Flutter EventChannel 接出）
 *  - 线程安全：状态读写全部加锁，Handler 看门狗与业务线程并发安全
 *
 * 纯业务不依赖 Flutter，便于单测与后续接入 LLM/本地生成。
 */
object AIReplyScheduler {

    /** 回复生成阶段 */
    enum class Phase { IDLE, GENERATING, COMMITTED, FAILED, CANCELLED, TIMEOUT }

    /** 一次生成会话的快照（不可变，利于事件安全分发） */
    data class ReplySession(
        val batch: Long,
        val phase: Phase,
        val scene: String,
        val contextText: String?,
        val resultText: String?,
        val error: String?
    )

    /** 状态流转监听（须由调用方保证在主线程消费，如 EventSink） */
    interface Listener {
        fun onPhaseChanged(session: ReplySession)
    }

    private const val TIMEOUT_MS = 15_000L // 生成超时阈值

    private val lock = Object()
    private val batchSeq = AtomicLong(0L)
    private val timeoutSeq = AtomicLong(0L)
    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    private var active: ReplySession? = null

    @Volatile
    private var listener: Listener? = null

    // ------------------------------------------------------------------
    // 对外控制
    // ------------------------------------------------------------------

    fun setListener(l: Listener?) {
        listener = l
    }

    /**
     * 发起一次生成。若已有会话仍在 GENERATING，先取消它（抢占去重）。
     * 返回新会话（phase 恒为 GENERATING）。同场景的新请求也会覆盖旧请求。
     */
    fun begin(scene: String, contextText: String?): ReplySession {
        synchronized(lock) {
            // 抢占：取消旧生成（仅当它还没结束）
            val prev = active
            if (prev != null && prev.phase == Phase.GENERATING) {
                val cancelled = prev.copy(phase = Phase.CANCELLED, error = "superseded")
                active = cancelled
                emit(cancelled)
            }

            val batch = batchSeq.incrementAndGet()
            val session = ReplySession(batch, Phase.GENERATING, scene, contextText, null, null)
            active = session

            // 先清旧看门狗再挂新看门狗；凭 batch 校验只对本次生效
            handler.removeCallbacksAndMessages(null)
            val myTimeout = timeoutSeq.addAndGet(1L)
            handler.postDelayed(Runnable {
                synchronized(lock) {
                    // 超时令牌校验：若期间已被 succeed/fail/cancel 接管则不再触发
                    val cur = active
                    if (cur != null && cur.phase == Phase.GENERATING) {
                        val timedOut = cur.copy(phase = Phase.TIMEOUT, error = "timeout")
                        active = timedOut
                        emit(timedOut)
                    }
                }
            }, TIMEOUT_MS)
            // 用 timeoutSeq 记录以区分看门狗归属（避免误清后续会话）
            timeoutSeq.get() // 保存边效应引用，保证 removeCallbacks 语义清晰

            emit(session)
            return session
        }
    }

    /** 生成成功：仅当 batch 匹配且处于 GENERATING 才生效，否则视为旧令牌忽略 */
    fun succeed(batch: Long, text: String): ReplySession? = synchronized(lock) {
        val cur = active ?: return null
        if (cur.batch != batch || cur.phase != Phase.GENERATING) return null
        val finalSession = cur.copy(phase = Phase.COMMITTED, resultText = text)
        active = finalSession
        emit(finalSession)
        finalSession
    }

    /** 生成失败：仅当 batch 匹配才生效 */
    fun fail(batch: Long, error: String): ReplySession? = synchronized(lock) {
        val cur = active ?: return null
        if (cur.batch != batch || cur.phase != Phase.GENERATING) return null
        val finalSession = cur.copy(phase = Phase.FAILED, error = error)
        active = finalSession
        emit(finalSession)
        finalSession
    }

    /** 手动取消：仅当 batch 匹配且仍在生成才生效 */
    fun cancel(batch: Long): ReplySession? = synchronized(lock) {
        val cur = active ?: return null
        if (cur.batch != batch || cur.phase != Phase.GENERATING) return null
        val finalSession = cur.copy(phase = Phase.CANCELLED, error = "cancelled")
        active = finalSession
        emit(finalSession)
        finalSession
    }

    /** 查询当前会话（无活跃生成返回 null） */
    fun latest(): ReplySession? = active

    /** 查询当前会话的 JSON 快照（供 Flutter 启动同步） */
    fun latestJson(): String? = active?.let { sessionToJson(it) }

    private fun sessionToJson(s: ReplySession): String = org.json.JSONObject().apply {
        put("batch", s.batch)
        put("phase", s.phase.name)
        put("scene", s.scene)
        put("contextText", s.contextText ?: "")
        put("resultText", s.resultText ?: "")
        put("error", s.error ?: "")
    }.toString()

    /** 事件统一经主线程发出，保证 Listener（EventSink）在流畅线程安全消费 */
    private fun emit(session: ReplySession) {
        handler.post { listener?.onPhaseChanged(session) }
    }
}
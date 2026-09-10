package com.example.lovekey_clone

import android.content.Context
import org.json.JSONObject
import java.util.concurrent.Executors

/**
 * AI 回复的本地兜底生成器（状态机的真正生产者）。
 *
 * 定位：Flutter 侧 LLM 是主路径；但当 Flutter 不可达、离线或调用失败时，原生层仍要
 * 保证"AI 回复"功能可用——由本对象在后台线程用本地模板即时生成，并把结果通过
 * AIReplyScheduler 的 succeed(batch, text) 交回状态机，触发 IME 上屏与事件广播。
 *
 * 规则（与键盘面板保持一致的商业质感）：
 *  1. 用户在当前场景下的自定义话术优先（SettingsStore 实时读取，尊重增删改）
 *  2. 其次场景内置话术池（含亲密度个性化）
 *  3. 上下文 contextText 非空时做轻度前缀编排（"对方说 X → 回应"）
 *
 * 线程模型：单线程 Executor 串行生成，天然避免并发写状态机；begin/succeed 本身
 * 在状态机内加锁 + 令牌校验，双保险。
 */
object AIReplyProducer {

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "air-reply-producer").apply { isDaemon = true }
    }

    /** 内置场景兜底池（各场景两档：通用 / 亲密加成） */
    private val SCENE_POOL: Map<String, List<String>> = mapOf(
        SettingsStore.SCENE_FLIRT to listOf(
            "今天和你聊天感觉特别开心，嘴角一直没下来过",
            "你这么一说，我都想立刻见到你了"
        ),
        SettingsStore.SCENE_COMFORT to listOf(
            "别太往心里去，你已经做得很好了",
            "难受就跟我讲讲，我一直都在"
        ),
        SettingsStore.SCENE_DAILY to listOf(
            "刚忙完，想你了就过来看看消息",
            "今天天气不错，心情也跟着好起来了"
        ),
        SettingsStore.SCENE_ARGUE to listOf(
            "先别生气，我们好好说，我刚刚语气不对",
            "我在意你才这样，我们冷静下来聊聊好吗"
        ),
        SettingsStore.SCENE_GENERAL to listOf(
            "收到啦，刚看到消息就马上回你了",
            "嗯嗯，在的，你说我听着"
        )
    )

    /**
     * 发起一次本地兜底生成：begin → 后台生成 → succeed/fail。
     * @param scene 场景（SettingsStore.SCENE_*）
     * @param contextText 对方消息上下文（可空）
     */
    fun start(context: Context, scene: String, contextText: String?) {
        val session = AIReplyScheduler.begin(scene, contextText)
        val batch = session.batch
        executor.execute {
            try {
                // 模拟 LLM 思考延迟（约 0.8s），给 UI 展示生成态
                Thread.sleep(800)
                val text = buildLocalReply(context, scene, contextText)
                AIReplyScheduler.succeed(batch, text)
            } catch (e: Exception) {
                AIReplyScheduler.fail(batch, e.message ?: "local generation failed")
            }
        }
    }

    /** 本地模板生成：自定义话术 → 场景池（亲密加成）→ 上下文编排 */
    private fun buildLocalReply(context: Context, scene: String, contextText: String?): String {
        val intimacy = SettingsStore.getIntimacy(context)
        val persona = SettingsStore.getPersona(context)

        // 1. 自定义话术优先
        val custom = SettingsStore.getCustomPhrases(context)
            .asSequence()
            .filter { it.optString("scene", SettingsStore.SCENE_GENERAL) == scene }
            .map { it.optString("text") }
            .filter { it.isNotBlank() }
            .toList()
        if (custom.isNotEmpty()) {
            return pick(custom, intimacy)
        }

        // 2. 场景内置池
        val pool = SCENE_POOL[scene] ?: SCENE_POOL.getValue(SettingsStore.SCENE_GENERAL)
        val text = pick(pool, intimacy)

        // 3. 上下文编排：对方消息 + 人设前缀
        val prefix = contextText?.takeIf { it.isNotBlank() }
        return when {
            prefix != null && persona.isNotBlank() && persona != SettingsStore.DEFAULT_PERSONA ->
                "$text\n（${persona}人设）对方说：${prefix.take(30)}"
            prefix != null -> "$text\n对方说：${prefix.take(30)}"
            else -> text
        }
    }

    /** 按亲密度偏移选取：亲密度越高越倾向取后面的"亲密加成"文案 */
    private fun pick(list: List<String>, intimacy: Int): String {
        if (list.isEmpty()) return "嗯嗯，我在呢"
        val index = (list.size - 1) * intimacy.coerceIn(0, 100) / 100
        return list[index.coerceIn(0, list.size - 1)]
    }
}
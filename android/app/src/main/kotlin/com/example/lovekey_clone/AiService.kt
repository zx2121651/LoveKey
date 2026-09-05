package com.example.lovekey_clone

import kotlinx.coroutines.delay

/**
 * AI 回复结果：风格标签 + 文案内容
 */
data class AiReply(val style: String, val text: String)

/**
 * AI 服务抽象层。
 * 后续接入真实大模型（GPT / 文心 / 通义等）时，
 * 只需用真实实现替换 AiServiceProvider.instance 即可，UI 层无需改动。
 */
interface AiService {
    /** 根据对方的话（或当前草稿）生成多条候选回复 */
    suspend fun generateReplies(input: String, persona: String = "通用"): List<AiReply>

    /** 对用户已写好的草稿做润色改写 */
    suspend fun refineDraft(draft: String): List<AiReply>

    /** 根据用户自定义意图描述直接生成一段文案 */
    suspend fun generateFromPrompt(prompt: String): String
}

/**
 * AI 服务提供者（全局单例）。
 * 注意：接入真实大模型时，把 instance 替换为真实实现即可。
 */
object AiServiceProvider {
    var instance: AiService = MockAiService()
}

/**
 * Mock 实现：用固定延迟 + 精心编写的中文恋爱话术模拟真实大模型返回。
 */
class MockAiService : AiService {

    // 各人设对应的候选回复池
    private val replyPool: Map<String, List<AiReply>> = mapOf(
        "通用" to listOf(
            AiReply("高情商", "被你这么一说，我今天的心情都跟着变好了"),
            AiReply("幽默", "收到收到，已第一时间向我的心跳汇报了此事"),
            AiReply("贴心", "那你先忙，忙完记得跟我说一声，我等你"),
            AiReply("暧昧拉扯", "你这样子，可是会让我误会的哦")
        ),
        "恋爱大师" to listOf(
            AiReply("暧昧拉扯", "本来心情一般，看到你的消息突然就好了，你说怪不怪"),
            AiReply("直球", "不绕弯子了，我就是想你了"),
            AiReply("高情商", "你这句话我截图收藏了，以后每天都要拿出来看一遍"),
            AiReply("幽默", "警告你，再这么可爱我可要开始收费了"),
            AiReply("贴心", "累不累？要不要我把肩膀快递给你靠一会儿")
        ),
        "幽默" to listOf(
            AiReply("幽默", "你这话说的，我差点就信了，还好我机智地截了图"),
            AiReply("幽默", "跟你聊天挺费电的，嘴角一直上扬根本停不下来"),
            AiReply("高情商", "别人聊天靠流量，我们聊天靠默契"),
            AiReply("直球", "废话不多说，今晚我请客，你负责来就行")
        ),
        "暖男" to listOf(
            AiReply("贴心", "别太累了，你的辛苦我都看在眼里"),
            AiReply("贴心", "外面降温了，出门记得多穿一件，围巾带了吗"),
            AiReply("直球", "有我在，天塌下来也先砸我"),
            AiReply("高情商", "你只管开心就好，剩下的都交给我")
        )
    )

    override suspend fun generateReplies(input: String, persona: String): List<AiReply> {
        delay(1400) // 模拟网络请求延迟
        return replyPool[persona] ?: replyPool.getValue("通用")
    }

    override suspend fun refineDraft(draft: String): List<AiReply> {
        delay(1400) // 模拟网络请求延迟
        return listOf(
            AiReply("高情商", "原来是这样呀，那我可要好好表现一下了~"),
            AiReply("幽默", "大师，我悟了！这就照着做"),
            AiReply("暧昧拉扯", "那就要看你的表现咯……"),
            AiReply("直球", "其实我早就想这么说了，只是差一个合适的时机")
        )
    }

    override suspend fun generateFromPrompt(prompt: String): String {
        delay(1400) // 模拟网络请求延迟
        return "关于「$prompt」，我想认真地说：你的每一份心意我都有收到，往后的日子，就让我用行动慢慢回应你。"
    }
}

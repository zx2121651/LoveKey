package com.example.lovekey_clone

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Shared settings between the Flutter host app and the IME.
 * Both run in the same process, so SharedPreferences is a reliable bridge.
 *
 * 增强点：
 *  - 实时变更监听：任意一方修改设置，监听方可立即感知，无需轮询
 *  - JSON 健壮性：解析失败自动自愈回默认值，不抛异常
 *  - 自定义人设增删改查（按 id 更新 / 去重）
 *  - 批量快照 getAllSettings()，方便一次性同步
 */
object SettingsStore {

    private const val PREFS_NAME = "lovekey_settings"
    private const val KEY_INTIMACY = "intimacy_level"
    private const val KEY_PERSONA = "selected_persona"
    private const val KEY_CUSTOM_PERSONAS = "custom_personas"
    private const val KEY_BALL_X = "floatball_x"
    private const val KEY_BALL_Y = "floatball_y"

    const val DEFAULT_INTIMACY = 50
    const val DEFAULT_PERSONA = "通用"

    fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ------------------------------------------------------------------
    // 实时变更监听
    // ------------------------------------------------------------------

    /**
     * 注册设置变更监听。返回的 Runnable 用于注销，避免泄漏。
     * 典型使用：IME 在 onCreate 注册、onDestroy 注销；
     * Flutter 侧也可通过 EventChannel 监听。
     */
    fun registerChangeListener(
        context: Context,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ): Runnable {
        val prefs = getPrefs(context)
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return Runnable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    // ------------------------------------------------------------------
    // 亲密度 / 人设
    // ------------------------------------------------------------------

    fun getIntimacy(context: Context): Int =
        getPrefs(context).getInt(KEY_INTIMACY, DEFAULT_INTIMACY)

    fun setIntimacy(context: Context, level: Int) {
        getPrefs(context).edit().putInt(KEY_INTIMACY, level.coerceIn(0, 100)).apply()
    }

    fun getPersona(context: Context): String =
        getPrefs(context).getString(KEY_PERSONA, DEFAULT_PERSONA) ?: DEFAULT_PERSONA

    fun setPersona(context: Context, name: String) {
        getPrefs(context).edit().putString(KEY_PERSONA, name.ifBlank { DEFAULT_PERSONA }).apply()
    }

    // ------------------------------------------------------------------
    // 自定义人设（增删改查 + 去重 + 自愈）
    // ------------------------------------------------------------------

    /**
     * 读取自定义人设列表；解析失败（数据损坏）时自愈回空列表。
     * 会过滤掉缺失 id / name 的非法条目。
     */
    fun getCustomPersonas(context: Context): List<JSONObject> {
        val raw = getPrefs(context).getString(KEY_CUSTOM_PERSONAS, "[]") ?: "[]"
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.optJSONObject(i) ?: return@mapNotNull null
                if (obj.optString("id").isBlank() || obj.optString("name").isBlank()) null else obj
            }
        }.getOrDefault(emptyList())
    }

    fun getCustomPersonaById(context: Context, id: String): JSONObject? =
        getCustomPersonas(context).firstOrNull { it.optString("id") == id }

    /** 新增或覆盖同名 id 的自定义人设 */
    fun saveCustomPersona(context: Context, persona: JSONObject) {
        val id = persona.optString("id").ifBlank { return }
        val list = getCustomPersonas(context).toMutableList()
        val existingIndex = list.indexOfFirst { it.optString("id") == id }
        if (existingIndex >= 0) list[existingIndex] = persona else list.add(persona)
        persistCustomPersonas(context, list)
    }

    fun deleteCustomPersona(context: Context, id: String) {
        val list = getCustomPersonas(context).filter { it.optString("id") != id }
        persistCustomPersonas(context, list)
    }

    private fun persistCustomPersonas(context: Context, list: List<JSONObject>) {
        getPrefs(context).edit().putString(KEY_CUSTOM_PERSONAS, encodeList(list)).apply()
    }

    private fun encodeList(list: List<JSONObject>): String {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    // ------------------------------------------------------------------
    // 悬浮球位置持久化
    // ------------------------------------------------------------------

    fun getBallPosition(context: Context): Pair<Int, Int> {
        val prefs = getPrefs(context)
        return prefs.getInt(KEY_BALL_X, -1) to prefs.getInt(KEY_BALL_Y, -1)
    }

    fun setBallPosition(context: Context, x: Int, y: Int) {
        getPrefs(context).edit().putInt(KEY_BALL_X, x).putInt(KEY_BALL_Y, y).apply()
    }

    // ------------------------------------------------------------------
    // 批量快照 / 重置
    // ------------------------------------------------------------------

    /** 一次性读取全部设置，供 Flutter 端启动同步 */
    fun getAllSettings(context: Context): JSONObject = JSONObject().apply {
        put(KEY_INTIMACY, getIntimacy(context))
        put(KEY_PERSONA, getPersona(context))
        put(KEY_CUSTOM_PERSONAS, JSONArray(getCustomPersonas(context).map { it.toString() }))
    }

    /** 恢复默认设置（保留悬浮球位置） */
    fun resetSettings(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_INTIMACY)
            .remove(KEY_PERSONA)
            .remove(KEY_CUSTOM_PERSONAS)
            .apply()
    }
}

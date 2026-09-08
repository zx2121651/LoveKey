package com.example.lovekey_clone

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Shared settings between the Flutter host app and the IME.
 * Both run in the same process, so SharedPreferences is a reliable bridge.
 */
object SettingsStore {
    private const val PREFS_NAME = "lovekey_settings"
    private const val KEY_INTIMACY = "intimacy_level"
    private const val KEY_PERSONA = "selected_persona"
    private const val KEY_CUSTOM_PERSONAS = "custom_personas"

    fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getIntimacy(context: Context): Int =
        getPrefs(context).getInt(KEY_INTIMACY, 50)

    fun setIntimacy(context: Context, level: Int) {
        getPrefs(context).edit().putInt(KEY_INTIMACY, level.coerceIn(0, 100)).apply()
    }

    fun getPersona(context: Context): String =
        getPrefs(context).getString(KEY_PERSONA, "通用") ?: "通用"

    fun setPersona(context: Context, name: String) {
        getPrefs(context).edit().putString(KEY_PERSONA, name).apply()
    }

    fun getCustomPersonas(context: Context): List<JSONObject> {
        val raw = getPrefs(context).getString(KEY_CUSTOM_PERSONAS, "[]") ?: "[]"
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getJSONObject(it) }
        }.getOrDefault(emptyList())
    }

    fun saveCustomPersona(context: Context, persona: JSONObject) {
        val list = getCustomPersonas(context).toMutableList().also { it.add(persona) }
        getPrefs(context).edit().putString(KEY_CUSTOM_PERSONAS, encodeList(list)).apply()
    }

    fun deleteCustomPersona(context: Context, id: String) {
        val list = getCustomPersonas(context).filter { it.optString("id") != id }
        getPrefs(context).edit().putString(KEY_CUSTOM_PERSONAS, encodeList(list)).apply()
    }

    private fun encodeList(list: List<JSONObject>): String {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }
}

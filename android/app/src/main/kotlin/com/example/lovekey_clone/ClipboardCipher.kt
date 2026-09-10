package com.example.lovekey_clone

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 剪贴板历史加密工具：AES-256-GCM，密钥保存在 Android Keystore（不落盘）。
 *
 * 商业级隐私要求：剪贴板内容可能含密码 / 验证码等敏感信息，
 * 明文写 SharedPreferences 不符合合规标准，故统一加密后落盘。
 *
 * 兼容策略：
 *  - 旧版本明文数据：decrypt 返回 null，调用方回退原文，下次持久化时自动迁移为加密；
 *  - API 23 以下无 Keystore：降级为明文（功能可用，隐私弱化，低版本占比极小）。
 */
object ClipboardCipher {

    private const val KEY_ALIAS = "lovekey_clipboard_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_BITS = 128
    private const val PREFIX = "enc:"

    private val keystoreSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    /** 判定是否为已加密格式（用于旧明文迁移判断） */
    fun isEncrypted(raw: String): Boolean = raw.startsWith(PREFIX)

    /** 加密为 enc:<iv_b64>:<ct_b64>；不支持 Keystore 时原样返回 */
    fun encrypt(context: Context, plain: String): String {
        if (plain.isEmpty() || !keystoreSupported) return plain
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
            PREFIX +
                Base64.encodeToString(iv, Base64.NO_WRAP) + ":" +
                Base64.encodeToString(encrypted, Base64.NO_WRAP)
        }.getOrDefault(plain) // 加密异常（罕见）：降级明文，保证功能可用
    }

    /** 解密；非加密格式（旧明文 / 损坏）返回 null */
    fun decrypt(context: Context, raw: String): String? {
        if (!raw.startsWith(PREFIX) || !keystoreSupported) return null
        return runCatching {
            val body = raw.removePrefix(PREFIX)
            val parts = body.split(":")
            if (parts.size != 2) {
                null
            } else {
                val iv = Base64.decode(parts[0], Base64.NO_WRAP)
                val data = Base64.decode(parts[1], Base64.NO_WRAP)
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
                String(cipher.doFinal(data), Charsets.UTF_8)
            }
        }.getOrNull()
    }
}

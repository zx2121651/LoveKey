package com.example.lovekey_clone

import android.content.Context
import android.content.res.AssetManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 将 assets/rime 下的引擎资源部署到 filesDir/rime。
 *
 * 增强点：
 *  - 版本校验：按 assets/rime/version.txt 记录部署版本，未变更则跳过复制，避免每次启动全量 IO
 *  - 递归复制：支持子目录（schema 依赖的 opencc / 子词典等）
 *  - 版本不一致时先清理旧目录，防止残留脏数据
 *  - 用户词典保护：清理前把 build/*.userdb 迁移到独立目录 rime_user，
 *    升级词库 / 引擎资源不再丢学习记录（自造词、词频）
 */
object RimeDeployer {

    private const val VERSION_FILE = "version.txt"
    private const val RIME_ASSETS_PATH = "rime"
    private const val PREFS_NAME = "rime_deploy"
    private const val KEY_VERSION = "version"

    /** 用户词典独立目录（与部署目录同级的 rime_user），见 Rime.startup */
    private const val USER_DIR_NAME = "rime_user"

    /** @return 是否部署成功（version.txt 缺失视为失败） */
    fun deployAssets(context: Context, sharedDir: String): Boolean {
        val assetManager = context.assets
        val destDir = File(sharedDir)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val assetVersion = readAssetVersion(assetManager) ?: return false
        if (destDir.exists() && prefs.getString(KEY_VERSION, null) == assetVersion) {
            return true // 已部署且版本一致，跳过
        }

        // 升级前迁移用户词典，防止全量清理丢失学习记录
        migrateUserDbs(File(destDir, "build"), File(File(context.filesDir, USER_DIR_NAME), "build"))

        destDir.deleteRecursively()
        destDir.mkdirs()
        copyDir(assetManager, RIME_ASSETS_PATH, destDir)
        prefs.edit().putString(KEY_VERSION, assetVersion).apply()
        return true
    }

    /** 把 srcBuild 下的 *.userdb 目录移动到 dstBuild（目标已存在时跳过，避免覆盖） */
    private fun migrateUserDbs(srcBuild: File, dstBuild: File) {
        if (!srcBuild.isDirectory) return
        srcBuild.listFiles()
            ?.filter { it.isDirectory && it.name.endsWith(".userdb") }
            ?.forEach { db ->
                dstBuild.mkdirs()
                val dest = File(dstBuild, db.name)
                if (!dest.exists()) {
                    if (!db.renameTo(dest)) {
                        runCatching { db.copyRecursively(dest, overwrite = true) }
                        db.deleteRecursively()
                    }
                }
            }
    }

    private fun readAssetVersion(assetManager: AssetManager): String? = runCatching {
        assetManager.open("$RIME_ASSETS_PATH/$VERSION_FILE").bufferedReader().use { it.readText().trim() }
    }.getOrNull()

    private fun copyDir(assetManager: AssetManager, assetPath: String, destDir: File) {
        val entries = assetManager.list(assetPath) ?: return
        for (name in entries) {
            val childAsset = "$assetPath/$name"
            val childFile = File(destDir, name)
            if (isDirectory(assetManager, childAsset)) {
                childFile.mkdirs()
                copyDir(assetManager, childAsset, childFile)
            } else {
                copyFile(assetManager, childAsset, childFile)
            }
        }
    }

    /** assets 里区分目录与文件：能 open 则是文件，抛 IOException 则是目录 */
    private fun isDirectory(assetManager: AssetManager, assetPath: String): Boolean {
        return try {
            assetManager.open(assetPath).close()
            false
        } catch (e: IOException) {
            true
        }
    }

    private fun copyFile(assetManager: AssetManager, assetPath: String, destFile: File) {
        val input = assetManager.open(assetPath)
        val output = FileOutputStream(destFile)
        input.use { ins ->
            output.use { outs ->
                val buffer = ByteArray(4096)
                var read: Int
                while (ins.read(buffer).also { read = it } != -1) {
                    outs.write(buffer, 0, read)
                }
            }
        }
    }
}

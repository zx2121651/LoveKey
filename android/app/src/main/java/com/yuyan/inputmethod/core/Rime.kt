package com.yuyan.inputmethod.core

import android.content.Context
import java.io.File
import kotlin.system.measureTimeMillis

class Rime(context: Context, fullCheck: Boolean) {

    init {
        startup(context, fullCheck)
    }

    companion object {
        private var instance: Rime? = null
        var mContext: RimeContext? = null
        var mStatus: RimeStatus? = null

        @JvmStatic
        fun getInstance(context: Context, fullCheck: Boolean = false): Rime {
            if (instance == null) instance = Rime(context, fullCheck)
            return instance!!
        }

        init {
            System.loadLibrary("yuyanime")
        }

        fun startup(context: Context, fullCheck: Boolean) {
            val sharedDir = context.filesDir.absolutePath + "/rime"
            val userDir = context.filesDir.absolutePath + "/rime_user"
            // 用户词典独立目录：与部署目录分离，升级引擎资源 / 词库时学习记录不受影响
            File(userDir).mkdirs()
            // 兜底迁移：代码首次升级且部署版本未变化时，旧 userdb 仍留在 rime/build
            migrateLegacyUserDbs(File(sharedDir, "build"), File(userDir, "build"))
            startupRime(context, sharedDir, userDir, fullCheck)
            updateStatus()
        }

        /** 把 srcBuild 下的 *.userdb 迁移到 dstBuild（幂等，目标已存在则跳过） */
        private fun migrateLegacyUserDbs(srcBuild: File, dstBuild: File) {
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

        @JvmStatic
        fun destroy() {
            exitRime()
            instance = null
        }

        fun updateStatus() {
            measureTimeMillis {
                mStatus = getRimeStatus() ?: RimeStatus()
            }
        }

        fun updateContext() {
            measureTimeMillis {
                mContext = getRimeContext() ?: RimeContext()
            }
            updateStatus()
        }

        @JvmStatic
        val isComposing get() = mStatus?.isComposing == true

        @JvmStatic
        fun hasMenu(): Boolean {
            return isComposing && mContext?.menu?.numCandidates != 0
        }

        @JvmStatic
        fun hasRight(): Boolean {
            return hasMenu() && mContext?.menu?.isLastPage == false
        }

        @JvmStatic
        val composition: RimeComposition?
            get() = mContext?.composition

        @JvmStatic
        val compositionText: String
            get() = composition?.preedit ?: ""

        @JvmStatic
        fun processKey(keycode: Int, mask: Int): Boolean {
            if (keycode <= 0 || keycode == 0xffffff) return false
            return processRimeKey(keycode, mask).also {
                updateContext()
            }
        }

        /** 设置候选每页数量（默认由 default.custom.yaml 的 menu.page_size 决定） */
        @JvmStatic
        fun setPageSize(size: Int) {
            setRimePageSize(size.coerceIn(1, 100))
        }

        /** 候选翻页：上一页 / 下一页 */
        @JvmStatic
        fun pageUp(): Boolean = processKey(getRimeKeycodeByName("Page_Up"), 0)

        @JvmStatic
        fun pageDown(): Boolean = processKey(getRimeKeycodeByName("Page_Down"), 0)

        @JvmStatic
        val isAsciiMode: Boolean get() = mStatus?.isAsciiMode ?: false

        @JvmStatic
        val hasPrevPage: Boolean get() = (mContext?.menu?.pageNo ?: 0) > 0

        @JvmStatic
        val hasNextPage: Boolean get() = mContext?.menu?.isLastPage == false

        @JvmStatic
        val currentPageNo: Int get() = mContext?.menu?.pageNo ?: 0

        @JvmStatic
        fun replaceKey(caretPos: Int, length: Int, key: String): Boolean {
            return replaceRimeKey(caretPos, length, key).also {
                updateContext()
            }
        }

        @JvmStatic
        fun clearComposition() { clearRimeComposition()
            updateContext()
        }

        @JvmStatic
        fun selectCandidate(index: Int): Boolean {
            return selectRimeCandidate(index).also {
                updateContext()
            }
        }

        @JvmStatic
        fun setOption(option: String, value: Boolean) {
            setRimeOption(option, value)
        }

        @JvmStatic
        fun selectSchema(schemaId: String): Boolean {
            return selectRimeSchema(schemaId).also {
                updateContext()
            }
        }

        fun getAssociateList(key: String?): Array<String?> {
            return getRimeAssociateList(key)
        }

        fun chooseAssociate(index: Int): Boolean {
            return selectRimeAssociate(index)
        }

        @JvmStatic
        external fun startupRime(context: Context, sharedDir: String, userDir: String, fullCheck: Boolean, )

        @JvmStatic
        external fun exitRime()

        @JvmStatic
        external fun setRimePageSize(pageSize:Int)

        @JvmStatic
        external fun processRimeKey(keycode: Int, mask: Int): Boolean

        @JvmStatic
        external fun replaceRimeKey(caretPos: Int, length: Int, key: String?): Boolean

        @JvmStatic
        external fun clearRimeComposition()

        @JvmStatic
        external fun getRimeCommit(): RimeCommit?

        @JvmStatic
        external fun getRimeContext(): RimeContext?

        @JvmStatic
        external fun getRimeStatus(): RimeStatus?

        @JvmStatic
        external fun setRimeOption(option: String, value: Boolean, )

        @JvmStatic
        external fun getCurrentRimeSchema(): String

        @JvmStatic
        external fun selectRimeSchema(schemaId: String): Boolean

        @JvmStatic
        external fun selectRimeCandidate(index: Int): Boolean

        @JvmStatic
        external fun getRimeKeycodeByName(name: String): Int

        @JvmStatic
        external fun getRimeAssociateList(key: String?): Array<String?>

        @JvmStatic
        external fun selectRimeAssociate(index: Int): Boolean
    }
}

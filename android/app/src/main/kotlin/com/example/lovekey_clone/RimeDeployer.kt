package com.example.lovekey_clone

import android.content.Context
import java.io.File
import java.io.FileOutputStream

object RimeDeployer {

    fun deployAssets(context: Context, sharedDir: String) {
        val assetManager = context.assets
        val rimeAssetsPath = "rime"
        val destDir = File(sharedDir)

        if (!destDir.exists()) {
            destDir.mkdirs()
        }

        try {
            val files = assetManager.list(rimeAssetsPath) ?: return
            for (filename in files) {
                val destFile = File(destDir, filename)
                val inputStream = assetManager.open("$rimeAssetsPath/$filename")
                val outputStream = FileOutputStream(destFile)

                val buffer = ByteArray(1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }

                inputStream.close()
                outputStream.flush()
                outputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

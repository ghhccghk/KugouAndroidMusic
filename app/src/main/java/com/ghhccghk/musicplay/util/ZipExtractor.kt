package com.ghhccghk.musicplay.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

object ZipExtractor {

    fun extractZipOnFirstRun(context: Context, zipFileName: String, outputDirName: String) {
        val outputDir = File(context.filesDir, outputDirName)

        if (!outputDir.exists()) {
            outputDir.mkdirs()

            try {
                context.assets.open(zipFileName).use { inputStream ->
                    ZipInputStream(inputStream).use { zipStream ->
                        var entry = zipStream.nextEntry
                        while (entry != null) {
                            if (!entry.isDirectory) {
                                val outFile = File(outputDir, entry.name)
                                outFile.parentFile?.mkdirs()

                                FileOutputStream(outFile).use { output ->
                                    val buffer = ByteArray(4096)
                                    var len: Int
                                    while (zipStream.read(buffer).also { len = it } > 0) {
                                        output.write(buffer, 0, len)
                                    }
                                }
                            }
                            zipStream.closeEntry()
                            entry = zipStream.nextEntry
                        }
                    }
                }
                Log.d("ZipExtractor", "解压完成：$zipFileName -> ${outputDir.absolutePath}")
            } catch (e: Exception) {
                Log.e("ZipExtractor", "解压失败：${e.message}")
            }
        } else {
            Log.d("ZipExtractor", "已解压过，跳过：${outputDir.absolutePath}")
        }
    }
}

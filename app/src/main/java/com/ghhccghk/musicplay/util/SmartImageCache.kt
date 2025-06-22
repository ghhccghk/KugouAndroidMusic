package com.ghhccghk.musicplay.util

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest

object SmartImageCache {
    private lateinit var cacheDir: File
    private var maxCacheSize: Long = 50L * 1024 * 1024 // 默认50MB
    private val client = OkHttpClient()

    fun init(context: Context, dirName: String = "cache/smart_image_cache", maxSize: Long = maxCacheSize) {
        cacheDir = File(context.getExternalFilesDir(null), dirName).apply { mkdirs() }
        maxCacheSize = maxSize
    }

    fun hasCache(url: String, customHash: String? = null): Boolean {
        val fileName = (customHash ?: url).md5()
        val file = File(cacheDir, fileName)
        return file.exists()
    }

    fun getCachedUri(url: String , customHash: String? = null): Uri? {
        val fileName = (customHash ?: url).md5()
        val file = File(cacheDir,fileName)
        return if (file.exists()) Uri.fromFile(file) else null
    }

    suspend fun getOrDownload(url: String, customHash: String? = null): Uri? = withContext(Dispatchers.IO) {
        val fileName = (customHash ?: url).md5()
        val file = File(cacheDir, fileName)

        if (file.exists()) {
            file.setLastModified(System.currentTimeMillis())
            Log.d("SmartImageCache", "获取到缓存 : $file")
            return@withContext Uri.fromFile(file)
        }

        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.bytes()
                if (body != null) {
                    file.writeBytes(body)
                    file.setLastModified(System.currentTimeMillis())
                    trimCache()
                    Log.d("SmartImageCache", "request success 获取到缓存 : $file")
                    return@withContext Uri.fromFile(file)
                }
            }
        } catch (e: Exception) {
            Log.d("SmartImageCache", "报错 : ${e} url :$url")
            e.printStackTrace()
        }

        return@withContext null
    }


    fun clearAll() {
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
    }

    private fun trimCache() {
        val files = cacheDir.listFiles() ?: return
        var total = files.sumOf { it.length() }
        if (total <= maxCacheSize) return

        files.sortedBy { it.lastModified() }.forEach {
            if (it.delete()) total -= it.length()
            if (total <= maxCacheSize) return
        }
    }

    private fun String.md5(): String {
        val digest = MessageDigest.getInstance("MD5")
        return digest.digest(toByteArray()).joinToString("") { "%02x".format(it) }
    }
}

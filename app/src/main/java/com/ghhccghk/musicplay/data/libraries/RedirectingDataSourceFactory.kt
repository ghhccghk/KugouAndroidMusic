package com.ghhccghk.musicplay.data.libraries

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.data.songurl.getsongurl.GetSongUrlBase
import com.ghhccghk.musicplay.util.UrlCacheManager
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@UnstableApi
class RedirectingDataSourceFactory(
    private val defaultFactory: DataSource.Factory
) : DataSource.Factory {

    override fun createDataSource(): DataSource {
        return RedirectingDataSource(defaultFactory.createDataSource())
    }
}

@UnstableApi
class RedirectingDataSource(
    private val actualDataSource: DataSource
) : DataSource {

    private var currentUri: Uri? = null
    private var id: String? = null
    private var perfs = MainActivity.lontext.getSharedPreferences("play_setting_prefs", 0)
    private val quality = perfs.getString("song_quality","128").toString()

    override fun addTransferListener(transferListener: TransferListener) {

    }

    override fun open(dataSpec: DataSpec): Long {
        if (dataSpec.uri.scheme == "musicplay" && dataSpec.uri.host == "playurl") {
            val cid = dataSpec.uri.getQueryParameter("id") ?: "0"
            val url = dataSpec.uri.getQueryParameter("url") ?: ""
            val hash = dataSpec.uri.getQueryParameter("hash") ?: ""
            id = (cid + quality)
            currentUri = runBlocking {
                resolveUrl(hash, url)
            }
            if (currentUri == null || currentUri.toString() == "") {
                currentUri = url.toUri()
            }
        } else {
            currentUri = dataSpec.uri
        }

        val redirectedSpec = dataSpec.buildUpon().setUri(currentUri!!).setKey(id!!).build()
        return actualDataSource.open(redirectedSpec)
    }

    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        return actualDataSource.read(buffer, offset, readLength)
    }

    override fun getUri(): Uri? = currentUri

    override fun close() {
        actualDataSource.close()
    }

    /**
     * 简单的 URL 可用性检测（如 HEAD 请求或连接尝试）
     */
    private fun isUrlAvailable(url: String?): Boolean {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "HEAD"
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.responseCode in 200..399
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchRealUrlFromServer(id: String): String? {
        return try {
            val json = withContext(Dispatchers.IO) {
                KugouAPi.getSongsUrl(id)
            }
            val gson = Gson()
            val re = gson.fromJson(json, GetSongUrlBase::class.java)
            re.url?.getOrNull(1) ?: re.url?.getOrNull(0) ?: re.backupUrl?.getOrNull(1) ?:re.backupUrl?.getOrNull(0) ?: ""
        } catch (e: Exception) {
            null
        }
    }

    suspend fun resolveUrl(hash: String, fallbackUrl: String?): Uri? = withContext(Dispatchers.IO) {
        val songid = (hash + quality)
        var finalUrl: String? = UrlCacheManager.get(songid)?.url

        if (finalUrl.isNullOrEmpty() || !isUrlAvailable(finalUrl)) {
            // 缓存无效，尝试重新请求
            finalUrl = fetchRealUrlFromServer(hash)

            if (!finalUrl.isNullOrEmpty() && isUrlAvailable(finalUrl)) {
                UrlCacheManager.put(songid, finalUrl)
            } else {
                // 失败，使用 fallback 或旧缓存
                finalUrl = fallbackUrl?.ifEmpty { UrlCacheManager.get(hash)?.url }
            }
        }
        return@withContext finalUrl?.toUri()

    }

}

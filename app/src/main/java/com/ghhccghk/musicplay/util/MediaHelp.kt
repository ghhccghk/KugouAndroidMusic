package com.ghhccghk.musicplay.util

import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.Log
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.data.libraries.MediaItemEntity
import com.ghhccghk.musicplay.data.searchLyric.searchLyricBase
import com.ghhccghk.musicplay.data.songurl.getsongurl.GetSongUrlBase
import com.ghhccghk.musicplay.data.user.playListDetail.songlist.Song
import com.ghhccghk.musicplay.data.user.playListDetail.songlist.SongListBase
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.ghhccghk.musicplay.util.others.toMediaItem
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.URLEncoder

object MediaHelp {
    suspend fun createMediaItemWithId(artist: String?, title: String?, url: String, result: GetSongUrlBase, hash: String,album_audio_id: String): MediaItem {
        val mediaId = "$artist - $title"
        val urla = result.trans_param.union_cover.replaceFirst("http://", "https://")
            .replaceFirst("/{size}/", "/")
        val urlcache = withContext(Dispatchers.IO) {
            SmartImageCache.getOrDownload(urla,hash)
            return@withContext urla
        }

        val b = withContext(Dispatchers.IO) {
            KugouAPi.getSearchSongLyrics(hash = hash)
        }
        if (b == null || b == "502" || b == "404") {
           Log.e("createMediaItemWithId","load error")
        } else {
            val gson = Gson()
            val resulta = gson.fromJson(b, searchLyricBase::class.java)
            val accesskey = resulta.candidates.getOrNull(0)?.accesskey
            val id = resulta.candidates.getOrNull(0)?.id

            val abc = MediaItemEntity(
                mediaId = mediaId,
                uri = url.toString(),
                artists = artist,
                title = title,
                album = "",
                albumArtists = "",
                trackNumber = 0,
                discNumber = 0,
                thumb = urlcache.toString(),
                lrcId = id,
                songHash = hash,
                lrcAccesskey = accesskey,
                songtitle = title,
                albumAudioId = album_audio_id
            )
            return abc.toMediaItem()
        }
        val data = MediaMetadata.Builder()
            .setExtras(
                Bundle().apply {
                    putString("songHash",hash)
                }
            ).build()
        return MediaItem.Builder().setUri(url).setMediaMetadata(data).build()
    }

    fun getAllSongsFlow(
        ids: String,
        pageSize: Int = 50,
        maxConcurrency: Int = 5
    ): Flow<Song> = flow {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(SongListBase::class.java)

        // 获取第一页
        val firstJson = KugouAPi.getPlayListAllSongs(ids, 1, pageSize) ?: return@flow
        val firstPage = adapter.fromJson(firstJson) ?: return@flow
        val firstData = firstPage.data ?: return@flow

        val totalCount = firstData.count
        val totalPages = (totalCount + pageSize - 1) / pageSize

        // 先 emit 第1页
        firstData.songs.forEach { emit(it) }

        val semaphore = Semaphore(maxConcurrency)

        // 并发加载后续页面，并依次 emit 歌曲
        coroutineScope {
            (2..totalPages).map { page ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        delay(500)
                        try {
                            val json = KugouAPi.getPlayListAllSongs(ids, page, pageSize) ?: return@withPermit emptyList<Song>()
                            val pageData = adapter.fromJson(json)
                            pageData?.data?.songs ?: emptyList()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            emptyList()
                        }
                    }
                }
            }.forEach { deferred ->
                val songs = deferred.await()
                songs.forEach { emit(it) }
            }
        }
    }.flowOn(Dispatchers.IO)//协程网络请求


    fun splitArtistAndTitle(text: String): Pair<String, String>? {
        val parts = text.split(" - ", limit = 2)
        return if (parts.size == 2) {
            parts[0].trim() to parts[1].trim() // 保证去掉首尾空格，保留中间空格
        } else {
            null // 无法拆分，可能格式不标准
        }
    }

    suspend fun List<Song>.toMediaItemListParallel(): List<MediaItem> = coroutineScope {
        this@toMediaItemListParallel
            .map { song ->
                async {
                    delay(500)
                    song.toMediaItemSuspend() // 注意是挂起函数
                }
            }
            .awaitAll()
            .filterNotNull()
    }

    suspend fun Song.toMediaItemSuspend(): MediaItem? {
        var perfs = MainActivity.lontext.getSharedPreferences("play_setting_prefs", 0)
        val quality = perfs.getString("song_quality","128").toString()
        if (this.name.isNullOrBlank() || this.hash.isNullOrBlank() || this.hash == "null" || this.shield != 0) {
            return null
        }

        return try {
            val json = withContext(Dispatchers.IO) {
                KugouAPi.getSongsUrl(hash, quality = quality)
            }
            if (json == null || json == "502" || json == "404") {
                null
            } else {
                val re = Gson().fromJson(json, GetSongUrlBase::class.java)
                val url = re.url?.getOrNull(1) ?: re.url?.getOrNull(0) ?: re.backupUrl?.getOrNull(1) ?:re.backupUrl?.getOrNull(0) ?: ""

                val encodedUrl = URLEncoder.encode(url, "UTF-8")
                val uri = "musicplay://playurl?id=${name + hash}&url=$encodedUrl&hash=${hash}".toUri().toString()

                val (title, artist) = splitArtistAndTitle(name ?: "未知歌曲") ?: return null

                createMediaItemWithId(title, artist, uri, re,re.hash,this.add_mixsongid.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
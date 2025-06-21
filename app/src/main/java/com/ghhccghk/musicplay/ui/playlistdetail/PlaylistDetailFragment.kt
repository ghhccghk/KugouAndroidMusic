package com.ghhccghk.musicplay.ui.playlistdetail

import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.libraries.MediaItemEntity
import com.ghhccghk.musicplay.data.searchLyric.searchLyricBase
import com.ghhccghk.musicplay.data.songurl.getsongurl.GetSongUrlBase
import com.ghhccghk.musicplay.data.user.playListDetail.PlayListDetail
import com.ghhccghk.musicplay.data.user.playListDetail.songlist.Song
import com.ghhccghk.musicplay.data.user.playListDetail.songlist.SongListBase
import com.ghhccghk.musicplay.databinding.FragmentPlaylistBinding
import com.ghhccghk.musicplay.util.SmartImageCache
import com.ghhccghk.musicplay.util.adapte.SongAdapter
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.ghhccghk.musicplay.util.others.toMediaItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class PlaylistDetailFragment : Fragment() {


    private var _binding: FragmentPlaylistBinding? = null
    private lateinit var player : MediaController

    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player = MainActivity.controllerFuture.get()

        // 隐藏 BottomNavigationView
        val a = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        hideBottomNav(a)

        KugouAPi.init()
        if (MainActivity.isNodeRunning){
            val playlistId = arguments?.getString("playlistId",null)
            val picurl = arguments?.getString("picurl",null)
            val theme = arguments?.getBoolean("theme",false)
            if (playlistId != null && theme == false){
                lifecycleScope.launch {
                    val json = withContext(Dispatchers.IO) {
                        KugouAPi.getPlayListDetail(playlistId)
                    }
                    if (json == null || json == "502" || json == "404") {
                        Toast.makeText(context, "数据加载失败", Toast.LENGTH_LONG).show()
                    } else {
                        try {
                            val gson = Gson()
                            val result = gson.fromJson(json, PlayListDetail::class.java)
                            val playList = result.data
                            binding.tvPlaylistName.text = playList[0].name
                            binding.tvCreator.text = playList[0].list_create_username
                            binding.tvIntro.text = playList[0].intro
                            if (picurl != null){
                                Log.d("picurl",picurl)
                                val urlcache = withContext(Dispatchers.IO) {
                                    SmartImageCache.getOrDownload(picurl,picurl.hashCode().toString())
                                }
                                Glide.with(requireContext())
                                    .load(urlcache)
                                    .into(binding.ivPlaylistCover)
                            } else {
                                val secureUrl =
                                    playList[0].create_user_pic.replaceFirst("/{size}/", "/")
                                val urlcache = withContext(Dispatchers.IO) {
                                    SmartImageCache.getOrDownload(secureUrl,secureUrl.hashCode().toString())
                                }
                                Log.d("picurl",picurl.toString())
                                Glide.with(requireContext())
                                    .load(urlcache)
                                    .into(binding.ivPlaylistCover)
                            }


                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(
                                context,
                                "数据加载失败: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }


                }

                lifecycleScope.launch {
                    val json = mutableListOf<Song>()

                    try {
                        binding.playlistAllPlay.setOnClickListener {
                            lifecycleScope.launch {
                                withContext(Dispatchers.Main) {
                                    player.setMediaItems(json.toMediaItemListParallel())
                                }
                            }
                        }
                        binding.rvSongs.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                        val adp = SongAdapter(json){
                            lifecycleScope.launch {
                                val json = withContext(Dispatchers.IO) {
                                    it.hash?.let { hash -> KugouAPi.getSongsUrl(hash) }
                                }
                                if (json == null || json == "502" || json == "404") {
                                    Toast.makeText(context, "SongAdapter 数据加载失败", Toast.LENGTH_LONG).show()
                                } else {
                                    try {
                                        val gson = Gson()
                                        val result = gson.fromJson(json, GetSongUrlBase::class.java)
                                        val re = result
                                        val url = re.url?.getOrNull(1) ?: re.url?.getOrNull(0) ?: re.backupUrl?.getOrNull(1) ?:re.backupUrl?.getOrNull(0) ?: ""


                                        // 含真实 URL 和 ID 的占位 URI
                                        val encodedUrl = URLEncoder.encode(url, "UTF-8")
                                        val uri = "musicplay://playurl?id=${it?.name + it?.hash}&url=${encodedUrl}&hash=${it?.hash}".toUri().toString()

                                        val name = it.name?.let { it1 -> splitArtistAndTitle(it1) }

                                        val item = it?.let { artist -> createMediaItemWithId(name?.first,
                                            name?.second,
                                            uri,
                                            result,result.hash) }

                                        item?.let { mediaItem -> MainActivity.controllerFuture.get().setMediaItem(mediaItem) }

                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(
                                            context,
                                            "数据加载失败: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }
                        binding.rvSongs.adapter = adp

                        getAllSongsFlow(playlistId,30).chunked(10).collect { chunk ->
                            val start = json.size
                            json.addAll(chunk)
                            adp.notifyItemRangeInserted(start, chunk.size)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.d("PlaylistDetailFragment", "onCreateView: ${e.message}")
                    }
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }


    private fun hideBottomNav(bottomNav: BottomNavigationView) {
        val slideOut = ObjectAnimator.ofFloat(bottomNav, "translationY", 0f, bottomNav.height.toFloat())
        slideOut.duration = 100
        slideOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                bottomNav.visibility = View.GONE
            }
        })
        slideOut.start()
    }

    suspend fun createMediaItemWithId(artist: String?, title: String?, url: String, result: GetSongUrlBase,hash: String): MediaItem {
        val mediaId = "$artist - $title"
        val urla = result.trans_param.union_cover.replaceFirst("http://", "https://")
            .replaceFirst("/{size}/", "/")
        val urlcache = withContext(Dispatchers.IO) {
            SmartImageCache.getOrDownload(urla,hash)
        }

        val b = withContext(Dispatchers.IO) {
            KugouAPi.getSearchSongLyrics(hash = hash)
        }
        if (b == null || b == "502" || b == "404") {
            Toast.makeText(context, "数据加载失败", Toast.LENGTH_LONG).show()
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
                songtitle = title
            )
            return abc.toMediaItem()
        }
        return MediaItem.Builder().setUri(url).build()
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
        if (this.name.isNullOrBlank() || this.hash.isNullOrBlank() || this.hash == "null" || this.shield != 0) {
            return null
        }

        return try {
            val json = withContext(Dispatchers.IO) {
                KugouAPi.getSongsUrl(hash)
            }
            if (json == null || json == "502" || json == "404") {
                null
            } else {
                val re = Gson().fromJson(json, GetSongUrlBase::class.java)
                val url = re.url?.getOrNull(1) ?: re.url?.getOrNull(0) ?: re.backupUrl?.getOrNull(1) ?:re.backupUrl?.getOrNull(0) ?: ""

                val encodedUrl = URLEncoder.encode(url, "UTF-8")
                val uri = "musicplay://playurl?id=${name + hash}&url=$encodedUrl&hash=${hash}".toUri().toString()

                val (title, artist) = splitArtistAndTitle(name ?: "未知歌曲") ?: return null

                createMediaItemWithId(title, artist, uri, re,re.hash)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


}

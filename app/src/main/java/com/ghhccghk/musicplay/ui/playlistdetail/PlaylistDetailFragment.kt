package com.ghhccghk.musicplay.ui.playlistdetail

import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.libraries.MediaItemEntity
import com.ghhccghk.musicplay.data.searchLyric.searchLyricBase
import com.ghhccghk.musicplay.data.songurl.getsongurl.GetSongUrlBase
import com.ghhccghk.musicplay.data.user.playListDetail.PlayListDetail
import com.ghhccghk.musicplay.data.user.playListDetail.songlist.SongListBase
import com.ghhccghk.musicplay.databinding.FragmentPlaylistBinding
import com.ghhccghk.musicplay.util.adapte.SongAdapter
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.ghhccghk.musicplay.util.others.toMediaItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import kotlin.text.replaceFirst
import androidx.core.net.toUri
import com.ghhccghk.musicplay.data.user.playListDetail.songlist.Song

class PlaylistDetailFragment : Fragment() {


    private var _binding: FragmentPlaylistBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 隐藏 BottomNavigationView
        val a = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        hideBottomNav(a)

        KugouAPi.init()
        if (MainActivity.isNodeRunning){
            val playlistId = arguments?.getString("playlistId",null)
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
                            val secureUrl = playList[0].create_user_pic.replaceFirst("http://", "https://")
                            Glide.with(requireContext())
                                .load(secureUrl)
                                .into(binding.ivPlaylistCover)

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
                    val json = withContext(Dispatchers.IO) {
                        getAllSongsMergedMoshi(playlistId,30)
                    }
                        try {
                            binding.rvSongs.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                            binding.rvSongs.adapter = SongAdapter(json){
                                lifecycleScope.launch {
                                    val json = withContext(Dispatchers.IO) {
                                        it.hash?.let { hash -> KugouAPi.getSongsUrl(hash) }
                                    }
                                    if (json == null || json == "502" || json == "404") {
                                        Toast.makeText(context, "数据加载失败", Toast.LENGTH_LONG).show()
                                    } else {
                                        try {
                                            val gson = Gson()
                                            val result = gson.fromJson(json, GetSongUrlBase::class.java)
                                            val url = result.backupUrl[0].toString()

                                            // 含真实 URL 和 ID 的占位 URI
                                            val encodedUrl = URLEncoder.encode(url, "UTF-8")
                                            val uri = "musicplay://playurl?id=${it?.name + it?.hash}&url=${encodedUrl}".toUri().toString()

                                            val name = it.name?.let { it1 -> splitArtistAndTitle(it1) }

                                            val item = it?.let { artist -> createMediaItemWithId(name?.first,
                                                name?.second,
                                                uri,
                                                result) }

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

    suspend fun createMediaItemWithId(artist: String?, title: String?, url: String, result: GetSongUrlBase): MediaItem {
        val mediaId = "$artist - $title"
        val urla = result.trans_param.union_cover.replaceFirst("http://", "https://")
            .replaceFirst("/{size}/", "/")

        val b = withContext(Dispatchers.IO) {
            KugouAPi.getSearchSongLyrics(hash = result.hash)
        }
        if (b == null || b == "502" || b == "404") {
            Toast.makeText(context, "数据加载失败", Toast.LENGTH_LONG).show()
        } else {
            val gson = Gson()
            val resulta = gson.fromJson(b, searchLyricBase::class.java)
            val accesskey = resulta.candidates[0].accesskey
            val id = resulta.candidates[0].id

            val abc = MediaItemEntity(
                mediaId = mediaId,
                uri = url.toString(),
                artists = artist,
                title = title,
                album = "",
                albumArtists = "",
                trackNumber = 0,
                discNumber = 0,
                thumb = urla,
                lrcId = id,
                songHash = result.hash,
                lrcAccesskey = accesskey
            )
            return abc.toMediaItem()
        }
        return MediaItem.Builder().setUri(url).build()
    }

    fun getAllSongsMergedMoshi(
        ids: String,
        pageSize: Int = 50
    ): List<Song>? {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(SongListBase::class.java)

        val allSongs = mutableListOf<Song>()
        var currentPage = 1

        while (true) {
            val json = KugouAPi.getPlayListAllSongs(ids, currentPage, pageSize) ?: break
            val songListBase = adapter.fromJson(json)
            val songs = songListBase?.data?.songs ?: emptyList()

            if (songs.isEmpty()) break

            allSongs.addAll(songs)

            if (songs.size < pageSize) break

            currentPage++
        }

        return allSongs
    }

    fun splitArtistAndTitle(text: String): Pair<String, String>? {
        val parts = text.split(" - ", limit = 2)
        return if (parts.size == 2) {
            parts[0].trim() to parts[1].trim() // 保证去掉首尾空格，保留中间空格
        } else {
            null // 无法拆分，可能格式不标准
        }
    }


}

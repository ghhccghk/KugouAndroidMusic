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
import androidx.media3.common.MediaMetadata
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.songurl.getsongurl.GetSongUrlBase
import com.ghhccghk.musicplay.data.user.playListDetail.PlayListDetail
import com.ghhccghk.musicplay.data.user.playListDetail.songlist.SongListBase
import com.ghhccghk.musicplay.databinding.FragmentPlaylistBinding
import com.ghhccghk.musicplay.util.adapte.SongAdapter
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.replaceFirst

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
                        KugouAPi.getPlayListAllSongs(playlistId)
                    }
                    if (json == null || json == "502" || json == "404") {
                        Toast.makeText(context, "数据加载失败", Toast.LENGTH_LONG).show()
                    } else {
                        try {
                            val moshi = Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .build()
                            val adapter = moshi.adapter(SongListBase::class.java)
                            val songList = adapter.fromJson(json)
                            val songlist = songList?.data?.songs

                            binding.rvSongs.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                            binding.rvSongs.adapter = SongAdapter(songlist){
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
                                            val url = result.url[0].toString().replaceFirst("http://", "https://")
                                            Log.d("debug",url)

                                            val item = it.singerinfo?.get(0)?.name?.let { artist -> createMediaItemWithId(artist,result.fileName,url,result) }

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

                        }catch (e: Exception) {
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

    fun createMediaItemWithId(artist: String, title: String, url: String, result: GetSongUrlBase): MediaItem {
        val mediaId = "$artist - $title"
        val urla = result.trans_param.union_cover.replaceFirst("http://", "https://")
            .replaceFirst("/{size}/", "/").toUri()
        return MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(MediaMetadata.Builder().setArtworkUri(urla).build())
            .setMediaId(mediaId)
            .build()
    }


}

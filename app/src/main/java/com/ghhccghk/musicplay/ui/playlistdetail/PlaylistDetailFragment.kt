package com.ghhccghk.musicplay.ui.playlistdetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.songurl.getsongurl.GetSongUrlBase
import com.ghhccghk.musicplay.data.user.playListDetail.PlayListDetail
import com.ghhccghk.musicplay.data.user.playListDetail.songlist.Song
import com.ghhccghk.musicplay.databinding.FragmentPlaylistBinding
import com.ghhccghk.musicplay.util.MediaHelp.createMediaItemWithId
import com.ghhccghk.musicplay.util.MediaHelp.getAllSongsFlow
import com.ghhccghk.musicplay.util.MediaHelp.splitArtistAndTitle
import com.ghhccghk.musicplay.util.MediaHelp.toMediaItemListParallel
import com.ghhccghk.musicplay.util.SmartImageCache
import com.ghhccghk.musicplay.util.adapte.SongAdapter
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class PlaylistDetailFragment : Fragment() {


    private var _binding: FragmentPlaylistBinding? = null
    private lateinit var player : MediaController

    private val binding get() = _binding!!

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player = MainActivity.controllerFuture.get()

        KugouAPi.init()
        if (MainActivity.isNodeRunning){
            val playlistId = arguments?.getString("playlistId",null)
            val picurl = arguments?.getString("picurl",null)
            val theme = arguments?.getBoolean("theme",false)
            if (playlistId != null && theme == false){
                viewLifecycleOwner.lifecycleScope.launch {
                    val json = withContext(Dispatchers.IO) {
                        KugouAPi.getPlayListDetail(playlistId)
                    }
                    if (json == null || json == "502" || json == "404") {
                        Toast.makeText(context, "数据加载失败", Toast.LENGTH_LONG).show()
                    } else {
                        try {
                            val gson = Gson()
                            val result = gson.fromJson(json, PlayListDetail::class.java)
                            val playList = result.data[0]
                            binding.tvPlaylistName.text = playList.name
                            binding.tvCreator.text = playList.list_create_username
                            binding.tvIntro.text = playList.intro
                            val userPlayListIcon = playList.create_user_pic

                            if (picurl != "null" || picurl != ""){
                                val urlcache = SmartImageCache.getOrDownload(picurl.toString(),picurl.hashCode().toString())
                                if (urlcache != null){
                                    Glide.with(requireContext())
                                        .load(urlcache)
                                        .into(binding.ivPlaylistCover)
                                } else {
                                    binding.ivPlaylistCover.setImageBitmap(binding.ivPlaylistCover.context.getDrawable(R.drawable.ic_favorite_filled)?.toBitmap())
                                }
                            } else {
                                val secureUrl = playList.create_user_pic.replaceFirst("/{size}/", "/")
                                val urlcache = SmartImageCache.getOrDownload(secureUrl,secureUrl.hashCode().toString())
                                Glide.with(requireContext())
                                    .load(urlcache)
                                    .into(binding.ivPlaylistCover)
                            }


                            if (userPlayListIcon != "null" || userPlayListIcon != ""){
                                val secureUrl = playList.create_user_pic.replaceFirst("/{size}/", "/")
                                Glide.with(requireContext())
                                    .load(secureUrl)
                                    .into(binding.userPlaylistIcon)
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

                viewLifecycleOwner.lifecycleScope.launch {
                    val json = mutableListOf<Song>()

                    try {
                        binding.playlistAllPlay.setOnClickListener {
                            viewLifecycleOwner.lifecycleScope.launch {
                                withContext(Dispatchers.Main) {
                                    player.setMediaItems(json.toMediaItemListParallel())
                                }
                            }
                        }
                        binding.rvSongs.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                        val adp = SongAdapter(json){
                            viewLifecycleOwner.lifecycleScope.launch {
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


                                        val mixsongid = it.mixsongid?: ""
                                        // 含真实 URL 和 ID 的占位 URI
                                        val encodedUrl = URLEncoder.encode(url, "UTF-8")
                                        val uri = "musicplay://playurl?id=${it?.name + it?.hash}&url=${encodedUrl}&hash=${it?.hash}".toUri().toString()

                                        val name = it.name?.let { it1 -> splitArtistAndTitle(it1) }

                                        val item = it?.let { _ -> createMediaItemWithId(name?.first,
                                            name?.second,
                                            uri,
                                            result,result.hash,mixsongid) }

                                        item?.let { mediaItem ->
                                            val controller = MainActivity.controllerFuture.get()
                                            val currentIndex = controller.currentMediaItemIndex
                                            controller.addMediaItem(currentIndex + 1, mediaItem) // 紧跟当前播放项后追加
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
            } else {
                Toast.makeText(context, "数据加载失败", Toast.LENGTH_LONG).show()
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


}

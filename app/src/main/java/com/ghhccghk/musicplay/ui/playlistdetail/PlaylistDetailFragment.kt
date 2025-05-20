package com.ghhccghk.musicplay.ui.playlistdetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.data.SongDataBase
import com.ghhccghk.musicplay.data.user.playListDetail.PlayListDetail
import com.ghhccghk.musicplay.databinding.FragmentLyricsBinding
import com.ghhccghk.musicplay.databinding.FragmentPlaylistBinding
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistDetailFragment : Fragment() {


    private var _binding: FragmentPlaylistBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        val root: View = binding.root

        KugouAPi.init()
        if (MainActivity.isNodeRunning){
            val playlistId = arguments?.getString("playlistId",null)
            if (playlistId != null){
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
            }
        }
        return root
    }

}

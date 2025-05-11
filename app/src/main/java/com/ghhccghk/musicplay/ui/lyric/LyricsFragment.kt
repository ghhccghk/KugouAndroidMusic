package com.ghhccghk.musicplay.ui.lyric

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.data.LyricLine
import com.ghhccghk.musicplay.data.ThemeMusicList
import com.ghhccghk.musicplay.data.getLyricCode
import com.ghhccghk.musicplay.databinding.FragmentLyricsBinding
import com.ghhccghk.musicplay.ui.widgets.YosLyricView
import com.ghhccghk.musicplay.util.LyricsAdapter
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.ghhccghk.musicplay.util.lrc.YosLrcFactory
import com.ghhccghk.musicplay.util.lrc.YosMediaEvent
import com.ghhccghk.musicplay.util.playlist.PlayMusicSceneAdapter
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LyricsFragment: Fragment() {

    private var _binding: FragmentLyricsBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLyricsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (MainActivity.isNodeRunning){
            testlyric()
        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

    }


    fun testlyric(){
        lifecycleScope.launch {

            val json = withContext(Dispatchers.IO) {

                KugouAPi.getSongLyrics(id = "329624309", accesskey = "80B48FA9CBE574CDBE5686BBCA4DBD58",
                    fmt = "lrc",decode = true)
            }

            try {
                val gson = Gson()
                val result = gson.fromJson(json, getLyricCode::class.java)
                val lyric = result.decodeContent
                val ok = YosLrcFactory().formatLrcEntries(lyric)

                binding.lyricsContainerComposeView.setContent {
                    YosLyricView(
                        lrcEntriesLambda = { ok },
                        liveTimeLambda = { 0 },
                        mediaEvent = object : YosMediaEvent {
                            override fun onSeek(position: Int) {
                                Toast.makeText(context, "点击: ${position}", Toast.LENGTH_LONG).show()
                            }
                        },
                        weightLambda = { false },
                        onBackClick = {
                        }
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "数据加载失败: ${e.message}", Toast.LENGTH_LONG).show()
            }



        }
    }


}
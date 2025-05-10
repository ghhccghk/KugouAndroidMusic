package com.ghhccghk.musicplay.ui.lyric

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ghhccghk.musicplay.data.LyricLine
import com.ghhccghk.musicplay.databinding.FragmentLyricsBinding
import com.ghhccghk.musicplay.util.LyricsAdapter

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
        // 示例歌词数据
        val lyrics = listOf(
            LyricLine(0, "第一句歌词"),
            LyricLine(5000, "第二句歌词"),
            LyricLine(10000, "第三句歌词"),
            LyricLine(15000, "第二句歌词"),
            LyricLine(20000, "第三句歌词"),
            LyricLine(25000, "第二句歌词"),
            LyricLine(50000, "第三句歌词"),
            LyricLine(90000, "第三句歌词")
        )

        val adapter = LyricsAdapter(lyrics)
        binding.rvLyrics.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLyrics.adapter = adapter
        adapter.highlightedPosition = 50000



        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()



    }


}
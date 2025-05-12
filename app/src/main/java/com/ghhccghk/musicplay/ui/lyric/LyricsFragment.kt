package com.ghhccghk.musicplay.ui.lyric

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.data.getLyricCode
import com.ghhccghk.musicplay.databinding.FragmentLyricsBinding
import com.ghhccghk.musicplay.ui.widgets.YosLyricView
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.ghhccghk.musicplay.util.lrc.YosLrcFactory
import com.ghhccghk.musicplay.util.lrc.YosMediaEvent
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

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
                    fmt = "krc",decode = true)
            }

            try {
                val gson = Gson()
                val result = gson.fromJson(json, getLyricCode::class.java)
                val lyric = result.decodeContent
                val out = convertKrcToLrc(lyric)
                val ok = YosLrcFactory().formatLrcEntries(out)

                binding.lyricsContainerComposeView.setContent {
                    YosLyricView(
                        lrcEntriesLambda = { ok },
                        liveTimeLambda = { 0 },
                        mediaEvent = object : YosMediaEvent {
                            override fun onSeek(position: Int) {
                                Toast.makeText(context, "点击: ${position}", Toast.LENGTH_LONG).show()
                            }
                        },
                        weightLambda = { true },
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

    fun convertKrcToLrc(krcContent: String): String {
        val timeFormat = DecimalFormat("00")
        val secFormat = DecimalFormat("00.00")

        val lineRegex = Regex("""\[(\d+),(\d+)](.*)""")
        val wordRegex = Regex("""<(\d+),(\d+),0>([^<])""")

        val output = mutableListOf<String>()

        val lines = krcContent.split("\n")

        for (line in lines) {
            val match = lineRegex.find(line) ?: continue
            val startTime = match.groupValues[1].toInt()
            val content = match.groupValues[3]

            val words = wordRegex.findAll(content).toList()
            if (words.isEmpty()) continue

            val sb = StringBuilder()

            // 对于每一行，单独处理字和时间戳
            var previousTime = startTime
            for ((index, word) in words.withIndex()) {
                val offset = word.groupValues[1].toInt()
                val char = word.groupValues[3]
                val wordTime = previousTime + offset
                val wordTimestamp = formatTime(wordTime, timeFormat, secFormat)
                sb.append("[$wordTimestamp]$char")

                // 更新为当前字的时间戳
                previousTime = wordTime

                // 如果是最后一个字，确保输出时间戳
                if (index == words.size - 1) {
                    val finalTimestamp = formatTime(previousTime, timeFormat, secFormat)
                    sb.append("[$finalTimestamp]") // 最后一个字后也输出时间戳
                }
            }

            output.add(sb.toString())
        }

        return output.joinToString("\n")
    }

    fun formatTime(ms: Int, minFmt: DecimalFormat, secFmt: DecimalFormat): String {
        val minutes = ms / 60000
        val seconds = (ms % 60000).toDouble() / 1000
        return "${minFmt.format(minutes)}:${secFmt.format(seconds)}"
    }

}
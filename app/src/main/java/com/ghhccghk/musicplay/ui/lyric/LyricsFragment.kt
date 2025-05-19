package com.ghhccghk.musicplay.ui.lyric

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.AssetDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.legacy.MediaSessionCompat
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.data.getLyricCode
import com.ghhccghk.musicplay.data.objects.MediaViewModelObject
import com.ghhccghk.musicplay.databinding.FragmentLyricsBinding
import com.ghhccghk.musicplay.ui.widgets.YosLyricView
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.ghhccghk.musicplay.util.lrc.YosLrcFactory
import com.ghhccghk.musicplay.util.lrc.YosMediaEvent
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.DecimalFormat
import kotlin.collections.joinToString

class LyricsFragment: Fragment() {

    private var _binding: FragmentLyricsBinding? = null

    private val binding get() = _binding!!

    val lrcEntries: MutableState<List<List<Pair<Float, String>>>> =
        MediaViewModelObject.lrcEntries

    @OptIn(UnstableApi::class)
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


    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @OptIn(UnstableApi::class)
    fun testlyric() {
        lifecycleScope.launch {

            val json = withContext(Dispatchers.IO) {
                KugouAPi.getSongLyrics(id = "164976364", accesskey = "DD1E9537A34B69F0BF69EEF3A1CED125",
                    fmt = "lrc",decode = true)
            }

            try {
                val gson = Gson()
                val play = MainActivity.controllerFuture
                val result = gson.fromJson(json, getLyricCode::class.java)
                val lyric = result.decodeContent
                val out = convertKrcToLrc(lyric)
                val fix = lrctimefix(lyric)
                //println(lyric)
                //println(out)
                val ok = YosLrcFactory(false).formatLrcEntries(fix)
                lrcEntries.value = ok
                binding.lyricsContainerComposeView.setContent {
                    YosLyricView(
                        lrcEntriesLambda = { lrcEntries.value },
                        liveTimeLambda = { (play.get()?.currentPosition?: 0).toInt() },
                        mediaEvent = object : YosMediaEvent {
                            override fun onSeek(position: Int) {
                                play.get()?.seekTo(position.toLong())
                            }
                        },
                        weightLambda = { false },
                        blurLambda = { false },
                        modifier = Modifier.drawWithCache {
                            onDrawWithContent {
                                val overlayPaint = Paint().apply {
                                    blendMode = BlendMode.Plus
                                }
                                val rect = Rect(0f, 0f, size.width, size.height)
                                val canvas = this.drawContext.canvas

                                canvas.saveLayer(rect, overlayPaint)

                                val colors = if (false) {
                                    listOf(
                                        Color.Transparent,
                                        Color(0x59000000),
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color(0x59000000),
                                        Color(0x21000000),
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Transparent
                                    )
                                } else {
                                    listOf(
                                        Color.Transparent,
                                        Color(0x59000000),
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black
                                    )
                                }

                                drawContent()

                                drawRect(
                                    brush = Brush.verticalGradient(colors),
                                    blendMode = BlendMode.DstIn
                                )

                                canvas.restore()
                            }
                        },
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

        val lineRegex = Regex("""\[(\d+),\d+]""")
        val wordRegex = Regex("""<(\d+),\d+,0>([^<]+)""") // 支持多字符内容

        val output = mutableListOf<String>()

        for (line in krcContent.lines()) {
            val lineStartMatch = lineRegex.find(line) ?: continue
            val lineStartTime = lineStartMatch.groupValues[1].toInt()

            val sb = StringBuilder()
            var currentTime = lineStartTime

            val matches = wordRegex.findAll(line).toList()

            for ((index, match) in matches.withIndex()) {
                val offset = match.groupValues[1].toInt()
                val char = match.groupValues[2]

                currentTime += offset
                val timestamp = formatTime(currentTime, timeFormat, secFormat)

                sb.append("[$timestamp]$char")
            }

            // 检查是否最后一个字符有时间戳
            if (matches.isNotEmpty()) {
                val lastTimestamp = formatTime(currentTime, timeFormat, secFormat)
                if (!sb.endsWith("[$lastTimestamp]")) {
                    sb.append("[$lastTimestamp]")
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

    fun lrctimefix(a: String): String {

        val timeRegex = "\\[(\\d{2}):(\\d{2})\\.(\\d{2})]".toRegex()

        // Map 时间戳文本 -> 它出现的行索引
        val timeMap = mutableMapOf<String, MutableList<Int>>()
        val inputLines = a.lines()

        inputLines.forEachIndexed { index, line ->
            val match = timeRegex.find(line)
            match?.value?.let { timeTag ->
                timeMap.computeIfAbsent(timeTag) { mutableListOf() }.add(index)
            }
        }

        val linesCopy = inputLines.toMutableList()

        for ((timeTag, indices) in timeMap) {
            if (indices.size >= 3) {
                for ((offset, i) in indices.withIndex()) {
                    val match = timeRegex.find(timeTag)
                    if (match != null) {
                        val minutes = match.groupValues[1].toInt()
                        val seconds = match.groupValues[2].toInt()
                        val hundredths = match.groupValues[3].toInt()

                        // 原时间戳 + offset * 10ms
                        var totalMillis = (minutes * 60 + seconds) * 1000 + hundredths * 10 + offset * 10

                        val newMinutes = totalMillis / 60000
                        val newSeconds = (totalMillis % 60000) / 1000
                        val newHundredths = (totalMillis % 1000) / 10

                        val newTimeTag = "[%02d:%02d.%02d]".format(newMinutes, newSeconds, newHundredths)
                        // 替换原行中的时间戳
                        linesCopy[i] = linesCopy[i].replace(timeRegex, newTimeTag)
                    }
                }
            }
        }

        // 输出结果
        return linesCopy.joinToString("\n")
    }

}
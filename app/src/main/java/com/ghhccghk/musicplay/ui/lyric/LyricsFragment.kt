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
import com.ghhccghk.musicplay.data.libraries.lrcAccesskey
import com.ghhccghk.musicplay.data.libraries.lrcId
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
        val play = MainActivity.controllerFuture
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
    }
}
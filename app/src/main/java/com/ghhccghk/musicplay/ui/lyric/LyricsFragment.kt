@file:Suppress("DEPRECATION")

package com.ghhccghk.musicplay.ui.lyric

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import  android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.objects.MediaViewModelObject
import com.ghhccghk.musicplay.databinding.FragmentLyricsBinding
import com.ghhccghk.musicplay.ui.widgets.YosLyricView
import com.ghhccghk.musicplay.util.lrc.YosMediaEvent
import com.ghhccghk.musicplay.util.lrc.YosUIConfig

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

        val window = requireActivity().window

        // 隐藏状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        if (MainActivity.isNodeRunning){
            testlyric()
        }

        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        testlyric()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @OptIn(UnstableApi::class)
    fun testlyric() {
        val play = MainActivity.controllerFuture
        val imageUrl = play.get().mediaMetadata.artworkUri

        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                val times = 5  // 模糊叠加3次
                val radius = 25f

                override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // 31+ 用 View 的 setRenderEffect 方式
                        val drawable = resource.toDrawable(resources)
                        val heavilyBlurredBitmap = blurMultipleTimes(MainActivity.lontext, drawable.toBitmap(), radius, times)
                        binding.backgroundImage.setImageBitmap(heavilyBlurredBitmap)
                        binding.backgroundImage.setRenderEffect(
                            RenderEffect.createBlurEffect(25f, 25f, Shader.TileMode.CLAMP)
                        )
                    } else {

                        // 手动模糊
                        val blurred = blurBitmapLegacy(MainActivity.lontext, resource, 25f)
                        val heavilyBlurredBitmap = blurMultipleTimes(MainActivity.lontext, blurred, radius, times)
                        binding.backgroundImage.setImageBitmap(heavilyBlurredBitmap)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })


        binding.lyricsContainerComposeView.setContent {
            YosLyricView(
                uiConfig = YosUIConfig( mainTextBasicColor = MainActivity.lontext.resources.getColor(R.color.lyric_main).toLong(),
                    subTextBasicColor = MainActivity.lontext.resources.getColor(R.color.lyric_sub).toLong()),
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

    override fun onResume() {
        super.onResume()
        val window = requireActivity().window
        if (MainActivity.isNodeRunning){
            testlyric()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    override fun onPause() {
        super.onPause()
        val window = requireActivity().window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.show(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    @Suppress("DEPRECATION")
    fun blurBitmapLegacy(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
        val inputBitmap = bitmap.scale(bitmap.width / 2, bitmap.height / 2, false)
        val outputBitmap = Bitmap.createBitmap(inputBitmap)

        val rs = RenderScript.create(context)
        val input = Allocation.createFromBitmap(rs, inputBitmap)
        val output = Allocation.createFromBitmap(rs, outputBitmap)

        val blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        blur.setRadius(radius.coerceIn(0f, 25f))
        blur.setInput(input)
        blur.forEach(output)

        output.copyTo(outputBitmap)
        rs.destroy()

        return outputBitmap
    }

    fun blurMultipleTimes(context: Context, bitmap: Bitmap, radius: Float, times: Int): Bitmap {
        var input = bitmap
        repeat(times) {
            input = blurBitmapLegacy(context, input, radius)
        }
        return input
    }



}
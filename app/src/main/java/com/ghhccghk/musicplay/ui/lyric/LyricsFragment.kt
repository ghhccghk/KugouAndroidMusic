@file:Suppress("DEPRECATION")

package com.ghhccghk.musicplay.ui.lyric

import android.animation.ValueAnimator
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.palette.graphics.Palette
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.objects.MediaViewModelObject
import com.ghhccghk.musicplay.data.objects.MediaViewModelObject.showControl
import com.ghhccghk.musicplay.databinding.FragmentLyricsBinding
import com.ghhccghk.musicplay.ui.player.PlayerFragment.Companion.BACKGROUND_COLOR_TRANSITION_SEC
import com.ghhccghk.musicplay.ui.player.PlayerFragment.Companion.FOREGROUND_COLOR_TRANSITION_SEC
import com.ghhccghk.musicplay.ui.widgets.YosLyricView
import com.ghhccghk.musicplay.util.lrc.YosMediaEvent
import com.ghhccghk.musicplay.util.lrc.YosUIConfig
import com.ghhccghk.musicplay.util.ui.ColorUtils
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.platform.MaterialSharedAxis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LyricsFragment: Fragment() {

    private var _binding: FragmentLyricsBinding? = null
    val play = MainActivity.controllerFuture.get()
    private val binding get() = _binding!!
    private lateinit var context: Context

    //动态取色相关
    private var currentJob: Job? = null
    private var wrappedContext: Context? = null
    private var fullPlayerFinalColor: Int = MediaViewModelObject.surfaceTransition.intValue
    private var colorPrimaryFinalColor: Int = Color.BLACK
    private var colorSecondaryContainerFinalColor: Int = Color.BLACK
    private var colorOnSecondaryContainerFinalColor: Int = Color.BLACK
    private var colorContrastFaintedFinalColor: Int = Color.BLACK
    private var colorOnSurfaceColor : Int = Color.BLACK
    private var colorOnSurfaceVariantColor : Int = Color.BLACK
    private val prefs = MainActivity.lontext.getSharedPreferences("play_setting_prefs", MODE_PRIVATE)
    private val colorbg = prefs.getBoolean("setting_color_background_set",false)
    private val setting_blur = prefs.getBoolean("setting_blur",false)
    private val translation = prefs.getBoolean("setting_translation",true)

    @OptIn(UnstableApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLyricsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        context = requireContext()

        val customTransition = TransitionSet().apply {
            addTransition(Slide(Gravity.END))
            addTransition(Fade(Fade.IN))
            duration = 600
            interpolator = FastOutSlowInInterpolator()
        }

        returnTransition = customTransition

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (MainActivity.isNodeRunning){
            testlyric()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @OptIn(UnstableApi::class)
    fun testlyric() {
        play.addListener(object : Player.Listener{
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)
                if (_binding != null){
                    updatebg()
                }
            }
        })
        updatebg()
        binding.lyricsContainerComposeView.setContent {
            showControl.value = false
            YosLyricView(
                uiConfig = YosUIConfig(),
                liveTimeLambda = { ( play.currentPosition?: 0).toInt() },
                mediaEvent = object : YosMediaEvent {
                    override fun onSeek(position: Int) {
                        play.seekTo(position.toLong())
                    }
                },
                weightLambda = { showControl.value },
                translationLambda = { translation },
                blurLambda = { setting_blur },
                onBackClick = {
                    showControl.value = true
                },
                mainTextBasicColor = {
                    androidx.compose.ui.graphics.Color(
                        MediaViewModelObject.colorOnSecondaryContainerFinalColor.value
                    )
                },
                subTextBasicColor = {
                    androidx.compose.ui.graphics.Color(
                        MediaViewModelObject.colorSecondaryContainerFinalColor.value
                    )
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        showControl.value = false
    }

    override fun onPause() {
        super.onPause()
        showControl.value = true
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

    // Generate palette synchronously and return it.
    fun createPaletteSync(bitmap: Bitmap): Palette = Palette.from(bitmap).generate()

    fun updatebg(){
        val imageUrl = play.mediaMetadata.artworkUri

        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                val times = 4  // 模糊叠加3次
                val radius = 25f
                override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                    // 31+ 用 View 的 setRenderEffect 方式
                    val drawable = resource.toDrawable(resources)
                    if (colorbg){
                        addColorScheme(drawable)
                    } else {
                        //选中字体颜色
                        MediaViewModelObject.colorOnSecondaryContainerFinalColor.intValue = ContextCompat.getColor(MainActivity.lontext,R.color.lyric_main_bg)
                        //未选中字体颜色
                        MediaViewModelObject.colorSecondaryContainerFinalColor.intValue = ContextCompat.getColor(MainActivity.lontext,R.color.lyric_sub_bg)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val heavilyBlurredBitmap = blurMultipleTimes(
                                MainActivity.lontext,
                                drawable.toBitmap(),
                                radius,
                                times
                            )
                            binding.backgroundImage.setImageBitmap(heavilyBlurredBitmap)
                            binding.backgroundImage.setColorFilter(
                                "#66000000".toColorInt(), // 半透明黑色，66 是透明度（十六进制）
                                PorterDuff.Mode.DARKEN // 或者使用 MULTIPLY 效果也不错
                            )
                            binding.backgroundImage.setRenderEffect(
                                RenderEffect.createBlurEffect(25f, 25f, Shader.TileMode.CLAMP)
                            )
                        } else {
                            // 手动模糊
                            val blurred = blurBitmapLegacy(MainActivity.lontext, resource, 25f)
                            val heavilyBlurredBitmap = blurMultipleTimes(MainActivity.lontext, blurred, radius, times)
                            binding.backgroundImage.setImageBitmap(heavilyBlurredBitmap)
                            binding.backgroundImage.setColorFilter(
                                "#66000000".toColorInt(), // 半透明黑色，66 是透明度（十六进制）
                                PorterDuff.Mode.DARKEN // 或者使用 MULTIPLY 效果也不错
                            )

                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })


    }

    private fun addColorScheme(a: Drawable) {
        currentJob?.cancel()
        currentJob = CoroutineScope(Dispatchers.Default).launch {
            var drawable = a
            if (drawable is TransitionDrawable) drawable = drawable.getDrawable(1)
            val bitmap = if (drawable is BitmapDrawable) drawable.bitmap else {
                removeColorScheme()
                return@launch
            }
            val colorAccuracy = prefs.getBoolean("color_accuracy", false)
            val targetWidth = if (colorAccuracy) (bitmap.width / 4).coerceAtMost(256) else 16
            val targetHeight = if (colorAccuracy) (bitmap.height / 4).coerceAtMost(256) else 16
            val scaledBitmap = bitmap.scale(targetWidth, targetHeight, false)

            val options = DynamicColorsOptions.Builder()
                .setContentBasedSource(scaledBitmap)
                .build() // <-- this is computationally expensive!

            wrappedContext = DynamicColors.wrapContextIfAvailable(
                context,
                options
            ).apply {
                // TODO does https://stackoverflow.com/a/58004553 describe this or another bug? will google ever fix anything?
                resources.configuration.uiMode = context.resources.configuration.uiMode
            }

            applyColorScheme()
        }
    }

    private suspend fun applyColorScheme() {
        val ctx = wrappedContext ?: context

        val colorSurface = MaterialColors.getColor(
            ctx,
            com.google.android.material.R.attr.colorSurface,
            -1
        )

        val colorOnSurface = MaterialColors.getColor(
            ctx,
            com.google.android.material.R.attr.colorOnSurface,
            -1
        )

        val colorOnSurfaceVariant = MaterialColors.getColor(
            ctx,
            com.google.android.material.R.attr.colorOnSurfaceVariant,
            -1
        )

        val colorPrimary =
            MaterialColors.getColor(
                ctx,
                com.google.android.material.R.attr.colorPrimary,
                -1
            )

        val colorSecondaryContainer =
            MaterialColors.getColor(
                ctx,
                com.google.android.material.R.attr.colorSecondaryContainer,
                -1
            )

        val colorOnSecondaryContainer =
            MaterialColors.getColor(
                ctx,
                com.google.android.material.R.attr.colorOnSecondaryContainer,
                -1
            )

        val backgroundProcessedColor = ColorUtils.getColor(
            colorSurface,
            ColorUtils.ColorType.COLOR_BACKGROUND_ELEVATED,
            ctx
        )

        val colorContrastFainted = ColorUtils.getColor(
            colorSecondaryContainer,
            ColorUtils.ColorType.COLOR_CONTRAST_FAINTED,
            ctx
        )

        val surfaceTransition = ValueAnimator.ofArgb(
            fullPlayerFinalColor,
            backgroundProcessedColor
        )

        val primaryTransition = ValueAnimator.ofArgb(
            colorPrimaryFinalColor,
            colorPrimary
        )

        val secondaryContainerTransition = ValueAnimator.ofArgb(
            colorSecondaryContainerFinalColor,
            colorSecondaryContainer
        )

        val onSecondaryContainerTransition = ValueAnimator.ofArgb(
            colorOnSecondaryContainerFinalColor,
            colorOnSecondaryContainer
        )

        val colorContrastFaintedTransition = ValueAnimator.ofArgb(
            colorContrastFaintedFinalColor,
            colorContrastFainted
        )

        surfaceTransition.apply {
            addUpdateListener { animation ->
                if (_binding != null) {
                    MediaViewModelObject.surfaceTransition.intValue = animation.animatedValue as Int
                    if (colorbg){
                        binding.backgroundImage.setBackgroundColor(
                            animation.animatedValue as Int
                        )
                    }
                }
            }
            duration = BACKGROUND_COLOR_TRANSITION_SEC
        }

        withContext(Dispatchers.Main) {
            if (_binding != null) {
                surfaceTransition.start()
                primaryTransition.start()
                secondaryContainerTransition.start()
                onSecondaryContainerTransition.start()
                colorContrastFaintedTransition.start()
            }
        }

        delay(FOREGROUND_COLOR_TRANSITION_SEC)
        fullPlayerFinalColor = backgroundProcessedColor
        colorPrimaryFinalColor = colorPrimary
        colorSecondaryContainerFinalColor = colorSecondaryContainer
        colorOnSecondaryContainerFinalColor = colorOnSecondaryContainer
        MediaViewModelObject.colorOnSecondaryContainerFinalColor.intValue = colorOnSecondaryContainer
        MediaViewModelObject.colorSecondaryContainerFinalColor.intValue = colorSecondaryContainer
        colorContrastFaintedFinalColor = colorContrastFainted
        colorOnSurfaceColor = colorOnSurface
        colorOnSurfaceVariantColor = colorOnSurfaceVariant

        currentJob = null

    }

    private fun removeColorScheme() {
        currentJob?.cancel()
        wrappedContext = null
        currentJob = CoroutineScope(Dispatchers.Default).launch {
            applyColorScheme()
        }
    }

}
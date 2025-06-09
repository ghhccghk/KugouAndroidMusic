@file:Suppress("DEPRECATION")

package com.ghhccghk.musicplay.ui.player

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.SeekBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.scale
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.objects.MediaViewModelObject
import com.ghhccghk.musicplay.data.objects.MediaViewModelObject.showControl
import com.ghhccghk.musicplay.databinding.FragmentPlayerBinding
import com.ghhccghk.musicplay.ui.components.GlobalPlaylistBottomSheetController
import com.ghhccghk.musicplay.ui.components.SquigglyProgress
import com.ghhccghk.musicplay.util.AudioFormatDetector
import com.ghhccghk.musicplay.util.AudioFormatDetector.AudioFormatInfo
import com.ghhccghk.musicplay.util.AudioFormatDetector.AudioQuality
import com.ghhccghk.musicplay.util.AudioFormatDetector.SpatialFormat
import com.ghhccghk.musicplay.util.Tools.dpToPx
import com.ghhccghk.musicplay.util.Tools.fadInAnimation
import com.ghhccghk.musicplay.util.Tools.fadOutAnimation
import com.ghhccghk.musicplay.util.Tools.formatMillis
import com.ghhccghk.musicplay.util.Tools.getAudioFormat
import com.ghhccghk.musicplay.util.Tools.playOrPause
import com.ghhccghk.musicplay.util.Tools.startAnimation
import com.ghhccghk.musicplay.util.ui.ColorUtils
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.color.MaterialColors
import com.google.android.material.slider.Slider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class PlayerFragment() : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    val handler = Handler(Looper.getMainLooper())
    private lateinit var progressDrawable: SquigglyProgress
    private lateinit var player : MediaController
    private var isUserTracking = false
    private lateinit var context: Context
    private var enableQualityInfo = true
    private val prefs = MainActivity.lontext.getSharedPreferences("play_setting_prefs", MODE_PRIVATE)


    //动态取色相关
    private var currentJob: Job? = null
    private var wrappedContext: Context? = null
    private var fullPlayerFinalColor: Int = -1
    private var colorPrimaryFinalColor: Int = -1
    private var colorSecondaryContainerFinalColor: Int = -1
    private var colorOnSecondaryContainerFinalColor: Int = -1
    private var colorContrastFaintedFinalColor: Int = -1

    companion object {
        const val BACKGROUND_COLOR_TRANSITION_SEC: Long = 300
        const val FOREGROUND_COLOR_TRANSITION_SEC: Long = 150
    }

    private val touchListener =
        object : SeekBar.OnSeekBarChangeListener, Slider.OnSliderTouchListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserTracking = true
                progressDrawable.animate = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val mediaId = player?.currentMediaItem
                if (mediaId != null) {
                    if (seekBar != null) {
                        player.seekTo((seekBar.progress.toLong()))
                    }
                }
                isUserTracking = false
                progressDrawable.animate =
                    player?.isPlaying == true || player?.playWhenReady == true
            }

            override fun onStartTrackingTouch(slider: Slider) {
                isUserTracking = true
            }

            override fun onStopTrackingTouch(slider: Slider) {
                val mediaId = player?.currentMediaItem
                if (mediaId != null) {
                    player.seekTo((slider.value.toLong()))
                }
                isUserTracking = false
            }
        }

    // 每 500ms 更新一次进度
    private val updateRunnable = object : Runnable {
        override fun run() {
            val seekBar = binding.sliderSquiggly
            val slider = binding.sliderVert

            slider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    player.seekTo(value.toLong())
                }
            }

            if (binding.fullSongName.text  != player.mediaMetadata.title){
                binding.fullSongName.text = player.mediaMetadata.title
                binding.fullSongArtist.text = player.mediaMetadata.artist
            }
            if (player.isPlaying) {
                seekBar.max = player.duration.toInt()
                slider.valueTo = player.duration.toFloat() // 设置最大值
                slider.value = player.currentPosition.toFloat()
                seekBar.progress = player.currentPosition.toInt()
                binding.position.text = formatMillis(player.currentPosition)
                binding.duration.text = formatMillis(player.duration)
            }
            handler.postDelayed(this, 130)
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    fun switchToNextFragment() {
        findNavController().navigate(R.id.lyricFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context = requireContext()
        //MainActivity.controllerFuture.addListener({  })
        player = MainActivity.controllerFuture.get()

        val seekBar = binding.sliderSquiggly
        val slider = binding.sliderVert
        val root: View = binding.root

        fullPlayerFinalColor = MaterialColors.getColor(
            root,
            com.google.android.material.R.attr.colorSurface
        )
        colorPrimaryFinalColor = MaterialColors.getColor(
            root,
            com.google.android.material.R.attr.colorPrimary
        )
        colorOnSecondaryContainerFinalColor = MaterialColors.getColor(
            root,
            com.google.android.material.R.attr.colorOnSecondaryContainer
        )
        colorSecondaryContainerFinalColor = MaterialColors.getColor(
            root,
            com.google.android.material.R.attr.colorSecondaryContainer
        )

        val seekBarProgressWavelength =
            context.resources
                .getDimensionPixelSize(R.dimen.media_seekbar_progress_wavelength)
                .toFloat()
        val seekBarProgressAmplitude =
            context.resources
                .getDimensionPixelSize(R.dimen.media_seekbar_progress_amplitude)
                .toFloat()
        val seekBarProgressPhase =
            context.resources
                .getDimensionPixelSize(R.dimen.media_seekbar_progress_phase)
                .toFloat()
        val seekBarProgressStrokeWidth =
            context.resources
                .getDimensionPixelSize(R.dimen.media_seekbar_progress_stroke_width)
                .toFloat()

        binding.slideDown.setOnClickListener {
            @Suppress("DEPRECATION")
            requireActivity().onBackPressed()
        }

        binding.lyrics.setOnClickListener {
            switchToNextFragment()
        }

        if (player.mediaMetadata.artworkUri != null ){
            val url = player.mediaMetadata.artworkUri
            Glide.with(binding.root)
                .load(player.mediaMetadata.artworkUri)
                .into(binding.fullSheetCover)
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val a = Glide.with(context)
                        .load(url)
                        .submit()
                        .get() // 注意：这是同步操作，需放在协程或后台线程中
                    addColorScheme(a)
                }
            }
        }

        val format = player.getAudioFormat()

        if(_binding != null){
            updateQualityIndicators(
                if (enableQualityInfo)
                    AudioFormatDetector.detectAudioFormat(format) else null
            )
        }


        player.addListener(
            object : Player.Listener {
                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    super.onMediaMetadataChanged(mediaMetadata)
                    val format = player.getAudioFormat()
                    val url = player.mediaMetadata.artworkUri
                    if (_binding != null ){
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                val a = Glide.with(context)
                                    .load(url)
                                    .submit()
                                    .get() // 注意：这是同步操作，需放在协程或后台线程中
                                addColorScheme(a)
                            }
                        }
                        updateQualityIndicators(if (enableQualityInfo)
                            AudioFormatDetector.detectAudioFormat(format) else null)
                        Glide.with(binding.root)
                            .load(url)
                            .into(binding.fullSheetCover)
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (_binding != null){
                        val url = player.mediaMetadata.artworkUri
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                val a = Glide.with(context)
                                    .load(url)
                                    .submit()
                                    .get() // 注意：这是同步操作，需放在协程或后台线程中
                                addColorScheme(a)
                            }
                        }

                        val format = player.getAudioFormat()
                        updateQualityIndicators(if (enableQualityInfo)
                            AudioFormatDetector.detectAudioFormat(format) else null)
                        if (player.isPlaying) {
                            progressDrawable.animate = true
                            Glide.with(binding.root)
                                .load(player.mediaMetadata.artworkUri)
                                .into(binding.fullSheetCover)
                            if (binding.sheetMidButton.tag != 1) {
                                binding.sheetMidButton.icon =
                                    AppCompatResources.getDrawable(
                                        context, R.drawable.play_anim
                                    )
                                binding.sheetMidButton.background =
                                    AppCompatResources.getDrawable(context, R.drawable.bg_play_anim)
                                binding.sheetMidButton.icon.startAnimation()
                                binding.sheetMidButton.background.startAnimation()
                                binding.sheetMidButton.tag = 1
                            }
                        } else {
                            val url = player.mediaMetadata.artworkUri
                            Glide.with(binding.root)
                                .load(player.mediaMetadata.artworkUri)
                                .into(binding.fullSheetCover)
                            lifecycleScope.launch {
                                withContext(Dispatchers.IO) {
                                    val a = Glide.with(context)
                                        .load(url)
                                        .submit()
                                        .get() // 注意：这是同步操作，需放在协程或后台线程中
                                    addColorScheme(a)
                                }
                            }
                            if (player.playbackState != Player.STATE_BUFFERING) {
                                if (binding.sheetMidButton.tag != 2) {
                                    binding.sheetMidButton.icon =
                                        AppCompatResources.getDrawable(
                                            context,
                                            R.drawable.pause_anim
                                        )
                                    binding.sheetMidButton.background =
                                        AppCompatResources.getDrawable(
                                            context,
                                            R.drawable.bg_pause_anim
                                        )
                                    binding.sheetMidButton.icon.startAnimation()
                                    binding.sheetMidButton.background.startAnimation()
                                    binding.sheetMidButton.tag = 2
                                }
                            }
                            progressDrawable.animate = false

                        }
                    }
                }
            }
        )

        binding.sheetMidButton.setOnClickListener {
            ViewCompat.performHapticFeedback(it, HapticFeedbackConstantsCompat.CONTEXT_CLICK)
            player.playOrPause()
        }

        binding.sliderSquiggly.progressDrawable = SquigglyProgress().also {
            progressDrawable = it
            it.waveLength = seekBarProgressWavelength
            it.lineAmplitude = seekBarProgressAmplitude
            it.phaseSpeed = seekBarProgressPhase
            it.strokeWidth = seekBarProgressStrokeWidth
            it.transitionEnabled = true
            it.animate = false
            it.setTint(
                MaterialColors.getColor(
                    binding.sliderSquiggly,
                    com.google.android.material.R.attr.colorPrimary,
                )
            )
        }

        binding.sliderSquiggly.setOnSeekBarChangeListener(touchListener)
        binding.sliderVert.addOnSliderTouchListener(touchListener)

        seekBar.max = 0
        slider.valueTo = 0f
        slider.value = 0f
        seekBar.progress = 0
        slider.visibility = View.GONE
        seekBar.visibility = View.VISIBLE

        if (player.duration.toInt() != 0){
            seekBar.max = player.duration.toInt()
            slider.valueTo = player.duration.toFloat() // 设置最大值
            slider.value = player.currentPosition.toFloat()
            seekBar.progress = player.currentPosition.toInt()
            binding.position.text = formatMillis(player.currentPosition)
            binding.duration.text = formatMillis(player.duration)
        }

        if (player.isPlaying) {
            progressDrawable.animate = true
            if (binding.sheetMidButton.tag != 1) {
                binding.sheetMidButton.icon =
                    AppCompatResources.getDrawable(
                        context, R.drawable.play_anim
                    )
                binding.sheetMidButton.background =
                    AppCompatResources.getDrawable(context, R.drawable.bg_play_anim)
                binding.sheetMidButton.icon.startAnimation()
                binding.sheetMidButton.background.startAnimation()
                binding.sheetMidButton.tag = 1
            }
        } else {
            progressDrawable.animate = false
            if (player.playbackState != Player.STATE_BUFFERING) {
                if (binding.sheetMidButton.tag != 2) {
                    binding.sheetMidButton.icon =
                        AppCompatResources.getDrawable(
                            context,
                            R.drawable.pause_anim
                        )
                    binding.sheetMidButton.background =
                        AppCompatResources.getDrawable(
                            context,
                            R.drawable.bg_pause_anim
                        )
                    binding.sheetMidButton.icon.startAnimation()
                    binding.sheetMidButton.background.startAnimation()
                    binding.sheetMidButton.tag = 2
                }
            }
        }

        binding.sheetNextSong.setOnClickListener {
            player.seekToNext()
        }
        binding.sheetPreviousSong.setOnClickListener {
            player.seekToPrevious()
        }

        binding.playlist.setOnClickListener {
            GlobalPlaylistBottomSheetController.show()
        }

        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val deltaX = e1?.let { it.x - e2.x }
                if (deltaX != null) {
                    if (deltaX > 100 && abs(velocityX) > 200) {
                        // 从右向左滑
                        switchToNextFragment()
                        return true
                    }
                }
                return false
            }
        })

        binding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }



    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        showControl.value = true
        handler.post(updateRunnable)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateRunnable)
        currentJob?.cancel()
    }


    override fun onResume() {
        super.onResume()
        showControl.value = true
        handler.post(updateRunnable)

    }

    override fun onPause() {
        super.onPause()
        currentJob?.cancel()
        handler.removeCallbacks(updateRunnable)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentJob?.cancel()
        _binding = null
        handler.removeCallbacksAndMessages(null)
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

        val selectorBackground =
            AppCompatResources.getColorStateList(
                ctx,
                R.color.sl_check_button
            )

        val selectorFavBackground =
            AppCompatResources.getColorStateList(
                ctx,
                R.color.sl_fav_button
            )

        val colorAccent =
            MaterialColors.getColor(
                ctx,
                com.google.android.material.R.attr.colorAccent,
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


        primaryTransition.apply {
            addUpdateListener { animation ->
                if (_binding != null) {
                    val progressColor = animation.animatedValue as Int
                    binding.sliderVert.thumbTintList =
                        ColorStateList.valueOf(progressColor)
                    binding.sliderVert.trackActiveTintList =
                        ColorStateList.valueOf(progressColor)
                    binding.sliderSquiggly.progressTintList =
                        ColorStateList.valueOf(progressColor)
                    binding.sliderSquiggly.thumbTintList =
                        ColorStateList.valueOf(progressColor)
                }
            }
            duration = BACKGROUND_COLOR_TRANSITION_SEC
        }

        secondaryContainerTransition.apply {
            addUpdateListener { animation ->
                if (_binding != null){
                    val progressColor = animation.animatedValue as Int
                    binding.sheetMidButton.backgroundTintList =
                        ColorStateList.valueOf(progressColor)
                }
            }
            duration = BACKGROUND_COLOR_TRANSITION_SEC
        }

        onSecondaryContainerTransition.apply {
            addUpdateListener { animation ->
                if (_binding != null) {
                    val progressColor = animation.animatedValue as Int
                    binding.sheetMidButton.iconTint =
                        ColorStateList.valueOf(progressColor)
                }
            }
            duration = BACKGROUND_COLOR_TRANSITION_SEC
        }

        colorContrastFaintedTransition.apply {
            addUpdateListener { animation ->
                if (_binding != null){
                    val progressColor = animation.animatedValue as Int
                    binding.sliderVert.trackInactiveTintList =
                        ColorStateList.valueOf(progressColor)
                }
            }
        }

        surfaceTransition.apply {
            addUpdateListener { animation ->
                if (_binding != null) {
                    MediaViewModelObject.surfaceTransition.intValue = animation.animatedValue as Int
                    binding.root.setBackgroundColor(
                        animation.animatedValue as Int
                    )
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
        colorContrastFaintedFinalColor = colorContrastFainted
        if (false){
            MediaViewModelObject.colorOnSecondaryContainerFinalColor.intValue =
                colorOnSecondaryContainer
            MediaViewModelObject.colorSecondaryContainerFinalColor.intValue =
                colorSecondaryContainer
        }
        currentJob = null
        withContext(Dispatchers.Main) {
            if (_binding != null) {
                binding.fullSongName.setTextColor(
                    colorOnSurface
                )
                binding.fullSongArtist.setTextColor(
                    colorOnSurfaceVariant
                )
                TextViewCompat.setCompoundDrawableTintList(
                    binding.qualityDetails,
                    ColorStateList.valueOf(colorOnSurfaceVariant)
                )
                binding.qualityDetails.setTextColor(
                    colorOnSurfaceVariant
                )
                binding.albumCoverFrame.setCardBackgroundColor(
                    colorSurface
                )

                binding.timer.iconTint =
                    ColorStateList.valueOf(colorOnSurface)
                binding.playlist.iconTint =
                    ColorStateList.valueOf(colorOnSurface)
                binding.sheetRandom.iconTint =
                    selectorBackground
                binding.sheetLoop.iconTint =
                    selectorBackground
                binding.lyrics.iconTint =
                    ColorStateList.valueOf(colorOnSurface)
                binding.favor.iconTint =
                    selectorFavBackground

                binding.sheetNextSong.iconTint =
                    ColorStateList.valueOf(colorOnSurface)
                binding.sheetPreviousSong.iconTint =
                    ColorStateList.valueOf(colorOnSurface)
                binding.slideDown.iconTint =
                    ColorStateList.valueOf(colorOnSurface)

                binding.position.setTextColor(
                    colorAccent
                )
                binding.duration.setTextColor(
                    colorAccent
                )
            }
        }

    }

    private fun removeColorScheme() {
        currentJob?.cancel()
        wrappedContext = null
        currentJob = CoroutineScope(Dispatchers.Default).launch {
            applyColorScheme()
        }
    }

    private fun updateQualityIndicators(info: AudioFormatInfo?) {
        if (_binding != null) {
            val oldInfo = (binding.qualityDetails.getTag(R.id.quality_details) as AudioFormatInfo?)
            if (oldInfo == info) return
            (binding.qualityDetails.getTag(R.id.fade_in_animation) as ViewPropertyAnimator?)?.cancel()
            (binding.qualityDetails.getTag(R.id.fade_out_animation) as ViewPropertyAnimator?)?.cancel()
            if (info == null && binding.qualityDetails.isInvisible) return
            if (oldInfo != null)
                applyQualityInfo(oldInfo)
            binding.qualityDetails.setTag(R.id.quality_details, info)
            binding.qualityDetails.fadOutAnimation(300) {
                if (info == null)
                    return@fadOutAnimation
                applyQualityInfo(info)
                binding.qualityDetails.fadInAnimation(300)
            }
        }
    }

    private fun applyQualityInfo(info: AudioFormatInfo) {
        val icon = when (info.spatialFormat) {
            SpatialFormat.SURROUND_5_0,
            SpatialFormat.SURROUND_5_1,
            SpatialFormat.SURROUND_6_1,
            SpatialFormat.SURROUND_7_1 -> R.drawable.ic_surround_sound

            SpatialFormat.DOLBY_AC3,
            SpatialFormat.DOLBY_AC4,
            SpatialFormat.DOLBY_EAC3,
            SpatialFormat.DOLBY_EAC3_JOC -> R.drawable.ic_dolby

            // TODO dts icon

            else -> when (info.quality) {
                AudioQuality.HIRES -> R.drawable.ic_high_res
                AudioQuality.HD -> R.drawable.ic_hd
                AudioQuality.CD -> R.drawable.ic_cd
                AudioQuality.LOSSY -> R.drawable.ic_lossy
                else -> null
            }
        }

        val drawable = icon?.let { iconRes ->
            AppCompatResources.getDrawable(context, iconRes)?.apply {
                setBounds(0, 0, 18.dpToPx(context), 18.dpToPx(context))
            }
        }
        binding.qualityDetails.setCompoundDrawablesRelative(drawable, null, null, null)

        binding.qualityDetails.text = buildString {
            var hadFirst = false
            info.bitDepth?.let {
                hadFirst = true
                append("${it}bit")
            }
            if (info.sampleRate != null) {
                if (hadFirst)
                    append(" / ")
                else
                    hadFirst = true
                append("${info.sampleRate / 1000f}kHz")
            }
            if (info.sourceChannels != null) {
                if (hadFirst)
                    append(" / ")
                else
                    hadFirst = true
                append("${info.sourceChannels}ch")
            }
            info.bitrate?.let {
                if (hadFirst)
                    append(" / ")
                else
                    hadFirst = true
                append("${it / 1000}kbps")
            }
        }
    }







}
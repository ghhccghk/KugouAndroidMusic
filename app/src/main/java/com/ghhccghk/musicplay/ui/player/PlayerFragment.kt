@file:Suppress("DEPRECATION")
package com.ghhccghk.musicplay.ui.player

import android.R.attr.colorAccent
import android.animation.ValueAnimator
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.TooltipCompat
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
import com.ghhccghk.musicplay.data.libraries.songHash
import com.ghhccghk.musicplay.data.libraries.songtitle
import com.ghhccghk.musicplay.data.objects.MediaViewModelObject
import com.ghhccghk.musicplay.data.objects.MediaViewModelObject.showControl
import com.ghhccghk.musicplay.databinding.FragmentPlayerBinding
import com.ghhccghk.musicplay.ui.components.GlobalPlaylistBottomSheetController
import com.ghhccghk.musicplay.ui.components.SquigglyProgress
import com.ghhccghk.musicplay.util.AudioFormatDetector
import com.ghhccghk.musicplay.util.AudioFormatDetector.AudioFormatInfo
import com.ghhccghk.musicplay.util.AudioFormatDetector.AudioQuality
import com.ghhccghk.musicplay.util.AudioFormatDetector.SpatialFormat
import com.ghhccghk.musicplay.util.SmartImageCache
import com.ghhccghk.musicplay.util.Tools.dpToPx
import com.ghhccghk.musicplay.util.Tools.fadInAnimation
import com.ghhccghk.musicplay.util.Tools.fadOutAnimation
import com.ghhccghk.musicplay.util.Tools.formatMillis
import com.ghhccghk.musicplay.util.Tools.getAudioFormat
import com.ghhccghk.musicplay.util.Tools.playOrPause
import com.ghhccghk.musicplay.util.Tools.startAnimation
import com.ghhccghk.musicplay.util.getTimer
import com.ghhccghk.musicplay.util.oem.SystemMediaControlResolver
import com.ghhccghk.musicplay.util.oem.UnstableMediaKitApi
import com.ghhccghk.musicplay.util.setTimer
import com.ghhccghk.musicplay.util.ui.ColorUtils
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class PlayerFragment : Fragment(), Player.Listener{

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var progressDrawable: SquigglyProgress
    private lateinit var player: MediaController
    private var isUserTracking = false
    private lateinit var context: Context
    private val prefs by lazy { MainActivity.lontext.getSharedPreferences("play_setting_prefs", MODE_PRIVATE) }
    private var enableQualityInfo = prefs.getBoolean("audio_quality_info", false)
    private var defaultProgressBar = prefs.getBoolean("default_progress_bar", false)
    private var currentFormat: AudioFormatDetector.AudioFormats? = null

    // 动态色彩
    private var currentJob: Job? = null
    private var wrappedContext: Context? = null
    private var fullPlayerFinalColor = -1
    private var colorPrimaryFinalColor = -1
    private var colorSecondaryContainerFinalColor = -1
    private var colorOnSecondaryContainerFinalColor = -1
    private var colorContrastFaintedFinalColor = -1

    companion object {
        const val BACKGROUND_COLOR_TRANSITION_SEC: Long = 300
        const val FOREGROUND_COLOR_TRANSITION_SEC: Long = 150
    }

    private val touchListener =
        object : SeekBar.OnSeekBarChangeListener, Slider.OnSliderTouchListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserTracking = true
                progressDrawable.animate = false
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                player.currentMediaItem?.let { player.seekTo(seekBar?.progress?.toLong() ?: 0L) }
                isUserTracking = false
                progressDrawable.animate = player.isPlaying
            }

            override fun onStartTrackingTouch(slider: Slider) { isUserTracking = true }
            override fun onStopTrackingTouch(slider: Slider) {
                player.currentMediaItem?.let { player.seekTo(slider.value.toLong()) }
                isUserTracking = false
            }
        }

    // 更新进度条
    private val updateRunnable = object : Runnable {
        override fun run() {
            val seekBar = binding.sliderSquiggly
            val slider = binding.sliderVert

            if (binding.fullSongName.text != player.currentMediaItem?.songtitle) {
                binding.fullSongName.text = player.currentMediaItem?.songtitle
                binding.fullSongArtist.text = player.mediaMetadata.artist
            }

            if (player.isPlaying) {
                seekBar.max = player.duration.toInt()
                slider.valueTo = player.duration.toFloat()
                slider.value = player.currentPosition.toFloat()
                seekBar.progress = player.currentPosition.toInt()
                binding.position.text = formatMillis(player.currentPosition)
                binding.duration.text = formatMillis(player.duration)
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

            handler.postDelayed(this, 130)
        }
    }

    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun switchToNextFragment() {
        findNavController().navigate(R.id.lyricFragment)
    }

    @OptIn(UnstableMediaKitApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context = requireContext()
        player = MainActivity.controllerFuture.get()
        player.addListener(this)



        val root = binding.root
        fullPlayerFinalColor = MaterialColors.getColor(root, com.google.android.material.R.attr.colorSurface)
        colorPrimaryFinalColor = MaterialColors.getColor(root, androidx.appcompat.R.attr.colorPrimary)
        colorOnSecondaryContainerFinalColor = MaterialColors.getColor(root, com.google.android.material.R.attr.colorOnSecondaryContainer)
        colorSecondaryContainerFinalColor = MaterialColors.getColor(root, com.google.android.material.R.attr.colorSecondaryContainer)

        // 触控手势
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val deltaX = e1?.let { it.x - e2.x } ?: return false
                if (deltaX > 100 && abs(velocityX) > 200) { switchToNextFragment(); return true }
                return false
            }
        })
        binding.root.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event); true }

        // 控件点击事件
        binding.slideDown.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.lyrics.setOnClickListener { switchToNextFragment() }
        binding.sheetMidButton.setOnClickListener { ViewCompat.performHapticFeedback(it, HapticFeedbackConstantsCompat.CONTEXT_CLICK); player.playOrPause() }
        binding.sheetNextSong.setOnClickListener { player.seekToNext() }
        binding.sheetPreviousSong.setOnClickListener { player.seekToPrevious() }
        binding.playlist.setOnClickListener { GlobalPlaylistBottomSheetController.show() }
        binding.mediaControl.setOnClickListener { SystemMediaControlResolver(context).intentSystemMediaDialog() }

        // 进度条初始化
        val seekBarProgressWavelength = context.resources.getDimensionPixelSize(R.dimen.media_seekbar_progress_wavelength).toFloat()
        val seekBarProgressAmplitude = context.resources.getDimensionPixelSize(R.dimen.media_seekbar_progress_amplitude).toFloat()
        val seekBarProgressPhase = context.resources.getDimensionPixelSize(R.dimen.media_seekbar_progress_phase).toFloat()
        val seekBarProgressStrokeWidth = context.resources.getDimensionPixelSize(R.dimen.media_seekbar_progress_stroke_width).toFloat()
        binding.sliderSquiggly.progressDrawable = SquigglyProgress().also {
            progressDrawable = it
            it.waveLength = seekBarProgressWavelength
            it.lineAmplitude = seekBarProgressAmplitude
            it.phaseSpeed = seekBarProgressPhase
            it.strokeWidth = seekBarProgressStrokeWidth
            it.transitionEnabled = true
            it.animate = player.isPlaying
            it.setTint(MaterialColors.getColor(binding.sliderSquiggly, androidx.appcompat.R.attr.colorPrimary))
        }

        binding.sliderSquiggly.setOnSeekBarChangeListener(touchListener)
        binding.sliderVert.addOnSliderTouchListener(touchListener)
        if (defaultProgressBar) { binding.sliderVert.visibility = View.VISIBLE; binding.sliderSquiggly.visibility = View.GONE }
        else { binding.sliderVert.visibility = View.GONE; binding.sliderSquiggly.visibility = View.VISIBLE }

        // 加载封面并应用动态色彩
        player.mediaMetadata.artworkUri?.let { uri ->
            val hash = player.currentMediaItem?.songHash
            viewLifecycleOwner.lifecycleScope.launch {
                val drawable = withContext(Dispatchers.IO) {
                    val fileUrl = SmartImageCache.getOrDownload(uri.toString(), hash)
                    Glide.with(context).load(fileUrl).submit().get()
                }
                binding.fullSheetCover.setImageDrawable(drawable)
                if (DynamicColors.isDynamicColorAvailable() && prefs.getBoolean("content_based_color", true)) addColorScheme(drawable) else removeColorScheme()
            }
        }

        currentFormat = player.getAudioFormat()
        updateQualityIndicators(if (enableQualityInfo) AudioFormatDetector.detectAudioFormat(currentFormat) else null)

        // Player 监听
        player.addListener(object : Player.Listener {
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                val format = player.getAudioFormat()
                val url = mediaMetadata.artworkUri
                _binding?.let {
                    viewLifecycleOwner.lifecycleScope.launch {
                        url?.let { artwork ->
                            val drawable = withContext(Dispatchers.IO) { Glide.with(context).load(artwork).submit().get() }
                            binding.fullSheetCover.setImageDrawable(drawable)
                            if (DynamicColors.isDynamicColorAvailable() && prefs.getBoolean("content_based_color", true)) addColorScheme(drawable) else removeColorScheme()
                        }
                        currentFormat = format
                        updateQualityIndicators(if (enableQualityInfo) AudioFormatDetector.detectAudioFormat(currentFormat) else null)
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _binding?.let { b ->
                    progressDrawable.animate = isPlaying
                    val url = player.mediaMetadata.artworkUri
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        url?.let {
                            val drawable = Glide.with(context).load(it).submit().get()
                            withContext(Dispatchers.Main) {
                                b.fullSheetCover.setImageDrawable(drawable)
                                if (DynamicColors.isDynamicColorAvailable() && prefs.getBoolean("content_based_color", true)) addColorScheme(drawable) else removeColorScheme()
                            }
                        }
                    }
                }
            }
        })

            binding.timer.setOnClickListener {
                updateTimer()
                // TODO(ASAP): expose wait until song end in ui
                ViewCompat.performHapticFeedback(it, HapticFeedbackConstantsCompat.CONTEXT_CLICK)
                val picker =
                    MaterialTimePicker
                        .Builder()
                        .setHour((player?.getTimer()?.first ?: 0) / 3600 / 1000)
                        .setMinute(((player?.getTimer()?.first ?: 0) % (3600 * 1000)) / (60 * 1000))
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                        .build()
                picker.addOnPositiveButtonClickListener {
                    val destinationTime: Int = picker.hour * 1000 * 3600 + picker.minute * 1000 * 60
                    player?.setTimer(destinationTime, false)
                }
                picker.show(this.parentFragmentManager, "timer")
            }

        binding.qualityDetails.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(R.string.audio_signal_chain)
                .setMessage(
                    currentFormat?.prettyToString(context)
                        ?: context.getString(R.string.audio_not_initialized)
                )
                .setPositiveButton(android.R.string.ok, null)
                .create()

            dialog.setOnShowListener {
                val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positive.setTextColor(colorOnSecondaryContainerFinalColor)
            }
            dialog.show()

            // 创建一个圆角背景 Drawable
            val radius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics
            )
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = radius
                setColor(MediaViewModelObject.surfaceTransition.intValue)
            }

            // 设置对话框背景
            dialog.window?.setBackgroundDrawable(drawable)
            dialog.window?.setLayout(
                (context.resources.displayMetrics.widthPixels * 0.8).toInt(),  // 宽度 = 屏幕 80%
                (context.resources.displayMetrics.heightPixels * 0.6).toInt()   // 宽度 = 屏幕 50%
            )

        }

        binding.sheetLoop.setOnClickListener {
            ViewCompat.performHapticFeedback(it, HapticFeedbackConstantsCompat.CONTEXT_CLICK)
            player?.repeatMode = when (player?.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
                else -> throw IllegalStateException()
            }
        }

        binding.sheetRandom.addOnCheckedChangeListener { _, isChecked ->
            player?.shuffleModeEnabled = isChecked
        }

        binding.sheetRandom.setOnClickListener {
            ViewCompat.performHapticFeedback(it, HapticFeedbackConstantsCompat.CONTEXT_CLICK)
        }

        when (player.repeatMode) {
            Player.REPEAT_MODE_ALL -> {
                binding.sheetLoop.isChecked = true
                binding.sheetLoop.icon =
                    AppCompatResources.getDrawable(context, R.drawable.ic_repeat)
            }

            Player.REPEAT_MODE_ONE -> {
                binding.sheetLoop.isChecked = true
                binding.sheetLoop.icon =
                    AppCompatResources.getDrawable(context, R.drawable.ic_repeat_one)
            }

            Player.REPEAT_MODE_OFF -> {
                binding.sheetLoop.isChecked = false
                binding.sheetLoop.icon =
                    AppCompatResources.getDrawable(context, R.drawable.ic_repeat)
            }
        }

        binding.sheetRandom.isChecked = player.shuffleModeEnabled
    }

    override fun onStart() { super.onStart(); showControl.value = true; handler.post(updateRunnable) ;player.addListener(this)}
    override fun onResume() { super.onResume(); showControl.value = true; handler.post(updateRunnable);player.addListener(this) }
    override fun onPause() { super.onPause(); currentJob?.cancel(); handler.removeCallbacks(updateRunnable) ; player.removeListener(this)}
    override fun onStop() { super.onStop(); handler.removeCallbacks(updateRunnable); currentJob?.cancel() ; player.removeListener(this)}
    override fun onDestroyView() { super.onDestroyView(); currentJob?.cancel(); _binding = null; handler.removeCallbacksAndMessages(null) ; player.removeListener(this)}

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        binding.sheetRandom.isChecked = shuffleModeEnabled
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        when (repeatMode) {
            Player.REPEAT_MODE_ALL -> {
                binding.sheetLoop.isChecked = true
                binding.sheetLoop.icon =
                    AppCompatResources.getDrawable(context, R.drawable.ic_repeat)
            }

            Player.REPEAT_MODE_ONE -> {
                binding.sheetLoop.isChecked = true
                binding.sheetLoop.icon =
                    AppCompatResources.getDrawable(context, R.drawable.ic_repeat_one)
            }

            Player.REPEAT_MODE_OFF -> {
                binding.sheetLoop.isChecked = false
                binding.sheetLoop.icon =
                    AppCompatResources.getDrawable(context, R.drawable.ic_repeat)
            }
        }
    }


    private fun addColorScheme(drawable: Drawable) {
        currentJob?.cancel()
        currentJob = viewLifecycleOwner.lifecycleScope.launch {
            var bmpDrawable = drawable
            if (bmpDrawable is TransitionDrawable) bmpDrawable = bmpDrawable.getDrawable(1)
            val bitmap = (bmpDrawable as? BitmapDrawable)?.bitmap ?: return@launch removeColorScheme()
            val colorAccuracy = prefs.getBoolean("content_based_color", false)
            val scaledBitmap = bitmap.scale(
                if (colorAccuracy) (bitmap.width / 4).coerceAtMost(256) else 16,
                if (colorAccuracy) (bitmap.height / 4).coerceAtMost(256) else 16,
                false
            )
            val options = DynamicColorsOptions.Builder().setContentBasedSource(scaledBitmap).build()
            wrappedContext = DynamicColors.wrapContextIfAvailable(context, options)
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
                androidx.appcompat.R.attr.colorPrimary,
                -1
            )

        val colorSecondary =
            MaterialColors.getColor(
                ctx,
                com.google.android.material.R.attr.colorSecondary,
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
                colorAccent,
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
        _binding?.let { b ->
            val oldInfo = b.qualityDetails.getTag(R.id.quality_details) as AudioFormatInfo?
            if (oldInfo == info) return
            (b.qualityDetails.getTag(R.id.fade_in_animation) as ViewPropertyAnimator?)?.cancel()
            (b.qualityDetails.getTag(R.id.fade_out_animation) as ViewPropertyAnimator?)?.cancel()
            if (info == null && b.qualityDetails.isInvisible) return
            if (oldInfo != null) applyQualityInfo(oldInfo)
            b.qualityDetails.setTag(R.id.quality_details, info)
            b.qualityDetails.fadOutAnimation(300) {
                info?.let { applyQualityInfo(it); b.qualityDetails.fadInAnimation(300) }
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
                AudioQuality.HQ -> R.drawable.ic_hq
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
                append("${it / 1000}kbps")
            }
        }
    }

    private fun updateTimer() {
        val t = player?.getTimer()
        binding.timer.isChecked = t?.first != null || t?.second == true
        TooltipCompat.setTooltipText(
            binding.timer,
            if (t?.first != null) context.getString(
                if (t.second) R.string.timer_expiry_eos else R.string.timer_expiry,
                DateFormat.getTimeFormat(context).format(System.currentTimeMillis() + t.first!!)
            ) else if (t?.second == true) context.getString(R.string.timer_expiry_end_of_this_song)
            else context.getString(R.string.timer)
        )
    }

}

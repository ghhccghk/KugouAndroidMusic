package com.ghhccghk.musicplay.ui.player

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.navigation.fragment.findNavController
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
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
import com.ghhccghk.musicplay.util.ui.CalculationUtils
import com.google.android.material.color.MaterialColors
import com.google.android.material.slider.Slider

class PlayerFragment() : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    val handler = Handler(Looper.getMainLooper())
    private lateinit var progressDrawable: SquigglyProgress
    private lateinit var player : MediaController
    private var isUserTracking = false
    private lateinit var context: Context
    private var enableQualityInfo = true

    private var currentFormat: AudioFormatDetector.AudioFormats? = null

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        context = requireContext()
        player = MainActivity.controllerFuture.get()
        val seekBar = binding.sliderSquiggly
        val slider = binding.sliderVert
        val root: View = binding.root
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
            findNavController().navigate(R.id.lyricFragment)

        }

        if (player.mediaMetadata.artworkUri != null ){
            Glide.with(binding.root)
                .load(player.mediaMetadata.artworkUri)
                .into(binding.fullSheetCover)
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
                    if (_binding != null ){
                        updateQualityIndicators(if (enableQualityInfo)
                            AudioFormatDetector.detectAudioFormat(format) else null)
                        Glide.with(binding.root)
                            .load(player.mediaMetadata.artworkUri)
                            .into(binding.fullSheetCover)
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (_binding != null){
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
                            Glide.with(binding.root)
                                .load(player.mediaMetadata.artworkUri)
                                .into(binding.fullSheetCover)
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

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        handler.post(updateRunnable)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateRunnable)
    }


    override fun onResume() {
        super.onResume()
        handler.post(updateRunnable)

    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacksAndMessages(null)
    }

    private fun updateQualityIndicators(info: AudioFormatInfo?) {
        Log.d("PlayerFragment", "updateQualityIndicators: $info")
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
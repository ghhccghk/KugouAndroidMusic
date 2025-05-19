package com.ghhccghk.musicplay.ui.player

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.Player
import androidx.navigation.fragment.findNavController
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.databinding.FragmentPlayerBinding
import com.ghhccghk.musicplay.ui.components.SquigglyProgress
import com.ghhccghk.musicplay.util.ui.CalculationUtils
import com.google.android.material.color.MaterialColors
import com.google.android.material.slider.Slider

class PlayerFragment() : Fragment() {

    private var _binding: FragmentPlayerBinding? = null

    private val binding get() = _binding!!
    val handler = Handler(Looper.getMainLooper())
    private lateinit var progressDrawable: SquigglyProgress
    val player = MainActivity.controllerFuture.get()
    private var isUserTracking = false
    private lateinit var context: Context

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
                playbottomam()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                val mediaId = player?.currentMediaItem
                if (mediaId != null) {
                    player.seekTo((slider.value.toLong()))
                }
                playbottomam()
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
                } else {
                    playbottomam()
                }
            }

            binding.fullSongName.text = MainActivity.controllerFuture.get().mediaMetadata.title
            binding.fullSongArtist.text = MainActivity.controllerFuture.get().mediaMetadata.artist

            if (player.isPlaying) {
                progressDrawable.animate = true
                seekBar.max = player.duration.toInt()
                slider.valueTo = player.duration.toFloat() // 设置最大值
                slider.visibility = View.GONE
                seekBar.visibility = View.VISIBLE
                slider.value = player.currentPosition.toFloat()
                seekBar.progress = player.currentPosition.toInt()
                binding.position.text = formatMillis(player.currentPosition)
                binding.duration.text = formatMillis(player.duration)
            } else {
                progressDrawable.animate = false
                playbottomam()

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
        val root: View = binding.root
        playbottomam()
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
            requireActivity().onBackPressed()
        }

        binding.lyrics.setOnClickListener {
            findNavController().navigate(R.id.lyricFragment)

        }

        binding.sheetMidButton.setOnClickListener {
            ViewCompat.performHapticFeedback(it, HapticFeedbackConstantsCompat.CONTEXT_CLICK)
            player.playOrPause()
            playbottomam()
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


        return root
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



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacksAndMessages(null)
    }

    fun playbottomam(){
        if (player.isPlaying){
            if (binding.sheetMidButton.tag != 1) {
                binding.sheetMidButton.icon =
                    AppCompatResources.getDrawable(context, R.drawable.play_anim
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
                        AppCompatResources.getDrawable(context,
                            R.drawable.pause_anim
                        )
                    binding.sheetMidButton.background =
                        AppCompatResources.getDrawable(context, R.drawable.bg_pause_anim)
                    binding.sheetMidButton.icon.startAnimation()
                    binding.sheetMidButton.background.startAnimation()
                    binding.sheetMidButton.tag = 2
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatMillis(millis: Long): String {
        val minutes = millis / 1000 / 60
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun Player.playOrPause() {
        if (playWhenReady) {
            pause()
        } else {
            play()
        }
    }

    fun Drawable.startAnimation() {
        when (this) {
            is AnimatedVectorDrawable -> start()
            is AnimatedVectorDrawableCompat -> start()
            else -> throw IllegalArgumentException()
        }
    }



}
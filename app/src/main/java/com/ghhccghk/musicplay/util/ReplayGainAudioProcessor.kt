package com.ghhccghk.musicplay.util

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.Log
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.audio.ToFloatPcmAudioProcessor
import org.nift4.gramophone.hificore.AdaptiveDynamicRangeCompression
import java.nio.ByteBuffer

class ReplayGainAudioProcessor : BaseAudioProcessor() {
    companion object {
        private const val TAG = "ReplayGainAP"
    }

    private var compressor: AdaptiveDynamicRangeCompression? = null
    var mode = ReplayGainUtil.Mode.None
        private set
    var rgGain = 0 // dB
        private set
    var nonRgGain = 0 // dB
        private set
    var boostGain = 0 // dB
        private set
    var offloadEnabled = false
        private set
    var reduceGain = false
        private set
    var settingsChangedListener: (() -> Unit)? = null
    var boostGainChangedListener: (() -> Unit)? = null
    var offloadEnabledChangedListener: (() -> Unit)? = null
    private val toFloatPcmAudioProcessor = ToFloatPcmAudioProcessor()
    private var gain = 1f
    private var kneeThresholdDb: Float? = null
    private var outputFloat: Boolean? = null
    private var pendingOutputFloat: Boolean? = null
    private var tags: ReplayGainUtil.ReplayGainInfo? = null
    override fun queueInput(inputBuffer: ByteBuffer) {
        val frameCount = inputBuffer.remaining() / inputAudioFormat.bytesPerFrame
        val outputBuffer = replaceOutputBuffer(frameCount * outputAudioFormat.bytesPerFrame)
        if (inputBuffer.hasRemaining()) {
            if (compressor != null) {
                var inputForCompressor = inputBuffer
                if (toFloatPcmAudioProcessor.isActive) {
                    toFloatPcmAudioProcessor.queueInput(inputBuffer)
                    inputForCompressor = toFloatPcmAudioProcessor.output
                }
                compressor!!.compress(
                    inputAudioFormat.channelCount,
                    gain,
                    kneeThresholdDb!!, 1f, inputForCompressor,
                    outputBuffer, frameCount
                )
                inputForCompressor.position(inputForCompressor.limit())
                outputBuffer.position(frameCount * outputAudioFormat.bytesPerFrame)
            } else {
                if (gain == 1f) {
                    outputBuffer.put(inputBuffer)
                } else {
                    while (inputBuffer.hasRemaining()) {
                        when (inputAudioFormat.encoding) {
                            C.ENCODING_PCM_8BIT -> outputBuffer.put(
                                (inputBuffer.get() * gain).toInt().toByte()
                            )

                            C.ENCODING_PCM_16BIT, C.ENCODING_PCM_16BIT_BIG_ENDIAN ->
                                outputBuffer.putShort(
                                    (inputBuffer.getShort() * gain).toInt().toShort()
                                )

                            C.ENCODING_PCM_24BIT, C.ENCODING_PCM_24BIT_BIG_ENDIAN -> {
                                Util.putInt24(
                                    outputBuffer, (Util.getInt24(
                                        inputBuffer,
                                        inputBuffer.position()
                                    ) * gain).toInt()
                                        .shl(8).shr(8)
                                )
                                inputBuffer.position(inputBuffer.position() + 3)
                            }

                            C.ENCODING_PCM_32BIT, C.ENCODING_PCM_32BIT_BIG_ENDIAN ->
                                outputBuffer.putInt((inputBuffer.getInt() * gain).toInt())

                            C.ENCODING_PCM_FLOAT -> outputBuffer.putFloat(
                                inputBuffer.getFloat() * gain
                            )

                            C.ENCODING_PCM_DOUBLE -> outputBuffer.putDouble(
                                inputBuffer.getDouble() * gain
                            )

                            else -> throw IllegalStateException("unreachable, bad encoding")
                        }
                    }
                }
            }
        }
        outputBuffer.flip()
    }

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (Util.getBitDepth(inputAudioFormat.encoding) % 8 != 0)
            throw IllegalStateException("unsupported pcm encoding ${inputAudioFormat.encoding}")
        if (Flags.TEST_RG_OFFLOAD) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        if (computeGain()?.second != null) { // we need a compressor which outputs float32
            pendingOutputFloat = true
            toFloatPcmAudioProcessor.configure(inputAudioFormat)
            return AudioProcessor.AudioFormat(
                inputAudioFormat.sampleRate, inputAudioFormat.channelCount,
                C.ENCODING_PCM_FLOAT
            )
        }
        pendingOutputFloat = false
        return inputAudioFormat
    }

    fun setMode(mode: ReplayGainUtil.Mode, doNotNotifyListener: Boolean): Boolean {
        val listener: (() -> Unit)?
        synchronized(this) {
            if (this.mode == mode) {
                return true
            }
            listener = settingsChangedListener
            this.mode = mode
        }
        if (!doNotNotifyListener) {
            listener?.invoke()
            return applyGain()
        } else return true
    }

    fun setRgGain(rgGain: Int): Boolean {
        val listener: (() -> Unit)?
        synchronized(this) {
            if (this.rgGain == rgGain) {
                return true
            }
            listener = settingsChangedListener
            this.rgGain = rgGain
        }
        listener?.invoke()
        return applyGain()
    }

    fun setNonRgGain(nonRgGain: Int): Boolean {
        val listener: (() -> Unit)?
        synchronized(this) {
            if (this.nonRgGain == nonRgGain) {
                return true
            }
            listener = settingsChangedListener
            this.nonRgGain = nonRgGain
        }
        listener?.invoke()
        return applyGain()
    }

    fun setBoostGain(boostGain: Int): Boolean {
        val listener: (() -> Unit)?
        synchronized(this) {
            if (this.boostGain == boostGain) {
                return true
            }
            listener = boostGainChangedListener
            this.boostGain = boostGain
        }
        listener?.invoke()
        return applyGain()
    }

    fun setReduceGain(reduceGain: Boolean): Boolean {
        val listener: (() -> Unit)?
        synchronized(this) {
            if (this.reduceGain == reduceGain) {
                return true
            }
            listener = settingsChangedListener
            this.reduceGain = reduceGain
        }
        listener?.invoke()
        return applyGain()
    }

    fun setOffloadEnabled(offloadEnabled: Boolean): Boolean {
        val listener: (() -> Unit)?
        synchronized(this) {
            if (this.offloadEnabled == offloadEnabled) {
                return true
            }
            listener = offloadEnabledChangedListener
            this.offloadEnabled = offloadEnabled
        }
        listener?.invoke()
        return applyGain()
    }

    fun setRootFormat(inputFormat: Format) {
        tags = ReplayGainUtil.parse(inputFormat)
    }

    private fun computeGain(): Pair<Float, Float?>? {
        val mode: ReplayGainUtil.Mode
        val rgGain: Int
        val reduceGain: Boolean
        synchronized(this) {
            mode = this.mode
            rgGain = this.rgGain
            reduceGain = this.reduceGain
        }
        return ReplayGainUtil.calculateGain(
            tags, mode, rgGain,
            reduceGain, ReplayGainUtil.RATIO
        )
    }

    private fun applyGain(): Boolean {
        val nonRgGain: Int
        synchronized(this) {
            nonRgGain = this.nonRgGain
        }
        val gain = computeGain()
        if ((gain?.second != null) != outputFloat) {
            return false
        }
        this.gain = gain?.first ?: ReplayGainUtil.dbToAmpl(nonRgGain.toFloat())
        this.kneeThresholdDb = gain?.second
        if (kneeThresholdDb != null) {
            if (compressor == null)
                compressor = AdaptiveDynamicRangeCompression()
            compressor!!.init(
                inputAudioFormat.sampleRate,
                ReplayGainUtil.TAU_ATTACK, ReplayGainUtil.TAU_RELEASE,
                ReplayGainUtil.RATIO
            )
        } else {
            onReset() // delete compressor
        }
        return true
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) {
        outputFloat = pendingOutputFloat
        toFloatPcmAudioProcessor.flush(streamMetadata)
        if (!applyGain())
            Log.d(TAG, "raced between flush and configure, do nothing for now")
    }

    override fun onReset() {
        toFloatPcmAudioProcessor.reset()
        compressor?.release()
        compressor = null
    }
}
package com.ghhccghk.musicplay.util.exoplayer

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.ForwardingAudioSink
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.text.TextOutput
import androidx.media3.exoplayer.video.VideoRendererEventListener

@OptIn(UnstableApi::class)
class GramophoneRenderFactory(context: Context,
                              private val configurationListener: (Format?) -> Unit,
                              private val audioSinkListener: (DefaultAudioSink) -> Unit) :
    DefaultRenderersFactory(context) {
    override fun buildTextRenderers(
        context: Context,
        output: TextOutput,
        outputLooper: Looper,
        extensionRendererMode: Int,
        out: ArrayList<Renderer>
    ) {
        // empty
    }

    override fun buildVideoRenderers(
        context: Context,
        extensionRendererMode: Int,
        mediaCodecSelector: MediaCodecSelector,
        enableDecoderFallback: Boolean,
        eventHandler: Handler,
        eventListener: VideoRendererEventListener,
        allowedVideoJoiningTimeMs: Long,
        out: java.util.ArrayList<Renderer>
    ) {
        // empty
    }

    override fun buildImageRenderers(out: java.util.ArrayList<Renderer>) {
        // empty
    }

    override fun buildCameraMotionRenderers(
        context: Context,
        extensionRendererMode: Int,
        out: java.util.ArrayList<Renderer>
    ) {
        // empty
    }

    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean
    ): AudioSink? {
        return MyForwardingAudioSink(
            super.buildAudioSink(context, enableFloatOutput, enableAudioTrackPlaybackParams)!!)
    }

    inner class MyForwardingAudioSink(sink: AudioSink) : ForwardingAudioSink(sink) {
        init {
            audioSinkListener(sink as DefaultAudioSink)
        }

        override fun configure(
            inputFormat: Format,
            specifiedBufferSize: Int,
            outputChannels: IntArray?
        ) {
            super.configure(inputFormat, specifiedBufferSize, outputChannels)
            configurationListener(inputFormat)
        }

        override fun reset() {
            super.reset()
            configurationListener(null)
        }

        override fun release() {
            super.release()
            configurationListener(null)
        }
    }
}
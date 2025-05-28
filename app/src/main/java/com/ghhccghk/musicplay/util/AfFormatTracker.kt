/*
 *     Copyright (C) 2025 Akane Foundation
 *
 *     Gramophone is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Gramophone is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


/**
 * 保留来自 Gramophone 的开源协议 */


package com.ghhccghk.musicplay.util

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioRouting
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.AudioSink.AudioTrackConfig
import androidx.media3.exoplayer.audio.DefaultAudioSink
import org.nift4.gramophone.hificore.AudioTrackHiddenApi
import kotlinx.parcelize.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class AfFormatInfo(
    val routedDeviceName: String?, val routedDeviceId: Int?,
    val routedDeviceType: Int?, val mixPortId: Int?, val mixPortName: String?,
    val mixPortFlags: Int?, val ioHandle: Int?, val sampleRateHz: UInt?,
    val audioFormat: String?, val channelCount: Int?, val channelMask: Int?,
    val grantedFlags: Int?, val policyPortId: Int?, val afTrackFlags: Int?
) : Parcelable

@Parcelize
data class AudioTrackInfo(
    val encoding: Int, val sampleRateHz: Int, val channelConfig: Int,
    val offload: Boolean
) : Parcelable {
    companion object {
        @OptIn(UnstableApi::class)
        fun fromMedia3AudioTrackConfig(config: AudioTrackConfig) =
            AudioTrackInfo(
                config.encoding, config.sampleRate, config.channelConfig,
                config.offload
            )
    }
}

@OptIn(UnstableApi::class)
class AfFormatTracker(
    private val context: Context, private val playbackHandler: Handler,
) : AnalyticsListener {
    companion object {
        private const val LOG_EVENTS = true
        private const val TAG = "AfFormatTracker"
    }
    // only access sink or track on PlaybackThread
    private var lastAudioTrack: AudioTrack? = null
    private var audioSink: DefaultAudioSink? = null
    var format: AfFormatInfo? = null
        private set
    var formatChangedCallback: ((AfFormatInfo?) -> Unit)? = null

    private val routingChangedListener = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        object : AudioRouting.OnRoutingChangedListener {
            override fun onRoutingChanged(router: AudioRouting) {
                this@AfFormatTracker.onRoutingChanged(router as AudioTrack)
            }
        } as Any
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        @Suppress("deprecation")
        object : AudioTrack.OnRoutingChangedListener {
            override fun onRoutingChanged(router: AudioTrack) {
                this@AfFormatTracker.onRoutingChanged(router)
            }
        } as Any
    } else null

    private fun onRoutingChanged(router: AudioTrack) {
        val audioTrack = (audioSink ?: throw NullPointerException(
            "audioSink is null in onAudioTrackInitialized"
        )).getAudioTrack()
        if (router !== audioTrack) return // stale callback
        buildFormat(audioTrack)
    }

    // TODO why do we have to reflect on app code, there must be a better solution
    private fun DefaultAudioSink.getAudioTrack(): AudioTrack? {
        val cls = javaClass
        val field = cls.getDeclaredField("audioTrack")
        field.isAccessible = true
        return field.get(this) as AudioTrack?
    }

    fun setAudioSink(sink: DefaultAudioSink) {
        this.audioSink = sink
    }

    override fun onAudioTrackInitialized(
        eventTime: AnalyticsListener.EventTime,
        audioTrackConfig: AudioTrackConfig
    ) {
        format = null
        playbackHandler.post {
            val audioTrack = (audioSink ?: throw NullPointerException(
                "audioSink is null in onAudioTrackInitialized"
            )).getAudioTrack()
            if (audioTrack != lastAudioTrack) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    lastAudioTrack?.removeOnRoutingChangedListener(
                        routingChangedListener as AudioRouting.OnRoutingChangedListener
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    @Suppress("deprecation")
                    lastAudioTrack?.removeOnRoutingChangedListener(
                        routingChangedListener as AudioTrack.OnRoutingChangedListener
                    )
                }
                this.lastAudioTrack = audioTrack
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    audioTrack?.addOnRoutingChangedListener(
                        routingChangedListener as AudioRouting.OnRoutingChangedListener,
                        playbackHandler
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    @Suppress("deprecation")
                    audioTrack?.addOnRoutingChangedListener(
                        routingChangedListener as AudioTrack.OnRoutingChangedListener,
                        playbackHandler
                    )
                }
            }
            buildFormat(audioTrack)
        }
    }

    private fun buildFormat(audioTrack: AudioTrack?) {
        audioTrack?.let {
            if (audioTrack.state == AudioTrack.STATE_UNINITIALIZED) return@let null
            val rd = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                audioTrack.routedDevice else null
            val deviceProductName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                rd?.productName.toString() else null
            val deviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                rd?.type else null
            val deviceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                rd?.id else null
            val ioHandle = AudioTrackHiddenApi.getOutput(audioTrack)
            val halSampleRate = AudioTrackHiddenApi.getHalSampleRate(audioTrack)
            val grantedFlags = AudioTrackHiddenApi.getGrantedFlags(audioTrack)
            val mixPort = AudioTrackHiddenApi.getMixPortForThread(ioHandle, halSampleRate)
            val latency = try {
                // this call writes to mAfLatency and mLatency fields, hence call dump after this
                AudioTrack::class.java.getMethod("getLatency").invoke(audioTrack) as Int
            } catch (t: Throwable) {
                Log.e(TAG, Log.getStackTraceString(t))
                null
            }
            val dump = AudioTrackHiddenApi.dump(audioTrack)
            AfFormatInfo(
                deviceProductName, deviceId, deviceType,
                mixPort?.id, mixPort?.name, mixPort?.flags,
                ioHandle, halSampleRate,
                audioFormatToString(AudioTrackHiddenApi.getHalFormat(audioTrack)),
                AudioTrackHiddenApi.getHalChannelCount(audioTrack),
                mixPort?.channelMask, grantedFlags, AudioTrackHiddenApi.getPortIdFromDump(dump),
                AudioTrackHiddenApi.findAfTrackFlags(dump, latency, audioTrack, grantedFlags)
            )
        }.let {
            if (LOG_EVENTS)
                Log.d(TAG, "audio hal format changed to: $it")
            format = it
            formatChangedCallback?.invoke(it)
        }
    }

    private fun audioFormatToString(audioFormat: UInt?): String {
        for (encoding in AudioFormatDetector.Encoding.entries) {
            if (encoding.isSupportedAsNative && encoding.native == audioFormat)
                encoding.enc2?.let { return it }
        }
        return "AUDIO_FORMAT_($audioFormat)"
    }
}
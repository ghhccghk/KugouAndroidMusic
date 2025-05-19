package com.ghhccghk.musicplay.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.graphics.Bitmap
import android.net.Uri
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.AssetDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import androidx.navigation.findNavController
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.MainActivity.Companion.playbar
import com.ghhccghk.musicplay.R
import java.io.IOException

class PlayService : MediaSessionService() {

    companion object {
        const val CHANNEL_ID = "audio_player_channel"
        const val NOTIF_ID = 101
    }
    private lateinit var mediaSession: MediaSession

    @UnstableApi
    override fun onCreate() {
        super.onCreate()
        val assetDataSource = AssetDataSource(this)
        val dataSpec = DataSpec(Uri.parse("asset:///野火.mp3"))

        try {
            assetDataSource.open(dataSpec)
        } catch (e: IOException) {
            e.printStackTrace()
        }


        val factory = DataSource.Factory { assetDataSource }
       // 初始化 ExoPlayer
        val player: ExoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(factory))
            .build()

        mediaSession = MediaSession.Builder(
            this,
            player // ExoPlayer 或其他支持的 Player 实现
        ).build()


        val mediaItem = MediaItem.Builder()
            .setUri(dataSpec.uri)
            .build()

       player.setMediaItem(mediaItem)

        val name = "Media Control"
        val descriptionText = "Media Control Notification Channel"
        val importance = NotificationManager.IMPORTANCE_NONE
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            enableVibration(false)
            vibrationPattern = longArrayOf(0)
            setSound(null, null)
        }
        val notificationManager: NotificationManager =
            ContextCompat.getSystemService(
                this,
                NotificationManager::class.java
            ) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notificationProvider =
            DefaultMediaNotificationProvider.Builder(this)
                .setNotificationId(NOTIF_ID)
                .setChannelId(CHANNEL_ID)
                .build()

        notificationProvider.setSmallIcon(R.drawable.ic_cd)

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    // 播放开始
                    println("▶️ 播放中")
                    playbar.findViewById<TextView>(R.id.playbar_artist).text = player.mediaMetadata.artist
                    playbar.findViewById<TextView>(R.id.playbar_title).text = player.mediaMetadata.title
                } else {
                    // 播放暂停
                    println("⏸️ 已暂停")
                    playbar.findViewById<TextView>(R.id.playbar_artist).text = player.mediaMetadata.artist
                    playbar.findViewById<TextView>(R.id.playbar_title).text = player.mediaMetadata.title
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_IDLE -> println("空闲")
                    Player.STATE_BUFFERING -> println("缓冲中")
                    Player.STATE_READY -> println("准备好")
                    Player.STATE_ENDED -> println("播放结束")
                }
            }
        })


        this.setMediaNotificationProvider(notificationProvider)




    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // --- Adapter for notification metadata ---
    @UnstableApi
    inner class DescriptionAdapter : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player) =
            "Track Title"        // TODO: 从你的数据源获取

        override fun createCurrentContentIntent(player: Player): PendingIntent? =
            // 点击通知进入主界面
            packageManager?.getLaunchIntentForPackage(packageName)?.let {
                PendingIntent.getActivity(baseContext, 0, it, FLAG_IMMUTABLE)
            }

        override fun getCurrentContentText(player: Player) =
            "Artist Name"        // TODO

        override fun getCurrentLargeIcon(
            player: Player, callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? = null      // 可异步加载专辑图后 callback.onBitmap()
    }
}
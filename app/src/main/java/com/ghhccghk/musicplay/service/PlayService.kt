package com.ghhccghk.musicplay.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.util.Base64
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.AssetDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.ghhccghk.musicplay.BuildConfig
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.MainActivity.Companion.playbar
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.objects.MainViewModelObject
import com.ghhccghk.musicplay.data.objects.MediaViewModelObject
import com.ghhccghk.musicplay.ui.lyric.MeiZuLyricsMediaNotificationProvider
import com.hchen.superlyricapi.SuperLyricData
import com.hchen.superlyricapi.SuperLyricPush
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class PlayService : MediaSessionService() {

    companion object {
        const val CHANNEL_ID = "audio_player_channel"
        const val NOTIF_ID = 101
        private const val PENDING_INTENT_SESSION_ID = 0
    }
    private lateinit var mediaSession: MediaSession
    private var lyric : String = ""

    fun run() {
        val base64 = drawableToBase64(getDrawable(R.drawable.ic_cd)!!)
        val handler by lazy { Handler(Looper.getMainLooper()) }

        val updateLyricsRunnable = object : Runnable {
            override fun run() {
                runCatching {
                    var currentLyricIndex: Int
                    var isPlaying: Boolean?
                    var liveTime: Long
                    var lastLyric = listOf<Pair<Float, String>>()

                    handler.post {
                        isPlaying = mediaSession.player.isPlaying

                        runCatching {
                            currentLyricIndex = MainViewModelObject.syncLyricIndex.intValue


                            if (isPlaying == true) {
                                liveTime = mediaSession.player.currentPosition

                                val lrcEntries = MediaViewModelObject.lrcEntries.value

                                val nextIndex = lrcEntries.indexOfFirst { line ->
                                    line.first().first >= liveTime
                                }

                                val sendLyric = fun() {
                                    try {
                                        MainViewModelObject.syncLyricIndex.intValue =
                                            currentLyricIndex

                                        val line = lrcEntries[currentLyricIndex]
                                        if (line == lastLyric) {
                                            return
                                        }

                                        val lyricb = StringBuffer("")

                                        line.forEachIndexed { charIndex, char ->
                                            if (charIndex >= line.size - 1) return@forEachIndexed
                                            lyricb.append(char.second)
                                        }

                                        val lyricResult = lyricb.toString()
                                        lyric = lyricResult

                                        Log.d("debug",lyric)

                                        if (true) {
                                            // 请注意，非常建议您设置包名，这是判断当前播放应用的唯一途径！！
                                            SuperLyricPush.onSuperLyric(
                                                SuperLyricData()
                                                    .setLyric(lyricResult) // 设置歌词
                                                    .setBase64Icon(base64)
                                                    .setPackageName(BuildConfig.APPLICATION_ID) // 设置本软件包名
                                            ) // 发送歌词
                                        }
                                        lastLyric = line
                                    } catch (_: Exception) {
                                    }
                                }

                                var newIndex = currentLyricIndex

                                if (nextIndex != -1 && nextIndex - 1 != currentLyricIndex) {
                                    newIndex = nextIndex - 1
                                } else if (nextIndex == -1 && currentLyricIndex != lrcEntries.size - 1) {
                                    newIndex = lrcEntries.size - 1
                                }

                                if (newIndex != currentLyricIndex) {
                                    currentLyricIndex = newIndex
                                    sendLyric()
                                }

                            }
                        }
                    }

                    handler.postDelayed(this, 70)
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            handler.post(updateLyricsRunnable)
        }
    }


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

        val cache = SimpleCache(
            File(this.cacheDir, "exo_music_cache"),
            LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024 * 1024), // 100MB
            StandaloneDatabaseProvider(this)
        )

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory()) // 自动联网
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)




       //val factory = DataSource.Factory { assetDataSource }
       // 初始化 ExoPlayer
        val player: ExoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .build()

        //通知点击返回应用
        val packageManager: PackageManager = getPackageManager()
        val it: Intent? = packageManager.getLaunchIntentForPackage(BuildConfig.APPLICATION_ID)


        mediaSession = MediaSession.Builder(
            this,
            player // ExoPlayer 或其他支持的 Player 实现
        ).setSessionActivity(
            PendingIntent.getActivity(
                this,
                PENDING_INTENT_SESSION_ID,
                it,
                FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        )
            .build()


        run()

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
                    playbar.findViewById<TextView>(R.id.playbar_artist).text = player.mediaMetadata.artist
                    playbar.findViewById<TextView>(R.id.playbar_title).text = player.mediaMetadata.title
                } else {
                    // 播放暂停
                    playbar.findViewById<TextView>(R.id.playbar_artist).text = player.mediaMetadata.artist
                    playbar.findViewById<TextView>(R.id.playbar_title).text = player.mediaMetadata.title
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_IDLE -> println("空闲")
                    Player.STATE_BUFFERING -> println("缓冲中")
                    Player.STATE_READY -> println("准备好")
                    Player.COMMAND_PLAY_PAUSE -> println("播放暂停")
                    Player.STATE_ENDED -> println("播放结束")
                }
            }
        })



        this.setMediaNotificationProvider(notificationProvider)
        this.setMediaNotificationProvider(MeiZuLyricsMediaNotificationProvider(this){ lyric })




    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    fun drawableToBase64(drawable: Drawable): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (drawable is AdaptiveIconDrawable) {
                return adaptiveIconDrawableBase64(drawable)
            }
        }
        when (drawable) {
            is BitmapDrawable -> {
                return drawableToBase64(drawable.bitmap)
            }

            is VectorDrawable -> {
                return drawableToBase64(
                    makeDrawableToBitmap(
                        drawable
                    )
                )
            }

            else -> {
                return try {
                    drawableToBase64((drawable as BitmapDrawable).bitmap)
                } catch (_: Exception) {
                    ""
                }
            }
        }
    }

    private fun adaptiveIconDrawableBase64(drawable: AdaptiveIconDrawable): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val background = drawable.background
            val foreground = drawable.foreground
            if (background != null && foreground != null) {
                val layerDrawable = LayerDrawable(arrayOf(background, foreground))
                val createBitmap = Bitmap.createBitmap(layerDrawable.intrinsicWidth, layerDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(createBitmap)
                layerDrawable.setBounds(0, 0, canvas.width, canvas.height)
                layerDrawable.draw(canvas)
                drawableToBase64(createBitmap)
            } else {
                ""
            }
        } else {
            ""
        }
    }

    private fun makeDrawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.apply {
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }
        return bitmap
    }

    fun drawableToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    @UnstableApi
    fun createDownloadRequest(
        uri: String,
        title: String,
        artist: String
    ): DownloadRequest {
        val fileName = "$artist - $title.mp3"

        // 用 extras 存储文件名等信息（可选）
        val extras = Bundle().apply {
            putString("fileName", fileName)
        }

        return DownloadRequest.Builder(fileName, Uri.parse(uri))
            .setMimeType(MimeTypes.AUDIO_MPEG) // 根据格式调整
            .setData(extras.toByteArray()) // 用于记录额外信息
            .build()
    }

    fun Bundle.toByteArray(): ByteArray {
        val parcel = Parcel.obtain()
        parcel.writeBundle(this)
        val bytes = parcel.marshall()
        parcel.recycle()
        return bytes
    }


}
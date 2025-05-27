package com.ghhccghk.musicplay.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.ghhccghk.musicplay.BuildConfig
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.MainActivity.Companion.playbar
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.getLyricCode
import com.ghhccghk.musicplay.data.libraries.MediaItemEntity
import com.ghhccghk.musicplay.data.libraries.lrcAccesskey
import com.ghhccghk.musicplay.data.libraries.lrcId
import com.ghhccghk.musicplay.data.objects.MediaViewModelObject
import com.ghhccghk.musicplay.ui.lyric.MeiZuLyricsMediaNotificationProvider
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.ghhccghk.musicplay.util.lrc.YosLrcFactory
import com.ghhccghk.musicplay.util.others.PlaylistRepository
import com.ghhccghk.musicplay.util.others.toEntity
import com.ghhccghk.musicplay.util.others.toMediaItem
import com.google.gson.Gson
import com.hchen.superlyricapi.SuperLyricData
import com.hchen.superlyricapi.SuperLyricPush
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.core.graphics.createBitmap
import androidx.media3.datasource.cache.CacheKeyFactory
import androidx.media3.session.doUpdateNotification
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.data.libraries.RedirectingDataSourceFactory


class PlayService : MediaSessionService() {

    companion object {
        const val CHANNEL_ID = "audio_player_channel"
        const val NOTIF_ID = 101
        private const val PENDING_INTENT_SESSION_ID = 0
    }
    private lateinit var mediaSession: MediaSession
    private var lyric : String = ""
    // 当前歌词行数
    private var currentLyricIndex: Int = 0

    // 创建一个 CoroutineScope，默认用 SupervisorJob 和 Main 调度器（UI线程）
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @SuppressLint("UseCompatLoadingForDrawables")
    fun run() {
        val base64 = drawableToBase64(getDrawable(R.drawable.ic_cd)!!)
        val handler by lazy { Handler(Looper.getMainLooper()) }

        val updateLyricsRunnable = object : Runnable {
            override fun run() {
                runCatching {
                    var isPlaying: Boolean?
                    var liveTime: Long
                    var lastLyric = listOf<Pair<Float, String>>()

                    handler.post {
                        isPlaying = mediaSession.player.isPlaying

                        runCatching {


                            if (isPlaying == true) {

                                liveTime = mediaSession.player.currentPosition

                                val lrcEntries = MediaViewModelObject.lrcEntries.value

                                val nextIndex = lrcEntries.indexOfFirst { line ->
                                    line.first().first >= liveTime
                                }

                                val sendLyric = fun() {
                                    try {

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


                                        if (playbar.visibility != View.GONE) {
                                            playbar.findViewById<TextView>(R.id.playbar_artist).text = lyricResult
                                        }

                                        if (true) {
                                            doUpdateNotification(mediaSession)
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
        val repo = PlaylistRepository(MainActivity.lontext)
        val prefs = MainActivity.lontext.getSharedPreferences("play_setting_prefs", Context.MODE_PRIVATE)

        val cache = SimpleCache(
            File(this.getExternalFilesDir(null), "exo_music_cache"),
            LeastRecentlyUsedCacheEvictor(900 * 1024 * 1024), // 100MB
            StandaloneDatabaseProvider(this)
        )

        val cacheKeyFactory = CacheKeyFactory { dataSpec ->
            val uri = dataSpec.uri
            val id = uri.getQueryParameter("id")
            if (id != null) {
                id
            } else {
                dataSpec.key ?: uri.toString() // fallback
            }
        }


        val dhttp = DefaultHttpDataSource.Factory()

        val redirectingFactory = RedirectingDataSourceFactory(dhttp)

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(redirectingFactory) // 自动联网
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            .setCacheKeyFactory(cacheKeyFactory)




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
            player
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

        CoroutineScope(Dispatchers.Main).launch {
            val list = repo.loadPlaylist().first()  // 只取一次
            if (list.isNotEmpty()) {
                val mediaItems = list.map { it.toMediaItem() }
                val last = prefs.getInt("lastplayitem",-1)
                player.setMediaItems(mediaItems)
                player.prepare()
                if (last != -1){
                    player.seekToDefaultPosition(last)
                }
                val artist = player.mediaMetadata?.artist
                val title = player.mediaMetadata?.title

                playbar.findViewById<TextView>(R.id.playbar_artist).text = if (artist.isNullOrBlank()) "未知艺术家" else artist
                playbar.findViewById<TextView>(R.id.playbar_title).text =  if (title.isNullOrBlank()) "未知歌曲" else title
            }
        }

        val artist = player.mediaMetadata?.artist
        val title = player.mediaMetadata?.title


        playbar.findViewById<TextView>(R.id.playbar_artist).text = if (artist.isNullOrBlank()) "未知艺术家" else artist
        playbar.findViewById<TextView>(R.id.playbar_title).text =  if (title.isNullOrBlank()) "未知歌曲" else title

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
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)

                val lyricid = mediaSession.player.currentMediaItem?.lrcId.toString()
                val lyricAccess = mediaSession.player.currentMediaItem?.lrcAccesskey.toString()

                val subDir = "cache/lyrics"
                val fileName = "${mediaSession.player.currentMediaItem?.mediaId}.lrc"

                val cachedData = readFromSubdirCache(MainActivity.lontext, subDir, fileName)

                if (cachedData != null) {
                    Log.d("Cache", "子目录缓存命中: $cachedData")
                    MediaViewModelObject.lrcEntries.value = YosLrcFactory(false).formatLrcEntries(cachedData)
                } else {
                    if ( MainActivity.isNodeRunning ) {
                        serviceScope.launch {
                            val json = withContext(Dispatchers.IO) {
                                KugouAPi.getSongLyrics(
                                    id = lyricid, accesskey = lyricAccess,
                                    fmt = "krc", decode = true
                                )
                            }
                            try {
                                val gson = Gson()
                                val result = gson.fromJson(json, getLyricCode::class.java)
                                val lyric = result.decodeContent
                                val out = convertKrcToLrc(lyric)
                                writeToSubdirCache(
                                    MainActivity.lontext,
                                    subDir,
                                    fileName,
                                    out.toString()
                                )
                                writeToSubdirCache(
                                    MainActivity.lontext,
                                    subDir,
                                    "ghhcc.lrc",
                                    lyric.toString()
                                )
                                val cachedDataa = readFromSubdirCache(MainActivity.lontext, subDir, fileName)
                                if (cachedDataa != null) {
                                    MediaViewModelObject.lrcEntries.value = YosLrcFactory(false).formatLrcEntries(cachedDataa)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(
                                    MainActivity.lontext,
                                    "数据加载失败: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        }
                    }
                }

            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    // 播放开始
                    playbar.findViewById<TextView>(R.id.playbar_artist).text = player.mediaMetadata.artist
                    playbar.findViewById<TextView>(R.id.playbar_title).text = player.mediaMetadata.title
                } else {
                    serviceScope.launch {
                        if (player.playbackState != Player.STATE_IDLE && player.currentTimeline.isEmpty.not()) {
                            val index = player.currentMediaItemIndex
                            saveCurrentPlaylist(player, repo)
                            prefs.edit().putInt("lastplayitem",index).apply()
                            Log.d("ExoPlayer", "当前播放索引：$index")
                        } else {
                            Log.d("ExoPlayer", "播放列表未就绪")
                        }

                    }
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
        this.setMediaNotificationProvider(MeiZuLyricsMediaNotificationProvider(this) { lyric })

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

    @SuppressLint("ObsoleteSdkInt")
    private fun adaptiveIconDrawableBase64(drawable: AdaptiveIconDrawable): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val background = drawable.background
            val foreground = drawable.foreground
            if (background != null && foreground != null) {
                val layerDrawable = LayerDrawable(arrayOf(background, foreground))
                val createBitmap =
                    createBitmap(layerDrawable.intrinsicWidth, layerDrawable.intrinsicHeight)
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
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
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

    fun getExternalSubdirFile(context: Context, subDir: String, fileName: String): File? {
        val baseDir = context.getExternalFilesDir(null)
        val targetDir = File(baseDir, subDir)

        if (!targetDir.exists()) {
            targetDir.mkdirs()  // 确保子目录存在
        }

        return File(targetDir, fileName)
    }

    fun writeToSubdirCache(context: Context, subDir: String, fileName: String, data: String) {
        val file = getExternalSubdirFile(context, subDir, fileName)
        file?.writeText(data)
    }

    fun readFromSubdirCache(context: Context, subDir: String, fileName: String): String? {
        val file = getExternalSubdirFile(context, subDir, fileName)
        return if (file != null && file.exists()) {
            file.readText()
        } else {
            null
        }
    }


    fun convertKrcToLrc(krcContent: String): String {
        val lineRegex = Regex("""\[(\d+),(\d+)]""")  // [开始时间, 持续时间]
        val wordRegex = Regex("""<(\d+),(\d+),\d+>(.*?)(?=<|$)""")  // <偏移, 持续, ?>文字

        val output = mutableListOf<String>()

        for (line in krcContent.lines()) {
            val lineMatch = lineRegex.find(line) ?: continue
            val lineStartTime = lineMatch.groupValues[1].toLong()

            val wordMatches = wordRegex.findAll(line).toList()
            if (wordMatches.isEmpty()) continue

            val sb = StringBuilder()

            for ((index, match) in wordMatches.withIndex()) {
                val offset = match.groupValues[1].toLong()
                val duration = match.groupValues[2].toLong()
                val word = match.groupValues[3]

                val time = lineStartTime + offset
                sb.append("[${millisToTimeStr(time)}]$word")

                // 如果是最后一个词，加一个“结尾时间标签”
                if (index == wordMatches.lastIndex) {
                    val endTime = time + duration
                    sb.append("[${millisToTimeStr(endTime)}]")
                }
            }

            output.add(sb.toString())
        }

        return output.joinToString("\n")
    }

    fun millisToTimeStr(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val hundredths = (ms % 1000) / 10
        return "%02d:%02d.%02d".format(minutes, seconds, hundredths)
    }


    fun lrctimefix(a: String): String {

        val timeRegex = "\\[(\\d{2}):(\\d{2})\\.(\\d{2})]".toRegex()

        // Map 时间戳文本 -> 它出现的行索引
        val timeMap = mutableMapOf<String, MutableList<Int>>()
        val inputLines = a.lines()

        inputLines.forEachIndexed { index, line ->
            val match = timeRegex.find(line)
            match?.value?.let { timeTag ->
                timeMap.computeIfAbsent(timeTag) { mutableListOf() }.add(index)
            }
        }

        val linesCopy = inputLines.toMutableList()

        for ((timeTag, indices) in timeMap) {
            if (indices.size >= 3) {
                for ((offset, i) in indices.withIndex()) {
                    val match = timeRegex.find(timeTag)
                    if (match != null) {
                        val minutes = match.groupValues[1].toInt()
                        val seconds = match.groupValues[2].toInt()
                        val hundredths = match.groupValues[3].toInt()

                        // 原时间戳 + offset * 10ms
                        var totalMillis = (minutes * 60 + seconds) * 1000 + hundredths * 10 + offset * 10

                        val newMinutes = totalMillis / 60000
                        val newSeconds = (totalMillis % 60000) / 1000
                        val newHundredths = (totalMillis % 1000) / 10

                        val newTimeTag = "[%02d:%02d.%02d]".format(newMinutes, newSeconds, newHundredths)
                        // 替换原行中的时间戳
                        linesCopy[i] = linesCopy[i].replace(timeRegex, newTimeTag)
                    }
                }
            }
        }

        // 输出结果
        return linesCopy.joinToString("\n")
    }

    suspend fun saveCurrentPlaylist(player: ExoPlayer, dao: PlaylistRepository) {
        val itemCount = player.mediaItemCount
        val entities = mutableListOf<MediaItemEntity>()

        for (i in 0 until itemCount) {
            val mediaItem = player.getMediaItemAt(i)
            val entity = mediaItem.toEntity()  // 你之前写的转换函数
            entities.add(entity)
        }

        // 批量插入数据库
        dao.savePlaylist(entities)
    }





}
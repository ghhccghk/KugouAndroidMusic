package com.ghhccghk.musicplay.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.media.AudioDeviceInfo
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheKeyFactory
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.ghhccghk.musicplay.BuildConfig
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.MainActivity.Companion.playbar
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.getLyricCode
import com.ghhccghk.musicplay.data.libraries.RedirectingDataSourceFactory
import com.ghhccghk.musicplay.data.libraries.lrcAccesskey
import com.ghhccghk.musicplay.data.libraries.lrcId
import com.ghhccghk.musicplay.data.libraries.songtitle
import com.ghhccghk.musicplay.data.libraries.uri
import com.ghhccghk.musicplay.data.objects.MainViewModelObject
import com.ghhccghk.musicplay.data.objects.MainViewModelObject.currentMediaItemIndex
import com.ghhccghk.musicplay.data.objects.MediaViewModelObject
import com.ghhccghk.musicplay.data.objects.MediaViewModelObject.mediaItems
import com.ghhccghk.musicplay.data.searchLyric.searchLyricBase
import com.ghhccghk.musicplay.ui.lyric.MeiZuLyricsMediaNotificationProvider
import com.ghhccghk.musicplay.ui.lyric.isManualNotificationUpdate
import com.ghhccghk.musicplay.util.AfFormatTracker
import com.ghhccghk.musicplay.util.AudioTrackInfo
import com.ghhccghk.musicplay.util.BtCodecInfo
import com.ghhccghk.musicplay.util.CustomKrcParser
import com.ghhccghk.musicplay.util.Flags
import com.ghhccghk.musicplay.util.LyricSyncManager
import com.ghhccghk.musicplay.util.NodeBridge
import com.ghhccghk.musicplay.util.Tools
import com.ghhccghk.musicplay.util.Tools.getBitrate
import com.ghhccghk.musicplay.util.Tools.getStringStrict
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.ghhccghk.musicplay.util.exoplayer.GramophoneRenderFactory
import com.ghhccghk.musicplay.util.others.PlaylistRepository
import com.ghhccghk.musicplay.util.others.toMediaItem
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.hchen.superlyricapi.SuperLyricData
import com.hchen.superlyricapi.SuperLyricPush
import com.hyperfocus.api.FocusApi
import com.hyperfocus.api.IslandApi
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeLine
import com.mocharealm.accompanist.lyrics.core.model.synced.SyncedLine
import com.mocharealm.accompanist.lyrics.core.model.synced.toSyncedLine
import com.mocharealm.accompanist.lyrics.core.parser.AutoParser
import com.mocharealm.accompanist.lyrics.core.utils.LyricsFormatGuesser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File


@UnstableApi
class PlayService : MediaSessionService(),
    MediaLibraryService.MediaLibrarySession.Callback, Player.Listener, AnalyticsListener {

    companion object {
        const val CHANNEL_ID = "audio_player_channel"
        const val NOTIF_ID = 101
        private const val PENDING_INTENT_SESSION_ID = 0
        const val SERVICE_GET_AUDIO_FORMAT = "get_audio_format"
    }

    private lateinit var mediaSession: MediaSession
    private var lyric: String = ""

    // 当前歌词行数
    private var currentLyricIndex: Int = 0
    val gson = Gson()


    //Node js 服务相关
    var isNodeRunning = false
    var isNodeRunError: String = ""
    private val nodeReadyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == NodeBridge.ACTION_NODE_READY) {
                isNodeRunning = true
            }
        }
    }

    // 创建一个 CoroutineScope，默认用 SupervisorJob 和 Main 调度器（UI线程）
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var afFormatTracker: AfFormatTracker
    private var downstreamFormat: Format? = null
    private lateinit var playbackHandler: Handler
    private lateinit var handler: Handler
    private var audioSinkInputFormat: Format? = null
    private var audioTrackInfo: AudioTrackInfo? = null
    private var audioTrackInfoCounter = 0
    private var audioTrackReleaseCounter = 0
    private var btInfo: BtCodecInfo? = null
    private var bitrate: Long? = null
    private val bitrateFetcher = CoroutineScope(Dispatchers.IO.limitedParallelism(1))
    private lateinit var repo: PlaylistRepository
    private lateinit var prefs: SharedPreferences
    private val nfBundle : Bundle = Bundle()
    val subDir = "cache/lyrics"
    private var proxy: BtCodecInfo.Companion.Proxy? = null



    @SuppressLint("UseCompatLoadingForDrawables")
    fun run() {
        val base64 = Tools.drawableToBase64(getDrawable(R.drawable.ic_cd)!!)
        val handler by lazy { Handler(Looper.getMainLooper()) }


        val updateLyricsRunnable = object : Runnable {
            override fun run() {
                runCatching {
                    var isPlaying: Boolean?
                    var liveTime: Long
                    var lastLyric = ""
                    val play_bar_lyrics = prefs.getBoolean("play_bar_lyrics",false)

                    handler.post {
                        isPlaying = mediaSession.player.isPlaying

                        runCatching {

                            if (isPlaying == true) {
                                val car_lyrics = prefs.getBoolean("car_lyrics", false)
                                val status_bar_lyrics = prefs.getBoolean("status_bar_lyrics", false)
                                val newlyric = MediaViewModelObject.newLrcEntries.value

                                MainViewModelObject.syncLyricIndex.intValue = currentLyricIndex

                                liveTime = mediaSession.player.currentPosition

//                                val lrcEntries = MediaViewModelObject.lrcEntries.value

                                val nextIndex = newlyric.lines.indexOfFirst { line ->
                                    line.start >= liveTime
                                }

                                val sendLyric = fun() {
                                    try {
                                        val newLine = newlyric.lines[currentLyricIndex]

                                        when (newLine){
                                            is KaraokeLine -> {
                                                if (lastLyric == newLine.toSyncedLine().content) return
                                            }
                                            is SyncedLine -> {
                                                if (lastLyric == newLine.content) return
                                            }
                                        }

                                        val lyricb = StringBuffer("")
                                        //翻译
                                        val translation = StringBuffer("")

                                        when (newLine){
                                            is KaraokeLine -> {
                                                lyricb.append(newLine.toSyncedLine().content)
                                                translation.append(newLine.toSyncedLine().translation)

                                            }
                                            is SyncedLine -> {
                                                lyricb.append(newLine.content)
                                                translation.append(newLine.translation)
                                            }
                                        }


                                        val lyricResult = lyricb.toString()
                                        val translationResult = translation.toString()

                                        if (playbar.visibility != View.GONE && play_bar_lyrics) {
                                            playbar.findViewById<TextView>(R.id.playbar_artist).text = lyricResult
                                        }

                                        bitrateFetcher.launch {
                                            withContext(Dispatchers.IO){
                                                LyricSyncManager(
                                                    this@PlayService,
                                                    MediaViewModelObject.newLrcEntries.value
                                                ).sync(currentLyricIndex)
                                            }
                                        }

                                        if (car_lyrics || status_bar_lyrics) {
                                            lyric = lyricResult
                                            if (car_lyrics) {
                                                val sessionMetadata =
                                                    mediaSession.player.mediaMetadata
                                                val sessionMediaItem =
                                                    mediaSession.player.currentMediaItem

                                                val newdata =
                                                    sessionMetadata.buildUpon().setTitle(lyric)
                                                        .build()
                                                val newmedia = sessionMediaItem?.buildUpon()
                                                    ?.setMediaMetadata(newdata)?.build()

                                                mediaSession.player.replaceMediaItem(
                                                    mediaSession.player.currentMediaItemIndex,
                                                    newmedia!!
                                                )
                                            }
                                            if (status_bar_lyrics) {// 请注意，非常建议您设置包名，这是判断当前播放应用的唯一途径！！
                                                if (translationResult != "null"){
                                                    SuperLyricPush.onSuperLyric(
                                                        SuperLyricData()
                                                            .setLyric(lyricResult) // 设置歌词
                                                            .setBase64Icon(base64)
                                                            .setPackageName(BuildConfig.APPLICATION_ID) // 设置本软件包名
                                                            .setTranslation(translationResult)
                                                    ) // 发送歌词
                                                } else {
                                                    SuperLyricPush.onSuperLyric(
                                                        SuperLyricData()
                                                            .setLyric(lyricResult) // 设置歌词
                                                            .setBase64Icon(base64)
                                                            .setPackageName(BuildConfig.APPLICATION_ID) // 设置本软件包名
                                                    ) // 发送歌词
                                                }
                                            }

                                            val param = JSONObject()
                                            val paramV2 = JSONObject()
                                            val island = JSONObject()

                                            island.put("shareData", IslandApi.ShareData(
                                                title = mediaSession?.player?.currentMediaItem?.songtitle?: "",
                                                content = mediaSession?.player?.mediaMetadata?.artist.toString(),
                                                pic = "miui.focus.pic_app",
                                                shareContent = lyric
                                            ))

                                            paramV2.put("param_island",island)
                                            param.put("param_v2",paramV2)

                                            nfBundle.putBundle("miui.focus.pics", FocusApi.addpics("app", Icon.createWithResource(this@PlayService,R.drawable.lycaon_icon)))
                                            nfBundle.putString("miui.focus.param.media",param.toString())


                                            mediaSession?.let {
                                                if (Looper.myLooper() != it.player.applicationLooper)
                                                    throw UnsupportedOperationException("wrong looper for triggerNotificationUpdate")
                                                isManualNotificationUpdate = true
                                                triggerNotificationUpdate()
                                                isManualNotificationUpdate = false
                                            }
                                        }
                                        lastLyric = lyricResult
                                    } catch (_: Exception) {
                                    }
                                }

                                var newIndex = currentLyricIndex

                                if (nextIndex != -1 && nextIndex - 1 != currentLyricIndex) {
                                    newIndex = nextIndex - 1
                                } else if (nextIndex == -1 && currentLyricIndex != newlyric.lines.size - 1) {
                                    newIndex = newlyric.lines.size - 1
                                }

                                if (newIndex != currentLyricIndex) {
                                    currentLyricIndex = newIndex
                                    sendLyric()
                                }

                            } else {
                                val sessionMetadata = mediaSession.player.mediaMetadata
                                val sessionMediaItem = mediaSession.player.currentMediaItem
                                val t = sessionMediaItem?.songtitle?.toString()

                                if (sessionMetadata.title != t) {
                                    val newdata = sessionMetadata.buildUpon().setTitle(t).build()
                                    val newmedia =
                                        sessionMediaItem?.buildUpon()?.setMediaMetadata(newdata)
                                            ?.build()
                                    mediaSession.player.replaceMediaItem(
                                        mediaSession.player.currentMediaItemIndex,
                                        newmedia!!
                                    )
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

    inner class LocalBinder : Binder() {
        fun getService(): PlayService = this@PlayService
    }

    @UnstableApi
    override fun onCreate() {
        super.onCreate()
        prefs = this.getSharedPreferences("play_setting_prefs", MODE_PRIVATE)
        repo = PlaylistRepository(applicationContext)
        handler = Handler(Looper.getMainLooper())
        val filter = IntentFilter(NodeBridge.ACTION_NODE_READY)
        LocalBroadcastManager.getInstance(this).registerReceiver(nodeReadyReceiver, filter)
        serviceScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NodeBridge.startNode() // 这里调用 native 方法
                    isNodeRunning = true
                } catch (e: Exception) {
                    isNodeRunning = false
                    isNodeRunError = e.toString()
                }
            }
        }
        val cacheSizeMB = prefs.getString("song_cache_size", "50")?.toLongOrNull() ?: 950L

        val cacheSizeBytes = cacheSizeMB * 1024 * 1024


        val cache = SimpleCache(
            File(this.getExternalFilesDir(null), "cache/exo_music_cache"),
            LeastRecentlyUsedCacheEvictor(cacheSizeBytes), // 100MB
            StandaloneDatabaseProvider(this)
        )

        val cacheKeyFactory = CacheKeyFactory { dataSpec ->
            val quality = prefs.getString("song_quality", "128").toString()
            val uri = dataSpec.uri
            val ida = uri.getQueryParameter("id")
            val id = (ida + quality)
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


        playbackHandler = Handler(Looper.getMainLooper())

        afFormatTracker = AfFormatTracker(this, playbackHandler,handler)
        afFormatTracker.formatChangedCallback = {
            mediaSession?.broadcastCustomCommand(
                SessionCommand(SERVICE_GET_AUDIO_FORMAT, Bundle.EMPTY),
                Bundle.EMPTY
            )
        }

        // 初始化 ExoPlayer
        val player: ExoPlayer = ExoPlayer.Builder(
            this, GramophoneRenderFactory(
                this, this::onAudioSinkInputFormatChanged,
                afFormatTracker::setAudioSink
            ).setPcmEncodingRestrictionLifted(
                prefs.getBoolean("floatoutput", false)
            )
                .setEnableDecoderFallback(true)
                .setEnableAudioTrackPlaybackParams(true)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        )
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(), true
            )
            .setTrackSelector(DefaultTrackSelector(this).apply {
                setParameters(buildUponParameters()
                    .setAllowInvalidateSelectionsOnRendererCapabilitiesChange(true)
                    .setAudioOffloadPreferences(
                        TrackSelectionParameters.AudioOffloadPreferences.Builder()
                            .apply {
                                val config = prefs.getStringStrict("offload", "0")?.toIntOrNull()
                                if (config != null && config > 0 && Flags.OFFLOAD) {
                                    setAudioOffloadMode(TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED)
                                    setIsGaplessSupportRequired(config == 2)
                                }
                            }
                            .build()))
            })
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .build()

        player.addAnalyticsListener(this)

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
        ).setCallback(this).build()

        run()

        CoroutineScope(Dispatchers.Main).launch {
            val list = repo.loadPlaylist().first()  // 只取一次
            if (list.isNotEmpty()) {
                val mediaItems = list.map { it.toMediaItem() }
                val last = prefs.getInt("lastplayitem", -1)
                player.setMediaItems(mediaItems)
                player.prepare()
                if (last != -1) {
                    player.seekToDefaultPosition(last)
                }
            }
        }

        mediaSession.broadcastCustomCommand(
            SessionCommand(SERVICE_GET_AUDIO_FORMAT, Bundle.EMPTY),
            Bundle.EMPTY
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O /* before 8, only sbc was supported */) {
            proxy = BtCodecInfo.getCodec(this) {
                Log.d("GramophonePlaybackService", "first bluetooth codec config $btInfo")
                btInfo = it
                mediaSession?.broadcastCustomCommand(
                    SessionCommand(SERVICE_GET_AUDIO_FORMAT, Bundle.EMPTY),
                    Bundle.EMPTY
                )
            }
        }

        player.addAnalyticsListener(afFormatTracker)

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

        player.addListener(this)

        this.setMediaNotificationProvider(notificationProvider)
        this.setMediaNotificationProvider(MeiZuLyricsMediaNotificationProvider(this, { lyric }, nfBundle))

    }

    override fun onDestroy() {
        super.onDestroy()
        isNodeRunning = false
        proxy?.let {
            it.adapter.closeProfileProxy(BluetoothProfile.A2DP, it.a2dp)
        }
    }

    // Configure commands available to the controller in onConnect()
    override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo)
            : MediaSession.ConnectionResult {
        val availableSessionCommands =
            MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
        availableSessionCommands.add(SessionCommand(SERVICE_GET_AUDIO_FORMAT, Bundle.EMPTY))


        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
            .setAvailableSessionCommands(availableSessionCommands.build())
            .build()
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        return Futures.immediateFuture(
            when (customCommand.customAction) {
                SERVICE_GET_AUDIO_FORMAT -> {
                    SessionResult(SessionResult.RESULT_SUCCESS).also {
                        it.extras.putBundle("file_format", downstreamFormat?.toBundle())
                        it.extras.putBundle("sink_format", audioSinkInputFormat?.toBundle())
                        it.extras.putParcelable("track_format", audioTrackInfo)
                        it.extras.putParcelable("hal_format", afFormatTracker.format)
                        bitrate?.let { value -> it.extras.putLong("bitrate", value) }
                        if (afFormatTracker.format?.routedDeviceType == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
                            it.extras.putParcelable("bt", btInfo)
                        }
                    }
                }

                else -> {
                    SessionResult(SessionError.ERROR_BAD_VALUE)
                }
            })
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super<Player.Listener>.onMediaItemTransition(mediaItem, reason)
        val prevIndex = mediaSession.player.getPreviousMediaItemIndex()
        val car_lyrics = prefs.getBoolean("car_lyrics", false)

        if (car_lyrics) {
            if (prevIndex != C.INDEX_UNSET) {
                val previousItem = mediaSession.player.getMediaItemAt(prevIndex)
                val sessionMetadata = previousItem.mediaMetadata
                val sessionMediaItem = previousItem
                val te = sessionMediaItem?.songtitle?.toString()
                if (sessionMetadata.title != te) {
                    val newdata = sessionMetadata.buildUpon().setTitle(te).build()
                    val newmedia = sessionMediaItem?.buildUpon()?.setMediaMetadata(newdata)?.build()
                    mediaSession.player.replaceMediaItem(
                        prevIndex,
                        newmedia!!
                    )
                }
            }
        }

        bitrate = null
        bitrateFetcher.launch {
            bitrate = mediaItem?.getBitrate() // TODO subtract cover size
            mediaSession?.broadcastCustomCommand(
                SessionCommand(SERVICE_GET_AUDIO_FORMAT, Bundle.EMPTY),
                Bundle.EMPTY
            )
        }

        val fileName = Tools.sanitizeFileName("${mediaSession.player.currentMediaItem?.mediaId}.lrc")

        val cachedData = Tools.readFromSubdirCache(this.applicationContext, subDir, fileName)

        if (cachedData != null) {

//            Log.d("lyrictest",lyricss.title)
//            Log.d("lyrictest",lyricss.lines.toString())

            val myCustomFormat = LyricsFormatGuesser.LyricsFormat(
                name = "MY_CUSTOM_FORMAT",
                detector = { content ->
                    val a = isEnhancedKrc(content)
                    Log.d("CustomKrcParser",a.toString())
                    a
                }
            )
            val autoParserLyric = AutoParser.Builder()
                .withFormat(myCustomFormat, CustomKrcParser())
                .build()
            val lyricss = autoParserLyric.parse(cachedData)
            MediaViewModelObject.newLrcEntries.value = lyricss

//            MediaViewModelObject.lrcEntries.value =
//                YosLrcFactory(false).formatLrcEntries(Tools.convertKrcToLrc(cachedData))
        } else {
            serviceScope.launch {
                if (!MainActivity.isNodeRunning?: false) return@launch

                val item = mediaSession.player.currentMediaItem
                val hashA = item?.uri?.getQueryParameter("hash") ?: ""
                val lyricId = item?.lrcId.orEmpty()
                val lyricAccess = item?.lrcAccesskey.orEmpty()

                // 1. 先直接用 lyricId + accessKey 尝试获取
                val firstAttempt = fetchLyrics(lyricId, lyricAccess)
                if (firstAttempt != null) {
                    cacheAndLoadLyrics(firstAttempt)
                    return@launch
                }

                // 2. 如果失败，尝试搜索
                val searchJson = withContext(Dispatchers.IO) {
                    KugouAPi.getSearchSongLyrics(hash = hashA)
                }
                if (searchJson.isNullOrEmpty() || searchJson == "502" || searchJson == "404") {
                    return@launch
                }

                try {
                    val searchResult = gson.fromJson(searchJson, searchLyricBase::class.java)
                    val candidate = searchResult.candidates.getOrNull(0)
                    val secondAttempt = fetchLyrics(
                        id = candidate?.id.orEmpty(),
                        accessKey = candidate?.accesskey.orEmpty()
                    )
                    if (secondAttempt != null) {
                        cacheAndLoadLyrics(secondAttempt)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }

    }

    private suspend fun fetchLyrics(id: String, accessKey: String): String? {
        val json = withContext(Dispatchers.IO) {
            KugouAPi.getSongLyrics(id = id, accesskey = accessKey, fmt = "krc", decode = true)
        }
        return try {
            val result = gson.fromJson(json, getLyricCode::class.java)
            result.decodeContent
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun cacheAndLoadLyrics(content: String) {
        
        val fileName = Tools.sanitizeFileName("${mediaSession.player.currentMediaItem?.mediaId}.lrc")
        Tools.writeToSubdirCache(this.applicationContext, subDir, fileName, content.toString())
        Tools.readFromSubdirCache(this.applicationContext, subDir, fileName)?.let { cached ->
//            MediaViewModelObject.lrcEntries.value = YosLrcFactory(false).formatLrcEntries(Tools.convertKrcToLrc(cached))
        }
        val myCustomFormat = LyricsFormatGuesser.LyricsFormat(
            name = "MY_CUSTOM_FORMAT",
            detector = { content ->
                // Example: check for a unique tag
                val a = isEnhancedKrc(content)
                Log.d("CustomKrcParser",a.toString())
               a
            }
        )
        val autoParserLyric = AutoParser.Builder()
            .withFormat(myCustomFormat, CustomKrcParser())
            .build()
        val lyricss = autoParserLyric.parse(content)
        MediaViewModelObject.newLrcEntries.value = lyricss

    }


    override fun onIsPlayingChanged(isPlaying: Boolean) {

        val itemCount = mediaSession.player.mediaItemCount
        mediaItems.value =
            List(itemCount) { index -> mediaSession.player.getMediaItemAt(index) }.toMutableList()
        currentMediaItemIndex.value = mediaSession.player.currentMediaItemIndex

        if (isPlaying) {
            serviceScope.launch {
                if (mediaSession.player.playbackState != Player.STATE_IDLE && mediaSession.player.currentTimeline.isEmpty.not()) {
                    val index = mediaSession.player.currentMediaItemIndex
                    Tools.saveCurrentPlaylist(mediaSession.player, repo)
                    prefs.edit().putInt("lastplayitem", index).apply()
                    Log.d("ExoPlayer", "当前播放索引：$index")
                } else {
                    Log.d("ExoPlayer", "播放列表未就绪")
                }

            }
        }
    }

    override fun onPlaybackStateChanged(state: Int) {
        val car_lyrics = prefs.getBoolean("car_lyrics", false)
        when (state) {
            Player.STATE_IDLE -> println("空闲")
            Player.STATE_BUFFERING -> println("缓冲中")
            Player.STATE_READY -> println("准备好")
            Player.STATE_ENDED -> {
                if (car_lyrics) {
                    val sessionMetadata = mediaSession.player.mediaMetadata
                    val sessionMediaItem = mediaSession.player.currentMediaItem
                    val t = sessionMediaItem?.songtitle?.toString()

                    if (t != sessionMetadata.title){
                        val newdata = sessionMetadata.buildUpon().setTitle(t).build()
                        val newmedia =
                            sessionMediaItem?.buildUpon()?.setMediaMetadata(newdata)?.build()

                        mediaSession.player.replaceMediaItem(
                            mediaSession.player.currentMediaItemIndex,
                            newmedia!!
                        )
                    }
                }
                println("播放结束")
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onAudioTrackInitialized(
        eventTime: AnalyticsListener.EventTime,
        audioTrackConfig: AudioSink.AudioTrackConfig
    ) {
        audioTrackInfoCounter++
        audioTrackInfo = AudioTrackInfo.fromMedia3AudioTrackConfig(audioTrackConfig)
        mediaSession?.broadcastCustomCommand(
            SessionCommand(SERVICE_GET_AUDIO_FORMAT, Bundle.EMPTY),
            Bundle.EMPTY
        )
    }

    override fun onAudioTrackReleased(
        eventTime: AnalyticsListener.EventTime,
        audioTrackConfig: AudioSink.AudioTrackConfig
    ) {
        // Normally called after the replacement has been initialized, but if old track is released
        // without replacement, we want to instantly know that instead of keeping stale data.
        if (++audioTrackReleaseCounter == audioTrackInfoCounter) {
            audioTrackInfo = null
            mediaSession?.broadcastCustomCommand(
                SessionCommand(SERVICE_GET_AUDIO_FORMAT, Bundle.EMPTY),
                Bundle.EMPTY
            )
        }
    }

    override fun onDownstreamFormatChanged(
        eventTime: AnalyticsListener.EventTime,
        mediaLoadData: MediaLoadData
    ) {
        downstreamFormat = mediaLoadData.trackFormat
        mediaSession?.broadcastCustomCommand(
            SessionCommand(SERVICE_GET_AUDIO_FORMAT, Bundle.EMPTY),
            Bundle.EMPTY
        )
    }

    private fun onAudioSinkInputFormatChanged(inputFormat: Format?) {
        audioSinkInputFormat = inputFormat
        mediaSession?.broadcastCustomCommand(
            SessionCommand(SERVICE_GET_AUDIO_FORMAT, Bundle.EMPTY),
            Bundle.EMPTY
        )
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        if (state == Player.STATE_IDLE) {
            downstreamFormat = null
            mediaSession?.broadcastCustomCommand(
                SessionCommand(SERVICE_GET_AUDIO_FORMAT, Bundle.EMPTY),
                Bundle.EMPTY
            )
        }
    }

    fun isEnhancedKrc(content: String): Boolean {
        val lines = content.lines().map { it.trim() }.filter { it.isNotEmpty() }

        // 行时间戳正则 [0,3946]
        val lineTimeRegex = Regex("""^\[\d+,\d+]""")

        // 字时间戳正则 <0,171,0>字
        val wordTimeRegex = Regex("""<\d+,\d+,\d+>.{1}""")

        // 至少有一行符合格式
        return lines.any { line ->
            lineTimeRegex.containsMatchIn(line) && wordTimeRegex.containsMatchIn(line)
        }
    }
}
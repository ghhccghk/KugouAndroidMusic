package com.ghhccghk.musicplay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.IBinder
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.data.libraries.songHash
import com.ghhccghk.musicplay.data.libraries.songtitle
import com.ghhccghk.musicplay.data.objects.MainViewModelObject.currentMediaItemIndex
import com.ghhccghk.musicplay.data.objects.MediaViewModelObject.mediaItems
import com.ghhccghk.musicplay.databinding.ActivityMainBinding
import com.ghhccghk.musicplay.service.PlayService
import com.ghhccghk.musicplay.ui.components.GlobalPlaylistBottomSheetController
import com.ghhccghk.musicplay.ui.components.PlaylistBottomSheet
import com.ghhccghk.musicplay.util.NodeBridge
import com.ghhccghk.musicplay.util.SmartImageCache
import com.ghhccghk.musicplay.util.TokenManager
import com.ghhccghk.musicplay.util.UrlCacheManager
import com.ghhccghk.musicplay.util.ZipExtractor
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.nift4.gramophone.hificore.UacManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    @SuppressLint("UnsafeOptInUsageError")
    private var nodeService: PlayService? = null
    var bound = false
    private val viewModel by viewModels<MainViewModel>()
    var isNodeRunning = false
    lateinit var uacManager: UacManager
        private set

    private val nodeReadyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == NodeBridge.ACTION_NODE_READY) {
                isNodeRunning = true
                viewModel.noderun = isNodeRunning
            }
        }
    }

    private val connection = object : ServiceConnection {
        @UnstableApi
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as PlayService.LocalBinder
            nodeService = binder.getService()
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bound = false
            isNodeRunning = false
            viewModel.noderun = isNodeRunning
            nodeService = null
        }
    }

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        enableEdgeToEdge()
        uacManager = UacManager(this)
        UrlCacheManager.init(this)
        val prefs = getSharedPreferences("play_setting_prefs", Context.MODE_PRIVATE)
        val cacheSizeMB = prefs.getString("image_cache_size", "50")?.toLongOrNull() ?: 950L

        val cacheSizeBytes = cacheSizeMB * 1024 * 1024
        SmartImageCache.init(applicationContext, maxSize = cacheSizeBytes)

        if (isFirstRun(this)) {
            ZipExtractor.extractZipOnFirstRun(this, "api_js.zip", "nodejs_files")
            start()
        } else {
            start()
        }
        when (prefs.getString("theme_mode", "0")) {
            "0" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                Log.d("MainActivity", "theme_mode is ${prefs.getString("theme_mode", "0")}")
            }

            "1" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Log.d("MainActivity", "theme_mode is ${prefs.getString("theme_mode", "0")}")
            }

            "2" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Log.d("MainActivity", "theme_mode is ${prefs.getString("theme_mode", "0")}")
            }
        }
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
        if (isFirstRun(this)) {
            ZipExtractor.extractZipOnFirstRun(this, "api_js.zip", "nodejs_files")
            start()
        } else {
            start()
        }

    }

    fun isFirstRun(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirst = prefs.getBoolean("is_first_run", true)
        if (isFirst) {
            prefs.edit().putBoolean("is_first_run", false).apply()
        }
        return isFirst
    }



    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    override fun onResume() {
        super.onResume()
        val navView: NavigationBarView = binding.navView
        isNodeRunning = viewModel.noderun

        val a = findViewById<BottomNavigationView>(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        if (findNavController(R.id.nav_host_fragment_activity_main).currentDestination?.id == R.id.playerFragment) {
            // 隐藏 BottomNavigationView
            hideBottomNav(a)
            hideLinearLayout(playbar, a)
        }
        if (findNavController(R.id.nav_host_fragment_activity_main).currentDestination?.id == R.id.lyricFragment) {
            hideBottomNav(a)
            hideLinearLayout(playbar, a)
        }
        if (findNavController(R.id.nav_host_fragment_activity_main).currentDestination?.id == R.id.playlistDetailFragment) {
            hideBottomNav(a)
        }


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        navView.setupWithNavController(navController)
        playbar.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.fragment_enter)    // 进入动画
                .setExitAnim(R.anim.fragment_exit)      // 退出动画
                .setPopEnterAnim(R.anim.fragment_pop_enter)  // 弹出时的进入动画（从下往上）
                .setPopExitAnim(R.anim.fragment_pop_exit)    // 弹出时的退出动画（从上往下）
                .build()
            // 跳转到播放 Fragment
            navController.navigate(R.id.playerFragment, null, navOptions)

            // 隐藏 BottomNavigationView
            val a = findViewById<BottomNavigationView>(R.id.nav_view)
            hideBottomNav(a)
            hideLinearLayout(playbar, a)
        }

        playbar.findViewById<ImageButton>(R.id.playerbar_play_pause).setOnClickListener {
            if (controllerFuture.get().isPlaying) {
                controllerFuture.get().pause()
                playbar.findViewById<ImageButton>(R.id.playerbar_play_pause)
                    .setImageResource(R.drawable.ic_play_arrow_filled)
            } else {
                controllerFuture.get().play()
                playbar.findViewById<ImageButton>(R.id.playerbar_play_pause)
                    .setImageResource(R.drawable.ic_pause_filled)
            }
        }
        controllerFuture.addListener({
            val player = controllerFuture.get()  // 此时 get() 安全：在后台线程

            val artlurl = player.mediaMetadata?.artworkUri.toString()
            val playbaricon = playbar.findViewById<ImageView>(R.id.player_album)
            if (artlurl.isNullOrBlank() || artlurl == "" || artlurl == "null") {
                Glide.with(playbar)
                    .load(R.drawable.lycaon_icon)
                    .into(playbaricon)
            } else {
                val hash = player.currentMediaItem?.songHash
                lifecycleScope.launch {
                    val fileUrl = withContext(Dispatchers.IO){
                        SmartImageCache.getOrDownload(artlurl.toString(), hash)
                    }
                    Glide.with(playbar)
                        .load(fileUrl)
                        .into(playbaricon)
                }
            }

            val maxHeight = (Resources.getSystem().displayMetrics.heightPixels * 0.8).toInt()
            binding.comui.layoutParams.height = maxHeight


            val itemCount = player.mediaItemCount
            mediaItems.value =
                List(itemCount) { index -> player.getMediaItemAt(index) }.toMutableList()


            binding.comui.setContent {
                Setplaylistui(player)
            }



            player.addListener(object : Player.Listener {
                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    super.onMediaMetadataChanged(mediaMetadata)
                    val title = controllerFuture.get().currentMediaItem?.songtitle
                    val artlurl = player.mediaMetadata?.artworkUri.toString()
                    if (artlurl.isNullOrBlank() || artlurl == "" || artlurl == "null") {
                        Glide.with(playbar)
                            .load(R.drawable.lycaon_icon)
                            .into(playbaricon)
                    } else {
                        val hash = player.currentMediaItem?.songHash
                        lifecycleScope.launch {
                            val fileUrl = withContext(Dispatchers.IO){
                                SmartImageCache.getOrDownload(artlurl.toString(), hash)
                            }
                            Glide.with(playbar)
                                .load(fileUrl)
                                .into(playbaricon)
                        }
                    }

                    playbar.findViewById<TextView>(R.id.playbar_title).text =
                        if (title.isNullOrBlank()) "未知歌曲" else title
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    val artlurl = player.mediaMetadata?.artworkUri.toString()
                    playbar.findViewById<ImageButton>(R.id.playerbar_play_pause).setImageResource(
                        if (isPlaying) {
                            R.drawable.ic_pause_filled
                        } else {
                            val artist = controllerFuture.get().mediaMetadata.artist
                            playbar.findViewById<TextView>(R.id.playbar_artist).text =
                                if (artist.isNullOrBlank()) "未知艺术家" else artist
                            R.drawable.ic_play_arrow_filled
                        }
                    )
                    if (artlurl == "" || artlurl == "null") {
                        Glide.with(playbar)
                            .load(R.drawable.lycaon_icon)
                            .into(playbaricon)
                    } else {
                        val hash = player.currentMediaItem?.songHash
                        lifecycleScope.launch {
                            val fileUrl = withContext(Dispatchers.IO){
                                SmartImageCache.getOrDownload(artlurl.toString(), hash)
                            }
                            Glide.with(playbar)
                                .load(fileUrl)
                                .into(playbaricon)
                        }
                    }
                }
            })
        }, ContextCompat.getMainExecutor(this))

        binding.playerBar.findViewById<ImageButton>(R.id.playerbar_play_next).setOnClickListener {
            controllerFuture.get().seekToNext()
        }


    }

    // 处理返回时的操作，确保返回时显示 BottomNavigationView
    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // 只有当不在播放 Fragment 时才显示 BottomNavigationView
        if (findNavController(R.id.nav_host_fragment_activity_main).currentDestination?.id == R.id.playerFragment) {
            super.onBackPressed()
            val navView = findViewById<BottomNavigationView>(R.id.nav_view)
            showBottomNav(navView)
            val playbar = findViewById<LinearLayout>(R.id.player_bar)
            showLinearLayout(playbar, navView)
        } else {
            // 只有当不在playlistDetailFragment 时才显示 BottomNavigationView
            if (findNavController(R.id.nav_host_fragment_activity_main).currentDestination?.id == R.id.playlistDetailFragment) {
                val navView = findViewById<BottomNavigationView>(R.id.nav_view)
                showBottomNav(navView)
                super.onBackPressed()
            } else {
                super.onBackPressed()  // 调用系统的默认行为
            }
        }
    }

    private fun showBottomNav(bottomNav: BottomNavigationView) {
        bottomNav.visibility = View.VISIBLE
        val slideIn =
            ObjectAnimator.ofFloat(bottomNav, "translationY", bottomNav.height.toFloat(), 0f)
        slideIn.duration = 100
        slideIn.start()

    }

    private fun hideBottomNav(bottomNav: BottomNavigationView) {
        val slideOut =
            ObjectAnimator.ofFloat(bottomNav, "translationY", 0f, bottomNav.height.toFloat())
        slideOut.duration = 100
        slideOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                bottomNav.visibility = View.GONE
            }
        })
        slideOut.start()
    }

    private fun showLinearLayout(bottomNav: LinearLayout, play: BottomNavigationView) {
        bottomNav.visibility = View.VISIBLE
        val slideIn = ObjectAnimator.ofFloat(
            bottomNav,
            "translationY",
            bottomNav.height.toFloat() + play.height.toFloat(),
            0f
        )
        slideIn.duration = 100
        slideIn.start()
    }

    private fun hideLinearLayout(bottomNav: LinearLayout, play: BottomNavigationView) {
        val slideOut = ObjectAnimator.ofFloat(
            bottomNav,
            "translationY",
            0f,
            bottomNav.height.toFloat() + play.height.toFloat()
        )
        slideOut.duration = 100
        slideOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                bottomNav.visibility = View.GONE
                play.visibility = View.GONE
            }
        })
        slideOut.start()
    }

    companion object {
        private lateinit var instance: MainActivity
        val lontext: Context
            get() = instance.applicationContext
        var isNodeRunning: Boolean
            get() = instance.isNodeRunning
            set(value) {
                instance.isNodeRunning = value
            }
        val controllerFuture: ListenableFuture<MediaController>
            get() = instance.viewModel.controllerFuture
        val playbar: LinearLayout
            get() = instance.findViewById<LinearLayout>(R.id.player_bar)
    }


    @OptIn(UnstableApi::class)
    fun start() {
        TokenManager.init(this)
        KugouAPi.init()

        val filter = IntentFilter(NodeBridge.ACTION_NODE_READY)
        LocalBroadcastManager.getInstance(this).registerReceiver(nodeReadyReceiver, filter)
        Intent(this, PlayService::class.java).also {
            bindService(it, connection, BIND_AUTO_CREATE)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 启动 Service
        // 初始化媒体控制器
        val sessionToken = SessionToken(this, ComponentName(this, PlayService::class.java))
        viewModel.controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        val navView: NavigationBarView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        navView.setupWithNavController(navController)
        playbar.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.fragment_enter)    // 进入动画
                .setExitAnim(R.anim.fragment_exit)      // 退出动画
                .setPopEnterAnim(R.anim.fragment_pop_enter)  // 弹出时的进入动画（从下往上）
                .setPopExitAnim(R.anim.fragment_pop_exit)    // 弹出时的退出动画（从上往下）
                .build()
            // 跳转到播放 Fragment
            navController.navigate(R.id.playerFragment, null, navOptions)

            // 隐藏 BottomNavigationView
            val a = findViewById<BottomNavigationView>(R.id.nav_view)
            hideBottomNav(a)
            hideLinearLayout(playbar, a)
        }

        playbar.findViewById<ImageButton>(R.id.playerbar_play_pause).setOnClickListener {
            if (controllerFuture.get().isPlaying) {
                controllerFuture.get().pause()
                playbar.findViewById<ImageButton>(R.id.playerbar_play_pause)
                    .setImageResource(R.drawable.ic_play_arrow_filled)
            } else {
                controllerFuture.get().play()
                playbar.findViewById<ImageButton>(R.id.playerbar_play_pause)
                    .setImageResource(R.drawable.ic_pause_filled)
            }
        }

        binding.playerbarPlaylist.setOnClickListener {
            GlobalPlaylistBottomSheetController.show()
        }

        controllerFuture.addListener({
            val player = controllerFuture.get()  // 此时 get() 安全：在后台线程

            val itemCount = player.mediaItemCount
            mediaItems.value =
                List(itemCount) { index -> player.getMediaItemAt(index) }.toMutableList()
            currentMediaItemIndex.value = player.currentMediaItemIndex

            binding.comui.setContent {
                Setplaylistui(player)
            }

            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    val itemCount = player.mediaItemCount
                    mediaItems.value =
                        List(itemCount) { index -> player.getMediaItemAt(index) }.toMutableList()
                    currentMediaItemIndex.value = player.currentMediaItemIndex
                }

                override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
                    super.onPlaylistMetadataChanged(mediaMetadata)
                    val itemCount = player.mediaItemCount
                    mediaItems.value =
                        List(itemCount) { index -> player.getMediaItemAt(index) }.toMutableList()
                    currentMediaItemIndex.value = player.currentMediaItemIndex

                }

                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    super.onMediaMetadataChanged(mediaMetadata)
                    val itemCount = player.mediaItemCount
                    mediaItems.value =
                        List(itemCount) { index -> player.getMediaItemAt(index) }.toMutableList()
                    currentMediaItemIndex.value = player.currentMediaItemIndex

                }
            })

            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    playbar.findViewById<ImageButton>(R.id.playerbar_play_pause)
                        .setImageResource(
                            if (isPlaying) R.drawable.ic_pause_filled else R.drawable.ic_play_arrow_filled
                        )
                }
            })
        }, ContextCompat.getMainExecutor(this))
    }

    fun isDarkMode(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode
        val nightMode = uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightMode == Configuration.UI_MODE_NIGHT_YES
    }

    @Composable
    fun Setplaylistui(player: Player){
        PlaylistBottomSheet(
            controller = GlobalPlaylistBottomSheetController,
            songs = { mediaItems.value },
            onDismissRequest = {
                GlobalPlaylistBottomSheetController._visible.value = false
            },
            onSongClick = { i, _ ->
                player.seekTo(i, 0)
                player.play()
            },
            onDeleteClick = { i, _ ->
                player.removeMediaItem(i)
                Toast.makeText(
                    lontext,
                    "已删除了 ${mediaItems.value[i].mediaMetadata.title}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            currentIndex = {
                currentMediaItemIndex.value
            }
        )
    }


}
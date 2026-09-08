package com.ghhccghk.musicplay.ui.playlistdetail

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.media3.session.MediaController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.songurl.getsongurl.GetSongUrlBase
import com.ghhccghk.musicplay.data.user.playListDetail.PlayListDetail
import com.ghhccghk.musicplay.data.user.playListDetail.songlist.Song
import com.ghhccghk.musicplay.util.MediaHelp.createMediaItemWithId
import com.ghhccghk.musicplay.util.MediaHelp.getAllSongsFlow
import com.ghhccghk.musicplay.util.MediaHelp.splitArtistAndTitle
import com.ghhccghk.musicplay.util.MediaHelp.toMediaItemListParallel
import com.ghhccghk.musicplay.util.SmartImageCache
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

/**
 * 使用 Compose 重写的歌单详情页面
 * 要点：
 * - 保留原有逻辑（网络请求、MediaController 操作），以保证兼容性
 * - 使用 Compose 的 State / LazyColumn 实现更高性能的渲染与按需加载
 * - 使用 AndroidView + Glide 来加载图片，避免引入额外图片库依赖
 * - 模块化：分离 Header、SongItem、Screen 三个可复用 composable
 * - 中文注释说明每一部分目的与性能考虑
 */
class PlaylistDetailFragment : Fragment() {

    private lateinit var player: MediaController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 主线程同步获取 controller（和原来行为一致）
        player = MainActivity.controllerFuture.get()
        // 呼叫 init 保证 Api 尝试检索 dfid（无副作用即安全多次调用）
        KugouAPi.init()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val composeView = ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(
                    colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (isSystemInDarkTheme()) dynamicDarkColorScheme(LocalContext.current)
                    else dynamicLightColorScheme(LocalContext.current)
                } else {
                    if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
                }) {
                    Surface(modifier = Modifier.padding(start = 6.dp, end = 6.dp)) {
                        PlaylistDetailScreen(
                            playlistId = arguments?.getString("playlistId"),
                            picurl = arguments?.getString("picurl"),
                            onAddMediaItem = { mediaItem ->
                                // 调用主线程的 controller 操作
                                val controller = MainActivity.controllerFuture.get()
                                try {
                                    val currentIndex = controller.currentMediaItemIndex
                                    controller.addMediaItem(currentIndex + 1, mediaItem)
                                } catch (t: Throwable) {
                                    Log.w("PlaylistDetail", "addMediaItem failed", t)
                                }
                            },
                            onSetMediaItems = { items ->
                                val controller = MainActivity.controllerFuture.get()
                                controller.setMediaItems(items)
                            },
                            onBackPressed = {
                                // 使用 Fragment 返回栈管理
                                parentFragmentManager.popBackStack()
                            }
                        )
                    }
                }
            }
        }
        return composeView
    }

}

// ------------------------- Composables -------------------------

/**
 * Screen 根 composable，负责协调数据与状态
 * 设计：尽量使用 remember/LaunchedEffect 来控制副作用，按需加载数据，减少重组开销
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: String?,
    picurl: String?,
    onAddMediaItem: (androidx.media3.common.MediaItem) -> Unit,
    onSetMediaItems: (List<androidx.media3.common.MediaItem>) -> Unit,
    // 新增可选长按回调（默认空实现）
    onSongLongPress: (Song) -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    val context = LocalContext.current

    // 页面状态
    var loading by remember { mutableStateOf(true) }
    var playlistName by remember { mutableStateOf("") }
    var playlistCreator by remember { mutableStateOf("") }
    var playlistIntro by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf<String?>(null) }
    val songs = remember { mutableStateListOf<Song>() }

    // 用于在回调里启动协程（避免在 lambda 里调用 rememberCoroutineScope）
    val clickScope = rememberCoroutineScope()

    // 启动时加载歌单信息与曲目流（异步、IO 调度）
    LaunchedEffect(playlistId) {
        if (playlistId.isNullOrBlank()) {
            Toast.makeText(context, "数据加载失败: playlistId 为空", Toast.LENGTH_SHORT).show()
            loading = false
            return@LaunchedEffect
        }

        loading = true
        val id = playlistId
        // 1) 获取歌单详情
        val json = withContext(Dispatchers.IO) {
            try {
                KugouAPi.getPlayListDetail(id)
            } catch (t: Throwable) {
                Log.w("PlaylistDetail", "getPlayListDetail error", t)
                null
            }
        }

        if (json == null || json == "502" || json == "404") {
            Toast.makeText(context, "数据加载失败", Toast.LENGTH_LONG).show()
            loading = false
            return@LaunchedEffect
        }

        try {
            val moshi = Moshi.Builder().build()
            val result = moshi.adapter(PlayListDetail::class.java).fromJson(json!!)
            val playList = result?.data[0]
            playlistName = playList?.name ?: ""
            playlistCreator = playList?.list_create_username ?: ""
            playlistIntro = playList?.intro ?: ""
            // 优先使用传入的 picurl（可能为 null），否则从数据里获取并修正
            coverUrl =
                if (!picurl.isNullOrBlank() && picurl != "null") picurl else playList?.create_user_pic?.replaceFirst(
                    "/{size}/",
                    "/"
                )
        } catch (e: Exception) {
            Log.e("PlaylistDetail", "parse playlist detail", e)
            Toast.makeText(context, "数据加载失败: ${e.message}", Toast.LENGTH_LONG).show()
            loading = false
            return@LaunchedEffect
        }

        // 2) 获取曲目列表流并收集（底层 Flow 每次 emit 单首 Song）
        try {
            // getAllSongsFlow 已在内部使用 flowOn(Dispatchers.IO)，这里直接 collect 保证在主线程更新 UI 状态
            getAllSongsFlow(id, 30).collect { song ->
                songs.add(song)
            }
        } catch (t: Throwable) {
            Log.w("PlaylistDetail", "collect songs flow failed", t)
        }

        loading = false
    }

    // 布局：Header + 歌曲列表
    Column(modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding()) {
        // 顶部 Toolbar（Material3），避开状态栏开孔和沉浸式状态栏
        TopAppBar(
            title = { Text(text = "歌单详情") },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            }
        )

        // 内容区：使用 LazyColumn 统一管理 Header 和歌曲列表，避免嵌套滚动问题
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp)) {
                // Header 区域
                item {
                    PlaylistHeader(
                        coverUrl = coverUrl,
                        name = playlistName,
                        creator = playlistCreator,
                        intro = playlistIntro
                    )
                }

                // 按钮区域
                item {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        androidx.compose.material3.Button(
                            onClick = {
                                // 一键播放：转换当前 songs 为 MediaItem 并设置（toMediaItemListParallel 为 suspend）
                                clickScope.launch {
                                    try {
                                        val items = withContext(Dispatchers.Default) { songs.toMediaItemListParallel() }
                                        onSetMediaItems(items)
                                    } catch (e: Exception) {
                                        Log.w("PlaylistDetail", "toMediaItemListParallel failed", e)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "播放全部")
                        }

                        androidx.compose.material3.OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "已加入播放队列", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "加入队列")
                        }
                    }
                }

                // 歌曲列表
                itemsIndexed(items = songs) { index, song ->
                    SongRow(
                        song = song,
                        index = index,
                        onSongClick = { s ->
                            // 点击单首：后台请求获取 url 并调用 onAddMediaItem
                            clickScope.launch {
                                val json = withContext(Dispatchers.IO) {
                                    s.hash?.let { h -> KugouAPi.getSongsUrl(h) }
                                }
                                if (json == null || json == "502" || json == "404") {
                                    Toast.makeText(context, "Song 数据加载失败", Toast.LENGTH_LONG).show()
                                } else {
                                    try {
                                        val moshi = Moshi.Builder().build()
                                        val result = moshi.adapter(GetSongUrlBase::class.java)
                                            .fromJson(json!!)
                                        val url =
                                            result?.url?.getOrNull(1) ?: result?.url?.getOrNull(0)
                                            ?: result?.backupUrl?.getOrNull(1)
                                            ?: result?.backupUrl?.getOrNull(0) ?: ""

                                        val mixsongid = s.add_mixsongid ?: ""
                                        val encodedUrl = URLEncoder.encode(url, "UTF-8")
                                        val uri = "musicplay://playurl?id=${s.name + s.hash}&url=${encodedUrl}&hash=${s.hash}".toUri().toString()
                                        val name = s.name?.let { splitArtistAndTitle(it) }
                                        if (result != null) {
                                            val item = createMediaItemWithId(
                                                name?.first,
                                                name?.second,
                                                uri,
                                                result,
                                                result.hash,
                                                mixsongid.toString()
                                            )
                                            onAddMediaItem(item)
                                        }

                                    } catch (e: Exception) {
                                        Log.e("PlaylistDetail", "parse song url", e)
                                        Toast.makeText(context, "数据加载失败: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        onSongLongClick = { s ->
                            // 默认行为：调用长按回调（可由上层覆盖）
                            onSongLongPress(s)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Header：歌单封面/标题/作者/简介 + 操作按钮
 * MD3 风格：使用主题色、优化间距，按钮放在文字下方
 * 性能点：使用 AndroidView + Glide 异步加载图片并缓存到 SmartImageCache（与原代码一致）
 */
@Composable
fun PlaylistHeader(coverUrl: String?, name: String, creator: String, intro: String) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 8.dp)) {
        // 上方：封面 + 信息行
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically) {
            // 使用 AndroidView + Glide 加载图片，兼容旧项目无需引入 Coil
            AndroidImage(
                url = coverUrl,
                contentDescription = "playlist cover",
                modifier = Modifier
                    .size(100.dp)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier
                .weight(1f), verticalArrangement = Arrangement.Center) {
                // 歌单名称：标题字体稍小，控制行数
                Text(
                    text = name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 创建者信息：使用次要文本样式
                Text(
                    text = "创建者: $creator",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 简介：更小的字体，更柔和的颜色
                Text(
                    text = intro,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * AndroidView 包装的图片加载器，使用 Glide 异步加载并保持与 SmartImageCache 协同
 * 处理要点：
 * - SmartImageCache.getOrDownload 是 suspend 的，需要在 LaunchedEffect 中调用
 * - 使用 state 保存最终要加载的 URL，再在 AndroidView 的 update 中使用 Glide 加载
 */
@Composable
fun AndroidImage(url: String?, contentDescription: String?, modifier: Modifier = Modifier) {
    var loadTargetUrl by remember { mutableStateOf<String?>(null) }
    val finalUrl = url

    LaunchedEffect(finalUrl) {
        if (finalUrl.isNullOrBlank() || finalUrl == "null") {
            loadTargetUrl = null
        } else {
            try {
                // 清理 URL：删除占位符 {size}，以及多余的斜杠段，避免 Glide 解析尺寸占位出错
                val sanitized = finalUrl.replace(Regex("\\{size\\}"), "")
                    .replace("//", "/").replace(":/", "://")

                val cached = SmartImageCache.getOrDownload(sanitized, sanitized.hashCode().toString())
                loadTargetUrl = cached?.toString() ?: sanitized
            } catch (e: Exception) {
                Log.w("AndroidImage", "SmartImageCache failed", e)
                // fallback: still try original finalUrl if sanitizing/cache failed
                loadTargetUrl = finalUrl
            }
        }
    }

    AndroidView(factory = { ctx ->
        val iv = android.widget.ImageView(ctx)
        iv.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        iv.contentDescription = contentDescription
        iv
    }, update = { imageView ->
        if (loadTargetUrl.isNullOrBlank()) {
            val drawable = AppCompatResources.getDrawable(imageView.context, R.drawable.ic_favorite_filled)
            imageView.setImageBitmap(drawable?.toBitmap())
            return@AndroidView
        }

        // 计算圆角半径（以 dp 为单位，这里使用 12.dp 大小，转换为 px）
        val radiusPx = (12 * imageView.context.resources.displayMetrics.density).toInt()
        val requestOptions = RequestOptions().transform(RoundedCorners(radiusPx))

        Glide.with(imageView.context)
            .asBitmap()
            .load(loadTargetUrl)
            .apply(requestOptions)
            .into(object : CustomTarget<android.graphics.Bitmap>() {
                override fun onResourceReady(resource: android.graphics.Bitmap, transition: Transition<in android.graphics.Bitmap>?) {
                    imageView.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                    // no-op
                }

                override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    val drawable = AppCompatResources.getDrawable(imageView.context, R.drawable.ic_favorite_filled)
                    imageView.setImageBitmap(drawable?.toBitmap())
                }
            })
    }, modifier = modifier)
}


/**
 * 单首行视图：显示标题和艺术家，点击执行回调
 * MD3 风格：紧凑间距，合理的 padding，使用主题颜色
 */
@Composable
fun SongRow(song: Song, index: Int, onSongClick: (Song) -> Unit, onSongLongClick: (Song) -> Unit = {}) {
    val artistText = song.singerinfo?.firstOrNull()?.name ?: "未知艺术家"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onSongLongClick(song) },
                    onTap = { onSongClick(song) }
                )
            }
    ) {
        Row(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 序号：使用固定宽度，文字居中对齐
            Text(
                text = "${index + 1}",
                modifier = Modifier
                    .width(28.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 歌曲封面缩略图：小号尺寸，圆角处理，宽高比为正方形
            AndroidImage(
                url = song.cover,
                contentDescription = song.name ?: "歌曲封面",
                modifier = Modifier
                    .size(55.dp)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // 歌曲信息列
            Column(modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                // 歌曲名称
                Text(
                    text = song.name ?: "未知歌曲",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 艺术家信息：更小更柔和的样式
                Text(
                    text = artistText,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


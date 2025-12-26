package com.ghhccghk.musicplay.ui.components

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.libraries.songHash
import com.ghhccghk.musicplay.data.libraries.songtitle
import com.ghhccghk.musicplay.data.objects.MainViewModelObject
import com.ghhccghk.musicplay.util.SmartImageCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object GlobalPlaylistBottomSheetController : PlaylistBottomSheetController()


open class PlaylistBottomSheetController {
    internal val _visible = MainViewModelObject._visible
    val visible: State<Boolean> get() = _visible

    fun show() {
        _visible.value = true
    }

    fun hide() {
        _visible.value = false
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistBottomSheet(
    controller: PlaylistBottomSheetController = remember { PlaylistBottomSheetController() },
    player: Player,
    onDismissRequest: () -> Unit,
    onSongClick: (index: Int, song: MediaItem) -> Unit,
    onDeleteClick: (index: Int, song: MediaItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val listState = rememberLazyListState()

    // SnapshotStateList 来维护 UI 列表
    val mediaItems = remember {
        mutableStateListOf<MediaItem>().apply {
            addAll(List(player.mediaItemCount) { index -> player.getMediaItemAt(index) })
        }
    }

    // currentIndex 自动刷新
    val currentIndex by player.currentMediaItemIndexAsState()

    // 拖拽状态
    val dragDropState = remember { DragDropState(listState, player, mediaItems) }

    // 每次 ExoPlayer playlist 改变时，同步 UI
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)
                mediaItems.clear()
                mediaItems.addAll(List(player.mediaItemCount) { index -> player.getMediaItemAt(index) })
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(controller.visible.value, currentIndex) {
        if (controller.visible.value) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    if (!controller.visible.value) return

    MaterialTheme(
        colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isSystemInDarkTheme()) dynamicDarkColorScheme(MainActivity.lontext)
            else dynamicLightColorScheme(MainActivity.lontext)
        } else {
            if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
        }
    ) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState
        ) {
            val currentSong = mediaItems.getOrNull(currentIndex)
            val currentTitle = currentSong?.songtitle.orEmpty()
            val currentArtwork = currentSong?.mediaMetadata?.artworkUri

            ListItem(
                modifier = Modifier.fillMaxWidth(),
                leadingContent = {
                    if (currentArtwork != null) RotatingArtwork(currentArtwork, currentSong?.songHash)
                    else Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(40.dp))
                },
                headlineContent = {
                    Text(
                        text = if (currentTitle.isNotBlank()) stringResource(R.string.playlist_now_playing, currentTitle)
                        else stringResource(R.string.playlist),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(currentTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f)
                )
            )

            Divider()

            LazyColumn(
                state = listState,
                modifier = Modifier.pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            val item = listState.layoutInfo.visibleItemsInfo.firstOrNull {
                                offset.y.toInt() in it.offset..(it.offset + it.size)
                            }
                            item?.let { dragDropState.onDragStart(it.index) }
                        },
                        onDrag = { _, dragAmount -> dragDropState.onDrag(dragAmount.y) },
                        onDragEnd = { dragDropState.onDragEnd() },
                        onDragCancel = { dragDropState.onDragEnd() }
                    )
                }
            ) {
                items(mediaItems.size, key = { mediaItems[it].mediaId }) { index ->
                    val song = mediaItems[index]
                    val isSelected = index == currentIndex
                    val isDragging = dragDropState.draggingItemIndex == index

                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                if (isDragging) {
                                    scaleX = 1.02f
                                    scaleY = 1.02f
                                    shadowElevation = 12f
                                }
                            }
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { onSongClick(index, song) },
                        leadingContent = {
                            if (song.mediaMetadata.artworkUri != null) {
                                RotatingArtwork(song.mediaMetadata.artworkUri, song.songHash)
                            } else {
                                Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(40.dp))
                            }
                        },
                        headlineContent = { Text(song.songtitle.toString(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        supportingContent = { Text(song.mediaMetadata.artist.toString(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        trailingContent = {
                            IconButton(onClick = { onDeleteClick(index, song) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(id = R.string.delete)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

// 拖拽状态
class DragDropState(
    private val listState: LazyListState,
    private val player: Player,
    private val mediaItems: SnapshotStateList<MediaItem>
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    private var dragOffset by mutableStateOf(0f)

    fun onDragStart(index: Int) {
        draggingItemIndex = index
        dragOffset = 0f
    }

    fun onDrag(delta: Float) {
        dragOffset += delta
        val draggingIndex = draggingItemIndex ?: return
        val visibleItems = listState.layoutInfo.visibleItemsInfo

        val draggingItem = visibleItems.firstOrNull { it.index == draggingIndex } ?: return
        val draggingItemCenter = draggingItem.offset + dragOffset + draggingItem.size / 2f

        val targetItem = visibleItems.firstOrNull {
            val start = it.offset.toFloat()
            val end = start + it.size
            draggingItemCenter in start..end && it.index != draggingIndex
        }

        if (targetItem != null) {
            // 拖拽同步 ExoPlayer
            player.moveMediaItems(draggingIndex, draggingIndex + 1, if (targetItem.index > draggingIndex) targetItem.index + 1 else targetItem.index)
            // 同步 UI
            val item = mediaItems.removeAt(draggingIndex)
            mediaItems.add(targetItem.index, item)

            draggingItemIndex = targetItem.index
            dragOffset = 0f
        }
    }

    fun onDragEnd() {
        draggingItemIndex = null
        dragOffset = 0f
    }
}

// currentIndex 监听扩展
@Composable
fun Player.currentMediaItemIndexAsState(): State<Int> {
    val state = remember { mutableStateOf(currentMediaItemIndex) }

    DisposableEffect(this) {
        val listener = object : Player.Listener {
            override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
                state.value = currentMediaItemIndex
            }
        }
        addListener(listener)
        onDispose { removeListener(listener) }
    }

    return state
}


@Composable
fun RotatingArtwork(uri: Uri?,hash: String?) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer {
                rotationZ = 0f
            }
            .clip(RoundedCornerShape(8.dp)) // 👈 设置圆角半径
    ) {
        GlideComposeImage(url = uri.toString(), hash = hash ,modifier = Modifier.fillMaxSize(), circleCrop = false)
    }
}


@SuppressLint("CheckResult")
@Composable
fun GlideComposeImage(
    url: String?,
    hash: String?,
    modifier: Modifier = Modifier,
    placeholderResId: Int? = null,
    errorResId: Int? = null,
    circleCrop: Boolean = false,
) {
    var fileUrl by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(url) {
        fileUrl = withContext(Dispatchers.IO) {
            SmartImageCache.getOrDownload(url.toString(), hash.toString())
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        update = { imageView ->
            val request = Glide.with(imageView.context)
                .load(fileUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)

            if (placeholderResId != null) {
                request.placeholder(placeholderResId)
            }
            if (errorResId != null) {
                request.error(errorResId)
            }
            if (circleCrop) {
                request.apply(RequestOptions.circleCropTransform())
            }

            request.into(imageView)
        }
    )
}

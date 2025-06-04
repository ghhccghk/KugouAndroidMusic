package com.ghhccghk.musicplay.ui.components

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import com.ghhccghk.musicplay.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ghhccghk.musicplay.MainActivity


object GlobalPlaylistBottomSheetController : PlaylistBottomSheetController()


open class PlaylistBottomSheetController {
    internal val _visible = mutableStateOf(false)
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
    songs: List<MediaItem>,
    onDismissRequest: () -> Unit,
    onSongClick: (index: Int, song: MediaItem) -> Unit,
    onDeleteClick: (index: Int, song: MediaItem) -> Unit,
    currentIndex: () -> Int
) {
    val sheetState = rememberModalBottomSheetState()
    val listState = rememberLazyListState()
    val a = currentIndex()

    LaunchedEffect(currentIndex, controller.visible.value) {
        if (controller.visible.value && a != null) {
            listState.animateScrollToItem(a)
        }
    }

    if (controller.visible.value) {
        MaterialTheme(
            colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isSystemInDarkTheme()) dynamicDarkColorScheme(MainActivity.lontext) else dynamicLightColorScheme(MainActivity.lontext)
            } else {
                if (isSystemInDarkTheme()){
                    darkColorScheme()    // 静态深色方案
                } else{
                    lightColorScheme()   // 静态亮色方案
                }
            }
        ){
            ModalBottomSheet(
                onDismissRequest = onDismissRequest,
                modifier = Modifier
                    .padding(WindowInsets.statusBars.asPaddingValues()),
                sheetState = sheetState,
            ) {
                val currentSong = songs.getOrNull(currentIndex())
                val currentTitle = currentSong?.mediaMetadata?.title?.toString().orEmpty()
                val currentArtwork = currentSong?.mediaMetadata?.artworkUri?.toString()

                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                    ,
                    leadingContent = {
                        if (currentArtwork != null) {
                            RotatingArtwork(
                                uri = currentSong.mediaMetadata.artworkUri,
                                isPlaying = false
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = stringResource(id = R.string.music_icon),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    },
                    headlineContent = {
                        Text(
                            text = if (currentTitle.isNotBlank()) {
                                stringResource(id = R.string.playlist_now_playing, currentTitle)
                            } else {
                                stringResource(id = R.string.playlist)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    supportingContent = {
                        Text(
                            currentTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.0f)
                    )
                )

                Divider()

                LazyColumn(state = listState) {
                    items(songs.size) { index ->
                        val song = songs[index]
                        val isSelected = index == a

                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    else
                                        Color.Transparent
                                )
                                .clickable { onSongClick(index, song) },
                            leadingContent = {
                                if (song.mediaMetadata.artworkUri != null) {
                                    RotatingArtwork(
                                        uri = song.mediaMetadata.artworkUri,
                                        isPlaying = false
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = stringResource(id = R.string.music_icon),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            },
                            headlineContent = {
                                Text(
                                    song.mediaMetadata.title.toString(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingContent = {
                                Text(
                                    song.mediaMetadata.artist.toString(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
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
}

@Composable
fun RotatingArtwork(uri: Uri?, isPlaying: Boolean) {
    val rotation = rememberInfiniteTransition()
    val angle by rotation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing))
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer {
                rotationZ = if (isPlaying) angle else 0f
            }
    ) {
        GlideComposeImage(url = uri.toString(), modifier = Modifier.fillMaxSize(), circleCrop = true)
    }
}


@SuppressLint("CheckResult")
@Composable
fun GlideComposeImage(
    url: String?,
    modifier: Modifier = Modifier,
    placeholderResId: Int? = null,
    errorResId: Int? = null,
    circleCrop: Boolean = false,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        update = { imageView ->
            val request = Glide.with(imageView.context)
                .load(url)
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

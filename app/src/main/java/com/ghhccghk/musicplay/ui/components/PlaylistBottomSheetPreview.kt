package com.ghhccghk.musicplay.ui.components

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
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
    songs: () -> List<MediaItem>,
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
                val currentSong by remember {
                    derivedStateOf {
                        val index = currentIndex()
                        songs().getOrNull(index)
                    }
                }
                val songs = songs()

                val currentTitle = currentSong?.songtitle?.toString().orEmpty()
                val currentArtwork = currentSong?.mediaMetadata?.artworkUri?.toString()

                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                    ,
                    leadingContent = {
                        if (currentArtwork != null) {
                            RotatingArtwork(
                                uri = currentSong?.mediaMetadata?.artworkUri,
                                hash = currentSong?.songHash
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
                                        hash = song.songHash
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
                                    song.songtitle.toString(),
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

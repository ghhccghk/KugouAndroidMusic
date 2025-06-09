package com.ghhccghk.musicplay.data.objects

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R

@Stable
object MediaViewModelObject {
    val lrcEntries: MutableState<List<List<Pair<Float, String>>>> = mutableStateOf(listOf())
    val otherSideForLines = mutableStateListOf<Boolean>()

    // var mainLyricLines = mutableStateListOf<AnnotatedString>()
    val showControl: MutableState<Boolean> = mutableStateOf(false)

    val bitrate = mutableIntStateOf(0)

    //选中字体颜色
    val colorOnSecondaryContainerFinalColor = mutableIntStateOf(ContextCompat.getColor(MainActivity.lontext,R.color.lyric_main_bg))
    //未选中字体颜色
    val colorSecondaryContainerFinalColor = mutableIntStateOf(ContextCompat.getColor(MainActivity.lontext,R.color.lyric_sub_bg))
    //背景色
    val surfaceTransition = mutableIntStateOf(Color.Black.toArgb())

    val mediaItems = mutableStateOf(mutableListOf<MediaItem>())

    // val songSort = mutableStateOf(SettingData.getString("yos_player_song_sort", "MUSIC_TITLE"))
    // val enableDescending = mutableStateOf(SettingData.get("yos_player_enable_descending", false))
}
package com.ghhccghk.musicplay.data.objects

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

@Stable
object MediaViewModelObject {
    val lrcEntries: MutableState<List<List<Pair<Float, String>>>> = mutableStateOf(listOf())
    val otherSideForLines = mutableStateListOf<Boolean>()

    // var mainLyricLines = mutableStateListOf<AnnotatedString>()
    val showControl: MutableState<Boolean> = mutableStateOf(false)

    val bitrate = mutableIntStateOf(0)

    val colorOnSecondaryContainerFinalColor = mutableIntStateOf(Color.Black.toArgb())

    val colorSecondaryContainerFinalColor = mutableIntStateOf(Color.Black.toArgb())


    // val songSort = mutableStateOf(SettingData.getString("yos_player_song_sort", "MUSIC_TITLE"))
    // val enableDescending = mutableStateOf(SettingData.get("yos_player_enable_descending", false))
}
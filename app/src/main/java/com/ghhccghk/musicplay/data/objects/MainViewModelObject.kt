package com.ghhccghk.musicplay.data.objects

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

@Stable
object MainViewModelObject {
    val syncLyricIndex = mutableIntStateOf(-1)
    val _visible = mutableStateOf(false)

}
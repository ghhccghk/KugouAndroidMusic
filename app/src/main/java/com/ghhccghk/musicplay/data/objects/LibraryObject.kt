package com.ghhccghk.musicplay.data.objects

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import com.ghhccghk.musicplay.data.libraries.YosMediaItem

@Stable
object LibraryObject {
    @Stable
    private val targetAlbumName = mutableStateOf("")
    fun setTargetAlbumName(name: String) {
        targetAlbumName.value = name
    }

    fun getTargetAlbumName(): String {
        return targetAlbumName.value
    }

    @Stable
    private val targetList: MutableState<List<YosMediaItem>> = mutableStateOf(emptyList())
    @Stable
    private val targetListTitle = mutableStateOf("")
    fun setTargetListWithTitle(title: String, list: List<YosMediaItem>) {
        targetListTitle.value = title
        targetList.value = list
    }
    fun getTargetListWithTitle(): Pair<String, List<YosMediaItem>> {
        return Pair(targetListTitle.value, targetList.value)
    }
}
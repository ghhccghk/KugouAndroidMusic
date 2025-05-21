package com.ghhccghk.musicplay.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BottomSheetViewModel : ViewModel() {
    private val _isPlaylistVisible = MutableStateFlow(false)
    val isPlaylistVisible = _isPlaylistVisible.asStateFlow()

    fun showPlaylist() {
        _isPlaylistVisible.value = true
    }

    fun hidePlaylist() {
        _isPlaylistVisible.value = false
    }
}
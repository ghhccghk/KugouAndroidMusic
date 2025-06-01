package com.ghhccghk.musicplay.data.searchLyric.lyric

data class Content(
    val language: Int = 0 ,
    val lyricContent: List<List<String>> = emptyList<List<String>>(),
    val type: Int = 0
)
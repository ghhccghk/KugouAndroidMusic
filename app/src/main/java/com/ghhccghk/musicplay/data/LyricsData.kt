package com.ghhccghk.musicplay.data

data class LyricLine(
    val time: Long,      // 毫秒
    val text: String,
    val translation: String? = null  // 翻译内容，可以为 null
)

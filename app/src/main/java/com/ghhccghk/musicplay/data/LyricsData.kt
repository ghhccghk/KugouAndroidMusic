package com.ghhccghk.musicplay.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LyricLine(
    val time: Long = 0L,      // 毫秒
    val text: String = "",
    val translation: String? = null  // 翻译内容，可以为 null
)


@JsonClass(generateAdapter = true)
data class getLyricCode(
    val status: Int = 0,
    val info : String = "",
    val error_code: String = "",
    val contenttype: String = "",
    val _source : String = "",
    val charset: String = "",
    val content: String = "",
    val id: String = "",
    val decodeContent: String= ""
)
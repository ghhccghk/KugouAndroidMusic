package com.ghhccghk.musicplay.data.searchLyric

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Base(
    val author_id: Int = 0,
    val author_name: String = "",
    val avatar: String = "",
    val country: String = "",
    val identity: Int = 0,
    val is_publish: Int = 0,
    val language: String = "",
    val type: Int = 0
)
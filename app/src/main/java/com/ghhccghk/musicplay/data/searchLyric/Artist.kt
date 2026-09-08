package com.ghhccghk.musicplay.data.searchLyric

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Artist(
    val base: Base = Base(),
    val identity: Int = 0
)
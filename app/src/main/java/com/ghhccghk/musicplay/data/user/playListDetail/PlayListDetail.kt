package com.ghhccghk.musicplay.data.user.playListDetail

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlayListDetail(
    val data: List<Data> = emptyList(),
    val error_code: Int = 0,
    val status: Int = 0
)
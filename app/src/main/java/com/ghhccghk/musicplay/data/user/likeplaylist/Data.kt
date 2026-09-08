package com.ghhccghk.musicplay.data.user.likeplaylist

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Data(
    val album_count: Long = 0L,
    val collect_count: Long = 0L,
    val info: List<Info> = emptyList(),
    val list_count: Long = 0L,
    val phone_flag: Long = 0L,
    val total_ver: Int = 0,
    val userid: Long = 0L
)
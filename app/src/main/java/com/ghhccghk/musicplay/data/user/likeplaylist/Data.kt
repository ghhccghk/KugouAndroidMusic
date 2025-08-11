package com.ghhccghk.musicplay.data.user.likeplaylist

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Data(
    val album_count: Long,
    val collect_count: Long,
    val info: List<Info>,
    val list_count: Long,
    val phone_flag: Long,
    val total_ver: Int,
    val userid: Long
)
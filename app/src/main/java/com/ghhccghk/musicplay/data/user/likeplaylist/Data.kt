package com.ghhccghk.musicplay.data.user.likeplaylist

data class Data(
    val album_count: Int,
    val collect_count: Int,
    val info: List<Info>,
    val list_count: Int,
    val phone_flag: Int,
    val total_ver: Int,
    val userid: Long
)
package com.ghhccghk.musicplay.data.user.playListDetail.songlist

data class HashOffset(
    val clip_hash: String,
    val end_byte: Int,
    val end_ms: Int,
    val file_type: Int,
    val offset_hash: String,
    val start_byte: Int,
    val start_ms: Int
)
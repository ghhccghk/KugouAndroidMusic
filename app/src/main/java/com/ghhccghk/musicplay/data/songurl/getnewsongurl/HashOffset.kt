package com.ghhccghk.musicplay.data.songurl.getnewsongurl

data class HashOffset(
    val end_byte: Int,
    val end_ms: Int,
    val file_type: Int,
    val offset_hash: String,
    val start_byte: Int,
    val start_ms: Int
)
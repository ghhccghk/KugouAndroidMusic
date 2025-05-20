package com.ghhccghk.musicplay.data.user.playListDetail.songlist

data class Data(
    val begin_idx: Int,
    val count: Int,
    val list_info: ListInfo,
    val pagesize: Int,
    val popularization: Popularization,
    val songs: List<Song>,
    val userid: Int
)
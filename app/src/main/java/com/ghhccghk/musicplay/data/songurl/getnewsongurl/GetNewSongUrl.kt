package com.ghhccghk.musicplay.data.songurl.getnewsongurl

data class GetNewSongUrl(
    val `data`: List<Data>,
    val error_code: Int,
    val message: String,
    val status: Int
)
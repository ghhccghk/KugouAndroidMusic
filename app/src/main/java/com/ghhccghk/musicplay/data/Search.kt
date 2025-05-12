package com.ghhccghk.musicplay.data

data class SearchBase(
    val Shortcuts : List<String>,
    val error_code : Int,
    val SingerShortcut: SingerShortcut,
    val ErrorCode: Int,
    val data: List<SearchData>,
    val status: String
)

data class SearchData(
    val RecordCount : Int,
    val LableName: String,
    val RecordDatas: List<RecordData>,
)

data class RecordData(
    val Use: String,
    val IsRadio: String,
    val tags_v2: List<String>,
    val IsKlist: String,
    val HintInfo: String,
    val MatchCount: String,
    val Hot: String
)

data class SingerShortcut(
    val album_count: String,
    val id: String,
    val fans_count: String,
    val name : String,
    val img: String,
    val song_count: String,
)
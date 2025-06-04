package com.ghhccghk.musicplay.data.songurl.getsongurl

data class TransParam(
    val classmap: Classmap,
    val cpy_attr0: Int = 0,
    val display: Int = 0,
    val display_rate: Int = 0,
    val hash_multitrack: String = "",
    val ipmap: Ipmap,
    val language: String = "",
    val ogg_128_filesize: Int = 0,
    val ogg_128_hash: String,
    val ogg_320_filesize: Int = 0,
    val ogg_320_hash: String = "",
    val pay_block_tpl: Int = 0,
    val qualitymap: Qualitymap,
    val union_cover: String = ""
)
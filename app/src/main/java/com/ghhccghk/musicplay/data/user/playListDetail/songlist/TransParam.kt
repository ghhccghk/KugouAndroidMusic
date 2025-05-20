package com.ghhccghk.musicplay.data.user.playListDetail.songlist

data class TransParam(
    val appid_block: String,
    val cid: Int,
    val classmap: Classmap,
    val cpy_attr0: Int,
    val cpy_grade: Int,
    val cpy_level: Int,
    val display: Int,
    val display_rate: Int,
    val free_for_ad: Int,
    val free_plays: Int,
    val hash_multitrack: String,
    val hash_offset: HashOffset,
    val ipmap: Ipmap,
    val language: String,
    val musicpack_advance: Int,
    val ogg_128_filesize: Int,
    val ogg_128_hash: String,
    val ogg_320_filesize: Int,
    val ogg_320_hash: String,
    val pay_block_tpl: Int,
    val qualitymap: Qualitymap,
    val songname_suffix: String,
    val union_cover: String
)
package com.ghhccghk.musicplay.data.songurl

import com.ghhccghk.musicplay.data.Classmap
import com.ghhccghk.musicplay.data.Ipmap
import com.ghhccghk.musicplay.data.Qualitymap
import com.ghhccghk.musicplay.data.TransParam

data class SongUrlBase(
    val auth_through: List<Any?>,
    val backupUrl: List<String>,
    val bitRate: Int,
    val classmap: Classmap,
    val extName: String,
    val fileHead: Int,
    val fileName: String,
    val fileSize: Int,
    val hash: String,
    val priv_status: Int,
    val q: Int,
    val status: Int,
    val std_hash: String,
    val std_hash_time: Int,
    val timeLength: Int,
    val tracker_through: TrackerThrough,
    val trans_param: TransParam,
    val url: List<String>,
    val volume: Double,
    val volume_gain: Int,
    val volume_peak: Double
)


data class Ipmap(
    val attr0: Long
)
data class Classmap(
    val attr0: Int
)

data class TrackerThrough(
    val all_quality_free: Int,
    val cpy_grade: Int,
    val cpy_level: Int,
    val identity_block: Int,
    val musicpack_advance: Int
)

data class TransParam(
    val classmap: Classmap,
    val cpy_attr0: Int,
    val display: Int,
    val display_rate: Int,
    val hash_multitrack: String,
    val ipmap: Ipmap,
    val language: String,
    val ogg_128_filesize: Int,
    val ogg1_28_hash: String,
    val ogg_320_filesize: Int,
    val ogg_320_hash: String,
    val pay_block_tpl: Int,
    val qualitymap: Qualitymap,
    val union_cover: String
)
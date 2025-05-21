package com.ghhccghk.musicplay.data.songurl.getsongurl

data class GetSongUrlBase(
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
    val volume: Int,
    val volume_gain: Int,
    val volume_peak: Double
)
package com.ghhccghk.musicplay.data.songurl.getsongurl

data class GetSongUrlBase(
    val auth_through: List<Any?> = emptyList(),
    val backupUrl: List<String> = emptyList(),
    val bitRate: Int = 1,
    val classmap: Classmap,
    val extName: String = "",
    val fileHead: Int = 0,
    val fileName: String= "",
    val fileSize: Int = 0,
    val hash: String= "",
    val priv_status: Int = 0,
    val q: Int = 0,
    val status: Int = 0,
    val std_hash: String = "",
    val std_hash_time: Int = 0,
    val timeLength: Int = 0,
    val tracker_through: TrackerThrough,
    val trans_param: TransParam,
    val url: List<String> = emptyList(),
    val volume: String = "",
    val volume_gain: String= "",
    val volume_peak: Double
)
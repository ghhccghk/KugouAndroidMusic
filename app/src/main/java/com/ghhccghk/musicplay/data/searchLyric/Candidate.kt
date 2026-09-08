package com.ghhccghk.musicplay.data.searchLyric

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Candidate(
    val accesskey: String = "",
    val adjust: Int = 0,
    val can_score: Boolean = false,
    val content_format: Int = 0,
    val contenttype: Int = 0,
    val download_id: String = "",
    val duration: Int = 0,
    val hitcasemask: Int = 0,
    val hitlayer: Int = 0,
    val id: String = "",
    val krctype: Int = 0,
    val language: String = "",
    val nickname: String = "",
    val originame: String = "",
    val origiuid: String = "",
    val parinfo: List<Any> = emptyList(),
    val parinfoExt: List<Any> = emptyList(),
    val product_from: String = "",
    val score: Int = 0,
    val singer: String = "",
    val song: String = "",
    val soundname: String = "",
    val sounduid: String = "",
    val transname: String = "",
    val transuid: String = "",
    val uid: String = ""
)
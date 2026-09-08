package com.ghhccghk.musicplay.data.searchLyric

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class searchLyricBase(
    val ai_candidates: List<Any> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val candidates: List<Candidate> = emptyList(),
    val companys: String = "",
    val errcode: Int = 0,
    val errmsg: String = "",
    val expire: Int = 0,
    val has_complete_right: Int = 0,
    val info: String = "",
    val keyword: String = "",
    val proposal: String = "",
    val status: Int = 0,
    val ugc: Int = 0,
    val ugccandidates: List<Any> = emptyList(),
    val ugccount: Int = 0
)
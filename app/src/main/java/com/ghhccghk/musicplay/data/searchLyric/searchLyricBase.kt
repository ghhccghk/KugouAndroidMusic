package com.ghhccghk.musicplay.data.searchLyric

data class searchLyricBase(
    val ai_candidates: List<Any>,
    val artists: List<Artist>,
    val candidates: List<Candidate>,
    val companys: String,
    val errcode: Int,
    val errmsg: String,
    val expire: Int,
    val has_complete_right: Int,
    val info: String,
    val keyword: String,
    val proposal: String,
    val status: Int,
    val ugc: Int,
    val ugccandidates: List<Any>,
    val ugccount: Int
)
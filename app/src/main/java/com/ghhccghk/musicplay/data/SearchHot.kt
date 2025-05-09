package com.ghhccghk.musicplay.data

data class KeywordItem(
    val keyword: String,
    val reason: String
)

data class HotSearchResponse(
    val status: Int,
    val errcode: Int,
    val data: Data
)

data class Data(
    val timestamp: Long,
    val list: List<HotSearchList>
)

data class HotSearchList(
    val name: String,
    val keywords: List<Keyword>
)

data class Keyword(
    val reason: String,
    val keyword: String
)

data class KeywordGroup(
    val name: String,
    val keywords: List<KeywordItem>
)

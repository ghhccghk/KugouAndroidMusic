package com.ghhccghk.musicplay.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class KeywordItem(
    val keyword: String = "",
    val reason: String = ""
)

@JsonClass(generateAdapter = true)
data class HotSearchResponse(
    val status: Int = 0,
    val errcode: Int = 0,
    val data: Data = Data()
)

@JsonClass(generateAdapter = true)
data class Data(
    val timestamp: Long = 0L,
    val list: List<HotSearchList> = emptyList()
)

@JsonClass(generateAdapter = true)
data class HotSearchList(
    val name: String = "",
    val keywords: List<Keyword> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Keyword(
    val reason: String = "",
    val keyword: String = ""
)

@JsonClass(generateAdapter = true)
data class KeywordGroup(
    val name: String = "",
    val keywords: List<KeywordItem> = emptyList()
)
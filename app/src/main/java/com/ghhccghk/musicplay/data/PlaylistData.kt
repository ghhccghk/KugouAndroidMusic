package com.ghhccghk.musicplay.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ThemeMusicScene(
    val id: Int = 0,
    val type: String? = null,
    val play_count: Int = 0,
    val en_title: String? = null,
    val pic_net_save: String? = null,
    val show_tm: List<ShowTime> = emptyList(),
    val theme_list_title: String? = null,
    val pic: String? = null,
    val sort_score: Int? = null,
    val title: String? = null,
    val intro: String? = null,
    val detail_pic: String? = null
)

@JsonClass(generateAdapter = true)
data class ShowTime(
    val beg: String = "",
    val end: String = ""
)

@JsonClass(generateAdapter = true)
data class ThemeMusicList(
    val data: ThemeList? = null,
    val error_code: String = "",
    val status: Int = 0
)

@JsonClass(generateAdapter = true)
data class ThemeList(
    @Json(name = "theme_list")
    val themeList: List<ThemeMusicScene> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class PlayCategoryBase(
    val data: List<PlayCategory> = emptyList(),
    val error_code: String = "",
    val status: Int = 0
)

@JsonClass(generateAdapter = true)
data class PlayCategory(
    val tag_id: String = "",
    val tag_name: String = "",
    val son: List<PlayListTag> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PlayListTag(
    val tag_id: String = "",
    val tag_name: String = "",
    val sort: String = ""
)
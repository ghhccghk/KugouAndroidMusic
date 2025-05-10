package com.ghhccghk.musicplay.data

data class ThemeMusicScene(
    val id: Int,
    val type: String?,
    val play_count: Int,
    val en_title: String?,
    val pic_net_save: String?,
    val show_tm: List<ShowTime> = emptyList(),
    val theme_list_title: String?,
    val pic: String?,
    val sort_score: Int?,
    val title: String?,
    val intro: String?,
    val detail_pic: String?
)

data class ShowTime(
    val beg: String,
    val end: String
)

data class ThemeMusicList(
    val data: theme_list?,
    val error_code: String,
    val status: Int
)

data class theme_list(
    val theme_list: List<ThemeMusicScene> = emptyList(),
)

data class PlayCategoryBase(
    val data: List<PlayCategory> = emptyList(),
    val error_code: String,
    val status: Int
)

data class PlayCategory(
    val tag_id: String,
    val tag_name: String,
    val son: List<PlayListTag> = emptyList()
)

data class PlayListTag(
    val tag_id: String,
    val tag_name: String,
    val sort: String
)


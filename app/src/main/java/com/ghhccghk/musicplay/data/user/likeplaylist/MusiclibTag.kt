package com.ghhccghk.musicplay.data.user.likeplaylist

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MusiclibTag(
    val parent_id: Int = 0,
    val tag_id: Int = 0,
    val tag_name: String = ""
)
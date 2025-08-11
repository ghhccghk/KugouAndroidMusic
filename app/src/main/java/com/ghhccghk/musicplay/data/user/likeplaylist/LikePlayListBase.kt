package com.ghhccghk.musicplay.data.user.likeplaylist

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LikePlayListBase(
    val `data`: Data,
    val error_code: Int,
    val status: Int
)
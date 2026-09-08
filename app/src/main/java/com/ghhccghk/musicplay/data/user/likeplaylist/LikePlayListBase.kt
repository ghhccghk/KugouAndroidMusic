package com.ghhccghk.musicplay.data.user.likeplaylist

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LikePlayListBase(
    val data: Data = Data(),
    val error_code: Int = 0,
    val status: Int = 0
)
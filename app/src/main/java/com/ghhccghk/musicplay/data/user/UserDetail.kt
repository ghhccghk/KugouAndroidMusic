package com.ghhccghk.musicplay.data.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDetail(
    val `data`: Data,
    val error_code: Int,
    val status: Int
)
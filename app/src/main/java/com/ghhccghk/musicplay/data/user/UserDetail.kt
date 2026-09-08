package com.ghhccghk.musicplay.data.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDetail(
    val data: com.ghhccghk.musicplay.data.user.Data = com.ghhccghk.musicplay.data.user.Data(),
    val error_code: Int = 0,
    val status: Int = 0
)
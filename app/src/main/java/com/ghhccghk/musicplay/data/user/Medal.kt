package com.ghhccghk.musicplay.data.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Medal(
    val ktv: Ktv
)
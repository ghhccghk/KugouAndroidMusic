package com.ghhccghk.musicplay.data.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Ktv(
    val type1: String = "",
    val type2: String  = "",
    val type3: String  = ""
)
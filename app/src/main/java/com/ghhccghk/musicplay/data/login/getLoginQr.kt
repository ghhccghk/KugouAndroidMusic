package com.ghhccghk.musicplay.data.login

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class getLoginQr(
    val data: Data = Data(),
    val error_code: Int = 0,
    val status: Int = 0
)

@JsonClass(generateAdapter = true)
data class Data(
    val qrcode: String = "",
    val qrcode_img: String = ""
)

@JsonClass(generateAdapter = true)
data class QrImg(
    val code: Int = 0,
    val data: qrData = qrData()
)

@JsonClass(generateAdapter = true)
data class qrData(
    val base64: String = "",
    val url: String = ""
)

@JsonClass(generateAdapter = true)
data class QrLoginkey(
    val data: KeyData = KeyData(),
    val error_code: Int = 0,
    val status: Int = 0
)

@JsonClass(generateAdapter = true)
data class KeyData(
    val nickname: String = "",
    val pic: String = "",
    val status: Int = 0,
    val token: String = "",
    val userid: Long = 0L
)
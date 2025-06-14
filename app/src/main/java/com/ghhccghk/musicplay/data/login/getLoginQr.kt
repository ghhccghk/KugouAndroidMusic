package com.ghhccghk.musicplay.data.login

data class getLoginQr(
    val `data`: Data,
    val error_code: Int,
    val status: Int
)

data class Data(
    val qrcode: String,
    val qrcode_img: String
)

data class QrImg(
    val code: Int,
    val `data`: qrData
)

data class qrData(
    val base64: String,
    val url: String
)


data class QrLoginkey(
    val `data`: KeyData,
    val error_code: Int,
    val status: Int
)

data class KeyData(
    val nickname: String,
    val pic: String,
    val status: Int,
    val token: String,
    val userid: Long
)
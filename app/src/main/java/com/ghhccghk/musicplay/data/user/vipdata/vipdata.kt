package com.ghhccghk.musicplay.data.user.vipdata
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VipResponse(
    val data: VipData,
    val status: Int,
    @field:Json(name = "error_code") val errorCode: Int
)

@JsonClass(generateAdapter = true)
data class VipData(
    @field:Json(name = "is_vip") val isVip: Int,
    @field:Json(name = "vip_begin_time") val vipBeginTime: String?,
    @field:Json(name = "vip_end_time") val vipEndTime: String?,
    @field:Json(name = "svip_level") val svipLevel: Int,
    @field:Json(name = "svip_score") val svipScore: Int,
    @field:Json(name = "busi_vip") val busiVip: List<BusiVip>
)

@JsonClass(generateAdapter = true)
data class BusiVip(
    @field:Json(name = "is_vip") val isVip: Int,
    @field:Json(name = "product_type") val productType: String,
    @field:Json(name = "vip_begin_time") val vipBeginTime: String,
    @field:Json(name = "vip_end_time") val vipEndTime: String,
    @field:Json(name = "vip_limit_quota") val vipLimitQuota: VipLimitQuota?
)

@JsonClass(generateAdapter = true)
data class VipLimitQuota(
    val total: Int? = null
)

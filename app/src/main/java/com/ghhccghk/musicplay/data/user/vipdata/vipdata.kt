package com.ghhccghk.musicplay.data.user.vipdata
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VipResponse(
    val data: VipData = VipData(),
    val status: Int = 0,
    @field:Json(name = "error_code") val errorCode: Int = 0
)

@JsonClass(generateAdapter = true)
data class VipData(
    @field:Json(name = "is_vip") val isVip: Int = 0,
    @field:Json(name = "vip_begin_time") val vipBeginTime: String? = null,
    @field:Json(name = "vip_end_time") val vipEndTime: String? = null,
    @field:Json(name = "svip_level") val svipLevel: Int = 0,
    @field:Json(name = "svip_score") val svipScore: Int = 0,
    @field:Json(name = "busi_vip") val busiVip: List<BusiVip> = emptyList()
)

@JsonClass(generateAdapter = true)
data class BusiVip(
    @field:Json(name = "is_vip") val isVip: Int = 0,
    @field:Json(name = "product_type") val productType: String = "",
    @field:Json(name = "vip_begin_time") val vipBeginTime: String = "",
    @field:Json(name = "vip_end_time") val vipEndTime: String = "",
    @field:Json(name = "vip_limit_quota") val vipLimitQuota: VipLimitQuota? = null
)

@JsonClass(generateAdapter = true)
data class VipLimitQuota(
    val total: Int? = null
)
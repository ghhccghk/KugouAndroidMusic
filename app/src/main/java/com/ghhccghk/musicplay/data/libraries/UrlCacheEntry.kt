package com.ghhccghk.musicplay.data.libraries

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "url_cache")
data class UrlCacheEntry(
    @PrimaryKey val hash: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis() // 可用于过期判断
)

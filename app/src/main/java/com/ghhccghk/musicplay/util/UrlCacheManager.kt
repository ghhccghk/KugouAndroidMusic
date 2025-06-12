package com.ghhccghk.musicplay.util

import android.content.Context
import com.ghhccghk.musicplay.data.libraries.UrlCacheDao
import com.ghhccghk.musicplay.data.libraries.UrlCacheEntry

object UrlCacheManager {
    private lateinit var dao: UrlCacheDao

    fun init(context: Context) {
        dao = UrlDatabase.getInstance(context).urlCacheDao()
    }

    suspend fun get(hash: String): UrlCacheEntry? = dao.getUrlByHash(hash)

    fun put(hash: String, url: String) {
        dao.insert(UrlCacheEntry(hash, url, System.currentTimeMillis()))
    }
}

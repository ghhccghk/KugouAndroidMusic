package com.ghhccghk.musicplay.data.libraries

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UrlCacheDao {

    @Query("SELECT * FROM url_cache WHERE hash = :hash LIMIT 1")
    suspend fun getUrlByHash(hash: String): UrlCacheEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: UrlCacheEntry)

    @Query("DELETE FROM url_cache WHERE hash = :hash")
    suspend fun deleteByHash(hash: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: UrlCacheEntry)
}

package com.ghhccghk.musicplay.util

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ghhccghk.musicplay.data.libraries.UrlCacheDao
import com.ghhccghk.musicplay.data.libraries.UrlCacheEntry

@Database(entities = [UrlCacheEntry::class], version = 1)
abstract class UrlDatabase : RoomDatabase() {
    abstract fun urlCacheDao(): UrlCacheDao

    companion object {
        @Volatile private var INSTANCE: UrlDatabase? = null

        fun getInstance(context: Context): UrlDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    UrlDatabase::class.java,
                    "url_cache.db"
                ).build().also { INSTANCE = it }
            }
    }
}

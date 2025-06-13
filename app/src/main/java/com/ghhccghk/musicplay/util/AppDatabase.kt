package com.ghhccghk.musicplay.util

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ghhccghk.musicplay.data.libraries.MediaItemDao
import com.ghhccghk.musicplay.data.libraries.MediaItemEntity

@Database(entities = [MediaItemEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaItemDao(): MediaItemDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                                context.applicationContext,
                                AppDatabase::class.java, "playlist.db"
                            ).fallbackToDestructiveMigration(false).build().also { instance = it }
            }
    }
}

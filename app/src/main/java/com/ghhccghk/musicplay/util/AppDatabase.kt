package com.ghhccghk.musicplay.util

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ghhccghk.musicplay.data.libraries.MediaItemDao
import com.ghhccghk.musicplay.data.libraries.MediaItemEntity

@Database(entities = [MediaItemEntity::class], version = 3, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaItemDao(): MediaItemDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
            ALTER TABLE media_items
            ADD COLUMN songtitle TEXT
            """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
            ALTER TABLE media_items
            ADD COLUMN albumAudioId TEXT
            """.trimIndent()
                )

                db.execSQL(
                    """
            ALTER TABLE media_items
            ADD COLUMN audioId TEXT
            """.trimIndent()
                )


            }
        }

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                                context.applicationContext,
                                AppDatabase::class.java, "playlist.db"
                            ).addMigrations(MIGRATION_1_2).addMigrations(MIGRATION_2_3).build().also { instance = it }
            }
    }
}

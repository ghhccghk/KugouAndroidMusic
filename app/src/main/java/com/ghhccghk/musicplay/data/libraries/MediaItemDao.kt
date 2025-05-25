package com.ghhccghk.musicplay.data.libraries


import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaItemEntity>)

    @Query("SELECT * FROM media_items")
    fun getAll(): Flow<List<MediaItemEntity>> // Flow 方便实时观察变化

    @Query("DELETE FROM media_items")
    suspend fun clear()
}

package com.ghhccghk.musicplay.util.others

import android.content.Context
import android.util.Log
import com.ghhccghk.musicplay.data.libraries.MediaItemEntity
import com.ghhccghk.musicplay.util.AppDatabase
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.mediaItemDao()

    // 保存播放列表
    suspend fun savePlaylist(playlist: List<MediaItemEntity>) {
        Log.d("debug", "清空并插入 ${playlist.size} 个条目")
        dao.clear()
        dao.insertAll(playlist)
    }

    // 加载播放列表
    fun loadPlaylist(): Flow<List<MediaItemEntity>> {
        return dao.getAll()
    }
}

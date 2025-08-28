package com.ghhccghk.musicplay.util

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.ghhccghk.musicplay.ui.LyricGlanceWidget
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeLine
import com.mocharealm.accompanist.lyrics.core.model.synced.SyncedLine
import com.mocharealm.accompanist.lyrics.core.model.synced.toSyncedLine

class LyricSyncManager(
    private val context: Context,
    private var lyrics: SyncedLyrics
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lyric_widget", Context.MODE_PRIVATE)

    /**
     * 提供一个播放时间（毫秒），会自动判断歌词行并更新 Glance Widget。
     */
    suspend fun sync(currentTimeMs: Int) {
        val lastLine = StringBuilder()
        val currentLine = StringBuilder()
        val nextLine = StringBuilder()

        val line = lyrics.lines.getOrNull(currentTimeMs)
        val lastIndex = lyrics.lines.getOrNull(currentTimeMs - 1)
        val nextIndex = lyrics.lines.getOrNull(currentTimeMs + 1)

        when (line){
            is KaraokeLine -> {
                currentLine.append(line.toSyncedLine().content)

            }
            is SyncedLine -> {
                currentLine.append(line.content)
            }
        }

        when (lastIndex){
            is KaraokeLine -> {
                lastLine.append(lastIndex.toSyncedLine().content)

            }
            is SyncedLine -> {
                lastLine.append(lastIndex.content)
            }
        }


        when (nextIndex){
            is KaraokeLine -> {
                nextLine.append(nextIndex.toSyncedLine().content)

            }
            is SyncedLine -> {
                nextLine.append(nextIndex.content)
            }
        }


        val manager = GlanceAppWidgetManager(context)
        val widget = LyricGlanceWidget()
        val glanceIds = manager.getGlanceIds(widget.javaClass)
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) {
                it[stringPreferencesKey("line_last")] = lastLine.toString()
                it[stringPreferencesKey("line_current")] = currentLine.toString()
                it[stringPreferencesKey("line_next")] = nextLine.toString()
            }
            widget.update(context, glanceId)
        }

    }


    /**
     * 格式化一整行歌词
     */
    private fun formatLine(line: List<Pair<Float, String>>?): String {
        return line?.joinToString("") { it.second } ?: ""
    }
}

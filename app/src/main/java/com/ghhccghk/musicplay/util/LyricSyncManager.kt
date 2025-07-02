package com.ghhccghk.musicplay.util

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.ghhccghk.musicplay.ui.LyricGlanceWidget

class LyricSyncManager(
    private val context: Context,
    private var lyrics: List<List<Pair<Float, String>>>
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lyric_widget", Context.MODE_PRIVATE)

    /**
     * 提供一个播放时间（毫秒），会自动判断歌词行并更新 Glance Widget。
     */
    suspend fun sync(currentTimeMs: Long) {
        val currentIndex = findCurrentLineIndex(currentTimeMs)
        val lastLine = formatLine(lyrics.getOrNull(currentIndex - 1))
        val currentLine = formatLine(lyrics.getOrNull(currentIndex))
        val nextLine = formatLine(lyrics.getOrNull(currentIndex + 1))

        val manager = GlanceAppWidgetManager(context)
        val widget = LyricGlanceWidget()
        val glanceIds = manager.getGlanceIds(widget.javaClass)
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) {
                it[stringPreferencesKey("line_last")] = lastLine
                it[stringPreferencesKey("line_current")] = currentLine
                it[stringPreferencesKey("line_next")] = nextLine
            }
            widget.update(context, glanceId)
        }

    }

    /**
     * 找到当前歌词所在行的索引（你可以自定义更精准逻辑）
     */
    private fun findCurrentLineIndex(currentTimeMs: Long): Int {
        val currentTimeSec = currentTimeMs
        for (i in lyrics.indices) {
            val start = lyrics[i].firstOrNull()?.first ?: continue
            val nextStart = lyrics.getOrNull(i + 1)?.firstOrNull()?.first ?: Float.MAX_VALUE
            if (currentTimeSec >= start && currentTimeSec < nextStart) {
                return i
            }
        }
        return 0
    }

    /**
     * 格式化一整行歌词
     */
    private fun formatLine(line: List<Pair<Float, String>>?): String {
        return line?.joinToString("") { it.second } ?: ""
    }
}

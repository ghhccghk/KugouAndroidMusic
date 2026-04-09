package com.ghhccghk.musicplay.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.ghhccghk.musicplay.data.objects.MediaViewModelObject
import com.ghhccghk.musicplay.ui.widgets.LyricGlanceWidget
import com.ghhccghk.musicplay.ui.widgets.PREF_AGGRESSIVE_SCALE
import com.ghhccghk.musicplay.ui.widgets.PREF_LINE_CURRENT
import com.ghhccghk.musicplay.ui.widgets.PREF_LINE_CURRENT_TF
import com.ghhccghk.musicplay.ui.widgets.PREF_LINE_LAST
import com.ghhccghk.musicplay.ui.widgets.PREF_LINE_LAST_TF
import com.ghhccghk.musicplay.ui.widgets.PREF_LINE_NEXT
import com.ghhccghk.musicplay.ui.widgets.PREF_LINE_NEXT_TF
import com.ghhccghk.musicplay.ui.widgets.PREF_TYPEWRITER_INDEX
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeLine
import com.mocharealm.accompanist.lyrics.core.model.synced.SyncedLine
import com.mocharealm.accompanist.lyrics.core.model.synced.mapper.toSyncedLine
import kotlin.math.max

class LyricSyncManager private constructor(
    private val context: Context,
) {

    companion object {
        // 单例实例（应用级复用），避免频繁 new 对象
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: LyricSyncManager? = null

        // 每个 widget 最小更新间隔（ms），用于节流频繁更新（逐字每帧可能很快）
        private const val MIN_WIDGET_UPDATE_INTERVAL_MS = 80L

        fun getInstance(context: Context): LyricSyncManager {
            return instance ?: synchronized(this) {
                (instance ?: LyricSyncManager(context.applicationContext).also {
                    instance = it
                })
            }.also { it.updateLyrics() }
        }
    }

    // 缓存每个 widget 上次发送的 current 文本与 typewriterIndex，避免重复更新
    private val lastSentCurrent = mutableMapOf<String, String>()
    private val lastSentIndex = mutableMapOf<String, Int>()

    // 缓存 Glance 对象，避免每次创建
    private val manager: GlanceAppWidgetManager = GlanceAppWidgetManager(context)
    private val widget: LyricGlanceWidget = LyricGlanceWidget()

    // 记录每个 widget 上次实际更新时间（用于节流）
    private val lastUpdateMs = mutableMapOf<String, Long>()

    // 预计算并缓存 lyrics 元数据以减少 sync 时的重复计算
    private var linesAreMap: Boolean? = null
    private var cachedNumericKeys: List<Long>? = null
    private var cachedListStarts: List<Long>? = null
    private var cachedListRef: List<Any>? = null

    /**
     * 提供一个播放时间（毫秒），会自动判断歌词行并更新 Glance Widget。
     * 这个函数会尽量兼容 lyrics.lines 是 Map 或 List 的情况：
     * - 若为 Map（以时间作为键），会寻找短边最接近的键并读取前/当前/后行
     * - 若为其它集合类型，会尝试按索引访问（保留旧行为作为回退）
     */
    suspend fun sync(currentTimeMs: Int) {
        val lastLine = StringBuilder()
        val lastLineTf = StringBuilder()
        val currentLine = StringBuilder()
        val currentLineTf = StringBuilder()
        val nextLine = StringBuilder()
        val nextLineTf = StringBuilder()


        // 评估日志等级，避免不必要的字符串构造
        val verbose = Log.isLoggable("LyricSyncManager", Log.VERBOSE)

        // 尝试处理 Map 类型（最常见的时间->行映射）
        val linesObj = MediaViewModelObject.newLrcEntries.value.lines
        // 调试日志：显示调用与 lines 对象类型（避免不必要的安全调用）
        if (verbose) Log.v(
            "LyricSyncManager",
            "sync called currentTimeMs=$currentTimeMs linesClass=${linesObj.javaClass.name}"
        )
        var currentAny: Any? = null
        var lastAny: Any? = null
        var nextAny: Any? = null

        if (linesObj is Map<*, *>) {
            // 尝试使用缓存的 numeric keys，避免每次分配
            val numericKeys = cachedNumericKeys ?: linesObj.keys.mapNotNull { k ->
                when (k) {
                    is Number -> k.toLong()
                    is String -> k.toLongOrNull()
                    else -> null
                }
            }.sorted()

            // 如果传入的值看起来像是行索引（例如小于行数），优先当索引处理
            val maybeIndex = currentTimeMs
            if (maybeIndex >= 0 && maybeIndex < numericKeys.size) {
                val idx = maybeIndex
                val curKey = numericKeys.getOrNull(idx)
                val prevKey = numericKeys.getOrNull(idx - 1)
                val nextKey = numericKeys.getOrNull(idx + 1)

                if (verbose) Log.v(
                    "LyricSyncManager",
                    "Detected index mode: idx=$idx curKey=$curKey prevKey=$prevKey nextKey=$nextKey now=$currentTimeMs (treated as index)"
                )

                currentAny = curKey?.let { key ->
                    linesObj.entries.find { entry -> (entry.key as? Number)?.toLong() == key || (entry.key as? String)?.toLongOrNull() == key }?.value
                }
                lastAny = prevKey?.let { key ->
                    linesObj.entries.find { entry -> (entry.key as? Number)?.toLong() == key || (entry.key as? String)?.toLongOrNull() == key }?.value
                }
                nextAny = nextKey?.let { key ->
                    linesObj.entries.find { entry -> (entry.key as? Number)?.toLong() == key || (entry.key as? String)?.toLongOrNull() == key }?.value
                }
            } else {
                // 处理 currentTimeMs 与 keys 单位不一致的情况（比如 currentTimeMs 可能是秒而 keys 是毫秒）
                var nowForKeys = currentTimeMs.toLong()
                val minKey = numericKeys.firstOrNull() ?: 0L
                if (minKey > 1000 && nowForKeys < 1000) {
                    // 很可能传入的是秒 -> 转成毫秒
                    nowForKeys *= 1000
                }

                val curKey = numericKeys.lastOrNull { it <= nowForKeys }
                val prevKey = numericKeys.lastOrNull { it < nowForKeys }
                val nextKey = numericKeys.firstOrNull { it > nowForKeys }

                if (verbose) Log.v(
                    "LyricSyncManager",
                    "numericKeys=${numericKeys.joinToString()} curKey=$curKey prevKey=$prevKey nextKey=$nextKey now=$currentTimeMs nowForKeys=$nowForKeys"
                )

                // 正常情况：把找到的 cur/prev/next key 对应的 value 赋给 currentAny/lastAny/nextAny
                currentAny = curKey?.let { key ->
                    linesObj.entries.find { entry -> (entry.key as? Number)?.toLong() == key || (entry.key as? String)?.toLongOrNull() == key }?.value
                }
                lastAny = prevKey?.let { key ->
                    linesObj.entries.find { entry -> (entry.key as? Number)?.toLong() == key || (entry.key as? String)?.toLongOrNull() == key }?.value
                }
                nextAny = nextKey?.let { key ->
                    linesObj.entries.find { entry -> (entry.key as? Number)?.toLong() == key || (entry.key as? String)?.toLongOrNull() == key }?.value
                }

                // 回退：如果没有找到 <= now 的键，选择时间上最近的键作为回退，避免 currentAny 为空
                if (currentAny == null && numericKeys.isNotEmpty()) {
                    val closest = numericKeys.minByOrNull { kotlin.math.abs(it - nowForKeys) }
                    if (closest != null) {
                        val fallbackPrev = numericKeys.lastOrNull { it < closest }
                        val fallbackNext = numericKeys.firstOrNull { it > closest }
                        if (verbose) Log.v(
                            "LyricSyncManager",
                            "Fallback to closest key=$closest prev=$fallbackPrev next=$fallbackNext"
                        )
                        currentAny =
                            linesObj.entries.find { entry -> (entry.key as? Number)?.toLong() == closest || (entry.key as? String)?.toLongOrNull() == closest }?.value
                        lastAny =
                            fallbackPrev?.let { key -> linesObj.entries.find { entry -> (entry.key as? Number)?.toLong() == key || (entry.key as? String)?.toLongOrNull() == key }?.value }
                        nextAny =
                            fallbackNext?.let { key -> linesObj.entries.find { entry -> (entry.key as? Number)?.toLong() == key || (entry.key as? String)?.toLongOrNull() == key }?.value }
                    }
                }
            }
        } else {
            // 如果 lines 是 List（最常见），按每行的 start 字段匹配播放时间
            try {
                // 优先使用 cachedListRef/cachedListStarts
                val listRef = cachedListRef ?: (linesObj as? List<*>)
                val starts = cachedListStarts ?: listRef?.mapNotNull { item ->
                    when (item) {
                        is KaraokeLine -> item.start.toLong()
                        is SyncedLine -> item.start.toLong()
                        else -> null
                    }
                }
                if (listRef != null && listRef.isNotEmpty()) {
                    // 收集每个元素的 start（若可用），便于判断单位与做匹配
                    val startsList = starts ?: emptyList()
                    if (startsList.isEmpty()) {
                        if (verbose) Log.v(
                            "LyricSyncManager",
                            "List items have no start timestamps; skipping time-based match"
                        )
                    } else {
                        val minStart = startsList.minOrNull() ?: 0L
                        val maxStart = startsList.maxOrNull() ?: 0L
                        var nowForMatch = currentTimeMs.toLong()

                        // 判断 starts 的时间单位与传入 now 的单位是否一致，做简单缩放
                        // 若 starts 明显为毫秒（maxStart > 1000）但 now 很小（<1000），将 now 转为毫秒
                        if (maxStart > 1000 && nowForMatch < 1000) {
                            nowForMatch *= 1000
                            if (verbose) Log.v(
                                "LyricSyncManager",
                                "Adjusted nowForMatch -> ms (was seconds): nowForMatch=$nowForMatch"
                            )
                        }
                        // 若 starts 明显为秒（maxStart < 1000）但 now 很大（>1000），将 now 转为秒
                        else if (maxStart < 1000 && nowForMatch > 1000) {
                            nowForMatch /= 1000
                            if (verbose) Log.v(
                                "LyricSyncManager",
                                "Adjusted nowForMatch -> seconds (was ms): nowForMatch=$nowForMatch"
                            )
                        }

                        if (verbose) Log.v(
                            "LyricSyncManager",
                            "List start sample: ${startsList.take(6)} min=$minStart max=$maxStart nowForMatch=$nowForMatch"
                        )

                        var foundIndex: Int? = null
                        for ((i, item) in listRef.withIndex()) {
                            val start = when (item) {
                                is KaraokeLine -> item.start.toLong()
                                is SyncedLine -> item.start.toLong()
                                else -> null
                            } ?: continue

                            if (start <= nowForMatch) {
                                foundIndex = i
                            } else {
                                break
                            }
                        }

                        // 如果仍未找到，尝试按最近邻回退到时间最接近的一行；再否则如果传入值像索引则按索引处理
                        if (foundIndex == null) {
                            // 最近邻回退
                            val closestIdx = startsList.withIndex()
                                .minByOrNull { kotlin.math.abs(it.value - nowForMatch) }?.index
                            if (closestIdx != null) {
                                if (verbose) Log.v(
                                    "LyricSyncManager",
                                    "Fallback to closestIndex=$closestIdx (start=${startsList[closestIdx]})"
                                )
                                foundIndex = closestIdx
                            } else if (currentTimeMs >= 0 && currentTimeMs < listRef.size) {
                                if (verbose) Log.v(
                                    "LyricSyncManager",
                                    "Fallback: treating currentTimeMs as line index: $currentTimeMs"
                                )
                                foundIndex = currentTimeMs
                            }
                        }

                        currentAny = foundIndex?.let { listRef.getOrNull(it) }
                        lastAny = foundIndex?.let { listRef.getOrNull(it - 1) }
                        nextAny = foundIndex?.let { listRef.getOrNull(it + 1) }
                    }
                }
            } catch (_: Exception) {
                // 忽略异常，currentAny 可能为 null
            }
        }

        fun extractContent(obj: Any?): String {
            return when (obj) {
                is KaraokeLine -> obj.toSyncedLine().content
                is SyncedLine -> obj.content
                else -> ""
            }
        }
        fun extractContentTF(obj: Any?): String? {
            return when (obj) {
                is KaraokeLine -> obj.translation
                is SyncedLine -> obj.translation
                else -> null
            }
        }

        currentLineTf.append(extractContentTF(currentAny) ?: "")
        currentLine.append(extractContent(currentAny))
        lastLineTf.append(extractContentTF(lastAny) ?: "")
        lastLine.append(extractContent(lastAny))
        nextLineTf.append(extractContentTF(nextAny) ?: "")
        nextLine.append(extractContent(nextAny))


        // 逐字索引计算：如果是 KaraokeLine，就根据 syllables 的 start/end/content 计算已显示字符数
        var typewriterIndex: Int
        if (currentAny is KaraokeLine) {
            val sylls = currentAny.syllables
            val fullText = sylls.joinToString(separator = "") { it.content }
            val totalChars = fullText.length

            // 统一时间单位：如果 syllable 的时间大多数是毫秒级，而 currentTimeMs 很小（可能是秒），则把 currentTimeMs 转为毫秒
            var nowMs = currentTimeMs.toLong()
            val maxSyllTime = sylls.maxOfOrNull { it.end.toLong() } ?: 0L
            if (maxSyllTime > 1000 && nowMs < 1000) {
                nowMs *= 1000
                if (verbose) Log.v(
                    "LyricSyncManager",
                    "Adjusted nowMs to milliseconds: nowMs=$nowMs (original currentTimeMs=$currentTimeMs)"
                )
            }

            var revealedChars = 0
            val now = nowMs

            if (verbose) Log.d(
                "LyricSyncManager",
                "KaraokeLine found: full='$fullText' now=$now totalChars=$totalChars"
            )
            for ((idx, syll) in sylls.withIndex()) {
                val sStart = syll.start.toLong()
                val sEnd = syll.end.toLong()
                val text = syll.content
                if (now >= sEnd) {
                    // 完整展示此音节
                    revealedChars += text.length
                    if (verbose) Log.v(
                        "LyricSyncManager",
                        "syll[$idx] full: start=$sStart end=$sEnd text='$text' -> +${text.length}"
                    )
                } else if (now >= sStart) {
                    // 部分展示当前音节，按时间比例展示部分字符
                    val dur = (sEnd - sStart).coerceAtLeast(1L)
                    val elapsed = (now - sStart).coerceIn(0L, dur)
                    var partial = ((elapsed.toDouble() / dur.toDouble()) * text.length).toInt()
                    // 若 elapsed > 0 但按比例计算得到 0，至少显示 1 个字符，避免进度卡住
                    if (elapsed > 0 && partial == 0) partial = 1
                    partial = max(0, partial)
                    revealedChars += partial
                    if (verbose) Log.v(
                        "LyricSyncManager",
                        "syll[$idx] partial: start=$sStart end=$sEnd text='$text' dur=$dur elapsed=$elapsed -> +$partial"
                    )
                    break // 后续音节尚未开始
                } else {
                    if (verbose) Log.v(
                        "LyricSyncManager",
                        "syll[$idx] future: start=$sStart end=$sEnd text='$text'"
                    )
                    break // 尚未到下一个音节
                }
            }

            typewriterIndex = revealedChars.coerceIn(0, totalChars)
            Log.d(
                "LyricSyncManager",
                "Computed typewriterIndex=$typewriterIndex for line='$fullText' (nowMs=$nowMs)"
            )
        } else {
            // 非 KaraokeLine 时不启用逐字
            typewriterIndex = -1
        }

        val glanceIds = manager.getGlanceIds(widget.javaClass)
        // 若当前行为 KaraokeLine，使用 syllables 拼接的 fullText 作为 currentStr，保证 manager 与 widget 的字符串一致
        val currentStrRaw = if (currentAny is KaraokeLine) {
            currentAny.syllables.joinToString(separator = "") { it.content }
        } else currentLine.toString()
        val lastStr = lastLine.toString()
        val nextStr = nextLine.toString()

        if (currentAny == null) {
            Log.w(
                "LyricSyncManager",
                "No current lyric matched for time=$currentTimeMs; currentStrRaw='${currentStrRaw}' last='${lastStr}' next='${nextStr}'"
            )
        }

        val nowRealtime = SystemClock.elapsedRealtime()
        var anyUpdatedLocal = false
        glanceIds.forEach { glanceId ->
            val gid = glanceId.toString()
            val prevCurrent = lastSentCurrent[gid]
            val prevIndex = lastSentIndex[gid] ?: Int.MIN_VALUE

            val currentToSend = if (currentStrRaw.isBlank() && !prevCurrent.isNullOrBlank()) {
                // 保持之前的显示，避免清空界面
                Log.v(
                    "LyricSyncManager",
                    "Keep previous current for $gid because new current is blank"
                )
                prevCurrent
            } else currentStrRaw

            // 如果之前没有发送过任何歌词，且当前也没有匹配到歌词，则跳过更新该 widget
            if (prevCurrent.isNullOrBlank() && currentToSend.isBlank()) {
                if (verbose) Log.v(
                    "LyricSyncManager",
                    "Skip update for $gid because no lyrics available yet and current is blank"
                )
                return@forEach
            }

            // 节流：如果距离上次更新未达到最小间隔，则跳过更新
            val lastUp = lastUpdateMs[gid] ?: 0L
            if (nowRealtime - lastUp < MIN_WIDGET_UPDATE_INTERVAL_MS) {
                if (verbose) Log.v(
                    "LyricSyncManager",
                    "Throttle skip for $gid: now=$nowRealtime last=$lastUp diff=${nowRealtime - lastUp}"
                )
                return@forEach
            }

            if (prevCurrent != currentToSend || prevIndex != typewriterIndex) {
                if (verbose) Log.d(
                    "LyricSyncManager",
                    "Updating widget $gid current='${currentToSend}' index=$typewriterIndex last='$prevCurrent'"
                )
                updateAppWidgetState(context, glanceId) {
                    it[PREF_LINE_LAST] = lastStr
                    it[PREF_LINE_LAST_TF] = lastLineTf.toString()
                    it[PREF_LINE_CURRENT_TF] = currentLineTf.toString()
                    it[PREF_LINE_NEXT_TF] = nextLineTf.toString()
                    it[PREF_LINE_CURRENT] = currentLine.toString()
                    it[PREF_LINE_CURRENT] = currentToSend
                    it[PREF_AGGRESSIVE_SCALE] = true
                    it[PREF_LINE_NEXT] = nextStr
                    it[PREF_TYPEWRITER_INDEX] = typewriterIndex
                }
                widget.update(context, glanceId)

                lastSentCurrent[gid] = currentToSend
                lastSentIndex[gid] = typewriterIndex
                lastUpdateMs[gid] = SystemClock.elapsedRealtime()
                anyUpdatedLocal = true
            } else {
                if (verbose) Log.v(
                    "LyricSyncManager",
                    "Skip widget $gid update: no change (index=$typewriterIndex)"
                )
            }
        }

        if (!anyUpdatedLocal) {
            Log.v(
                "LyricSyncManager",
                "No widget updated in this sync call (likely no matched lyric). Skipping further actions."
            )
        }
    }

    /**
     * 更新歌词内容（供单例外部调用）
     */
    fun updateLyrics() {
        val lyrics = MediaViewModelObject.newLrcEntries.value
        // 预计算并缓存结构
        val linesObj = lyrics.lines
        if (linesObj is Map<*, *>) {
            linesAreMap = true
            cachedNumericKeys = linesObj.keys.mapNotNull { k ->
                when (k) {
                    is Number -> k.toLong()
                    is String -> k.toLongOrNull()
                    else -> null
                }
            }.sorted()
            cachedListStarts = null
            cachedListRef = null
        } else {
            linesAreMap = false
            val list = (linesObj as? List<*>)
            if (list != null) {
                cachedListRef = list.filterNotNull().map { it }
                cachedListStarts = cachedListRef?.mapNotNull { item ->
                    when (item) {
                        is KaraokeLine -> item.start.toLong()
                        is SyncedLine -> item.start.toLong()
                        else -> null
                    }
                }
            } else {
                cachedListRef = null
                cachedListStarts = null
            }
            cachedNumericKeys = null
        }
    }
}

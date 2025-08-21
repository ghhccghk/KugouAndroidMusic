package com.ghhccghk.musicplay.util


/**
 * Copyright (C) 2024 Akane Foundation
 * Copyright (C) 2025 李太白 (@github ghhccghk )
 *
 * This file is part of Gramophone.
 *
 * Gramophone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gramophone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * @author AkaneTan
 * @author ghhccghk
 */

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.VectorDrawable
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.Insets
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.set
import androidx.core.net.toFile
import androidx.core.os.BundleCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.ghhccghk.musicplay.BuildConfig
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.libraries.MediaItemEntity
import com.ghhccghk.musicplay.data.libraries.uri
import com.ghhccghk.musicplay.data.searchLyric.lyric.fanyiLyricbase
import com.ghhccghk.musicplay.service.PlayService.Companion.SERVICE_GET_AUDIO_FORMAT
import com.ghhccghk.musicplay.util.others.PlaylistRepository
import com.ghhccghk.musicplay.util.others.toEntity
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.jetbrains.annotations.Contract
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Locale
import kotlin.math.max

object Tools {
    /** 显示二维码 */
    fun showQrDialog(context: Context, text: String,title: String, size: Int = 512) {
        val bitmap = generateQRCode(text, size)

        val imageView = ImageView(context).apply {
            setImageBitmap(bitmap)
            setPadding(32, 32, 32, 32)
        }

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(imageView)
            .setPositiveButton("关闭", null)
            .show()
    }

    fun generateQRCode(content: String, size: Int = 512): Bitmap {
        val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
        val bitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bitmap = createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return bitmap
    }

    fun Player.playOrPause() {
        if (playWhenReady) {
            pause()
        } else {
            play()
        }
    }

    fun Drawable.startAnimation() {
        when (this) {
            is AnimatedVectorDrawable -> start()
            is AnimatedVectorDrawableCompat -> start()
            else -> throw IllegalArgumentException()
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatMillis(millis: Long): String {
        val minutes = millis / 1000 / 60
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun Int.toLocaleString() = String.format(Locale.getDefault(), "%d", this)

    @Suppress("NOTHING_TO_INLINE")
    inline fun Int.dpToPx(context: Context): Int =
        (this.toFloat() * context.resources.displayMetrics.density).toInt()

    @Suppress("NOTHING_TO_INLINE")
    inline fun Float.dpToPx(context: Context): Float =
        (this * context.resources.displayMetrics.density)



// ViewExtensions

    fun View.fadOutAnimation(
        duration: Long = 300,
        visibility: Int = View.INVISIBLE,
        completion: (() -> Unit)? = null
    ) {
        (getTag(R.id.fade_out_animation) as ViewPropertyAnimator?)?.cancel()
        setTag(R.id.fade_out_animation, animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction {
                this.visibility = visibility
                setTag(R.id.fade_out_animation, null)
                completion?.let {
                    it()
                }
            })
    }

    fun View.fadInAnimation(duration: Long = 300, completion: (() -> Unit)? = null) {
        (getTag(R.id.fade_in_animation) as ViewPropertyAnimator?)?.cancel()
        alpha = 0f
        visibility = View.VISIBLE
        setTag(R.id.fade_in_animation, animate()
            .alpha(1f)
            .setDuration(duration)
            .withEndAction {
                setTag(R.id.fade_in_animation, null)
                completion?.let {
                    it()
                }
            })
    }

    @OptIn(UnstableApi::class)
    fun MediaController.getAudioFormat(): AudioFormatDetector.AudioFormats? =
        sendCustomCommand(
            SessionCommand(SERVICE_GET_AUDIO_FORMAT, Bundle.EMPTY),
            Bundle.EMPTY
        ).get().extras.let {
            AudioFormatDetector.AudioFormats(
                it.getBundle("file_format")?.let { bundle -> Format.fromBundle(bundle) },
                it.getBundle("sink_format")?.let { bundle -> Format.fromBundle(bundle) },
                BundleCompat.getParcelable(it, "track_format", AudioTrackInfo::class.java),
                BundleCompat.getParcelable(it, "hal_format", AfFormatInfo::class.java),
                BundleCompat.getParcelable(it, "bt", BtCodecInfo::class.java)
            )
        }

    fun MediaItem.getBitrate(): Long? {
        val retriever = MediaMetadataRetriever()
        return try {
            val filePath = getFile()?.path ?: return null
            retriever.setDataSource(filePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                ?.toLongOrNull()
        } catch (e: Exception) {
            Log.w("getBitrate", Log.getStackTraceString(e))
            null
        } finally {
            retriever.release()
        }
    }

    fun MediaItem.getFile(): File? {
        return if (uri?.scheme == "file") {
            uri?.toFile()
        } else {
            null
        }
    }



    // the whole point of this function is to do literally nothing at all (but without impacting
// performance) in release builds and ignore StrictMode violations in debug builds
    inline fun <reified T> allowDiskAccessInStrictMode(relax: Boolean = false, doIt: () -> T): T {
        return if (BuildConfig.DEBUG) {
            if (Looper.getMainLooper() != Looper.myLooper()) {
                if (relax) doIt() else
                    throw IllegalStateException("allowDiskAccessInStrictMode(false) on wrong thread")
            } else {
                val policy = StrictMode.allowThreadDiskReads()
                try {
                    StrictMode.allowThreadDiskWrites()
                    doIt()
                } finally {
                    StrictMode.setThreadPolicy(policy)
                }
            }
        } else doIt()
    }



    fun View.enableEdgeToEdgePaddingListener(
        ime: Boolean = false, top: Boolean = false,
        extra: ((Insets) -> Unit)? = null
    ) {
        if (fitsSystemWindows) throw IllegalArgumentException("must have fitsSystemWindows disabled")
        if (this is AppBarLayout) {
            if (ime) throw IllegalArgumentException("AppBarLayout must have ime flag disabled")
            // AppBarLayout fitsSystemWindows does not handle left/right for a good reason, it has
            // to be applied to children to look good; we rewrite fitsSystemWindows in a way mostly specific
            // to Gramophone to support shortEdges displayCutout
            val collapsingToolbarLayout =
                children.find { it is CollapsingToolbarLayout } as CollapsingToolbarLayout?
            collapsingToolbarLayout?.let {
                // The CollapsingToolbarLayout mustn't consume insets, we handle padding here anyway
                ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets -> insets }
            }
            val expandedTitleMarginStart = collapsingToolbarLayout?.expandedTitleMarginStart
            val expandedTitleMarginEnd = collapsingToolbarLayout?.expandedTitleMarginEnd
            ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
                val cutoutAndBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            or WindowInsetsCompat.Type.displayCutout()
                )
                (v as AppBarLayout).children.forEach {
                    if (it is CollapsingToolbarLayout) {
                        val es = expandedTitleMarginStart!! + if (it.layoutDirection
                            == View.LAYOUT_DIRECTION_LTR
                        ) cutoutAndBars.left else cutoutAndBars.right
                        if (es != it.expandedTitleMarginStart) it.expandedTitleMarginStart = es
                        val ee = expandedTitleMarginEnd!! + if (it.layoutDirection
                            == View.LAYOUT_DIRECTION_RTL
                        ) cutoutAndBars.left else cutoutAndBars.right
                        if (ee != it.expandedTitleMarginEnd) it.expandedTitleMarginEnd = ee
                    }
                    it.setPadding(cutoutAndBars.left, 0, cutoutAndBars.right, 0)
                }
                v.setPadding(0, cutoutAndBars.top, 0, 0)
                val i = insets.getInsetsIgnoringVisibility(
                    WindowInsetsCompat.Type.systemBars()
                            or WindowInsetsCompat.Type.displayCutout()
                )
                extra?.invoke(cutoutAndBars)
                return@setOnApplyWindowInsetsListener WindowInsetsCompat.Builder(insets)
                    .setInsets(
                        WindowInsetsCompat.Type.systemBars()
                                or WindowInsetsCompat.Type.displayCutout(),
                        Insets.of(cutoutAndBars.left, 0, cutoutAndBars.right, cutoutAndBars.bottom)
                    )
                    .setInsetsIgnoringVisibility(
                        WindowInsetsCompat.Type.systemBars()
                                or WindowInsetsCompat.Type.displayCutout(),
                        Insets.of(i.left, 0, i.right, i.bottom)
                    )
                    .build()
            }
        } else {
            val pl = paddingLeft
            val pt = paddingTop
            val pr = paddingRight
            val pb = paddingBottom
            ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
                val mask = WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout() or
                        if (ime) WindowInsetsCompat.Type.ime() else 0
                val i = insets.getInsets(mask)
                // TODO is this really the best way lol?
                val pbsp = 0
                v.setPadding(
                    pl + i.left, pt + (if (top) i.top else 0), pr + i.right,
                    pb + max(i.bottom, pbsp)
                )
                extra?.invoke(i)
                return@setOnApplyWindowInsetsListener insets
            }
        }
    }



    /** 检测端口是否打开 */
    fun isPortOpen(port: Int = 9600, timeout: Int = 200): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("127.0.0.1", port), timeout)
                true
            }
        } catch (e: IOException) {
            false
        }
    }

    fun loadImageFileAsByteArray(file: File): ByteArray? {
        return try {
            file.inputStream().use { it.readBytes() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



    fun drawableToBase64(drawable: Drawable): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (drawable is AdaptiveIconDrawable) {
                return adaptiveIconDrawableBase64(drawable)
            }
        }
        when (drawable) {
            is BitmapDrawable -> {
                return drawableToBase64(drawable.bitmap)
            }

            is VectorDrawable -> {
                return drawableToBase64(
                    makeDrawableToBitmap(
                        drawable
                    )
                )
            }

            else -> {
                return try {
                    drawableToBase64(drawable.toBitmap())
                } catch (_: Exception) {
                    ""
                }
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    fun adaptiveIconDrawableBase64(drawable: AdaptiveIconDrawable): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val background = drawable.background
            val foreground = drawable.foreground
            if (background != null && foreground != null) {
                val layerDrawable = LayerDrawable(arrayOf(background, foreground))
                val createBitmap =
                    createBitmap(layerDrawable.intrinsicWidth, layerDrawable.intrinsicHeight)
                val canvas = Canvas(createBitmap)
                layerDrawable.setBounds(0, 0, canvas.width, canvas.height)
                layerDrawable.draw(canvas)
                drawableToBase64(createBitmap)
            } else {
                ""
            }
        } else {
            ""
        }
    }


    fun makeDrawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.apply {
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }
        return bitmap
    }

    fun drawableToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    fun getExternalSubdirFile(context: Context, subDir: String, fileName: String): File? {
        val baseDir = context.getExternalFilesDir(null)
        val targetDir = File(baseDir, "$subDir")

        if (!targetDir.exists()) {
            targetDir.mkdirs()  // 确保子目录存在
        }

        return File(targetDir, fileName)
    }

    fun writeToSubdirCache(context: Context, subDir: String, fileName: String, data: String) {
        val file = getExternalSubdirFile(context, subDir, fileName)
        file?.writeText(data)
    }

    fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }


    fun readFromSubdirCache(context: Context, subDir: String, fileName: String): String? {
        val file = getExternalSubdirFile(context, subDir, fileName)
        return if (file != null && file.exists()) {
            file.readText()
        } else {
            null
        }
    }

    fun convertKrcToLrc(krcContent: String): String {
        val lineRegex = Regex("""\[(\d+),(\d+)]""")  // [开始时间, 持续时间]
        val wordRegex = Regex("""<(\d+),(\d+),\d+>(.*?)(?=<|$)""")  // <偏移, 持续, ?>文字
        var lastLineStartTime: Long = -1L  // 记录上一行的时间戳

        // 解析 Base64 里的 JSON 翻译内容
        val regex = "\\[language:(.*?)]".toRegex()
        val matchResult = regex.find(krcContent)?.groups?.get(1)?.value

        val output = mutableListOf<String>()

        // 提前获取翻译内容列表，避免重复查找
        val translationLyricList = if (!matchResult.isNullOrBlank()) {
            val decodedBytes = Base64.decode(matchResult, Base64.DEFAULT)
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(fanyiLyricbase::class.java)
            adapter.fromJson(decodedBytes.toString(Charsets.UTF_8))
                ?.content?.find { it.type == 1 }?.lyricContent
        } else null

        var lyricLineIndex = 0  // 只计数歌词行

        for (line in krcContent.lines()) {
            val trimmed = line.trim()

            // 判断是否是歌词行（带时间戳）
            if (!lineRegex.containsMatchIn(trimmed)) {
                // 不是歌词行，直接加，不加翻译，不增加索引
                output.add(trimmed)
                continue
            }

            // 是歌词行，解析时间和单词
            val lineMatch = lineRegex.find(line) ?: continue
            var lineStartTime = lineMatch.groupValues[1].toLong()

            if (lastLineStartTime != -1L && lineStartTime <= lastLineStartTime) {
                lineStartTime = lastLineStartTime + 3
            }
            lastLineStartTime = lineStartTime

            val wordMatches = wordRegex.findAll(line).toList()
            if (wordMatches.isEmpty()) continue


            val sb = StringBuilder()
            var pendingRole: String? = null

            for ((index, match) in wordMatches.withIndex()) {
                val offset = match.groupValues[1].toLong()
                val duration = match.groupValues[2].toLong()
                val word = match.groupValues[3]
                val time = lineStartTime + offset

                if (pendingRole != null) {
                    sb.append("[${millisToTimeStr(time)}]$pendingRole：")
                    pendingRole = null
                } else if (word.length == 1 && index + 1 < wordMatches.size &&
                    wordMatches[index + 1].groupValues[3] == "："
                ) {
                    pendingRole = word
                    continue
                } else if (word != "：") {
                    sb.append("[${millisToTimeStr(time)}]$word")
                }

                if (index == wordMatches.lastIndex) {
                    val endTime = time + duration
                    sb.append("[${millisToTimeStr(endTime)}]")
                }
            }

            output.add(sb.toString())

            // 添加对应翻译行，使用 lyricLineIndex
            val translationLine =
                translationLyricList?.getOrNull(lyricLineIndex)?.joinToString(separator = "") ?: " "
            if (translationLyricList != null) {
                output.add("[${millisToTimeStr(lineStartTime)}]$translationLine")
            }

            lyricLineIndex++  // 只在歌词行累加
        }

        return output.joinToString("\n")
    }


    fun millisToTimeStr(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val hundredths = (ms % 1000) / 10
        return "%02d:%02d.%02d".format(minutes, seconds, hundredths)
    }


    fun lrctimefix(a: String): String {

        val timeRegex = "\\[(\\d{2}):(\\d{2})\\.(\\d{2})]".toRegex()

        // Map 时间戳文本 -> 它出现的行索引
        val timeMap = mutableMapOf<String, MutableList<Int>>()
        val inputLines = a.lines()

        inputLines.forEachIndexed { index, line ->
            val match = timeRegex.find(line)
            match?.value?.let { timeTag ->
                timeMap.computeIfAbsent(timeTag) { mutableListOf() }.add(index)
            }
        }

        val linesCopy = inputLines.toMutableList()

        for ((timeTag, indices) in timeMap) {
            if (indices.size >= 3) {
                for ((offset, i) in indices.withIndex()) {
                    val match = timeRegex.find(timeTag)
                    if (match != null) {
                        val minutes = match.groupValues[1].toInt()
                        val seconds = match.groupValues[2].toInt()
                        val hundredths = match.groupValues[3].toInt()

                        // 原时间戳 + offset * 10ms
                        var totalMillis =
                            (minutes * 60 + seconds) * 1000 + hundredths * 10 + offset * 10

                        val newMinutes = totalMillis / 60000
                        val newSeconds = (totalMillis % 60000) / 1000
                        val newHundredths = (totalMillis % 1000) / 10

                        val newTimeTag =
                            "[%02d:%02d.%02d]".format(newMinutes, newSeconds, newHundredths)
                        // 替换原行中的时间戳
                        linesCopy[i] = linesCopy[i].replace(timeRegex, newTimeTag)
                    }
                }
            }
        }

        // 输出结果
        return linesCopy.joinToString("\n")
    }

    suspend fun saveCurrentPlaylist(player: Player, dao: PlaylistRepository) {
        val itemCount = player.mediaItemCount
        val entities = mutableListOf<MediaItemEntity>()

        for (i in 0 until itemCount) {
            val mediaItem = player.getMediaItemAt(i)
            val entity = mediaItem.toEntity()
            entities.add(entity)
        }

        // 批量插入数据库
        dao.savePlaylist(entities)
    }

    inline fun <reified T> SharedPreferences.use(
        relax: Boolean = false,
        doIt: SharedPreferences.() -> T
    ): T {
        return allowDiskAccessInStrictMode(relax) { doIt() }
    }


    // use below functions if accessing from UI thread only
    @Suppress("NOTHING_TO_INLINE")
    @Contract(value = "_,!null->!null")
    inline fun SharedPreferences.getStringStrict(key: String, defValue: String?): String? {
        return use { getString(key, defValue) }
    }


    fun isFirstRun(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirst = prefs.getBoolean("is_first_run", true)
        if (isFirst) {
            prefs.edit().putBoolean("is_first_run", false).apply()
        }
        return isFirst
    }


}

// the whole point of this function is to do literally nothing at all (but without impacting
// performance) in release builds and ignore StrictMode violations in debug builds
inline fun <reified T> allowDiskAccessInStrictMode(relax: Boolean = false, doIt: () -> T): T {
    return if (BuildConfig.DEBUG) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            if (relax) doIt() else
                throw IllegalStateException("allowDiskAccessInStrictMode(false) on wrong thread")
        } else {
            val policy = StrictMode.allowThreadDiskReads()
            try {
                StrictMode.allowThreadDiskWrites()
                doIt()
            } finally {
                StrictMode.setThreadPolicy(policy)
            }
        }
    } else doIt()
}
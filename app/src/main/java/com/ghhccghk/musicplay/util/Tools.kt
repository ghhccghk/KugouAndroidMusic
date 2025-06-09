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
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.Insets
import androidx.core.graphics.createBitmap
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
import com.ghhccghk.musicplay.data.libraries.uri
import com.ghhccghk.musicplay.service.PlayService.Companion.SERVICE_GET_AUDIO_FORMAT
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.io.File
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
                if (it.containsKey("bitrate")) it.getLong("bitrate") else null,
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




}
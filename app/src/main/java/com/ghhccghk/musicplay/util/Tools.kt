package com.ghhccghk.musicplay.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.net.toFile
import androidx.core.os.BundleCompat
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.util.Locale
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.libraries.uri
import com.ghhccghk.musicplay.service.PlayService.Companion.SERVICE_GET_AUDIO_FORMAT
import java.io.File

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
        return this.uri?.toFile()
    }



}
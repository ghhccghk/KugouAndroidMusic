package com.ghhccghk.musicplay.util.others

import android.graphics.Bitmap
import androidx.compose.runtime.Stable
import androidx.core.graphics.scale

/**
 * 播放界面背景动态效果
 */


val NowplayingBackgroundEffect : Boolean = true

@Stable
object BitmapResolver {
    fun bitmapCompress(bitmap: Bitmap, lowQuality: Boolean = false): Bitmap {
        val px = if (lowQuality) 4 else (if (NowplayingBackgroundEffect) 96 else 32)
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        var compressedBitmap = bitmap

        val size = minOf(originalWidth, originalHeight)
        val xOffset = (originalWidth - size) / 2
        val yOffset = (originalHeight - size) / 2
        val squareBitmap = Bitmap.createBitmap(bitmap, xOffset, yOffset, size, size)

        if (size > px) {
            val scaleFactor = size / px
            val scaledSize = size / scaleFactor
            compressedBitmap = squareBitmap.scale(scaledSize, scaledSize)
        }

        val config = Bitmap.Config.RGB_565
        return compressedBitmap.copy(config, false)
    }
}
package com.ghhccghk.musicplay.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

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
}
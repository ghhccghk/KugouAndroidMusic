@file:Suppress("DEPRECATION")

package com.ghhccghk.musicplay.util.others

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.Stable

@Stable
class Vibrator {
    companion object {
        fun click(context: Context) {
            val vibrator =
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(
                    VibrationEffect.createPredefined(
                        VibrationEffect.EFFECT_CLICK
                    )
                )
            } else {
                vibrator.vibrate(30)
            }
        }

        fun longClick(context: Context) {
            val vibrator =
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(
                    VibrationEffect.createPredefined(
                        VibrationEffect.EFFECT_HEAVY_CLICK
                    )
                )
            } else {
                vibrator.vibrate(30)
            }
        }

        fun doubleClick(context: Context) {
            val vibrator =
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(
                    VibrationEffect.createPredefined(
                        VibrationEffect.EFFECT_DOUBLE_CLICK
                    )
                )
            } else {
                vibrator.vibrate(30)
            }
        }
    }
}
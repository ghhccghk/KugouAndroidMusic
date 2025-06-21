package com.ghhccghk.musicplay.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.preference.Preference
import com.ghhccghk.musicplay.R

class NoAppFallbackPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
    init {
        onPreferenceClickListener = OnPreferenceClickListener { _ ->
            intent?.let {
                try {
                    context.startActivity(it)
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_LONG).show()
                }
            } != null
        }
    }
}
package com.ghhccghk.musicplay

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatDelegate
import androidx.media3.common.util.UnstableApi
import com.ghhccghk.musicplay.util.TokenManager
import com.ghhccghk.musicplay.util.UrlCacheManager
import org.nift4.gramophone.hificore.UacManager

class MainApplication: Application() {

    lateinit var uacManager: UacManager
        private set

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        uacManager = UacManager(this)
        UrlCacheManager.init(this)
        TokenManager.init(this)

        val prefs = getSharedPreferences("play_setting_prefs", Context.MODE_PRIVATE)

        when (prefs.getString("theme_mode", "0")) {
            "0" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                Log.d("MainActivity", "theme_mode is ${prefs.getString("theme_mode", "0")}")
            }

            "1" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Log.d("MainActivity", "theme_mode is ${prefs.getString("theme_mode", "0")}")
            }

            "2" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Log.d("MainActivity", "theme_mode is ${prefs.getString("theme_mode", "0")}")
            }
        }
    }
}
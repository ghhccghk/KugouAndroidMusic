package com.ghhccghk.musicplay

import android.app.Application
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi

class MainApplication: Application() {
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

    }
}
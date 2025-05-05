package com.ghhccghk.musicplay.util

import android.util.Log

object NodeBridge {
    init {
        // 加载本地库
        System.loadLibrary("node")
        System.loadLibrary("native-lib")
    }

    // 声明本地方法
    @JvmStatic
    external fun startNode()

    // Kotlin 中的静态方法，用于日志输出
    @JvmStatic
    fun logFromNative(msg: String) {
        Log.d("KugouApi", msg)
    }
}

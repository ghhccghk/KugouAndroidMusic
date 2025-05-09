package com.ghhccghk.musicplay.util

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ghhccghk.musicplay.MainActivity

object NodeBridge {

    const val ACTION_NODE_READY = "com.ghhccghk.musicplay.KugouApi_NODE_READY"

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
        if (msg.contains("server running @ http://localhost:9600")) {
            // 发送广播通知服务已启动
            val intent = Intent(ACTION_NODE_READY)
            LocalBroadcastManager.getInstance(MainActivity.lontext).sendBroadcast(intent)
        }
    }
}

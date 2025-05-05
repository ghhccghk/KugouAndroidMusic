package com.ghhccghk.musicplay.service

// NodeService.kt
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.util.NodeBridge

class NodeService : Service() {

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        Log.d("NodeService", "服务创建，准备启动 Node.js")
        Thread {
            try {
                NodeBridge.startNode() // 这里调用 native 方法
                Log.d("NodeService", "Node.js 启动完成")
            } catch (e: Exception) {
                Log.e("NodeService", "启动 Node.js 失败", e)
            }
        }.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY 表示服务被杀后自动重启
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

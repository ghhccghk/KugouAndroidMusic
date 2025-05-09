package com.ghhccghk.musicplay.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ghhccghk.musicplay.util.NodeBridge

class NodeService : Service() {


    private val binder = LocalBinder()
    var isNodeRunning = false
    var isNodeRunError : String = ""
    private val nodeReadyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == NodeBridge.ACTION_NODE_READY) {
                isNodeRunning = true
                Log.d("NodeService", "Node.js 已完全启动")
            }
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): NodeService = this@NodeService
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(NodeBridge.ACTION_NODE_READY)
        LocalBroadcastManager.getInstance(this).registerReceiver(nodeReadyReceiver, filter)

        Log.d("NodeService", "服务创建，准备启动 Node.js")
        Thread {
            try {
                NodeBridge.startNode() // 这里调用 native 方法
                Log.d("NodeService", "Node.js 启动完成")
                isNodeRunning = true
            } catch (e: Exception) {
                Log.e("NodeService", "启动 Node.js 失败", e)
                isNodeRunning = false
                isNodeRunError = e.toString()
            }
        }.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY 表示服务被杀后自动重启
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        isNodeRunning = false
    }
}

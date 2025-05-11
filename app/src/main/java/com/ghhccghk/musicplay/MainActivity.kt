package com.ghhccghk.musicplay

import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import com.google.android.material.navigation.NavigationBarView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ghhccghk.musicplay.databinding.ActivityMainBinding
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.ghhccghk.musicplay.service.NodeService
import com.ghhccghk.musicplay.util.NodeBridge
import com.ghhccghk.musicplay.util.ZipExtractor
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var nodeService: NodeService? = null
    var isNodeRunning = false
    var bound = false

    private val nodeReadyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == NodeBridge.ACTION_NODE_READY) {
                isNodeRunning = true
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as NodeService.LocalBinder
            nodeService = binder.getService()
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bound = false
            isNodeRunning = false
            nodeService = null
        }
    }

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        enableEdgeToEdge()

        val filter = IntentFilter(NodeBridge.ACTION_NODE_READY)
        LocalBroadcastManager.getInstance(this).registerReceiver(nodeReadyReceiver, filter)
        Intent(this, NodeService::class.java).also {
            bindService(it, connection, BIND_AUTO_CREATE)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent(this, NodeService::class.java)
        startService(intent)


        if (isFirstRun(this)) {
            ZipExtractor.extractZipOnFirstRun(this, "api_js.zip", "nodejs_files")
        }

        val navView: NavigationBarView = binding.navView
        val playerBar = findViewById<LinearLayout>(R.id.player_bar)
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        navView.setupWithNavController(navController)
        playerBar.setOnClickListener {

            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.fragment_enter)    // 进入动画
                .setExitAnim(R.anim.fragment_exit)      // 退出动画
                .setPopEnterAnim(R.anim.fragment_pop_enter)  // 弹出时的进入动画（从下往上）
                .setPopExitAnim(R.anim.fragment_pop_exit)    // 弹出时的退出动画（从上往下）
                .build()
            // 跳转到播放 Fragment
            navController.navigate(R.id.playerFragment,null,navOptions)

            // 隐藏 BottomNavigationView
            val a = findViewById<BottomNavigationView>(R.id.nav_view)
            hideBottomNav(a)
            hideLinearLayout(playerBar,a)
        }
    }

    fun isFirstRun(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirst = prefs.getBoolean("is_first_run", true)
        if (isFirst) {
            prefs.edit() { putBoolean("is_first_run", false) }
        }
        return isFirst
    }


    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    // 处理返回时的操作，确保返回时显示 BottomNavigationView
    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // 只有当不在播放 Fragment 时才显示 BottomNavigationView
        if (findNavController(R.id.nav_host_fragment_activity_main).currentDestination?.id == R.id.playerFragment) {
            super.onBackPressed()
            val a = findViewById<BottomNavigationView>(R.id.nav_view)
            showBottomNav(a)
            val b = findViewById<LinearLayout>(R.id.player_bar)
            showLinearLayout(b,a)
        } else {
            super.onBackPressed()  // 调用系统的默认行为
        }
    }

    private fun showBottomNav(bottomNav: BottomNavigationView) {
        bottomNav.visibility = View.VISIBLE
        val slideIn = ObjectAnimator.ofFloat(bottomNav, "translationY", bottomNav.height.toFloat(), 0f)
        slideIn.duration = 100
        slideIn.start()

    }

    private fun hideBottomNav(bottomNav: BottomNavigationView) {
        val slideOut = ObjectAnimator.ofFloat(bottomNav, "translationY", 0f, bottomNav.height.toFloat())
        slideOut.duration = 100
        slideOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                bottomNav.visibility = View.GONE
            }
        })
        slideOut.start()
    }

    private fun showLinearLayout(bottomNav: LinearLayout,play: BottomNavigationView) {
        bottomNav.visibility = View.VISIBLE
        val slideIn = ObjectAnimator.ofFloat(bottomNav, "translationY", bottomNav.height.toFloat() + play.height.toFloat(), 0f)
        slideIn.duration = 100
        slideIn.start()
    }

    private fun hideLinearLayout(bottomNav: LinearLayout,play: BottomNavigationView) {
        val slideOut = ObjectAnimator.ofFloat(bottomNav, "translationY", 0f, bottomNav.height.toFloat() + play.height.toFloat())
        slideOut.duration = 100
        slideOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                bottomNav.visibility = View.GONE
                play.visibility = View.GONE
            }
        })
        slideOut.start()
    }

    companion object {
        private lateinit var instance: MainActivity
        val lontext: Context
            get() = instance.applicationContext
        val isNodeRunning : Boolean
            get() = instance.isNodeRunning
        val bound : Boolean
            get() = instance.bound
    }

}
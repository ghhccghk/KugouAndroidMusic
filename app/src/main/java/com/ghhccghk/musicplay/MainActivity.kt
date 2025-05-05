package com.ghhccghk.musicplay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.google.android.material.navigation.NavigationBarView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ghhccghk.musicplay.databinding.ActivityMainBinding
import androidx.core.content.edit
import com.ghhccghk.musicplay.service.NodeService
import com.ghhccghk.musicplay.util.ZipExtractor

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent(this, NodeService::class.java)
        startService(intent)


        if (isFirstRun(this)) {
            ZipExtractor.extractZipOnFirstRun(this, "api_js.zip", "nodejs_files")
        }

        val navView: NavigationBarView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        navView.setupWithNavController(navController)
    }

    fun isFirstRun(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirst = prefs.getBoolean("is_first_run", true)
        if (isFirst) {
            prefs.edit() { putBoolean("is_first_run", false) }
        }
        return isFirst
    }

}
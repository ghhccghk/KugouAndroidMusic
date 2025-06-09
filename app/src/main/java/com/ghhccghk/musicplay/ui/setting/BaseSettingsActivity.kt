package com.ghhccghk.musicplay.ui.setting

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.util.Tools.enableEdgeToEdgePaddingListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar

abstract class BaseSettingsActivity(
    private val str: Int,
    private val fragmentCreator: () -> Fragment
) : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_top_settings)
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        val collapsingToolbar = findViewById<CollapsingToolbarLayout>(R.id.collapsingtoolbar)

        findViewById<AppBarLayout>(R.id.appbarlayout).enableEdgeToEdgePaddingListener()
        collapsingToolbar.title = getString(str)

        topAppBar.setNavigationOnClickListener {
            finish()
        }

        supportFragmentManager
            .beginTransaction()
            .add(R.id.settings, fragmentCreator())
            .commit()
    }
}
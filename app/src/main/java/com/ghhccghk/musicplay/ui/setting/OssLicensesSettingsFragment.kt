package com.ghhccghk.musicplay.ui.setting

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.mikepenz.aboutlibraries.ui.compose.android.rememberLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer


class OssLicensesSettingsActivity : AppCompatActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (isSystemInDarkTheme()) dynamicDarkColorScheme(MainActivity.lontext) else dynamicLightColorScheme(
                        MainActivity.lontext
                    )
                } else {
                    if (isSystemInDarkTheme()) {
                        darkColorScheme()    // 静态深色方案
                    } else {
                        lightColorScheme()   // 静态亮色方案
                    }
                }
            ) {
                val libraries by rememberLibraries(R.raw.aboutlibraries)
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.settings_open_source_licenses)) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                                }
                            }
                        )
                    },
                    content = { paddingValues ->
                        LibrariesContainer(
                            libraries = libraries,
                            contentPadding = paddingValues
                        )
                    }
                )
            }
        }
    }
}

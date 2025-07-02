package com.ghhccghk.musicplay.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.ghhccghk.musicplay.R

@SuppressLint("RestrictedApi")
class LyricGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
        DpSize(120.dp, 60.dp),
        DpSize(240.dp, 120.dp),
        DpSize(480.dp, 200.dp),
    ))

    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val last = prefs[stringPreferencesKey("line_last")] ?: ""
            val current = prefs[stringPreferencesKey("line_current")] ?: ""
            val next = prefs[stringPreferencesKey("line_next")] ?: ""

            val size = LocalSize.current

            val isCompact = size.width < 120.dp || size.height < 60.dp
            val isMedium = size.width < 200.dp

            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(R.color.black)) // 自定义颜色
                        .padding(8.dp)
                ) {
                    if (isCompact) {
                        CompactLayout(current)
                    } else if (isMedium) {
                        MediumLayout(last, current, next)
                    } else {
                        FullLayout(last, current, next)
                    }
                }
            }
        }
    }

    @Composable
    fun CompactLayout(current: String) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = current,
                style = TextStyle(fontSize = 16.sp, color = ColorProvider(Color.White))
            )
        }
    }

    @Composable
    fun MediumLayout(last: String,current: String, next: String) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = last, style = TextStyle(fontSize = 16.sp, color = ColorProvider(Color.Gray)))
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(text = current, style = TextStyle(fontSize = 16.sp, color = ColorProvider(Color.White)))
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(text = next, style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color.Gray)))
        }
    }


    @Composable
    fun FullLayout(last: String, current: String, next: String) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = last, style = TextStyle(fontSize = 16.sp, color = ColorProvider(Color.Gray)))
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(text = current, style = TextStyle(fontSize = 20.sp, color = ColorProvider(Color.White)))
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(text = next, style = TextStyle(fontSize = 16.sp, color = ColorProvider(Color.Gray)))
        }
    }
}


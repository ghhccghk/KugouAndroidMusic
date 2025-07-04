package com.ghhccghk.musicplay

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.ghhccghk.musicplay.ui.LyricGlanceWidget

class LyricGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LyricGlanceWidget()
}

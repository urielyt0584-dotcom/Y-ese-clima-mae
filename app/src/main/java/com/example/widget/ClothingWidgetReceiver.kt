package com.example.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class ClothingWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ClothingWidget()

    override fun onReceive(context: Context, intent: Intent) {
        try {
            intent.extras?.keySet()
            super.onReceive(context, intent)
        } catch (t: Throwable) {
            Log.w("ClothingWidgetReceiver", "Ignored malformed or malicious broadcast intent", t)
        }
    }
}

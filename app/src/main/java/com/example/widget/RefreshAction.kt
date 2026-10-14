package com.example.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.data.local.AppDatabase
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val dao = db.weatherDao()
                val latest = dao.getLatestWeatherOnce()
                if (latest != null) {
                    val repository = WeatherRepository(weatherDao = dao)
                    repository.getWeatherData(
                        latitude = latest.latitude,
                        longitude = latest.longitude,
                        locationName = latest.cityName,
                        forceRefresh = true
                    )
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
        ClothingWidget().update(context, glanceId)
    }
}

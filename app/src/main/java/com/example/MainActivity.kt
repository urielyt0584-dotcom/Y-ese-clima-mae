package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.awaitCancellation
import com.example.ui.WeatherScreen
import com.example.ui.WeatherViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {

  private val weatherViewModel: WeatherViewModel by viewModels {
    WeatherViewModel.Factory(application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Security: sanitize incoming intent extras against Intent Fuzzing and BadParcelable attacks
    sanitizeIntentExtras(intent)

    enableEdgeToEdge()

    // Initialize the notification channel for Tico weather alerts
    try {
      NotificationHelper.createNotificationChannel(this)
    } catch (t: Throwable) {
      Log.w("MainActivity", "Failed to initialize notification channel", t)
    }

    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.RESUMED) {
        try {
          WeatherViewModel.isAppInForeground = true
          Log.d("WeatherRefresh", "Foreground polling started")
          weatherViewModel.autoRefreshWeather()
          weatherViewModel.startForegroundBarometerCheck()
          while (isActive) {
            delay(5 * 60 * 1000L)
            weatherViewModel.autoRefreshWeather()
          }
        } catch (t: Throwable) {
          Log.w("WeatherRefresh", "Auto-refresh error during resumed state", t)
        } finally {
          WeatherViewModel.isAppInForeground = false
          weatherViewModel.stopForegroundBarometerCheck()
          Log.d("WeatherRefresh", "Foreground polling stopped")
        }
      }
    }

    setContent {
      val isWatch = packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_WATCH)
      
      if (isWatch) {
          com.example.ui.wear.WearWeatherScreen(viewModel = weatherViewModel)
      } else {
          MyApplicationTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
              WeatherScreen(viewModel = weatherViewModel)
            }
          }
      }
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    sanitizeIntentExtras(intent)
  }

  private fun sanitizeIntentExtras(targetIntent: android.content.Intent?) {
    if (targetIntent == null) return
    try {
      targetIntent.extras?.keySet()
    } catch (e: Throwable) {
      Log.w("MainActivity", "Intent extras corrupted or malicious, stripping to prevent crash", e)
      targetIntent.replaceExtras(Bundle())
    }
  }
}


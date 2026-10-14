package com.example.util

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("y_ese_clima_prefs", Context.MODE_PRIVATE)

    var isFamilyFriendlyMode: Boolean
        get() = prefs.getBoolean("family_friendly_mode", true)
        set(value) = prefs.edit().putBoolean("family_friendly_mode", value).apply()

    var isNotificationsEnabled: Boolean
        get() = prefs.getBoolean("notifications_enabled", true)
        set(value) = prefs.edit().putBoolean("notifications_enabled", value).apply()

    var sanPedroBlameCount: Int
        get() = prefs.getInt("san_pedro_blame_count", 0)
        set(value) = prefs.edit().putInt("san_pedro_blame_count", value).apply()

    var isBarometerAlertsEnabled: Boolean
        get() = prefs.getBoolean("barometer_alerts_enabled", true)
        set(value) = prefs.edit().putBoolean("barometer_alerts_enabled", value).apply()

    var isBarometerAltitudeFilterEnabled: Boolean
        get() = prefs.getBoolean("barometer_altitude_filter_enabled", true)
        set(value) = prefs.edit().putBoolean("barometer_altitude_filter_enabled", value).apply()
}

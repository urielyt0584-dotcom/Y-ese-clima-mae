package com.example

import android.app.Application
import android.util.Log

/**
 * Main application class providing global crash protection,
 * defensive exception handling, and lifecycle safety.
 */
class TicoClimaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Protect against uncaught thread exceptions that could take down the app process.
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("TicoClimaSecurity", "Capturada excepción no controlada en hilo '${thread.name}': ${throwable.message}")
            } catch (_: Throwable) {
                // Ignore any secondary logging failures
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}

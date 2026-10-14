package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.WeatherCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class NotificationStatus {
    SUCCESS,
    PERMISSION_DENIED,
    NOTIFICATIONS_DISABLED_SYSTEM,
    ERROR
}

object NotificationHelper {

    // Upgraded channel ID to v6 to force fresh OS settings with Heads-Up (pop up), sound, and lights
    const val CHANNEL_ID = "tico_weather_alerts_popup_v6"
    private const val NOTIFICATION_ID = 1001
    private const val NOTIFICATION_ID_BAROMETER = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Remove legacy channels to force OS recreation with Heads-Up & lights
            listOf(
                "tico_weather_alerts_channel",
                "tico_weather_alerts_channel_v2",
                "tico_weather_alerts_channel_v3",
                "tico_weather_alerts_headsup_v4",
                "tico_weather_alerts_headsup_v5"
            ).forEach { oldChannelId ->
                try {
                    notificationManager.deleteNotificationChannel(oldChannelId)
                } catch (_: Throwable) {}
            }

            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_desc)

            // IMPORTANCE_HIGH enables Heads-Up banner pop-ups over apps/screen, sound, vibration, and lights
            val importance = NotificationManager.IMPORTANCE_HIGH
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.rgb(255, 179, 0) // Amber / Golden alert light
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
                setBypassDnd(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Strobe the device's camera flashlight if available (3 energetic pulses)
     * with sufficient pulse width (300ms) for hardware LED driver ignition.
     */
    fun flashDeviceTorch(context: Context, pulses: Int = 3) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
                
                // Prioritize rear camera with flash, or fallback to any camera with flash
                val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                    try {
                        val characteristics = cameraManager.getCameraCharacteristics(id)
                        val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                        val isBack = characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
                        hasFlash && isBack
                    } catch (_: Throwable) {
                        false
                    }
                } ?: cameraManager.cameraIdList.firstOrNull { id ->
                    try {
                        val characteristics = cameraManager.getCameraCharacteristics(id)
                        characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    } catch (_: Throwable) {
                        false
                    }
                } ?: return

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        repeat(pulses) {
                            cameraManager.setTorchMode(cameraId, true)
                            delay(300)
                            cameraManager.setTorchMode(cameraId, false)
                            delay(180)
                        }
                    } catch (t: Throwable) {
                        Log.d("NotificationHelper", "Torch pulse interrupted: ${t.message}")
                    } finally {
                        try {
                            cameraManager.setTorchMode(cameraId, false)
                        } catch (_: Throwable) {}
                    }
                }
            } catch (t: Throwable) {
                Log.d("NotificationHelper", "Flashlight pulse not supported: ${t.message}")
            }
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun areSystemNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun getAlertTexts(category: WeatherCategory?, isFamilyFriendlyMode: Boolean): Pair<String, String> {
        val title = if (isFamilyFriendlyMode) {
            when (category) {
                WeatherCategory.THUNDERSTORM -> "⚡ ¡Alerta de tormenta, mae!"
                WeatherCategory.HEAVY_RAIN, WeatherCategory.RAIN -> "🌧️ ¡Se vino el aguacero!"
                WeatherCategory.HIGH_UV, WeatherCategory.EXTREME_HEAT -> "☀️ ¡El sol está que quema, mae!"
                WeatherCategory.COLD -> "🥶 ¡Qué frío está haciendo, pura vida!"
                else -> "🌦️ ¿Y Ese Clima, Mae?"
            }
        } else {
            when (category) {
                WeatherCategory.THUNDERSTORM -> "⚡ ¡ALERTA MAE: TORMENTA ELÉCTRICA!"
                WeatherCategory.HEAVY_RAIN, WeatherCategory.RAIN -> "🌧️ ¡Diay Mae, se vino el agua!"
                WeatherCategory.HIGH_UV, WeatherCategory.EXTREME_HEAT -> "☀️ ¡Mae, el Sol anda criminal!"
                WeatherCategory.COLD -> "🥶 ¡Mae, qué frío más hijueputa!"
                else -> "🌦️ ¿Y Ese Clima, Mae?"
            }
        }

        val message = if (isFamilyFriendlyMode) {
            when (category) {
                WeatherCategory.THUNDERSTORM -> "Hay rayería, mae. Busque refugio al suave."
                WeatherCategory.HEAVY_RAIN, WeatherCategory.RAIN -> "Parece que va a llover pronto. No olvide la sombrilla."
                WeatherCategory.HIGH_UV, WeatherCategory.EXTREME_HEAT -> "El índice UV está muy alto. Póngase bloqueador."
                WeatherCategory.COLD -> "Aproveche el fresquito y abríguese bien, mae."
                else -> "Asómese a la ventana antes de salir, pura vida."
            }
        } else {
            when (category) {
                WeatherCategory.THUNDERSTORM -> "ALERTA MAE: tormenta eléctrica. Métase a la choza antes de que San Pedro lo fulmine."
                WeatherCategory.HEAVY_RAIN, WeatherCategory.RAIN -> "Mae, va a llover en 20 minutos. Si sale sin paraguas, ya eso es problema suyo."
                WeatherCategory.HIGH_UV, WeatherCategory.EXTREME_HEAT -> "Mae, el Sol está criminal hoy. Bloqueador. YA."
                WeatherCategory.COLD -> "Mae, saque la cobija de tigre y ponga café chorreado ya."
                else -> "Mae, asómese a la ventana antes de salir a la calle."
            }
        }

        return Pair(title, message)
    }

    fun sendTicoWeatherAlert(
        context: Context,
        title: String? = null,
        message: String? = null,
        category: WeatherCategory? = null,
        isFamilyFriendlyMode: Boolean = true
    ): NotificationStatus {
        if (!areSystemNotificationsEnabled(context)) {
            Log.w("NotificationHelper", "Notifications are disabled at the system level")
            return NotificationStatus.NOTIFICATIONS_DISABLED_SYSTEM
        }

        if (!hasNotificationPermission(context)) {
            Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted")
            return NotificationStatus.PERMISSION_DENIED
        }

        createNotificationChannel(context)

        val (defaultTitle, defaultMessage) = getAlertTexts(category, isFamilyFriendlyMode)
        val alertTitle = title ?: defaultTitle
        val alertMessage = message ?: defaultMessage

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // WearableExtender ensures native notification bridging and rich display on Wear OS watches
        val wearableExtender = NotificationCompat.WearableExtender()
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(alertTitle)
            .setContentText(alertMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alertMessage))
            .setPriority(NotificationCompat.PRIORITY_MAX) // PRIORITY_MAX triggers Heads-Up pop up banner
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setLights(Color.rgb(255, 179, 0), 1000, 500)
            .setFullScreenIntent(pendingIntent, false) // Explicitly informs OS to present as high-priority heads-up pop up
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .extend(wearableExtender)

        // Flash hardware torch if supported
        flashDeviceTorch(context, pulses = 3)

        return try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
            NotificationStatus.SUCCESS
        } catch (e: Throwable) {
            Log.w("NotificationHelper", "Failed to post Tico weather alert", e)
            NotificationStatus.ERROR
        }
    }

    fun sendBarometerRainAlert(
        context: Context,
        prediction: BarometerPrediction,
        isFamilyFriendlyMode: Boolean = true
    ): NotificationStatus {
        if (!areSystemNotificationsEnabled(context)) {
            Log.w("NotificationHelper", "Notifications disabled at system level")
            return NotificationStatus.NOTIFICATIONS_DISABLED_SYSTEM
        }

        if (!hasNotificationPermission(context)) {
            Log.w("NotificationHelper", "POST_NOTIFICATIONS not granted for barometer alert")
            return NotificationStatus.PERMISSION_DENIED
        }

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val wearableExtender = NotificationCompat.WearableExtender()
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(prediction.title)
            .setContentText(prediction.description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(prediction.description))
            .setPriority(NotificationCompat.PRIORITY_MAX) // PRIORITY_MAX triggers Heads-Up pop up banner
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setLights(Color.rgb(255, 179, 0), 1000, 500)
            .setFullScreenIntent(pendingIntent, false) // Explicitly informs OS to present as high-priority heads-up pop up
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .extend(wearableExtender)

        // Flash hardware torch if supported
        flashDeviceTorch(context, pulses = 3)

        return try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID_BAROMETER, builder.build())
            }
            NotificationStatus.SUCCESS
        } catch (e: Throwable) {
            Log.w("NotificationHelper", "Failed to post barometer rain alert", e)
            NotificationStatus.ERROR
        }
    }
}

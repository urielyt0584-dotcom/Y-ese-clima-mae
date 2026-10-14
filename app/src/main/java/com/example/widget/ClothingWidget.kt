package com.example.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.ImageProvider
import androidx.glance.Image
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import android.content.Intent
import android.content.ComponentName
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.domain.ClothingRecommendationEngine
import com.example.util.SettingsManager
import com.example.domain.ClothingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClothingWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Fetch cached data and try to update if needed
        val weatherEntity = withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val dao = db.weatherDao()
                var latest = dao.getLatestWeatherOnce()
                
                if (latest != null) {
                    val currentTime = System.currentTimeMillis()
                    val cacheAgeMillis = currentTime - latest.cachedAt
                    // Only fetch from network if the cache is older than 3 hours (10800000 ms)
                    // Glance redraws frequently, we shouldn't trigger network calls on every redraw
                    // Also prohibit network calls if the app is currently in the foreground (it will handle its own updates)
                    if (cacheAgeMillis > 30 * 60 * 1000L && !com.example.ui.WeatherViewModel.isAppInForeground) {
                        try {
                            val repository = com.example.data.repository.WeatherRepository(weatherDao = dao)
                            repository.getWeatherData(
                                latitude = latest.latitude,
                                longitude = latest.longitude,
                                locationName = latest.cityName
                            )
                            latest = dao.getLatestWeatherOnce()
                        } catch (e: Exception) {
                            // Ignore network/repository errors and just use the cached version
                        }
                    }
                }
                latest
            } catch (e: Exception) {
                null
            }
        }

        provideContent {
            GlanceTheme {
                WidgetContent(weatherEntity)
            }
        }
    }

    @Composable
    private fun WidgetContent(entity: com.example.data.local.CachedWeatherEntity?) {
        val context = LocalContext.current
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val isFamilyFriendly = SettingsManager(context).isFamilyFriendlyMode
        val titles = if (isFamilyFriendly) {
            listOf("👕 Ropa para hoy", "👗 Sugerencia de ropa", "🧢 ¿Qué me pongo?", "🧥 Clima y estilo", "😎 Outfit del día")
        } else {
            listOf("👕 ROPA PA' HOY, PLAYO", "🧢 ¿QUÉ ME PONGO, MAE?", "👗 ALISTE EL CAITE", "🧥 PA' NO MORIRSE", "😎 QUÉ SE PONE, RASTA", "👕 ROPA PA' HOY, CAREPICHA", "🧥 VÍSTASE, FETO CON PATAS")
        }
        val fallbackMessages = if (isFamilyFriendly) {
            listOf(
                "“Sin conexión. Por favor revisa tu internet para ver las sugerencias de hoy.”",
                "“No tenemos datos del clima actuales. Asómate a la ventana para confirmar.”",
                "“Fallo de conexión. Espera un momento y vuelve a intentarlo.”"
            )
        } else {
            listOf(
                "“Mae, no tengo internet ni sé qué clima hace. Asome la jupa por la ventana, no sea vago.”",
                "“Estamos sin saldo (o sin internet). Salga y fíjese arriba a ver si llueve, mae.”",
                "“Puta sal, sin conexión. Fíjese en el cielo a lo antiguo, no dependa tanto del chunche este.”",
                "“Mae, coman picha todos. No tengo internet para revisar esta vara.”",
                "“Ey usted, feto con patas, fíjese a ver si llueve afuera, no me pida a mí que no tengo conexión.”"
            )
        }

        // Use a consistent index based on the day or current time so it doesn't flicker on minor Glance updates
        val titleIndex = ((System.currentTimeMillis() / 1000) % titles.size).toInt()
        val title = titles[titleIndex]
        
        val fallbackIndex = ((System.currentTimeMillis() / 1000) % fallbackMessages.size).toInt()
        val fallbackMsg = fallbackMessages[fallbackIndex]

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(16.dp)
                .clickable(actionStartActivity(intent))
        ) {
            if (entity == null) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = fallbackMsg,
                        style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 14.sp)
                    )
                }
            } else {
                val ticoPhrases = if (isFamilyFriendly) {
                    listOf(
                        "¡Pura vida, mae!",
                        "Tuanis el clima de hoy.",
                        "A cachete, todo tranquilo.",
                        "Al suave, lleve paraguas por si las moscas.",
                        "Qué chiva el día, disfrútelo.",
                        "Apenas para un gallito de pinto.",
                        "Climita tuanis para tomar cafecito."
                    )
                } else {
                    listOf(
                        "¡Pura vida, carepicha!",
                        "Mae, este clima está de locos.",
                        "Aliste el yodo que va a llover, mae.",
                        "Coman picha con este clima.",
                        "Ey usted, feto con patas, salga con cuidado.",
                        "Mae, este clima está para mandarlo todo a la picha.",
                        "Qué agüevazón de día, mae.",
                        "Fíjese arriba antes de salir, no sea bestia."
                    )
                }

                val recommendation = ClothingRecommendationEngine.recommend(
                    isFamilyFriendlyMode = isFamilyFriendly,
                    temperature = entity.temperature,
                    feelsLike = entity.feelsLike,
                    humidity = entity.humidity,
                    precipitationProbability = entity.precipitationProbability,
                    windSpeed = entity.windSpeed,
                    uvIndex = entity.uvIndex,
                    weatherCondition = entity.conditionDescription,
                    isDay = entity.isDay
                )

                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    item {
                        // Header
                        Text(
                            text = title,
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                    
                    item {
                        // Weather summary
                        val temp = entity.temperature.toInt()
                        val hum = entity.humidity
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = GlanceModifier.fillMaxWidth()) {
                            Text(
                                text = "📍 ${entity.cityName} · 🌡️ $temp°C · 💧 $hum%",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )
                            Image(
                                provider = ImageProvider(android.R.drawable.ic_popup_sync),
                                contentDescription = "Actualizar",
                                modifier = GlanceModifier.clickable(actionRunCallback<RefreshAction>())
                            )
                        }
                        Spacer(modifier = GlanceModifier.height(12.dp))
                    }

                    // Recommended Items
                    items(recommendation.clothingItems) { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.emoji, style = TextStyle(fontSize = 14.sp))
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Text(
                                text = item.name,
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurface,
                                    fontSize = 14.sp
                                )
                            )
                        }
                        Spacer(modifier = GlanceModifier.height(4.dp))
                    }

                    // Absurd Item
                    recommendation.uselessItem?.let { useless ->
                        item {
                            Spacer(modifier = GlanceModifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = useless.emoji, style = TextStyle(fontSize = 14.sp))
                                Spacer(modifier = GlanceModifier.width(8.dp))
                                val uselessMessage = if (isFamilyFriendly) "${useless.name}: MALA IDEA" else "${useless.name}: ESTÁ MAMANDO"
                                Text(
                                    text = uselessMessage,
                                    style = TextStyle(
                                        color = GlanceTheme.colors.error,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            useless.sarcasticComment?.let { comment ->
                                Spacer(modifier = GlanceModifier.height(4.dp))
                                Text(
                                    text = "“$comment”",
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontStyle = androidx.glance.text.FontStyle.Italic
                                    )
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = GlanceModifier.height(12.dp))
                        // Verdict
                        Text(
                            text = "“${recommendation.verdict}”",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                fontStyle = androidx.glance.text.FontStyle.Italic
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(16.dp))
                    }

                    item {
                        // Tico Phrases Header
                        Text(
                            text = "🇨🇷 Frases Ticas (Desliza para ver más):",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                    
                    items(ticoPhrases) { phrase ->
                        Text(
                            text = "• $phrase",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(6.dp))
                    }

                    item {
                        Spacer(modifier = GlanceModifier.height(12.dp))
                        // Last updated
                        val sdf = SimpleDateFormat("HH:mm", Locale.US)
                        val timeString = sdf.format(Date(entity.cachedAt))
                        Text(
                            text = "Último dato: $timeString",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 10.sp
                            ),
                            modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

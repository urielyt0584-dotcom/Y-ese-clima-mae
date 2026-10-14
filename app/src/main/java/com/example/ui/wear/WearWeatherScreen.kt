package com.example.ui.wear

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import com.example.data.model.HourlyForecastItem
import com.example.data.model.WeatherData
import com.example.domain.WeatherShittinessCategory
import com.example.domain.WeatherShittinessResult
import com.example.ui.WeatherUiState
import com.example.ui.WeatherViewModel
import com.example.util.BarometerPrediction
import com.example.util.BarometerTrendType
import com.example.util.NotificationHelper

private fun getWearWeatherEmoji(weatherCode: Int, isDay: Boolean): String {
    return when (weatherCode) {
        0 -> if (isDay) "☀️" else "🌙"
        1, 2 -> if (isDay) "⛅" else "☁️"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55, 56, 57 -> "🌦️"
        61, 63 -> "🌧️"
        65 -> "⛈️"
        66, 67 -> "🌨️"
        71, 73, 75, 77, 85, 86 -> "❄️"
        80, 81, 82 -> "🌊"
        95, 96, 99 -> "⚡"
        else -> if (isDay) "🌤️" else "🌑"
    }
}

private fun getWsiColor(category: WeatherShittinessCategory): Color {
    return when (category) {
        WeatherShittinessCategory.PERFECT -> Color(0xFF00E676)
        WeatherShittinessCategory.PRETTY_GOOD -> Color(0xFF00B0FF)
        WeatherShittinessCategory.MEH -> Color(0xFFFFD600)
        WeatherShittinessCategory.GETTING_BAD -> Color(0xFFFF9100)
        WeatherShittinessCategory.BAD -> Color(0xFFFF6D00)
        WeatherShittinessCategory.VERY_BAD -> Color(0xFFFF3D00)
        WeatherShittinessCategory.ABSOLUTELY_HORRIBLE -> Color(0xFFFF1744)
    }
}

private fun getWsiSeveritySegments(score: Int): Int {
    return when {
        score <= 20 -> 1
        score <= 40 -> 2
        score <= 60 -> 3
        score <= 80 -> 4
        else -> 5
    }
}

/**
 * Pantalla principal de Wear OS optimizada para smartwatches.
 * Cuenta con Scaffold nativo (TimeText, Vignette, PositionIndicator),
 * Hero Dashboard ultra-glanceable, tarjeta WSI con medidor visual,
 * "La Píldora del Mae" estilo Tile y Carrusel de las siguientes 8 horas.
 */
@Composable
fun WearWeatherScreen(
    viewModel: WeatherViewModel,
    onExitWatchMode: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    MaterialTheme(
        colors = Colors(
            primary = Color(0xFFFF9800),
            primaryVariant = Color(0xFFF57C00),
            secondary = Color(0xFF00E5FF),
            background = Color.Black,
            surface = Color(0xFF14141A),
            error = Color(0xFFFF5252),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White,
            onError = Color.Black
        )
    ) {
        Scaffold(
            timeText = {
                TimeText(
                    modifier = Modifier.padding(top = 2.dp)
                )
            },
            vignette = {
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            },
            positionIndicator = {
                PositionIndicator(scalingLazyListState = listState)
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                when (val state = uiState) {
                    is WeatherUiState.Loading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp,
                                indicatorColor = Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Averiguando el clima...",
                                style = MaterialTheme.typography.caption2,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    is WeatherUiState.Success -> {
                        ScalingLazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            autoCentering = AutoCenteringParams(itemIndex = 1),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(top = 22.dp, start = 8.dp, end = 8.dp, bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Ubicación Badge (Tappable para refrescar)
                            item(key = "location_badge") {
                                CompactChip(
                                    onClick = { viewModel.refreshWeather() },
                                    label = {
                                        Text(
                                            text = "📍 ${state.weather.cityName}",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.caption2.copy(fontWeight = FontWeight.Bold)
                                        )
                                    },
                                    colors = ChipDefaults.chipColors(
                                        backgroundColor = Color(0xFF22222C),
                                        contentColor = Color(0xFF00E5FF)
                                    )
                                )
                            }

                            // 2. Rediseño Hero Dashboard (Glanceable en medio segundo)
                            item(key = "hero_dashboard") {
                                WearHeroDashboardCard(weather = state.weather)
                            }

                            // 3. WSI Card (Weather Shittiness Index) con medidor
                            item(key = "wsi_card") {
                                WearWsiCard(result = state.shittinessResult)
                            }

                            // 4. Barómetro Local del Reloj (Sensor Barométrico)
                            item(key = "barometer_card") {
                                WearBarometerCard(
                                    viewModel = viewModel,
                                    isFamilyFriendly = state.familyFriendlyEnabled
                                )
                            }

                            // 5. La Píldora del Mae (Tile glance card con dados interactivos)
                            item(key = "pildora_del_mae") {
                                WearPildoraDelMaeCard(
                                    comment = state.weather.sarcasticComment,
                                    onRollNewComment = { viewModel.rollNewComment() }
                                )
                            }

                            // 5. Carrusel de las siguientes 8 horas
                            item(key = "hourly_carousel_header") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⏰ Próximas 8 Horas",
                                        style = MaterialTheme.typography.caption2.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFFFB74D)
                                    )
                                }
                            }

                            item(key = "hourly_carousel") {
                                val next8Hours = remember(state.weather.hourlyForecasts) {
                                    state.weather.hourlyForecasts.take(8)
                                }
                                WearHourlyCarousel(items = next8Hours)
                            }

                            // 6. Action: Refrescar Clima
                            item(key = "refresh_action") {
                                CompactChip(
                                    onClick = { viewModel.refreshWeather() },
                                    label = {
                                        Text(
                                            text = if (state.isRefreshing) "Actualizando..." else "🔄 Actualizar",
                                            style = MaterialTheme.typography.caption2.copy(fontWeight = FontWeight.Bold)
                                        )
                                    },
                                    colors = ChipDefaults.chipColors(
                                        backgroundColor = Color(0xFF2C2C38),
                                        contentColor = Color(0xFFFFB74D)
                                    ),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            // 7. Salir del modo reloj / preview (si está habilitado)
                            if (onExitWatchMode != null) {
                                item(key = "exit_preview") {
                                    CompactChip(
                                        onClick = onExitWatchMode,
                                        label = {
                                            Text(
                                                text = "📱 Volver al cel",
                                                style = MaterialTheme.typography.caption3.copy(fontWeight = FontWeight.Medium)
                                            )
                                        },
                                        colors = ChipDefaults.chipColors(
                                            backgroundColor = Color(0xFF1E1E24),
                                            contentColor = Color.White.copy(alpha = 0.75f)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    is WeatherUiState.Error -> {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "⚠️ Mae, fallo:",
                                style = MaterialTheme.typography.caption1.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFF5252),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.ticoMessage,
                                style = MaterialTheme.typography.caption2,
                                textAlign = TextAlign.Center,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.refreshWeather() },
                                modifier = Modifier.size(ButtonDefaults.SmallButtonSize),
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF9800))
                            ) {
                                Text("🔄", style = MaterialTheme.typography.body2)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Hero Dashboard ultra-glanceable para pantallas de Wear OS.
 * Muestra temperatura gigante (42sp), condición con emoji, min/max del día,
 * sensación térmica y píldora con las 3 métricas críticas (humedad, lluvia %, viento).
 */
@Composable
private fun WearHeroDashboardCard(weather: WeatherData) {
    val emoji = remember(weather.weatherCode, weather.isDay) {
        getWearWeatherEmoji(weather.weatherCode, weather.isDay)
    }
    val todayForecast = remember(weather.dailyForecasts) {
        weather.dailyForecasts.firstOrNull()
    }

    Card(
        onClick = { /* Glance tap */ },
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFF2C2C3A), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = Color(0xFF15151D),
            endBackgroundColor = Color(0xFF15151D)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            // Emoji + Condición
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.title2
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = weather.conditionTitle,
                    style = MaterialTheme.typography.caption1.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFFFCC80),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Temperatura Principal Gigante (Glanceable instantáneo)
            Text(
                text = "${weather.temperature.toInt()}°",
                style = MaterialTheme.typography.display1.copy(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                ),
                color = Color.White
            )

            // Rango Min / Max del día si existe + Sensación
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (todayForecast != null) {
                    Text(
                        text = "⬇️${todayForecast.tempMin.toInt()}° ⬆️${todayForecast.tempMax.toInt()}°",
                        style = MaterialTheme.typography.caption3.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFB0BEC5)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.caption3,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = "Sensación ${weather.feelsLike.toInt()}°C",
                    style = MaterialTheme.typography.caption3,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Micro-stats row (Glanceable rápido: Humedad, Lluvia, Viento)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF222230))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💧 ${weather.humidity}%",
                    style = MaterialTheme.typography.caption3.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xFF81D4FA)
                )
                Text(
                    text = "🌧️ ${weather.precipitationProbability}%",
                    style = MaterialTheme.typography.caption3.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xFF80CBC4)
                )
                Text(
                    text = "💨 ${weather.windSpeed.toInt()}k",
                    style = MaterialTheme.typography.caption3.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xFFFFE082)
                )
            }
        }
    }
}

/**
 * Tarjeta de WSI (Weather Shittiness Index) en el reloj.
 * Muestra el puntaje 0-100, categoría en color, medidor de 5 segmentos
 * y la advertencia tica de un solo vistazo.
 */
@Composable
private fun WearWsiCard(result: WeatherShittinessResult) {
    val badgeColor = remember(result.category) {
        getWsiColor(result.category)
    }
    val activeSegments = remember(result.score) {
        getWsiSeveritySegments(result.score)
    }

    Card(
        onClick = { /* Glance tap */ },
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = Color(0xFF131317),
            endBackgroundColor = Color(0xFF131317)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge de Puntaje WSI
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "💩 WSI:",
                    style = MaterialTheme.typography.caption3.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${result.score}/100",
                    style = MaterialTheme.typography.caption2.copy(fontWeight = FontWeight.ExtraBold),
                    color = badgeColor
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Medidor visual de 5 segmentos
            Row(
                modifier = Modifier.padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    val isFilled = i <= activeSegments
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isFilled) badgeColor else Color(0xFF2C2C36))
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${result.emoji} ${result.title}",
                style = MaterialTheme.typography.caption1.copy(fontWeight = FontWeight.Bold),
                color = badgeColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = result.description,
                style = MaterialTheme.typography.caption3,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Tarjeta de Barómetro y Predicción Local de Lluvia para Wear OS.
 * Mide la presión atmosférica con el sensor de hardware (Sensor.TYPE_PRESSURE)
 * del Galaxy Watch / reloj inteligente, calcula la tendencia barométrica (ΔP en hPa)
 * y emite alertas de lluvia locales con vibración háptica.
 */
@Composable
private fun WearBarometerCard(
    viewModel: WeatherViewModel,
    isFamilyFriendly: Boolean
) {
    val context = LocalContext.current
    val prediction by viewModel.barometerPredictionFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.measureBarometerPressure()
    }

    val trendColor = remember(prediction.trend) {
        when (prediction.trend) {
            BarometerTrendType.STORM_INCOMING -> Color(0xFFFF3D00)
            BarometerTrendType.RAIN_LIKELY -> Color(0xFFFFD600)
            BarometerTrendType.STEADY -> Color(0xFF00E676)
            BarometerTrendType.IMPROVING -> Color(0xFF00B0FF)
            BarometerTrendType.ALTITUDE_CHANGE -> Color(0xFF29B6F6)
            BarometerTrendType.CALIBRATING -> Color(0xFFFFB74D)
        }
    }

    val trendEmoji = remember(prediction.trend) {
        when (prediction.trend) {
            BarometerTrendType.STORM_INCOMING -> "⚡"
            BarometerTrendType.RAIN_LIKELY -> "🌧️"
            BarometerTrendType.STEADY -> "🌤️"
            BarometerTrendType.IMPROVING -> "☀️"
            BarometerTrendType.ALTITUDE_CHANGE -> "🧗"
            BarometerTrendType.CALIBRATING -> "🧭"
        }
    }

    val wearNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.simulateBarometerRainAlert(context)
            Toast.makeText(context, "🔔 Alerta enviada al reloj", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "⚠️ Se requiere permiso para alertas", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        onClick = { viewModel.measureBarometerPressure() },
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, trendColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = Color(0xFF141923),
            endBackgroundColor = Color(0xFF141923)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🧭 BARÓMETRO DEL RELOJ",
                    style = MaterialTheme.typography.caption3.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFF80D8FF)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Presión Actual
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = String.format(java.util.Locale.US, "%.1f", prediction.currentPressureHpa),
                    style = MaterialTheme.typography.title3.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "hPa",
                    style = MaterialTheme.typography.caption3.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Badge de Tendencia
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(trendColor.copy(alpha = 0.2f))
                    .border(0.5.dp, trendColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$trendEmoji ${prediction.title}",
                    style = MaterialTheme.typography.caption3.copy(fontWeight = FontWeight.Bold),
                    color = trendColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Descripción y predicción
            Text(
                text = prediction.description,
                style = MaterialTheme.typography.caption3.copy(fontSize = 9.sp),
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Chip indicador del filtro de altitud (8m ≈ 1 hPa)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (prediction.isAltitudeSpikeDetected || prediction.trend == BarometerTrendType.ALTITUDE_CHANGE)
                            Color(0xFF0277BD).copy(alpha = 0.35f)
                        else
                            Color(0xFF1E2838).copy(alpha = 0.6f)
                    )
                    .border(
                        0.5.dp,
                        if (prediction.isAltitudeSpikeDetected || prediction.trend == BarometerTrendType.ALTITUDE_CHANGE)
                            Color(0xFF29B6F6).copy(alpha = 0.8f)
                        else
                            Color(0xFF37474F).copy(alpha = 0.5f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (prediction.isAltitudeSpikeDetected || prediction.trend == BarometerTrendType.ALTITUDE_CHANGE)
                        "🛡️ Filtro: subida ~+${String.format(java.util.Locale.US, "%.0f", kotlin.math.abs(prediction.estimatedAltitudeDeltaMeters))}m detectada"
                    else
                        "🛡️ Filtro 8m ≈ 1 hPa activo (anti-gradas)",
                    style = MaterialTheme.typography.caption3.copy(fontSize = 7.5.sp, fontWeight = FontWeight.SemiBold),
                    color = if (prediction.isAltitudeSpikeDetected || prediction.trend == BarometerTrendType.ALTITUDE_CHANGE)
                        Color(0xFF80D8FF)
                    else
                        Color(0xFFB0BEC5),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Botón interactivo para probar alerta local en el reloj
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2838))
                    .border(0.5.dp, Color(0xFF3B5275), RoundedCornerShape(12.dp))
                    .clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificationHelper.hasNotificationPermission(context)) {
                            wearNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.simulateBarometerRainAlert(context)
                            Toast.makeText(context, "🔔 Alerta enviada al reloj", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "🔔 Probar Alerta en Reloj",
                    style = MaterialTheme.typography.caption3.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF80D8FF)
                )
            }
        }
    }
}

/**
 * "La Píldora del Mae" inspirada en los Tiles de Wear OS.
 * Muestra el comentario sarcástico con un botón interactivo para tirar los dados.
 */
@Composable
private fun WearPildoraDelMaeCard(
    comment: String,
    onRollNewComment: () -> Unit
) {
    Card(
        onClick = onRollNewComment,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(Color(0xFFFF9800), Color(0xFFFF5722))),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = Color(0xFF1C1521),
            endBackgroundColor = Color(0xFF1C1521)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "💊 LA PÍLDORA DEL MAE",
                    style = MaterialTheme.typography.caption3.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFFFFB74D)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "\"$comment\"",
                style = MaterialTheme.typography.caption2.copy(
                    fontWeight = FontWeight.Medium,
                    lineHeight = 15.sp
                ),
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Botón interactivo tipo tile pill para pedir otra opinión
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF382742))
                    .clickable { onRollNewComment() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "🎲 El mae opina",
                    style = MaterialTheme.typography.caption3.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFFFCC80)
                )
            }
        }
    }
}

/**
 * Carrusel horizontal que muestra las siguientes 8 horas de pronóstico.
 */
@Composable
private fun WearHourlyCarousel(items: List<HourlyForecastItem>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(items) { item ->
            WearHourlyItem(item = item)
        }
    }
}

@Composable
private fun WearHourlyItem(item: HourlyForecastItem) {
    val emoji = remember(item.weatherCode, item.isDay) {
        getWearWeatherEmoji(item.weatherCode, item.isDay)
    }

    Box(
        modifier = Modifier
            .width(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E1E26))
            .border(0.5.dp, Color(0xFF333342), RoundedCornerShape(14.dp))
            .padding(vertical = 6.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.timeDisplay,
                style = MaterialTheme.typography.caption3.copy(fontSize = 9.sp),
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = emoji,
                style = MaterialTheme.typography.caption1.copy(fontSize = 15.sp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${item.temperature.toInt()}°",
                style = MaterialTheme.typography.caption2.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            if (item.precipitationProbability > 0) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "${item.precipitationProbability}%",
                    style = MaterialTheme.typography.caption3.copy(fontSize = 8.sp, fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF4FC3F7)
                )
            }
        }
    }
}

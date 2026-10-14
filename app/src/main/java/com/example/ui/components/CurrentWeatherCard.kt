package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.WeatherData

@Composable
fun CurrentWeatherCard(
    weather: WeatherData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Main temperature and condition block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "${weather.temperature.toInt()}",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 68.sp,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "°C",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFFD54F)
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Feels-like badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = Color(0xFFFFCC80),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stringResource(R.string.weather_feels_like)} ${weather.feelsLike.toInt()}°C",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // Weather Condition Icon & Tico title
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    WeatherConditionIcon(
                        weatherCode = weather.weatherCode,
                        isDay = weather.isDay,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = weather.conditionTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 2
                    )
                    Text(
                        text = weather.conditionDescription,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6-metric grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    title = stringResource(R.string.weather_humidity),
                    value = "${weather.humidity}%",
                    subtitle = if (weather.humidity >= 85) "¡Sopa!" else "Normal",
                    icon = Icons.Default.WaterDrop,
                    iconTint = Color(0xFF4FC3F7),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = stringResource(R.string.weather_rain_chance),
                    value = "${weather.precipitationProbability}%",
                    subtitle = if (weather.precipitationProbability >= 60) "Lleve paraguas" else "Baja prob.",
                    icon = Icons.Default.Umbrella,
                    iconTint = Color(0xFF81D4FA),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = stringResource(R.string.weather_uv),
                    value = String.format(java.util.Locale.US, "%.1f", weather.uvIndex),
                    subtitle = if (weather.uvIndex >= 8) "¡Extremo!" else if (weather.uvIndex >= 5) "Alto" else "Moderado",
                    icon = Icons.Default.WbSunny,
                    iconTint = Color(0xFFFFB300),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    title = stringResource(R.string.weather_wind),
                    value = "${weather.windSpeed.toInt()} km/h",
                    subtitle = "Dirección",
                    icon = Icons.Default.Air,
                    iconTint = Color(0xFFA7FFEB),
                    extraIcon = {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .size(14.dp)
                                .rotate(weather.windDirection.toFloat())
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = stringResource(R.string.weather_sunrise),
                    value = weather.sunrise,
                    subtitle = "Madrugada",
                    icon = Icons.Default.WbTwilight,
                    iconTint = Color(0xFFFFD54F),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = stringResource(R.string.weather_sunset),
                    value = weather.sunset,
                    subtitle = "Tarde",
                    icon = Icons.Default.WbTwilight,
                    iconTint = Color(0xFFFF8A65),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    extraIcon: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                if (extraIcon != null) {
                    extraIcon()
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFFFFD54F).copy(alpha = 0.85f),
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object WeatherIconMapper {

    fun getIcon(weatherCode: Int, isDay: Boolean): ImageVector {
        return when (weatherCode) {
            0 -> if (isDay) Icons.Default.WbSunny else Icons.Default.Bedtime
            1, 2 -> if (isDay) Icons.Default.WbCloudy else Icons.Default.Bedtime
            3 -> Icons.Default.Cloud
            45, 48 -> Icons.Default.Air
            51, 53, 55, 56, 57 -> Icons.Default.WaterDrop
            61, 63, 65, 66, 67 -> Icons.Default.Grain
            71, 73, 75, 77, 85, 86 -> Icons.Default.Grain
            80, 81, 82 -> Icons.Default.Grain
            95, 96, 99 -> Icons.Default.Thunderstorm
            else -> if (isDay) Icons.Default.WbSunny else Icons.Default.Bedtime
        }
    }

    fun getIconTint(weatherCode: Int, isDay: Boolean): Color {
        return when (weatherCode) {
            0 -> if (isDay) Color(0xFFFFB300) else Color(0xFFE0E0E0)
            1, 2 -> if (isDay) Color(0xFFFFD54F) else Color(0xFFCFD8DC)
            3 -> Color(0xFFB0BEC5)
            45, 48 -> Color(0xFF90A4AE)
            51, 53, 55 -> Color(0xFF4FC3F7)
            61, 63, 65 -> Color(0xFF29B6F6)
            80, 81, 82 -> Color(0xFF0288D1)
            95, 96, 99 -> Color(0xFFFFCA28)
            else -> if (isDay) Color(0xFFFFB300) else Color(0xFFE0E0E0)
        }
    }
}

@Composable
fun WeatherConditionIcon(
    weatherCode: Int,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    contentDescription: String = "Ícono del clima"
) {
    val icon = WeatherIconMapper.getIcon(weatherCode, isDay)
    val tint = WeatherIconMapper.getIconTint(weatherCode, isDay)
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

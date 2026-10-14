package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DailyForecastItem

@Composable
fun DailyForecastCard(
    dailyList: List<DailyForecastItem>,
    modifier: Modifier = Modifier
) {
    val weekMin = dailyList.minOfOrNull { it.tempMin } ?: 0.0
    val weekMax = dailyList.maxOfOrNull { it.tempMax } ?: 40.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_forecast_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.daily_forecast_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.testTag("daily_forecast_list")
            ) {
                dailyList.forEachIndexed { index, item ->
                    DailyItemRow(
                        item = item,
                        weekMin = weekMin,
                        weekMax = weekMax
                    )
                    if (index < dailyList.lastIndex) {
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.08f),
                            thickness = 0.8.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyItemRow(
    item: DailyForecastItem,
    weekMin: Double,
    weekMax: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Day name
        Text(
            text = item.dateDisplay,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 14.sp
            ),
            modifier = Modifier.width(64.dp)
        )

        // Weather icon and rain prob
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(70.dp)
        ) {
            WeatherConditionIcon(
                weatherCode = item.weatherCode,
                isDay = true,
                modifier = Modifier.size(24.dp)
            )
            if (item.precipitationProbability >= 20) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF4FC3F7),
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = "${item.precipitationProbability}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF4FC3F7),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Min temp
        Text(
            text = "${item.tempMin.toInt()}°",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.65f),
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            ),
            modifier = Modifier.width(32.dp)
        )

        // Dynamic Temperature Bar Indicator relative to week's range
        val range = (weekMax - weekMin).coerceAtLeast(1.0)
        val startRatio = ((item.tempMin - weekMin) / range).toFloat().coerceIn(0f, 0.85f)
        val endRatio = ((item.tempMax - weekMin) / range).toFloat().coerceIn(0.15f, 1f)
        val widthRatio = (endRatio - startRatio).coerceAtLeast(0.12f)

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.12f))
        ) {
            val startOffset = maxWidth * startRatio
            val barWidth = (maxWidth * widthRatio).coerceAtMost(maxWidth - startOffset)

            Box(
                modifier = Modifier
                    .padding(start = startOffset)
                    .width(barWidth)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF4FC3F7), Color(0xFFFFB300), Color(0xFFFF7043))
                        )
                    )
            )
        }

        // Max temp
        Text(
            text = "${item.tempMax.toInt()}°",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            ),
            modifier = Modifier.width(32.dp)
        )
    }
}

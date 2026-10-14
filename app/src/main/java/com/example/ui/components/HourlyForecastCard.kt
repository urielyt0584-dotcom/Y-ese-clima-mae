package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.HourlyForecastItem

@Composable
fun HourlyForecastCard(
    hourlyList: List<HourlyForecastItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hourly_forecast_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.hourly_forecast_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("hourly_forecast_row")
            ) {
                items(
                    items = hourlyList,
                    key = { item -> "${item.timeDisplay}_${item.temperature}" }
                ) { item ->
                    HourlyItemView(item)
                }
            }
        }
    }
}

@Composable
private fun HourlyItemView(item: HourlyForecastItem) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.width(72.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp)
        ) {
            Text(
                text = item.timeDisplay,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            WeatherConditionIcon(
                weatherCode = item.weatherCode,
                isDay = item.isDay,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${item.temperature.toInt()}°",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (item.precipitationProbability > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

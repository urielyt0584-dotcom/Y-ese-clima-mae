package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyForecastItem
import com.example.data.model.WeatherCategory
import com.example.data.model.WeatherData

@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey
    val locationKey: String,
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val feelsLike: Double,
    val weatherCode: Int,
    val conditionTitle: String,
    val conditionDescription: String,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: Double,
    val uvIndex: Double,
    val precipitationProbability: Int,
    val isDay: Boolean,
    val sunrise: String,
    val sunset: String,
    val categoryName: String,
    val sarcasticComment: String,
    val hourlyJson: String,
    val dailyJson: String,
    val isLatest: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)

fun CachedWeatherEntity.toDomain(
    hourlyList: List<HourlyForecastItem>,
    dailyList: List<DailyForecastItem>
): WeatherData {
    val cat = try {
        WeatherCategory.valueOf(categoryName)
    } catch (e: Exception) {
        WeatherCategory.CLEAR_SKIES
    }

    return WeatherData(
        cityName = cityName,
        country = country,
        latitude = latitude,
        longitude = longitude,
        temperature = temperature,
        feelsLike = feelsLike,
        weatherCode = weatherCode,
        conditionTitle = conditionTitle,
        conditionDescription = conditionDescription,
        humidity = humidity,
        windSpeed = windSpeed,
        windDirection = windDirection,
        uvIndex = uvIndex,
        precipitationProbability = precipitationProbability,
        isDay = isDay,
        sunrise = sunrise,
        sunset = sunset,
        hourlyForecasts = hourlyList,
        dailyForecasts = dailyList,
        sarcasticComment = sarcasticComment,
        category = cat,
        isFromCache = true,
        cachedTimestamp = cachedAt
    )
}

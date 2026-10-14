package com.example.data.model

import androidx.compose.runtime.Stable
import androidx.compose.runtime.Immutable

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class WeatherCategory {
    EXTREME_HEAT,
    COLD,
    RAIN,
    HEAVY_RAIN,
    THUNDERSTORM,
    CLOUDS,
    CLEAR_SKIES,
    HIGH_HUMIDITY,
    STRONG_WIND,
    HIGH_UV,
    PERFECT,
    TROPICAL,
    NIGHT
}

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null,
    val current: CurrentWeatherDto? = null,
    val hourly: HourlyWeatherDto? = null,
    val daily: DailyWeatherDto? = null
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @field:Json(name = "temperature_2m") val temperature2m: Double,
    @field:Json(name = "relative_humidity_2m") val relativeHumidity2m: Int,
    @field:Json(name = "apparent_temperature") val apparentTemperature: Double,
    @field:Json(name = "is_day") val isDay: Int,
    val precipitation: Double? = 0.0,
    @field:Json(name = "weather_code") val weatherCode: Int,
    @field:Json(name = "wind_speed_10m") val windSpeed10m: Double,
    @field:Json(name = "wind_direction_10m") val windDirection10m: Double? = 0.0,
    @field:Json(name = "uv_index") val uvIndex: Double? = 0.0
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherDto(
    val time: List<String> = emptyList(),
    @field:Json(name = "temperature_2m") val temperature2m: List<Double> = emptyList(),
    @field:Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
    @field:Json(name = "precipitation_probability") val precipitationProbability: List<Int>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
    val time: List<String> = emptyList(),
    @field:Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
    @field:Json(name = "temperature_2m_max") val temperature2mMax: List<Double> = emptyList(),
    @field:Json(name = "temperature_2m_min") val temperature2mMin: List<Double> = emptyList(),
    val sunrise: List<String>? = emptyList(),
    val sunset: List<String>? = emptyList(),
    @field:Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = emptyList(),
    @field:Json(name = "uv_index_max") val uvIndexMax: List<Double>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val results: List<GeocodingResultDto>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResultDto(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null
)

@Stable
data class WeatherData(
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
    val hourlyForecasts: List<HourlyForecastItem>,
    val dailyForecasts: List<DailyForecastItem>,
    val sarcasticComment: String,
    val category: WeatherCategory,
    val isFromCache: Boolean = false,
    val cachedTimestamp: Long = 0L
)

@JsonClass(generateAdapter = true)
data class HourlyForecastItem(
    val timeDisplay: String,
    val temperature: Double,
    val weatherCode: Int,
    val precipitationProbability: Int,
    val isDay: Boolean
)

@JsonClass(generateAdapter = true)
data class DailyForecastItem(
    val dateDisplay: String,
    val tempMax: Double,
    val tempMin: Double,
    val weatherCode: Int,
    val precipitationProbability: Int,
    val conditionName: String
)

@Immutable
@JsonClass(generateAdapter = true)
data class CityLocation(
    val name: String,
    val subtitle: String,
    val latitude: Double,
    val longitude: Double,
    val country: String = "Costa Rica"
)

@Immutable
@com.google.firebase.firestore.IgnoreExtraProperties
data class SeasonalInsult(
    val id: String = "",
    val text: String = "",
    val season: String = "general",
    val category: String = "ALL",
    val isFamilyFriendly: Boolean = true,
    val isActive: Boolean = true,
    val priority: Int = 1
)

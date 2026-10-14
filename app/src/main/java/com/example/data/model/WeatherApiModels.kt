package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherApiResponse(
    val current: WaCurrent,
    val forecast: WaForecast
)

@JsonClass(generateAdapter = true)
data class WaCurrent(
    val temp_c: Double,
    val is_day: Int,
    val condition: WaCondition,
    val wind_kph: Double,
    val humidity: Int,
    val feelslike_c: Double,
    val uv: Double,
    val precip_mm: Double
)

@JsonClass(generateAdapter = true)
data class WaCondition(
    val code: Int
)

@JsonClass(generateAdapter = true)
data class WaForecast(
    val forecastday: List<WaForecastDay>
)

@JsonClass(generateAdapter = true)
data class WaForecastDay(
    val date: String,
    val day: WaDay,
    val hour: List<WaHour>
)

@JsonClass(generateAdapter = true)
data class WaDay(
    val maxtemp_c: Double,
    val mintemp_c: Double,
    val daily_chance_of_rain: Int,
    val condition: WaCondition,
    val uv: Double
)

@JsonClass(generateAdapter = true)
data class WaHour(
    val time: String,
    val temp_c: Double,
    val is_day: Int,
    val condition: WaCondition,
    val chance_of_rain: Int
)

package com.example.util

import com.example.data.model.WeatherData
import com.example.domain.WeatherCommentEngine

fun WeatherData.withFamilyFriendlyMode(isFamilyFriendlyMode: Boolean, checkCount: Int = 1): WeatherData {
    val newComment = WeatherCommentEngine.generateComment(
        isFamilyFriendlyMode = isFamilyFriendlyMode,
        temperature = this.temperature,
        feelsLike = this.feelsLike,
        humidity = this.humidity,
        precipitationProbability = this.precipitationProbability,
        windSpeed = this.windSpeed,
        uvIndex = this.uvIndex,
        weatherCode = this.weatherCode,
        isDay = this.isDay,
        checkCount = checkCount,
        country = this.country,
        latitude = this.latitude,
        longitude = this.longitude,
        locationName = this.cityName
    )
    val newTitle = WeatherCommentEngine.getTicoConditionTitle(this.weatherCode, this.isDay, isFamilyFriendlyMode)
    return this.copy(sarcasticComment = newComment, conditionTitle = newTitle)
}

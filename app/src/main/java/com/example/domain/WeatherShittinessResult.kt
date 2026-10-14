package com.example.domain

/**
 * Breakdown of individual weather factors contributing to the final Weather Shittiness Index.
 * The individual component values add up to the final score after applying the scoring/capping logic.
 */
data class WeatherShittinessBreakdown(
    val feelsLikeImpact: Int,
    val humidityImpact: Int,
    val rainImpact: Int,
    val windImpact: Int,
    val uvImpact: Int,
    val conditionImpact: Int
) {
    val total: Int get() = feelsLikeImpact + humidityImpact + rainImpact + windImpact + uvImpact + conditionImpact
}

/**
 * Result representation of the "How Shitty Is The Weather?" calculation.
 */
data class WeatherShittinessResult(
    val score: Int,
    val category: WeatherShittinessCategory,
    val emoji: String,
    val title: String,
    val description: String,
    val breakdown: WeatherShittinessBreakdown
)

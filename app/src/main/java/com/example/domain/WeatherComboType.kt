package com.example.domain

import com.example.data.model.WeatherData

enum class WeatherComboType(
    val title: String,
    val badgeLabel: String,
    val emoji: String
) {
    TRIFECTA(
        title = "La Trifecta del Despiche",
        badgeLabel = "Trifecta: Lluvia + Viento + Frío",
        emoji = "⛈️🥶💨"
    ),
    THUNDER_WIND(
        title = "Apocalipsis Eléctrico",
        badgeLabel = "Combo: Tormenta + Ventolero",
        emoji = "⚡🌪️"
    ),
    RAIN_WIND(
        title = "Aguacero con Ventolero",
        badgeLabel = "Combo: Lluvia + Viento fuerte",
        emoji = "🌧️💨"
    ),
    HEAT_HUMIDITY(
        title = "Bochorno del Infierno",
        badgeLabel = "Combo: Calor + Humedad asfixiante",
        emoji = "🔥♨️"
    ),
    COLD_RAIN(
        title = "Temporal Helado",
        badgeLabel = "Combo: Frío + Lluvia constante",
        emoji = "🌧️❄️"
    ),
    COLD_WIND(
        title = "Chiflón Polar",
        badgeLabel = "Combo: Frío + Viento helado",
        emoji = "🥶💨"
    ),
    HEAT_UV(
        title = "Sartén Cósmica",
        badgeLabel = "Combo: Calor + UV extremo",
        emoji = "☀️☢️"
    ),
    RAIN_AND_SUN(
        title = "Se Está Casando la Venada",
        badgeLabel = "Combo: Lluvia con Solazo",
        emoji = "🌦️🦌"
    ),
    HEAT_WIND(
        title = "Horno con Abanico",
        badgeLabel = "Combo: Calor + Viento caliente",
        emoji = "🔥💨"
    ),
    NIGHT_STORM(
        title = "Diluvio Nocturno",
        badgeLabel = "Combo: Noche + Rayería/Baldazo",
        emoji = "🌙⚡"
    ),
    NIGHT_COLD(
        title = "Noche Polar de Tigre",
        badgeLabel = "Combo: Noche + Frío helado",
        emoji = "🌙🥶"
    ),
    HUMIDITY_FOG(
        title = "Sopa de Niebla Espesa",
        badgeLabel = "Combo: Humedad + Neblina",
        emoji = "🌫️💧"
    )
}

fun detectWeatherCombo(
    temperature: Double,
    feelsLike: Double,
    humidity: Int,
    precipitationProbability: Int,
    windSpeed: Double,
    uvIndex: Double,
    weatherCode: Int,
    isDay: Boolean
): WeatherComboType? {
    val isRainCode = weatherCode in listOf(51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82)
    val isHeavyRainCode = weatherCode in listOf(65, 67, 82)
    val isThunderCode = weatherCode in 95..99
    val hasRain = isRainCode || isHeavyRainCode || isThunderCode || precipitationProbability >= 50
    val hasHeavyRain = isHeavyRainCode || isThunderCode || precipitationProbability >= 70

    val isCold = temperature <= 19.5 || feelsLike <= 19.0
    val isVeryCold = temperature <= 17.5 || feelsLike <= 17.0
    val isHot = temperature >= 28.5 || feelsLike >= 31.0
    val isHighWind = windSpeed >= 24.0
    val isHighHumidity = humidity >= 75
    val isExtremeUv = uvIndex >= 7.5

    // 1. Check TRIFECTA first (Heavy rain + Strong wind + Cold)
    if (hasRain && isHighWind && isCold) {
        return WeatherComboType.TRIFECTA
    }

    // 2. Thunderstorm + Strong wind
    if (isThunderCode && isHighWind) {
        return WeatherComboType.THUNDER_WIND
    }

    // 3. Rain + Sun ("Se está casando la venada")
    if (hasRain && isDay && (uvIndex >= 3.5 || weatherCode in listOf(0, 1, 2) || (feelsLike >= 25.0 && precipitationProbability in 35..85))) {
        return WeatherComboType.RAIN_AND_SUN
    }

    // 4. Rain + Strong wind (la sombrilla se dobla al revés)
    if (hasRain && isHighWind) {
        return WeatherComboType.RAIN_WIND
    }

    // 5. Cold + Rain (temporal helado)
    if (isCold && hasRain) {
        return WeatherComboType.COLD_RAIN
    }

    // 6. Cold + Strong wind (chiflón arranca-jupas)
    if (isCold && isHighWind) {
        return WeatherComboType.COLD_WIND
    }

    // 7. Heat + Extreme UV (sartén cósmica)
    if (isHot && isExtremeUv && isDay) {
        return WeatherComboType.HEAT_UV
    }

    // 8. Heat + High Humidity (bochorno / vaporón)
    if (isHot && isHighHumidity) {
        return WeatherComboType.HEAT_HUMIDITY
    }

    // 9. Heat + Strong wind (horno con ventilador)
    if (isHot && isHighWind) {
        return WeatherComboType.HEAT_WIND
    }

    // 10. Night + Storm/Heavy rain
    if (!isDay && (hasHeavyRain || isThunderCode)) {
        return WeatherComboType.NIGHT_STORM
    }

    // 11. Night + Extreme Cold
    if (!isDay && isVeryCold) {
        return WeatherComboType.NIGHT_COLD
    }

    // 12. Humidity + Fog
    if (humidity >= 85 && (weatherCode in listOf(45, 48) || (isRainCode && temperature <= 21.0))) {
        return WeatherComboType.HUMIDITY_FOG
    }

    return null
}

val WeatherData.activeCombo: WeatherComboType?
    get() = detectWeatherCombo(
        temperature = temperature,
        feelsLike = feelsLike,
        humidity = humidity,
        precipitationProbability = precipitationProbability,
        windSpeed = windSpeed,
        uvIndex = uvIndex,
        weatherCode = weatherCode,
        isDay = isDay
    )

package com.example.domain

import kotlin.random.Random

object ClothingRecommendationEngine {
    fun recommend(
        isFamilyFriendlyMode: Boolean = true,
        temperature: Double,
        feelsLike: Double,
        humidity: Int,
        precipitationProbability: Int,
        windSpeed: Double,
        uvIndex: Double,
        weatherCondition: String,
        isDay: Boolean,
        randomSeed: Long? = null
    ): ClothingRecommendation {
        val random = randomSeed?.let { Random(it) } ?: Random.Default
        val clothingItems = mutableListOf<ClothingItem>()
        var verdict = ""

        val isHot = feelsLike >= 28.0
        val isWarm = feelsLike in 22.0..27.9
        val isMild = feelsLike in 16.0..21.9
        val isCold = feelsLike < 16.0
        
        val isRaining = precipitationProbability > 60 || weatherCondition.contains("rain", ignoreCase = true) || weatherCondition.contains("lluv", ignoreCase = true)
        val isHeavyRain = precipitationProbability > 80 || weatherCondition.contains("thunder", ignoreCase = true) || weatherCondition.contains("tormenta", ignoreCase = true)
        val isHighHumidity = humidity > 85

        if (isHeavyRain) {
            clothingItems.add(ClothingItem("Jacket Impermeable", "🧥"))
            clothingItems.add(ClothingItem("Zapatos cerrados", "👞"))
            clothingItems.add(ClothingItem("Sombrilla", "☔"))
            verdict = if (isFamilyFriendlyMode) "Mejor quédese en la choza, está lloviendo muy fuerte, mae." else "Mae, ¿para qué putas quiere salir? Quédese en la choza, no sea necio."
        } else if (isRaining) {
            clothingItems.add(ClothingItem("Jacket Impermeable", "🧥"))
            clothingItems.add(ClothingItem("Zapatos cerrados", "👞"))
            if (isWindy(windSpeed)) {
                verdict = if (isFamilyFriendlyMode) "Lleve jacket, mae. La sombrilla no le va a aguantar este ventolero." else "Lleve jacket, la sombrilla se le va a hacer un colocho con este viento, mae."
            } else {
                clothingItems.add(ClothingItem("Sombrilla", "☔"))
                verdict = if (isFamilyFriendlyMode) "No olvide la sombrilla, mae, parece que va a llover." else "Mae, ande sombrilla o se va a ganar una buena mojada."
            }
        } else if (isCold) {
            clothingItems.add(ClothingItem("Abrigo pesado", "🧥"))
            clothingItems.add(ClothingItem("Pantalón largo", "👖"))
            clothingItems.add(ClothingItem("Medias gruesas", "🧦"))
            verdict = if (isFamilyFriendlyMode) "Hace bastante frío, mae. Abríguese bien, pura vida." else "Hace un frío de la puta madre, mae. Parecerá Cartago, póngase todo lo que encuentre."
        } else if (isHot) {
            clothingItems.add(ClothingItem("Ropa fresca", "👕"))
            clothingItems.add(ClothingItem("Shorts", "🩳"))
            if (isDay && uvIndex > 6.0) {
                clothingItems.add(ClothingItem("Bloqueador", "🧴"))
                clothingItems.add(ClothingItem("Gorra", "🧢"))
                verdict = if (isFamilyFriendlyMode) "Hace un calorón y el sol está fuerte. Protéjase con bloqueador, mae." else "Mae, póngase bloqueador si no quiere quedar como un camarón."
            } else {
                verdict = if (isFamilyFriendlyMode) "Clima muy tuanis y cálido. Ropa fresquita, mae." else "Ropa fresca mae, hace un calor que se las trae."
            }
        } else if (isWarm) {
            clothingItems.add(ClothingItem("Camisa cómoda", "👕"))
            clothingItems.add(ClothingItem("Pantalón o short", "👖"))
            if (isHighHumidity) {
                verdict = if (isFamilyFriendlyMode) "Hay mucho bochorno, use ropa ligera." else "El bochorno está rudo mae, ropa ligera porque va a sudar."
            } else {
                verdict = if (isFamilyFriendlyMode) "Clima súper tuanis, pura vida." else "Clima calidad, póngase lo que le ronque, mae."
            }
        } else if (isMild) {
            clothingItems.add(ClothingItem("Pantalón largo", "👖"))
            clothingItems.add(ClothingItem("Suéter ligero", "🧥"))
            verdict = if (isFamilyFriendlyMode) "Está medio frijolito, mae. Mejor lleve un suetico." else "Frijolito, mae. Un suetico apenas pa' no pasar ronchas."
        } else {
            clothingItems.add(ClothingItem("Ropa normal", "👕"))
            verdict = if (isFamilyFriendlyMode) "Buen clima, mae. Pura vida para salir." else "Pura vida mae, salga tranquilo."
        }

        val salir = if (isHeavyRain) {
            if (isFamilyFriendlyMode) "Mejor no salir" else "¡Ni a putas!"
        } else if (isRaining || isCold) {
            if (isFamilyFriendlyMode) "Diay sí, pero abríguese" else "Sí, pero apriete"
        } else {
            if (isFamilyFriendlyMode) "Dele, pura vida" else "Dele, sin miedo"
        }

        return ClothingRecommendation(
            clothingItems = clothingItems,
            uselessItem = null,
            verdict = verdict
        )
    }

    private fun isWindy(windSpeed: Double): Boolean {
        return windSpeed > 25.0
    }
}

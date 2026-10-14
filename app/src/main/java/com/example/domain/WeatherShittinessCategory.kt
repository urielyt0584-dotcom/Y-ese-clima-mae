package com.example.domain

/**
 * Categories for the Weather Shittiness Index (WSI), ranging from 0 to 100.
 * Thresholds, titles, emojis, and default commentary can be customized or configured.
 */
enum class WeatherShittinessCategory(
    val minScore: Int,
    val maxScore: Int,
    val emoji: String,
    val title: String,
    val defaultPhrase: String
) {
    PERFECT(
        minScore = 0,
        maxScore = 15,
        emoji = "🌤️",
        title = "Pura Vida",
        defaultPhrase = "Pura vida, mae."
    ),
    PRETTY_GOOD(
        minScore = 16,
        maxScore = 30,
        emoji = "😎",
        title = "Apenas Bueno",
        defaultPhrase = "No tengo nada que reclamar."
    ),
    MEH(
        minScore = 31,
        maxScore = 45,
        emoji = "🌥️",
        title = "Medio Medio",
        defaultPhrase = "Diay… podría estar peor."
    ),
    GETTING_BAD(
        minScore = 46,
        maxScore = 60,
        emoji = "😐",
        title = "Dando Pereza",
        defaultPhrase = "Mae, ya esto empieza a dar pereza."
    ),
    BAD(
        minScore = 61,
        maxScore = 75,
        emoji = "🌧️",
        title = "Clima Feo",
        defaultPhrase = "Mae, lleve sombrilla y no joda."
    ),
    VERY_BAD(
        minScore = 76,
        maxScore = 90,
        emoji = "💀",
        title = "Fatal",
        defaultPhrase = "Mae, ¿usted de verdad tiene que salir?"
    ),
    ABSOLUTELY_HORRIBLE(
        minScore = 91,
        maxScore = 100,
        emoji = "☠️",
        title = "Catastrófico",
        defaultPhrase = "NO SALGA, HIJUEPUTA."
    );

    companion object {
        /**
         * Resolves category from a given score (0..100).
         */
        fun fromScore(score: Int): WeatherShittinessCategory {
            val clamped = score.coerceIn(0, 100)
            return entries.firstOrNull { clamped in it.minScore..it.maxScore } ?: ABSOLUTELY_HORRIBLE
        }
    }
}

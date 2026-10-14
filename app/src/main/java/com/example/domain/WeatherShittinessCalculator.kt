package com.example.domain

import com.example.data.model.WeatherData
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object WeatherShittinessCalculator {

    private val categoryCommentaryFamilyFriendly: Map<WeatherShittinessCategory, List<String>> = mapOf(
        WeatherShittinessCategory.PERFECT to listOf(
            "El clima está apenas, mae.",
            "Un día súper tuanis para salir.",
            "Pura vida, condiciones ideales.",
            "Día espectacular para dar un paseo, mae."
        ),
        WeatherShittinessCategory.PRETTY_GOOD to listOf(
            "El clima está bastante bien, al suave.",
            "Nada de qué quejarse, mae.",
            "Un día muy tuanis.",
            "Buenas condiciones hoy, pura vida."
        ),
        WeatherShittinessCategory.MEH to listOf(
            "El clima está regular, mae.",
            "Ni fu ni fa, todo tranquilo.",
            "Diay... podría estar mejor, pero aguanta.",
            "Un día bastante normalito, pura vida."
        ),
        WeatherShittinessCategory.GETTING_BAD to listOf(
            "Se está poniendo feo el asunto.",
            "Mejor tener cuidado si sale, mae.",
            "El clima ya no está tan pura vida.",
            "Tiene pinta de que va a empeorar pronto."
        ),
        WeatherShittinessCategory.BAD to listOf(
            "Se vino la lluvia, mae.",
            "Mejor quedarse en la choza si es posible.",
            "El clima está bastante feíllo hoy.",
            "Definitivamente ande sombrilla si sale.",
            "Día gris y agüevado, pura vida."
        ),
        WeatherShittinessCategory.VERY_BAD to listOf(
            "Clima feo y pesado, mae.",
            "Si no tiene que salir, mejor espere un toque.",
            "Está muy feo para andar afuera."
        ),
        WeatherShittinessCategory.ABSOLUTELY_HORRIBLE to listOf(
            "Clima extremo, mae. Evite salir.",
            "Rayería y aguacero. Manténgase seguro.",
            "Definitivamente no asome la nariz."
        )
    )

    private val categoryCommentaryUnfiltered: Map<WeatherShittinessCategory, List<String>> = mapOf(
        WeatherShittinessCategory.PERFECT to listOf(
            "Pura vida, mae. Salga tranquilo.",
            "Mae, por una vez el clima se comportó.",
            "El sol no quema y no hay aguacero, aproveche carepicha.",
            "Día de gloria tica: ni un solo pero.",
            "Hasta dan ganas de salir a caminar, qué milagro.",
            "Milagro de San Pedro: clima de ensueño, aproveche antes de que se despiche.",
            "Ni una gota de lluvia ni calor aplastante. No lo cague quejándose, mae.",
            "Disfrute carebarro, que un día tan bueno como este no se repite en meses.",
            "Clima de reyes, mae. Vaya tómese una birra y deje de quejarse de la vida."
        ),
        WeatherShittinessCategory.PRETTY_GOOD to listOf(
            "No tengo ni picha que reclamar, mae.",
            "Bastante decente. Un día tuanis para hacer mandados.",
            "Clima amable, disfrútelo antes de que se arrepienta, carebarro.",
            "Apenas para un cafecito tranquilo sin andar mentando madres.",
            "Condiciones tuanis, mae. Rápido antes de que el cielo cambie de parecer.",
            "Diay, el clima está decente. Al menos no se está ahogando ni derritiendo.",
            "Bastante pasable, mae. Si se pone a llorar hoy es por puro gusto."
        ),
        WeatherShittinessCategory.MEH to listOf(
            "Diay… podría estar peor, pero qué agüevazón.",
            "Diay mae, está medio vara el ambiente.",
            "No está tan mal… pero tampoco se emocione, pedazo de animal.",
            "Ni frío ni calor extremo, pero algo trama este cielo carepicha.",
            "Típico clima indeciso: ni chicha ni limonada, pura mierda.",
            "Cielo con cara de pocos amigos. No me fío ni media verga de este día.",
            "Clima soso como sopa de asilo. Da una pereza que cala los huesos.",
            "Ni fu ni fa, mae. Puro día sin gracia para estar amargado."
        ),
        WeatherShittinessCategory.GETTING_BAD to listOf(
            "Mae, ya esto empieza a dar una soberana pereza.",
            "El cielo se está poniendo con cara de culo.",
            "Ya se siente esa pesadez que presagia aguacero concho.",
            "Mejor vaya buscando el paraguas, carebarro.",
            "Clima engañoso, no confíe en ese rayito de sol ni a putas.",
            "Se viene un pichazo de agua en cualquier momento, fije la fecha.",
            "Diay mae, el cielo se está emputando. Vaya apretando el paso.",
            "Se puso fea la cosa. Si tiene ropa tendida, corra como idiota."
        ),
        WeatherShittinessCategory.BAD to listOf(
            "Mae, qué asco de día más hijueputa.",
            "Si no tiene que salir, mejor ni asome la jupa, idiota.",
            "Clima nivel 'quédese en la choza y mande todo a la mierda'.",
            "Sopa de nubes y agua. Qué picha de clima más desgraciado.",
            "Mae, lleve sombrilla y no joda que esto se va a poner peor.",
            "Baldazo traicionero arruinando la tarde. Qué soberano despiche.",
            "Clima asqueroso, todo embarrialado y los zapatos empapados, qué delicia.",
            "Diay mae, ¿quién pidió este despiche de clima? Qué mierda salir así."
        ),
        WeatherShittinessCategory.VERY_BAD to listOf(
            "Un día perfecto para cancelar todos los planes y putear parejo.",
            "Si sale hoy es porque de verdad no valora su vida, pedazo de bestia.",
            "Clima fatal, las calles son un río de barro y presas de la gran puta.",
            "Mae, ¿usted de verdad tiene que salir o es que le gusta comer mierda?",
            "Temporal asqueroso. Ni con capa impermeable se salva de empaparse hasta las bolas.",
            "Todo despichado afuera. Las alcantarillas nadando y el tráfico colapsado.",
            "Nivel de clima: 'mande un audio diciendo que se le inundó la choza y duérmase'."
        ),
        WeatherShittinessCategory.ABSOLUTELY_HORRIBLE to listOf(
            "Mae, es el apocalipsis ahí afuera. NO SALGA, HIJUEPUTA.",
            "Ni los zopilotes salen con este clima tan malparido.",
            "Clima nivel 'Cancele todo, métase bajo la cobija y llore'.",
            "Esto ya no es Costa Rica, es un castigo divino. Despiche total.",
            "Me cago en todo, el cielo se está cayendo a pedazos.",
            "Rayería y diluvio satánico. Si asoma la jupa lo fulminan de una vez.",
            "El clima le está diciendo 'coma mierda' a nivel bíblico. No salga ni a la esquina.",
            "Despiche catastrófico. Quédese encerrado rezando para que no se le vuele el techo."
        )
    )

    fun calculate(
        weatherData: WeatherData,
        checkCount: Int = 1,
        isFamilyFriendlyMode: Boolean = true
    ): WeatherShittinessResult {
        val feelsLike = weatherData.feelsLike
        val humidity = weatherData.humidity
        val rainProb = weatherData.precipitationProbability
        val windSpeed = weatherData.windSpeed
        val uvIndex = weatherData.uvIndex
        val weatherCondition = weatherData.conditionTitle
        val randomSeed = weatherData.cityName.hashCode() + checkCount
        val isForeign = WeatherForeignInsults.isForeignLocation(weatherData.country, weatherData.latitude, weatherData.longitude)
        val specialLoc = SpecialLocationInsults.detectSpecialLocation(weatherData.cityName, weatherData.latitude, weatherData.longitude)

        return calculateInternal(
            feelsLike,
            humidity,
            rainProb,
            windSpeed,
            uvIndex,
            weatherCondition,
            randomSeed,
            isFamilyFriendlyMode,
            isForeign = isForeign,
            country = weatherData.country,
            specialLoc = specialLoc
        )
    }

    private fun calculateInternal(
        feelsLike: Double,
        humidity: Int,
        rainProb: Int,
        windSpeed: Double,
        uvIndex: Double,
        weatherCondition: String,
        randomSeed: Int,
        isFamilyFriendlyMode: Boolean,
        isForeign: Boolean = false,
        country: String = "Costa Rica",
        specialLoc: SpecialLocation? = null
    ): WeatherShittinessResult {
        // Safe casting bounds (prevent NaN / Infinity)
        val safeFeelsLike = if (feelsLike.isFinite()) feelsLike.coerceIn(-20.0, 50.0) else 22.0
        val safeHumidity = humidity.coerceIn(0, 100).toDouble()
        val safeRainProb = rainProb.coerceIn(0, 100)
        val safeWind = if (windSpeed.isFinite()) windSpeed.coerceIn(0.0, 200.0) else 10.0
        val safeUv = if (uvIndex.isFinite()) uvIndex.coerceIn(0.0, 20.0) else 5.0

        // 1. Feels Like Temperature Discomfort (0 to 22 pts)
        var rawFeelsLike = when {
            safeFeelsLike in 21.0..26.0 -> 0
            safeFeelsLike in 17.0..20.9 -> ((21.0 - safeFeelsLike) * 0.8).roundToInt().coerceIn(1, 4)
            safeFeelsLike < 17.0 -> (4 + (17.0 - safeFeelsLike) * 1.5).roundToInt().coerceIn(5, 18)
            safeFeelsLike in 26.1..29.0 -> ((safeFeelsLike - 26.0) * 1.2).roundToInt().coerceIn(1, 4)
            safeFeelsLike in 29.1..34.0 -> (4 + (safeFeelsLike - 29.0) * 2.0).roundToInt().coerceIn(5, 14)
            else -> (14 + (safeFeelsLike - 34.0) * 2.5).roundToInt().coerceIn(15, 22)
        }

        // 2. Humidity Discomfort (0 to 18 pts)
        var rawHumidity = when {
            safeHumidity in 40.0..60.0 -> 0
            safeHumidity < 40.0 -> ((40.0 - safeHumidity) * 0.15).roundToInt().coerceIn(1, 4)
            safeHumidity in 60.1..75.0 -> ((safeHumidity - 60.0) * 0.3).roundToInt().coerceIn(1, 5)
            safeHumidity in 75.1..85.0 -> (5 + (safeHumidity - 75.0) * 0.7).roundToInt().coerceIn(6, 12)
            else -> (12 + (safeHumidity - 85.0) * 0.4).roundToInt().coerceIn(13, 20)
        }

        // Compound heat + humidity penalty
        if (feelsLike >= 27.0 && safeHumidity >= 70.0) {
            val muggyExtra = if (safeHumidity >= 85.0) 6 else 4
            rawHumidity = min(22, rawHumidity + muggyExtra)
        }

        // 3. Rain Probability (0 to 30 pts)
        var rawRain = when {
            safeRainProb <= 15 -> 0
            safeRainProb <= 30 -> ((safeRainProb - 15) * 0.25).roundToInt().coerceIn(1, 4)
            safeRainProb <= 55 -> (4 + (safeRainProb - 30) * 0.35).roundToInt().coerceIn(5, 13)
            safeRainProb <= 80 -> (13 + (safeRainProb - 55) * 0.45).roundToInt().coerceIn(14, 24)
            else -> (24 + (safeRainProb - 80) * 0.3).roundToInt().coerceIn(25, 30)
        }

        // 4. Wind Discomfort (0 to 22 pts)
        var rawWind = when {
            safeWind <= 15.0 -> 0
            safeWind <= 28.0 -> ((safeWind - 15.0) * 0.38).roundToInt().coerceIn(1, 5)
            safeWind <= 45.0 -> (5 + (safeWind - 28.0) * 0.50).roundToInt().coerceIn(6, 13)
            safeWind <= 65.0 -> (13 + (safeWind - 45.0) * 0.35).roundToInt().coerceIn(14, 20)
            else -> min(22, (20 + (safeWind - 65.0) * 0.2).roundToInt())
        }

        // 5. UV Index Discomfort (0 to 18 pts)
        var rawUv = when {
            safeUv <= 2.0 -> 0
            safeUv <= 5.0 -> ((safeUv - 2.0) * 1.0).roundToInt().coerceIn(1, 3)
            safeUv <= 7.0 -> (3 + (safeUv - 5.0) * 2.0).roundToInt().coerceIn(4, 7)
            safeUv <= 10.0 -> (7 + (safeUv - 7.0) * 2.0).roundToInt().coerceIn(8, 13)
            else -> (13 + (safeUv - 10.0) * 1.5).roundToInt().coerceIn(14, 18)
        }

        // 6. Weather Condition Modifier (0 to 46 pts)
        val normalizedCondition = weatherCondition.lowercase()
        val isThunderstorm = normalizedCondition.contains("tormenta") ||
                normalizedCondition.contains("thunder") ||
                normalizedCondition.contains("eléctrica") ||
                normalizedCondition.contains("electrica")

        val isHeavyRain = !isThunderstorm && (
                normalizedCondition.contains("fuerte") ||
                normalizedCondition.contains("aguacero") ||
                normalizedCondition.contains("heavy") ||
                normalizedCondition.contains("chubasco fuerte") ||
                normalizedCondition.contains("baldazo")
        )

        val isRegularRain = !isThunderstorm && !isHeavyRain && (
                normalizedCondition.contains("lluvia") ||
                normalizedCondition.contains("rain") ||
                normalizedCondition.contains("chubasco") ||
                normalizedCondition.contains("precipitaci")
        )

        val isDrizzle = !isThunderstorm && !isHeavyRain && !isRegularRain && (
                normalizedCondition.contains("llovizna") ||
                normalizedCondition.contains("drizzle") ||
                normalizedCondition.contains("chipi") ||
                normalizedCondition.contains("garúa")
        )

        val isFog = normalizedCondition.contains("niebla") ||
                normalizedCondition.contains("neblina") ||
                normalizedCondition.contains("fog")

        val isSnow = normalizedCondition.contains("nieve") ||
                normalizedCondition.contains("granizo") ||
                normalizedCondition.contains("snow") ||
                normalizedCondition.contains("hail")

        val isCloudy = normalizedCondition.contains("nublado") ||
                normalizedCondition.contains("overcast") ||
                normalizedCondition.contains("cubierto") ||
                normalizedCondition.contains("cloud")

        val isHeatwave = (normalizedCondition.contains("calor") ||
                normalizedCondition.contains("caluroso") ||
                normalizedCondition.contains("heat") ||
                normalizedCondition.contains("bochorno") ||
                normalizedCondition.contains("solazo")) && feelsLike >= 28.0

        val isFreezing = (normalizedCondition.contains("frío") ||
                normalizedCondition.contains("frio") ||
                normalizedCondition.contains("congelante") ||
                normalizedCondition.contains("frost") ||
                normalizedCondition.contains("helada")) && feelsLike <= 14.0

        var rawCondition = when {
            isThunderstorm -> 44
            isSnow -> 28
            isHeavyRain -> 24
            isRegularRain -> 14
            isHeatwave -> 12
            isFreezing -> 12
            isDrizzle -> 7
            isFog -> 9
            isCloudy -> 4
            else -> 0
        }

        // Avoid double-counting between rain probability and rain condition
        if ((isHeavyRain || isRegularRain) && safeRainProb >= 70) {
            val totalRainAndCondition = rawRain + rawCondition
            val cap = if (isHeavyRain) 46 else 38
            if (totalRainAndCondition > cap) {
                val excess = totalRainAndCondition - cap
                rawCondition = max(6, rawCondition - excess)
            }
        }

        if (isThunderstorm) {
            if (rawRain < 18 && safeRainProb >= 40) {
                rawRain = max(rawRain, 18)
            }
        }

        val rawTotal = rawFeelsLike + rawHumidity + rawRain + rawWind + rawUv + rawCondition
        val clampedScore = rawTotal.coerceIn(0, 100)

        val finalBreakdown: WeatherShittinessBreakdown = if (rawTotal == 0) {
            WeatherShittinessBreakdown(0, 0, 0, 0, 0, 0)
        } else if (rawTotal <= 100) {
            WeatherShittinessBreakdown(
                feelsLikeImpact = rawFeelsLike,
                humidityImpact = rawHumidity,
                rainImpact = rawRain,                windImpact = rawWind,
                uvImpact = rawUv,
                conditionImpact = rawCondition
            )
        } else {
            val scale = 100.0 / rawTotal
            var sFeelsLike = (rawFeelsLike * scale).roundToInt()
            var sHumidity = (rawHumidity * scale).roundToInt()
            var sRain = (rawRain * scale).roundToInt()
            var sWind = (rawWind * scale).roundToInt()
            var sUv = (rawUv * scale).roundToInt()
            var sCondition = (rawCondition * scale).roundToInt()
            val scaledSum = sFeelsLike + sHumidity + sRain + sWind + sUv + sCondition
            val diff = 100 - scaledSum

            val components = mutableListOf(
                Pair("condition", sCondition),
                Pair("feelsLike", sFeelsLike),
                Pair("rain", sRain),
                Pair("humidity", sHumidity),
                Pair("wind", sWind),
                Pair("uv", sUv)
            ).sortedByDescending { it.second }

            if (diff != 0) {
                when (components.first().first) {
                    "condition" -> sCondition += diff
                    "feelsLike" -> sFeelsLike += diff
                    "rain" -> sRain += diff
                    "humidity" -> sHumidity += diff
                    "wind" -> sWind += diff
                    else -> sUv += diff
                }
            }
            WeatherShittinessBreakdown(
                feelsLikeImpact = max(0, sFeelsLike),
                humidityImpact = max(0, sHumidity),
                rainImpact = max(0, sRain),
                windImpact = max(0, sWind),
                uvImpact = max(0, sUv),
                conditionImpact = max(0, sCondition)
            )
        }

        val finalScore = finalBreakdown.total.coerceIn(0, 100)
        val category = WeatherShittinessCategory.fromScore(finalScore)

        val comboDescription: String? = when {
            finalBreakdown.rainImpact >= 10 && finalBreakdown.windImpact >= 8 && finalBreakdown.feelsLikeImpact >= 8 && feelsLike <= 19.5 -> {
                if (isFamilyFriendlyMode) {
                    "La trifecta del temporal: lluvia fuerte, viento y frío. El índice castiga este clima tan bravo."
                } else {
                    "La trifecta del despiche: baldazo con ventolero y frío polar. El índice de porquería se fue al carajo."
                }
            }
            finalBreakdown.rainImpact >= 12 && finalBreakdown.windImpact >= 10 -> {
                if (isFamilyFriendlyMode) {
                    "Combo de lluvia y viento: el agua cae de lado y las sombrillas sufren. El índice sube por las ráfagas."
                } else {
                    "Combo de pesadilla: lluvia con ventolero que despedaza sombrillas. El índice de porquería se disparó."
                }
            }
            finalBreakdown.feelsLikeImpact >= 12 && finalBreakdown.humidityImpact >= 10 -> {
                if (isFamilyFriendlyMode) {
                    "Combo de bochorno pesado: calor sofocante con humedad muy alta. El índice castiga este vaporón."
                } else {
                    "Despiche en combo: calor asfixiante con una humedad del demonio. Bochorno nivel olla de carne hirviendo."
                }
            }
            finalBreakdown.rainImpact >= 10 && finalBreakdown.feelsLikeImpact >= 10 && feelsLike <= 18.5 -> {
                if (isFamilyFriendlyMode) {
                    "Combo de temporal helado: agua y frío parejos. Puntuación alta por riesgo inminente de resfrío."
                } else {
                    "Combo helado: lluvia con frío que cala los huesos. Mojarse hoy es neumonía asegurada, carepicha."
                }
            }
            finalBreakdown.uvImpact >= 10 && finalBreakdown.feelsLikeImpact >= 10 -> {
                if (isFamilyFriendlyMode) {
                    "Combo de radiación extrema con calor: sol que quema duro. El índice advierte peligro de insolación."
                } else {
                    "Combo sartén cósmica: radiación extrema con calorón infernal. El índice advierte pellejo frito."
                }
            }
            else -> null
        }

        val categoryCommentary = if (isFamilyFriendlyMode) categoryCommentaryFamilyFriendly else categoryCommentaryUnfiltered
        val baseList = categoryCommentary[category] ?: listOf(category.defaultPhrase)
        val foreignDescription: String? = if (isForeign) {
            val cleanC = WeatherForeignInsults.cleanForeignCountryName(country)
            if (isFamilyFriendlyMode) {
                "El índice evalúa el clima en $cleanC: ¡a cuidarse y disfrutar el viaje!"
            } else {
                "Índice internacional: el clima en $cleanC no le tiene ni un gramo de piedad a los extranjeros."
            }
        } else null

        val specialLocDescription: String? = when (specialLoc) {
            SpecialLocation.CERRO_DE_LA_MUERTE -> if (isFamilyFriendlyMode) "Índice en el Cerro de la Muerte: frío bravo y neblina de altura, maneje despacio." else "Índice en el Cerro de la Muerte: frío polar y neblina que no deja ver ni picha."
            SpecialLocation.GOLFITO -> if (isFamilyFriendlyMode) "Índice en Golfito: vaporón del Golfo Dulce con riesgo de baldazo tropical." else "Índice en Golfito: bochorno del demonio, aquí se suda aceite de palma."
            SpecialLocation.PASO_CANOAS -> if (isFamilyFriendlyMode) "Índice en Paso Canoas: calor pesado en la pura frontera." else "Índice en Paso Canoas: horno fronterizo, se le derriten las tenis en el asfalto."
            SpecialLocation.CIUDAD_NEILY -> if (isFamilyFriendlyMode) "Índice en Ciudad Neily: ambiente tropical cálido y húmedo." else "Índice en Ciudad Neily: sancocho concho donde el aire no se mueve ni por caridad."
            SpecialLocation.RIO_CLARO -> if (isFamilyFriendlyMode) "Índice en Río Claro: parada calurosa en el cruce de la Zona Sur." else "Índice en Río Claro: calor rajado y ráfagas calientes de la Interamericana."
            null -> null
        }

        val commentaryList = when {
            comboDescription != null -> listOf(comboDescription) + baseList
            specialLocDescription != null -> listOf(specialLocDescription) + baseList
            foreignDescription != null -> listOf(foreignDescription) + baseList
            else -> baseList
        }
        val description = com.example.util.SmartInsultSelector.getUnique(commentaryList, randomSeed)

        return WeatherShittinessResult(
            score = finalScore,
            category = category,
            emoji = category.emoji,
            title = category.title,
            description = description,
            breakdown = finalBreakdown
        )
    }
}

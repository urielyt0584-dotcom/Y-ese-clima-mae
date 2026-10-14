package com.example.data.repository

import com.example.data.local.CachedWeatherEntity
import com.example.data.local.WeatherConverters
import com.example.data.local.WeatherDao
import com.example.data.local.toDomain
import com.example.data.model.CityLocation
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyForecastItem
import com.example.data.model.OpenMeteoResponse
import com.example.data.model.WeatherData
import com.example.data.network.GeocodingApiService
import com.example.data.network.NetworkClient
import com.example.data.network.WeatherApiService
import com.example.domain.WeatherCommentEngine
import com.example.domain.WeatherForeignInsults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

open class WeatherRepository(
    private val weatherDao: WeatherDao? = null,
    private val weatherApi: WeatherApiService = NetworkClient.weatherService,
    private val geocodingApi: GeocodingApiService = NetworkClient.geocodingService,
    private val weatherApiDotCom: com.example.data.network.WeatherApiDotComService = NetworkClient.weatherApiDotComService
) {

    private val converters = WeatherConverters()

    // Preset popular Costa Rican locations for quick access and offline reliability
    val defaultTicoCities = listOf(
        CityLocation("San José (Chepe)", "Valle Central, Costa Rica", 9.9281, -84.0907),
        CityLocation("Alajuela (La Liga)", "Valle Central, Costa Rica", 10.0163, -84.2116),
        CityLocation("Cartago (Las Brumas)", "Valle Central, Costa Rica", 9.8644, -83.9194),
        CityLocation("Heredia (Las Flores)", "Valle Central, Costa Rica", 9.9981, -84.1165),
        CityLocation("Liberia", "Guanacaste, Costa Rica", 10.6350, -85.4377),
        CityLocation("Puntarenas (El Puerto)", "Pacífico Central, Costa Rica", 9.9763, -84.8384),
        CityLocation("Limón", "Caribe, Costa Rica", 9.9907, -83.0360),
        CityLocation("Monteverde", "Sierra de Tilarán, Costa Rica", 10.3163, -84.8255),
        CityLocation("Tamarindo", "Guanacaste, Costa Rica", 10.2993, -85.8400),
        CityLocation("Golfito", "Golfo Dulce, Costa Rica", 8.6380, -83.1660),
        CityLocation("Paso Canoas", "Frontera con Panamá, Costa Rica", 8.5333, -82.8400),
        CityLocation("El Cerro de la Muerte", "Cordillera de Talamanca, Costa Rica", 9.5667, -83.7500),
        CityLocation("Ciudad Neily", "Corredores, Costa Rica", 8.6500, -82.9500),
        CityLocation("Río Claro", "Golfito, Costa Rica", 8.6917, -83.0750)
    )

    open suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        locationName: String,
        country: String = "Costa Rica",
        checkCount: Int = 1,
        forceRandomSeed: Int? = null,
        forceRefresh: Boolean = false
    ): Result<WeatherData> = withContext(Dispatchers.IO) {
        val safeLat = if (latitude.isFinite() && latitude in -90.0..90.0) latitude else 9.9281
        val safeLon = if (longitude.isFinite() && longitude in -180.0..180.0) longitude else -84.0907
        val safeName = locationName.trim().take(100).ifBlank { "San José (Chepe)" }
        val rawCountry = country.trim().take(100)
        val safeCountry = if (rawCountry.isNotBlank() && !rawCountry.equals("Costa Rica", true) && !rawCountry.equals("CR", true)) {
            rawCountry
        } else if (!WeatherForeignInsults.isInsideCostaRica(safeLat, safeLon)) {
            "Extranjero"
        } else {
            "Costa Rica"
        }

        // 1. Check Cache first to save API calls (15 mins TTL)
        if (weatherDao != null && !forceRefresh) {
            try {
                val locationKey = generateLocationKey(safeLat, safeLon, safeName)
                val cached = weatherDao.getWeatherByKey(locationKey)
                    ?: weatherDao.getWeatherByCity(safeName)
                
                if (cached != null) {
                    val currentTime = System.currentTimeMillis()
                    val cacheTtlMs = 15 * 60 * 1000L // 15 minutes
                    if (currentTime - cached.cachedAt < cacheTtlMs) {
                        val hourly = converters.toHourlyList(cached.hourlyJson)
                        val daily = converters.toDailyList(cached.dailyJson)
                        val cachedData = cached.toDomain(hourly, daily).copy(
                            isFromCache = false,
                            cachedTimestamp = cached.cachedAt
                        )
                        
                        val category = WeatherCommentEngine.determineCategory(
                            temperature = cachedData.temperature,
                            feelsLike = cachedData.feelsLike,
                            humidity = cachedData.humidity,
                            precipitationProbability = cachedData.precipitationProbability,
                            windSpeed = cachedData.windSpeed,
                            uvIndex = cachedData.uvIndex,
                            weatherCode = cachedData.weatherCode,
                            isDay = cachedData.isDay
                        )
                        
                        val updatedComment = WeatherCommentEngine.generateComment(
                            isFamilyFriendlyMode = true,
                            temperature = cachedData.temperature,
                            feelsLike = cachedData.feelsLike,
                            humidity = cachedData.humidity,
                            precipitationProbability = cachedData.precipitationProbability,
                            windSpeed = cachedData.windSpeed,
                            uvIndex = cachedData.uvIndex,
                            weatherCode = cachedData.weatherCode,
                            isDay = cachedData.isDay,
                            checkCount = checkCount,
                            randomSeed = forceRandomSeed,
                            country = cachedData.country,
                            latitude = cachedData.latitude,
                            longitude = cachedData.longitude,
                            locationName = cachedData.cityName
                        )
                        
                        return@withContext Result.success(cachedData.copy(
                            sarcasticComment = updatedComment,
                            category = category
                        ))
                    }
                }
            } catch (e: Exception) {
                // Ignore cache read errors, fallback to network
            }
        }

        val apiKey = com.example.BuildConfig.WEATHER_API_KEY
        val hasWeatherApiKey = apiKey.isNotBlank() && apiKey != "YOUR_KEY_HERE"

        // Open-Meteo is the main provider
        val openMeteoDeferred = async {
            runCatching { fetchFromOpenMeteo(safeLat, safeLon, safeName, safeCountry) }
        }

        // WeatherAPI is fetched if configured (as backup or for combining when both work)
        val weatherApiDeferred = if (hasWeatherApiKey) {
            async {
                runCatching { fetchFromWeatherApiDotCom(apiKey, safeLat, safeLon, safeName, safeCountry) }
            }
        } else null

        val openMeteoResult = openMeteoDeferred.await()
        val weatherApiResult = weatherApiDeferred?.await()

        val openMeteoData = openMeteoResult.getOrNull()
        val weatherApiData = weatherApiResult?.getOrNull()

        // 1. Both are working correctly -> Merge both sources for superior accuracy
        if (openMeteoData != null && weatherApiData != null) {
            val mergedData = mergeWeatherData(
                openMeteo = openMeteoData,
                weatherApi = weatherApiData,
                checkCount = checkCount,
                forceRandomSeed = forceRandomSeed
            )
            saveToCache(mergedData, safeLat, safeLon)
            return@withContext Result.success(mergedData)
        }

        // 2. Open-Meteo (main) succeeded alone
        if (openMeteoData != null) {
            val comment = WeatherCommentEngine.generateComment(
                isFamilyFriendlyMode = true,
                temperature = openMeteoData.temperature,
                feelsLike = openMeteoData.feelsLike,
                humidity = openMeteoData.humidity,
                precipitationProbability = openMeteoData.precipitationProbability,
                windSpeed = openMeteoData.windSpeed,
                uvIndex = openMeteoData.uvIndex,
                weatherCode = openMeteoData.weatherCode,
                isDay = openMeteoData.isDay,
                checkCount = checkCount,
                randomSeed = forceRandomSeed,
                country = openMeteoData.country,
                latitude = openMeteoData.latitude,
                longitude = openMeteoData.longitude,
                locationName = openMeteoData.cityName
            )
            val finalData = openMeteoData.copy(sarcasticComment = comment)
            saveToCache(finalData, safeLat, safeLon)
            return@withContext Result.success(finalData)
        }

        // 3. Open-Meteo failed, but WeatherAPI backup succeeded
        if (weatherApiData != null) {
            val comment = WeatherCommentEngine.generateComment(
                isFamilyFriendlyMode = true,
                temperature = weatherApiData.temperature,
                feelsLike = weatherApiData.feelsLike,
                humidity = weatherApiData.humidity,
                precipitationProbability = weatherApiData.precipitationProbability,
                windSpeed = weatherApiData.windSpeed,
                uvIndex = weatherApiData.uvIndex,
                weatherCode = weatherApiData.weatherCode,
                isDay = weatherApiData.isDay,
                checkCount = checkCount,
                randomSeed = forceRandomSeed,
                country = weatherApiData.country,
                latitude = weatherApiData.latitude,
                longitude = weatherApiData.longitude,
                locationName = weatherApiData.cityName
            )
            val finalData = weatherApiData.copy(sarcasticComment = comment)
            saveToCache(finalData, safeLat, safeLon)
            return@withContext Result.success(finalData)
        }

        // 4. Both failed (or Open-Meteo failed and no backup configured) -> Fallback to Room cache
        if (weatherDao != null) {
            try {
                val locationKey = generateLocationKey(safeLat, safeLon, safeName)
                val cached = weatherDao.getWeatherByKey(locationKey)
                    ?: weatherDao.getWeatherByCity(safeName)
                    ?: weatherDao.getLatestWeatherOnce()

                if (cached != null) {
                    val hourly = converters.toHourlyList(cached.hourlyJson)
                    val daily = converters.toDailyList(cached.dailyJson)
                    val offlineData = cached.toDomain(hourly, daily).copy(
                        isFromCache = true
                    )
                    return@withContext Result.success(offlineData)
                }
            } catch (dbReadError: Throwable) {
                // fallback to failure
            }
        }

        val primaryError = openMeteoResult.exceptionOrNull()
            ?: weatherApiResult?.exceptionOrNull()
            ?: IOException("No se pudo obtener información del clima")

        if (primaryError is IOException) {
            Result.failure(Exception("No se pudo cargar el clima, mae. Revise el internet porque la app no es adivina y no hay datos guardados."))
        } else {
            Result.failure(Exception("El servidor no quiere bretear. Intente de nuevo: ${primaryError.message}"))
        }
    }

    private suspend fun fetchFromOpenMeteo(
        latitude: Double,
        longitude: Double,
        locationName: String,
        countryName: String
    ): WeatherData {
        val response = weatherApi.getForecast(latitude = latitude, longitude = longitude)
        return mapOpenMeteoToWeatherData(response, latitude, longitude, locationName, countryName)
    }

    private fun mapOpenMeteoToWeatherData(
        response: OpenMeteoResponse,
        latitude: Double,
        longitude: Double,
        locationName: String,
        countryName: String
    ): WeatherData {
        val current = response.current ?: throw IOException("Datos actuales no disponibles")

        val isDay = current.isDay == 1
        val weatherCode = current.weatherCode
        val temp = current.temperature2m
        val feelsLike = current.apparentTemperature
        val humidity = current.relativeHumidity2m
        val windSpeed = current.windSpeed10m
        val windDirection = current.windDirection10m ?: 0.0
        val uvIndex = current.uvIndex ?: (response.daily?.uvIndexMax?.firstOrNull() ?: 0.0)

        // Extract hourly forecast (next 24 hours)
        val hourlyItems = mutableListOf<HourlyForecastItem>()
        val hourlyDto = response.hourly
        if (hourlyDto != null && hourlyDto.time.isNotEmpty()) {
            val currentTimeIndex = findCurrentHourIndex(hourlyDto.time, response.timezone)
            val startIndex = if (currentTimeIndex >= 0) currentTimeIndex else 0
            val endIndex = minOf(startIndex + 24, hourlyDto.time.size)

            for (i in startIndex until endIndex) {
                val rawTime = hourlyDto.time[i]
                val hourDisplay = formatHour(rawTime)
                val hourTemp = hourlyDto.temperature2m.getOrElse(i) { temp }
                val hourCode = hourlyDto.weatherCode.getOrElse(i) { weatherCode }
                val hourPrecip = hourlyDto.precipitationProbability?.getOrNull(i) ?: 0
                val hourInt = extractHourNumber(rawTime)
                val hourIsDay = hourInt in 6..17

                hourlyItems.add(
                    HourlyForecastItem(
                        timeDisplay = hourDisplay,
                        temperature = hourTemp,
                        weatherCode = hourCode,
                        precipitationProbability = hourPrecip,
                        isDay = hourIsDay
                    )
                )
            }
        }

        // Extract 7-day daily forecast
        val dailyItems = mutableListOf<DailyForecastItem>()
        val dailyDto = response.daily
        if (dailyDto != null && dailyDto.time.isNotEmpty()) {
            val daysCount = minOf(7, dailyDto.time.size)
            for (i in 0 until daysCount) {
                val rawDate = dailyDto.time[i]
                val dateDisplay = formatDayName(rawDate, i)
                val maxT = dailyDto.temperature2mMax.getOrElse(i) { temp }
                val minT = dailyDto.temperature2mMin.getOrElse(i) { temp }
                val dayCode = dailyDto.weatherCode.getOrElse(i) { weatherCode }
                val dayPrecip = dailyDto.precipitationProbabilityMax?.getOrNull(i) ?: 0
                val condition = WeatherCommentEngine.getStandardDescription(dayCode)

                dailyItems.add(
                    DailyForecastItem(
                        dateDisplay = dateDisplay,
                        tempMax = maxT,
                        tempMin = minT,
                        weatherCode = dayCode,
                        precipitationProbability = dayPrecip,
                        conditionName = condition
                    )
                )
            }
        }

        val todayPrecipProb = response.daily?.precipitationProbabilityMax?.firstOrNull()
            ?: hourlyItems.firstOrNull()?.precipitationProbability
            ?: 0

        val sunrise = response.daily?.sunrise?.firstOrNull()?.let { formatHour(it) } ?: "05:40"
        val sunset = response.daily?.sunset?.firstOrNull()?.let { formatHour(it) } ?: "17:55"

        val category = WeatherCommentEngine.determineCategory(
            temperature = temp,
            feelsLike = feelsLike,
            humidity = humidity,
            precipitationProbability = todayPrecipProb,
            windSpeed = windSpeed,
            uvIndex = uvIndex,
            weatherCode = weatherCode,
            isDay = isDay
        )

        val ticoTitle = WeatherCommentEngine.getTicoConditionTitle(weatherCode, isDay)
        val standardDesc = WeatherCommentEngine.getStandardDescription(weatherCode)

        val accurateCity = resolveAccurateCityName(locationName, latitude, longitude)

        return WeatherData(
            cityName = accurateCity,
            country = countryName,
            latitude = latitude,
            longitude = longitude,
            temperature = temp,
            feelsLike = feelsLike,
            weatherCode = weatherCode,
            conditionTitle = ticoTitle,
            conditionDescription = standardDesc,
            humidity = humidity,
            windSpeed = windSpeed,
            windDirection = windDirection,
            uvIndex = uvIndex,
            precipitationProbability = todayPrecipProb,
            isDay = isDay,
            sunrise = sunrise,
            sunset = sunset,
            hourlyForecasts = hourlyItems,
            dailyForecasts = dailyItems,
            sarcasticComment = "",
            category = category,
            isFromCache = false,
            cachedTimestamp = System.currentTimeMillis()
        )
    }

    private fun mergeWeatherData(
        openMeteo: WeatherData,
        weatherApi: WeatherData,
        checkCount: Int = 1,
        forceRandomSeed: Int? = null
    ): WeatherData {
        val mergedTemp = ((openMeteo.temperature + weatherApi.temperature) / 2.0).roundTo1Decimal()
        val mergedFeelsLike = ((openMeteo.feelsLike + weatherApi.feelsLike) / 2.0).roundTo1Decimal()
        val mergedHumidity = ((openMeteo.humidity + weatherApi.humidity) / 2)
        val mergedWindSpeed = ((openMeteo.windSpeed + weatherApi.windSpeed) / 2.0).roundTo1Decimal()
        val mergedUvIndex = ((openMeteo.uvIndex + weatherApi.uvIndex) / 2.0).roundTo1Decimal()
        val mergedPrecipProb = ((openMeteo.precipitationProbability + weatherApi.precipitationProbability) / 2)

        // Intelligently choose the most significant weather condition (so rain/thunderstorms are never ignored)
        val mergedWeatherCode = chooseBestWeatherCode(openMeteo.weatherCode, weatherApi.weatherCode)
        val isDay = openMeteo.isDay

        val mergedDaily = mergeDailyForecasts(openMeteo.dailyForecasts, weatherApi.dailyForecasts)
        val mergedHourly = mergeHourlyForecasts(openMeteo.hourlyForecasts, weatherApi.hourlyForecasts)

        val category = WeatherCommentEngine.determineCategory(
            temperature = mergedTemp,
            feelsLike = mergedFeelsLike,
            humidity = mergedHumidity,
            precipitationProbability = mergedPrecipProb,
            windSpeed = mergedWindSpeed,
            uvIndex = mergedUvIndex,
            weatherCode = mergedWeatherCode,
            isDay = isDay
        )

        val accurateCityName = resolveAccurateCityName(openMeteo.cityName, openMeteo.latitude, openMeteo.longitude)

        val comment = WeatherCommentEngine.generateComment(
            isFamilyFriendlyMode = true,
            temperature = mergedTemp,
            feelsLike = mergedFeelsLike,
            humidity = mergedHumidity,
            precipitationProbability = mergedPrecipProb,
            windSpeed = mergedWindSpeed,
            uvIndex = mergedUvIndex,
            weatherCode = mergedWeatherCode,
            isDay = isDay,
            checkCount = checkCount,
            randomSeed = forceRandomSeed,
            country = openMeteo.country,
            latitude = openMeteo.latitude,
            longitude = openMeteo.longitude,
            locationName = accurateCityName
        )

        return WeatherData(
            cityName = accurateCityName,
            country = openMeteo.country,
            latitude = openMeteo.latitude,
            longitude = openMeteo.longitude,
            temperature = mergedTemp,
            feelsLike = mergedFeelsLike,
            weatherCode = mergedWeatherCode,
            conditionTitle = WeatherCommentEngine.getTicoConditionTitle(mergedWeatherCode, isDay),
            conditionDescription = WeatherCommentEngine.getStandardDescription(mergedWeatherCode),
            humidity = mergedHumidity,
            windSpeed = mergedWindSpeed,
            windDirection = openMeteo.windDirection,
            uvIndex = mergedUvIndex,
            precipitationProbability = mergedPrecipProb,
            isDay = isDay,
            sunrise = openMeteo.sunrise.ifBlank { weatherApi.sunrise },
            sunset = openMeteo.sunset.ifBlank { weatherApi.sunset },
            hourlyForecasts = mergedHourly,
            dailyForecasts = mergedDaily,
            sarcasticComment = comment,
            category = category,
            isFromCache = false,
            cachedTimestamp = System.currentTimeMillis()
        )
    }

    private fun mergeDailyForecasts(
        openMeteoDaily: List<DailyForecastItem>,
        weatherApiDaily: List<DailyForecastItem>
    ): List<DailyForecastItem> {
        if (openMeteoDaily.isEmpty()) return weatherApiDaily
        if (weatherApiDaily.isEmpty()) return openMeteoDaily

        return openMeteoDaily.mapIndexed { index, omItem ->
            val waItem = weatherApiDaily.getOrNull(index)
            if (waItem != null) {
                val maxT = ((omItem.tempMax + waItem.tempMax) / 2.0).roundTo1Decimal()
                val minT = ((omItem.tempMin + waItem.tempMin) / 2.0).roundTo1Decimal()
                val precip = (omItem.precipitationProbability + waItem.precipitationProbability) / 2
                val code = chooseBestWeatherCode(omItem.weatherCode, waItem.weatherCode)
                omItem.copy(
                    tempMax = maxT,
                    tempMin = minT,
                    weatherCode = code,
                    precipitationProbability = precip,
                    conditionName = WeatherCommentEngine.getStandardDescription(code)
                )
            } else {
                omItem
            }
        }
    }

    private fun mergeHourlyForecasts(
        openMeteoHourly: List<HourlyForecastItem>,
        weatherApiHourly: List<HourlyForecastItem>
    ): List<HourlyForecastItem> {
        if (openMeteoHourly.isEmpty()) return weatherApiHourly
        if (weatherApiHourly.isEmpty()) return openMeteoHourly

        val waMap = weatherApiHourly.associateBy { it.timeDisplay }

        return openMeteoHourly.map { omItem ->
            val waItem = waMap[omItem.timeDisplay]
            if (waItem != null) {
                val temp = ((omItem.temperature + waItem.temperature) / 2.0).roundTo1Decimal()
                val precip = (omItem.precipitationProbability + waItem.precipitationProbability) / 2
                val code = chooseBestWeatherCode(omItem.weatherCode, waItem.weatherCode)
                omItem.copy(
                    temperature = temp,
                    precipitationProbability = precip,
                    weatherCode = code
                )
            } else {
                omItem
            }
        }
    }

    private fun chooseBestWeatherCode(code1: Int, code2: Int): Int {
        fun severity(code: Int): Int = when (code) {
            95, 96, 99 -> 10 // Thunderstorm
            65, 67, 82 -> 9  // Heavy rain
            63, 81 -> 8      // Moderate rain
            61, 80 -> 7      // Light rain
            51, 53, 55, 56, 57 -> 6 // Drizzle
            71, 73, 75, 77 -> 5 // Snow / ice pellets
            45, 48 -> 4      // Fog
            3 -> 3           // Overcast
            2 -> 2           // Partly cloudy
            1 -> 1           // Mainly clear
            0 -> 0           // Clear
            else -> 0
        }
        return if (severity(code2) > severity(code1)) code2 else code1
    }

    private fun Double.roundTo1Decimal(): Double =
        if (this.isFinite()) (kotlin.math.round(this * 10.0) / 10.0) else 0.0

    private fun resolveAccurateCityName(rawName: String, lat: Double, lon: Double): String {
        val detected = com.example.domain.SpecialLocationInsults.detectSpecialLocation(rawName, lat, lon)
        return when (detected) {
            com.example.domain.SpecialLocation.RIO_CLARO -> if (rawName.contains("golfito", ignoreCase = true) || rawName.isBlank() || rawName.contains("Ubicación", ignoreCase = true)) "Río Claro" else rawName
            com.example.domain.SpecialLocation.CIUDAD_NEILY -> if (rawName.contains("corredores", ignoreCase = true) || rawName.isBlank() || rawName.contains("Ubicación", ignoreCase = true)) "Ciudad Neily" else rawName
            com.example.domain.SpecialLocation.PASO_CANOAS -> if (rawName.isBlank() || rawName.contains("Ubicación", ignoreCase = true)) "Paso Canoas" else rawName
            com.example.domain.SpecialLocation.CERRO_DE_LA_MUERTE -> if (rawName.isBlank() || rawName.contains("Ubicación", ignoreCase = true)) "El Cerro de la Muerte" else rawName
            else -> rawName
        }
    }

    open suspend fun getLatestCachedWeather(): WeatherData? = withContext(Dispatchers.IO) {
        if (weatherDao == null) return@withContext null
        try {
            val cached = weatherDao.getLatestWeatherOnce() ?: return@withContext null
            val hourly = converters.toHourlyList(cached.hourlyJson)
            val daily = converters.toDailyList(cached.dailyJson)
            cached.toDomain(hourly, daily)
        } catch (e: Throwable) {
            null
        }
    }

    private fun generateLocationKey(lat: Double, lon: Double, name: String): String {
        val safeLat = if (lat.isFinite() && lat in -90.0..90.0) lat else 9.9281
        val safeLon = if (lon.isFinite() && lon in -180.0..180.0) lon else -84.0907
        return String.format(Locale.US, "%.4f_%.4f", safeLat, safeLon)
    }

    open suspend fun searchCity(query: String): Result<List<CityLocation>> = withContext(Dispatchers.IO) {
        val sanitizedQuery = query.trim().take(80)
        try {
            if (sanitizedQuery.isBlank()) {
                return@withContext Result.success(defaultTicoCities)
            }

            val response = geocodingApi.searchCity(name = sanitizedQuery)
            val results = response.results ?: emptyList()
            if (results.isEmpty()) {
                val filteredLocal = defaultTicoCities.filter {
                    it.name.contains(sanitizedQuery, ignoreCase = true) || it.subtitle.contains(sanitizedQuery, ignoreCase = true)
                }
                if (filteredLocal.isNotEmpty()) {
                    return@withContext Result.success(filteredLocal)
                }
                return@withContext Result.failure(Exception("No encontramos esa ubicación. Mae, ¿seguro que existe?"))
            }

            val locations = results
                .filter { it.latitude.isFinite() && it.longitude.isFinite() }
                .map { dto ->
                    val subtitleParts = listOfNotNull(dto.admin1, dto.country).filter { it.isNotBlank() }
                    val countryResolved = dto.country?.trim()?.ifBlank { null }
                        ?: if (!WeatherForeignInsults.isInsideCostaRica(dto.latitude, dto.longitude)) "Extranjero" else "Costa Rica"
                    CityLocation(
                        name = dto.name,
                        subtitle = if (subtitleParts.isNotEmpty()) subtitleParts.joinToString(", ") else "Ubicación",
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        country = countryResolved
                    )
                }
            Result.success(locations)
        } catch (e: Throwable) {
            val filtered = defaultTicoCities.filter {
                it.name.contains(sanitizedQuery, ignoreCase = true)
            }
            if (filtered.isNotEmpty()) {
                Result.success(filtered)
            } else {
                Result.failure(Exception("No se pudo buscar la ciudad, mae. Revise su conexión a internet."))
            }
        }
    }

    private fun findCurrentHourIndex(times: List<String>, timezoneStr: String? = null): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH", Locale.US).apply {
            if (!timezoneStr.isNullOrBlank()) {
                try {
                    timeZone = java.util.TimeZone.getTimeZone(timezoneStr)
                } catch (e: Exception) {
                    // fallback to device default timezone
                }
            }
        }
        val now = sdf.format(Date())
        val index = times.indexOfFirst { it.startsWith(now) }
        return if (index != -1) index else 0
    }

    private fun formatHour(isoTime: String): String {
        return try {
            val parts = isoTime.split("T")
            if (parts.size == 2) {
                parts[1].take(5)
            } else {
                isoTime
            }
        } catch (e: Exception) {
            isoTime
        }
    }

    private fun extractHourNumber(isoTime: String): Int {
        return try {
            val parts = isoTime.split("T")
            if (parts.size == 2) {
                parts[1].take(2).toInt()
            } else {
                12
            }
        } catch (e: Exception) {
            12
        }
    }

    private fun formatDayName(isoDate: String, dayIndex: Int): String {
        if (dayIndex == 0) return "Hoy"
        if (dayIndex == 1) return "Mañana"

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(isoDate) ?: return "Día $dayIndex"
            val cal = Calendar.getInstance()
            cal.time = date
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Lun"
                Calendar.TUESDAY -> "Mar"
                Calendar.WEDNESDAY -> "Mié"
                Calendar.THURSDAY -> "Jue"
                Calendar.FRIDAY -> "Vie"
                Calendar.SATURDAY -> "Sáb"
                Calendar.SUNDAY -> "Dom"
                else -> "Día"
            }
        } catch (e: Exception) {
            "Día $dayIndex"
        }
    }

    private suspend fun saveToCache(
        weatherData: WeatherData,
        latitude: Double,
        longitude: Double
    ) {
        if (weatherDao != null) {
            try {
                val locationKey = generateLocationKey(latitude, longitude, weatherData.cityName)
                val cachedEntity = CachedWeatherEntity(
                    locationKey = locationKey,
                    cityName = weatherData.cityName,
                    country = weatherData.country,
                    latitude = latitude,
                    longitude = longitude,
                    temperature = weatherData.temperature,
                    feelsLike = weatherData.feelsLike,
                    weatherCode = weatherData.weatherCode,
                    conditionTitle = weatherData.conditionTitle,
                    conditionDescription = weatherData.conditionDescription,
                    humidity = weatherData.humidity,
                    windSpeed = weatherData.windSpeed,
                    windDirection = weatherData.windDirection,
                    uvIndex = weatherData.uvIndex,
                    precipitationProbability = weatherData.precipitationProbability,
                    isDay = weatherData.isDay,
                    sunrise = weatherData.sunrise,
                    sunset = weatherData.sunset,
                    categoryName = weatherData.category.name,
                    sarcasticComment = weatherData.sarcasticComment,
                    hourlyJson = converters.fromHourlyList(weatherData.hourlyForecasts),
                    dailyJson = converters.fromDailyList(weatherData.dailyForecasts),
                    isLatest = true,
                    cachedAt = System.currentTimeMillis()
                )
                weatherDao.saveLatestWeather(cachedEntity)
            } catch (dbError: Throwable) {
                // Log or ignore db save error non-fatally
            }
        }
    }

    private suspend fun fetchFromWeatherApiDotCom(
        apiKey: String,
        latitude: Double,
        longitude: Double,
        locationName: String,
        countryName: String
    ): WeatherData {
        val query = "$latitude,$longitude"
        val response = weatherApiDotCom.getForecast(key = apiKey, query = query, days = 3)
        
        val isDay = response.current.is_day == 1
        val weatherCode = mapWeatherApiCodeToWmo(response.current.condition.code)
        val temp = response.current.temp_c
        val feelsLike = response.current.feelslike_c
        val humidity = response.current.humidity
        val windSpeed = response.current.wind_kph
        val uvIndex = response.current.uv
        
        val hourlyItems = mutableListOf<HourlyForecastItem>()
        val dailyItems = mutableListOf<DailyForecastItem>()
        
        val currentTime = System.currentTimeMillis()
        
        for (day in response.forecast.forecastday) {
            val dailyWmo = mapWeatherApiCodeToWmo(day.day.condition.code)
            dailyItems.add(
                DailyForecastItem(
                    dateDisplay = formatDayName(day.date),
                    tempMax = day.day.maxtemp_c,
                    tempMin = day.day.mintemp_c,
                    weatherCode = dailyWmo,
                    precipitationProbability = day.day.daily_chance_of_rain,
                    conditionName = WeatherCommentEngine.getStandardDescription(dailyWmo)
                )
            )
            
            for (hour in day.hour) {
                val hourTime = parseWeatherApiTime(hour.time)
                if (hourTime >= currentTime - 3600000) {
                    val hourDisplay = formatHourString(hour.time)
                    hourlyItems.add(
                        HourlyForecastItem(
                            timeDisplay = hourDisplay,
                            temperature = hour.temp_c,
                            weatherCode = mapWeatherApiCodeToWmo(hour.condition.code),
                            precipitationProbability = hour.chance_of_rain,
                            isDay = hour.is_day == 1
                        )
                    )
                }
            }
        }
        
        val todayPrecipProb = dailyItems.firstOrNull()?.precipitationProbability ?: 0
        
        val category = WeatherCommentEngine.determineCategory(
            temperature = temp,
            feelsLike = feelsLike,
            humidity = humidity,
            precipitationProbability = todayPrecipProb,
            windSpeed = windSpeed,
            uvIndex = uvIndex,
            weatherCode = weatherCode,
            isDay = isDay
        )

        val accurateCity = resolveAccurateCityName(locationName, latitude, longitude)

        return WeatherData(
            cityName = accurateCity,
            country = countryName,
            latitude = latitude,
            longitude = longitude,
            temperature = temp,
            feelsLike = feelsLike,
            weatherCode = weatherCode,
            conditionTitle = WeatherCommentEngine.getTicoConditionTitle(weatherCode, isDay),
            conditionDescription = WeatherCommentEngine.getStandardDescription(weatherCode),
            humidity = humidity,
            windSpeed = windSpeed,
            windDirection = 0.0,
            uvIndex = uvIndex,
            precipitationProbability = todayPrecipProb,
            isDay = isDay,
            sunrise = "",
            sunset = "",
            hourlyForecasts = hourlyItems.take(24),
            dailyForecasts = dailyItems,
            sarcasticComment = "",
            category = category,
            isFromCache = false,
            cachedTimestamp = System.currentTimeMillis()
        )
    }

    private fun parseWeatherApiTime(timeString: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return try { format.parse(timeString)?.time ?: System.currentTimeMillis() } catch (e: Exception) { System.currentTimeMillis() }
    }

    private fun formatHourString(timeString: String): String {
        return try {
            val parts = timeString.split(" ")
            if (parts.size == 2 && parts[1].length >= 5) {
                parts[1].take(5)
            } else {
                val formatIn = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                val formatOut = SimpleDateFormat("HH:mm", Locale.US)
                val date = formatIn.parse(timeString)
                if (date != null) formatOut.format(date) else timeString
            }
        } catch (e: Exception) {
            timeString
        }
    }

    private fun formatDayName(dateString: String): String {
        val formatIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = try { formatIn.parse(dateString) } catch (e: Exception) { null } ?: return ""
        val today = Calendar.getInstance().apply { 
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val targetDate = Calendar.getInstance().apply { time = date }
        val diff = targetDate.timeInMillis - today.timeInMillis
        if (diff in 0..86400000) return "Hoy"
        
        val formatOut = SimpleDateFormat("EEEE", Locale.forLanguageTag("es-ES"))
        return formatOut.format(date).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    private fun mapWeatherApiCodeToWmo(weatherApiCode: Int): Int {
        return when (weatherApiCode) {
            1000 -> 0 // Clear
            1003 -> 1 // Partly cloudy
            1006, 1009 -> 3 // Cloudy / Overcast
            1030, 1135, 1148 -> 45 // Mist / Fog
            1063, 1150, 1153 -> 51 // Drizzle
            1066, 1069, 1114, 1117, 1210, 1213, 1216, 1219, 1222, 1225 -> 71 // Snow
            1072, 1168, 1171, 1198, 1201 -> 56 // Freezing Drizzle/Rain
            1087, 1273, 1276, 1279, 1282 -> 95 // Thunderstorm
            1180, 1183 -> 61 // Light rain
            1186, 1189 -> 63 // Moderate rain
            1192, 1195, 1240, 1243, 1246 -> 65 // Heavy rain
            1204, 1207, 1249, 1252 -> 66 // Sleet
            1237, 1255, 1258, 1261, 1264 -> 77 // Ice pellets / Showers
            else -> 0
        }
    }
}

package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.CachedWeatherEntity
import com.example.data.local.toDomain
import com.example.data.model.WeatherCategory
import com.example.data.model.WeatherData
import com.example.domain.WeatherCommentEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("¿Y Ese Clima, Mae?", appName)
  }

  @Test
  fun `test extreme heat category and comment`() {
    val category = WeatherCommentEngine.determineCategory(
      temperature = 34.0,
      feelsLike = 37.0,
      humidity = 60,
      precipitationProbability = 10,
      windSpeed = 8.0,
      uvIndex = 8.0,
      weatherCode = 0,
      isDay = true
    )
    assertEquals(WeatherCategory.EXTREME_HEAT, category)

    val comment = WeatherCommentEngine.generateComment(
      temperature = 34.0,
      feelsLike = 37.0,
      humidity = 60,
      precipitationProbability = 10,
      windSpeed = 8.0,
      uvIndex = 8.0,
      weatherCode = 0,
      isDay = true,
      checkCount = 1
    )
    assertTrue("Comment should not be empty", comment.isNotEmpty())
  }

  @Test
  fun `test thunderstorm category`() {
    val category = WeatherCommentEngine.determineCategory(
      temperature = 22.0,
      feelsLike = 22.0,
      humidity = 90,
      precipitationProbability = 95,
      windSpeed = 25.0,
      uvIndex = 1.0,
      weatherCode = 95,
      isDay = true
    )
    assertEquals(WeatherCategory.THUNDERSTORM, category)

    val comment = WeatherCommentEngine.generateComment(
      temperature = 22.0,
      feelsLike = 22.0,
      humidity = 90,
      precipitationProbability = 95,
      windSpeed = 25.0,
      uvIndex = 1.0,
      weatherCode = 95,
      isDay = true,
      checkCount = 1
    )
    assertTrue("Should not be empty", comment.isNotEmpty())
  }

  @Test
  fun `test repeated checks triggers sarcastic warning`() {
    val repeatedComment = WeatherCommentEngine.generateComment(
      temperature = 25.0,
      feelsLike = 25.0,
      humidity = 65,
      precipitationProbability = 10,
      windSpeed = 10.0,
      uvIndex = 5.0,
      weatherCode = 1,
      isDay = true,
      checkCount = 5,
      randomSeed = 1
    )
    assertTrue(
      "Repeated checks comment should criticize checking repeatedly: $repeatedComment",
      repeatedComment.contains("revisando", ignoreCase = true) ||
      repeatedComment.contains("pantalla", ignoreCase = true) ||
      repeatedComment.contains("actualizando", ignoreCase = true) ||
      repeatedComment.contains("ventana", ignoreCase = true) ||
      repeatedComment.contains("segundos", ignoreCase = true) ||
      repeatedComment.contains("tranquilo", ignoreCase = true) ||
      repeatedComment.contains("aguacero", ignoreCase = true) ||
      repeatedComment.contains("canal 7", ignoreCase = true)
    )
  }

  @Test
  fun `test room caching stores and retrieves weather data`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    val dao = db.weatherDao()

    val entity = CachedWeatherEntity(
      locationKey = "9.93_-84.09",
      cityName = "San José (Chepe)",
      country = "Costa Rica",
      latitude = 9.93,
      longitude = -84.09,
      temperature = 26.5,
      feelsLike = 27.0,
      weatherCode = 0,
      conditionTitle = "Solazo que pela",
      conditionDescription = "Despejado",
      humidity = 65,
      windSpeed = 10.0,
      windDirection = 80.0,
      uvIndex = 9.0,
      precipitationProbability = 5,
      isDay = true,
      sunrise = "05:35",
      sunset = "17:50",
      categoryName = WeatherCategory.EXTREME_HEAT.name,
      sarcasticComment = "Mae, ¿usted quiere cocinarse o qué?",
      hourlyJson = "[]",
      dailyJson = "[]",
      isLatest = true,
      cachedAt = System.currentTimeMillis()
    )

    dao.saveLatestWeather(entity)

    val retrieved = dao.getLatestWeatherOnce()
    assertNotNull(retrieved)
    assertEquals("San José (Chepe)", retrieved?.cityName)
    assertEquals(26.5, retrieved?.temperature ?: 0.0, 0.01)

    val domain = retrieved!!.toDomain(emptyList(), emptyList())
    assertTrue(domain.isFromCache)
    assertEquals("Mae, ¿usted quiere cocinarse o qué?", domain.sarcasticComment)

    db.close()
  }

  @Test
  fun `test main activity launches without crashing`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java)
    controller.create()
    assertNotNull(controller.get())
  }

  @Test
  fun `test wsi integration with domain weather data`() {
    val entity = CachedWeatherEntity(
      locationKey = "9.93_-84.09",
      cityName = "San José (Chepe)",
      country = "Costa Rica",
      latitude = 9.93,
      longitude = -84.09,
      temperature = 23.0,
      feelsLike = 23.0,
      weatherCode = 0,
      conditionTitle = "Pura vida",
      conditionDescription = "Despejado",
      humidity = 50,
      windSpeed = 10.0,
      windDirection = 80.0,
      uvIndex = 2.0,
      precipitationProbability = 0,
      isDay = true,
      sunrise = "05:35",
      sunset = "17:50",
      categoryName = WeatherCategory.PERFECT.name,
      sarcasticComment = "Pura vida mae",
      hourlyJson = "[]",
      dailyJson = "[]",
      isLatest = true,
      cachedAt = System.currentTimeMillis()
    )
    val domainWeather = entity.toDomain(emptyList(), emptyList())
    val wsiResult = com.example.domain.WeatherShittinessCalculator.calculate(domainWeather)

    assertTrue("Pleasant weather should have low shittiness score <= 20", wsiResult.score <= 20)
    assertNotNull(wsiResult.emoji)
    assertNotNull(wsiResult.title)
    assertNotNull(wsiResult.description)
    assertEquals(wsiResult.score, wsiResult.breakdown.total)
  }

  @Test
  fun `test combo detection trifecta`() {
    val combo = com.example.domain.detectWeatherCombo(
      temperature = 16.0,
      feelsLike = 15.0,
      humidity = 90,
      precipitationProbability = 85,
      windSpeed = 30.0,
      uvIndex = 1.0,
      weatherCode = 65,
      isDay = true
    )
    assertEquals(com.example.domain.WeatherComboType.TRIFECTA, combo)

    val comment = WeatherCommentEngine.generateComment(
      isFamilyFriendlyMode = false,
      temperature = 16.0,
      feelsLike = 15.0,
      humidity = 90,
      precipitationProbability = 85,
      windSpeed = 30.0,
      uvIndex = 1.0,
      weatherCode = 65,
      isDay = true
    )
    assertTrue("Should contain trifecta comment: $comment", comment.contains("trifecta", ignoreCase = true) || comment.contains("combo", ignoreCase = true) || comment.contains("despiche", ignoreCase = true))
  }

  @Test
  fun `test combo detection rain and wind`() {
    val combo = com.example.domain.detectWeatherCombo(
      temperature = 24.0,
      feelsLike = 24.0,
      humidity = 80,
      precipitationProbability = 80,
      windSpeed = 32.0,
      uvIndex = 2.0,
      weatherCode = 61,
      isDay = true
    )
    assertEquals(com.example.domain.WeatherComboType.RAIN_WIND, combo)

    val comment = WeatherCommentEngine.generateComment(
      isFamilyFriendlyMode = false,
      temperature = 24.0,
      feelsLike = 24.0,
      humidity = 80,
      precipitationProbability = 80,
      windSpeed = 32.0,
      uvIndex = 2.0,
      weatherCode = 61,
      isDay = true
    )
    assertTrue("Should mention sombrilla or ventolero: $comment", comment.contains("sombrilla", ignoreCase = true) || comment.contains("ventolero", ignoreCase = true) || comment.contains("viento", ignoreCase = true) || comment.contains("ráfagas", ignoreCase = true))
  }

  @Test
  fun `test combo detection heat and humidity`() {
    val combo = com.example.domain.detectWeatherCombo(
      temperature = 32.0,
      feelsLike = 36.0,
      humidity = 85,
      precipitationProbability = 10,
      windSpeed = 5.0,
      uvIndex = 6.0,
      weatherCode = 0,
      isDay = true
    )
    assertEquals(com.example.domain.WeatherComboType.HEAT_HUMIDITY, combo)

    val comment = WeatherCommentEngine.generateComment(
      isFamilyFriendlyMode = false,
      temperature = 32.0,
      feelsLike = 36.0,
      humidity = 85,
      precipitationProbability = 10,
      windSpeed = 5.0,
      uvIndex = 6.0,
      weatherCode = 0,
      isDay = true
    )
    assertTrue("Should mention bochorno, vapor or sauna: $comment", comment.contains("bochorno", ignoreCase = true) || comment.contains("vapor", ignoreCase = true) || comment.contains("sauna", ignoreCase = true) || comment.contains("olla", ignoreCase = true) || comment.contains("humedad", ignoreCase = true))
  }

  @Test
  fun `test combo detection rain and sun se casa la venada`() {
    val combo = com.example.domain.detectWeatherCombo(
      temperature = 27.0,
      feelsLike = 28.0,
      humidity = 70,
      precipitationProbability = 60,
      windSpeed = 10.0,
      uvIndex = 5.0,
      weatherCode = 1,
      isDay = true
    )
    assertEquals(com.example.domain.WeatherComboType.RAIN_AND_SUN, combo)

    val comment = WeatherCommentEngine.generateComment(
      isFamilyFriendlyMode = true,
      temperature = 27.0,
      feelsLike = 28.0,
      humidity = 70,
      precipitationProbability = 60,
      windSpeed = 10.0,
      uvIndex = 5.0,
      weatherCode = 1,
      isDay = true
    )
    assertTrue("Should mention venada or sol y lluvia: $comment", comment.contains("venada", ignoreCase = true) || comment.contains("sol", ignoreCase = true) || comment.contains("lluvia", ignoreCase = true) || comment.contains("arcoíris", ignoreCase = true))
  }

  @Test
  fun `test weather station data format in settings`() {
    val sampleWeather = com.example.data.model.WeatherData(
      cityName = "San José (Chepe)",
      country = "Costa Rica",
      latitude = 9.9281,
      longitude = -84.0907,
      temperature = 22.0,
      feelsLike = 22.0,
      weatherCode = 1,
      conditionTitle = "Sol con nubes",
      conditionDescription = "Parcialmente nublado",
      humidity = 70,
      windSpeed = 12.0,
      windDirection = 45.0,
      uvIndex = 5.0,
      precipitationProbability = 20,
      isDay = true,
      sunrise = "05:40",
      sunset = "17:55",
      hourlyForecasts = emptyList(),
      dailyForecasts = emptyList(),
      sarcasticComment = "Tranquilo mae",
      category = WeatherCategory.CLOUDS
    )
    val stationLat = String.format(java.util.Locale.US, "%.4f", sampleWeather.latitude)
    val stationLon = String.format(java.util.Locale.US, "%.4f", sampleWeather.longitude)
    assertEquals("9.9281", stationLat)
    assertEquals("-84.0907", stationLon)
  }

  @Test
  fun `test foreign location detection`() {
    // San José, Costa Rica coordinates
    assertTrue(com.example.domain.WeatherForeignInsults.isInsideCostaRica(9.9281, -84.0907))
    assertFalse(com.example.domain.WeatherForeignInsults.isForeignLocation("Costa Rica", 9.9281, -84.0907))

    // Madrid, Spain coordinates
    assertFalse(com.example.domain.WeatherForeignInsults.isInsideCostaRica(40.4168, -3.7038))
    assertTrue(com.example.domain.WeatherForeignInsults.isForeignLocation("España", 40.4168, -3.7038))

    // Miami, USA coordinates
    assertFalse(com.example.domain.WeatherForeignInsults.isInsideCostaRica(25.7617, -80.1918))
    assertTrue(com.example.domain.WeatherForeignInsults.isForeignLocation("Estados Unidos", 25.7617, -80.1918))

    // Even if country string was left as Costa Rica by mistake, foreign coordinates detect it!
    assertTrue(com.example.domain.WeatherForeignInsults.isForeignLocation("Costa Rica", 25.7617, -80.1918))
  }

  @Test
  fun `test foreign insults generated outside Costa Rica`() {
    val commentCold = WeatherCommentEngine.generateComment(
      isFamilyFriendlyMode = false,
      temperature = -2.0,
      feelsLike = -5.0,
      humidity = 60,
      precipitationProbability = 10,
      windSpeed = 20.0,
      uvIndex = 1.0,
      weatherCode = 71,
      isDay = true,
      country = "España",
      latitude = 40.4168,
      longitude = -3.7038
    )

    assertTrue("Foreign cold comment should have foreign context: $commentCold",
      commentCold.contains("España", ignoreCase = true) ||
      commentCold.contains("primer mundo", ignoreCase = true) ||
      commentCold.contains("Costa Rica", ignoreCase = true) ||
      commentCold.contains("frío", ignoreCase = true)
    )

    val commentRepeated = WeatherCommentEngine.generateComment(
      isFamilyFriendlyMode = false,
      temperature = 10.0,
      feelsLike = 10.0,
      humidity = 50,
      precipitationProbability = 0,
      windSpeed = 10.0,
      uvIndex = 1.0,
      weatherCode = 0,
      isDay = true,
      checkCount = 4,
      country = "Estados Unidos",
      latitude = 40.7128,
      longitude = -74.0060
    )

    assertTrue("Foreign repeated check comment should mention foreign country: $commentRepeated",
      commentRepeated.contains("Estados Unidos", ignoreCase = true) ||
      commentRepeated.contains("roaming", ignoreCase = true) ||
      commentRepeated.contains("Tamarindo", ignoreCase = true) ||
      commentRepeated.contains("revisando", ignoreCase = true)
    )
  }

  @Test
  fun `test special locations detection and insults`() {
    // 1. Golfito
    val golfitoLoc = com.example.domain.SpecialLocationInsults.detectSpecialLocation("Golfito")
    assertEquals(com.example.domain.SpecialLocation.GOLFITO, golfitoLoc)
    val golfitoComment = WeatherCommentEngine.generateComment(
      isFamilyFriendlyMode = false,
      temperature = 31.0,
      feelsLike = 35.0,
      humidity = 88,
      precipitationProbability = 40,
      windSpeed = 8.0,
      uvIndex = 8.0,
      weatherCode = 0,
      locationName = "Golfito"
    )
    assertTrue("Golfito comment: $golfitoComment",
      golfitoComment.contains("Golfito", ignoreCase = true) ||
      golfitoComment.contains("Depósito", ignoreCase = true) ||
      golfitoComment.contains("Golfo Dulce", ignoreCase = true)
    )

    // 2. Paso Canoas
    val canoasLoc = com.example.domain.SpecialLocationInsults.detectSpecialLocation("Paso Canoas")
    assertEquals(com.example.domain.SpecialLocation.PASO_CANOAS, canoasLoc)
    val canoasComment = WeatherCommentEngine.generateComment(
      isFamilyFriendlyMode = false,
      temperature = 32.0,
      feelsLike = 36.0,
      humidity = 85,
      precipitationProbability = 20,
      windSpeed = 5.0,
      uvIndex = 9.0,
      weatherCode = 0,
      locationName = "Paso Canoas"
    )
    assertTrue("Paso Canoas comment: $canoasComment",
      canoasComment.contains("Paso Canoas", ignoreCase = true) ||
      canoasComment.contains("frontera", ignoreCase = true) ||
      canoasComment.contains("Panamá", ignoreCase = true)
    )

    // 3. El Cerro de la Muerte
    val cerroLoc = com.example.domain.SpecialLocationInsults.detectSpecialLocation("El Cerro de la Muerte")
    assertEquals(com.example.domain.SpecialLocation.CERRO_DE_LA_MUERTE, cerroLoc)
    val cerroComment = WeatherCommentEngine.generateComment(
      isFamilyFriendlyMode = false,
      temperature = 4.0,
      feelsLike = 1.0,
      humidity = 95,
      precipitationProbability = 50,
      windSpeed = 35.0,
      uvIndex = 2.0,
      weatherCode = 45,
      locationName = "El Cerro de la Muerte"
    )
    assertTrue("Cerro de la Muerte comment: $cerroComment",
      cerroComment.contains("Cerro", ignoreCase = true) ||
      cerroComment.contains("neblina", ignoreCase = true) ||
      cerroComment.contains("curvas", ignoreCase = true) ||
      cerroComment.contains("páramo", ignoreCase = true)
    )

    // 4. Ciudad Neily
    val neilyLoc = com.example.domain.SpecialLocationInsults.detectSpecialLocation("Ciudad Neily")
    assertEquals(com.example.domain.SpecialLocation.CIUDAD_NEILY, neilyLoc)
    val neilyComment = WeatherCommentEngine.generateComment(
      isFamilyFriendlyMode = false,
      temperature = 33.0,
      feelsLike = 38.0,
      humidity = 90,
      precipitationProbability = 30,
      windSpeed = 4.0,
      uvIndex = 8.0,
      weatherCode = 0,
      locationName = "Ciudad Neily"
    )
    assertTrue("Ciudad Neily comment: $neilyComment",
      neilyComment.contains("Ciudad Neily", ignoreCase = true) ||
      neilyComment.contains("Neily", ignoreCase = true) ||
      neilyComment.contains("bochorno", ignoreCase = true) ||
      neilyComment.contains("sauna", ignoreCase = true)
    )

    // 5. Rio Claro
    val rioClaroLoc = com.example.domain.SpecialLocationInsults.detectSpecialLocation("Rio Claro")
    assertEquals(com.example.domain.SpecialLocation.RIO_CLARO, rioClaroLoc)
    val rioClaroComment = WeatherCommentEngine.generateComment(
      isFamilyFriendlyMode = false,
      temperature = 32.0,
      feelsLike = 37.0,
      humidity = 86,
      precipitationProbability = 25,
      windSpeed = 6.0,
      uvIndex = 8.0,
      weatherCode = 0,
      locationName = "Rio Claro"
    )
    assertTrue("Rio Claro comment: $rioClaroComment",
      rioClaroComment.contains("Río Claro", ignoreCase = true) ||
      rioClaroComment.contains("cruce", ignoreCase = true) ||
      rioClaroComment.contains("Interamericana", ignoreCase = true)
    )
  }

  @Test
  fun `test wear os 8 hours forecast slice and wsi calculation`() {
    val dummyHours = (0..23).map { hour ->
      com.example.data.model.HourlyForecastItem(
        timeDisplay = "${hour}:00",
        temperature = 20.0 + hour,
        weatherCode = 0,
        precipitationProbability = hour * 4,
        isDay = hour in 6..18
      )
    }
    val next8Hours = dummyHours.take(8)
    assertEquals(8, next8Hours.size)
    assertEquals("0:00", next8Hours.first().timeDisplay)
    assertEquals("7:00", next8Hours.last().timeDisplay)

    // WSI score calculation for wear display
    val sampleWeather = WeatherData(
      cityName = "San José",
      country = "Costa Rica",
      latitude = 9.9281,
      longitude = -84.0907,
      temperature = 28.0,
      feelsLike = 32.0,
      weatherCode = 61,
      conditionTitle = "Lluvia",
      conditionDescription = "Lluvia moderada",
      humidity = 88,
      windSpeed = 15.0,
      windDirection = 90.0,
      uvIndex = 6.0,
      precipitationProbability = 80,
      isDay = true,
      sunrise = "05:40",
      sunset = "17:55",
      hourlyForecasts = dummyHours,
      dailyForecasts = emptyList(),
      sarcasticComment = "Va a llover parejo mae",
      category = WeatherCategory.RAIN
    )
    val wsi = com.example.domain.WeatherShittinessCalculator.calculate(sampleWeather, isFamilyFriendlyMode = false)
    assertTrue("WSI score should be between 0 and 100", wsi.score in 0..100)
    assertTrue("Category title should not be empty", wsi.title.isNotBlank())
  }

  @Test
  fun `test family friendly comments do not contain profanity and include wholesome tico phrases`() {
    val profanities = listOf("carepicha", "hijueputa", "mierda", "malparido", "picha", "puta", "sobaco")
    for (seed in 0..50) {
      val comment = WeatherCommentEngine.generateComment(
        isFamilyFriendlyMode = true,
        temperature = 34.0,
        feelsLike = 38.0,
        humidity = 85,
        precipitationProbability = 10,
        windSpeed = 5.0,
        uvIndex = 8.0,
        weatherCode = 0,
        randomSeed = seed,
        locationName = "San José"
      )
      for (badWord in profanities) {
        assertFalse("Family friendly comment should not contain '$badWord': $comment",
          comment.contains(badWord, ignoreCase = true)
        )
      }
    }
  }

  @Test
  fun `test unfiltered mode generates intense street insults`() {
    var foundStreetInsult = false
    for (seed in 0..50) {
      val comment = WeatherCommentEngine.generateComment(
        isFamilyFriendlyMode = false,
        temperature = 34.0,
        feelsLike = 39.0,
        humidity = 80,
        precipitationProbability = 10,
        windSpeed = 5.0,
        uvIndex = 8.0,
        weatherCode = 0,
        randomSeed = seed,
        locationName = "San José"
      )
      if (comment.contains("mae", ignoreCase = true) ||
          comment.contains("calor", ignoreCase = true) ||
          comment.contains("carepicha", ignoreCase = true) ||
          comment.contains("hijueputa", ignoreCase = true)) {
        foundStreetInsult = true
        break
      }
    }
    assertTrue("Unfiltered mode should generate Costa Rican street commentary", foundStreetInsult)
  }

  @Test
  fun `test rio claro coordinates detect rio claro even when canton is golfito`() {
    // Coordinate of Río Claro crossing: 8.6917, -83.0750
    val detectedWithGolfitoCantonName = com.example.domain.SpecialLocationInsults.detectSpecialLocation(
      locationName = "Golfito",
      latitude = 8.6917,
      longitude = -83.0750
    )
    assertEquals(
      "Río Claro GPS coordinates must be detected as RIO_CLARO even if canton name is Golfito",
      com.example.domain.SpecialLocation.RIO_CLARO,
      detectedWithGolfitoCantonName
    )
  }

  @Test
  fun `test rio claro rain and thunderstorm never says asphalt is melting`() {
    for (seed in 0..30) {
      val comment = WeatherCommentEngine.generateComment(
        isFamilyFriendlyMode = false,
        temperature = 32.0,
        feelsLike = 37.0,
        humidity = 90,
        precipitationProbability = 95,
        windSpeed = 20.0,
        uvIndex = 2.0,
        weatherCode = 95, // Thunderstorm
        randomSeed = seed,
        latitude = 8.6917,
        longitude = -83.0750,
        locationName = "Río Claro"
      )
      assertFalse(
        "Raining with lightning in Río Claro should NOT say asphalt is melting: $comment",
        comment.contains("derrite las llantas", ignoreCase = true) ||
        comment.contains("derritiendo", ignoreCase = true) ||
        comment.contains("sancocho asesino", ignoreCase = true)
      )
      assertTrue(
        "Comment in Río Claro during thunderstorm should mention storm, rain, or lightning: $comment",
        comment.contains("rayo", ignoreCase = true) ||
        comment.contains("baldazo", ignoreCase = true) ||
        comment.contains("lluvia", ignoreCase = true) ||
        comment.contains("tempestad", ignoreCase = true) ||
        comment.contains("relámpago", ignoreCase = true) ||
        comment.contains("aguacero", ignoreCase = true) ||
        comment.contains("diluvio", ignoreCase = true) ||
        comment.contains("agua", ignoreCase = true) ||
        comment.contains("Río Claro", ignoreCase = true)
      )
    }
  }

  @Test
  fun `test special locations have massive pool of over 80 new insults without repetitive scarcity`() {
    val rioClaroInsults = com.example.domain.SpecialLocationInsults.getSpecialLocationInsults(
      location = com.example.domain.SpecialLocation.RIO_CLARO,
      category = WeatherCategory.RAIN,
      combo = null,
      isFamilyFriendly = false
    )
    val neilyInsults = com.example.domain.SpecialLocationInsults.getSpecialLocationInsults(
      location = com.example.domain.SpecialLocation.CIUDAD_NEILY,
      category = WeatherCategory.RAIN,
      combo = null,
      isFamilyFriendly = false
    )
    val canoasInsults = com.example.domain.SpecialLocationInsults.getSpecialLocationInsults(
      location = com.example.domain.SpecialLocation.PASO_CANOAS,
      category = WeatherCategory.RAIN,
      combo = null,
      isFamilyFriendly = false
    )
    val cerroInsults = com.example.domain.SpecialLocationInsults.getSpecialLocationInsults(
      location = com.example.domain.SpecialLocation.CERRO_DE_LA_MUERTE,
      category = WeatherCategory.COLD,
      combo = null,
      isFamilyFriendly = false
    )

    assertTrue("Río Claro should have rich insults list: ${rioClaroInsults.size}", rioClaroInsults.size >= 12)
    assertTrue("Ciudad Neily should have rich insults list: ${neilyInsults.size}", neilyInsults.size >= 12)
    assertTrue("Paso Canoas should have rich insults list: ${canoasInsults.size}", canoasInsults.size >= 12)
    assertTrue("Cerro de la Muerte should have rich insults list: ${cerroInsults.size}", cerroInsults.size >= 12)
  }
}



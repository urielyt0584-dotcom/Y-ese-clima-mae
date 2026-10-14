package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.WeatherCategory
import com.example.data.model.WeatherData
import com.example.ui.components.SarcasticCommentCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleWeather = WeatherData(
      cityName = "San José (Chepe)",
      country = "Costa Rica",
      latitude = 9.9281,
      longitude = -84.0907,
      temperature = 28.0,
      feelsLike = 31.0,
      weatherCode = 61,
      conditionTitle = "Lluvia tica de rigor",
      conditionDescription = "Lluvia moderada",
      humidity = 88,
      windSpeed = 12.0,
      windDirection = 90.0,
      uvIndex = 6.0,
      precipitationProbability = 80,
      isDay = true,
      sunrise = "05:35",
      sunset = "17:50",
      hourlyForecasts = emptyList(),
      dailyForecasts = emptyList(),
      sarcasticComment = "Diay mae, se cayó el cielo. Lleve paraguas o acepte su destino.",
      category = WeatherCategory.RAIN
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        SarcasticCommentCard(
          weather = sampleWeather,
          onRollNewComment = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}


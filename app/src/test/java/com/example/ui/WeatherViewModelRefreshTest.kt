package com.example.ui

import android.app.Application
import com.example.data.model.CityLocation
import com.example.data.model.WeatherCategory
import com.example.data.model.WeatherData
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelRefreshTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: WeatherViewModel
    private lateinit var fakeRepository: FakeWeatherRepository
    
    private var simulatedTimeMs = 1000L

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeWeatherRepository()
        viewModel = WeatherViewModel(ApplicationProvider.getApplicationContext(), fakeRepository)
        viewModel.currentTimeProvider = { simulatedTimeMs }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `autoRefresh skipped if under 30 minutes`() = runTest {
        // Initial fetch finishes
        advanceUntilIdle()
        fakeRepository.fetchCount = 0

        simulatedTimeMs = 1000L
        viewModel.autoRefreshWeather()
        advanceUntilIdle()
        assertEquals(0, fakeRepository.fetchCount)

        // Advance 25 minutes
        simulatedTimeMs += 25 * 60 * 1000L
        viewModel.autoRefreshWeather()
        advanceUntilIdle()
        assertEquals(0, fakeRepository.fetchCount)

        // Advance 6 more minutes (total 31 minutes since init)
        simulatedTimeMs += 6 * 60 * 1000L
        viewModel.autoRefreshWeather()
        advanceUntilIdle()
        assertEquals(1, fakeRepository.fetchCount)
    }

    @Test
    fun `manual refresh respects rate limit to protect API quotas`() = runTest {
        advanceUntilIdle()
        fakeRepository.fetchCount = 0
        
        // Refreshing immediately within rate limit (1 sec later) updates sarcasm locally without network call
        simulatedTimeMs += 1000L
        viewModel.refreshWeather()
        advanceUntilIdle()
        assertEquals(0, fakeRepository.fetchCount)
        assertEquals(2, (viewModel.uiState.value as com.example.ui.WeatherUiState.Success).checkCount)

        // Refreshing after rate limit (2 minutes later) triggers a real network fetch
        simulatedTimeMs += 2 * 60 * 1000L
        viewModel.refreshWeather()
        advanceUntilIdle()
        assertEquals(1, fakeRepository.fetchCount)
        assertEquals(3, (viewModel.uiState.value as com.example.ui.WeatherUiState.Success).checkCount)
    }

    @Test
    fun `concurrent requests are blocked by mutex`() = runTest {
        advanceUntilIdle()
        fakeRepository.fetchCount = 0
        fakeRepository.delayMs = 2000L
        
        // Advance time to allow auto refresh
        simulatedTimeMs += 31 * 60 * 1000L

        // Trigger two simultaneous auto refreshes
        viewModel.autoRefreshWeather()
        viewModel.autoRefreshWeather()
        
        advanceUntilIdle()
        
        // Only one should have actually fetched because of the mutex tryLock
        assertEquals(1, fakeRepository.fetchCount)
    }

    @Test
    fun `failed request does not update timestamp`() = runTest {
        advanceUntilIdle()
        fakeRepository.fetchCount = 0
        
        simulatedTimeMs += 31 * 60 * 1000L
        val originalTime = simulatedTimeMs
        
        // Make it fail
        fakeRepository.shouldFail = true
        viewModel.autoRefreshWeather()
        advanceUntilIdle()
        
        assertEquals(1, fakeRepository.fetchCount)
        
        // Next auto refresh should NOT be skipped since it didn't update the successful timestamp
        simulatedTimeMs += 61000L // Wait 61 secs to bypass backoff
        fakeRepository.shouldFail = false
        viewModel.autoRefreshWeather()
        advanceUntilIdle()
        
        assertEquals(2, fakeRepository.fetchCount)
    }
}

class FakeWeatherRepository : WeatherRepository() {
    var fetchCount = 0
    var shouldFail = false
    var delayMs = 0L

    override suspend fun getLatestCachedWeather(): WeatherData? {
        return null
    }

    override suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        locationName: String,
        country: String,
        checkCount: Int,
        forceRandomSeed: Int?,
        forceRefresh: Boolean
    ): Result<WeatherData> {
        delay(delayMs)
        fetchCount++
        if (shouldFail) {
            return Result.failure(Exception("Fake failure"))
        }
        return Result.success(
            WeatherData(
                cityName = locationName,
                country = "CR",
                latitude = latitude,
                longitude = longitude,
                temperature = 25.0,
                feelsLike = 26.0,
                weatherCode = 0,
                conditionTitle = "Soleado",
                conditionDescription = "Clear sky",
                humidity = 50,
                windSpeed = 10.0,
                windDirection = 0.0,
                uvIndex = 5.0,
                precipitationProbability = 0,
                isDay = true,
                sunrise = "06:00",
                sunset = "18:00",
                hourlyForecasts = emptyList(),
                dailyForecasts = emptyList(),
                sarcasticComment = "Fake comment",
                category = WeatherCategory.CLOUDS,
                isFromCache = false,
                cachedTimestamp = System.currentTimeMillis()
            )
        )
    }
}

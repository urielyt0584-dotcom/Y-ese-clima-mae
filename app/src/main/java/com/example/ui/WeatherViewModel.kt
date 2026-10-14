package com.example.ui

import androidx.annotation.VisibleForTesting
import androidx.glance.appwidget.updateAll
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CityLocation
import com.example.data.model.WeatherCategory
import com.example.data.model.WeatherData
import com.example.data.repository.WeatherRepository
import com.example.domain.WeatherCommentEngine
import com.example.domain.WeatherForeignInsults
import com.example.domain.WeatherShittinessCalculator
import com.example.domain.WeatherShittinessResult
import com.example.util.BarometerPrediction
import com.example.util.BarometerRainPredictor
import com.example.util.LocationHelper
import com.example.util.NotificationHelper
import com.example.util.NotificationStatus
import com.example.util.SettingsManager
import com.example.util.withFamilyFriendlyMode
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.util.Log
import kotlin.random.Random

sealed interface WeatherUiState {
    data class Loading(val message: String = "Cargando el clima, mae... paciencia.") : WeatherUiState
    data class Success(
        val weather: WeatherData,
        val checkCount: Int = 1,
        val isRefreshing: Boolean = false,
        val notificationsEnabled: Boolean = true,
        val familyFriendlyEnabled: Boolean = true,
        val searchResults: List<CityLocation> = emptyList(),
        val isSearching: Boolean = false,
        val searchQuery: String = "",
        val shittinessResult: WeatherShittinessResult,
        val sanPedroBlameCount: Int = 0,
        val barometerAltitudeFilterEnabled: Boolean = true
    ) : WeatherUiState
    data class Error(
        val ticoMessage: String,
        val technicalDetails: String
    ) : WeatherUiState
}

data class InAppAlertData(
    val title: String,
    val message: String,
    val category: WeatherCategory? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class WeatherViewModel(
    application: Application,
    private val repository: WeatherRepository
) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager(application)

    constructor(application: Application) : this(
        application = application,
        repository = WeatherRepository(
            weatherDao = AppDatabase.getDatabase(application).weatherDao()
        )
    )

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
                return WeatherViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        var isAppInForeground = false
        const val BAROMETER_CHECK_INTERVAL_MS = 20 * 60 * 1000L // 20 minutos
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("WeatherViewModel", "Excepción no controlada capturada en corrutina", throwable)
        val current = _uiState.value
        if (current !is WeatherUiState.Success) {
            _uiState.value = WeatherUiState.Error(
                ticoMessage = if (settingsManager.isFamilyFriendlyMode) "Ocurrió un problema de conexión. Intente de nuevo." else "Se despichó algo en la conexión, mae. Toque refrescar.",
                technicalDetails = throwable.localizedMessage ?: "Error de corrutina"
            )
        }
    }

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _inAppAlertBanner = MutableStateFlow<InAppAlertData?>(null)
    val inAppAlertBanner: StateFlow<InAppAlertData?> = _inAppAlertBanner.asStateFlow()

    fun dismissInAppAlertBanner() {
        _inAppAlertBanner.value = null
    }

    fun triggerInAppAlertBanner(title: String, message: String, category: WeatherCategory? = null) {
        val alert = InAppAlertData(title = title, message = message, category = category)
        _inAppAlertBanner.value = alert
        viewModelScope.launch {
            delay(6000L)
            if (_inAppAlertBanner.value?.timestamp == alert.timestamp) {
                _inAppAlertBanner.value = null
            }
        }
    }

    private var lastSuccessfulNetworkFetch = 0L
    private var lastNetworkAttempt = 0L
    private val refreshMutex = Mutex()
    private val MINIMUM_REFRESH_INTERVAL_MS = 30 * 60 * 1000L

    @VisibleForTesting
    var currentTimeProvider: () -> Long = { System.currentTimeMillis() }

    // Tracking session inspection count
    private var checkCount = 0

    // Selected location coordinates
    private var currentLat: Double = 9.9281
    private var currentLon: Double = -84.0907
    private var currentLocationName: String = "San José (Chepe)"
    private var currentCountry: String = "Costa Rica"

    private var lastBlameTimestamp = 0L

    val presetCities = repository.defaultTicoCities

    val barometerPredictor: BarometerRainPredictor by lazy {
        BarometerRainPredictor(getApplication())
    }

    val barometerPredictionFlow: StateFlow<BarometerPrediction> by lazy {
        barometerPredictor.predictionState
    }

    init {

        // First check Room database for cached weather data so user sees content instantly even offline
        viewModelScope.launch(coroutineExceptionHandler) {
            val cached = repository.getLatestCachedWeather()
            if (cached != null) {
                currentLat = cached.latitude
                currentLon = cached.longitude
                currentLocationName = cached.cityName
                currentCountry = cached.country
                val currentFamilyFriendly = settingsManager.isFamilyFriendlyMode
                val currentNotificationsEnabled = settingsManager.isNotificationsEnabled
                val dataWithFamilyFriendly = cached.withFamilyFriendlyMode(currentFamilyFriendly, 1)
                val shittinessResult = WeatherShittinessCalculator.calculate(weatherData = dataWithFamilyFriendly, checkCount = 1, isFamilyFriendlyMode = currentFamilyFriendly)
                
                _uiState.value = WeatherUiState.Success(
                    weather = dataWithFamilyFriendly,
                    checkCount = 1,
                    isRefreshing = false,
                    notificationsEnabled = currentNotificationsEnabled,
                    familyFriendlyEnabled = currentFamilyFriendly,
                    shittinessResult = shittinessResult,
                    sanPedroBlameCount = settingsManager.sanPedroBlameCount,
                    barometerAltitudeFilterEnabled = settingsManager.isBarometerAltitudeFilterEnabled
                )
                
                // Initialize network fetch tracker with cache timestamp so we don't fetch on resume
                lastSuccessfulNetworkFetch = cached.cachedTimestamp
                
                // Only fetch fresh weather on startup if the cache is older than 30 minutes
                if (System.currentTimeMillis() - cached.cachedTimestamp > 30 * 60 * 1000L) {
                    loadWeather(currentLat, currentLon, currentLocationName, currentCountry)
                }
            } else {
                // No cache found, fetch fresh weather
                loadWeather(currentLat, currentLon, currentLocationName, currentCountry)
            }
        }

        // Measure barometer pressure on app launch
        measureBarometerPressure()
    }

    fun loadWeather(
        lat: Double,
        lon: Double,
        name: String,
        country: String = "Costa Rica",
        isUserRefresh: Boolean = false,
        isAutoRefresh: Boolean = false
    ) {
        val safeLat = if (lat.isFinite() && lat in -90.0..90.0) lat else 9.9281
        val safeLon = if (lon.isFinite() && lon in -180.0..180.0) lon else -84.0907
        val safeName = name.trim().take(100).ifBlank { "San José (Chepe)" }
        val rawCountry = country.trim().take(100)
        val safeCountry = if (rawCountry.isNotBlank() && !rawCountry.equals("Costa Rica", true) && !rawCountry.equals("CR", true)) {
            rawCountry
        } else if (!WeatherForeignInsults.isInsideCostaRica(safeLat, safeLon)) {
            "Extranjero"
        } else {
            "Costa Rica"
        }

        viewModelScope.launch(coroutineExceptionHandler) {
            val acquired = if (isAutoRefresh) {
                refreshMutex.tryLock()
            } else {
                refreshMutex.lock()
                true
            }
            
            if (!acquired) {
                Log.d("WeatherRefresh", "Refresh skipped: request already running")
                return@launch
            }
            try {
                if (isAutoRefresh) {
                    val now = currentTimeProvider()
                    if (now - lastSuccessfulNetworkFetch < MINIMUM_REFRESH_INTERVAL_MS) {
                        Log.d("WeatherRefresh", "Refresh skipped: minimum interval not reached")
                        return@launch
                    }
                    if (now - lastNetworkAttempt < 60_000L) { // 1 minute backoff on failure
                        Log.d("WeatherRefresh", "Refresh skipped: backing off after recent attempt")
                        return@launch
                    }
                }

                if (isUserRefresh) {
                    val now = currentTimeProvider()
                    val currentSuccess = _uiState.value as? WeatherUiState.Success
                    // If network was fetched less than 2 minutes ago, refresh sarcasm locally from memory without burning API calls
                    if (currentSuccess != null && (now - lastSuccessfulNetworkFetch < 120_000L)) {
                        checkCount++
                        val currentFamilyFriendly = settingsManager.isFamilyFriendlyMode
                        val updatedWeather = currentSuccess.weather.withFamilyFriendlyMode(currentFamilyFriendly, checkCount)
                        val shittinessResult = WeatherShittinessCalculator.calculate(
                            weatherData = updatedWeather,
                            checkCount = checkCount,
                            isFamilyFriendlyMode = currentFamilyFriendly
                        )
                        _uiState.value = currentSuccess.copy(
                            weather = updatedWeather,
                            checkCount = checkCount,
                            isRefreshing = false,
                            shittinessResult = shittinessResult
                        )
                        Log.d("WeatherRefresh", "Refresh served from memory: checkCount $checkCount")
                        return@launch
                    }
                }

                Log.d("WeatherRefresh", "Refresh triggered")

                if (isUserRefresh) {
                    checkCount++
                } else if (!isAutoRefresh) {
                    checkCount = 1
                }

                currentLat = safeLat
                currentLon = safeLon
                currentLocationName = safeName
                currentCountry = safeCountry

                val currentNotificationsEnabled = settingsManager.isNotificationsEnabled
                val currentFamilyFriendly = settingsManager.isFamilyFriendlyMode

                if (!isUserRefresh && !isAutoRefresh) {
                    _uiState.value = WeatherUiState.Loading(if (settingsManager.isFamilyFriendlyMode) "Averiguando el clima de $safeName..." else "Averiguando el clima de $safeName, mae...")
                } else {
                    val currentSuccess = _uiState.value as? WeatherUiState.Success
                    if (currentSuccess != null) {
                        _uiState.value = currentSuccess.copy(isRefreshing = true, checkCount = checkCount)
                    }
                }

                lastNetworkAttempt = currentTimeProvider()
                val result = repository.getWeatherData(
                    latitude = safeLat,
                    longitude = safeLon,
                    locationName = safeName,
                    country = safeCountry,
                    checkCount = checkCount,
                    forceRefresh = isUserRefresh
                )

                result.fold(
                    onSuccess = { data ->
                        if (!data.isFromCache) {
                            lastSuccessfulNetworkFetch = currentTimeProvider()
                            Log.d("WeatherRefresh", "Refresh succeeded")
                        } else {
                            Log.d("WeatherRefresh", "Refresh failed: loaded from cache")
                        }
                        
                        val dataWithFamilyFriendly = data.withFamilyFriendlyMode(currentFamilyFriendly, checkCount)
                        val shittinessResult = WeatherShittinessCalculator.calculate(weatherData = dataWithFamilyFriendly, checkCount = checkCount, isFamilyFriendlyMode = currentFamilyFriendly)
                        
                        _uiState.value = WeatherUiState.Success(
                            weather = dataWithFamilyFriendly,
                            checkCount = checkCount,
                            isRefreshing = false,
                            notificationsEnabled = currentNotificationsEnabled,
                            familyFriendlyEnabled = currentFamilyFriendly,
                            shittinessResult = shittinessResult,
                            sanPedroBlameCount = settingsManager.sanPedroBlameCount,
                            barometerAltitudeFilterEnabled = settingsManager.isBarometerAltitudeFilterEnabled
                        )
                        
                        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                            try {
                                com.example.widget.ClothingWidget().updateAll(getApplication<Application>())
                            } catch (e: Throwable) {
                                Log.e("WeatherViewModel", "Failed to update widget", e)
                            }
                        }
                    },
                    onFailure = { error ->
                        Log.d("WeatherRefresh", "Refresh failed")
                        _uiState.value = WeatherUiState.Error(
                            ticoMessage = if (settingsManager.isFamilyFriendlyMode) "No se pudo cargar el clima. Verifique su conexión a internet." else "No se pudo cargar el clima, mae. Revise el internet porque la app no es adivina.",
                            technicalDetails = error.localizedMessage ?: "Error de red"
                        )
                    }
                )
            } finally {
                refreshMutex.unlock()
            }
        }
    }

    fun autoRefreshWeather() {
        loadWeather(currentLat, currentLon, currentLocationName, country = currentCountry, isUserRefresh = false, isAutoRefresh = true)
    }

    fun refreshWeather() {
        if (!refreshMutex.isLocked) {
            loadWeather(currentLat, currentLon, currentLocationName, country = currentCountry, isUserRefresh = true)
        } else {
            Log.d("WeatherRefresh", "Refresh skipped: request already running")
        }
    }

    fun rollNewComment() {
        val currentState = _uiState.value as? WeatherUiState.Success ?: return
        val weather = currentState.weather

        val isFamilyFriendly = settingsManager.isFamilyFriendlyMode
        val newComment = WeatherCommentEngine.generateComment(
            isFamilyFriendlyMode = isFamilyFriendly,
            temperature = weather.temperature,
            feelsLike = weather.feelsLike,
            humidity = weather.humidity,
            precipitationProbability = weather.precipitationProbability,
            windSpeed = weather.windSpeed,
            uvIndex = weather.uvIndex,
            weatherCode = weather.weatherCode,
            isDay = weather.isDay,
            checkCount = 1,
            excludeComment = weather.sarcasticComment,
            country = weather.country,
            latitude = weather.latitude,
            longitude = weather.longitude,
            locationName = weather.cityName
        )

        val newShittiness = WeatherShittinessCalculator.calculate(
            weatherData = weather,
            checkCount = currentState.checkCount + Random.nextInt(1, 50),
            isFamilyFriendlyMode = settingsManager.isFamilyFriendlyMode
        )

        _uiState.value = currentState.copy(
            weather = weather.copy(sarcasticComment = newComment),
            shittinessResult = newShittiness
        )
    }

    private var searchJob: Job? = null

    fun searchCity(query: String) {
        val currentState = _uiState.value as? WeatherUiState.Success ?: return
        val sanitizedQuery = query.take(80)
        _uiState.value = currentState.copy(searchQuery = sanitizedQuery, isSearching = true)

        searchJob?.cancel()
        searchJob = viewModelScope.launch(coroutineExceptionHandler) {
            delay(350)
            
            if (sanitizedQuery.isBlank()) {
                val stateNow = _uiState.value as? WeatherUiState.Success ?: return@launch
                _uiState.value = stateNow.copy(
                    searchQuery = "",
                    searchResults = presetCities,
                    isSearching = false
                )
                return@launch
            }

            val result = repository.searchCity(sanitizedQuery)
            val currentStateNow = _uiState.value as? WeatherUiState.Success ?: return@launch
            result.fold(
                onSuccess = { list ->
                    _uiState.value = currentStateNow.copy(
                        searchResults = list,
                        isSearching = false
                    )
                },
                onFailure = {
                    _uiState.value = currentStateNow.copy(
                        searchResults = emptyList(),
                        isSearching = false
                    )
                }
            )
        }
    }

    fun clearSearch() {
        val currentState = _uiState.value as? WeatherUiState.Success ?: return
        _uiState.value = currentState.copy(searchQuery = "", searchResults = emptyList(), isSearching = false)
    }

    fun toggleNotifications(enabled: Boolean) {
        settingsManager.isNotificationsEnabled = enabled
        val currentState = _uiState.value as? WeatherUiState.Success ?: return
        _uiState.value = currentState.copy(notificationsEnabled = enabled)
    }

    fun toggleBarometerAltitudeFilter(enabled: Boolean) {
        settingsManager.isBarometerAltitudeFilterEnabled = enabled
        val currentState = _uiState.value as? WeatherUiState.Success ?: return
        _uiState.value = currentState.copy(barometerAltitudeFilterEnabled = enabled)
    }

    fun blameSanPedro() {
        val newCount = settingsManager.sanPedroBlameCount + 1
        settingsManager.sanPedroBlameCount = newCount
        val currentState = _uiState.value as? WeatherUiState.Success ?: return
        _uiState.value = currentState.copy(sanPedroBlameCount = newCount)

        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            try {
                val firebaseRepo = com.example.data.repository.FirebaseRepository.create(getApplication())
                val user = firebaseRepo.getCurrentUser()
                if (user != null) {
                    firebaseRepo.syncUserProfile(
                        displayName = user.displayName ?: "Mae",
                        favoriteCantons = listOf(currentLocationName),
                        sanPedroBlamesCount = newCount
                    )
                }
            } catch (e: Throwable) {
                Log.d("WeatherViewModel", "Firebase sync skipped: ${e.message}")
            }
        }
    }

    fun toggleFamilyFriendly(enabled: Boolean) {
        settingsManager.isFamilyFriendlyMode = enabled
        val currentState = _uiState.value as? WeatherUiState.Success ?: return
        
        val updatedWeather = currentState.weather.withFamilyFriendlyMode(enabled, currentState.checkCount)
        val shittinessResult = WeatherShittinessCalculator.calculate(weatherData = updatedWeather, checkCount = currentState.checkCount, isFamilyFriendlyMode = enabled)
        
        _uiState.value = currentState.copy(
            familyFriendlyEnabled = enabled,
            weather = updatedWeather,
            shittinessResult = shittinessResult
        )
        
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            try {
                com.example.widget.ClothingWidget().updateAll(getApplication<Application>())
            } catch (e: Throwable) {
                Log.e("WeatherViewModel", "Failed to update widget", e)
            }
        }
    }

    fun sendTestNotification(context: Context): NotificationStatus {
        val currentState = _uiState.value as? WeatherUiState.Success
        val category = currentState?.weather?.category
        val currentFamilyFriendly = settingsManager.isFamilyFriendlyMode
        val status = NotificationHelper.sendTicoWeatherAlert(
            context = context,
            category = category,
            isFamilyFriendlyMode = currentFamilyFriendly
        )
        if (status == NotificationStatus.SUCCESS) {
            val (title, msg) = NotificationHelper.getAlertTexts(category, currentFamilyFriendly)
            triggerInAppAlertBanner(title, msg, category)
        }
        return status
    }

    fun detectCurrentLocation(context: Context) {
        val currentState = _uiState.value as? WeatherUiState.Success
        
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                if (currentState == null) {
                    _uiState.value = WeatherUiState.Loading("Buscando dónde diablos está usted, mae...")
                }
                
                val location = LocationHelper.getCurrentLocation(context)
                if (location != null && LocationHelper.isValidCoordinate(location.latitude, location.longitude)) {
                    if (location.hasAltitude()) {
                        barometerPredictor.updateGpsAltitude(location.altitude.toFloat())
                    }
                    if (currentState != null) {
                        _uiState.value = WeatherUiState.Loading("Buscando dónde diablos está usted, mae...")
                    }
                    val cityLocation = LocationHelper.getCityLocationDetails(
                        context,
                        location.latitude,
                        location.longitude
                    )
                    loadWeather(location.latitude, location.longitude, cityLocation.name, cityLocation.country)
                } else {
                    // Fallback to San José
                    if (currentState != null && currentState.weather.cityName == currentLocationName) {
                        // We already have the fallback location loaded! Just restore state.
                        _uiState.value = currentState
                    } else {
                        if (currentState != null) {
                            _uiState.value = WeatherUiState.Loading("Buscando dónde diablos está usted, mae...")
                        }
                        loadWeather(currentLat, currentLon, currentLocationName, currentCountry)
                    }
                }
            } catch (t: Throwable) {
                Log.w("WeatherViewModel", "Error in detectCurrentLocation", t)
                loadWeather(currentLat, currentLon, currentLocationName, currentCountry)
            }
        }
    }

    fun getBarometerPrediction(): BarometerPrediction {
        return barometerPredictor.getLatestPrediction(settingsManager.isFamilyFriendlyMode)
    }

    fun measureBarometerPressure() {
        barometerPredictor.measureCurrentPressure()
    }

    private var periodicBarometerJob: Job? = null

    fun startForegroundBarometerCheck() {
        periodicBarometerJob?.cancel()
        periodicBarometerJob = viewModelScope.launch(coroutineExceptionHandler) {
            measureBarometerPressure()
            while (isAppInForeground) {
                delay(BAROMETER_CHECK_INTERVAL_MS)
                if (isAppInForeground) {
                    Log.d("BarometerPredictor", "Periodic 20-min barometer check triggered")
                    measureBarometerPressure()
                }
            }
        }
    }

    fun stopForegroundBarometerCheck() {
        periodicBarometerJob?.cancel()
        periodicBarometerJob = null
    }

    fun simulateBarometerRainAlert(context: Context): BarometerPrediction {
        val prediction = barometerPredictor.simulatePressureDropForTest(
            context = context,
            dropHpa = 2.4f,
            isFamilyFriendly = settingsManager.isFamilyFriendlyMode
        )
        triggerInAppAlertBanner(prediction.title, prediction.description, WeatherCategory.THUNDERSTORM)
        return prediction
    }
}

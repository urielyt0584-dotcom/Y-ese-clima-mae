package com.example.ui

import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.WeatherCategory
import com.example.ui.components.CitySearchBottomSheet
import com.example.ui.components.CurrentWeatherCard
import com.example.ui.components.DailyForecastCard
import com.example.ui.components.HourlyForecastCard
import com.example.ui.components.SettingsDialog
import com.example.ui.components.InAppHeadsUpBanner
import com.example.ui.components.QuickCityChipsRow
import com.example.ui.components.SarcasticCommentCard
import com.example.ui.components.WeatherDynamicBackground
import com.example.ui.components.WeatherShittinessCard
import com.example.ui.wear.WearWeatherScreen
import com.example.util.LocationHelper
import com.example.util.NotificationHelper
import com.example.util.NotificationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val inAppAlert by viewModel.inAppAlertBanner.collectAsStateWithLifecycle()

    var showSearchSheet by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showWatchPreview by remember { mutableStateOf(false) }

    BackHandler(enabled = showWatchPreview) {
        showWatchPreview = false
    }

    if (showWatchPreview) {
        WearWeatherScreen(
            viewModel = viewModel,
            onExitWatchMode = { showWatchPreview = false }
        )
        return
    }

    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            viewModel.detectCurrentLocation(context)
        }
    }

    // Permission launchers
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.detectCurrentLocation(context)
        } else {
            Toast.makeText(context, "Diay mae, sin permiso no sabemos dónde está.", Toast.LENGTH_SHORT).show()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.toggleNotifications(true)
            val result = viewModel.sendTestNotification(context)
            if (result == NotificationStatus.SUCCESS) {
                Toast.makeText(context, "🔔 ¡Permiso otorgado! Alerta del Mae enviada con pop-up y luz.", Toast.LENGTH_SHORT).show()
            }
        } else {
            viewModel.toggleNotifications(false)
            Toast.makeText(context, "⚠️ Mae, sin permiso no te podemos avisar si cae un baldazo.", Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
        is WeatherUiState.Loading -> {
            WeatherDynamicBackground(
                category = WeatherCategory.CLEAR_SKIES,
                isDay = true
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFFFD54F),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        is WeatherUiState.Error -> {
            WeatherDynamicBackground(
                category = WeatherCategory.CLOUDS,
                isDay = true
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFFF7043),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.ticoMessage,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.technicalDetails,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.6f)
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.refreshWeather() },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("error_retry_button")
                            ) {
                                Text(stringResource(R.string.retry_action))
                            }
                        }
                    }
                }
            }
        }

        is WeatherUiState.Success -> {
            val weather = state.weather

            WeatherDynamicBackground(
                category = weather.category,
                isDay = weather.isDay
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0.dp),
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            ),
                            title = {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.Black.copy(alpha = 0.3f),
                                    onClick = { showSearchSheet = true },
                                    modifier = Modifier
                                        .testTag("location_header_button")
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD54F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = weather.cityName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Cambiar cantón",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            actions = {
                                // GPS Current Location button
                                IconButton(
                                    onClick = {
                                        if (LocationHelper.hasLocationPermission(context)) {
                                            viewModel.detectCurrentLocation(context)
                                        } else {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    },
                                    modifier = Modifier.testTag("gps_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "Mi Ubicación",
                                        tint = Color.White
                                    )
                                }

                                // Search city button
                                IconButton(
                                    onClick = { showSearchSheet = true },
                                    modifier = Modifier.testTag("search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Buscar cantón",
                                        tint = Color.White
                                    )
                                }

                                // Settings config button
                                FilledTonalButton(
                                    onClick = { showNotificationsDialog = true },
                                    modifier = Modifier.padding(end = 4.dp).testTag("notifications_button"),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Ajustes",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ajustes")
                                }

                                // Refresh weather button
                                FilledTonalIconButton(
                                    onClick = { viewModel.refreshWeather() },
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .testTag("refresh_weather_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refrescar clima",
                                        tint = Color.White
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item(key = "status") {
                            val statusText = when {
                                state.isRefreshing -> "Actualizando, mae..."
                                weather.isFromCache -> "Mostrando datos guardados."
                                else -> "Actualizado hace un momento."
                            }
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.8f)),
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                        }
                        // Quick Costa Rican city pills
                        item(key = "quick_chips") {
                            QuickCityChipsRow(
                                presetCities = viewModel.presetCities,
                                selectedCityName = weather.cityName,
                                onSelectCity = { city ->
                                    viewModel.loadWeather(city.latitude, city.longitude, city.name, city.country)
                                }
                            )
                        }

                        // Weather Shittiness Index (WSI) Card
                        item(key = "wsi") {
                            WeatherShittinessCard(result = state.shittinessResult)
                        }

                        // Prominent Hero Sarcastic Comment Card
                        item(key = "sarcastic") {
                            SarcasticCommentCard(
                                weather = weather,
                                onRollNewComment = { viewModel.rollNewComment() }
                            )
                        }

                        // Current Weather Stats Card
                        item(key = "current") {
                            CurrentWeatherCard(weather = weather)
                        }

                        // 24-Hour Forecast
                        if (weather.hourlyForecasts.isNotEmpty()) {
                            item(key = "hourly") {
                                HourlyForecastCard(hourlyList = weather.hourlyForecasts)
                            }
                        }

                        // 7-Day Forecast
                        if (weather.dailyForecasts.isNotEmpty()) {
                            item(key = "daily") {
                                DailyForecastCard(dailyList = weather.dailyForecasts)
                            }
                        }

                        // Footer note
                        item(key = "footer") {
                            Text(
                                text = "🇨🇷 Hecho en Costa Rica • Open-Meteo & WeatherAPI • Humor 100% tico",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                }

                // City Search Bottom Sheet
                CitySearchBottomSheet(
                    visible = showSearchSheet,
                    onDismiss = {
                        showSearchSheet = false
                        viewModel.clearSearch()
                    },
                    searchQuery = state.searchQuery,
                    onQueryChange = { query -> viewModel.searchCity(query) },
                    isSearching = state.isSearching,
                    searchResults = if (state.searchResults.isNotEmpty()) state.searchResults else viewModel.presetCities,
                    onSelectCity = { city ->
                        viewModel.loadWeather(city.latitude, city.longitude, city.name, city.country)
                    },
                    onRequestCurrentLocation = {
                        if (LocationHelper.hasLocationPermission(context)) {
                            viewModel.detectCurrentLocation(context)
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                )

                // Notifications Dialog
                SettingsDialog(
                    visible = showNotificationsDialog,
                    onDismiss = { showNotificationsDialog = false },
                    notificationsEnabled = state.notificationsEnabled,
                    onToggleNotifications = { enabled ->
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificationHelper.hasNotificationPermission(context)) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else if (!NotificationHelper.areSystemNotificationsEnabled(context)) {
                                Toast.makeText(context, "⚠️ Las notificaciones están desactivadas para la app en los ajustes del cel.", Toast.LENGTH_LONG).show()
                                viewModel.toggleNotifications(true)
                            } else {
                                viewModel.toggleNotifications(true)
                                Toast.makeText(context, "✅ Alertas del Mae activadas", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            viewModel.toggleNotifications(false)
                        }
                    },
                    familyFriendlyEnabled = state.familyFriendlyEnabled,
                    onToggleFamilyFriendly = { enabled ->
                        viewModel.toggleFamilyFriendly(enabled)
                    },
                    onSendTestNotification = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificationHelper.hasNotificationPermission(context)) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            val result = viewModel.sendTestNotification(context)
                            when (result) {
                                NotificationStatus.SUCCESS -> {
                                    showNotificationsDialog = false
                                    Toast.makeText(context, "🔔 ¡Alerta del Mae activada! Revisa el pop-up y la luz.", Toast.LENGTH_SHORT).show()
                                }
                                NotificationStatus.NOTIFICATIONS_DISABLED_SYSTEM -> {
                                    Toast.makeText(context, "⚠️ Las notificaciones están bloqueadas en los ajustes de tu cel.", Toast.LENGTH_LONG).show()
                                }
                                NotificationStatus.PERMISSION_DENIED -> {
                                    Toast.makeText(context, "⚠️ Mae, se requiere permiso de notificaciones.", Toast.LENGTH_LONG).show()
                                }
                                NotificationStatus.ERROR -> {
                                    Toast.makeText(context, "⚠️ Diay mae, falló el envío de la notificación.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    sanPedroBlameCount = state.sanPedroBlameCount,
                    onBlameSanPedro = {
                        viewModel.blameSanPedro()
                    },
                    currentWeatherCategory = state.weather.category,
                    currentWeather = state.weather,
                    onOpenWatchPreview = { showWatchPreview = true },
                    barometerAltitudeFilterEnabled = state.barometerAltitudeFilterEnabled,
                    onToggleBarometerAltitudeFilter = { enabled ->
                        viewModel.toggleBarometerAltitudeFilter(enabled)
                    }
                )
            }
        }
    }

    // Heads-Up Pop-Up Banner floating over the screen with amber flash & vibration
    InAppHeadsUpBanner(
        alert = inAppAlert,
        onDismiss = { viewModel.dismissInAppAlertBanner() }
    )
}
}

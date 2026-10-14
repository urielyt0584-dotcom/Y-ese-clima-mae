package com.example.util

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BarometerRainPredictorTest {

    private lateinit var predictor: BarometerRainPredictor
    private lateinit var context: android.content.Context
    private lateinit var settingsManager: SettingsManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val prefs = context.getSharedPreferences("test_barometer_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        val appPrefs = context.getSharedPreferences("y_ese_clima_prefs", android.content.Context.MODE_PRIVATE)
        appPrefs.edit().clear().commit()

        settingsManager = SettingsManager(context)
        settingsManager.isBarometerAltitudeFilterEnabled = true
        predictor = BarometerRainPredictor(context, "test_barometer_prefs")
    }

    @Test
    fun `initial state without samples is calibrating`() {
        val prediction = predictor.getLatestPrediction(isFamilyFriendly = true)
        assertEquals(BarometerTrendType.CALIBRATING, prediction.trend)
    }

    @Test
    fun `severe pressure drop predicts storm incoming`() {
        val now = System.currentTimeMillis()
        val twoHoursAgo = now - (2 * 3600 * 1000L)

        // Baseline 2 hours ago: 1014.0 hPa
        predictor.recordSample(1014.0f, twoHoursAgo, isFamilyFriendly = true)
        // Current: 1011.0 hPa (Drop of 3.0 hPa over 2 hours -> valid rate -1.5 hPa/h)
        predictor.recordSample(1011.0f, now, isFamilyFriendly = true)

        val prediction = predictor.getLatestPrediction(isFamilyFriendly = true)
        assertEquals(BarometerTrendType.STORM_INCOMING, prediction.trend)
        assertTrue(prediction.title.contains("tormenta", ignoreCase = true) || prediction.title.contains("alerta", ignoreCase = true))

        // Check unfiltered text
        val unfilteredPrediction = predictor.getLatestPrediction(isFamilyFriendly = false)
        assertEquals(BarometerTrendType.STORM_INCOMING, unfilteredPrediction.trend)
        assertTrue(unfilteredPrediction.title.contains("DESPLOMÓ", ignoreCase = true))
    }

    @Test
    fun `moderate pressure drop predicts rain likely`() {
        val now = System.currentTimeMillis()
        val twoHoursAgo = now - (2 * 3600 * 1000L)

        // Baseline: 1012.0 hPa
        predictor.recordSample(1012.0f, twoHoursAgo, isFamilyFriendly = true)
        // Drop of 1.4 hPa over 2 hours -> valid rate -0.7 hPa/h
        predictor.recordSample(1010.6f, now, isFamilyFriendly = true)

        val prediction = predictor.getLatestPrediction(isFamilyFriendly = true)
        assertEquals(BarometerTrendType.RAIN_LIKELY, prediction.trend)
        assertTrue(prediction.description.contains("probabilidad de lluvia", ignoreCase = true))
    }

    @Test
    fun `rapid pressure drop from climbing 8m stairs triggers altitude change filter instead of storm`() {
        val now = System.currentTimeMillis()
        val twentyMinutesAgo = now - (20 * 60 * 1000L) // 20 minutos atrás

        // Ground floor baseline: 1013.0 hPa
        predictor.recordSample(1013.0f, twentyMinutesAgo, isFamilyFriendly = true)
        // Subió 8 metros de gradas: la presión cae 1.0 hPa a 1012.0 hPa en 20 min (tasa -3.0 hPa/h)
        predictor.recordSample(1012.0f, now, isFamilyFriendly = true)

        val prediction = predictor.getLatestPrediction(isFamilyFriendly = true)
        // El filtro de altitud debe clasificarlo como cambio de altitud, NO como lluvia o tormenta
        assertEquals(BarometerTrendType.ALTITUDE_CHANGE, prediction.trend)
        assertTrue(prediction.isAltitudeSpikeDetected)
        // Δh estimada debe ser aproximadamente ~8.3 metros (1 hPa * 8.33)
        assertTrue(prediction.estimatedAltitudeDeltaMeters in 7.0f..10.0f)
        assertTrue(prediction.description.contains("filtro anti-escaleras", ignoreCase = true) || prediction.title.contains("altitud", ignoreCase = true))
    }

    @Test
    fun `rapid pressure drop from elevator triggers altitude change filter and avoids false alarm`() {
        val now = System.currentTimeMillis()
        val fiveMinutesAgo = now - (5 * 60 * 1000L)

        // Baseline: 1014.0 hPa
        predictor.recordSample(1014.0f, fiveMinutesAgo, isFamilyFriendly = false)
        // Ascensor 4 pisos (~16m): caída de 2.0 hPa en 5 minutos
        predictor.recordSample(1012.0f, now, isFamilyFriendly = false)

        val prediction = predictor.getLatestPrediction(isFamilyFriendly = false)
        assertEquals(BarometerTrendType.ALTITUDE_CHANGE, prediction.trend)
        assertTrue(prediction.isAltitudeSpikeDetected)
        assertTrue(prediction.title.contains("gradas o ascensor", ignoreCase = true))
    }

    @Test
    fun `step altitude change followed by steady pressure correctly predicts steady weather`() {
        val now = System.currentTimeMillis()
        val twoHoursAgo = now - (2 * 3600 * 1000L)
        val ninetyMinutesAgo = now - (90 * 60 * 1000L)
        val thirtyMinutesAgo = now - (30 * 60 * 1000L)

        // Piso 1: 1013.0 hPa hace 2h
        predictor.recordSample(1013.0f, twoHoursAgo, isFamilyFriendly = true)
        // Subió a piso 2 hace 90 min: 1012.0 hPa
        predictor.recordSample(1012.0f, ninetyMinutesAgo, isFamilyFriendly = true)
        // Se quedó en piso 2: lecturas planas en los últimos 30 min
        predictor.recordSample(1012.0f, thirtyMinutesAgo, isFamilyFriendly = true)
        predictor.recordSample(1012.0f, now, isFamilyFriendly = true)

        val prediction = predictor.getLatestPrediction(isFamilyFriendly = true)
        // Como la presión en el nuevo piso está totalmente estabilizada, no debe predecir lluvia
        assertEquals(BarometerTrendType.STEADY, prediction.trend)
    }

    @Test
    fun `gps altitude compensation negates barometric drop when climbing stairs or hills`() {
        val now = System.currentTimeMillis()
        val twoHoursAgo = now - (2 * 3600 * 1000L)

        // Muestra inicial en Cartago: 860.0 hPa a 1400m de altitud GPS
        predictor.recordSample(860.0f, twoHoursAgo, isFamilyFriendly = true, altitudeMeters = 1400.0f)
        // Subió a 1408.3m (+8.3m): barómetro baja a 859.0 hPa (-1.0 hPa)
        predictor.recordSample(859.0f, now, isFamilyFriendly = true, altitudeMeters = 1408.33f)

        val prediction = predictor.getLatestPrediction(isFamilyFriendly = true)
        // La caída de presión está 100% compensada por el GPS (+8.3m = -1.0 hPa)
        // Por tanto la presión meteorológica es constante (STEADY) o ALTITUDE_CHANGE, no tormenta
        assertTrue(prediction.trend == BarometerTrendType.STEADY || prediction.trend == BarometerTrendType.ALTITUDE_CHANGE)
    }

    @Test
    fun `disabled altitude filter allows raw behavior when user toggles it off`() {
        settingsManager.isBarometerAltitudeFilterEnabled = false
        val predictorUnfiltered = BarometerRainPredictor(context, "test_barometer_prefs")

        val now = System.currentTimeMillis()
        val thirtyMinutesAgo = now - (30 * 60 * 1000L)

        predictorUnfiltered.recordSample(1014.0f, thirtyMinutesAgo, isFamilyFriendly = true)
        predictorUnfiltered.recordSample(1012.0f, now, isFamilyFriendly = true)

        val prediction = predictorUnfiltered.getLatestPrediction(isFamilyFriendly = true)
        assertFalse(prediction.isAltitudeFilterActive)
    }

    @Test
    fun `stable pressure predicts steady weather`() {
        val now = System.currentTimeMillis()
        val twoHoursAgo = now - (2 * 3600 * 1000L)

        predictor.recordSample(1013.0f, twoHoursAgo, isFamilyFriendly = true)
        predictor.recordSample(1012.9f, now, isFamilyFriendly = true)

        val prediction = predictor.getLatestPrediction(isFamilyFriendly = true)
        assertEquals(BarometerTrendType.STEADY, prediction.trend)
        assertTrue(prediction.title.contains("estable", ignoreCase = true))
    }

    @Test
    fun `rising pressure predicts improving weather`() {
        val now = System.currentTimeMillis()
        val twoHoursAgo = now - (2 * 3600 * 1000L)

        predictor.recordSample(1010.0f, twoHoursAgo, isFamilyFriendly = true)
        predictor.recordSample(1012.5f, now, isFamilyFriendly = true)

        val prediction = predictor.getLatestPrediction(isFamilyFriendly = true)
        assertEquals(BarometerTrendType.IMPROVING, prediction.trend)
        assertTrue(prediction.title.contains("Mejorando", ignoreCase = true) || prediction.title.contains("subiendo", ignoreCase = true))
    }

    @Test
    fun `simulation triggers alert and updates prediction`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val prediction = predictor.simulatePressureDropForTest(context, dropHpa = 2.5f, isFamilyFriendly = false)
        
        assertEquals(BarometerTrendType.STORM_INCOMING, prediction.trend)
        assertTrue(prediction.deltaHpa <= -2.0f)
    }

    @Test
    fun `notification helper sends weather alert and barometer alert successfully`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            org.robolectric.Shadows.shadowOf(app).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        
        val weatherAlertStatus = NotificationHelper.sendTicoWeatherAlert(
            context = app,
            title = "Test Alerta",
            message = "Prueba de notificación",
            isFamilyFriendlyMode = true
        )
        assertEquals(NotificationStatus.SUCCESS, weatherAlertStatus)

        val prediction = predictor.getLatestPrediction(isFamilyFriendly = true)
        val barometerAlertStatus = NotificationHelper.sendBarometerRainAlert(
            context = app,
            prediction = prediction,
            isFamilyFriendlyMode = true
        )
        assertEquals(NotificationStatus.SUCCESS, barometerAlertStatus)
    }

    @Test
    fun `last sample timestamp updates correctly`() {
        val now = System.currentTimeMillis()
        predictor.recordSample(1013.25f, now, isFamilyFriendly = true)
        assertEquals(now, predictor.getLastSampleTimestamp())
    }
}

package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.abs

enum class BarometerTrendType {
    STORM_INCOMING,   // Caída sostenida ΔP <= -1.8 hPa en >= 45 min y tasa meteorológica válida (-0.6 a -2.2 hPa/h)
    RAIN_LIKELY,      // Caída sostenida ΔP <= -0.9 hPa en >= 45 min y tasa meteorológica válida (-0.35 a -1.8 hPa/h)
    STEADY,           // -0.8 < ΔP < +0.8 hPa (presión estable)
    IMPROVING,        // Subida de presión ΔP >= +0.8 hPa (tiempo despejando)
    ALTITUDE_CHANGE,  // Caída o salto brusco (> 2.0 hPa/h o salto rápido de ~8m) atribuible a subir gradas o ascensor
    CALIBRATING       // Menos de 2 lecturas espaciadas para calcular tendencia
}

data class PressureSample(
    val timestampMs: Long,
    val pressureHpa: Float,
    val altitudeMeters: Float? = null
)

data class BarometerPrediction(
    val currentPressureHpa: Float,
    val previousPressureHpa: Float?,
    val deltaHpa: Float,
    val hoursDifference: Float,
    val trend: BarometerTrendType,
    val title: String,
    val description: String,
    val isHardwareSensorAvailable: Boolean,
    val timestampMs: Long = System.currentTimeMillis(),
    val estimatedAltitudeDeltaMeters: Float = 0f,
    val isAltitudeSpikeDetected: Boolean = false,
    val isAltitudeFilterActive: Boolean = true
)

/**
 * Predictor local de lluvia basado en la tendencia del barómetro (Sensor.TYPE_PRESSURE).
 * Funciona completamente offline en el Galaxy Watch / Wear OS (o teléfono)
 * midiendo variaciones de presión atmosférica (hPa) en intervalos de tiempo.
 *
 * Incluye un filtro anti-falsas alarmas por altitud:
 * 1 hPa de presión equivale físicamente a ~8.33 metros de altura.
 * Subir gradas o tomar un ascensor genera una caída abrupta de presión que NO es lluvia.
 * El algoritmo filtra cambios verticales rápidos (> 2.0 hPa/h o saltos en < 45 min)
 * y compensa con altitud GPS cuando está disponible.
 */
class BarometerRainPredictor(
    private val context: Context,
    private val prefsName: String = "barometer_history_prefs"
) : SensorEventListener {

    private val prefs: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val pressureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
    private val settingsManager = SettingsManager(context)

    val isHardwareSensorAvailable: Boolean = pressureSensor != null

    private val _predictionState = MutableStateFlow(getLatestPrediction())
    val predictionState: StateFlow<BarometerPrediction> = _predictionState.asStateFlow()

    private var isListening = false
    private var lastKnownGpsAltitudeMeters: Float? = null

    companion object {
        private const val TAG = "BarometerPredictor"
        private const val KEY_SAMPLES = "pressure_samples"
        private const val KEY_LAST_ALERT_TIME = "last_barometer_alert_time"
        private const val MAX_HISTORY_MS = 6 * 60 * 60 * 1000L // Mantener últimas 6 horas
        private const val MIN_RECORD_INTERVAL_MS = 5 * 60 * 1000L // 5 min entre muestras continuas
        private const val ALERT_THROTTLE_MS = 45 * 60 * 1000L // Mínimo 45 min entre alertas automáticas
        const val METERS_PER_HPA = 8.33f // Fórmula barométrica terrestre estándar cerca del nivel del suelo
    }

    init {
        _predictionState.value = getLatestPrediction()
    }

    fun updateGpsAltitude(altitudeMeters: Float) {
        lastKnownGpsAltitudeMeters = altitudeMeters
    }

    fun measureCurrentPressure() {
        if (!isHardwareSensorAvailable || sensorManager == null || pressureSensor == null) {
            Log.d(TAG, "Sensor de presión no disponible en este hardware")
            return
        }
        if (isListening) return

        try {
            isListening = true
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_UI)
        } catch (t: Throwable) {
            Log.w(TAG, "Error registrando sensor barómetro", t)
            isListening = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PRESSURE) {
            val pressure = event.values.firstOrNull() ?: return
            if (pressure in 500f..1150f) { // Rango válido terrestre en hPa
                val isFamilyFriendly = settingsManager.isFamilyFriendlyMode
                recordSample(
                    pressureHpa = pressure,
                    timestampMs = System.currentTimeMillis(),
                    isFamilyFriendly = isFamilyFriendly,
                    altitudeMeters = lastKnownGpsAltitudeMeters
                )
            }
            try {
                sensorManager?.unregisterListener(this)
            } catch (t: Throwable) {
                Log.w(TAG, "Error desregistrando sensor", t)
            } finally {
                isListening = false
            }
        }
    }

    fun getLastSampleTimestamp(): Long {
        return loadSamples().lastOrNull()?.timestampMs ?: 0L
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Sin acción necesaria
    }

    fun recordSample(
        pressureHpa: Float,
        timestampMs: Long = System.currentTimeMillis(),
        isFamilyFriendly: Boolean = true,
        altitudeMeters: Float? = lastKnownGpsAltitudeMeters
    ) {
        val samples = loadSamples().toMutableList()
        val lastSample = samples.lastOrNull()

        // Evitar duplicar muestras en menos de MIN_RECORD_INTERVAL_MS a menos que haya un cambio drástico
        if (lastSample != null && (timestampMs - lastSample.timestampMs) < MIN_RECORD_INTERVAL_MS) {
            // Reemplazar la última muestra con la más reciente
            samples[samples.size - 1] = PressureSample(timestampMs, pressureHpa, altitudeMeters ?: lastSample.altitudeMeters)
        } else {
            samples.add(PressureSample(timestampMs, pressureHpa, altitudeMeters))
        }

        saveSamples(samples)
        val prediction = calculatePrediction(samples, isFamilyFriendly)
        _predictionState.value = prediction

        // Si la tendencia indica lluvia o tormenta inminente y NO es un pico de altitud, evaluar notificación
        if (prediction.trend == BarometerTrendType.STORM_INCOMING || prediction.trend == BarometerTrendType.RAIN_LIKELY) {
            checkAndSendAutoAlert(prediction, isFamilyFriendly)
        }
    }

    private fun checkAndSendAutoAlert(prediction: BarometerPrediction, isFamilyFriendly: Boolean) {
        // El filtro de altitud bloquea falsas alarmas provocadas por subir escaleras o ascensor
        if (prediction.isAltitudeSpikeDetected || prediction.trend == BarometerTrendType.ALTITUDE_CHANGE) {
            Log.d(TAG, "Alerta descartada por filtro de altitud: subida detectada de ~${prediction.estimatedAltitudeDeltaMeters}m")
            return
        }

        val now = System.currentTimeMillis()
        val lastAlertTime = prefs.getLong(KEY_LAST_ALERT_TIME, 0L)
        if (now - lastAlertTime >= ALERT_THROTTLE_MS) {
            prefs.edit().putLong(KEY_LAST_ALERT_TIME, now).apply()
            NotificationHelper.sendBarometerRainAlert(
                context = context,
                prediction = prediction,
                isFamilyFriendlyMode = isFamilyFriendly
            )
        }
    }

    fun getLatestPrediction(isFamilyFriendly: Boolean = true): BarometerPrediction {
        val samples = loadSamples()
        return calculatePrediction(samples, isFamilyFriendly)
    }

    private fun calculatePrediction(
        samples: List<PressureSample>,
        isFamilyFriendly: Boolean
    ): BarometerPrediction {
        val now = System.currentTimeMillis()
        val altitudeFilterEnabled = settingsManager.isBarometerAltitudeFilterEnabled

        if (samples.isEmpty()) {
            val defaultPressure = 1013.25f // Estándar nivel del mar
            val (title, desc) = getTexts(BarometerTrendType.CALIBRATING, defaultPressure, 0f, 0f, isFamilyFriendly)
            return BarometerPrediction(
                currentPressureHpa = defaultPressure,
                previousPressureHpa = null,
                deltaHpa = 0f,
                hoursDifference = 0f,
                trend = BarometerTrendType.CALIBRATING,
                title = title,
                description = desc,
                isHardwareSensorAvailable = isHardwareSensorAvailable,
                timestampMs = now,
                estimatedAltitudeDeltaMeters = 0f,
                isAltitudeSpikeDetected = false,
                isAltitudeFilterActive = altitudeFilterEnabled
            )
        }

        val current = samples.last()
        if (samples.size == 1) {
            val (title, desc) = getTexts(BarometerTrendType.CALIBRATING, current.pressureHpa, 0f, 0f, isFamilyFriendly)
            return BarometerPrediction(
                currentPressureHpa = current.pressureHpa,
                previousPressureHpa = null,
                deltaHpa = 0f,
                hoursDifference = 0f,
                trend = BarometerTrendType.CALIBRATING,
                title = title,
                description = desc,
                isHardwareSensorAvailable = isHardwareSensorAvailable,
                timestampMs = current.timestampMs,
                estimatedAltitudeDeltaMeters = 0f,
                isAltitudeSpikeDetected = false,
                isAltitudeFilterActive = altitudeFilterEnabled
            )
        }

        // Buscar muestra histórica de hace 1 a 3 horas (óptimo 2h)
        val targetTime = current.timestampMs - (2 * 60 * 60 * 1000L)
        val baseline = samples.dropLast(1).minByOrNull { abs(it.timestampMs - targetTime) } ?: samples.first()
        val dtMillis = (current.timestampMs - baseline.timestampMs).coerceAtLeast(1000L)
        val dtHours = dtMillis / (1000f * 3600f)
        val deltaHpa = current.pressureHpa - baseline.pressureHpa
        val ratePerHour = if (dtHours > 0.05f) deltaHpa / dtHours else deltaHpa

        // Estimación de desnivel de altitud en metros: Δh ≈ -ΔP * 8.33m
        val estimatedAltitudeDelta = -deltaHpa * METERS_PER_HPA

        // Si tenemos datos de altitud GPS en ambas muestras, calcular compensación de elevación
        val gpsAltDelta = if (current.altitudeMeters != null && baseline.altitudeMeters != null) {
            current.altitudeMeters - baseline.altitudeMeters
        } else null

        val compensatedDeltaHpa = if (gpsAltDelta != null && abs(gpsAltDelta) >= 4.0f) {
            val altPressureCorrection = -gpsAltDelta / METERS_PER_HPA
            deltaHpa - altPressureCorrection
        } else {
            deltaHpa
        }

        // Detección física de subida de altitud (gradas, ascensor, colina):
        // 1. Caída de presión rápida que excede la tasa meteorológica máxima (> 2.0 hPa/h o caída en < 45 min).
        // 2. O cambio confirmado por altitud GPS donde el barómetro bajó por altura física y no clima.
        val isRapidAltitudeClimb = altitudeFilterEnabled && (
            (deltaHpa <= -0.7f && (dtHours < 0.65f || ratePerHour <= -2.0f)) ||
            (gpsAltDelta != null && gpsAltDelta >= 5.0f && abs(compensatedDeltaHpa) < 0.6f)
        )

        // Detección de escalón estabilizado: el usuario subió gradas hace un rato y la presión se quedó fija en la nueva altura
        val isStepLeveledOff = altitudeFilterEnabled && (deltaHpa <= -0.8f) && dtHours >= 0.75f && run {
            val recentSamples = samples.filter { current.timestampMs - it.timestampMs in 600_000L..3600_000L }
            recentSamples.isNotEmpty() && abs(current.pressureHpa - recentSamples.first().pressureHpa) < 0.35f
        }

        val trend = when {
            // Salto brusco por gradas o ascensor (filtro de altitud activo)
            isRapidAltitudeClimb -> BarometerTrendType.ALTITUDE_CHANGE

            // Se estabilizó tras subir gradas o piso anterior
            isStepLeveledOff -> BarometerTrendType.STEADY

            // Caída violenta sostenida meteorológica (mínimo 45 min de observación, tasa entre -0.6 y -2.2 hPa/h)
            dtHours >= 0.75f && compensatedDeltaHpa <= -1.8f && ratePerHour in -2.2f..-0.6f -> BarometerTrendType.STORM_INCOMING

            // Caída moderada sostenida meteorológica (mínimo 45 min de observación, tasa entre -0.35 y -1.8 hPa/h)
            dtHours >= 0.75f && compensatedDeltaHpa <= -0.9f && ratePerHour in -1.8f..-0.35f -> BarometerTrendType.RAIN_LIKELY

            // Si el usuario desactivó el filtro de altitud, comportamiento crudo tradicional
            !altitudeFilterEnabled && (deltaHpa <= -2.0f || (dtHours >= 0.4f && ratePerHour <= -0.8f)) -> BarometerTrendType.STORM_INCOMING
            !altitudeFilterEnabled && (deltaHpa <= -1.0f || (dtHours >= 0.4f && ratePerHour <= -0.4f)) -> BarometerTrendType.RAIN_LIKELY

            // Subida clara de presión (mejora del tiempo)
            compensatedDeltaHpa >= 0.8f || (dtHours >= 0.4f && ratePerHour >= 0.35f) -> BarometerTrendType.IMPROVING

            // Presión estable
            dtHours >= 0.25f && abs(compensatedDeltaHpa) < 0.8f -> BarometerTrendType.STEADY

            // Muestras insuficientes o tiempo de calibración
            else -> BarometerTrendType.CALIBRATING
        }

        val (title, desc) = getTexts(trend, current.pressureHpa, deltaHpa, estimatedAltitudeDelta, isFamilyFriendly)

        return BarometerPrediction(
            currentPressureHpa = current.pressureHpa,
            previousPressureHpa = baseline.pressureHpa,
            deltaHpa = deltaHpa,
            hoursDifference = dtHours,
            trend = trend,
            title = title,
            description = desc,
            isHardwareSensorAvailable = isHardwareSensorAvailable,
            timestampMs = current.timestampMs,
            estimatedAltitudeDeltaMeters = estimatedAltitudeDelta,
            isAltitudeSpikeDetected = isRapidAltitudeClimb,
            isAltitudeFilterActive = altitudeFilterEnabled
        )
    }

    private fun getTexts(
        trend: BarometerTrendType,
        currentPressureHpa: Float,
        deltaHpa: Float,
        estimatedAltitudeDelta: Float,
        isFamilyFriendly: Boolean
    ): Pair<String, String> {
        val deltaStr = String.format(Locale.US, "%.1f", abs(deltaHpa))
        val presStr = String.format(Locale.US, "%.1f", currentPressureHpa)
        val altStr = String.format(Locale.US, "%.0f", abs(estimatedAltitudeDelta))

        return when (trend) {
            BarometerTrendType.ALTITUDE_CHANGE -> {
                val title = if (isFamilyFriendly) "🧗 Cambio de altitud (~+$altStr m)" else "🧗 ¡Subió gradas o ascensor, mae! (~+$altStr m)"
                val desc = if (isFamilyFriendly) {
                    "La presión bajó -$deltaStr hPa en muy poco tiempo (~+$altStr m de desnivel). El filtro anti-escaleras (8m ≈ 1 hPa) evitó una falsa alarma de lluvia."
                } else {
                    "La presión cayó -$deltaStr hPa en dos toques (~+$altStr m de altura). Mae, fueron gradas o ascensor, no se embarque pensando que va a llover."
                }
                Pair(title, desc)
            }
            BarometerTrendType.STORM_INCOMING -> {
                val title = if (isFamilyFriendly) "⚡ ¡Alerta de tormenta barométrica!" else "⚡ ¡MAE, SE DESPLOMÓ LA PRESIÓN!"
                val desc = if (isFamilyFriendly) {
                    "El barómetro cayó -$deltaStr hPa en las últimas horas ($presStr hPa). Se prevé baldazo o rayería inminente."
                } else {
                    "¡La presión cayó -$deltaStr hPa ($presStr hPa)! Viene un baldazo con rayería en menos de una hora, busque techo ya."
                }
                Pair(title, desc)
            }
            BarometerTrendType.RAIN_LIKELY -> {
                val title = if (isFamilyFriendly) "🌧️ Presión a la baja (Lluvia probable)" else "🌧️ ¡El barómetro viene en picada!"
                val desc = if (isFamilyFriendly) {
                    "Caída gradual de -$deltaStr hPa detectada ($presStr hPa). Hay alta probabilidad de lluvia pronto, lleve sombrilla."
                } else {
                    "El sensor bajó -$deltaStr hPa ($presStr hPa). Va a llover pronto mae, si no saca el paraguas se va a empapar."
                }
                Pair(title, desc)
            }
            BarometerTrendType.STEADY -> {
                val title = if (isFamilyFriendly) "🌤️ Presión atmosférica estable" else "🌤️ Presión estable, todo al suave"
                val desc = if (isFamilyFriendly) {
                    "Lectura actual: $presStr hPa (variación de solo $deltaStr hPa). No se esperan cambios bruscos de clima."
                } else {
                    "Presión en $presStr hPa (variación de $deltaStr hPa). El tiempo está tranquilo, ni sube ni baja, pura vida."
                }
                Pair(title, desc)
            }
            BarometerTrendType.IMPROVING -> {
                val title = if (isFamilyFriendly) "☀️ Presión subiendo (Mejorando)" else "☀️ ¡Va escampando, pura vida!"
                val desc = if (isFamilyFriendly) {
                    "La presión aumentó +$deltaStr hPa ($presStr hPa). El tiempo tiende a mejorar y despejar."
                } else {
                    "Subió +$deltaStr hPa ($presStr hPa). Ya va pasando el mal tiempo, deje de quejarse un toque."
                }
                Pair(title, desc)
            }
            BarometerTrendType.CALIBRATING -> {
                val title = if (isFamilyFriendly) "🧭 Barómetro registrando datos" else "🧭 Barómetro activo en el reloj"
                val desc = if (isFamilyFriendly) {
                    "Presión actual: $presStr hPa. Recopilando historial para calcular la tendencia de lluvia con precisión."
                } else {
                    "Presión: $presStr hPa. Registrando lecturas del sensor para ver si la presión se desploma o qué."
                }
                Pair(title, desc)
            }
        }
    }

    /**
     * Permite probar la alerta barométrica simulando una caída gradual de presión de 2 horas
     * para verificar la notificación háptica en el Galaxy Watch o celular sin disparar el filtro de altitud.
     */
    fun simulatePressureDropForTest(
        context: Context,
        dropHpa: Float = 2.4f,
        isFamilyFriendly: Boolean = true
    ): BarometerPrediction {
        val samples = loadSamples().toMutableList()
        val now = System.currentTimeMillis()
        val basePressure = samples.lastOrNull()?.pressureHpa ?: 1012.0f

        // Añadir muestra base de hace 2 horas para simular una caída meteorológica gradual legítima
        if (samples.isEmpty()) {
            samples.add(PressureSample(now - (2 * 3600 * 1000L), basePressure))
        } else {
            // Asegurar que la muestra anterior esté espaciada 2 horas para simular tormenta real
            samples[samples.size - 1] = PressureSample(now - (2 * 3600 * 1000L), basePressure)
        }

        // Añadir muestra actual con la caída de presión simulada
        val droppedPressure = (basePressure - dropHpa).coerceAtLeast(800.0f)
        samples.add(PressureSample(now, droppedPressure))
        saveSamples(samples)

        val prediction = calculatePrediction(samples, isFamilyFriendly)
        _predictionState.value = prediction

        NotificationHelper.sendBarometerRainAlert(
            context = context,
            prediction = prediction,
            isFamilyFriendlyMode = isFamilyFriendly
        )

        return prediction
    }

    private fun loadSamples(): List<PressureSample> {
        val raw = prefs.getString(KEY_SAMPLES, null) ?: return emptyList()
        val now = System.currentTimeMillis()
        return raw.split(";")
            .mapNotNull { item ->
                val parts = item.split(":")
                if (parts.size >= 2) {
                    val time = parts[0].toLongOrNull()
                    val pressure = parts[1].toFloatOrNull()
                    val altitude = if (parts.size >= 3) parts[2].toFloatOrNull() else null
                    if (time != null && pressure != null && (now - time) <= MAX_HISTORY_MS) {
                        PressureSample(time, pressure, altitude)
                    } else null
                } else null
            }
            .sortedBy { it.timestampMs }
    }

    private fun saveSamples(samples: List<PressureSample>) {
        val now = System.currentTimeMillis()
        val valid = samples.filter { (now - it.timestampMs) <= MAX_HISTORY_MS }.takeLast(25)
        val serialized = valid.joinToString(";") {
            if (it.altitudeMeters != null) "${it.timestampMs}:${it.pressureHpa}:${it.altitudeMeters}"
            else "${it.timestampMs}:${it.pressureHpa}"
        }
        prefs.edit().putString(KEY_SAMPLES, serialized).apply()
    }
}

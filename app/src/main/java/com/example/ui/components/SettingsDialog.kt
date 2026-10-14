package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import com.example.R
import com.example.data.model.WeatherCategory
import com.example.data.model.WeatherData
import com.example.domain.ExcuseGenerator
import androidx.compose.material.icons.filled.Place
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.FirebaseRepository

@Composable
fun SettingsDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    notificationsEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    familyFriendlyEnabled: Boolean,
    onToggleFamilyFriendly: (Boolean) -> Unit,
    onSendTestNotification: () -> Unit,
    sanPedroBlameCount: Int,
    onBlameSanPedro: () -> Unit,
    currentWeatherCategory: WeatherCategory? = null,
    currentWeather: WeatherData? = null,
    onOpenWatchPreview: () -> Unit = {},
    barometerAltitudeFilterEnabled: Boolean = true,
    onToggleBarometerAltitudeFilter: (Boolean) -> Unit = {}
) {
    if (!visible) return

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showWarningDialog by remember { mutableStateOf(false) }
    var showWearDisclaimerDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    val firebaseRepo = remember { FirebaseRepository.create(context) }
    val coroutineScope = rememberCoroutineScope()
    val currentUser by firebaseRepo.currentUser.collectAsStateWithLifecycle(initialValue = firebaseRepo.getCurrentUser())
    var isSigningIn by remember { mutableStateOf(false) }

    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Privacidad y Datos",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "• Ubicación Opcional: El permiso de GPS es 100% opcional. Podés buscar cualquier cantón o distrito de Costa Rica (o del extranjero) de forma manual en el buscador sin activar el GPS.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Proveedores Meteorológicos: Cuando se consulta el clima, las coordenadas geográficas se envían de forma anónima y cifrada por HTTPS a api.open-meteo.com y api.weatherapi.com únicamente para consultar la información del tiempo.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Privacidad Garantizada: La app no almacena tu historial de ubicaciones, no rastrea tu actividad en segundo plano y no comparte ni vende ningún dato personal.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyPolicyDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    if (showWearDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { showWearDisclaimerDialog = false },
            icon = {
                Text("⌚", fontSize = 32.sp)
            },
            title = {
                Text(
                    text = "Aviso: Modo Reloj (Wear OS)",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Mae, esta vista simula la interfaz y funcionamiento en un smartwatch con Wear OS (Galaxy Watch 8 Classic, etc.).\n\n" +
                            "Incluye el WSI glanceable, La Píldora del Mae, carrusel de 8h y el Barómetro local con filtro anti-altitud (8m ≈ 1 hPa) para predecir lluvia real sin falsas alarmas al subir gradas o ascensor.\n\n" +
                            "Podés probar la alerta barométrica directamente o salir tocando 'Volver al cel' al final."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWearDisclaimerDialog = false
                        onDismiss()
                        onOpenWatchPreview()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Entendido, Probar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showWearDisclaimerDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showWarningDialog) {
        AlertDialog(
            onDismissRequest = { showWarningDialog = false },
            title = {
                Text(
                    text = "⚠️ Mae, momento…",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Estás a punto de activar el Modo Calle Sin Filtro.\n\nLa app podrá usar lenguaje vulgar, insultos y comentarios que probablemente no querás enseñarle a tu abuela.\n\n¿Seguro que querés continuar?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWarningDialog = false
                        onToggleFamilyFriendly(false)
                    }
                ) {
                    Text("Sí, quitar el filtro")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showWarningDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Configuración",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Weather Station Information Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Estación meteorológica en uso 📡",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val stationCity = currentWeather?.cityName ?: "San José (Chepe)"
                        val stationCountry = currentWeather?.country ?: "Costa Rica"
                        val lat = currentWeather?.latitude ?: 9.9281
                        val lon = currentWeather?.longitude ?: -84.0907

                        Text(
                            text = "📍 Ubicación: $stationCity, $stationCountry",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🌐 Coordenadas de la estación: ${String.format(java.util.Locale.US, "%.4f", lat)}° N, ${String.format(java.util.Locale.US, "%.4f", lon)}° O",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🛰️ Red de datos: Cuadrícula meteorológica WMO / IMN (Open-Meteo & WeatherAPI)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        if (currentWeather?.isFromCache == true) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💾 Estación operando con datos locales de caché offline",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                // Toggle switch row - Family Friendly
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Modo Family Friendly 👨‍👩‍👧‍👦",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Mantiene los comentarios aptos para toda la familia.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Switch(
                            checked = familyFriendlyEnabled,
                            onCheckedChange = { checked ->
                                if (!checked) {
                                    showWarningDialog = true
                                } else {
                                    onToggleFamilyFriendly(true)
                                }
                            },
                            modifier = Modifier.testTag("family_friendly_switch")
                        )
                    }
                }

                // Toggle switch row - Notifications
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Alertas meteorológicas",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (notificationsEnabled) "Activadas" else "Desactivadas",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = onToggleNotifications,
                            modifier = Modifier.testTag("notifications_switch")
                        )
                    }
                }

                if (notificationsEnabled) {
                    FilledTonalButton(
                        onClick = onSendTestNotification,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_notification_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.notifications_test_button))
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Toggle switch row - Barometer Altitude Filter (8m ≈ 1 hPa)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "🧗 Filtro anti-altitud (8m ≈ 1 hPa)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (barometerAltitudeFilterEnabled) {
                                    "Activo: evita falsas alarmas de lluvia si subes gradas o ascensor (~8m)."
                                } else {
                                    "Desactivado: lee presión atmosférica cruda sin filtrar subidas."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Switch(
                            checked = barometerAltitudeFilterEnabled,
                            onCheckedChange = onToggleBarometerAltitudeFilter,
                            modifier = Modifier.testTag("barometer_altitude_filter_switch")
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Blame San Pedro counter
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Veces que le has echado la culpa a San Pedro (Global):",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "$sanPedroBlameCount",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Button(
                            onClick = onBlameSanPedro,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onErrorContainer,
                                contentColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Echarle la culpa a San Pedro 🤬")
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Excuse Generator button
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Llegar tarde con estilo:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Genera una excusa tica según el clima y cópiala para el profe.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(vertical = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = {
                                val excuse = ExcuseGenerator.generateExcuse(currentWeatherCategory, familyFriendlyEnabled)
                                clipboardManager.setText(AnnotatedString(excuse))
                                Toast.makeText(context, "Excusa copiada al portapapeles", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                contentColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Generar y Copiar Excusa 📱")
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Modo Reloj (Wear OS)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⌚",
                            fontSize = 26.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Modo Reloj (Wear OS)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "WSI, Píldora del Mae, carrusel y Barómetro",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = {
                                showWearDisclaimerDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("wear_os_preview_button")
                        ) {
                            Text("Probar", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Firebase Cloud Sync Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firebase_sync_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "☁️",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sincronización en la Nube (Firebase)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (currentUser != null) "Conectado como ${currentUser?.displayName ?: "Usuario"}" else "Respaldá tus cantones y culpas a San Pedro",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val user = currentUser
                        if (user != null) {
                            Text(
                                text = "Cuenta: ${user.email ?: user.uid}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    firebaseRepo.signOut(context, coroutineScope) {
                                        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("firebase_sign_out_button")
                            ) {
                                Text("Cerrar Sesión")
                            }
                        } else {
                            Text(
                                text = "Iniciá sesión con Google para sincronizar tus cantones favoritos y culpas a San Pedro en Firestore.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    isSigningIn = true
                                    firebaseRepo.signInWithGoogle(
                                        context = context,
                                        scope = coroutineScope,
                                        onSuccess = {
                                            isSigningIn = false
                                            Toast.makeText(context, "¡Sesión iniciada con Google!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { error ->
                                            isSigningIn = false
                                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                        },
                                        onCancelled = {
                                            isSigningIn = false
                                        }
                                    )
                                },
                                enabled = !isSigningIn,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("google_sign_in_button")
                            ) {
                                if (isSigningIn) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Iniciando sesión...")
                                } else {
                                    Text("Iniciar sesión con Google")
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Epic Movie Credits
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color.Black,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CRÉDITOS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp
                            ),
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = "CREADOR Y DIRECTOR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = androidx.compose.ui.graphics.Color.LightGray
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "Uriel",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )
                        
                        Text(
                            text = "COMBUSTIBLE PRINCIPAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = androidx.compose.ui.graphics.Color.LightGray
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "Café",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )
                        
                        Text(
                            text = "CÓDIGO & ASISTENCIA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = androidx.compose.ui.graphics.Color.LightGray
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "IA",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Privacy Policy and Location Data Transparency Button
                OutlinedButton(
                    onClick = { showPrivacyPolicyDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("privacy_policy_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Privacidad y Manejo de Ubicación")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Listo")
            }
        }
    )
}

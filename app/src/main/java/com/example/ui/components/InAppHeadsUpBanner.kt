package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeatherCategory
import com.example.ui.InAppAlertData
import kotlinx.coroutines.delay

/**
 * Animated Heads-Up Pop-Up Banner that slides in from the top of the screen
 * when a Mae weather alert is posted or simulated, accompanied by screen flash
 * animation and haptic feedback.
 */
@Composable
fun InAppHeadsUpBanner(
    alert: InAppAlertData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val flashAnim = remember { Animatable(0f) }

    LaunchedEffect(alert?.timestamp) {
        if (alert != null) {
            // Strobe 3 high-intensity pulses across the screen with synchronized haptic feedback
            repeat(3) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                flashAnim.animateTo(0.65f, animationSpec = tween(120, easing = FastOutSlowInEasing))
                flashAnim.animateTo(0f, animationSpec = tween(140, easing = FastOutSlowInEasing))
                delay(70)
            }
        } else {
            flashAnim.snapTo(0f)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_border")
    val pulseBorderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_alpha"
    )

    // Full screen flash strobe layer
    if (flashAnim.value > 0.01f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFB300).copy(alpha = flashAnim.value))
        )
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = statusBarPadding + 8.dp, start = 12.dp, end = 12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = alert != null,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(250)
            ) + fadeOut(animationSpec = tween(200))
        ) {
            if (alert != null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF131C2E)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("in_app_heads_up_banner")
                        .border(
                            width = 2.dp,
                            color = Color(0xFFFFB300).copy(alpha = pulseBorderAlpha),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Header row with Icon, Badge, and Close Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFB300).copy(alpha = 0.25f))
                                        .border(1.dp, Color(0xFFFFB300), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val iconEmoji = when (alert.category) {
                                        WeatherCategory.THUNDERSTORM -> "⚡"
                                        WeatherCategory.HEAVY_RAIN, WeatherCategory.RAIN -> "🌧️"
                                        WeatherCategory.HIGH_UV, WeatherCategory.EXTREME_HEAT -> "☀️"
                                        WeatherCategory.COLD -> "🥶"
                                        else -> "🔔"
                                    }
                                    Text(iconEmoji, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "ALERTA DEL MAE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 1.sp
                                            ),
                                            color = Color(0xFFFFB300)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFFF3D00).copy(alpha = 0.25f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "POP-UP",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color(0xFFFF6E40)
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("dismiss_in_app_banner_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar alerta",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Title
                        Text(
                            text = alert.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Message body
                        Text(
                            text = alert.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Footer with action button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 Luz flash y vibración háptica activadas",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color(0xFFFFD54F).copy(alpha = 0.85f)
                            )

                            FilledTonalButton(
                                onClick = onDismiss,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFFFFB300).copy(alpha = 0.25f),
                                    contentColor = Color(0xFFFFD54F)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = "Entendido",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

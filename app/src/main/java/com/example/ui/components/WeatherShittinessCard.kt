package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.WeatherShittinessBreakdown
import com.example.domain.WeatherShittinessCategory
import com.example.domain.WeatherShittinessResult

@Composable
fun WeatherShittinessCard(
    result: WeatherShittinessResult,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val categoryColor = when (result.category) {
        WeatherShittinessCategory.PERFECT -> Color(0xFF10B981)
        WeatherShittinessCategory.PRETTY_GOOD -> Color(0xFF06B6D4)
        WeatherShittinessCategory.MEH -> Color(0xFFF59E0B)
        WeatherShittinessCategory.GETTING_BAD -> Color(0xFFF97316)
        WeatherShittinessCategory.BAD -> Color(0xFFEF4444)
        WeatherShittinessCategory.VERY_BAD -> Color(0xFFDC2626)
        WeatherShittinessCategory.ABSOLUTELY_HORRIBLE -> Color(0xFF8B5CF6)
    }

    val gradientCardColors = when (result.category) {
        WeatherShittinessCategory.PERFECT -> listOf(Color(0xFF064E3B).copy(alpha = 0.85f), Color(0xFF0F172A).copy(alpha = 0.9f))
        WeatherShittinessCategory.PRETTY_GOOD -> listOf(Color(0xFF0E7490).copy(alpha = 0.85f), Color(0xFF0F172A).copy(alpha = 0.9f))
        WeatherShittinessCategory.MEH -> listOf(Color(0xFF78350F).copy(alpha = 0.85f), Color(0xFF0F172A).copy(alpha = 0.9f))
        WeatherShittinessCategory.GETTING_BAD -> listOf(Color(0xFF9A3412).copy(alpha = 0.85f), Color(0xFF0F172A).copy(alpha = 0.9f))
        WeatherShittinessCategory.BAD -> listOf(Color(0xFF991B1B).copy(alpha = 0.85f), Color(0xFF0F172A).copy(alpha = 0.9f))
        WeatherShittinessCategory.VERY_BAD -> listOf(Color(0xFF7F1D1D).copy(alpha = 0.9f), Color(0xFF0F172A).copy(alpha = 0.95f))
        WeatherShittinessCategory.ABSOLUTELY_HORRIBLE -> listOf(Color(0xFF4C1D95).copy(alpha = 0.95f), Color(0xFF1E1B4B).copy(alpha = 0.95f))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("wsi_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradientCardColors))
                .border(
                    width = 1.dp,
                    color = categoryColor.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header badge row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = categoryColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = categoryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "¿QUÉ TAN FEA ESTÁ LA VARA?",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                color = Color.White.copy(alpha = 0.95f)
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "Nivel de Desmadre",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f)
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Circular Progress Gauge & Score Display
                WsiCircularGauge(
                    score = result.score,
                    accentColor = categoryColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category pill: emoji + title
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = categoryColor.copy(alpha = 0.22f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, categoryColor.copy(alpha = 0.55f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = result.emoji,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = result.title.uppercase(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                color = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Commentary Quote Box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "“${result.description}”",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Italic,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // "Why" Accordion Button
                val arrowRotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(durationMillis = 300),
                    label = "arrow_rotation"
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = if (isExpanded) 0.15f else 0.08f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = { isExpanded = !isExpanded }
                        )
                        .testTag("wsi_why_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "¿Por qué este puntaje?",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Ocultar detalles" else "Ver detalles",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier
                                .size(22.dp)
                                .rotate(arrowRotation)
                        )
                    }
                }

                // Expandable Breakdown Section
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                    modifier = Modifier.testTag("wsi_breakdown_section")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        BreakdownDetailsCard(
                            breakdown = result.breakdown,
                            totalScore = result.score,
                            accentColor = categoryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WsiCircularGauge(
    score: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "wsi_score_int"
    )

    val animatedSweep by animateFloatAsState(
        targetValue = (score / 100f) * 270f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "wsi_gauge_sweep"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(160.dp)
    ) {
        // Gauge Canvas
        Canvas(modifier = Modifier.size(148.dp)) {
            val strokeWidth = 14.dp.toPx()
            val startAngle = 135f
            val maxSweep = 270f

            // Background Track
            drawArc(
                color = Color.White.copy(alpha = 0.12f),
                startAngle = startAngle,
                sweepAngle = maxSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress Arc
            if (animatedSweep > 0.1f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to accentColor.copy(alpha = 0.75f),
                        0.5f to accentColor,
                        1.0f to accentColor
                    ),
                    startAngle = startAngle,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Center Score Counter
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$animatedScore",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 46.sp,
                    color = Color.White
                ),
                modifier = Modifier.testTag("wsi_score_text")
            )
            Text(
                text = "/ 100",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
private fun BreakdownDetailsCard(
    breakdown: WeatherShittinessBreakdown,
    totalScore: Int,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Black.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Desglose de factores contribuyentes:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                ),
                modifier = Modifier.padding(bottom = 2.dp)
            )

            BreakdownItemRow(emoji = "🌡️", label = "Sensación térmica", impact = breakdown.feelsLikeImpact)
            BreakdownItemRow(emoji = "💧", label = "Humedad ambiental", impact = breakdown.humidityImpact)
            BreakdownItemRow(emoji = "🌧️", label = "Prob. de lluvia", impact = breakdown.rainImpact)
            BreakdownItemRow(emoji = "💨", label = "Velocidad del viento", impact = breakdown.windImpact)
            BreakdownItemRow(emoji = "☀️", label = "Radiación UV", impact = breakdown.uvImpact)
            BreakdownItemRow(emoji = "⛈️", label = "Condición del tiempo", impact = breakdown.conditionImpact)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color.White.copy(alpha = 0.15f)
            )

            // Total confirmation row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Puntaje total calculado",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "= $totalScore / 100",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                )
            }
        }
    }
}

@Composable
private fun BreakdownItemRow(
    emoji: String,
    label: String,
    impact: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = emoji,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Text(
            text = if (impact > 0) "+$impact pts" else "0 pts",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (impact > 0) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.45f)
            )
        )
    }
}

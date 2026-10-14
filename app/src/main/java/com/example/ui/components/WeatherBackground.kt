package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.model.WeatherCategory

@Composable
fun WeatherDynamicBackground(
    category: WeatherCategory,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val (targetTopColor, targetMidColor, targetBottomColor) = remember(category, isDay) {
        when {
            category == WeatherCategory.THUNDERSTORM -> Triple(
                Color(0xFF19122B),
                Color(0xFF261D42),
                Color(0xFF0F0B1C)
            )
            category == WeatherCategory.HEAVY_RAIN || category == WeatherCategory.RAIN -> Triple(
                Color(0xFF1C2B38),
                Color(0xFF243B4D),
                Color(0xFF0E1720)
            )
            category == WeatherCategory.EXTREME_HEAT -> Triple(
                Color(0xFFBF360C),
                Color(0xFFE65100),
                Color(0xFF3E1F07)
            )
            category == WeatherCategory.COLD -> Triple(
                Color(0xFF1A365D),
                Color(0xFF2B4C7E),
                Color(0xFF0F1E33)
            )
            !isDay || category == WeatherCategory.NIGHT -> Triple(
                Color(0xFF090D16),
                Color(0xFF131D2E),
                Color(0xFF06090F)
            )
            category == WeatherCategory.CLOUDS -> Triple(
                Color(0xFF37474F),
                Color(0xFF546E7A),
                Color(0xFF263238)
            )
            category == WeatherCategory.HIGH_UV -> Triple(
                Color(0xFFD84315),
                Color(0xFFEF6C00),
                Color(0xFF4E2609)
            )
            else -> Triple(
                // Clear pleasant tropical day
                Color(0xFF0277BD),
                Color(0xFF0288D1),
                Color(0xFF01579B)
            )
        }
    }

    val animatedTop by animateColorAsState(
        targetValue = targetTopColor,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "bgTop"
    )
    val animatedMid by animateColorAsState(
        targetValue = targetMidColor,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "bgMid"
    )
    val animatedBottom by animateColorAsState(
        targetValue = targetBottomColor,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "bgBottom"
    )

    val backgroundBrush = remember(animatedTop, animatedMid, animatedBottom) {
        Brush.verticalGradient(
            colors = listOf(animatedTop, animatedMid, animatedBottom)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
    ) {
        content()
    }
}

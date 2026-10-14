package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.WeatherData
import com.example.domain.activeCombo

@Composable
fun SarcasticCommentCard(
    weather: WeatherData,
    onRollNewComment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val conditionHeader = "${weather.temperature.toInt()}°C, ${weather.humidity}% humedad y ${weather.conditionDescription.lowercase()}."

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sarcastic_comment_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with Sleek Tico Opinion Badge & Condition Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFB300).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.45f)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "El Mae Opina:",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        )
                        Text(
                            text = "🇨🇷 Humor tico en tiempo real",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Share button
                IconButton(
                    onClick = {
                        val shareText = "«${weather.sarcasticComment}»\n\n- ¿Y Ese Clima, Mae? (${weather.cityName}: ${weather.temperature.toInt()}°C)"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Compartir qué dijo el mae"))
                    },
                    modifier = Modifier.testTag("share_comment_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.share_comment),
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Offline cached notification badge if data was retrieved from Room
            if (weather.isFromCache) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE65100).copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sin señal, mae • Mostrando datos guardados",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Active combination badge if multiple severe weather factors combine
            val currentCombo = weather.activeCombo
            if (currentCombo != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFC62828).copy(alpha = 0.88f),
                    border = BorderStroke(1.dp, Color(0xFFFF8A80).copy(alpha = 0.6f)),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${currentCombo.emoji} ${currentCombo.badgeLabel}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Condition summary text
            Text(
                text = conditionHeader,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Main large sarcastic comment with smooth animated fade transition
            AnimatedContent(
                targetState = weather.sarcasticComment,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "sarcasticCommentAnim"
            ) { commentText ->
                Text(
                    text = commentText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 28.sp,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action button: Generar otro comentario del Mae (Sleek, modern, and non-cringe)
            FilledTonalButton(
                onClick = onRollNewComment,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("shuffle_comment_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFFFB300).copy(alpha = 0.22f),
                    contentColor = Color(0xFFFFD54F)
                ),
                border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFFFFD54F)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Generar otro comentario del Mae",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

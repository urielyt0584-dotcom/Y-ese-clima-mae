package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CityLocation

@Composable
fun QuickCityChipsRow(
    presetCities: List<CityLocation>,
    selectedCityName: String,
    onSelectCity: (CityLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = presetCities,
            key = { city -> city.name }
        ) { city ->
            val isSelected = selectedCityName.startsWith(city.name.take(6), ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCity(city) },
                label = {
                    Text(
                        text = city.name.split(" ").firstOrNull() ?: city.name,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Black.copy(alpha = 0.25f),
                    labelColor = Color.White.copy(alpha = 0.85f),
                    selectedContainerColor = Color(0xFFFFD54F),
                    selectedLabelColor = Color.Black
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.White.copy(alpha = 0.2f),
                    selectedBorderColor = Color(0xFFFFD54F)
                ),
                modifier = Modifier.testTag("city_quick_chip_${city.name}")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySearchBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    isSearching: Boolean,
    searchResults: List<CityLocation>,
    onSelectCity: (CityLocation) -> Unit,
    onRequestCurrentLocation: () -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "¿De dónde quiere ver el clima, mae?",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search text field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_city_input"),
                placeholder = { Text(stringResource(R.string.search_city_hint)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Current location button
            OutlinedButton(
                onClick = {
                    onRequestCurrentLocation()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("use_current_location_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.use_my_location))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isSearching) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Buscando con ganas, mae...")
                }
            } else {
                Text(
                    text = if (searchQuery.isEmpty()) "Ciudades y cantones sugeridos:" else "Resultados:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    items(searchResults) { city ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCity(city)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = city.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        text = city.subtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

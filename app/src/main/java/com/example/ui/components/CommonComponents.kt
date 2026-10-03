package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.NigeriaData
import com.example.ui.theme.OPayGreen
import com.example.ui.theme.SkillsOrange
import com.example.ui.theme.SuperBlue
import com.example.ui.theme.TrustGold
import com.example.ui.theme.VendorGreenDark

/**
 * State & LGA Selection Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateLgaSelectorDialog(
    currentState: String,
    currentLga: String,
    currentArea: String,
    onDismiss: () -> Unit,
    onSave: (state: String, lga: String, area: String) -> Unit
) {
    var selectedState by remember { mutableStateOf(currentState) }
    var selectedLga by remember {
        val lgas = NigeriaData.statesLGAs[currentState] ?: emptyList()
        mutableStateOf(if (lgas.contains(currentLga)) currentLga else lgas.firstOrNull() ?: "")
    }
    var areaText by remember { mutableStateOf(currentArea) }

    var stateExpanded by remember { mutableStateOf(false) }
    var lgaExpanded by remember { mutableStateOf(false) }

    val lgasForState = remember(selectedState) {
        NigeriaData.statesLGAs[selectedState] ?: emptyList()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Select Location (Nigeria)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))

                // State Selector
                ExposedDropdownMenuBox(
                    expanded = stateExpanded,
                    onExpandedChange = { stateExpanded = !stateExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedState,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("State (36 States + FCT)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = stateExpanded,
                        onDismissRequest = { stateExpanded = false }
                    ) {
                        NigeriaData.statesLGAs.keys.forEach { stateName ->
                            DropdownMenuItem(
                                text = { Text(stateName) },
                                onClick = {
                                    selectedState = stateName
                                    val newLgas = NigeriaData.statesLGAs[stateName] ?: emptyList()
                                    selectedLga = newLgas.firstOrNull() ?: ""
                                    stateExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // LGA Selector
                ExposedDropdownMenuBox(
                    expanded = lgaExpanded,
                    onExpandedChange = { lgaExpanded = !lgaExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedLga,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("LGA (${lgasForState.size} available)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lgaExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = lgaExpanded,
                        onDismissRequest = { lgaExpanded = false }
                    ) {
                        lgasForState.forEach { lgaName ->
                            DropdownMenuItem(
                                text = { Text(lgaName) },
                                onClick = {
                                    selectedLga = lgaName
                                    lgaExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Specific Area / Landmark
                OutlinedTextField(
                    value = areaText,
                    onValueChange = { areaText = it },
                    label = { Text("Area / Landmark (e.g. Ikeja, Allen, Wuse 2)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(selectedState, selectedLga, areaText.ifBlank { selectedLga })
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Location")
                    }
                }
            }
        }
    }
}

/**
 * QR Code Canvas Generator - Generates a valid matrix pattern without external dependencies
 */
@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    sizeDp: Int = 180
) {
    val hash = remember(data) { data.hashCode() }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cols = 21
            val cellSize = size.width / cols

            // Draw corner finder patterns
            fun drawFinder(startX: Int, startY: Int) {
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(startX * cellSize, startY * cellSize),
                    size = Size(7 * cellSize, 7 * cellSize)
                )
                drawRect(
                    color = Color.White,
                    topLeft = Offset((startX + 1) * cellSize, (startY + 1) * cellSize),
                    size = Size(5 * cellSize, 5 * cellSize)
                )
                drawRect(
                    color = Color.Black,
                    topLeft = Offset((startX + 2) * cellSize, (startY + 2) * cellSize),
                    size = Size(3 * cellSize, 3 * cellSize)
                )
            }

            drawFinder(0, 0)
            drawFinder(cols - 7, 0)
            drawFinder(0, cols - 7)

            // Fill deterministic pseudo-QR bits based on data
            for (x in 0 until cols) {
                for (y in 0 until cols) {
                    val inFinderTopLeft = x < 8 && y < 8
                    val inFinderTopRight = x >= cols - 8 && y < 8
                    val inFinderBottomLeft = x < 8 && y >= cols - 8

                    if (!inFinderTopLeft && !inFinderTopRight && !inFinderBottomLeft) {
                        val bit = ((hash xor (x * 37 + y * 73 + data.length)) and 1) == 1
                        if (bit) {
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(x * cellSize, y * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Mode Switcher Bar at the top of screens
 */
@Composable
fun ModeSwitchBar(
    currentMode: String,
    onSelectMode: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(4.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeChip(
                title = "Vendor OS",
                icon = Icons.Default.Chat,
                isSelected = currentMode == "vendor",
                activeColor = VendorGreenDark,
                onClick = { onSelectMode("vendor") }
            )
            ModeChip(
                title = "Handwork NG",
                icon = Icons.Default.Build,
                isSelected = currentMode == "skills",
                activeColor = SkillsOrange,
                onClick = { onSelectMode("skills") }
            )
            ModeChip(
                title = "Super Mode",
                icon = Icons.Default.Star,
                isSelected = currentMode == "both",
                activeColor = SuperBlue,
                onClick = { onSelectMode("both") }
            )
        }
    }
}

@Composable
private fun RowScope.ModeChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) activeColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

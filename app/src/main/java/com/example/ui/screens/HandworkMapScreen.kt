package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.SkillMaster
import com.example.repository.AppRepository
import com.example.ui.theme.OPayGreen
import com.example.ui.theme.SkillsOrange
import com.example.ui.theme.SuperBlue
import com.example.ui.theme.TrustGold
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandworkMapScreen(
    repository: AppRepository,
    onBack: () -> Unit,
    onViewProfile: (SkillMaster) -> Unit
) {
    val context = LocalContext.current
    val user by repository.userProfile.collectAsState()
    val masters by repository.masters.collectAsState()

    var radiusKm by remember { mutableStateOf(10f) }
    var selectedMaster by remember { mutableStateOf<SkillMaster?>(null) }

    val stateMasters = remember(masters, user.selectedState) {
        masters.filter { it.state.equals(user.selectedState, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artisan Radar & Map", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Radar centered on ${user.selectedArea}, ${user.selectedState}", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "My Location", tint = SkillsOrange)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Interactive Map Radar Canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                // Tap nearest pin
                                selectedMaster = stateMasters.minByOrNull { master ->
                                    val idx = stateMasters.indexOf(master)
                                    val angle = idx * (2 * Math.PI / stateMasters.size.coerceAtLeast(1))
                                    val r = 160f + (idx % 3) * 60f
                                    val cx = size.width / 2 + (r * Math.cos(angle)).toFloat()
                                    val cy = size.height / 2 + (r * Math.sin(angle)).toFloat()
                                    val dx = offset.x - cx
                                    val dy = offset.y - cy
                                    dx * dx + dy * dy
                                }
                            }
                        }
                ) {
                    val center = Offset(size.width / 2, size.height / 2)

                    // Draw concentric radar distance circles
                    val maxRadius = size.width.coerceAtMost(size.height) * 0.42f
                    for (i in 1..4) {
                        val r = maxRadius * (i / 4f)
                        drawCircle(
                            color = Color(0xFF1E293B),
                            radius = r,
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    // Center user position
                    drawCircle(
                        color = Color(0xFF38BDF8),
                        radius = 10.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = center
                    )

                    // Draw artisan marker pins around center
                    stateMasters.forEachIndexed { idx, master ->
                        val angle = idx * (2 * Math.PI / stateMasters.size.coerceAtLeast(1))
                        val distanceFactor = (idx % 4 + 1) / 4f
                        val distRadius = maxRadius * distanceFactor * (radiusKm / 20f)
                        val pinX = center.x + (distRadius * Math.cos(angle)).toFloat()
                        val pinY = center.y + (distRadius * Math.sin(angle)).toFloat()
                        val pinCenter = Offset(pinX, pinY)

                        val pinColor = when (master.skillCategory) {
                            "Barber" -> SkillsOrange
                            "Tailoring" -> SuperBlue
                            "Phone Repair" -> Color(0xFF10B981)
                            "Makeup" -> Color(0xFFEC4899)
                            "Solar Installation" -> TrustGold
                            else -> Color(0xFF8B5CF6)
                        }

                        // Glow
                        drawCircle(
                            color = pinColor.copy(alpha = 0.3f),
                            radius = 16.dp.toPx(),
                            center = pinCenter
                        )
                        // Pin Dot
                        drawCircle(
                            color = pinColor,
                            radius = 9.dp.toPx(),
                            center = pinCenter
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = pinCenter
                        )
                    }
                }

                // Legend & Location Header
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    Text(
                        "${user.selectedArea}, ${user.selectedState} • ${stateMasters.size} Masters Nearby",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Radius Slider Controls (1km - 20km)
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Search Radius", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("${radiusKm.toInt()} km", fontWeight = FontWeight.Black, color = SkillsOrange)
                    }

                    Slider(
                        value = radiusKm,
                        onValueChange = { radiusKm = it },
                        valueRange = 1f..20f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = SkillsOrange,
                            activeTrackColor = SkillsOrange
                        )
                    )

                    // Bottom sheet for selected master
                    AnimatedVisibility(visible = selectedMaster != null) {
                        val m = selectedMaster ?: return@AnimatedVisibility
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                    ) {
                                        if (m.images.isNotEmpty()) {
                                            AsyncImage(
                                                model = m.images.first(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(m.businessName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${m.skillName} • ${m.area}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = TrustGold, modifier = Modifier.size(13.dp))
                                            Spacer(Modifier.width(2.dp))
                                            Text("%.1f".format(m.rating), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.width(8.dp))
                                            Text("₦%,d/month".format(m.pricePerMonth), color = SkillsOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${m.phone}"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Call")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val text = "Hi ${m.ownerName}, I saw your ${m.skillName} workshop on VendorOS HandworkNG."
                                            val uri = Uri.parse("https://wa.me/${m.whatsapp}?text=${URLEncoder.encode(text, "UTF-8")}")
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF25D366))
                                        Spacer(Modifier.width(4.dp))
                                        Text("WhatsApp")
                                    }

                                    Button(
                                        onClick = { onViewProfile(m) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SkillsOrange)
                                    ) {
                                        Text("Profile")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

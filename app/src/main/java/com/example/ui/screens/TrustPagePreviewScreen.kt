package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.DeliveryProof
import com.example.repository.AppRepository
import com.example.ui.components.QrCodeView
import com.example.ui.theme.OPayGreen
import com.example.ui.theme.SuperBlue
import com.example.ui.theme.TrustGold
import com.example.ui.theme.VendorGreenDark
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustPagePreviewScreen(
    repository: AppRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val user by repository.userProfile.collectAsState()
    val proofs by repository.proofs.collectAsState()

    val publicSlug = remember(user.businessName, user.uid) {
        val cleanName = user.businessName.lowercase().replace(" ", "-").replace("[^a-z0-9-]".toRegex(), "")
        "$cleanName-${user.uid.takeLast(4)}"
    }
    val publicUrl = "https://vendoros.ng/v/$publicSlug"

    val confirmedProofs = remember(proofs) {
        proofs.filter { it.status == "confirmed" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Public Trust Badge Page", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Hero Banner
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF1E40AF))),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = user.businessName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = OPayGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "100% Verified Vendor Badge",
                                color = OPayGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Large Trust Score Box
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                "%.1f".format(user.trustScore),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Row {
                                    repeat(5) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = TrustGold, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Text(
                                    "${user.verifiedDeliveries} Successful Waybills & Deliveries",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        // QR Code
                        QrCodeView(data = publicUrl, sizeDp = 150)

                        Spacer(Modifier.height(10.dp))

                        Text(
                            publicUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = SuperBlue,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(16.dp))

                        // Share Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Trust Page", publicUrl))
                                    Toast.makeText(context, "Trust Page URL Copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Copy Link")
                            }

                            Button(
                                onClick = {
                                    try {
                                        val text = "Check my verified deliveries and trust score on VendorOS: $publicUrl"
                                        val uri = Uri.parse("https://wa.me/?text=${URLEncoder.encode(text, "UTF-8")}")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Sharing link...", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VendorGreenDark)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("WhatsApp")
                            }
                        }
                    }
                }
            }

            // Confirmed Deliveries List
            item {
                Text(
                    "Verified Customer Orders (${confirmedProofs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(confirmedProofs) { proof ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = proof.photoUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(proof.orderId, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Default.Verified, contentDescription = null, tint = OPayGreen, modifier = Modifier.size(14.dp))
                            }
                            Text(
                                "Customer: ${proof.customerName} (${proof.customerPhoneMasked})",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (proof.comment.isNotBlank()) {
                                Text(
                                    "\"${proof.comment}\"",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

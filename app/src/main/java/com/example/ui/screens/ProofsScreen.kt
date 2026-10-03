package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.DeliveryProof
import com.example.repository.AppRepository
import com.example.ui.components.QrCodeView
import com.example.ui.theme.OPayGreen
import com.example.ui.theme.SuperBlue
import com.example.ui.theme.TrustGold
import com.example.ui.theme.VendorGreenDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProofsScreen(
    repository: AppRepository,
    onNavigateToTrustPreview: () -> Unit
) {
    val context = LocalContext.current
    val proofs by repository.proofs.collectAsState()
    val user by repository.userProfile.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: All, 1: Pending, 2: Confirmed
    var selectedProofForDetail by remember { mutableStateOf<DeliveryProof?>(null) }
    var showCreateProofDialog by remember { mutableStateOf(false) }
    var showSuccessQrDialog by remember { mutableStateOf<DeliveryProof?>(null) }

    val filteredProofs = remember(proofs, selectedTab) {
        when (selectedTab) {
            1 -> proofs.filter { it.status == "pending" }
            2 -> proofs.filter { it.status == "confirmed" }
            else -> proofs
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trust Proofs & Delivery", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = onNavigateToTrustPreview) {
                        Text("Public Page", fontWeight = FontWeight.Bold, color = SuperBlue)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateProofDialog = true },
                containerColor = VendorGreenDark,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.AddPhotoAlternate, contentDescription = null) },
                text = { Text("Upload Proof", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All (${proofs.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Pending (${proofs.count { it.status == "pending" }})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Confirmed (${proofs.count { it.status == "confirmed" }})", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(Modifier.height(12.dp))

            if (filteredProofs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("No Proofs in this tab", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Tap 'Upload Proof' below to add a dispatch receipt.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProofs) { proof ->
                        ProofItemCard(
                            proof = proof,
                            onClick = { selectedProofForDetail = proof }
                        )
                    }
                    item {
                        Spacer(Modifier.height(64.dp))
                    }
                }
            }
        }
    }

    // Detail BottomSheet / Dialog
    if (selectedProofForDetail != null) {
        val proof = selectedProofForDetail!!
        Dialog(onDismissRequest = { selectedProofForDetail = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(proof.orderId, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                            Text(
                                if (proof.status == "confirmed") "Verified Delivery" else "Pending Customer Confirmation",
                                color = if (proof.status == "confirmed") OPayGreen else TrustGold,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { selectedProofForDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Proof Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = proof.photoUrl,
                            contentDescription = "Order Proof",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Text("Customer: ${proof.customerName}", fontWeight = FontWeight.Bold)
                    Text("Phone: ${proof.customerPhoneMasked} (Private)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (proof.comment.isNotBlank()) {
                        Text("Comment: \"${proof.comment}\"", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    // QR / Public Link preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Proof Link", proof.publicLink))
                                Toast.makeText(context, "Proof Link Copied: ${proof.publicLink}", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Copy Link")
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "See my verified delivery on VendorOS: ${proof.publicLink}")
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Proof Link"))
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuperBlue)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share Proof")
                        }
                    }

                    if (proof.status == "pending") {
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                repository.confirmProof(proof.proofId)
                                selectedProofForDetail = null
                                Toast.makeText(context, "Proof Confirmed! Trust Score Updated.", Toast.LENGTH_LONG).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = OPayGreen)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Simulate Customer Confirmation")
                        }
                    }
                }
            }
        }
    }

    // Create Proof Dialog
    if (showCreateProofDialog) {
        CreateProofDialog(
            onDismiss = { showCreateProofDialog = false },
            onSubmit = { customerName, phone, orderId, photoUrl ->
                val newProof = repository.addProof(customerName, phone, orderId, photoUrl)
                showCreateProofDialog = false
                showSuccessQrDialog = newProof
            }
        )
    }

    // Success QR Dialog
    if (showSuccessQrDialog != null) {
        val p = showSuccessQrDialog!!
        Dialog(onDismissRequest = { showSuccessQrDialog = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).background(OPayGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = OPayGreen)
                    }

                    Spacer(Modifier.height(10.dp))

                    Text("Proof Created Successfully!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Order: ${p.orderId}", color = MaterialTheme.colorScheme.outline)

                    Spacer(Modifier.height(16.dp))

                    QrCodeView(data = p.publicLink, sizeDp = 160)

                    Spacer(Modifier.height(12.dp))

                    Text(
                        p.publicLink,
                        style = MaterialTheme.typography.bodySmall,
                        color = SuperBlue,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Hello ${p.customerName}, your order ${p.orderId} dispatch proof: ${p.publicLink}")
                                }
                                context.startActivity(Intent.createChooser(intent, "Share with customer"))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Share Link")
                        }

                        Button(
                            onClick = { showSuccessQrDialog = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VendorGreenDark)
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProofItemCard(
    proof: DeliveryProof,
    onClick: () -> Unit
) {
    val dateStr = remember(proof.createdAt) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(proof.createdAt))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(proof.orderId, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Surface(
                        color = if (proof.status == "confirmed") OPayGreen.copy(alpha = 0.15f) else TrustGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (proof.status == "confirmed") "CONFIRMED" else "PENDING",
                            color = if (proof.status == "confirmed") OPayGreen else TrustGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "Delivered to: ${proof.customerName} (${proof.customerPhoneMasked})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun CreateProofDialog(
    onDismiss: () -> Unit,
    onSubmit: (customerName: String, customerPhone: String, orderId: String, photoUrl: String) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var orderId by remember { mutableStateOf("ORD-${(10000..99999).random()}") }
    var selectedPhotoUrl by remember {
        mutableStateOf("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop&q=80")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Create Delivery Proof", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Customer Phone (Kept Private) *") },
                    placeholder = { Text("08012345678") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = orderId,
                    onValueChange = { orderId = it },
                    label = { Text("Order ID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(14.dp))

                // Dashed border photo selector
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable {
                            // Cycle through high-res order sample images
                            val samplePhotos = listOf(
                                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80",
                                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80",
                                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop&q=80"
                            )
                            selectedPhotoUrl = samplePhotos.random()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = selectedPhotoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Dispatch Photo Attached", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Tap to change / snap new photo", style = MaterialTheme.typography.labelSmall, color = SuperBlue)
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

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
                            if (customerName.isNotBlank() && customerPhone.isNotBlank()) {
                                onSubmit(customerName, customerPhone, orderId, selectedPhotoUrl)
                            }
                        },
                        enabled = customerName.isNotBlank() && customerPhone.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VendorGreenDark)
                    ) {
                        Text("Submit & Generate QR")
                    }
                }
            }
        }
    }
}

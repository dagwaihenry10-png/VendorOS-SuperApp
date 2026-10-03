package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeliveryProof
import com.example.model.MessageLog
import com.example.model.UserProfile
import com.example.repository.AppRepository
import com.example.services.AiReplyService
import com.example.ui.components.StateLgaSelectorDialog
import com.example.ui.theme.OPayGreen
import com.example.ui.theme.SuperBlue
import com.example.ui.theme.TrustGold
import com.example.ui.theme.VendorGreen
import com.example.ui.theme.VendorGreenDark
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(
    repository: AppRepository,
    onNavigateToRules: () -> Unit,
    onNavigateToProofs: () -> Unit,
    onNavigateToTrustPreview: () -> Unit,
    onCreateProofClick: () -> Unit
) {
    val user by repository.userProfile.collectAsState()
    val proofs by repository.proofs.collectAsState()
    val logs by repository.logs.collectAsState()
    val scope = rememberCoroutineScope()

    var showLocationDialog by remember { mutableStateOf(false) }

    // Test AI simulator state
    var testCustomerMsg by remember { mutableStateOf("How much for delivery to Lekki? I want order now.") }
    var generatedAiReply by remember { mutableStateOf("") }
    var isGeneratingReply by remember { mutableStateOf(false) }

    val quickReplies = remember(user.businessName) {
        AiReplyService.generateQuickReplies(user.businessName)
    }

    if (showLocationDialog) {
        StateLgaSelectorDialog(
            currentState = user.selectedState,
            currentLga = user.selectedLGA,
            currentArea = user.selectedArea,
            onDismiss = { showLocationDialog = false },
            onSave = { state, lga, area ->
                repository.updateSelectedLocation(state, lga, area)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = user.businessName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showLocationDialog = true }
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = VendorGreenDark
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = "${user.selectedArea}, ${user.selectedState} ▾",
                                style = MaterialTheme.typography.labelSmall,
                                color = VendorGreenDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        color = if (user.isPro) OPayGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (user.isPro) "PRO ACTIVE" else "FREE TIER",
                            color = if (user.isPro) OPayGreen else MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateProofClick,
                containerColor = VendorGreenDark,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.AddPhotoAlternate, contentDescription = null) },
                text = { Text("Create Proof", fontWeight = FontWeight.Bold) }
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
            // Card 1: Auto-Reply Status Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(VendorGreenDark.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = VendorGreenDark,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (user.autoReplyOn) "AUTO-REPLY ACTIVE" else "AUTO-REPLY PAUSED",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (user.autoReplyOn) VendorGreenDark else MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "Hours: ${user.workingHoursStart} - ${user.workingHoursEnd}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = user.autoReplyOn,
                                onCheckedChange = { repository.toggleAutoReply(it) }
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = SuperBlue, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("AI Sales Persuasion", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("Converts doubts to immediate orders", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }

                            Switch(
                                checked = user.aiModeEnabled,
                                onCheckedChange = { repository.toggleAiMode(it) }
                            )
                        }
                    }
                }
            }

            // Card 2: Trust Score Card (Gradient #10B981 to #1E40AF)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF10B981), Color(0xFF1E40AF))
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "VENDOR TRUST SCORE",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "%.1f".format(user.trustScore),
                                            color = Color.White,
                                            fontSize = 38.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Row {
                                                repeat(5) {
                                                    Icon(
                                                        Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = TrustGold,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            if (user.verifiedDeliveries >= 10) {
                                                Text(
                                                    "🛡️ Verified Trusted Vendor",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = onNavigateToTrustPreview,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF1E40AF)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Public Badge", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            Text(
                                "${user.verifiedDeliveries} verified deliveries with zero fraud complaints. Customers order with confidence.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 2x2 Stats Grid
            item {
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "NG"))
                val salesSavedAmount = (logs.size.coerceAtLeast(1) * 500)

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Today Replied",
                            value = "${logs.size} chats",
                            subtitle = "Zero missed leads",
                            icon = Icons.Default.ChatBubbleOutline,
                            color = VendorGreenDark,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Sales Saved",
                            value = "₦%,d".format(salesSavedAmount),
                            subtitle = "Speed conversion",
                            icon = Icons.Default.Savings,
                            color = SuperBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Verified Deliveries",
                            value = "${user.verifiedDeliveries}",
                            subtitle = "100% Genuine",
                            icon = Icons.Default.CheckCircleOutline,
                            color = OPayGreen,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToProofs
                        )
                        StatCard(
                            title = "Pending Proofs",
                            value = "${user.pendingDeliveries}",
                            subtitle = "Awaiting confirm",
                            icon = Icons.Default.HourglassTop,
                            color = TrustGold,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToProofs
                        )
                    }
                }
            }

            // AI Persuasion Simulator Sandbox Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SuperBlue)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Test AI Persuasive Reply Sandbox",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "Simulate incoming WhatsApp customer questions to see how AI convinces them to place orders now.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        OutlinedTextField(
                            value = testCustomerMsg,
                            onValueChange = { testCustomerMsg = it },
                            label = { Text("Customer Message") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    isGeneratingReply = true
                                    generatedAiReply = AiReplyService.generateSmartReply(
                                        incomingMessage = testCustomerMsg,
                                        businessName = user.businessName,
                                        vendorState = user.selectedState
                                    )
                                    isGeneratingReply = false
                                }
                            },
                            enabled = !isGeneratingReply && testCustomerMsg.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SuperBlue)
                        ) {
                            if (isGeneratingReply) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Thinking with Gemini Flash...")
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Generate Persuasive Sales Reply")
                            }
                        }

                        AnimatedVisibility(visible = generatedAiReply.isNotBlank()) {
                            Surface(
                                color = VendorGreenDark.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "⚡ Persuasive Auto-Reply Output:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = VendorGreenDark
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        generatedAiReply,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Replies Chips
            item {
                Column {
                    Text(
                        "AI Quick Reply Templates",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickReplies) { chipText ->
                            SuggestionChip(
                                onClick = {
                                    testCustomerMsg = chipText
                                },
                                label = { Text(chipText, fontSize = 12.sp) },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }
            }

            // Mixed Recent Feed (Proofs & Logs)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Activity Feed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToProofs) {
                        Text("View Proofs")
                    }
                }
            }

            items(proofs.take(4)) { proof ->
                ProofSummaryCard(proof = proof, onConfirmClick = {
                    repository.confirmProof(proof.proofId)
                })
            }

            item {
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun ProofSummaryCard(
    proof: DeliveryProof,
    onConfirmClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (proof.status == "confirmed") OPayGreen.copy(alpha = 0.15f) else TrustGold.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (proof.status == "confirmed") Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (proof.status == "confirmed") OPayGreen else TrustGold
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(proof.orderId, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (proof.status == "confirmed") "Verified" else "Pending Confirmation",
                        color = if (proof.status == "confirmed") OPayGreen else TrustGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    "Customer: ${proof.customerName} (${proof.customerPhoneMasked})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (proof.status == "pending") {
                TextButton(onClick = onConfirmClick) {
                    Text("Confirm", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OPayGreen)
                }
            }
        }
    }
}

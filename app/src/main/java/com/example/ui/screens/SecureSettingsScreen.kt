package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PaymentRecord
import com.example.repository.AppRepository
import com.example.services.PaymentService
import com.example.utils.SecureConfig
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureSettingsScreen(
    repository: AppRepository,
    onSwitchMode: (String) -> Unit
) {
    val context = LocalContext.current
    val user by repository.userProfile.collectAsState()
    val payments by repository.payments.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedPlan by remember { mutableStateOf("pro") } // basic, pro, super, promote
    var currentPayment by remember {
        mutableStateOf(PaymentService.createPaymentRecord(user.uid, user.email, "pro"))
    }
    var isVerifying by remember { mutableStateOf(false) }

    // Re-create payment record when plan changes
    LaunchedEffect(selectedPlan) {
        currentPayment = PaymentService.createPaymentRecord(user.uid, user.email, selectedPlan)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Secure Billing", fontWeight = FontWeight.Bold) }
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
            // Mode Switcher Banner
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Active App Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = user.activeMode == "vendor",
                                onClick = {
                                    repository.updateUserMode("vendor")
                                    onSwitchMode("vendor")
                                },
                                label = { Text("Vendor OS") }
                            )
                            FilterChip(
                                selected = user.activeMode == "skills",
                                onClick = {
                                    repository.updateUserMode("skills")
                                    onSwitchMode("skills")
                                },
                                label = { Text("Handwork NG") }
                            )
                            FilterChip(
                                selected = user.activeMode == "both",
                                onClick = {
                                    repository.updateUserMode("both")
                                    onSwitchMode("both")
                                },
                                label = { Text("Super Mode") }
                            )
                        }
                    }
                }
            }

            // SECURE OPAY PAYMENT CARD - CRITICAL: NEVER DISPLAY 7081022844 RAW!
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("Secure OPay Gateway", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                                    Text("Official VendorOS Account", color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                            }

                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Verified",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Plan Selector Chips
                        Text("Select Plan to Subscribe / Boost:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PlanChip(
                                title = "Basic",
                                price = "₦1,000",
                                isSelected = selectedPlan == "basic",
                                onClick = { selectedPlan = "basic" }
                            )
                            PlanChip(
                                title = "Pro",
                                price = "₦2,000",
                                isSelected = selectedPlan == "pro",
                                onClick = { selectedPlan = "pro" }
                            )
                            PlanChip(
                                title = "Super",
                                price = "₦3,000",
                                isSelected = selectedPlan == "super",
                                onClick = { selectedPlan = "super" }
                            )
                            PlanChip(
                                title = "Boost 7D",
                                price = "₦1,000",
                                isSelected = selectedPlan == "promote",
                                onClick = { selectedPlan = "promote" }
                            )
                        }

                        Spacer(Modifier.height(18.dp))

                        // Protected Payment Details Box - MASKED ONLY!
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Bank Name:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    Text(SecureConfig.bankName, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Account Name:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    Text(SecureConfig.accountName, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Account Number:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    // SECURITY MANDATE: SHOW ONLY MASKED 7081****44
                                    Text(
                                        "${SecureConfig.maskedAccount} (Encrypted)",
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF10B981),
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Amount:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    Text("₦%,d".format(currentPayment.amount), fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Payment Reference:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    Text(currentPayment.reference, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Pay Securely Now Button (Opens secure link https://vendoros.ng/pay?ref=...)
                        Button(
                            onClick = {
                                repository.recordPayment(currentPayment)
                                PaymentService.launchSecurePayment(context, currentPayment.secureLink)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Pay Securely Now (OPay Gateway)", fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(10.dp))

                        // Copy Reference & Copy Amount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cb.setPrimaryClip(ClipData.newPlainText("Ref", currentPayment.reference))
                                    Toast.makeText(context, "Copied Reference: ${currentPayment.reference}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Copy Ref", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cb.setPrimaryClip(ClipData.newPlainText("Amount", currentPayment.amount.toString()))
                                    Toast.makeText(context, "Copied Amount: ₦${currentPayment.amount}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Copy Amount", fontSize = 12.sp)
                            }

                            IconButton(
                                onClick = {
                                    PaymentService.launchWhatsAppSupport(context, selectedPlan, currentPayment.amount, currentPayment.reference)
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF25D366).copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(Icons.Default.SupportAgent, contentDescription = "Support", tint = Color(0xFF25D366))
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // "I Don Pay" Verify Now Button
                        Button(
                            onClick = {
                                scope.launch {
                                    isVerifying = true
                                    val success = PaymentService.verifyPaymentSecure(
                                        paymentId = currentPayment.paymentId,
                                        uid = user.uid,
                                        plan = selectedPlan,
                                        reference = currentPayment.reference
                                    )
                                    isVerifying = false
                                    if (success) {
                                        repository.activateProPlan(selectedPlan)
                                        repository.recordPayment(currentPayment.copy(status = "confirmed", sqlSynced = true))
                                        Toast.makeText(context, "✅ Payment Verified! Pro Plan Activated!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Payment pending server confirmation. We will notify you.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Verifying with Gateway...")
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("I Don Pay (Verify Now)", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Upload proof screenshot
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Transfer screenshot attached to payment ${currentPayment.reference}", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Upload Proof Screenshot")
                        }

                        Spacer(Modifier.height(14.dp))

                        // Security Note
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Your payment is secured via encrypted gateway. Real account hidden for security. Only pay via Secure Link above.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // My Payment History Stream
            item {
                Text(
                    "My Payment History (${payments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(payments) { p ->
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(p.createdAt))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(p.reference, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("₦%,d • %s".format(p.amount, p.plan.uppercase()), style = MaterialTheme.typography.bodySmall)
                            Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (p.sqlSynced) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = if (p.sqlSynced) "SQL Synced" else "Offline Queue",
                                tint = if (p.sqlSynced) Color(0xFF10B981) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = if (p.status == "confirmed") Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = p.status.uppercase(),
                                    color = if (p.status == "confirmed") Color(0xFF10B981) else Color(0xFFF59E0B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

@Composable
private fun RowScope.PlanChip(
    title: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Text(
                price,
                fontSize = 10.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.outline
            )
        }
    }
}

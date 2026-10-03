package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AutoRule
import com.example.model.MessageLog
import com.example.repository.AppRepository
import com.example.services.AiReplyService
import com.example.ui.theme.OPayGreen
import com.example.ui.theme.SuperBlue
import com.example.ui.theme.VendorGreenDark
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    repository: AppRepository
) {
    val context = LocalContext.current
    val rules by repository.rules.collectAsState()
    val user by repository.userProfile.collectAsState()
    val logs by repository.logs.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var showLogsDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<AutoRule?>(null) }

    // Pre-fill fields for add/edit dialog
    var keywordsInput by remember { mutableStateOf("") }
    var replyTextInput by remember { mutableStateOf("") }
    var aiEnhanceChecked by remember { mutableStateOf(true) }
    var ruleActiveChecked by remember { mutableStateOf(true) }

    // Templates Grid (8 templates)
    val templates = listOf(
        "Price List" to "Our price starts from ₦5,000 for standard quality. Premium starts from ₦12,000. Delivery today across town! 🏷️",
        "Account Details" to "For zero-fraud payment, pay securely via our official VendorOS encrypted payment link. Link sent below! 🔒",
        "Store Hours" to "We are open Monday to Saturday from 08:00 AM to 09:00 PM. Orders placed now dispatch today! ⏰",
        "Location / Office" to "We dey ${user.selectedArea}, ${user.selectedState}. We also deliver nationwide with trusted waybills. 📍",
        "Delivery Info" to "Same-day delivery within ${user.selectedState} (₦1,500 - ₦2,500). Interstate delivery takes 24-48 hours. 🚚",
        "Thank You" to "Thank you for shopping with ${user.businessName}! Your order is packed and ready for dispatch. 🙏",
        "Payment Details" to "Secure bank transfer accepted via OPay / VendorOS verified gateway. Instant confirmation. 💳",
        "Follow-up" to "Hello boss! Hope you received your package intact? Kindly drop your review on our Trust Badge page! ⭐"
    )

    fun openDialogWithTemplate(title: String, text: String) {
        val defaultKeyword = when (title) {
            "Price List" -> "price, how much, cost"
            "Account Details" -> "account, pay, transfer"
            "Store Hours" -> "hours, time, open"
            "Location / Office" -> "where, location, shop"
            "Delivery Info" -> "delivery, waybill, send"
            "Thank You" -> "thanks, received, got it"
            "Payment Details" -> "details, bank, opay"
            else -> "followup, review"
        }
        editingRule = null
        keywordsInput = defaultKeyword
        replyTextInput = text
        aiEnhanceChecked = true
        ruleActiveChecked = true
        showAddDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto Rules & AI", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showLogsDialog = true }) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "Logs", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        editingRule = null
                        keywordsInput = ""
                        replyTextInput = ""
                        aiEnhanceChecked = true
                        ruleActiveChecked = true
                        showAddDialog = true
                    }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Rule", tint = VendorGreenDark)
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
            // Notification Access / WhatsApp Auto-Detection Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperBlue.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(SuperBlue.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SuperBlue)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Auto-Detect WhatsApp Messages",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "VendorOS listens for incoming customer messages and responds within 2 seconds.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "Notification Auto-Reply Service Enabled", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Enable", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Working Hours Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = VendorGreenDark)
                            Spacer(Modifier.width(8.dp))
                            Text("Active Auto-Reply Hours", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "From ${user.workingHoursStart} to ${user.workingHoursEnd} daily",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = {
                                Toast.makeText(context, "Working hours configured for 08:00 - 21:00", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Change", color = VendorGreenDark)
                            }
                        }
                    }
                }
            }

            // Quick Template Picker
            item {
                Column {
                    Text("Instant Rule Templates", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(180.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(templates) { (title, text) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { openDialogWithTemplate(title, text) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp), tint = VendorGreenDark)
                                    Spacer(Modifier.width(6.dp))
                                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            // Active Rules List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Configured Rules (${rules.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = {
                        editingRule = null
                        keywordsInput = ""
                        replyTextInput = ""
                        aiEnhanceChecked = true
                        ruleActiveChecked = true
                        showAddDialog = true
                    }) {
                        Text("+ New Rule", color = VendorGreenDark, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(rules) { rule ->
                RuleCard(
                    rule = rule,
                    onToggleActive = { repository.toggleRuleActive(rule.ruleId) },
                    onEdit = {
                        editingRule = rule
                        keywordsInput = rule.keywords.joinToString(", ")
                        replyTextInput = rule.replyText
                        aiEnhanceChecked = rule.aiEnhanced
                        ruleActiveChecked = rule.isActive
                        showAddDialog = true
                    },
                    onDelete = { repository.deleteRule(rule.ruleId) }
                )
            }

            item {
                Spacer(Modifier.height(48.dp))
            }
        }
    }

    // Add / Edit Rule Dialog
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        if (editingRule != null) "Edit Auto-Reply Rule" else "Add New Auto-Reply Rule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = keywordsInput,
                        onValueChange = { keywordsInput = it },
                        label = { Text("Keywords (comma separated)") },
                        placeholder = { Text("e.g. price, cost, how much") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = replyTextInput,
                        onValueChange = { replyTextInput = it },
                        label = { Text("Reply Message (Nigerian Pidgin+English)") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = aiEnhanceChecked,
                                onCheckedChange = { aiEnhanceChecked = it }
                            )
                            Text("AI Persuasive Polish", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }

                        if (aiEnhanceChecked) {
                            TextButton(onClick = {
                                scope.launch {
                                    val polished = AiReplyService.generateSmartReply(
                                        incomingMessage = keywordsInput,
                                        businessName = user.businessName,
                                        vendorState = user.selectedState
                                    )
                                    replyTextInput = polished
                                }
                            }) {
                                Text("Enhance", color = SuperBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = ruleActiveChecked,
                            onCheckedChange = { ruleActiveChecked = it }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (ruleActiveChecked) "Rule Active" else "Rule Inactive", style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val kwList = keywordsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                if (kwList.isEmpty() || replyTextInput.isBlank()) {
                                    Toast.makeText(context, "Please enter keywords and reply text", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val newRule = AutoRule(
                                    ruleId = editingRule?.ruleId ?: "rule_${UUID.randomUUID().toString().take(6)}",
                                    userId = user.uid,
                                    keywords = kwList,
                                    replyText = replyTextInput.trim(),
                                    type = "keyword",
                                    isActive = ruleActiveChecked,
                                    triggeredCount = editingRule?.triggeredCount ?: 0,
                                    aiEnhanced = aiEnhanceChecked
                                )
                                if (editingRule != null) {
                                    repository.updateRule(newRule)
                                } else {
                                    repository.addRule(newRule)
                                }
                                showAddDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VendorGreenDark)
                        ) {
                            Text("Save Rule")
                        }
                    }
                }
            }
        }
    }

    // Chat Logs Viewer Dialog
    if (showLogsDialog) {
        Dialog(onDismissRequest = { showLogsDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-Reply History Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showLogsDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(logs) { log ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(log.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        if (log.aiUsed) {
                                            Surface(
                                                color = SuperBlue.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("AI Used", color = SuperBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("💬 \"${log.incomingMessage}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    Spacer(Modifier.height(4.dp))
                                    Text("⚡ ${log.repliedWith}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleCard(
    rule: AutoRule,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = VendorGreenDark.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "IF CONTAINS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VendorGreenDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (rule.aiEnhanced) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = SuperBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "✨ AI Enhanced",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuperBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Switch(
                    checked = rule.isActive,
                    onCheckedChange = { onToggleActive() }
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = rule.keywords.joinToString("  •  "),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Then reply: ${rule.replyText}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Triggered ${rule.triggeredCount} times",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

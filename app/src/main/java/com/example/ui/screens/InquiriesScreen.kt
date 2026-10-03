package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Inquiry
import com.example.repository.AppRepository
import com.example.ui.theme.OPayGreen
import com.example.ui.theme.SkillsOrange
import com.example.ui.theme.SuperBlue
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquiriesScreen(
    repository: AppRepository
) {
    val context = LocalContext.current
    val inquiries by repository.inquiries.collectAsState()
    val user by repository.userProfile.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Sent, 1: Received

    val sentInquiries = remember(inquiries, user.uid) {
        inquiries.filter { it.learnerId == user.uid }
    }

    val receivedInquiries = remember(inquiries, user.uid) {
        inquiries.filter { it.masterId == "master_1" || it.masterId == user.uid }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apprenticeship Inquiries", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Sent (${sentInquiries.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Received (${receivedInquiries.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(Modifier.height(12.dp))

            val currentList = if (selectedTab == 0) sentInquiries else receivedInquiries

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            if (selectedTab == 0) "No inquiries sent yet" else "No inquiries received yet",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (selectedTab == 0) "Browse artisan masters and tap 'Request to Learn'." else "Your workshop inquiries will show up here.",
                            style = MaterialTheme.typography.bodySmall,
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
                    items(currentList) { inq ->
                        InquiryCard(
                            inquiry = inq,
                            isSent = selectedTab == 0,
                            onUpdateStatus = { newStatus ->
                                repository.updateInquiryStatus(inq.inquiryId, newStatus)
                            }
                        )
                    }
                    item {
                        Spacer(Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InquiryCard(
    inquiry: Inquiry,
    isSent: Boolean,
    onUpdateStatus: (String) -> Unit
) {
    val context = LocalContext.current
    val dateStr = remember(inquiry.createdAt) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(inquiry.createdAt))
    }

    val statusColor = when (inquiry.status) {
        "accepted" -> OPayGreen
        "completed" -> SuperBlue
        "rejected" -> MaterialTheme.colorScheme.error
        else -> SkillsOrange
    }

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
                Column {
                    Text(
                        if (isSent) inquiry.masterName else inquiry.learnerName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text("Skill: ${inquiry.skill}", style = MaterialTheme.typography.bodySmall, color = SkillsOrange)
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        inquiry.status.uppercase(),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "\"${inquiry.message}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)

            Spacer(Modifier.height(12.dp))

            // Action Buttons
            if (isSent) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:08023456789"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Call Master")
                    }

                    Button(
                        onClick = {
                            val text = "Hi, I sent an inquiry on VendorOS HandworkNG regarding learning ${inquiry.skill}."
                            val uri = Uri.parse("https://wa.me/2348023456789?text=${URLEncoder.encode(text, "UTF-8")}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("WhatsApp")
                    }
                }
            } else {
                // Received: Accept / Reject / Complete
                if (inquiry.status == "pending") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onUpdateStatus("rejected") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Decline")
                        }

                        Button(
                            onClick = {
                                onUpdateStatus("accepted")
                                val text = "Hi ${inquiry.learnerName}, your apprenticeship request on VendorOS HandworkNG has been accepted!"
                                val uri = Uri.parse("https://wa.me/234${inquiry.learnerPhone.takeLast(10)}?text=${URLEncoder.encode(text, "UTF-8")}")
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Request Accepted!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OPayGreen)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Accept")
                        }
                    }
                } else if (inquiry.status == "accepted") {
                    Button(
                        onClick = { onUpdateStatus("completed") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuperBlue)
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Mark Training as Completed")
                    }
                }
            }
        }
    }
}

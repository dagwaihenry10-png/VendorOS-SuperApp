package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.example.model.SkillMaster
import com.example.repository.AppRepository
import com.example.ui.theme.OPayGreen
import com.example.ui.theme.SkillsOrange
import com.example.ui.theme.TrustGold
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterDetailScreen(
    master: SkillMaster,
    repository: AppRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { master.images.size.coerceAtLeast(1) })
    var showInquiryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(master.masterId) {
        repository.incrementMasterViews(master.masterId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(master.businessName, fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Learn ${master.skillName} from ${master.businessName} on VendorOS: https://vendoros.ng/m/${master.masterId}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Master Profile"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Apprenticeship Fee", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text("₦%,d/mo".format(master.pricePerMonth), fontWeight = FontWeight.Black, fontSize = 18.sp, color = SkillsOrange)
                    }

                    Button(
                        onClick = { showInquiryDialog = true },
                        modifier = Modifier.weight(1.4f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SkillsOrange)
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Request to Learn", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Image Carousel
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        val img = master.images.getOrNull(page) ?: "https://images.unsplash.com/photo-1503951914875-452162b0f3f1?w=600&auto=format&fit=crop&q=80"
                        AsyncImage(
                            model = img,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Indicators
                    if (master.images.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(master.images.size) { idx ->
                                Box(
                                    modifier = Modifier
                                        .padding(3.dp)
                                        .size(if (pagerState.currentPage == idx) 8.dp else 6.dp)
                                        .background(
                                            if (pagerState.currentPage == idx) Color.White else Color.White.copy(alpha = 0.5f),
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Info & Details
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = SkillsOrange.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                master.skillCategory,
                                color = SkillsOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (master.isPromoted) {
                                Surface(
                                    color = SkillsOrange,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text("PROMOTED", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            if (master.isVerified) {
                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = OPayGreen, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("Verified", color = OPayGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(master.businessName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("Trainer: ${master.ownerName} (${master.yearsExperience} yrs experience)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = TrustGold, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("%.1f (%d students trained)".format(master.rating, master.totalStudents), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Contact Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${master.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Call")
                        }

                        Button(
                            onClick = {
                                val text = "Hi ${master.ownerName}, I saw your workshop profile on VendorOS. I want to inquire about training for ${master.skillName}."
                                val uri = Uri.parse("https://wa.me/${master.whatsapp}?text=${URLEncoder.encode(text, "UTF-8")}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("WhatsApp")
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Description
                    Text("About Workshop & Training", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(master.description, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(18.dp))

                    // Pricing Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Tuition & Lodging Plans", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Monthly Training:")
                                Text("₦%,d".format(master.pricePerMonth), fontWeight = FontWeight.Bold, color = SkillsOrange)
                            }
                            if (master.pricePerWeek != null) {
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Weekly Option:")
                                    Text("₦%,d".format(master.pricePerWeek), fontWeight = FontWeight.Bold)
                                }
                            }
                            if (master.hasAccommodation) {
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Accommodation Fee:")
                                    Text("₦%,d/mo (Available)".format(master.accommodationFee), fontWeight = FontWeight.Bold, color = OPayGreen)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Location & Hours
                    Text("Workshop Address & Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SkillsOrange, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("${master.address}, ${master.area}, ${master.lga}, ${master.state}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(master.openingHours, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }

                    Spacer(Modifier.height(24.dp))

                    // Student Reviews
                    Text("Apprentice Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    ReviewCard(
                        author = "Tobi Alabi",
                        rating = 5,
                        comment = "Master trained me for 3 months in cutting and modern fades. Now I open my own shop in Ikeja. Best master!",
                        date = "2 weeks ago"
                    )
                    Spacer(Modifier.height(8.dp))
                    ReviewCard(
                        author = "Kazeem Bello",
                        rating = 5,
                        comment = "Very patient master. Accommodation was clean and secure. 100% recommended for serious apprentices.",
                        date = "1 month ago"
                    )

                    Spacer(Modifier.height(64.dp))
                }
            }
        }
    }

    // Request to Learn Inquiry Dialog
    if (showInquiryDialog) {
        var inquiryMessage by remember {
            mutableStateOf("Good day ${master.ownerName}, I want to learn ${master.skillName}. Is admission and space available for next week?")
        }
        Dialog(onDismissRequest = { showInquiryDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Request to Learn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("To: ${master.businessName}", color = SkillsOrange, style = MaterialTheme.typography.bodySmall)

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = inquiryMessage,
                        onValueChange = { inquiryMessage = it },
                        label = { Text("Your Message to Master") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showInquiryDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                repository.sendInquiry(
                                    masterId = master.masterId,
                                    masterName = master.businessName,
                                    skill = master.skillCategory,
                                    message = inquiryMessage
                                )
                                showInquiryDialog = false
                                Toast.makeText(context, "Inquiry Sent! The master will reach you on WhatsApp.", Toast.LENGTH_LONG).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SkillsOrange)
                        ) {
                            Text("Send Inquiry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    author: String,
    rating: Int,
    comment: String,
    date: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(author, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row {
                    repeat(rating) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = TrustGold, modifier = Modifier.size(13.dp))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("\"$comment\"", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(2.dp))
            Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

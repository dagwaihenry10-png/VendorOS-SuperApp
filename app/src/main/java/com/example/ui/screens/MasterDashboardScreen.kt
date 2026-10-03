package com.example.ui.screens

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
import coil.compose.AsyncImage
import com.example.data.NigeriaData
import com.example.model.PaymentRecord
import com.example.model.SkillMaster
import com.example.repository.AppRepository
import com.example.services.PaymentService
import com.example.ui.theme.OPayGreen
import com.example.ui.theme.SkillsOrange
import com.example.ui.theme.SuperBlue
import com.example.ui.theme.TrustGold
import com.example.utils.SecureConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterDashboardScreen(
    repository: AppRepository,
    onViewPublicProfile: (SkillMaster) -> Unit
) {
    val context = LocalContext.current
    val user by repository.userProfile.collectAsState()
    val masters by repository.masters.collectAsState()
    val inquiries by repository.inquiries.collectAsState()
    val scope = rememberCoroutineScope()

    // Check if the current user has already registered a master workshop
    val myMasterProfile = remember(masters, user.uid) {
        masters.find { it.userId == user.uid }
    }

    // Step state for Become a Master form
    var currentStep by remember { mutableStateOf(1) } // 1..4

    // Step 1: Business info
    var bName by remember { mutableStateOf(user.businessName) }
    var ownerName by remember { mutableStateOf(user.name) }
    var skillCat by remember { mutableStateOf(NigeriaData.allSkills.first()) }
    var yearsExp by remember { mutableStateOf("6") }
    var descText by remember { mutableStateOf("Practical hands-on master apprenticeship training with guaranteed tools mastery.") }

    // Step 2: Location
    var selState by remember { mutableStateOf(user.selectedState) }
    val lgasList = remember(selState) { NigeriaData.statesLGAs[selState] ?: emptyList() }
    var selLga by remember { mutableStateOf(lgasList.firstOrNull() ?: "") }
    var areaInput by remember { mutableStateOf(user.selectedArea) }
    var addressInput by remember { mutableStateOf("15 Commercial Avenue, Beside Market") }

    // Step 3: Pricing
    var monthlyPrice by remember { mutableStateOf("6000") }
    var weeklyPrice by remember { mutableStateOf("2000") }
    var hasAccom by remember { mutableStateOf(true) }
    var accomFee by remember { mutableStateOf("4000") }

    // Step 4: Photos
    val portfolioPhotos = remember {
        mutableStateListOf(
            "https://images.unsplash.com/photo-1503951914875-452162b0f3f1?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1585747860715-2ba37e788b70?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600&auto=format&fit=crop&q=80"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (myMasterProfile != null) "Master Artisan Hub" else "Become a Master", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (myMasterProfile == null) {
            // MULTI-STEP REGISTRATION WIZARD
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Stepper Header
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(4) { idx ->
                                val stepNum = idx + 1
                                val isActive = currentStep == stepNum
                                val isDone = currentStep > stepNum
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                if (isDone) OPayGreen else if (isActive) SkillsOrange else MaterialTheme.colorScheme.outlineVariant,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDone) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        } else {
                                            Text("$stepNum", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                    if (idx < 3) {
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(28.dp)
                                                .height(2.dp)
                                                .background(if (isDone) OPayGreen else MaterialTheme.colorScheme.outlineVariant)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Step Contents
                when (currentStep) {
                    1 -> item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Step 1: Workshop & Skill Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = bName,
                                    onValueChange = { bName = it },
                                    label = { Text("Workshop / Business Name *") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = ownerName,
                                    onValueChange = { ownerName = it },
                                    label = { Text("Master Trainer Full Name *") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(Modifier.height(10.dp))

                                // Skill Category Dropdown
                                var catExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = catExpanded,
                                    onExpandedChange = { catExpanded = !catExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = skillCat,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Primary Skill Category *") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = catExpanded,
                                        onDismissRequest = { catExpanded = false }
                                    ) {
                                        NigeriaData.allSkills.forEach { s ->
                                            DropdownMenuItem(
                                                text = { Text("${NigeriaData.getSkillIconEmoji(s)} $s") },
                                                onClick = {
                                                    skillCat = s
                                                    catExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = yearsExp,
                                    onValueChange = { yearsExp = it },
                                    label = { Text("Years of Experience *") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = descText,
                                    onValueChange = { descText = it },
                                    label = { Text("Apprenticeship Description *") },
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    maxLines = 4
                                )

                                Spacer(Modifier.height(18.dp))

                                Button(
                                    onClick = { currentStep = 2 },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SkillsOrange)
                                ) {
                                    Text("Next: Location Setup")
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                                }
                            }
                        }
                    }

                    2 -> item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Step 2: Workshop Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(14.dp))

                                // State selector
                                var stateExp by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = stateExp,
                                    onExpandedChange = { stateExp = !stateExp }
                                ) {
                                    OutlinedTextField(
                                        value = selState,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("State (36 States + FCT)") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExp) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = stateExp,
                                        onDismissRequest = { stateExp = false }
                                    ) {
                                        NigeriaData.statesLGAs.keys.forEach { st ->
                                            DropdownMenuItem(
                                                text = { Text(st) },
                                                onClick = {
                                                    selState = st
                                                    val newL = NigeriaData.statesLGAs[st] ?: emptyList()
                                                    selLga = newL.firstOrNull() ?: ""
                                                    stateExp = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                // LGA selector
                                var lgaExp by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = lgaExp,
                                    onExpandedChange = { lgaExp = !lgaExp }
                                ) {
                                    OutlinedTextField(
                                        value = selLga,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("LGA") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lgaExp) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = lgaExp,
                                        onDismissRequest = { lgaExp = false }
                                    ) {
                                        lgasList.forEach { l ->
                                            DropdownMenuItem(
                                                text = { Text(l) },
                                                onClick = {
                                                    selLga = l
                                                    lgaExp = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = areaInput,
                                    onValueChange = { areaInput = it },
                                    label = { Text("Area / District (e.g. Surulere, Ikeja, Garki)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = addressInput,
                                    onValueChange = { addressInput = it },
                                    label = { Text("Full Street Address") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Location coordinates locked to $selState ($selLga)", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = SkillsOrange)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Use Current GPS Location")
                                }

                                Spacer(Modifier.height(18.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { currentStep = 1 },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Back")
                                    }
                                    Button(
                                        onClick = { currentStep = 3 },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SkillsOrange)
                                    ) {
                                        Text("Next: Pricing")
                                    }
                                }
                            }
                        }
                    }

                    3 -> item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Step 3: Training Fees & Accommodation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = monthlyPrice,
                                    onValueChange = { monthlyPrice = it },
                                    label = { Text("Apprenticeship Fee per Month (₦) *") },
                                    placeholder = { Text("e.g. 6000") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = weeklyPrice,
                                    onValueChange = { weeklyPrice = it },
                                    label = { Text("Weekly Fee (Optional ₦)") },
                                    placeholder = { Text("e.g. 2000") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Accommodation Available?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Provides lodging for distant apprentices", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                    Switch(
                                        checked = hasAccom,
                                        onCheckedChange = { hasAccom = it }
                                    )
                                }

                                if (hasAccom) {
                                    Spacer(Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = accomFee,
                                        onValueChange = { accomFee = it },
                                        label = { Text("Accommodation Fee (₦/month)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                Spacer(Modifier.height(18.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { currentStep = 2 },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Back")
                                    }
                                    Button(
                                        onClick = { currentStep = 4 },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SkillsOrange)
                                    ) {
                                        Text("Next: Photos")
                                    }
                                }
                            }
                        }
                    }

                    4 -> item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Step 4: Workshop Photos & Submit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(14.dp))

                                Text("Workshop & Portfolio Photos (3 attached):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    portfolioPhotos.forEach { url ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(80.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            AsyncImage(
                                                model = url,
                                                contentDescription = null,
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(14.dp))

                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Photo picker attached 3 HD workshop images.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = SkillsOrange)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Add More Photos")
                                }

                                Spacer(Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        val newMaster = SkillMaster(
                                            masterId = "master_${user.uid}",
                                            userId = user.uid,
                                            businessName = bName,
                                            ownerName = ownerName,
                                            skillCategory = skillCat,
                                            skillName = "$skillCat Master Trainer",
                                            otherSkills = listOf("General Apprenticeship", "Shop Management"),
                                            yearsExperience = yearsExp.toIntOrNull() ?: 5,
                                            description = descText,
                                            state = selState,
                                            lga = selLga,
                                            area = areaInput,
                                            address = addressInput,
                                            pricePerMonth = monthlyPrice.toIntOrNull() ?: 5000,
                                            pricePerWeek = weeklyPrice.toIntOrNull() ?: 1500,
                                            hasAccommodation = hasAccom,
                                            accommodationFee = if (hasAccom) accomFee.toIntOrNull() ?: 0 else 0,
                                            images = portfolioPhotos.toList(),
                                            phone = user.phone,
                                            whatsapp = user.phone.replace("+", ""),
                                            isAvailable = true,
                                            rating = 5.0,
                                            totalStudents = 0,
                                            views = 1,
                                            isPromoted = false,
                                            isVerified = true,
                                            openingHours = "08:00 - 20:00 (Mon-Sat)"
                                        )
                                        repository.registerMaster(newMaster)
                                        Toast.makeText(context, "🎉 Congratulations! Your Master Workshop is Live!", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SkillsOrange)
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Submit Workshop Profile", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(48.dp))
                }
            }
        } else {
            // ALREADY REGISTERED MASTER HUB DASHBOARD
            val m = myMasterProfile
            val masterInquiries = remember(inquiries, m.masterId) {
                inquiries.filter { it.masterId == m.masterId || it.masterId == "master_1" }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Profile Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SkillsOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Handyman, contentDescription = null, tint = SkillsOrange, modifier = Modifier.size(32.dp))
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(m.businessName, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                                        if (m.isVerified) {
                                            Spacer(Modifier.width(4.dp))
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = OPayGreen, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Text("${m.skillName} • ${m.area}, ${m.state}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    if (m.isPromoted) {
                                        Surface(
                                            color = SkillsOrange,
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(
                                                "PROMOTED ACTIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onViewPublicProfile(m) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("View Profile")
                                }

                                Button(
                                    onClick = {
                                        // 7 Days Boost (₦1,000) via Secure OPay
                                        val boostPayment = PaymentService.createPaymentRecord(user.uid, user.email, "promote")
                                        repository.recordPayment(boostPayment)
                                        repository.promoteMaster(m.masterId)
                                        PaymentService.launchSecurePayment(context, boostPayment.secureLink)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SkillsOrange)
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Boost (₦1,000)")
                                }
                            }
                        }
                    }
                }

                // 2x2 Stats Grid for Master
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MasterStatBox(
                                title = "Profile Views",
                                value = "${m.views}",
                                icon = Icons.Default.Visibility,
                                color = SuperBlue,
                                modifier = Modifier.weight(1f)
                            )
                            MasterStatBox(
                                title = "Total Inquiries",
                                value = "${masterInquiries.size}",
                                icon = Icons.Default.MailOutline,
                                color = SkillsOrange,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MasterStatBox(
                                title = "Students Trained",
                                value = "${m.totalStudents}",
                                icon = Icons.Default.School,
                                color = OPayGreen,
                                modifier = Modifier.weight(1f)
                            )
                            MasterStatBox(
                                title = "Master Rating",
                                value = "%.1f ⭐".format(m.rating),
                                icon = Icons.Default.Star,
                                color = TrustGold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Pending Inquiries for Workshop
                item {
                    Text("Received Learner Inquiries (${masterInquiries.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(masterInquiries) { inq ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(inq.learnerName, fontWeight = FontWeight.Bold)
                                Surface(
                                    color = if (inq.status == "accepted") OPayGreen.copy(alpha = 0.15f) else SkillsOrange.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        inq.status.uppercase(),
                                        color = if (inq.status == "accepted") OPayGreen else SkillsOrange,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("\"${inq.message}\"", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
private fun MasterStatBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        }
    }
}

package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.NigeriaData
import com.example.model.SkillMaster
import com.example.repository.AppRepository
import com.example.ui.components.StateLgaSelectorDialog
import com.example.ui.theme.OPayGreen
import com.example.ui.theme.SkillsOrange
import com.example.ui.theme.TrustGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandworkHomeScreen(
    repository: AppRepository,
    onMasterClick: (SkillMaster) -> Unit,
    onBecomeMasterClick: () -> Unit,
    onToggleMapView: () -> Unit
) {
    val user by repository.userProfile.collectAsState()
    val masters by repository.masters.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showLocationDialog by remember { mutableStateOf(false) }

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

    // Promoted masters for selected state
    val promotedMasters = remember(masters, user.selectedState) {
        masters.filter { it.isPromoted && it.state.equals(user.selectedState, ignoreCase = true) }
            .ifEmpty { masters.filter { it.isPromoted } }
    }

    // Filtered masters list
    val filteredMasters = remember(masters, user.selectedState, searchQuery, selectedCategory) {
        masters.filter { master ->
            val matchState = master.state.equals(user.selectedState, ignoreCase = true)
            val matchCategory = selectedCategory == null || master.skillCategory.equals(selectedCategory, ignoreCase = true)
            val matchSearch = searchQuery.isBlank() ||
                    master.businessName.contains(searchQuery, ignoreCase = true) ||
                    master.skillCategory.contains(searchQuery, ignoreCase = true) ||
                    master.skillName.contains(searchQuery, ignoreCase = true) ||
                    master.lga.contains(searchQuery, ignoreCase = true) ||
                    master.area.contains(searchQuery, ignoreCase = true)
            matchState && matchCategory && matchSearch
        }.sortedByDescending { it.rating }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(SkillsOrange, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Handyman, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("HandworkNG", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { showLocationDialog = true }
                            ) {
                                Text(
                                    text = "${user.selectedState} State ▾",
                                    fontSize = 11.sp,
                                    color = SkillsOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onToggleMapView) {
                        Icon(Icons.Default.Map, contentDescription = "Map View", tint = SkillsOrange)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search skill e.g. Barber in Ikeja") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SkillsOrange) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            // Category Horizontal Chips (20 skills)
            item {
                Column {
                    Text(
                        "Artisan Skill Categories",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("All Skills") }
                            )
                        }
                        items(NigeriaData.allSkills) { skill ->
                            val icon = NigeriaData.getSkillIconEmoji(skill)
                            FilterChip(
                                selected = selectedCategory == skill,
                                onClick = {
                                    selectedCategory = if (selectedCategory == skill) null else skill
                                },
                                label = { Text("$icon $skill") }
                            )
                        }
                    }
                }
            }

            // Promoted Masters Carousel
            if (promotedMasters.isNotEmpty()) {
                item {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = SkillsOrange, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Top Verified Masters", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Text("Promoted", fontSize = 11.sp, color = SkillsOrange, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(promotedMasters) { master ->
                                PromotedMasterCard(master = master, onClick = { onMasterClick(master) })
                            }
                        }
                    }
                }
            }

            // Nearby Masters List
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Masters in ${user.selectedState} (${filteredMasters.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onToggleMapView) {
                        Text("Open Map", color = SkillsOrange, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (filteredMasters.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.BuildCircle, contentDescription = null, modifier = Modifier.size(48.dp), tint = SkillsOrange)
                            Spacer(Modifier.height(10.dp))
                            Text("No masters found in ${user.selectedState} yet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Be the first master in your area to train apprentices.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = onBecomeMasterClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SkillsOrange)
                            ) {
                                Text("Become a Master")
                            }
                        }
                    }
                }
            } else {
                items(filteredMasters) { master ->
                    MasterListItem(master = master, onClick = { onMasterClick(master) })
                }
            }

            item {
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun PromotedMasterCard(
    master: SkillMaster,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (master.images.isNotEmpty()) {
                    AsyncImage(
                        model = master.images.first(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Surface(
                    color = SkillsOrange,
                    shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 12.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        master.skillCategory,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(master.businessName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${master.area}, ${master.lga}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = TrustGold, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("%.1f".format(master.rating), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("₦%,d/mo".format(master.pricePerMonth), color = SkillsOrange, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun MasterListItem(
    master: SkillMaster,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (master.images.isNotEmpty()) {
                    AsyncImage(
                        model = master.images.first(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(master.businessName, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (master.isVerified) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, contentDescription = null, tint = OPayGreen, modifier = Modifier.size(14.dp))
                    }
                }

                Surface(
                    color = SkillsOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        master.skillName,
                        color = SkillsOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }

                Text(
                    "${master.area}, ${master.lga} • 1.2km away",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = TrustGold, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("%.1f (%d)".format(master.rating, master.totalStudents * 4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "From ₦%,d/mo".format(master.pricePerMonth),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SkillsOrange)
            ) {
                Text("View", fontSize = 12.sp)
            }
        }
    }
}

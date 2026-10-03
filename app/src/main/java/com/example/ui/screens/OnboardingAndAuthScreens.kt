package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SkillsOrange
import com.example.ui.theme.SuperBlue
import com.example.ui.theme.VendorGreen
import com.example.ui.theme.VendorGreenDark
import kotlinx.coroutines.launch

/**
 * Onboarding Slides for VendorOS (3-in-1 Super App)
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val slides = listOf(
        Triple(
            "Stop Losing Sales",
            "Auto-reply instantly on WhatsApp & Instagram with Nigerian Pidgin & English AI that convinces buyers to pay.",
            Icons.Default.FlashOn
        ),
        Triple(
            "Prove You Are Not Scam",
            "Generate verified delivery receipts, public Trust Badges, and QR codes that make customers trust you instantly.",
            Icons.Default.VerifiedUser
        ),
        Triple(
            "Find Handwork Nationwide",
            "Connect with top masters, learn lucrative artisan skills, or list your workshop across all 36 States + FCT.",
            Icons.Default.Handyman
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(VendorGreenDark, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "VendorOS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            TextButton(onClick = onFinish) {
                Text("Skip", color = MaterialTheme.colorScheme.outline)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val slide = slides[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            when (page) {
                                0 -> VendorGreenDark.copy(alpha = 0.15f)
                                1 -> SuperBlue.copy(alpha = 0.15f)
                                else -> SkillsOrange.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = slide.third,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = when (page) {
                            0 -> VendorGreenDark
                            1 -> SuperBlue
                            else -> SkillsOrange
                        }
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = slide.first,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = slide.second,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page Indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                repeat(3) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .height(8.dp)
                            .width(if (isSelected) 28.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pagerState.currentPage == 2) SkillsOrange else VendorGreenDark
                )
            ) {
                Text(
                    if (pagerState.currentPage == 2) "Get Started Now" else "Next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

/**
 * Quick Auth & Onboarding Sign In Screen
 */
@Composable
fun AuthScreen(
    onSignedIn: () -> Unit
) {
    var vendorName by remember { mutableStateOf("Chidi Okafor") }
    var businessName by remember { mutableStateOf("Classic Fits & Tech") }
    var phone by remember { mutableStateOf("08039281726") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    Brush.linearGradient(listOf(VendorGreenDark, SuperBlue)),
                    RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Welcome to VendorOS",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black
        )
        Text(
            "Auto Reply + Trust Proofs + Handwork NG",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = vendorName,
            onValueChange = { vendorName = it },
            label = { Text("Your Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Business / Shop Name") },
            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("WhatsApp Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onSignedIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VendorGreenDark)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Continue with Google / Quick Start", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.width(4.dp))
            Text(
                "Encrypted Nigerian Gateway. Zero Data Leakage.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * Role Selection Screen:
 * "How you want use VendorOS?"
 * Card 1: Vendor Mode (#25D366, Chat + Shield)
 * Card 2: Skills Mode (#FF6B00, Hammer & Tools)
 * Card 3: Super Mode (#1E40AF, Crown - Both)
 */
@Composable
fun RoleSelectionScreen(
    onSelectRole: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "How you want use VendorOS?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Text(
            "Choose your primary mode. You fit switch anytime.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
        )

        // Card 1: Vendor Mode
        ModeSelectionCard(
            title = "Vendor Mode",
            tagline = "I sell on WhatsApp / Instagram",
            description = "I need Auto-Reply + AI Sales Convincer + Trust Badge & Verified Proofs.",
            badge = "Most Popular",
            color = VendorGreenDark,
            icon = Icons.Default.Chat,
            onClick = { onSelectRole("vendor") }
        )

        Spacer(Modifier.height(16.dp))

        // Card 2: Skills Mode
        ModeSelectionCard(
            title = "Skills Mode (HandworkNG)",
            tagline = "I want learn or teach handwork",
            description = "Find verified masters near me or list my workshop to train students across 36 states.",
            badge = "Artisan Hub",
            color = SkillsOrange,
            icon = Icons.Default.Build,
            onClick = { onSelectRole("skills") }
        )

        Spacer(Modifier.height(16.dp))

        // Card 3: Super Mode
        ModeSelectionCard(
            title = "Super Mode (3 in 1)",
            tagline = "I do both! I sell and I teach",
            description = "All features unlocked: Auto-reply, trust proofs, workshop training, and verified artisan search.",
            badge = "Pro Choice",
            color = SuperBlue,
            icon = Icons.Default.Star,
            onClick = { onSelectRole("both") }
        )
    }
}

@Composable
private fun ModeSelectionCard(
    title: String,
    tagline: String,
    description: String,
    badge: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            .size(46.dp)
                            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(tagline, style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.SemiBold)
                    }
                }

                Surface(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

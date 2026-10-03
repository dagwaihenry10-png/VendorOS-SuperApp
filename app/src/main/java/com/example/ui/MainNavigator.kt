package com.example.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SkillMaster
import com.example.repository.AppRepository
import com.example.ui.components.ModeSwitchBar
import com.example.ui.screens.*
import com.example.ui.theme.SkillsOrange
import com.example.ui.theme.SuperBlue
import com.example.ui.theme.VendorGreenDark

sealed class AppScreen {
    object Onboarding : AppScreen()
    object Auth : AppScreen()
    object RoleSelection : AppScreen()
    object Main : AppScreen()
    object TrustPreview : AppScreen()
    data class MasterDetail(val master: SkillMaster) : AppScreen()
    object MapView : AppScreen()
}

@Composable
fun MainNavigator(repository: AppRepository) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("vendoros_prefs", Context.MODE_PRIVATE) }
    val onboardingSeen = remember { prefs.getBoolean("onboarding_seen", false) }
    val user by repository.userProfile.collectAsState()

    var currentScreen by remember {
        mutableStateOf<AppScreen>(
            if (!onboardingSeen) {
                AppScreen.Onboarding
            } else if (user.activeMode == null) {
                AppScreen.RoleSelection
            } else {
                AppScreen.Main
            }
        )
    }

    var selectedVendorTab by remember { mutableStateOf(0) } // 0: Dash, 1: Rules, 2: Proofs, 3: Settings
    var selectedSkillsTab by remember { mutableStateOf(0) } // 0: Home, 1: Map, 2: Inquiries, 3: Master Hub

    var activeMode by remember(user.activeMode) {
        mutableStateOf(user.activeMode ?: "vendor")
    }

    // Handle back button on sub-screens
    BackHandler(enabled = currentScreen !is AppScreen.Main && currentScreen !is AppScreen.Onboarding) {
        currentScreen = AppScreen.Main
    }

    when (val screen = currentScreen) {
        is AppScreen.Onboarding -> {
            OnboardingScreen(
                onFinish = {
                    prefs.edit().putBoolean("onboarding_seen", true).apply()
                    currentScreen = if (user.activeMode == null) AppScreen.RoleSelection else AppScreen.Main
                }
            )
        }

        is AppScreen.Auth -> {
            AuthScreen(
                onSignedIn = {
                    currentScreen = if (user.activeMode == null) AppScreen.RoleSelection else AppScreen.Main
                }
            )
        }

        is AppScreen.RoleSelection -> {
            RoleSelectionScreen(
                onSelectRole = { selectedRole ->
                    repository.updateUserMode(selectedRole)
                    activeMode = selectedRole
                    currentScreen = AppScreen.Main
                }
            )
        }

        is AppScreen.TrustPreview -> {
            TrustPagePreviewScreen(
                repository = repository,
                onBack = { currentScreen = AppScreen.Main }
            )
        }

        is AppScreen.MasterDetail -> {
            MasterDetailScreen(
                master = screen.master,
                repository = repository,
                onBack = { currentScreen = AppScreen.Main }
            )
        }

        is AppScreen.MapView -> {
            HandworkMapScreen(
                repository = repository,
                onBack = { currentScreen = AppScreen.Main },
                onViewProfile = { m ->
                    currentScreen = AppScreen.MasterDetail(m)
                }
            )
        }

        is AppScreen.Main -> {
            val isVendorView = activeMode == "vendor" || (activeMode == "both" && selectedVendorTab <= 3 && selectedSkillsTab == -1)

            Scaffold(
                topBar = {
                    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
                        ModeSwitchBar(
                            currentMode = activeMode,
                            onSelectMode = { newMode ->
                                activeMode = newMode
                                repository.updateUserMode(newMode)
                            }
                        )
                    }
                },
                bottomBar = {
                    NavigationBar(
                        tonalElevation = 8.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        if (activeMode == "vendor") {
                            NavigationBarItem(
                                selected = selectedVendorTab == 0,
                                onClick = { selectedVendorTab = 0 },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = selectedVendorTab == 1,
                                onClick = { selectedVendorTab = 1 },
                                icon = { Icon(Icons.Default.Bolt, contentDescription = "Rules") },
                                label = { Text("Auto Rules", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = selectedVendorTab == 2,
                                onClick = { selectedVendorTab = 2 },
                                icon = { Icon(Icons.Default.Verified, contentDescription = "Proofs") },
                                label = { Text("Trust Proofs", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = selectedVendorTab == 3,
                                onClick = { selectedVendorTab = 3 },
                                icon = { Icon(Icons.Default.Lock, contentDescription = "Secure Pay") },
                                label = { Text("Secure Pay", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        } else if (activeMode == "skills") {
                            NavigationBarItem(
                                selected = selectedSkillsTab == 0,
                                onClick = { selectedSkillsTab = 0 },
                                icon = { Icon(Icons.Default.Explore, contentDescription = "Discover") },
                                label = { Text("Discover", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = selectedSkillsTab == 1,
                                onClick = { selectedSkillsTab = 1 },
                                icon = { Icon(Icons.Default.Map, contentDescription = "Radar Map") },
                                label = { Text("Radar Map", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = selectedSkillsTab == 2,
                                onClick = { selectedSkillsTab = 2 },
                                icon = { Icon(Icons.Default.MailOutline, contentDescription = "Inquiries") },
                                label = { Text("Inquiries", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = selectedSkillsTab == 3,
                                onClick = { selectedSkillsTab = 3 },
                                icon = { Icon(Icons.Default.Handyman, contentDescription = "Master Hub") },
                                label = { Text("Master Hub", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        } else {
                            // Super Mode (Unified)
                            NavigationBarItem(
                                selected = selectedVendorTab == 0 && selectedSkillsTab == -1,
                                onClick = { selectedVendorTab = 0; selectedSkillsTab = -1 },
                                icon = { Icon(Icons.Default.Chat, contentDescription = "Vendor OS") },
                                label = { Text("Vendor OS", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = selectedVendorTab == 2 && selectedSkillsTab == -1,
                                onClick = { selectedVendorTab = 2; selectedSkillsTab = -1 },
                                icon = { Icon(Icons.Default.Verified, contentDescription = "Proofs") },
                                label = { Text("Proofs", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = selectedSkillsTab == 0 && selectedVendorTab == -1,
                                onClick = { selectedSkillsTab = 0; selectedVendorTab = -1 },
                                icon = { Icon(Icons.Default.Build, contentDescription = "Handwork") },
                                label = { Text("Handwork", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = selectedVendorTab == 3 && selectedSkillsTab == -1,
                                onClick = { selectedVendorTab = 3; selectedSkillsTab = -1 },
                                icon = { Icon(Icons.Default.Lock, contentDescription = "Secure Pay") },
                                label = { Text("Secure Pay", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }
            ) { scaffoldPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding)
                ) {
                    if (activeMode == "vendor") {
                        when (selectedVendorTab) {
                            0 -> VendorDashboardScreen(
                                repository = repository,
                                onNavigateToRules = { selectedVendorTab = 1 },
                                onNavigateToProofs = { selectedVendorTab = 2 },
                                onNavigateToTrustPreview = { currentScreen = AppScreen.TrustPreview },
                                onCreateProofClick = { selectedVendorTab = 2 }
                            )
                            1 -> RulesScreen(repository = repository)
                            2 -> ProofsScreen(
                                repository = repository,
                                onNavigateToTrustPreview = { currentScreen = AppScreen.TrustPreview }
                            )
                            3 -> SecureSettingsScreen(
                                repository = repository,
                                onSwitchMode = { mode -> activeMode = mode }
                            )
                        }
                    } else if (activeMode == "skills") {
                        when (selectedSkillsTab) {
                            0 -> HandworkHomeScreen(
                                repository = repository,
                                onMasterClick = { m -> currentScreen = AppScreen.MasterDetail(m) },
                                onBecomeMasterClick = { selectedSkillsTab = 3 },
                                onToggleMapView = { selectedSkillsTab = 1 }
                            )
                            1 -> HandworkMapScreen(
                                repository = repository,
                                onBack = { selectedSkillsTab = 0 },
                                onViewProfile = { m -> currentScreen = AppScreen.MasterDetail(m) }
                            )
                            2 -> InquiriesScreen(repository = repository)
                            3 -> MasterDashboardScreen(
                                repository = repository,
                                onViewPublicProfile = { m -> currentScreen = AppScreen.MasterDetail(m) }
                            )
                        }
                    } else {
                        // Super Mode
                        if (selectedSkillsTab == 0) {
                            HandworkHomeScreen(
                                repository = repository,
                                onMasterClick = { m -> currentScreen = AppScreen.MasterDetail(m) },
                                onBecomeMasterClick = { selectedSkillsTab = 3 },
                                onToggleMapView = { currentScreen = AppScreen.MapView }
                            )
                        } else {
                            when (selectedVendorTab) {
                                0 -> VendorDashboardScreen(
                                    repository = repository,
                                    onNavigateToRules = { selectedVendorTab = 1 },
                                    onNavigateToProofs = { selectedVendorTab = 2 },
                                    onNavigateToTrustPreview = { currentScreen = AppScreen.TrustPreview },
                                    onCreateProofClick = { selectedVendorTab = 2 }
                                )
                                2 -> ProofsScreen(
                                    repository = repository,
                                    onNavigateToTrustPreview = { currentScreen = AppScreen.TrustPreview }
                                )
                                3 -> SecureSettingsScreen(
                                    repository = repository,
                                    onSwitchMode = { mode -> activeMode = mode }
                                )
                                else -> VendorDashboardScreen(
                                    repository = repository,
                                    onNavigateToRules = { selectedVendorTab = 1 },
                                    onNavigateToProofs = { selectedVendorTab = 2 },
                                    onNavigateToTrustPreview = { currentScreen = AppScreen.TrustPreview },
                                    onCreateProofClick = { selectedVendorTab = 2 }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

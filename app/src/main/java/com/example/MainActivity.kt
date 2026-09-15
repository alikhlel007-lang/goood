package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.Strings
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CashierOrdersScreen
import com.example.ui.screens.CustomerMenuScreen
import com.example.ui.screens.CustomerThemeSelectorDialog
import com.example.ui.screens.ManualWaiterOrderScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CafeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CafeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentPreset by viewModel.currentThemePreset.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            MyApplicationTheme(preset = currentPreset, themeMode = themeMode) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: CafeViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val currentCafe by viewModel.currentCafe.collectAsState()

    val pendingCount = allOrders.count { (it.status == "APPROVAL" || it.status == "PREPARING") && !it.isPaid }

    val layoutDirection = if (currentLang.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    var currentTab by remember { mutableStateOf("MENU") } // "MENU", "CASHIER", "SETTINGS"
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        CustomerThemeSelectorDialog(
            viewModel = viewModel,
            lang = currentLang,
            onDismiss = { showThemeDialog = false }
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        if (currentUser == null) {
            // Auth / Guest / Role selection screen
            AuthScreen(
                viewModel = viewModel,
                lang = currentLang,
                onLoginSuccess = {
                    currentTab = "MENU"
                }
            )
        } else {
            val isCustomer = currentUser?.role == "CUSTOMER"

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    if (isCustomer) {
                        TopAppBar(
                            title = {
                                Text(
                                    text = currentCafe?.name ?: Strings.get("app_title", currentLang),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            actions = {
                                IconButton(onClick = { viewModel.logout() }) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = Strings.get("logout", currentLang),
                                        tint = Color(0xFFE57373)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF141414)
                            )
                        )
                    } else {
                        // Staff / Owner Top Bar matching screenshot
                        TopAppBar(
                            title = {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // LEFT SIDE: Settings Gear + Logout
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val isSettingsActive = currentTab == "SETTINGS"
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSettingsActive) Color(0xFFE5A93C) else Color(0xFF222222))
                                                    .border(
                                                        BorderStroke(1.dp, if (isSettingsActive) Color(0xFFE5A93C) else Color(0xFF383838)),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { currentTab = "SETTINGS" },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Settings,
                                                    contentDescription = "Settings",
                                                    tint = if (isSettingsActive) Color(0xFF141414) else Color(0xFFE5A93C),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            IconButton(
                                                onClick = { showThemeDialog = true },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Palette,
                                                    contentDescription = "Theme",
                                                    tint = Color(0xFFE5A93C).copy(alpha = 0.8f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.logout() },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ExitToApp,
                                                    contentDescription = Strings.get("logout", currentLang),
                                                    tint = Color(0xFFE57373),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        // RIGHT SIDE: Capsule Tabs [menu | cashier]
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(Color(0xFF222222))
                                                .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(20.dp))
                                                .padding(3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Tab 1: Menu
                                            val isMenuActive = currentTab == "MENU"
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(18.dp))
                                                    .background(if (isMenuActive) Color(0xFFE5A93C) else Color.Transparent)
                                                    .clickable { currentTab = "MENU" }
                                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Smartphone,
                                                        contentDescription = null,
                                                        tint = if (isMenuActive) Color(0xFF141414) else Color(0xFFCCCCCC),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "مينيو",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.5.sp,
                                                        color = if (isMenuActive) Color(0xFF141414) else Color(0xFFCCCCCC)
                                                    )
                                                }
                                            }

                                            // Tab 2: Cashier
                                            val isCashierActive = currentTab == "CASHIER"
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(18.dp))
                                                    .background(if (isCashierActive) Color(0xFFE5A93C) else Color.Transparent)
                                                    .clickable { currentTab = "CASHIER" }
                                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (pendingCount > 0) {
                                                        BadgedBox(
                                                            badge = {
                                                                Badge(containerColor = Color(0xFFE53935)) {
                                                                    Text("$pendingCount", fontSize = 10.sp)
                                                                }
                                                            }
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Computer,
                                                                contentDescription = null,
                                                                tint = if (isCashierActive) Color(0xFF141414) else Color(0xFFCCCCCC),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    } else {
                                                        Icon(
                                                            imageVector = Icons.Default.Computer,
                                                            contentDescription = null,
                                                            tint = if (isCashierActive) Color(0xFF141414) else Color(0xFFCCCCCC),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "كاشير",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.5.sp,
                                                        color = if (isCashierActive) Color(0xFF141414) else Color(0xFFCCCCCC)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF141414)
                            )
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isCustomer) {
                        CustomerMenuScreen(
                            viewModel = viewModel,
                            lang = currentLang
                        )
                    } else {
                        when (currentTab) {
                            "MENU" -> ManualWaiterOrderScreen(viewModel = viewModel, lang = currentLang)
                            "CASHIER" -> CashierOrdersScreen(viewModel = viewModel, lang = currentLang)
                            "SETTINGS" -> SettingsScreen(viewModel = viewModel, lang = currentLang)
                        }
                    }
                }
            }
        }
    }
}

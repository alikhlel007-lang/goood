package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.ui.util.ImageStorageHelper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.TableEntity
import com.example.data.qr.QrCodeGenerator
import com.example.ui.components.CafeIconBadge
import com.example.ui.components.CafeIcons
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.Strings
import com.example.ui.viewmodel.CafeViewModel

enum class SettingsSubSection {
    HOME,
    LANGUAGE,
    THEMES,
    MENU,
    TABLES,
    OFFERS,
    CAFE_PROFILE
}

@Composable
fun SettingsScreen(
    viewModel: CafeViewModel,
    lang: AppLanguage
) {
    val context = LocalContext.current
    var currentSubSection by remember { mutableStateOf(SettingsSubSection.HOME) }

    when (currentSubSection) {
        SettingsSubSection.HOME -> {
            SettingsHomeScreen(
                viewModel = viewModel,
                lang = lang,
                onNavigateTo = { currentSubSection = it }
            )
        }
        SettingsSubSection.LANGUAGE -> {
            LanguageSettingsScreen(
                viewModel = viewModel,
                lang = lang,
                onBack = { currentSubSection = SettingsSubSection.HOME }
            )
        }
        SettingsSubSection.THEMES -> {
            ThemeSettingsScreen(
                viewModel = viewModel,
                lang = lang,
                onBack = { currentSubSection = SettingsSubSection.HOME }
            )
        }
        SettingsSubSection.MENU -> {
            MenuManagementScreen(
                viewModel = viewModel,
                lang = lang,
                onBack = { currentSubSection = SettingsSubSection.HOME }
            )
        }
        SettingsSubSection.TABLES -> {
            TablesManagementScreen(
                viewModel = viewModel,
                lang = lang,
                onBack = { currentSubSection = SettingsSubSection.HOME }
            )
        }
        SettingsSubSection.OFFERS -> {
            OffersManagementScreen(
                viewModel = viewModel,
                lang = lang,
                onBack = { currentSubSection = SettingsSubSection.HOME }
            )
        }
        SettingsSubSection.CAFE_PROFILE -> {
            CafeProfileSettingsScreen(
                viewModel = viewModel,
                lang = lang,
                onBack = { currentSubSection = SettingsSubSection.HOME }
            )
        }
    }
}

@Composable
fun SettingsHomeScreen(
    viewModel: CafeViewModel,
    lang: AppLanguage,
    onNavigateTo: (SettingsSubSection) -> Unit
) {
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = Strings.get("settings", lang),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "تخصيص وإعداد كامل لنظام الكافيه الإلكتروني",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(18.dp))

        data class SettingsOption(
            val section: SettingsSubSection,
            val title: String,
            val subtitle: String,
            val icon: androidx.compose.ui.graphics.vector.ImageVector,
            val iconColor: Color,
            val containerColor: Color
        )

        val menuOptions = listOf(
            SettingsOption(
                section = SettingsSubSection.MENU,
                title = Strings.get("menu_management", lang),
                subtitle = "إضافة وتعديل الأقسام والمنتجات والأسعار والتوافر",
                icon = Icons.Default.RestaurantMenu,
                iconColor = Color(0xFFFF9800), // Amber / Warm Orange
                containerColor = Color(0xFF2E1A0A)
            ),
            SettingsOption(
                section = SettingsSubSection.OFFERS,
                title = Strings.get("offers_management", lang),
                subtitle = "تخفيض المواد، تخفيض الأقسام، وإدارة الخصومات النشطة",
                icon = Icons.Default.LocalOffer,
                iconColor = Color(0xFFE53935), // Vibrant Red
                containerColor = Color(0xFF2E1010)
            ),
            SettingsOption(
                section = SettingsSubSection.TABLES,
                title = Strings.get("tables_management", lang),
                subtitle = "إدارة أرقام الطاولات وتوليد وحفظ أكواد الـ QR",
                icon = Icons.Default.TableRestaurant,
                iconColor = Color(0xFF00BCD4), // Cyan / Teal
                containerColor = Color(0xFF0C2429)
            ),
            SettingsOption(
                section = SettingsSubSection.CAFE_PROFILE,
                title = Strings.get("cafe_profile", lang),
                subtitle = "اسم الكافيه، الشعار، والرسالة الترحيبية للزبائن",
                icon = Icons.Default.Storefront,
                iconColor = Color(0xFFAB47BC), // Purple
                containerColor = Color(0xFF26102C)
            ),
            SettingsOption(
                section = SettingsSubSection.THEMES,
                title = "الثيمات ومظهر المينو",
                subtitle = "تخصيص ألوان الواجهة والمظهر العام للتطبيق",
                icon = Icons.Default.Palette,
                iconColor = Color(0xFF26A69A), // Teal / Emerald
                containerColor = Color(0xFF0E2724)
            ),
            SettingsOption(
                section = SettingsSubSection.LANGUAGE,
                title = Strings.get("language", lang),
                subtitle = "التبديل بين العربية والإنجليزية والكردية",
                icon = Icons.Default.Language,
                iconColor = Color(0xFF42A5F5), // Sky Blue
                containerColor = Color(0xFF0F2236)
            )
        )

        menuOptions.forEach { opt ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { onNavigateTo(opt.section) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(opt.containerColor)
                            .border(BorderStroke(1.dp, opt.iconColor.copy(alpha = 0.35f)), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = opt.icon,
                            contentDescription = null,
                            tint = opt.iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = opt.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = opt.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Logout Card at bottom of Settings
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable { showLogoutConfirmDialog = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF221111)),
            border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.45f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF381414))
                        .border(BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.5f)), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "تسجيل الخروج",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تسجيل الخروج",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF5350),
                            fontSize = 14.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "تسجيل الخروج من حساب الإدارة والكاشير",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFAAAAAA),
                            fontSize = 11.5.sp
                        )
                    )
                }

                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = Color(0xFFE53935).copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (showLogoutConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmDialog = false },
                title = {
                    Text("تسجيل الخروج", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("هل أنت متأكد من رغبتك في تسجيل الخروج من نظام الكاشير والإدارة؟")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutConfirmDialog = false
                            viewModel.logout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text("تسجيل الخروج", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showLogoutConfirmDialog = false }
                    ) {
                        Text("إلغاء")
                    }
                }
            )
        }
    }
}

// 1. Language Settings
@Composable
fun LanguageSettingsScreen(
    viewModel: CafeViewModel,
    lang: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf(lang) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = Strings.get("language", lang),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = Strings.get("select_language", lang),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        AppLanguage.entries.forEach { appLang ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { selectedLanguage = appLang },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedLanguage == appLang) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedLanguage == appLang,
                        onClick = { selectedLanguage = appLang }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = appLang.titleInLanguage,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        val subtitle = when (appLang) {
                            AppLanguage.ARABIC -> "اللغة الافتراضية"
                            AppLanguage.ENGLISH -> "English interface"
                            AppLanguage.KURDISH -> "زمانی کوردی (سۆرانی)"
                        }
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Save Button (Mandatory Save Pattern)
        Button(
            onClick = {
                viewModel.setLanguage(selectedLanguage)
                Toast.makeText(context, "تم حفظ تغيير اللغة بنجاح", Toast.LENGTH_SHORT).show()
                onBack()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Strings.get("save_changes", lang), fontWeight = FontWeight.Bold)
        }
    }
}

// 2. Menu Management Screen (Categories & Items Settings, Rename, Visibility, Image upload, Icon selection)
@Composable
fun MenuManagementScreen(
    viewModel: CafeViewModel,
    lang: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var activeAddCategoryId by remember { mutableStateOf<String?>(null) }

    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }
    var itemToEdit by remember { mutableStateOf<MenuItemEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<MenuItemEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = Strings.get("menu_management", lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "إدارة الأقسام والمواد وتغيير الأسماء",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { showAddCategoryDialog = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE5A93C),
                    contentColor = Color(0xFF141414)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Strings.get("add_category", lang),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Categories List with Edit, Visibility Toggle & Items overview
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(categories) { cat ->
                val itemsInCat = menuItems.filter { it.categoryId == cat.id }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Category Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                CafeIconBadge(
                                    iconName = cat.iconName,
                                    customImageUri = cat.customImageUri,
                                    size = 40.dp,
                                    iconSize = 22.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cat.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "${itemsInCat.size} مواد في هذا القسم",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Category Action Buttons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // One-tap Visibility Toggle
                                IconButton(
                                    onClick = {
                                        viewModel.toggleCategoryVisibility(cat)
                                        val msg = if (cat.isVisible) "تم إخفاء قسم ${cat.name} عن الزبائن" else "تم إظهار قسم ${cat.name} للزبائن"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (cat.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "إخفاء/إظهار",
                                        tint = if (cat.isVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Edit Category Settings
                                IconButton(
                                    onClick = { categoryToEdit = cat },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "تعديل إعدادات القسم",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Delete Category
                                IconButton(
                                    onClick = { categoryToDelete = cat },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "حذف القسم",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Items inside this category
                        if (itemsInCat.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "لا توجد مواد مضافة في هذا القسم بعد",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            itemsInCat.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (item.isAvailable) 0.35f else 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        CafeIconBadge(
                                            iconName = item.iconName,
                                            customImageUri = item.customImageUri,
                                            size = 38.dp,
                                            iconSize = 22.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    "${item.effectivePrice.toInt()} د.ع",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (item.hasDiscount) Color(0xFFE5A93C) else MaterialTheme.colorScheme.primary
                                                )
                                                if (item.hasDiscount) {
                                                    Spacer(modifier = Modifier.width(5.dp))
                                                    Text(
                                                        "${item.originalPrice.toInt()} د.ع",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                        textDecoration = TextDecoration.LineThrough
                                                    )
                                                    val pct = item.discountPercentage ?: (((item.originalPrice - (item.discountedPrice ?: item.originalPrice)) / item.originalPrice) * 100).toInt()
                                                    if (pct > 0) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(Color(0xFFE53935))
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = "$pct%",
                                                                color = Color.White,
                                                                fontSize = 8.5.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                    if (!item.showInOffers) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(Color(0xFF383838))
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = "مخفية من العروض",
                                                                color = Color(0xFFAAAAAA),
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Action buttons for Item: Quick Toggle Visibility, Offers Toggle, Edit, Delete
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Quick Toggle Offers Visibility (إظهار/إخفاء من العروض)
                                        IconButton(
                                            onClick = {
                                                viewModel.toggleMenuItemOffersVisibility(item)
                                                val msg = if (item.showInOffers) "تم إخفاء ${item.name} من قسم العروض" else "تم إظهار ${item.name} في قسم العروض"
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocalOffer,
                                                contentDescription = if (item.showInOffers) "إخفاء من العروض" else "إظهار في العروض",
                                                tint = if (item.showInOffers) Color(0xFFE5A93C) else Color(0xFF555555),
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }

                                        // Quick Toggle Visibility
                                        IconButton(
                                            onClick = {
                                                viewModel.toggleMenuItemVisibility(item)
                                                val msg = if (item.isAvailable) "تم إخفاء ${item.name} عن الزبائن" else "تم إظهار ${item.name} للزبائن"
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (item.isAvailable) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "إخفاء/إظهار",
                                                tint = if (item.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Edit Item Settings Dialog
                                        IconButton(
                                            onClick = { itemToEdit = item },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "تعديل المادة",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Delete Item
                                        IconButton(
                                            onClick = { itemToDelete = item },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "حذف المادة",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                activeAddCategoryId = cat.id
                                showAddItemDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة مادة جديدة إلى ${cat.name}", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Edit Category Dialog
    if (categoryToEdit != null) {
        EditCategoryDialog(
            category = categoryToEdit!!,
            lang = lang,
            onDismiss = { categoryToEdit = null },
            onSave = { updatedCategory ->
                viewModel.updateCategory(updatedCategory)
                categoryToEdit = null
                Toast.makeText(context, "تم حفظ إعدادات القسم بنجاح", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Item Dialog
    if (itemToEdit != null) {
        EditMenuItemDialog(
            item = itemToEdit!!,
            categories = categories,
            lang = lang,
            onDismiss = { itemToEdit = null },
            onSave = { updatedItem ->
                viewModel.updateMenuItem(updatedItem)
                itemToEdit = null
                Toast.makeText(context, "تم حفظ إعدادات المادة بنجاح", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Category Confirmation Dialog
    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("تأكيد حذف القسم", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف قسم \"${categoryToDelete?.name}\" وجميع المواد الموجودة بداخله؟") },
            confirmButton = {
                Button(
                    onClick = {
                        categoryToDelete?.id?.let { viewModel.deleteCategory(it) }
                        categoryToDelete = null
                        Toast.makeText(context, "تم حذف القسم ومواده بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { categoryToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Delete Item Confirmation Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("تأكيد حذف المادة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف مادة \"${itemToDelete?.name}\" نهائياً؟") },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.id?.let { viewModel.deleteMenuItem(it) }
                        itemToDelete = null
                        Toast.makeText(context, "تم حذف المادة بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Add Category Dialog with Photo upload support
    if (showAddCategoryDialog) {
        var newCatName by remember { mutableStateOf("") }
        var selectedIcon by remember { mutableStateOf("coffee") }
        var customCatImageUri by remember { mutableStateOf<String?>(null) }

        val catPhotoLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                val saved = ImageStorageHelper.saveImageToInternalStorage(context, uri)
                if (saved != null) {
                    customCatImageUri = saved
                    Toast.makeText(context, "تم حفظ صورة القسم", Toast.LENGTH_SHORT).show()
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text(Strings.get("add_category", lang), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Preview
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CafeIconBadge(
                            iconName = selectedIcon,
                            customImageUri = customCatImageUri,
                            size = 60.dp,
                            iconSize = 34.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.Center) {
                        Button(
                            onClick = {
                                catPhotoLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (customCatImageUri != null) "تغيير الصورة" else "رفع صورة من المعرض", fontSize = 11.sp)
                        }
                        if (customCatImageUri != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = { customCatImageUri = null },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("إزالة الصورة", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text(Strings.get("category_name", lang)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (customCatImageUri == null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            Strings.get("select_icon", lang),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CafeIcons.AVAILABLE_ICONS) { (iconKey, label) ->
                                val isSelected = selectedIcon == iconKey
                                Card(
                                    modifier = Modifier.clickable { selectedIcon = iconKey },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CafeIconBadge(iconName = iconKey, size = 32.dp, iconSize = 18.dp)
                                        Text(label, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCatName.isNotBlank()) {
                            viewModel.addCategory(newCatName.trim(), selectedIcon, customCatImageUri)
                            Toast.makeText(context, "تم حفظ القسم الجديد بنجاح", Toast.LENGTH_SHORT).show()
                            showAddCategoryDialog = false
                        }
                    }
                ) {
                    Text(Strings.get("save_changes", lang))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddCategoryDialog = false }) {
                    Text(Strings.get("cancel_dismiss", lang))
                }
            }
        )
    }

    // Add Item Dialog with Photo upload support
    if (showAddItemDialog && activeAddCategoryId != null) {
        var newItemName by remember { mutableStateOf("") }
        var newItemDesc by remember { mutableStateOf("") }
        var newItemPrice by remember { mutableStateOf("") }
        var selectedItemIcon by remember { mutableStateOf("coffee") }
        var customItemImageUri by remember { mutableStateOf<String?>(null) }
        var newItemShowInOffers by remember { mutableStateOf(false) }
        var newOfferBackgroundUri by remember { mutableStateOf<String?>(null) }

        val itemPhotoLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                val saved = ImageStorageHelper.saveImageToInternalStorage(context, uri)
                if (saved != null) {
                    customItemImageUri = saved
                    Toast.makeText(context, "تم حفظ صورة المادة", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val offerBgLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                val saved = ImageStorageHelper.saveImageToInternalStorage(context, uri)
                if (saved != null) {
                    newOfferBackgroundUri = saved
                    Toast.makeText(context, "تم حفظ خلفية العرض بنجاح", Toast.LENGTH_SHORT).show()
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text(Strings.get("add_item", lang), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Preview
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CafeIconBadge(
                            iconName = selectedItemIcon,
                            customImageUri = customItemImageUri,
                            size = 60.dp,
                            iconSize = 34.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.Center) {
                        Button(
                            onClick = {
                                itemPhotoLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (customItemImageUri != null) "تغيير الصورة" else "رفع صورة من المعرض", fontSize = 11.sp)
                        }
                        if (customItemImageUri != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = { customItemImageUri = null },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("إزالة الصورة", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text(Strings.get("item_name", lang)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newItemPrice,
                        onValueChange = { newItemPrice = it },
                        label = { Text(Strings.get("item_price", lang)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newItemDesc,
                        onValueChange = { newItemDesc = it },
                        label = { Text(Strings.get("item_desc", lang)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Offers Visibility Switch Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (newItemShowInOffers) Color(0xFFE5A93C).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, if (newItemShowInOffers) Color(0xFFE5A93C).copy(alpha = 0.4f) else Color.Transparent)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = if (newItemShowInOffers) Color(0xFFE5A93C) else Color(0xFF888888),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (newItemShowInOffers) "إظهار في قسم العروض والجديد ✨" else "إخفاء من قسم العروض والجديد",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (newItemShowInOffers) Color(0xFFE5A93C) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (newItemShowInOffers) "ستظهر المادة في بنر العروض وأحدث الإضافات" else "ستظهر في المينو العادي فقط دون قسم العروض",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    fontSize = 10.5.sp
                                )
                            }
                            Switch(
                                checked = newItemShowInOffers,
                                onCheckedChange = { newItemShowInOffers = it }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Offer Background Image Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, Color(0xFFE5A93C).copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = Color(0xFFE5A93C),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "خلفية بطاقة العرض (اختياري)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFEEEEEE)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ارفع صورة لتصبح خلفية مميزة لبطاقة المادة في قسم العروض والجديد",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (newOfferBackgroundUri != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = newOfferBackgroundUri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.35f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "معاينة خلفية العرض ✓",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = {
                                        offerBgLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A93C)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color(0xFF141414), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (newOfferBackgroundUri != null) "تغيير خلفية العرض" else "رفع صورة خلفية العرض",
                                        color = Color(0xFF141414),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                if (newOfferBackgroundUri != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = { newOfferBackgroundUri = null },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("إزالة الخلفية", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (customItemImageUri == null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            Strings.get("select_icon", lang),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CafeIcons.AVAILABLE_ICONS) { (iconKey, label) ->
                                val isSelected = selectedItemIcon == iconKey
                                Card(
                                    modifier = Modifier.clickable { selectedItemIcon = iconKey },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CafeIconBadge(iconName = iconKey, size = 32.dp, iconSize = 18.dp)
                                        Text(label, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceNum = newItemPrice.toDoubleOrNull() ?: 0.0
                        if (newItemName.isNotBlank() && priceNum > 0) {
                            viewModel.addMenuItem(
                                categoryId = activeAddCategoryId!!,
                                name = newItemName.trim(),
                                description = newItemDesc.trim(),
                                price = priceNum,
                                iconName = selectedItemIcon,
                                customUri = customItemImageUri,
                                showInOffers = newItemShowInOffers,
                                offerBackgroundUri = newOfferBackgroundUri
                            )
                            Toast.makeText(context, "تم حفظ المادة بنجاح", Toast.LENGTH_SHORT).show()
                            showAddItemDialog = false
                        }
                    }
                ) {
                    Text(Strings.get("save_changes", lang))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddItemDialog = false }) {
                    Text(Strings.get("cancel_dismiss", lang))
                }
            }
        )
    }
}

// Dialog to edit Category settings (Rename, Hide/Show, Custom Image, Icon)
@Composable
fun EditCategoryDialog(
    category: CategoryEntity,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (CategoryEntity) -> Unit
) {
    val context = LocalContext.current
    var catName by remember { mutableStateOf(category.name) }
    var isVisible by remember { mutableStateOf(category.isVisible) }
    var customImageUri by remember { mutableStateOf(category.customImageUri) }
    var selectedIcon by remember { mutableStateOf(category.iconName) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImageToInternalStorage(context, uri)
            if (savedPath != null) {
                customImageUri = savedPath
                Toast.makeText(context, "تم حفظ الصورة بنجاح", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إعدادات القسم: ${category.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Photo / Icon Preview
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CafeIconBadge(
                        iconName = selectedIcon,
                        customImageUri = customImageUri,
                        size = 72.dp,
                        iconSize = 40.dp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (customImageUri != null) "تغيير الصورة" else "رفع صورة للقسم")
                    }

                    if (customImageUri != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { customImageUri = null },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إزالة الصورة")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name field
                OutlinedTextField(
                    value = catName,
                    onValueChange = { catName = it },
                    label = { Text("اسم القسم") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Visibility Switch Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isVisible) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isVisible) "القسم ظاهر في المينيو" else "القسم مخفي عن الزبائن",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = if (isVisible) "الزبائن يستطيعون مشاهدة هذا القسم ومواده والطلب منه"
                                else "سيتم إخفاء القسم وجميع مواده تماماً عن قائمة الزبائن",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isVisible,
                            onCheckedChange = { isVisible = it }
                        )
                    }
                }

                // If no custom image, show icons picker
                if (customImageUri == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "أو اختر أيقونة رمزية للقسم:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CafeIcons.AVAILABLE_ICONS) { (iconKey, label) ->
                            val isSelected = selectedIcon == iconKey
                            Card(
                                modifier = Modifier.clickable { selectedIcon = iconKey },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CafeIconBadge(iconName = iconKey, size = 34.dp, iconSize = 20.dp)
                                    Text(label, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (catName.isNotBlank()) {
                        onSave(
                            category.copy(
                                name = catName.trim(),
                                isVisible = isVisible,
                                customImageUri = customImageUri,
                                iconName = selectedIcon
                            )
                        )
                    }
                }
            ) {
                Text(Strings.get("save_changes", lang))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(Strings.get("cancel_dismiss", lang))
            }
        }
    )
}

// Dialog to edit Menu Item settings (Rename, Price, Hide/Show, Custom Image, Icon, Category)
@Composable
fun EditMenuItemDialog(
    item: MenuItemEntity,
    categories: List<CategoryEntity>,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (MenuItemEntity) -> Unit
) {
    val context = LocalContext.current
    var itemName by remember { mutableStateOf(item.name) }
    var itemPrice by remember {
        mutableStateOf(
            if (item.originalPrice == item.originalPrice.toLong().toDouble()) item.originalPrice.toLong().toString()
            else item.originalPrice.toString()
        )
    }
    var itemDesc by remember { mutableStateOf(item.description) }
    var selectedCategoryId by remember { mutableStateOf(item.categoryId) }
    var isAvailable by remember { mutableStateOf(item.isAvailable) }
    var showInOffers by remember { mutableStateOf(item.showInOffers) }
    var offerBackgroundImageUri by remember { mutableStateOf(item.offerBackgroundImageUri) }
    var customImageUri by remember { mutableStateOf(item.customImageUri) }
    var selectedIcon by remember { mutableStateOf(item.iconName) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImageToInternalStorage(context, uri)
            if (savedPath != null) {
                customImageUri = savedPath
                Toast.makeText(context, "تم حفظ صورة المادة بنجاح", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val offerBgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImageToInternalStorage(context, uri)
            if (savedPath != null) {
                offerBackgroundImageUri = savedPath
                Toast.makeText(context, "تم حفظ خلفية العرض بنجاح", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إعدادات المادة: ${item.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Photo / Icon Preview
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CafeIconBadge(
                        iconName = selectedIcon,
                        customImageUri = customImageUri,
                        size = 72.dp,
                        iconSize = 40.dp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (customImageUri != null) "تغيير الصورة" else "رفع صورة للمادة")
                    }

                    if (customImageUri != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { customImageUri = null },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إزالة الصورة")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name field
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("اسم المادة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price field
                OutlinedTextField(
                    value = itemPrice,
                    onValueChange = { itemPrice = it },
                    label = { Text("السعر الأصلي (د.ع)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description field
                OutlinedTextField(
                    value = itemDesc,
                    onValueChange = { itemDesc = it },
                    label = { Text("وصف المادة (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category selection
                Text(
                    text = "القسم التابع له:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isCatSelected = selectedCategoryId == cat.id
                        Card(
                            modifier = Modifier.clickable { selectedCategoryId = cat.id },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCatSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CafeIconBadge(
                                    iconName = cat.iconName,
                                    customImageUri = cat.customImageUri,
                                    size = 18.dp,
                                    iconSize = 12.dp,
                                    containerColor = Color.Transparent,
                                    iconColor = if (isCatSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    cat.name,
                                    fontSize = 11.sp,
                                    color = if (isCatSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Visibility Switch Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAvailable) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAvailable) "المادة ظاهرة للزبائن" else "المادة مخفية عن الزبائن",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = if (isAvailable) "تظهر في قائمة طعام الزبائن ويمكن طلبها"
                                else "مخفية من قائمة طعام الزبائن ولا يمكن طلبها",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAvailable,
                            onCheckedChange = { isAvailable = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Offers Visibility Switch Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (showInOffers) Color(0xFFE5A93C).copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, if (showInOffers) Color(0xFFE5A93C).copy(alpha = 0.4f) else Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = if (showInOffers) Color(0xFFE5A93C) else Color(0xFF888888),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (showInOffers) "المادة ظاهرة في قسم العروض والجديد" else "المادة مخفية من قسم العروض والجديد",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (showInOffers) Color(0xFFE5A93C) else Color(0xFFCCCCCC)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (showInOffers) "تظهر في مهرجان العروض وأحدث الإضافات"
                                else "مخفية من قسم العروض مع بقائها متاحة في المينو العادي",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = showInOffers,
                            onCheckedChange = { showInOffers = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Offer Background Image Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, Color(0xFFE5A93C).copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color(0xFFE5A93C),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "خلفية بطاقة العرض",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFEEEEEE)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "صورة مخصصة كخلفية لبطاقة المادة داخل قسم العروض والإضافات الجديدة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 10.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (offerBackgroundImageUri != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = offerBackgroundImageUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.35f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "معاينة خلفية العرض الحالية ✓",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    offerBgLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A93C)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color(0xFF141414), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (offerBackgroundImageUri != null) "تغيير خلفية العرض" else "رفع صورة خلفية للعرض",
                                    color = Color(0xFF141414),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            if (offerBackgroundImageUri != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { offerBackgroundImageUri = null },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("إزالة الخلفية", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // If no custom image, show icons picker
                if (customImageUri == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "أو اختر أيقونة رمزية للمادة:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CafeIcons.AVAILABLE_ICONS) { (iconKey, label) ->
                            val isSelected = selectedIcon == iconKey
                            Card(
                                modifier = Modifier.clickable { selectedIcon = iconKey },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CafeIconBadge(iconName = iconKey, size = 32.dp, iconSize = 18.dp)
                                    Text(label, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceNum = itemPrice.toDoubleOrNull() ?: item.originalPrice
                    if (itemName.isNotBlank() && priceNum > 0) {
                        onSave(
                            item.copy(
                                name = itemName.trim(),
                                originalPrice = priceNum,
                                description = itemDesc.trim(),
                                categoryId = selectedCategoryId,
                                isAvailable = isAvailable,
                                showInOffers = showInOffers,
                                offerBackgroundImageUri = offerBackgroundImageUri,
                                customImageUri = customImageUri,
                                iconName = selectedIcon
                            )
                        )
                    }
                }
            ) {
                Text(Strings.get("save_changes", lang))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(Strings.get("cancel_dismiss", lang))
            }
        }
    )
}

// 3. Tables Management Screen (Add/Remove tables, Set VIP/Outdoor/Indoor, Generate QR & Export PDF)
@Composable
fun TablesManagementScreen(
    viewModel: CafeViewModel,
    lang: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentCafe by viewModel.currentCafe.collectAsState()
    val tables by viewModel.tables.collectAsState()
    var countInput by remember { mutableIntStateOf(tables.size.coerceAtLeast(1)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = Strings.get("tables_management", lang),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Table Count Adjuster
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Strings.get("tables_count", lang),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "زيادة أو تقليل عدد طاولات الكافيه وتوليد رموز الباركود الفريدة لها",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { if (countInput > 1) countInput-- },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "$countInput",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        OutlinedButton(
                            onClick = { if (countInput < 99) countInput++ },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.updateTablesCount(countInput)
                            Toast.makeText(context, "تم حفظ وتحديث عدد الطاولات", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Strings.get("save_changes", lang), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Generate QR Codes & Export PDF Action Buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "باركودات الطاولات وملف الطباعة PDF",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "يمكنك إنشاء باركودات جديدة وتنزيل ملف PDF جاهز للطباعة ولصقه على الطاولات.",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.regenerateAllQrCodes()
                            Toast.makeText(context, Strings.get("qr_generated_success", lang), Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("توليد باركودات جديدة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.exportQrPdf(context) { pdfFile ->
                                Toast.makeText(context, "تم تجهيز ملف PDF!", Toast.LENGTH_SHORT).show()
                                QrCodeGenerator.openPdf(context, pdfFile)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Strings.get("download_pdf", lang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val hostingUrl = "https://alikhelel007-lang.github.io/goood/?cafe=default_cafe&table=1"
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(hostingUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("معاينة صفحة ويب الزبائن في المتصفح", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "تحديد نوع كل طاولة (VIP / جلسة خارجية / جلسة داخلية):",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Table Types List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tables) { table ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TableRestaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${Strings.get("table", lang)} #${table.tableNumber}", fontWeight = FontWeight.Bold)
                        }

                        // Type selector chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "INDOOR" to "داخلية",
                                "OUTDOOR" to "خارجية",
                                "VIP" to "VIP"
                            ).forEach { (typeCode, label) ->
                                val isSelected = table.tableType == typeCode
                                Card(
                                    modifier = Modifier.clickable {
                                        viewModel.updateTableType(table.tableNumber, typeCode)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. Offers & Discounts Manager (مطابق تماماً للتصميم في لقطة الشاشة)
@Composable
fun OffersManagementScreen(
    viewModel: CafeViewModel,
    lang: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()

    // 0: تحديد مواد من أقسام مختلفة, 1: تخفيض قسم كامل
    var selectedMode by remember { mutableIntStateOf(0) }

    // Category filter: null for "الكل" or categoryId
    var filterCategoryId by remember { mutableStateOf<String?>(null) }

    // Target item when uploading offer background image
    var itemForOfferBgUpload by remember { mutableStateOf<MenuItemEntity?>(null) }

    val offerBgPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            itemForOfferBgUpload?.let { targetItem ->
                val saved = ImageStorageHelper.saveImageToInternalStorage(context, uri)
                if (saved != null) {
                    viewModel.updateMenuItem(targetItem.copy(offerBackgroundImageUri = saved))
                    Toast.makeText(context, "تم حفظ خلفية العرض لـ ${targetItem.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Multi-selection of item IDs (default selects the first item or user clicks)
    val selectedItemIds = remember { mutableStateListOf<String>() }

    // Discount method: 0 = Percentage (%), 1 = Manual price (د.ع) (default 1 as in screenshot)
    var discountMethod by remember { mutableIntStateOf(1) }

    // Percentage input (for percentage mode)
    var percentageInput by remember { mutableStateOf("20") }

    // Per-item manual prices map: itemId -> price string
    val manualPrices = remember { mutableStateMapOf<String, String>() }

    // For whole category mode
    var selectedWholeCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }
    var categoryPercentInput by remember { mutableStateOf("15") }

    // Filtered items list
    val visibleItems = remember(menuItems, filterCategoryId) {
        if (filterCategoryId == null) {
            menuItems
        } else {
            menuItems.filter { it.categoryId == filterCategoryId }
        }
    }

    // Initialize selected item on start if empty
    androidx.compose.runtime.LaunchedEffect(menuItems) {
        if (selectedItemIds.isEmpty() && menuItems.isNotEmpty()) {
            val first = menuItems.first()
            selectedItemIds.add(first.id)
            val initialPrice = (first.discountedPrice ?: (first.originalPrice * 0.8)).toInt().toString()
            manualPrices[first.id] = initialPrice
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF141414))
        ) {
            // 1. Top Header Bar (الترويسة العلوية كما في لقطة الشاشة)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Right Button: رجوع للإعدادات ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1E1E1E))
                        .border(BorderStroke(1.dp, Color(0xFFE5A93C).copy(alpha = 0.5f)), RoundedCornerShape(20.dp))
                        .clickable { onBack() }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "رجوع للإعدادات",
                            color = Color(0xFFE5A93C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFFE5A93C),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Center Title: العروض والتخفيضات
                Text(
                    text = "العروض والتخفيضات",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                // Left Button: Circular Close (X)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222222))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // 2. Mode Selector (ثلاثة خيارات: تخفيض مادة | تخفيض قسم كامل | المواد والأقسام المخفضة)
                val currentlyDiscountedItems = remember(menuItems) { menuItems.filter { it.hasDiscount } }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E1E1E))
                        .border(BorderStroke(1.dp, Color(0xFF2E2E2E)), RoundedCornerShape(14.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Option 0: تخفيض مادة
                    val isItemMode = selectedMode == 0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isItemMode) Color(0xFFE5A93C) else Color.Transparent)
                            .clickable { selectedMode = 0 }
                            .padding(vertical = 7.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = if (isItemMode) Color(0xFF141414) else Color(0xFFCCCCCC),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تخفيض مادة",
                                color = if (isItemMode) Color(0xFF141414) else Color(0xFFCCCCCC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Option 1: تخفيض قسم كامل
                    val isCategoryMode = selectedMode == 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCategoryMode) Color(0xFFE5A93C) else Color.Transparent)
                            .clickable { selectedMode = 1 }
                            .padding(vertical = 7.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = if (isCategoryMode) Color(0xFF141414) else Color(0xFFCCCCCC),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تخفيض قسم",
                                color = if (isCategoryMode) Color(0xFF141414) else Color(0xFFCCCCCC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Option 2: المواد والأقسام المخفضة
                    val isDiscountedMode = selectedMode == 2
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDiscountedMode) Color(0xFFE5A93C) else Color.Transparent)
                            .clickable { selectedMode = 2 }
                            .padding(vertical = 7.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isDiscountedMode) Color(0xFF141414) else Color(0xFFCCCCCC),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentlyDiscountedItems.isNotEmpty()) "المخفضة (${currentlyDiscountedItems.size})" else "المواد المخفضة",
                                color = if (isDiscountedMode) Color(0xFF141414) else Color(0xFFCCCCCC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedMode == 0) {
                    // MODE 0: تحديد مواد من أقسام مختلفة (المطابق للصورة تماماً)

                    // 3. Filter Bar (تصفية: الكل + الأقسام)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تصفية:",
                            color = Color(0xFFAAAAAA),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Chip "الكل (count)"
                            item {
                                val isAllSelected = filterCategoryId == null
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isAllSelected) Color(0xFFE5A93C) else Color(0xFF222222))
                                        .border(BorderStroke(1.dp, if (isAllSelected) Color(0xFFE5A93C) else Color(0xFF383838)), RoundedCornerShape(14.dp))
                                        .clickable { filterCategoryId = null }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "الكل (${menuItems.size})",
                                        color = if (isAllSelected) Color(0xFF141414) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }

                            // Category chips
                            items(categories) { cat ->
                                val isSelected = filterCategoryId == cat.id
                                val iconPrefix = when {
                                    cat.name.contains("ساخنة") -> "☕ "
                                    cat.name.contains("باردة") -> "🍹 "
                                    cat.name.contains("حلويات") -> "🍰 "
                                    cat.name.contains("وجبات") -> "🍔 "
                                    else -> "☕ "
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) Color(0xFFE5A93C) else Color(0xFF222222))
                                        .border(BorderStroke(1.dp, if (isSelected) Color(0xFFE5A93C) else Color(0xFF383838)), RoundedCornerShape(14.dp))
                                        .clickable { filterCategoryId = cat.id }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "$iconPrefix${cat.name}",
                                        color = if (isSelected) Color(0xFF141414) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 4. Quick Actions + Discount Method Switcher (شريط مدمج يوفر مساحة كبيرة لتقليب المواد)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1A1A1A))
                            .border(BorderStroke(1.dp, Color(0xFF282828)), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick selection actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "تحديد الكل (${visibleItems.size})",
                                color = Color(0xFFE5A93C),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                modifier = Modifier.clickable {
                                    visibleItems.forEach { item ->
                                        if (!selectedItemIds.contains(item.id)) {
                                            selectedItemIds.add(item.id)
                                        }
                                        if (!manualPrices.containsKey(item.id)) {
                                            manualPrices[item.id] = (item.discountedPrice ?: (item.originalPrice * 0.8)).toInt().toString()
                                        }
                                    }
                                }
                            )

                            if (selectedItemIds.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "إلغاء التحديد (${selectedItemIds.size})",
                                    color = Color(0xFFE53935),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.clickable {
                                        selectedItemIds.clear()
                                    }
                                )
                            }
                        }

                        // Compact discount method toggle (% vs manual price)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color(0xFF121212))
                                .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(7.dp))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            val isPercent = discountMethod == 0
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(if (isPercent) Color(0xFFE5A93C) else Color.Transparent)
                                    .clickable { discountMethod = 0 }
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "% نسبة",
                                    color = if (isPercent) Color(0xFF141414) else Color(0xFFAAAAAA),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            }

                            val isManual = discountMethod == 1
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(if (isManual) Color(0xFFE5A93C) else Color.Transparent)
                                    .clickable { discountMethod = 1 }
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "د.ع يدوي",
                                    color = if (isManual) Color(0xFF141414) else Color(0xFFAAAAAA),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 5. Items List (مساحة واسعة جداً لتقليب واختيار المواد وتخفيضها)
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(visibleItems) { item ->
                            val isSelected = selectedItemIds.contains(item.id)
                            val isItemDiscounted = item.discountedPrice != null

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            selectedItemIds.remove(item.id)
                                        } else {
                                            selectedItemIds.add(item.id)
                                            if (!manualPrices.containsKey(item.id)) {
                                                manualPrices[item.id] = (item.discountedPrice ?: (item.originalPrice * 0.8)).toInt().toString()
                                            }
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) Color(0xFFE5A93C) else Color(0xFF2E2E2E)
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Checkbox on the right
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) Color(0xFFE5A93C) else Color(0xFF141414))
                                                .border(
                                                    BorderStroke(1.5.dp, if (isSelected) Color(0xFFE5A93C) else Color(0xFF555555)),
                                                    RoundedCornerShape(6.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color(0xFF141414),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Item Thumbnail
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF282828))
                                        ) {
                                            CafeIconBadge(
                                                iconName = item.iconName,
                                                customImageUri = item.customImageUri,
                                                size = 46.dp,
                                                containerColor = Color(0xFF282828)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Item Name and Price
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            if (isItemDiscounted) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "${item.discountedPrice?.toInt()} د.ع",
                                                        color = Color(0xFFE5A93C),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.5.sp
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "${item.originalPrice.toInt()} د.ع",
                                                        textDecoration = TextDecoration.LineThrough,
                                                        color = Color(0xFF888888),
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    text = "${item.originalPrice.toInt()} د.ع",
                                                    color = Color(0xFFE5A93C),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.5.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Quick Toggle Offers Visibility directly from Offers Management!
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (item.showInOffers) Color(0xFFE5A93C).copy(alpha = 0.18f) else Color(0xFF282828))
                                                .border(
                                                    BorderStroke(1.dp, if (item.showInOffers) Color(0xFFE5A93C).copy(alpha = 0.5f) else Color(0xFF3E3E3E)),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    viewModel.toggleMenuItemOffersVisibility(item)
                                                    val msg = if (item.showInOffers) "تم إخفاء ${item.name} من قسم العروض" else "تم إظهار ${item.name} في قسم العروض"
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.LocalOffer,
                                                    contentDescription = null,
                                                    tint = if (item.showInOffers) Color(0xFFE5A93C) else Color(0xFF888888),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (item.showInOffers) "في العروض" else "مخفية",
                                                    color = if (item.showInOffers) Color(0xFFE5A93C) else Color(0xFF888888),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Quick Offer Background Button directly on each item
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (item.offerBackgroundImageUri != null) Color(0xFFE5A93C).copy(alpha = 0.22f) else Color(0xFF282828))
                                                .border(
                                                    BorderStroke(1.dp, if (item.offerBackgroundImageUri != null) Color(0xFFE5A93C).copy(alpha = 0.6f) else Color(0xFF3E3E3E)),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    itemForOfferBgUpload = item
                                                    offerBgPickerLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                }
                                                .padding(horizontal = 7.dp, vertical = 5.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.AddPhotoAlternate,
                                                    contentDescription = null,
                                                    tint = if (item.offerBackgroundImageUri != null) Color(0xFFE5A93C) else Color(0xFFCCCCCC),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = if (item.offerBackgroundImageUri != null) "الخلفية 🖼️" else "خلفية",
                                                    color = if (item.offerBackgroundImageUri != null) Color(0xFFE5A93C) else Color(0xFFCCCCCC),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    // If this item is selected AND discountMethod is manual price:
                                    // Dedicated input box matching the screenshot!
                                    if (isSelected && discountMethod == 1) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Divider(color = Color(0xFF2C2C2C))
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Text(
                                                text = "السعر المخفض:",
                                                color = Color(0xFFE5A93C),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(100.dp)
                                                    .height(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF141414))
                                                    .border(BorderStroke(1.dp, Color(0xFFE5A93C)), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                BasicTextField(
                                                    value = manualPrices[item.id] ?: "${(item.discountedPrice ?: (item.originalPrice * 0.8)).toInt()}",
                                                    onValueChange = { manualPrices[item.id] = it },
                                                    textStyle = TextStyle(
                                                        color = Color(0xFFE5A93C),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        textAlign = TextAlign.Center
                                                    ),
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "د.ع",
                                                color = Color(0xFFCCCCCC),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedMode == 1) {
                    // MODE 1: تخفيض قسم كامل
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        border = BorderStroke(1.dp, Color(0xFF333333))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "اختر القسم الكامل المراد تخفيضه:",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(categories) { cat ->
                                    val isSelected = selectedWholeCategoryId == cat.id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Color(0xFFE5A93C) else Color(0xFF282828))
                                            .border(BorderStroke(1.dp, if (isSelected) Color(0xFFE5A93C) else Color(0xFF383838)), RoundedCornerShape(12.dp))
                                            .clickable { selectedWholeCategoryId = cat.id }
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = cat.name,
                                            color = if (isSelected) Color(0xFF141414) else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = categoryPercentInput,
                                onValueChange = { categoryPercentInput = it },
                                label = { Text("نسبة التخفيض للقسم كامل (مثال: 15%)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                } else if (selectedMode == 2) {
                    // MODE 2: المواد والأقسام المخفضة (إنهاء التخفيض)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Top Summary Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            border = BorderStroke(1.dp, Color(0xFF2E2E2E))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "المواد والأقسام المخفضة حالياً",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (currentlyDiscountedItems.isEmpty()) "لا توجد تخفيضات نشطة حالياً" else "يوجد ${currentlyDiscountedItems.size} مادة مشمولة بالتخفيض",
                                        color = if (currentlyDiscountedItems.isEmpty()) Color(0xFFAAAAAA) else Color(0xFFE5A93C),
                                        fontSize = 12.sp
                                    )
                                }

                                if (currentlyDiscountedItems.isNotEmpty()) {
                                    Button(
                                        onClick = {
                                            currentlyDiscountedItems.forEach {
                                                viewModel.removeDiscount(it.id)
                                            }
                                            Toast.makeText(context, "تم إنهاء جميع التخفيضات بنجاح", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "إنهاء الكل",
                                            color = Color.White,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (currentlyDiscountedItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = Color(0xFF555555),
                                        modifier = Modifier.size(52.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "لا توجد مواد أو أقسام مخفضة حالياً",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "يمكنك تطبيق تخفيضات جديدة من خلال خياري 'تخفيض مادة' أو 'تخفيض قسم'.",
                                        color = Color(0xFFAAAAAA),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 30.dp)
                            ) {
                                items(currentlyDiscountedItems) { item ->
                                    val catName = categories.firstOrNull { it.id == item.categoryId }?.name ?: ""
                                    val pct = item.discountPercentage ?: (((item.originalPrice - (item.discountedPrice ?: item.originalPrice)) / item.originalPrice) * 100).toInt()

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                                        border = BorderStroke(1.dp, Color(0xFFE5A93C).copy(alpha = 0.25f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Thumbnail + Badge
                                            Box(
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFF262626)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CafeIconBadge(
                                                    iconName = item.iconName,
                                                    customImageUri = item.customImageUri,
                                                    size = 46.dp,
                                                    iconSize = 24.dp,
                                                    containerColor = Color(0xFF262626),
                                                    iconColor = Color(0xFFE5A93C)
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopStart)
                                                        .padding(2.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFFE53935))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "$pct%",
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            // Details
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.name,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp
                                                )
                                                if (catName.isNotBlank()) {
                                                    Text(
                                                        text = catName,
                                                        color = Color(0xFFAAAAAA),
                                                        fontSize = 11.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "${formatArabicNumeralsPrice(item.discountedPrice ?: item.originalPrice)} د.ع",
                                                        color = Color(0xFFE5A93C),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.5.sp
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "${formatArabicNumeralsPrice(item.originalPrice)} د.ع",
                                                        color = Color(0xFF777777),
                                                        fontSize = 11.sp,
                                                        textDecoration = TextDecoration.LineThrough
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // End discount button
                                            Button(
                                                onClick = {
                                                    viewModel.removeDiscount(item.id)
                                                    Toast.makeText(context, "تم إنهاء التخفيض عن ${item.name}", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B1C1C)),
                                                border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.8f)),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = null,
                                                    tint = Color(0xFFE53935),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "إنهاء التخفيض",
                                                    color = Color(0xFFE53935),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Bottom Action Panel (اللوحة السفلية لتطبيق أو إلغاء التخفيض كما في الصورة)
            if (selectedMode == 0 && selectedItemIds.isNotEmpty()) {
                val firstSelectedItem = menuItems.firstOrNull { it.id == selectedItemIds.first() }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
                    border = BorderStroke(1.dp, Color(0xFFE5A93C).copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (selectedItemIds.size == 1) {
                                "تعديل سعر المادة: (${firstSelectedItem?.name ?: ""})"
                            } else {
                                "تعديل أسعار المواد المختارة (${selectedItemIds.size} مواد)"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )

                        if (selectedItemIds.size == 1 && firstSelectedItem != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "السعر الأصلي: ${firstSelectedItem.originalPrice.toInt()} د.ع",
                                color = Color(0xFFAAAAAA),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Offer background configuration for this selected item
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF262626)),
                                border = BorderStroke(0.5.dp, Color(0xFFE5A93C).copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = null,
                                            tint = Color(0xFFE5A93C),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "خلفية بطاقة العرض:",
                                            color = Color.White,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (firstSelectedItem.offerBackgroundImageUri != null) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                            ) {
                                                AsyncImage(
                                                    model = firstSelectedItem.offerBackgroundImageUri,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.updateMenuItem(firstSelectedItem.copy(offerBackgroundImageUri = null))
                                                    Toast.makeText(context, "تم إزالة خلفية العرض لـ ${firstSelectedItem.name}", Toast.LENGTH_SHORT).show()
                                                },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                                modifier = Modifier.height(26.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("إزالة", fontSize = 10.sp, color = Color(0xFFFF6B6B))
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }

                                        Button(
                                            onClick = {
                                                itemForOfferBgUpload = firstSelectedItem
                                                offerBgPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                            modifier = Modifier.height(26.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (firstSelectedItem.offerBackgroundImageUri != null) Color(0xFF383838) else Color(0xFFE5A93C)
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                contentDescription = null,
                                                tint = if (firstSelectedItem.offerBackgroundImageUri != null) Color(0xFFE5A93C) else Color(0xFF141414),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (firstSelectedItem.offerBackgroundImageUri != null) "تغيير الخلفية" else "رفع خلفية",
                                                color = if (firstSelectedItem.offerBackgroundImageUri != null) Color.White else Color(0xFF141414),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (discountMethod == 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = percentageInput,
                                onValueChange = { percentageInput = it },
                                label = { Text("النسبة المئوية (%)", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Apply discount button
                            Button(
                                onClick = {
                                    if (discountMethod == 1) {
                                        // Manual price
                                        selectedItemIds.forEach { id ->
                                            val item = menuItems.firstOrNull { it.id == id } ?: return@forEach
                                            val rawVal = manualPrices[id]?.toDoubleOrNull() ?: (item.discountedPrice ?: (item.originalPrice * 0.8))
                                            viewModel.applyDiscountToItem(id, rawVal, null, true)
                                        }
                                    } else {
                                        // Percentage
                                        val pct = percentageInput.toIntOrNull() ?: 20
                                        selectedItemIds.forEach { id ->
                                            viewModel.applyDiscountToItem(id, null, pct, true)
                                        }
                                    }
                                    Toast.makeText(context, "تم حفظ وتطبيق التخفيض بنجاح!", Toast.LENGTH_SHORT).show()
                                    onBack()
                                },
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A93C))
                            ) {
                                Text(
                                    text = "تطبيق وحفظ التخفيض",
                                    color = Color(0xFF141414),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }

                            // Cancel/remove discount button
                            val hasDiscountedItems = selectedItemIds.any { id ->
                                menuItems.firstOrNull { it.id == id }?.discountedPrice != null
                            }
                            if (hasDiscountedItems) {
                                OutlinedButton(
                                    onClick = {
                                        selectedItemIds.forEach { id ->
                                            viewModel.removeDiscount(id)
                                        }
                                        Toast.makeText(context, "تم إلغاء التخفيض عن المواد المحددة", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    },
                                    modifier = Modifier.height(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE53935))
                                ) {
                                    Text(
                                        text = "إلغاء التخفيض",
                                        color = Color(0xFFE53935),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (selectedMode == 1) {
                // Category bottom button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val pct = categoryPercentInput.toIntOrNull() ?: 15
                                viewModel.applyDiscountToCategory(selectedWholeCategoryId, pct, true)
                                Toast.makeText(context, "تم تخفيض كامل مواد القسم بنجاح!", Toast.LENGTH_SHORT).show()
                                onBack()
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A93C))
                        ) {
                            Text(
                                text = "تطبيق التخفيض على القسم",
                                color = Color(0xFF141414),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.removeCategoryDiscount(selectedWholeCategoryId)
                                Toast.makeText(context, "تم إلغاء تخفيض القسم", Toast.LENGTH_SHORT).show()
                                onBack()
                            },
                            modifier = Modifier.height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE53935))
                        ) {
                            Text(
                                text = "إلغاء تخفيض القسم",
                                color = Color(0xFFE53935),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 5. Cafe Profile & Logo Settings
@Composable
fun CafeProfileSettingsScreen(
    viewModel: CafeViewModel,
    lang: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentCafe by viewModel.currentCafe.collectAsState()

    var cafeNameInput by remember { mutableStateOf(currentCafe?.name ?: "") }
    var selectedLogoIcon by remember { mutableStateOf(currentCafe?.logoIconName ?: "coffee") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = Strings.get("cafe_profile", lang),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Preview
                CafeIconBadge(
                    iconName = selectedLogoIcon,
                    size = 90.dp,
                    iconSize = 50.dp,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = cafeNameInput,
                    onValueChange = { cafeNameInput = it },
                    label = { Text(Strings.get("cafe_name", lang)) },
                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "اختر أيقونة الشعار المميز للكافيه:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(CafeIcons.AVAILABLE_ICONS) { (iconKey, label) ->
                        val isSelected = selectedLogoIcon == iconKey
                        Card(
                            modifier = Modifier.clickable { selectedLogoIcon = iconKey },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CafeIconBadge(iconName = iconKey, size = 44.dp, iconSize = 24.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button (Mandatory Save Pattern)
        Button(
            onClick = {
                if (cafeNameInput.isNotBlank()) {
                    viewModel.saveCafeProfile(
                        name = cafeNameInput.trim(),
                        iconName = selectedLogoIcon,
                        customUri = null
                    )
                    Toast.makeText(context, "تم حفظ بيانات الكافيه بنجاح!", Toast.LENGTH_SHORT).show()
                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Strings.get("save_changes", lang), fontWeight = FontWeight.Bold)
        }
    }
}

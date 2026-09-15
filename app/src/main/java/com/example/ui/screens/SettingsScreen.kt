package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Palette
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    lang: AppLanguage,
    onNavigateTo: (SettingsSubSection) -> Unit
) {
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

        Spacer(modifier = Modifier.height(20.dp))

        val menuOptions = listOf(
            Triple(SettingsSubSection.LANGUAGE, Strings.get("language", lang), Icons.Default.Language),
            Triple(SettingsSubSection.THEMES, "الثيمات ومظهر المينو", Icons.Default.Palette),
            Triple(SettingsSubSection.MENU, Strings.get("menu_management", lang), Icons.Default.RestaurantMenu),
            Triple(SettingsSubSection.TABLES, Strings.get("tables_management", lang), Icons.Default.TableRestaurant),
            Triple(SettingsSubSection.OFFERS, Strings.get("offers_management", lang), Icons.Default.LocalOffer),
            Triple(SettingsSubSection.CAFE_PROFILE, Strings.get("cafe_profile", lang), Icons.Default.Storefront)
        )

        menuOptions.forEach { (section, title, icon) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onNavigateTo(section) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = Strings.get("menu_management", lang),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "إدارة الأقسام والمواد: تغيير الأسماء، رفع الصور، وإخفاء/إظهار",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = { showAddCategoryDialog = true },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(Strings.get("add_category", lang), fontSize = 12.sp)
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
                                    size = 44.dp,
                                    iconSize = 24.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            cat.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        // Visibility Tag
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (cat.isVisible) Color(0xFF4CAF50).copy(alpha = 0.15f)
                                                    else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (cat.isVisible) "ظاهر للزبائن" else "مخفي",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (cat.isVisible) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
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

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

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
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
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
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    item.name,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                // Item Visibility Tag
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(
                                                            if (item.isAvailable) Color(0xFF4CAF50).copy(alpha = 0.15f)
                                                            else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                                        )
                                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = if (item.isAvailable) "ظاهر" else "مخفي",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (item.isAvailable) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                            Text(
                                                "${item.effectivePrice.toInt()} د.ع",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    // Action buttons for Item: Quick Toggle, Edit, Delete
                                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                customUri = customItemImageUri
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

// 4. Offers & Discounts Manager
@Composable
fun OffersManagementScreen(
    viewModel: CafeViewModel,
    lang: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()

    var discountTarget by remember { mutableIntStateOf(0) } // 0: Single Item, 1: Whole Category
    var selectedItemId by remember { mutableStateOf(menuItems.firstOrNull()?.id ?: "") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }

    var discountType by remember { mutableIntStateOf(0) } // 0: Percentage, 1: Fixed Price
    var percentageInput by remember { mutableStateOf("20") }
    var fixedPriceInput by remember { mutableStateOf("") }
    var showInOffersPage by remember { mutableStateOf(true) }

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
                text = Strings.get("offers_management", lang),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Target Selector: Single Item vs Whole Category
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Strings.get("discount_target", lang),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.weight(1f).clickable { discountTarget = 0 },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = discountTarget == 0, onClick = { discountTarget = 0 })
                        Text(Strings.get("discount_single_item", lang), fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.weight(1f).clickable { discountTarget = 1 },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = discountTarget == 1, onClick = { discountTarget = 1 })
                        Text(Strings.get("discount_whole_section", lang), fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (discountTarget == 0) {
                    Text("اختر المادة المراد تخفيضها:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(menuItems) { item ->
                            val isSelected = selectedItemId == item.id
                            Card(
                                modifier = Modifier.clickable { selectedItemId = item.id },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("${item.originalPrice.toInt()} د.ع", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                } else {
                    Text("اختر القسم الكامل المراد تخفيضه:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            val isSelected = selectedCategoryId == cat.id
                            Card(
                                modifier = Modifier.clickable { selectedCategoryId = cat.id },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    cat.name,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Discount Method: Percentage or Fixed Price
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Strings.get("discount_type", lang),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.weight(1f).clickable { discountType = 0 },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = discountType == 0, onClick = { discountType = 0 })
                        Text(Strings.get("discount_percentage", lang))
                    }
                    if (discountTarget == 0) {
                        Row(
                            modifier = Modifier.weight(1f).clickable { discountType = 1 },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = discountType == 1, onClick = { discountType = 1 })
                            Text(Strings.get("discount_fixed_price", lang))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (discountType == 0) {
                    OutlinedTextField(
                        value = percentageInput,
                        onValueChange = { percentageInput = it },
                        label = { Text("النسبة المئوية (مثال: 20%)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                } else {
                    OutlinedTextField(
                        value = fixedPriceInput,
                        onValueChange = { fixedPriceInput = it },
                        label = { Text("السعر المخفض الجديد (بالدينار)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle: Show in Offers Page
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Strings.get("show_in_offers_page", lang),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "إبراز المادة بشكل ملفت في صفحة العروضات الخاصة مع زر إضافة مباشر",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showInOffersPage,
                        onCheckedChange = { showInOffersPage = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Button (Mandatory Save Pattern)
        Button(
            onClick = {
                if (discountTarget == 0) {
                    val pct = if (discountType == 0) percentageInput.toIntOrNull() else null
                    val fixed = if (discountType == 1) fixedPriceInput.toDoubleOrNull() else null
                    viewModel.applyDiscountToItem(selectedItemId, fixed, pct, showInOffersPage)
                } else {
                    val pct = percentageInput.toIntOrNull()
                    viewModel.applyDiscountToCategory(selectedCategoryId, pct, showInOffersPage)
                }
                Toast.makeText(context, "تم حفظ وتطبيق التخفيض بنجاح!", Toast.LENGTH_SHORT).show()
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

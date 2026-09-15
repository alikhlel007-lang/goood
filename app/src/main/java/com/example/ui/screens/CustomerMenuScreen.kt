package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.TableEntity
import com.example.ui.components.CafeIconBadge
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.Strings
import com.example.ui.viewmodel.CafeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMenuScreen(
    viewModel: CafeViewModel,
    lang: AppLanguage,
    tableNumberOverride: Int? = null,
    isWaiterMode: Boolean = false,
    onTableChangeRequested: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val currentCafe by viewModel.currentCafe.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()
    val specialOffers by viewModel.specialOffers.collectAsState()
    val tables by viewModel.tables.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val cartSubtotal by viewModel.cartSubtotal.collectAsState()
    val cartServiceFee by viewModel.cartServiceFee.collectAsState()
    val cartTotal by viewModel.cartTotal.collectAsState()

    val customerTableNum by viewModel.selectedCustomerTable.collectAsState()
    val effectiveTableNum = tableNumberOverride ?: customerTableNum
    val currentTable = tables.firstOrNull { it.tableNumber == effectiveTableNum }

    val visibleCategories = remember(categories) { categories.filter { it.isVisible } }
    val visibleMenuItems = remember(menuItems, visibleCategories) {
        menuItems.filter { it.isAvailable && visibleCategories.any { cat -> cat.id == it.categoryId } }
    }
    val visibleSpecialOffers = remember(specialOffers, visibleCategories) {
        specialOffers.filter { it.isAvailable && visibleCategories.any { cat -> cat.id == it.categoryId } }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Regular Menu, 1: Special Offers
    var selectedCategoryId by remember { mutableStateOf<String?>("ALL") }
    var showCartSheet by remember { mutableStateOf(false) }
    var showOrderSentDialog by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val customerName by viewModel.customerNameInput.collectAsState()
    val orderNotes by viewModel.orderNotesInput.collectAsState()

    val totalCartCount = cart.values.sumOf { it.quantity }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF141414))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Action Row: Left has "cart 🛍", Right has Table selector / badge
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // LEFT SIDE: Cart Button matching screenshot
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE5A93C))
                            .clickable { showCartSheet = true }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (totalCartCount > 0) "cart ($totalCartCount)" else "cart",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF141414),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Cart",
                                tint = Color(0xFF141414),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // RIGHT SIDE: Table indicator or Table selection pills
                    if (isWaiterMode || onTableChangeRequested != null) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(tables) { table ->
                                val isSelected = table.tableNumber == effectiveTableNum
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFFE5A93C) else Color(0xFF222222))
                                        .border(
                                            BorderStroke(1.dp, if (isSelected) Color(0xFFE5A93C) else Color(0xFF383838)),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            viewModel.setCustomerTable(table.tableNumber)
                                            onTableChangeRequested?.invoke(table.tableNumber)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "#${table.tableNumber}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color(0xFF141414) else Color(0xFFCCCCCC)
                                    )
                                }
                            }
                        }
                    } else {
                        // Customer Table Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF222222))
                                .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TableRestaurant,
                                    contentDescription = null,
                                    tint = Color(0xFFE5A93C),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${Strings.get("table", lang)} #$effectiveTableNum",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE5A93C)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Welcome Card Banner (البانر الترحيبي كما في الصورة تماماً)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
                border = BorderStroke(1.dp, Color(0xFFE5A93C).copy(alpha = 0.35f))
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left / Center: welcomeMessage pill + Slogan
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF262626))
                                    .border(
                                        BorderStroke(1.dp, Color(0xFFE5A93C).copy(alpha = 0.4f)),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "welcomeMessage ♡",
                                    color = Color(0xFFE5A93C),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "أجواء مودرن ونكهات لا تُنسى",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.End
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Right side: Coffee Cup Icon Badge
                        CafeIconBadge(
                            iconName = currentCafe?.logoIconName ?: "coffee",
                            customImageUri = currentCafe?.logoCustomUri,
                            size = 54.dp,
                            iconSize = 32.dp,
                            containerColor = Color(0xFF262626),
                            iconColor = Color(0xFFE5A93C)
                        )
                    }
                }
            }

            // 3. Category Selector Pills
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val isSelected = selectedCategoryId == "ALL" && selectedTabIndex == 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFFE5A93C) else Color(0xFF222222))
                            .border(
                                BorderStroke(1.dp, if (isSelected) Color(0xFFE5A93C) else Color(0xFF383838)),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                selectedTabIndex = 0
                                selectedCategoryId = "ALL"
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "الكل",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) Color(0xFF141414) else Color(0xFFCCCCCC)
                        )
                    }
                }

                items(visibleCategories) { cat ->
                    val isSelected = selectedCategoryId == cat.id && selectedTabIndex == 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFFE5A93C) else Color(0xFF222222))
                            .border(
                                BorderStroke(1.dp, if (isSelected) Color(0xFFE5A93C) else Color(0xFF383838)),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                selectedTabIndex = 0
                                selectedCategoryId = cat.id
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CafeIconBadge(
                                iconName = cat.iconName,
                                customImageUri = cat.customImageUri,
                                size = 20.dp,
                                iconSize = 14.dp,
                                containerColor = Color.Transparent,
                                iconColor = if (isSelected) Color(0xFF141414) else Color(0xFFE5A93C)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) Color(0xFF141414) else Color(0xFFCCCCCC)
                            )
                        }
                    }
                }

                if (visibleSpecialOffers.isNotEmpty()) {
                    item {
                        val isSelected = selectedTabIndex == 1
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFFE5A93C) else Color(0xFF222222))
                            .border(
                                BorderStroke(1.dp, if (isSelected) Color(0xFFE5A93C) else Color(0xFF383838)),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedTabIndex = 1 }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) Color(0xFF141414) else Color(0xFFE5A93C)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Strings.get("offers_title", lang),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color(0xFF141414) else Color(0xFFCCCCCC)
                                )
                            }
                        }
                    }
                }
            }

            // Main Content Area
            if (selectedTabIndex == 0) {
                // Regular Menu Page
                Column(modifier = Modifier.fillMaxSize()) {
                    // Filtered Menu Items List
                    val filteredItems = if (selectedCategoryId == "ALL") {
                        visibleMenuItems
                    } else {
                        visibleMenuItems.filter { it.categoryId == selectedCategoryId }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp)
                    ) {
                        items(filteredItems) { item ->
                            val cartItem = cart[item.id]
                            MenuItemRegularCard(
                                item = item,
                                cartQty = cartItem?.quantity ?: 0,
                                lang = lang,
                                onAdd = { viewModel.addToCart(item) },
                                onRemove = { viewModel.decreaseQuantity(item) }
                            )
                        }
                    }
                }
            } else {
                // Special Offers Page - Eye Catching Design
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "عروض الكافيه الحصرية لزبائننا",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "استمتع بأسعار مخفضة ووجبات مميزة لفترة محدودة",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    if (specialOffers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لا توجد عروض خاصة مفعلة حالياً في صفحة العروضات",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(visibleSpecialOffers) { offerItem ->
                            SpecialOfferCard(
                                item = offerItem,
                                lang = lang,
                                onAddToCart = {
                                    viewModel.addToCart(offerItem)
                                    Toast.makeText(context, "تمت الإضافة للسلة!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Cart Button
        if (totalCartCount > 0) {
            FloatingActionButton(
                onClick = { showCartSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("floating_cart_button"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text("$totalCartCount")
                            }
                        }
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = "Cart")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${cartTotal.toInt()} ${Strings.get("iqd", lang)}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Cart Modal BottomSheet
        if (showCartSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showCartSheet = false },
                sheetState = sheetState
            ) {
                CartSheetContent(
                    cart = cart,
                    cartSubtotal = cartSubtotal,
                    cartServiceFee = cartServiceFee,
                    cartTotal = cartTotal,
                    customerName = customerName,
                    orderNotes = orderNotes,
                    onCustomerNameChange = { viewModel.customerNameInput.value = it },
                    onOrderNotesChange = { viewModel.orderNotesInput.value = it },
                    lang = lang,
                    validationError = validationError,
                    onIncrease = { viewModel.addToCart(it) },
                    onDecrease = { viewModel.decreaseQuantity(it) },
                    onRemove = { viewModel.removeFromCart(it) },
                    onDismiss = { showCartSheet = false },
                    onSubmit = {
                        validationError = null
                        viewModel.submitOrder(
                            targetTable = effectiveTableNum,
                            onSuccess = {
                                showCartSheet = false
                                showOrderSentDialog = true
                            },
                            onError = { errKey ->
                                validationError = Strings.get(errKey, lang)
                            }
                        )
                    }
                )
            }
        }

        // Order Sent Success Dialog
        if (showOrderSentDialog) {
            AlertDialog(
                onDismissRequest = { showOrderSentDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { Text(Strings.get("order_sent_success", lang), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
                text = {
                    Text(
                        text = "تم استلام طلبك لطاولة #$effectiveTableNum، ودخل مرحلة الموافقة لدى الكاشير.\nشكراً لزيارتكم!",
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(onClick = { showOrderSentDialog = false }) {
                        Text("موافق")
                    }
                }
            )
        }
    }
}

fun formatArabicNumeralsPrice(price: Double): String {
    val intPrice = price.toInt()
    val formatted = String.format(java.util.Locale.US, "%,d", intPrice)
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val sb = StringBuilder()
    for (ch in formatted) {
        if (ch in '0'..'9') {
            sb.append(arabicDigits[ch - '0'])
        } else if (ch == ',') {
            sb.append('،')
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

@Composable
fun MenuItemRegularCard(
    item: MenuItemEntity,
    cartQty: Int,
    lang: AppLanguage,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        border = BorderStroke(1.dp, Color(0xFFE5A93C).copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // Enforce LTR inside the card so Image is strictly on the LEFT, and Arabic text on the RIGHT
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT SIDE: Image + order+ button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(82.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF282828)),
                        contentAlignment = Alignment.Center
                    ) {
                        CafeIconBadge(
                            iconName = item.iconName,
                            customImageUri = item.customImageUri,
                            size = 64.dp,
                            iconSize = 34.dp,
                            containerColor = Color(0xFF262626),
                            iconColor = Color(0xFFE5A93C)
                        )

                        if (item.hasDiscount) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(3.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color(0xFFE53935))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${item.discountPercentage ?: ""}%",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // order + button
                    if (cartQty > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF282828))
                                .border(BorderStroke(1.dp, Color(0xFFE5A93C)), RoundedCornerShape(10.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = onRemove,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color(0xFFE5A93C), modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = "$cartQty",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = onAdd,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color(0xFFE5A93C), modifier = Modifier.size(14.dp))
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF222222))
                                .border(BorderStroke(1.dp, Color(0xFFE5A93C).copy(alpha = 0.8f)), RoundedCornerShape(10.dp))
                                .clickable { onAdd() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "order +",
                                color = Color(0xFFE5A93C),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // RIGHT SIDE: Arabic text (Title, Description, Price) aligned to End/Right
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEEEEEE),
                            fontSize = 15.sp
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (item.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFAAAAAA),
                                fontSize = 11.5.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val effectivePrice = if (item.hasDiscount && item.discountedPrice != null) item.discountedPrice else item.originalPrice
                    Text(
                        text = "${formatArabicNumeralsPrice(effectivePrice)} د.ع",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5A93C),
                            fontSize = 15.sp
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun SpecialOfferCard(
    item: MenuItemEntity,
    lang: AppLanguage,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                CafeIconBadge(
                    iconName = item.iconName,
                    customImageUri = item.customImageUri,
                    size = 80.dp,
                    iconSize = 48.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    iconColor = MaterialTheme.colorScheme.primary
                )

                // Discount Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.error)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "خصم حارق ${item.discountPercentage ?: ""}%",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (item.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${Strings.get("old_price", lang)}: ${item.originalPrice.toInt()} ${Strings.get("iqd", lang)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "${Strings.get("new_price", lang)}: ${item.discountedPrice?.toInt() ?: item.originalPrice.toInt()} ${Strings.get("iqd", lang)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }

                    Button(
                        onClick = onAddToCart,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Strings.get("add_to_cart", lang), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CartSheetContent(
    cart: Map<String, com.example.ui.viewmodel.CartItem>,
    cartSubtotal: Double,
    cartServiceFee: Double,
    cartTotal: Double,
    customerName: String,
    orderNotes: String,
    onCustomerNameChange: (String) -> Unit,
    onOrderNotesChange: (String) -> Unit,
    lang: AppLanguage,
    validationError: String?,
    onIncrease: (MenuItemEntity) -> Unit,
    onDecrease: (MenuItemEntity) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Strings.get("cart", lang),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (cart.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(Strings.get("empty_cart", lang), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            // Cart Items List
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cart.values.toList()) { cartItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cartItem.item.name, fontWeight = FontWeight.Bold)
                            Text(
                                "${cartItem.item.effectivePrice.toInt()} × ${cartItem.quantity} = ${(cartItem.item.effectivePrice * cartItem.quantity).toInt()} ${Strings.get("iqd", lang)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onDecrease(cartItem.item) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Text("${cartItem.quantity}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                            IconButton(onClick = { onIncrease(cartItem.item) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mandatory Customer Name Field
            OutlinedTextField(
                value = customerName,
                onValueChange = onCustomerNameChange,
                label = { Text(Strings.get("customer_name_required", lang)) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_name_input"),
                singleLine = true,
                isError = validationError != null && customerName.isBlank()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Optional Notes Field
            OutlinedTextField(
                value = orderNotes,
                onValueChange = onOrderNotesChange,
                label = { Text(Strings.get("order_notes", lang)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 2
            )

            if (validationError != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = validationError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Price Calculation & Iraqi Service Fee Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(Strings.get("subtotal", lang), style = MaterialTheme.typography.bodyMedium)
                        Text("${cartSubtotal.toInt()} ${Strings.get("iqd", lang)}", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            Strings.get("service_fee", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "${cartServiceFee.toInt()} ${Strings.get("iqd", lang)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "* تُحسب رسوم الخدمة 1% أو بحد أدنى 250 د.ع لعدم وجود فئة نقدية أقل في العراق.",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)),
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            Strings.get("total_amount", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "${cartTotal.toInt()} ${Strings.get("iqd", lang)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions: موافق وإرسال / تراجع
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Strings.get("cancel_dismiss", lang))
                }

                Button(
                    onClick = onSubmit,
                    modifier = Modifier.weight(1.5f).height(48.dp).testTag("confirm_and_send_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Strings.get("confirm_and_send", lang), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

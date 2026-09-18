package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CafeEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.TableEntity
import com.example.data.local.entity.UserAccountEntity
import com.example.data.firebase.FirestoreMenuSync
import com.example.data.remote.EmailVerificationService
import com.example.data.qr.QrCodeGenerator
import com.example.data.repository.CafeRepository
import com.example.ui.locale.AppLanguage
import com.example.ui.theme.AppThemes
import com.example.ui.theme.CornerStyle
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class CartItem(
    val item: MenuItemEntity,
    val quantity: Int
)

class CafeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = CafeRepository(db.cafeDao())
    private val prefs = application.getSharedPreferences("cafe_app_prefs", Context.MODE_PRIVATE)
    private val firestoreSync = FirestoreMenuSync(db.cafeDao(), viewModelScope)

    private val _currentThemePreset = MutableStateFlow(
        AppThemes.fromId(prefs.getString("selected_theme_id", AppThemes.MIDNIGHT_GOLD.id))
    )
    val currentThemePreset: StateFlow<ThemePreset> = _currentThemePreset.asStateFlow()

    private val _themeMode = MutableStateFlow(
        runCatching {
            ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.DARK.name) ?: ThemeMode.DARK.name)
        }.getOrDefault(ThemeMode.DARK)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _cornerStyle = MutableStateFlow(
        runCatching {
            CornerStyle.valueOf(prefs.getString("corner_style", CornerStyle.MODERN_ROUNDED.name) ?: CornerStyle.MODERN_ROUNDED.name)
        }.getOrDefault(CornerStyle.MODERN_ROUNDED)
    )
    val cornerStyle: StateFlow<CornerStyle> = _cornerStyle.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.ARABIC)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _currentUser = MutableStateFlow<UserAccountEntity?>(null)
    val currentUser: StateFlow<UserAccountEntity?> = _currentUser.asStateFlow()

    val currentCafe: StateFlow<CafeEntity?> = repository.currentCafe
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val categories: StateFlow<List<CategoryEntity>> = repository.getCategories(CafeRepository.DEFAULT_CAFE_ID)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val menuItems: StateFlow<List<MenuItemEntity>> = repository.getAllMenuItems(CafeRepository.DEFAULT_CAFE_ID)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val specialOffers: StateFlow<List<MenuItemEntity>> = repository.getSpecialOffers(CafeRepository.DEFAULT_CAFE_ID)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tables: StateFlow<List<TableEntity>> = repository.getTables(CafeRepository.DEFAULT_CAFE_ID)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.getAllOrders(CafeRepository.DEFAULT_CAFE_ID)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active customer table
    private val _selectedCustomerTable = MutableStateFlow(1)
    val selectedCustomerTable: StateFlow<Int> = _selectedCustomerTable.asStateFlow()

    // Waiter manual order selected table
    private val _selectedWaiterTable = MutableStateFlow(1)
    val selectedWaiterTable: StateFlow<Int> = _selectedWaiterTable.asStateFlow()

    // Cart
    private val _cart = MutableStateFlow<Map<String, CartItem>>(emptyMap())
    val cart: StateFlow<Map<String, CartItem>> = _cart.asStateFlow()

    val customerNameInput = MutableStateFlow("")
    val orderNotesInput = MutableStateFlow("")

    val cartSubtotal: StateFlow<Double> = _cart.map { cartMap ->
        cartMap.values.sumOf { it.item.effectivePrice * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartServiceFee: StateFlow<Double> = cartSubtotal.map { subtotal ->
        if (subtotal <= 0.0) 0.0 else CafeRepository.calculateServiceFee(subtotal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartTotal: StateFlow<Double> = combine(cartSubtotal, cartServiceFee) { sub, fee ->
        if (sub <= 0.0) 0.0 else kotlin.math.ceil((sub + fee) / 250.0) * 250.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedInitialDataIfNeeded()
            // Start listening to real-time changes from Firestore
            firestoreSync.startListening(CafeRepository.DEFAULT_CAFE_ID)
            // If Firestore is empty, seed with initial items
            val existing = repository.getAllMenuItems(CafeRepository.DEFAULT_CAFE_ID)
            val initialItems = existing.firstOrNull() ?: emptyList()
            if (initialItems.isNotEmpty()) {
                firestoreSync.seedLocalItemsToFirestore(CafeRepository.DEFAULT_CAFE_ID, initialItems)
            }
            val initialCafe = repository.currentCafe.firstOrNull()
            if (initialCafe != null) {
                firestoreSync.syncCafeProfileToFirestore(initialCafe.id, initialCafe.name, initialCafe.logoIconName)
            }
        }
    }

    fun setThemePreset(preset: ThemePreset) {
        _currentThemePreset.value = preset
        prefs.edit().putString("selected_theme_id", preset.id).apply()
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun setCornerStyle(style: CornerStyle) {
        _cornerStyle.value = style
        prefs.edit().putString("corner_style", style.name).apply()
    }

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    fun setCustomerTable(tableNum: Int) {
        _selectedCustomerTable.value = tableNum
    }

    fun setWaiterTable(tableNum: Int) {
        _selectedWaiterTable.value = tableNum
    }

    fun addToCart(item: MenuItemEntity) {
        val current = _cart.value.toMutableMap()
        val existing = current[item.id]
        if (existing != null) {
            current[item.id] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current[item.id] = CartItem(item, 1)
        }
        _cart.value = current
    }

    fun decreaseQuantity(item: MenuItemEntity) {
        val current = _cart.value.toMutableMap()
        val existing = current[item.id] ?: return
        if (existing.quantity > 1) {
            current[item.id] = existing.copy(quantity = existing.quantity - 1)
        } else {
            current.remove(item.id)
        }
        _cart.value = current
    }

    fun removeFromCart(itemId: String) {
        val current = _cart.value.toMutableMap()
        current.remove(itemId)
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyMap()
        customerNameInput.value = ""
        orderNotesInput.value = ""
    }

    fun submitOrder(
        targetTable: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val name = customerNameInput.value.trim()
        if (name.isEmpty()) {
            onError("customer_name_error")
            return
        }
        val currentCart = _cart.value.values.toList()
        if (currentCart.isEmpty()) {
            onError("empty_cart")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val subtotal = currentCart.sumOf { it.item.effectivePrice * it.quantity }
            val summary = currentCart.joinToString(", ") { "${it.quantity}x ${it.item.name}" }
            val jsonArray = JSONArray()
            currentCart.forEach {
                val obj = JSONObject()
                obj.put("id", it.item.id)
                obj.put("name", it.item.name)
                obj.put("price", it.item.effectivePrice)
                obj.put("qty", it.quantity)
                jsonArray.put(obj)
            }

            repository.placeOrder(
                cafeId = CafeRepository.DEFAULT_CAFE_ID,
                tableNumber = targetTable,
                customerName = name,
                orderNotes = orderNotesInput.value.trim(),
                itemsSummary = summary,
                itemsJson = jsonArray.toString(),
                subtotal = subtotal
            )

            launch(Dispatchers.Main) {
                clearCart()
                onSuccess()
            }
        }
    }

    fun updateOrderStatus(orderId: String, nextStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateOrderStatus(orderId, nextStatus)
        }
    }

    fun payIndividualOrder(orderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.settleOrderIndividual(orderId)
        }
    }

    fun payWholeTable(tableNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.settleTableOrdersCombined(CafeRepository.DEFAULT_CAFE_ID, tableNumber)
        }
    }

    fun saveCafeProfile(name: String, iconName: String, customUri: String?) {
        val cafe = currentCafe.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = cafe.copy(
                name = name,
                logoIconName = iconName,
                logoCustomUri = customUri
            )
            repository.saveCafeProfile(updated)
            firestoreSync.syncCafeProfileToFirestore(updated.id, updated.name, updated.logoIconName)
        }
    }

    fun addCategory(name: String, iconName: String, customUri: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCategory(CafeRepository.DEFAULT_CAFE_ID, name, iconName, customUri)
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCategory(categoryId)
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCategory(category)
        }
    }

    fun toggleCategoryVisibility(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCategory(category.copy(isVisible = !category.isVisible))
        }
    }

    fun addMenuItem(
        categoryId: String,
        name: String,
        description: String,
        price: Double,
        iconName: String,
        customUri: String? = null,
        showInOffers: Boolean = false,
        offerBackgroundUri: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = repository.addMenuItem(CafeRepository.DEFAULT_CAFE_ID, categoryId, name, description, price, iconName, customUri, showInOffers, offerBackgroundUri)
            firestoreSync.syncMenuItemToFirestore(item)
        }
    }

    fun updateMenuItemOfferBackground(item: MenuItemEntity, bgUri: String?) {
        val updated = item.copy(offerBackgroundImageUri = bgUri)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMenuItem(updated)
            firestoreSync.syncMenuItemToFirestore(updated)
        }
    }

    fun updateMenuItem(item: MenuItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMenuItem(item)
            firestoreSync.syncMenuItemToFirestore(item)
        }
    }

    fun toggleMenuItemVisibility(item: MenuItemEntity) {
        val updated = item.copy(isAvailable = !item.isAvailable)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMenuItem(updated)
            firestoreSync.syncMenuItemToFirestore(updated)
        }
    }

    fun toggleMenuItemOffersVisibility(item: MenuItemEntity) {
        val updated = item.copy(showInOffers = !item.showInOffers)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMenuItem(updated)
            firestoreSync.syncMenuItemToFirestore(updated)
        }
    }

    fun deleteMenuItem(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMenuItem(itemId)
            firestoreSync.deleteMenuItemFromFirestore(CafeRepository.DEFAULT_CAFE_ID, itemId)
        }
    }

    fun applyDiscountToItem(itemId: String, newPrice: Double?, percentage: Int?, isSpecialOffer: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = repository.applyDiscountToItem(itemId, newPrice, percentage, isSpecialOffer)
            if (updated != null) {
                firestoreSync.syncMenuItemToFirestore(updated)
            }
        }
    }

    fun removeDiscount(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = repository.applyDiscountToItem(itemId, null, null, false)
            if (updated != null) {
                firestoreSync.syncMenuItemToFirestore(updated)
            }
        }
    }

    fun applyDiscountToCategory(categoryId: String, percentage: Int?, isSpecialOffer: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedList = repository.applyDiscountToCategory(categoryId, percentage, isSpecialOffer)
            for (item in updatedList) {
                firestoreSync.syncMenuItemToFirestore(item)
            }
        }
    }

    fun removeCategoryDiscount(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedList = repository.applyDiscountToCategory(categoryId, null, false)
            for (item in updatedList) {
                firestoreSync.syncMenuItemToFirestore(item)
            }
        }
    }

    fun updateTablesCount(newCount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTableCountAndRegenerate(CafeRepository.DEFAULT_CAFE_ID, newCount)
        }
    }

    fun updateTableType(tableNum: Int, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTableType(CafeRepository.DEFAULT_CAFE_ID, tableNum, type)
        }
    }

    fun regenerateAllQrCodes() {
        val currentCount = tables.value.size.coerceAtLeast(1)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTableCountAndRegenerate(CafeRepository.DEFAULT_CAFE_ID, currentCount)
        }
    }

    fun exportQrPdf(context: Context, onGenerated: (File) -> Unit) {
        val cafe = currentCafe.value?.name ?: "كافيه"
        val currentTables = tables.value
        viewModelScope.launch(Dispatchers.IO) {
            val file = QrCodeGenerator.createTablesPdf(context, cafe, currentTables)
            launch(Dispatchers.Main) {
                onGenerated(file)
            }
        }
    }

    fun login(email: String, pass: String, onSuccess: (UserAccountEntity) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.login(email.trim(), pass.trim())
            launch(Dispatchers.Main) {
                if (user != null) {
                    _currentUser.value = user
                    onSuccess(user)
                } else {
                    onError("بيانات الدخول غير صحيحة")
                }
            }
        }
    }

    // Pending registration state for email OTP verification
    private var pendingOtpCode: String? = null
    private var pendingOtpExpiry: Long = 0L
    private var pendingName: String = ""
    private var pendingEmail: String = ""
    private var pendingPass: String = ""
    private var pendingRole: String = "OWNER"
    private var pendingCafeName: String = ""

    fun sendRegistrationOtp(
        name: String,
        email: String,
        pass: String,
        cafeName: String,
        onCodeSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val trimmedEmail = email.trim()
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                launch(Dispatchers.Main) { onError("صيغة البريد الإلكتروني غير صحيحة") }
                return@launch
            }

            // Generate secure 6-digit random code
            val code = (100000 + kotlin.random.Random.nextInt(900000)).toString()
            pendingOtpCode = code
            pendingOtpExpiry = System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutes
            pendingName = name.trim()
            pendingEmail = trimmedEmail
            pendingPass = pass.trim()
            pendingCafeName = cafeName.trim()

            val result = EmailVerificationService.sendVerificationCode(
                recipientEmail = trimmedEmail,
                code = code,
                cafeName = cafeName.ifBlank { "كافيه النخيل" }
            )

            launch(Dispatchers.Main) {
                if (result.isSuccess) {
                    onCodeSent()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "تعذر إرسال رمز التحقق إلى البريد")
                }
            }
        }
    }

    fun verifyOtpAndCompleteRegistration(
        enteredCode: String,
        onSuccess: (UserAccountEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        val trimmedCode = enteredCode.trim()
        val now = System.currentTimeMillis()

        if (pendingOtpCode == null || now > pendingOtpExpiry) {
            onError("انتهت صلاحية الرمز، يرجى طلب رمز جديد")
            return
        }

        if (trimmedCode != pendingOtpCode) {
            onError("رمز التحقق غير صحيح، يرجى المحاولة مجدداً")
            return
        }

        // Code verified, proceed with registration
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = repository.registerUser(
                    pendingName,
                    pendingEmail,
                    pendingPass,
                    pendingRole,
                    CafeRepository.DEFAULT_CAFE_ID
                )
                if (pendingCafeName.isNotBlank()) {
                    repository.saveCafeProfile(
                        CafeEntity(
                            id = CafeRepository.DEFAULT_CAFE_ID,
                            name = pendingCafeName,
                            logoIconName = "coffee",
                            ownerEmail = pendingEmail
                        )
                    )
                }
                pendingOtpCode = null // consume code
                launch(Dispatchers.Main) {
                    _currentUser.value = user
                    onSuccess(user)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onError(e.message ?: "فشل إتمام إنشاء الحساب")
                }
            }
        }
    }

    fun resendOtp(
        onCodeSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (pendingEmail.isBlank()) {
            onError("لا توجد بيانات تسجيل معلقة")
            return
        }
        sendRegistrationOtp(
            name = pendingName,
            email = pendingEmail,
            pass = pendingPass,
            cafeName = pendingCafeName,
            onCodeSent = onCodeSent,
            onError = onError
        )
    }

    fun register(name: String, email: String, pass: String, role: String, onSuccess: (UserAccountEntity) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = repository.registerUser(name.trim(), email.trim(), pass.trim(), role, CafeRepository.DEFAULT_CAFE_ID)
                launch(Dispatchers.Main) {
                    _currentUser.value = user
                    onSuccess(user)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onError(e.message ?: "فشل إنشاء الحساب")
                }
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun quickCustomerMode(tableNum: Int) {
        _selectedCustomerTable.value = tableNum
        _currentUser.value = UserAccountEntity(
            id = "guest_${System.currentTimeMillis()}",
            email = "guest@customer.com",
            passwordHash = "",
            name = "زبون طاولة #$tableNum",
            role = "CUSTOMER",
            cafeId = CafeRepository.DEFAULT_CAFE_ID
        )
    }

    override fun onCleared() {
        super.onCleared()
        firestoreSync.stopListening()
    }
}

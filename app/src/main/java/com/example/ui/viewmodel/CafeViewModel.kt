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
import com.example.data.qr.QrCodeGenerator
import com.example.data.repository.CafeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await
import com.example.ui.locale.AppLanguage
import com.example.ui.theme.AppThemes
import com.example.ui.theme.CornerStyle
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

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

    val activeCafeId: StateFlow<String> = _currentUser.map { user ->
        user?.cafeId ?: prefs.getString("last_active_cafe_id", CafeRepository.DEFAULT_CAFE_ID) ?: CafeRepository.DEFAULT_CAFE_ID
    }.stateIn(viewModelScope, SharingStarted.Eagerly, prefs.getString("last_active_cafe_id", CafeRepository.DEFAULT_CAFE_ID) ?: CafeRepository.DEFAULT_CAFE_ID)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentCafe: StateFlow<CafeEntity?> = activeCafeId.flatMapLatest { cid ->
        repository.getCafe(cid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: StateFlow<List<CategoryEntity>> = activeCafeId.flatMapLatest { cid ->
        repository.getCategories(cid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val menuItems: StateFlow<List<MenuItemEntity>> = activeCafeId.flatMapLatest { cid ->
        repository.getAllMenuItems(cid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val specialOffers: StateFlow<List<MenuItemEntity>> = activeCafeId.flatMapLatest { cid ->
        repository.getSpecialOffers(cid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val tables: StateFlow<List<TableEntity>> = activeCafeId.flatMapLatest { cid ->
        repository.getTables(cid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allOrders: StateFlow<List<OrderEntity>> = activeCafeId.flatMapLatest { cid ->
        repository.getAllOrders(cid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        }

        viewModelScope.launch {
            activeCafeId.collectLatest { cid ->
                prefs.edit().putString("last_active_cafe_id", cid).apply()
                // Sync profile & menu items for active cafe to Firestore
                val cafe = repository.getCafe(cid).firstOrNull()
                if (cafe != null) {
                    firestoreSync.syncCafeProfileToFirestore(cafe.id, cafe.name, cafe.logoIconName)
                }
                val items = repository.getAllMenuItems(cid).firstOrNull() ?: emptyList()
                if (items.isNotEmpty()) {
                    firestoreSync.seedLocalItemsToFirestore(cid, items)
                }
                firestoreSync.startListening(cid)
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
                cafeId = activeCafeId.value,
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
            repository.settleTableOrdersCombined(activeCafeId.value, tableNumber)
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
            repository.addCategory(activeCafeId.value, name, iconName, customUri)
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
            val item = repository.addMenuItem(activeCafeId.value, categoryId, name, description, price, iconName, customUri, showInOffers, offerBackgroundUri)
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
            firestoreSync.deleteMenuItemFromFirestore(activeCafeId.value, itemId)
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
            repository.updateTableCountAndRegenerate(activeCafeId.value, newCount)
        }
    }

    fun updateTableType(tableNum: Int, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTableType(activeCafeId.value, tableNum, type)
        }
    }

    fun regenerateAllQrCodes() {
        val currentCount = tables.value.size.coerceAtLeast(1)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTableCountAndRegenerate(activeCafeId.value, currentCount)
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
            val trimmedEmail = email.trim()
            val trimmedPass = pass.trim()

            // 1. Try checking with Firebase Auth if account exists in Firebase
            val firebaseAuth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
            if (firebaseAuth != null) {
                try {
                    val authResult = firebaseAuth.signInWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                    val fbUser = authResult.user
                    if (fbUser != null) {
                        // Reload user state to get latest email verification status
                        fbUser.reload().await()
                        if (!fbUser.isEmailVerified) {
                            launch(Dispatchers.Main) {
                                onError("لم يتم تفعيل حسابك بعد! يرجى فتح الرسالة المرسلة لبريدك والضغط على رابط التأكيد.")
                            }
                            return@launch
                        }
                    }
                } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                    launch(Dispatchers.Main) { onError("كلمة المرور أو البريد الإلكتروني غير صحيح") }
                    return@launch
                } catch (e: com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                    // Might be a local fallback account (e.g. admin@cafe.com)
                } catch (e: Exception) {
                    // Network or other issue, allow fallback check below
                }
            }

            // 2. Check local database
            val user = repository.login(trimmedEmail, trimmedPass)
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

    // Pending registration state for email verification
    private var pendingName: String = ""
    private var pendingEmail: String = ""
    private var pendingPass: String = ""
    private var pendingCafeName: String = ""

    /**
     * Registers owner with Firebase Auth and sends a real Email Verification Link.
     */
    fun registerWithFirebaseEmailVerification(
        name: String,
        email: String,
        pass: String,
        cafeName: String,
        onVerificationLinkSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val trimmedEmail = email.trim()
            val trimmedPass = pass.trim()
            val trimmedName = name.trim()
            val trimmedCafe = cafeName.trim()

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                launch(Dispatchers.Main) { onError("صيغة البريد الإلكتروني غير صحيحة") }
                return@launch
            }
            if (trimmedPass.length < 6) {
                launch(Dispatchers.Main) { onError("كلمة المرور يجب أن تكون 6 خانات على الأقل") }
                return@launch
            }

            pendingName = trimmedName
            pendingEmail = trimmedEmail
            pendingPass = trimmedPass
            pendingCafeName = trimmedCafe

            try {
                // Generate a unique cafe ID
                val cleanPrefix = trimmedCafe.filter { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' }.lowercase().take(8)
                val randomSuffix = UUID.randomUUID().toString().take(6).lowercase()
                val newCafeId = if (cleanPrefix.isNotBlank()) "cafe_${cleanPrefix}_$randomSuffix" else "cafe_$randomSuffix"
                val actualCafeName = trimmedCafe.ifBlank { "كافيه $trimmedName" }

                val firebaseAuth = FirebaseAuth.getInstance()
                
                // 1. Create user in Firebase Auth
                var fbUser = try {
                    val createResult = firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                    createResult.user
                } catch (collision: FirebaseAuthUserCollisionException) {
                    // If user already registered in Firebase, sign in to send verification link
                    val signInResult = firebaseAuth.signInWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                    signInResult.user
                }

                if (fbUser != null) {
                    // Update display name
                    try {
                        val profileUpdates = userProfileChangeRequest {
                            displayName = trimmedName
                        }
                        fbUser.updateProfile(profileUpdates).await()
                    } catch (e: Exception) {
                        // Non-blocking
                    }

                    // 2. Send official Firebase Email Verification Link
                    fbUser.sendEmailVerification().await()
                }

                // 3. Register user in local database and create cafe profile
                val localUser = repository.registerUser(
                    trimmedName,
                    trimmedEmail,
                    trimmedPass,
                    "OWNER",
                    newCafeId
                )
                val newCafe = CafeEntity(
                    id = newCafeId,
                    name = actualCafeName,
                    logoIconName = "coffee",
                    ownerEmail = trimmedEmail
                )
                repository.saveCafeProfile(newCafe)
                repository.seedNewCafeDefaults(newCafeId, actualCafeName)
                firestoreSync.syncCafeProfileToFirestore(newCafeId, actualCafeName, "coffee")
                val starterItems = repository.getAllMenuItems(newCafeId).firstOrNull() ?: emptyList()
                if (starterItems.isNotEmpty()) {
                    firestoreSync.seedLocalItemsToFirestore(newCafeId, starterItems)
                }

                launch(Dispatchers.Main) {
                    onVerificationLinkSent()
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    val msg = e.localizedMessage ?: "حدث خطأ أثناء إنشاء الحساب"
                    onError(msg)
                }
            }
        }
    }

    /**
     * Resends the Firebase email verification link.
     */
    fun resendVerificationEmail(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetEmail = email.ifBlank { pendingEmail }.trim()
            val targetPass = pass.ifBlank { pendingPass }.trim()

            if (targetEmail.isBlank() || targetPass.isBlank()) {
                launch(Dispatchers.Main) { onError("يرجى إدخال البريد الإلكتروني وكلمة المرور") }
                return@launch
            }

            try {
                val firebaseAuth = FirebaseAuth.getInstance()
                val user = firebaseAuth.currentUser ?: run {
                    val signInResult = firebaseAuth.signInWithEmailAndPassword(targetEmail, targetPass).await()
                    signInResult.user
                }

                if (user != null) {
                    user.sendEmailVerification().await()
                    launch(Dispatchers.Main) { onSuccess() }
                } else {
                    launch(Dispatchers.Main) { onError("تعذر العثور على حسابك لإعادة الإرسال") }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "فشل إعادة إرسال الرابط")
                }
            }
        }
    }

    /**
     * Checks if the user has clicked the verification link and completes sign-in.
     */
    fun checkEmailVerifiedAndProceed(
        email: String,
        pass: String,
        onSuccess: (UserAccountEntity) -> Unit,
        onNotVerifiedYet: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetEmail = email.ifBlank { pendingEmail }.trim()
            val targetPass = pass.ifBlank { pendingPass }.trim()

            try {
                val firebaseAuth = FirebaseAuth.getInstance()
                val signInResult = firebaseAuth.signInWithEmailAndPassword(targetEmail, targetPass).await()
                val user = signInResult.user

                if (user != null) {
                    user.reload().await()
                    if (user.isEmailVerified) {
                        val localUser = repository.login(targetEmail, targetPass)
                        launch(Dispatchers.Main) {
                            if (localUser != null) {
                                _currentUser.value = localUser
                                onSuccess(localUser)
                            } else {
                                onError("تم تأكيد البريد بنجاح! يرجى تسجيل الدخول.")
                            }
                        }
                    } else {
                        launch(Dispatchers.Main) {
                            onNotVerifiedYet()
                        }
                    }
                } else {
                    launch(Dispatchers.Main) { onError("تعذر التحقق من الحساب") }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "حدث خطأ أثناء فحص حالة التفعيل")
                }
            }
        }
    }


    fun register(name: String, email: String, pass: String, role: String, onSuccess: (UserAccountEntity) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cleanPrefix = name.filter { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' }.lowercase().take(6)
                val randomSuffix = UUID.randomUUID().toString().take(6).lowercase()
                val newCafeId = if (cleanPrefix.isNotBlank()) "cafe_${cleanPrefix}_$randomSuffix" else "cafe_$randomSuffix"
                val cafeName = "كافيه $name"

                val user = repository.registerUser(name.trim(), email.trim(), pass.trim(), role, newCafeId)
                val newCafe = CafeEntity(
                    id = newCafeId,
                    name = cafeName,
                    logoIconName = "coffee",
                    ownerEmail = email.trim()
                )
                repository.saveCafeProfile(newCafe)
                repository.seedNewCafeDefaults(newCafeId, cafeName)
                firestoreSync.syncCafeProfileToFirestore(newCafeId, cafeName, "coffee")
                val starterItems = repository.getAllMenuItems(newCafeId).firstOrNull() ?: emptyList()
                if (starterItems.isNotEmpty()) {
                    firestoreSync.seedLocalItemsToFirestore(newCafeId, starterItems)
                }

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
            cafeId = activeCafeId.value
        )
    }

    override fun onCleared() {
        super.onCleared()
        firestoreSync.stopListening()
    }
}

package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.dao.CafeDao
import com.example.data.local.entity.CafeEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.TableEntity
import com.example.data.local.entity.UserAccountEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

class CafeRepository(private val cafeDao: CafeDao) {

    companion object {
        const val DEFAULT_CAFE_ID = "cafe_iraq_main"

        /**
         * 1% service fee with minimum 250 IQD rule.
         * In Iraq, there is no currency smaller than 250 IQD, so the fee is always rounded up
         * to the nearest multiple of 250 IQD (250, 500, 750, 1000, etc.).
         */
        fun calculateServiceFee(subtotal: Double): Double {
            if (subtotal <= 0.0) return 0.0
            val onePercent = subtotal * 0.01
            val rawFee = if (onePercent < 250.0) 250.0 else onePercent
            return kotlin.math.ceil(rawFee / 250.0) * 250.0
        }

        /**
         * Rounds any cash amount to a multiple of 250 IQD (250, 500, 750, 1000...)
         * because the smallest Iraqi currency denomination is 250 IQD.
         */
        fun roundTo250IQD(amount: Double): Double {
            if (amount <= 0.0) return 0.0
            return kotlin.math.ceil(amount / 250.0) * 250.0
        }
    }

    val currentCafe: Flow<CafeEntity?> = cafeDao.getFirstCafe()

    fun getCategories(cafeId: String): Flow<List<CategoryEntity>> = cafeDao.getCategories(cafeId)

    fun getAllMenuItems(cafeId: String): Flow<List<MenuItemEntity>> = cafeDao.getAllMenuItems(cafeId)

    fun getSpecialOffers(cafeId: String): Flow<List<MenuItemEntity>> = cafeDao.getSpecialOffers(cafeId)

    fun getTables(cafeId: String): Flow<List<TableEntity>> = cafeDao.getTables(cafeId)

    fun getAllOrders(cafeId: String): Flow<List<OrderEntity>> = cafeDao.getAllOrders(cafeId)

    fun getUnpaidOrders(cafeId: String): Flow<List<OrderEntity>> = cafeDao.getUnpaidOrders(cafeId)

    fun getUnpaidOrdersForTable(cafeId: String, tableNum: Int): Flow<List<OrderEntity>> =
        cafeDao.getUnpaidOrdersForTable(cafeId, tableNum)

    suspend fun saveCafeProfile(cafe: CafeEntity) {
        cafeDao.insertCafe(cafe)
    }

    suspend fun addCategory(cafeId: String, name: String, iconName: String, customUri: String? = null) {
        val category = CategoryEntity(
            id = UUID.randomUUID().toString(),
            cafeId = cafeId,
            name = name,
            iconName = iconName,
            customImageUri = customUri,
            sortOrder = 0
        )
        cafeDao.insertCategory(category)
    }

    suspend fun deleteCategory(categoryId: String) {
        cafeDao.deleteItemsInCategory(categoryId)
        cafeDao.deleteCategory(categoryId)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        cafeDao.updateCategory(category)
    }

    suspend fun addMenuItem(
        cafeId: String,
        categoryId: String,
        name: String,
        description: String,
        price: Double,
        iconName: String,
        customUri: String? = null
    ) {
        val item = MenuItemEntity(
            id = UUID.randomUUID().toString(),
            cafeId = cafeId,
            categoryId = categoryId,
            name = name,
            description = description,
            originalPrice = price,
            iconName = iconName,
            customImageUri = customUri
        )
        cafeDao.insertMenuItem(item)
    }

    suspend fun updateMenuItem(item: MenuItemEntity) {
        cafeDao.updateMenuItem(item)
    }

    suspend fun deleteMenuItem(itemId: String) {
        cafeDao.deleteMenuItem(itemId)
    }

    suspend fun applyDiscountToItem(
        itemId: String,
        newPrice: Double?,
        percentage: Int?,
        isSpecialOffer: Boolean
    ) {
        val items = cafeDao.getAllMenuItems(DEFAULT_CAFE_ID).firstOrNull() ?: return
        val target = items.firstOrNull { it.id == itemId } ?: return
        val discounted = if (percentage != null && percentage > 0) {
            target.originalPrice * (1.0 - (percentage / 100.0))
        } else {
            newPrice
        }
        val updated = target.copy(
            discountedPrice = discounted,
            discountPercentage = percentage,
            isSpecialOffer = isSpecialOffer
        )
        cafeDao.updateMenuItem(updated)
    }

    suspend fun applyDiscountToCategory(
        categoryId: String,
        percentage: Int?,
        isSpecialOffer: Boolean
    ) {
        val items = cafeDao.getAllMenuItems(DEFAULT_CAFE_ID).firstOrNull() ?: return
        val categoryItems = items.filter { it.categoryId == categoryId }
        for (item in categoryItems) {
            val discounted = if (percentage != null && percentage > 0) {
                item.originalPrice * (1.0 - (percentage / 100.0))
            } else null
            val updated = item.copy(
                discountedPrice = discounted,
                discountPercentage = percentage,
                isSpecialOffer = isSpecialOffer
            )
            cafeDao.updateMenuItem(updated)
        }
    }

    suspend fun updateTableCountAndRegenerate(cafeId: String, newCount: Int, defaultType: String = "INDOOR") {
        val existing = cafeDao.getTables(cafeId).firstOrNull() ?: emptyList()
        val tables = mutableListOf<TableEntity>()
        for (i in 1..newCount) {
            val prev = existing.firstOrNull { it.tableNumber == i }
            val type = prev?.tableType ?: defaultType
            val token = "CAFE-${cafeId.takeLast(4).uppercase()}-TBL-$i-${UUID.randomUUID().toString().take(6).uppercase()}"
            tables.add(
                TableEntity(
                    id = prev?.id ?: UUID.randomUUID().toString(),
                    cafeId = cafeId,
                    tableNumber = i,
                    tableType = type,
                    qrToken = token
                )
            )
        }
        cafeDao.deleteAllTables(cafeId)
        cafeDao.insertTables(tables)
    }

    suspend fun updateTableType(cafeId: String, tableNumber: Int, newType: String) {
        val table = cafeDao.getTableByNumber(cafeId, tableNumber) ?: return
        cafeDao.updateTable(table.copy(tableType = newType))
    }

    suspend fun placeOrder(
        cafeId: String,
        tableNumber: Int,
        customerName: String,
        orderNotes: String,
        itemsSummary: String,
        itemsJson: String,
        subtotal: Double
    ): OrderEntity {
        val fee = calculateServiceFee(subtotal)
        val total = roundTo250IQD(subtotal + fee)
        val order = OrderEntity(
            id = UUID.randomUUID().toString(),
            cafeId = cafeId,
            tableNumber = tableNumber,
            customerName = customerName,
            orderNotes = orderNotes,
            itemsSummary = itemsSummary,
            itemsJson = itemsJson,
            subtotal = subtotal,
            serviceFee = fee,
            totalAmount = total,
            status = "APPROVAL",
            isPaid = false
        )
        cafeDao.insertOrder(order)
        return order
    }

    suspend fun updateOrderStatus(orderId: String, nextStatus: String) {
        cafeDao.updateOrderStatus(orderId, nextStatus)
    }

    suspend fun settleOrderIndividual(orderId: String) {
        cafeDao.markOrderPaid(orderId)
    }

    suspend fun settleTableOrdersCombined(cafeId: String, tableNumber: Int) {
        cafeDao.markTableOrdersPaid(cafeId, tableNumber)
    }

    suspend fun login(email: String, pass: String): UserAccountEntity? {
        val user = cafeDao.getUserByEmail(email) ?: return null
        return if (user.passwordHash == pass) user else null
    }

    suspend fun registerUser(name: String, email: String, pass: String, role: String, cafeId: String): UserAccountEntity {
        val user = UserAccountEntity(
            id = UUID.randomUUID().toString(),
            email = email,
            passwordHash = pass,
            name = name,
            role = role,
            cafeId = cafeId
        )
        cafeDao.insertUser(user)
        return user
    }

    /**
     * Seeds initial cafe, menu, categories and tables if empty.
     */
    suspend fun seedInitialDataIfNeeded() {
        val existingCafe = cafeDao.getFirstCafe().firstOrNull()
        if (existingCafe != null) return

        val cafe = CafeEntity(
            id = DEFAULT_CAFE_ID,
            name = "كافيه النخيل العراقي",
            logoIconName = "coffee",
            ownerEmail = "owner@alnakheel.iq",
            languageCode = "ar"
        )
        cafeDao.insertCafe(cafe)

        val ownerUser = UserAccountEntity(
            id = "user_owner_1",
            email = "admin@cafe.com",
            passwordHash = "123456",
            name = "أبو فهد (مدير الكافيه)",
            role = "OWNER",
            cafeId = DEFAULT_CAFE_ID
        )
        cafeDao.insertUser(ownerUser)

        val catDrinksHot = CategoryEntity(
            id = "cat_hot_drinks",
            cafeId = DEFAULT_CAFE_ID,
            name = "مشروبات ساخنة",
            iconName = "coffee",
            sortOrder = 1
        )
        val catDrinksCold = CategoryEntity(
            id = "cat_cold_drinks",
            cafeId = DEFAULT_CAFE_ID,
            name = "مشروبات باردة وعصائر",
            iconName = "cold_drink",
            sortOrder = 2
        )
        val catSweets = CategoryEntity(
            id = "cat_sweets",
            cafeId = DEFAULT_CAFE_ID,
            name = "الحلويات والمخبوزات",
            iconName = "cake",
            sortOrder = 3
        )
        val catSnacks = CategoryEntity(
            id = "cat_snacks",
            cafeId = DEFAULT_CAFE_ID,
            name = "الوجبات الخفيفة والكرواسون",
            iconName = "breakfast",
            sortOrder = 4
        )
        val catHookah = CategoryEntity(
            id = "cat_hookah",
            cafeId = DEFAULT_CAFE_ID,
            name = "الأراكيل والمعسل",
            iconName = "hookah",
            sortOrder = 5
        )

        cafeDao.insertCategory(catDrinksHot)
        cafeDao.insertCategory(catDrinksCold)
        cafeDao.insertCategory(catSweets)
        cafeDao.insertCategory(catSnacks)
        cafeDao.insertCategory(catHookah)

        val items = listOf(
            MenuItemEntity(
                id = "item_1",
                cafeId = DEFAULT_CAFE_ID,
                categoryId = catDrinksHot.id,
                name = "شاي عراقي مهيل بالاستكانة",
                description = "شاي سيلاني فاخر مخدر على الفحم مع حبات الهيل",
                originalPrice = 1500.0,
                iconName = "tea"
            ),
            MenuItemEntity(
                id = "item_2",
                cafeId = DEFAULT_CAFE_ID,
                categoryId = catDrinksHot.id,
                name = "قهوة تركي بالهيل",
                description = "بن عربي مطحون مع حب الهيل الأخضر ورغوة كثيفة",
                originalPrice = 2500.0,
                iconName = "coffee"
            ),
            MenuItemEntity(
                id = "item_3",
                cafeId = DEFAULT_CAFE_ID,
                categoryId = catDrinksHot.id,
                name = "كابتشينو إيطالي كريمي",
                description = "إسبريسو غني مع رغوة حليب ناعمة ورشة كاكاو",
                originalPrice = 4500.0,
                discountedPrice = 3500.0,
                discountPercentage = 22,
                isSpecialOffer = true,
                iconName = "espresso"
            ),
            MenuItemEntity(
                id = "item_4",
                cafeId = DEFAULT_CAFE_ID,
                categoryId = catDrinksCold.id,
                name = "عصير ليمون ونعناع منعش",
                description = "ليمون طازج مع أوراق النعناع وقطع الثلج",
                originalPrice = 3000.0,
                iconName = "juice"
            ),
            MenuItemEntity(
                id = "item_5",
                cafeId = DEFAULT_CAFE_ID,
                categoryId = catDrinksCold.id,
                name = "ميلك شيك شوكولاتة أوريو",
                description = "حليب مع آيسكريم فانيليا وبسكويت أوريو وكريمة مخفوقة",
                originalPrice = 5000.0,
                discountedPrice = 4000.0,
                discountPercentage = 20,
                isSpecialOffer = true,
                iconName = "cold_drink"
            ),
            MenuItemEntity(
                id = "item_6",
                cafeId = DEFAULT_CAFE_ID,
                categoryId = catSweets.id,
                name = "كريب نوتيلا وفراولة ملكي",
                description = "كريب رقيق محشو بالنوتيلا مع قطع الفراولة والمكسرات",
                originalPrice = 6000.0,
                discountedPrice = 4800.0,
                discountPercentage = 20,
                isSpecialOffer = true,
                iconName = "cake"
            ),
            MenuItemEntity(
                id = "item_7",
                cafeId = DEFAULT_CAFE_ID,
                categoryId = catSweets.id,
                name = "تشيز كيك لوتس دافئ",
                description = "طبقة بسكويت مقرمشة مع كريمة جبنة وصوص زبدة اللوتس",
                originalPrice = 5500.0,
                iconName = "cake"
            ),
            MenuItemEntity(
                id = "item_8",
                cafeId = DEFAULT_CAFE_ID,
                categoryId = catSnacks.id,
                name = "كرواسون بالجبنة والزعتر",
                description = "كرواسون فرنسي طازج ومقرمش مخبوز يومياً",
                originalPrice = 3500.0,
                iconName = "breakfast"
            ),
            MenuItemEntity(
                id = "item_9",
                cafeId = DEFAULT_CAFE_ID,
                categoryId = catHookah.id,
                name = "أركيلة تفاحتين فاخر",
                description = "رأس فخاري مجهز بأجود أنواع الفحم الطبيعي",
                originalPrice = 7000.0,
                iconName = "hookah"
            )
        )
        for (item in items) {
            cafeDao.insertMenuItem(item)
        }

        // Tables 1 to 10
        val tableList = (1..10).map { i ->
            val type = when {
                i in 1..3 -> "VIP"
                i in 4..7 -> "OUTDOOR"
                else -> "INDOOR"
            }
            TableEntity(
                id = "tbl_$i",
                cafeId = DEFAULT_CAFE_ID,
                tableNumber = i,
                tableType = type,
                qrToken = "CAFE-IRAQ-TBL-$i-${UUID.randomUUID().toString().take(6).uppercase()}"
            )
        }
        cafeDao.insertTables(tableList)
    }
}

package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CafeEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.TableEntity
import com.example.data.local.entity.UserAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CafeDao {
    // Cafe
    @Query("SELECT * FROM cafes WHERE id = :cafeId LIMIT 1")
    fun getCafe(cafeId: String): Flow<CafeEntity?>

    @Query("SELECT * FROM cafes LIMIT 1")
    fun getFirstCafe(): Flow<CafeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCafe(cafe: CafeEntity)

    @Update
    suspend fun updateCafe(cafe: CafeEntity)

    // Users
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity)

    // Categories
    @Query("SELECT * FROM categories WHERE cafeId = :cafeId ORDER BY sortOrder ASC, name ASC")
    fun getCategories(cafeId: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: String)

    @Query("DELETE FROM menu_items WHERE categoryId = :categoryId")
    suspend fun deleteItemsInCategory(categoryId: String)

    // Menu Items
    @Query("SELECT * FROM menu_items WHERE cafeId = :cafeId ORDER BY name ASC")
    fun getAllMenuItems(cafeId: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE cafeId = :cafeId AND categoryId = :categoryId")
    fun getItemsByCategory(cafeId: String, categoryId: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE cafeId = :cafeId AND isSpecialOffer = 1")
    fun getSpecialOffers(cafeId: String): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity)

    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    @Query("DELETE FROM menu_items WHERE id = :itemId")
    suspend fun deleteMenuItem(itemId: String)

    @Query("UPDATE menu_items SET discountedPrice = :discountedPrice, discountPercentage = :percentage, isSpecialOffer = :isSpecialOffer WHERE categoryId = :categoryId")
    suspend fun updateCategoryDiscount(categoryId: String, discountedPrice: Double?, percentage: Int?, isSpecialOffer: Boolean)

    // Tables
    @Query("SELECT * FROM cafe_tables WHERE cafeId = :cafeId ORDER BY tableNumber ASC")
    fun getTables(cafeId: String): Flow<List<TableEntity>>

    @Query("SELECT * FROM cafe_tables WHERE cafeId = :cafeId AND tableNumber = :tableNum LIMIT 1")
    suspend fun getTableByNumber(cafeId: String, tableNum: Int): TableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTables(tables: List<TableEntity>)

    @Query("DELETE FROM cafe_tables WHERE cafeId = :cafeId")
    suspend fun deleteAllTables(cafeId: String)

    @Query("DELETE FROM cafe_tables WHERE cafeId = :cafeId AND tableNumber = :tableNum")
    suspend fun deleteTableByNumber(cafeId: String, tableNum: Int)

    @Update
    suspend fun updateTable(table: TableEntity)

    // Orders
    @Query("SELECT * FROM orders WHERE cafeId = :cafeId ORDER BY createdAt DESC")
    fun getAllOrders(cafeId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE cafeId = :cafeId AND isPaid = 0 ORDER BY createdAt DESC")
    fun getUnpaidOrders(cafeId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE cafeId = :cafeId AND tableNumber = :tableNumber AND isPaid = 0")
    fun getUnpaidOrdersForTable(cafeId: String, tableNumber: Int): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("UPDATE orders SET isPaid = 1, paidAt = :paidAt, status = 'PAID' WHERE id = :orderId")
    suspend fun markOrderPaid(orderId: String, paidAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET isPaid = 1, paidAt = :paidAt, status = 'PAID' WHERE cafeId = :cafeId AND tableNumber = :tableNumber AND isPaid = 0")
    suspend fun markTableOrdersPaid(cafeId: String, tableNumber: Int, paidAt: Long = System.currentTimeMillis())
}

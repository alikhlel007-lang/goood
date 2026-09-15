package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey val id: String,
    val cafeId: String,
    val categoryId: String,
    val name: String,
    val description: String = "",
    val originalPrice: Double,
    val discountedPrice: Double? = null,
    val discountPercentage: Int? = null,
    val isSpecialOffer: Boolean = false,
    val iconName: String = "coffee",
    val customImageUri: String? = null,
    val isAvailable: Boolean = true
) {
    val effectivePrice: Double
        get() = discountedPrice ?: originalPrice

    val hasDiscount: Boolean
        get() = discountedPrice != null && discountedPrice < originalPrice
}

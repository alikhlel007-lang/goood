package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cafes")
data class CafeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val logoIconName: String = "coffee",
    val logoCustomUri: String? = null,
    val ownerEmail: String = "owner@cafe.com",
    val languageCode: String = "ar",
    val defaultCurrency: String = "د.ع",
    val serviceFeePercentage: Double = 1.0, // 1%
    val minimumServiceFee: Double = 250.0  // 250 IQD
)

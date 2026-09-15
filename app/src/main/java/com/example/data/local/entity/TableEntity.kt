package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cafe_tables")
data class TableEntity(
    @PrimaryKey val id: String,
    val cafeId: String,
    val tableNumber: Int,
    val tableType: String = "INDOOR", // "VIP", "OUTDOOR", "INDOOR"
    val qrToken: String
)

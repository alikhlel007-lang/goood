package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val cafeId: String,
    val tableNumber: Int,
    val customerName: String,
    val orderNotes: String = "",
    val itemsSummary: String, // e.g. "2x كابتشينو, 1x كريب شوكولاتة"
    val itemsJson: String,
    val subtotal: Double,
    val serviceFee: Double,
    val totalAmount: Double,
    val status: String = "APPROVAL", // APPROVAL -> PREPARING -> DELIVERY -> INVOICE -> PAID
    val isPaid: Boolean = false,
    val paidAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

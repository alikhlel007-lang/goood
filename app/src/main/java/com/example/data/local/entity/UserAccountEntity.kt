package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserAccountEntity(
    @PrimaryKey val id: String,
    val email: String,
    val passwordHash: String,
    val name: String,
    val role: String = "OWNER", // "OWNER", "CASHIER", "CUSTOMER"
    val cafeId: String
)

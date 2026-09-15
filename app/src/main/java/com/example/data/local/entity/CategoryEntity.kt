package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val cafeId: String,
    val name: String,
    val iconName: String = "coffee",
    val customImageUri: String? = null,
    val isVisible: Boolean = true,
    val sortOrder: Int = 0
)

package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val menuItemId: String,
    val restaurantId: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val quantity: Int
)

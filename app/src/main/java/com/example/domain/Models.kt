package com.example.domain

import kotlinx.serialization.Serializable

@Serializable
data class Restaurant(
    val id: String,
    val name: String,
    val imageUrl: String,
    val rating: Double,
    val reviewCount: Int = 0,
    val deliveryTime: String,
    val categories: List<String>,
    val featured: Boolean = false
)

@Serializable
data class MenuItem(
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val isVegetarian: Boolean = false,
    val rating: Double = 0.0,
    val reviewCount: Int = 0
)

@Serializable
data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
)

@Serializable
data class Order(
    val id: String,
    val userId: String,
    val restaurantId: String,
    val items: List<CartItem>,
    val total: Double,
    val status: OrderStatus,
    val timestamp: Long,
    val pointsEarned: Int = 0,
    val discountAmount: Double = 0.0,
    val promoCode: String? = null,
    val promoDiscountAmount: Double = 0.0
)

enum class OrderStatus {
    PENDING, ACCEPTED, PREPARING, ON_THE_WAY, DELIVERED, CANCELLED
}

@Serializable
enum class LoyaltyTier {
    BRONZE, SILVER, GOLD, PLATINUM
}

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val loyaltyPoints: Int = 0,
    val lifetimePoints: Int = 0,
    val loyaltyTier: LoyaltyTier = LoyaltyTier.BRONZE,
    val deliveryAddress: String? = null,
    val paymentMethod: String? = null,
    val profilePictureUrl: String? = null
)

enum class UserRole {
    CUSTOMER, ADMIN
}

@Serializable
enum class TargetType {
    RESTAURANT, MENU_ITEM
}

@Serializable
data class Review(
    val id: String,
    val targetId: String,
    val targetType: TargetType,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val timestamp: Long
)

@Serializable
enum class DiscountType { PERCENTAGE, FIXED }

@Serializable
data class PromoCode(
    val code: String,
    val type: DiscountType,
    val value: Double,
    val minOrderValue: Double = 0.0,
    val isActive: Boolean = true
)

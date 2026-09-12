package com.example.domain

import com.example.data.MockSupabaseClient
import com.example.data.local.CartDao
import com.example.data.local.CartItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CraveRepository(
    private val supabaseClient: MockSupabaseClient,
    private val cartDao: CartDao
) {
    val restaurants: Flow<List<Restaurant>> = supabaseClient.restaurants
    val menuItems: Flow<List<MenuItem>> = supabaseClient.menuItems
    val orders: Flow<List<Order>> = supabaseClient.orders
    val reviews: Flow<List<Review>> = supabaseClient.reviews
    val promoCodes: Flow<List<PromoCode>> = supabaseClient.promoCodes
    val currentUser = supabaseClient.currentUser

    val cartItems: Flow<List<CartItem>> = cartDao.getAllCartItems().map { entities ->
        entities.map { entity ->
            CartItem(
                menuItem = MenuItem(
                    id = entity.menuItemId,
                    restaurantId = entity.restaurantId,
                    name = entity.name,
                    description = "",
                    price = entity.price,
                    imageUrl = entity.imageUrl
                ),
                quantity = entity.quantity
            )
        }
    }

    suspend fun addToCart(menuItem: MenuItem, quantity: Int = 1) {
        val entity = CartItemEntity(
            menuItemId = menuItem.id,
            restaurantId = menuItem.restaurantId,
            name = menuItem.name,
            price = menuItem.price,
            imageUrl = menuItem.imageUrl,
            quantity = quantity
        )
        cartDao.insertCartItem(entity)
    }

    suspend fun removeFromCart(menuItemId: String) {
        cartDao.deleteCartItemById(menuItemId)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    fun getMenuItemsForRestaurant(restaurantId: String): Flow<List<MenuItem>> {
        return menuItems.map { items -> items.filter { it.restaurantId == restaurantId } }
    }

    fun validatePromoCode(code: String): PromoCode? {
        return supabaseClient.promoCodes.value.find { it.code.equals(code, ignoreCase = true) && it.isActive }
    }

    suspend fun placeOrder(items: List<CartItem>, subtotal: Double, pointsRedeemed: Int, appliedPromoCode: PromoCode?, promoDiscount: Double) {
        val loyaltyDiscount = pointsRedeemed / 100.0
        val finalTotal = maxOf(0.0, subtotal - loyaltyDiscount - promoDiscount)
        
        val user = currentUser.value
        val multiplier = when (user?.loyaltyTier) {
            LoyaltyTier.PLATINUM -> 2.0
            LoyaltyTier.GOLD -> 1.5
            LoyaltyTier.SILVER -> 1.2
            else -> 1.0
        }
        val pointsEarned = (finalTotal * 10 * multiplier).toInt()

        val order = Order(
            id = "ORD-${System.currentTimeMillis()}",
            userId = user?.id ?: "unknown",
            restaurantId = items.firstOrNull()?.menuItem?.restaurantId ?: "",
            items = items,
            total = finalTotal,
            status = OrderStatus.PENDING,
            timestamp = System.currentTimeMillis(),
            pointsEarned = pointsEarned,
            discountAmount = loyaltyDiscount,
            promoCode = appliedPromoCode?.code,
            promoDiscountAmount = promoDiscount
        )
        supabaseClient.placeOrder(order, pointsRedeemed)
        clearCart()
    }
    
    fun toggleAdmin() {
        supabaseClient.toggleAdminRole()
    }
    
    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        supabaseClient.updateOrderStatus(orderId, status)
    }

    fun updateProfile(address: String, payment: String) {
        supabaseClient.updateProfile(address, payment)
    }

    fun addReview(targetId: String, targetType: TargetType, rating: Int, comment: String) {
        val user = currentUser.value ?: return
        val review = Review(
            id = "REV-${System.currentTimeMillis()}",
            targetId = targetId,
            targetType = targetType,
            userId = user.id,
            userName = user.name,
            rating = rating,
            comment = comment,
            timestamp = System.currentTimeMillis()
        )
        supabaseClient.addReview(review)
    }
}

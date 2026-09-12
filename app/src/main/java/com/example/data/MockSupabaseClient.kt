package com.example.data

import com.example.domain.MenuItem
import com.example.domain.Order
import com.example.domain.OrderStatus
import com.example.domain.Restaurant
import com.example.domain.Review
import com.example.domain.TargetType
import com.example.domain.User
import com.example.domain.UserRole
import com.example.domain.PromoCode
import com.example.domain.DiscountType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

import com.example.data.local.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A mock client simulating a Supabase real-time database connection.
 * Holds in-memory state that both the user app and admin dashboard can interact with.
 */
class MockSupabaseClient(private val sessionManager: SessionManager) {
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()
    
    private val _promoCodes = MutableStateFlow<List<PromoCode>>(emptyList())
    val promoCodes: StateFlow<List<PromoCode>> = _promoCodes.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    init {
        seedData()
        scope.launch {
            sessionManager.getUserStream().collectLatest { user ->
                if (user != null) {
                    _currentUser.value = user
                } else {
                    val defaultUser = User(
                        id = "user_1", 
                        name = "Guest", 
                        email = "guest@example.com", 
                        role = UserRole.CUSTOMER,
                        loyaltyPoints = 500,
                        lifetimePoints = 500,
                        loyaltyTier = com.example.domain.LoyaltyTier.BRONZE
                    )
                    _currentUser.value = defaultUser
                    sessionManager.saveUser(defaultUser)
                }
            }
        }
    }

    private fun seedData() {
        val seededPromoCodes = listOf(
            PromoCode("WELCOME10", DiscountType.PERCENTAGE, 0.10, 0.0, true),
            PromoCode("SAVE5", DiscountType.FIXED, 5.0, 20.0, true),
            PromoCode("FREEMEAL", DiscountType.PERCENTAGE, 1.0, 100.0, true)
        )
        _promoCodes.value = seededPromoCodes

        val seededReviews = listOf(
            Review("rev_1", "r1", TargetType.RESTAURANT, "user_2", "Alice", 5, "Amazing burgers, very fast delivery!", System.currentTimeMillis() - 86400000),
            Review("rev_2", "r1", TargetType.RESTAURANT, "user_3", "Bob", 4, "Good, but fries were a bit cold.", System.currentTimeMillis() - 40000000),
            Review("rev_3", "r2", TargetType.RESTAURANT, "user_4", "Charlie", 5, "Best pizza in town.", System.currentTimeMillis() - 10000000),
            Review("rev_4", "m1", TargetType.MENU_ITEM, "user_2", "Alice", 5, "Super juicy and fresh.", System.currentTimeMillis() - 80000000)
        )
        _reviews.value = seededReviews

        val r1 = Restaurant(
            id = "r1", name = "Burger Joint",
            imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=600&auto=format&fit=crop",
            rating = 4.5, reviewCount = 2, deliveryTime = "25-35 min", categories = listOf("American", "Burgers"), featured = true
        )
        val r2 = Restaurant(
            id = "r2", name = "Pizza Heaven",
            imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=600&auto=format&fit=crop",
            rating = 5.0, reviewCount = 1, deliveryTime = "30-45 min", categories = listOf("Italian", "Pizza")
        )
        val r3 = Restaurant(
            id = "r3", name = "Sushi Master",
            imageUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?q=80&w=600&auto=format&fit=crop",
            rating = 4.9, reviewCount = 0, deliveryTime = "40-55 min", categories = listOf("Japanese", "Sushi"), featured = true
        )

        _restaurants.value = listOf(r1, r2, r3)

        _menuItems.value = listOf(
            MenuItem("m1", "r1", "Classic Cheeseburger", "Juicy beef patty with melted cheddar.", 12.99, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=300&auto=format&fit=crop", rating = 5.0, reviewCount = 1),
            MenuItem("m2", "r1", "Truffle Fries", "Crispy fries tossed in truffle oil and parmesan.", 6.99, "https://images.unsplash.com/photo-1576107232684-1279f3908594?q=80&w=300&auto=format&fit=crop", true),
            MenuItem("m3", "r2", "Pepperoni Pizza", "Classic slice with spicy pepperoni.", 15.99, "https://images.unsplash.com/photo-1628840042765-356cda07504e?q=80&w=300&auto=format&fit=crop"),
            MenuItem("m4", "r2", "Margherita", "Fresh mozzarella, basil, and tomato sauce.", 14.99, "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?q=80&w=300&auto=format&fit=crop", true),
            MenuItem("m5", "r3", "Spicy Tuna Roll", "Fresh tuna with spicy mayo and cucumber.", 9.99, "https://images.unsplash.com/photo-1553621042-f6e147245754?q=80&w=300&auto=format&fit=crop")
        )
    }

    fun addReview(review: Review) {
        _reviews.update { it + review }
        
        val allTargetReviews = _reviews.value.filter { it.targetId == review.targetId }
        val avg = allTargetReviews.map { it.rating }.average()
        val newAvg = if (avg.isNaN()) 0.0 else Math.round(avg * 10.0) / 10.0
        val count = allTargetReviews.size

        if (review.targetType == TargetType.RESTAURANT) {
            _restaurants.update { list ->
                list.map { if (it.id == review.targetId) it.copy(rating = newAvg, reviewCount = count) else it }
            }
        } else {
            _menuItems.update { list ->
                list.map { if (it.id == review.targetId) it.copy(rating = newAvg, reviewCount = count) else it }
            }
        }
    }

    fun addRestaurant(restaurant: Restaurant) {
        _restaurants.update { it + restaurant }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        _orders.update { current ->
            current.map { if (it.id == orderId) it.copy(status = status) else it }
        }
    }
    
    fun placeOrder(order: Order, pointsRedeemed: Int = 0) {
        _orders.update { listOf(order) + it }
        
        scope.launch {
            val user = _currentUser.value
            if (user != null) {
                val newLifetime = user.lifetimePoints + order.pointsEarned
                val newPoints = user.loyaltyPoints - pointsRedeemed + order.pointsEarned
                val newTier = when {
                    newLifetime >= 10000 -> com.example.domain.LoyaltyTier.PLATINUM
                    newLifetime >= 4000 -> com.example.domain.LoyaltyTier.GOLD
                    newLifetime >= 1000 -> com.example.domain.LoyaltyTier.SILVER
                    else -> com.example.domain.LoyaltyTier.BRONZE
                }
                val updatedUser = user.copy(loyaltyPoints = newPoints, lifetimePoints = newLifetime, loyaltyTier = newTier)
                sessionManager.saveUser(updatedUser)
            }
        }
    }
    
    fun toggleAdminRole() {
        scope.launch {
            val user = _currentUser.value
            if (user != null) {
                val updatedUser = user.copy(role = if (user.role == UserRole.ADMIN) UserRole.CUSTOMER else UserRole.ADMIN) 
                sessionManager.saveUser(updatedUser)
            }
        }
    }
    
    fun updateProfile(address: String, payment: String) {
        scope.launch {
            val user = _currentUser.value
            if (user != null) {
                val updatedUser = user.copy(deliveryAddress = address, paymentMethod = payment)
                sessionManager.saveUser(updatedUser)
            }
        }
    }
}

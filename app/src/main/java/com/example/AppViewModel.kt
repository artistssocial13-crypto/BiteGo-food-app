package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.CartItem
import com.example.domain.CraveRepository
import com.example.domain.MenuItem
import com.example.domain.OrderStatus
import com.example.domain.PromoCode
import com.example.domain.Restaurant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val repository: CraveRepository,
    private val notificationService: com.example.notifications.NotificationService
) : ViewModel() {
    
    val restaurants = repository.restaurants.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val menuItems = repository.menuItems.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val cartItems = repository.cartItems.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val orders = repository.orders.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val reviews = repository.reviews.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val currentUser = repository.currentUser.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _isUploadingAvatar = MutableStateFlow(false)
    val isUploadingAvatar = _isUploadingAvatar.asStateFlow()

    private val _isUploadingBanner = MutableStateFlow(false)
    val isUploadingBanner = _isUploadingBanner.asStateFlow()

    fun addToCart(menuItem: MenuItem, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(menuItem, quantity)
        }
    }

    fun removeFromCart(menuItemId: String) {
        viewModelScope.launch {
            repository.removeFromCart(menuItemId)
        }
    }

    fun placeOrder(items: List<CartItem>, subtotal: Double, pointsRedeemed: Int, appliedPromoCode: PromoCode?, promoDiscount: Double) {
        viewModelScope.launch {
            repository.placeOrder(items, subtotal, pointsRedeemed, appliedPromoCode, promoDiscount)
        }
    }

    fun validatePromoCode(code: String): PromoCode? {
        return repository.validatePromoCode(code)
    }

    fun toggleAdmin() {
        repository.toggleAdmin()
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        repository.updateOrderStatus(orderId, status)
        
        val message = when (status) {
            OrderStatus.ACCEPTED -> "Your order has been accepted!"
            OrderStatus.PREPARING -> "Your order is being prepared."
            OrderStatus.ON_THE_WAY -> "Your order is out for delivery!"
            OrderStatus.DELIVERED -> "Your order has been delivered! Enjoy your meal."
            OrderStatus.CANCELLED -> "Your order has been cancelled."
            else -> "Your order status has been updated."
        }
        notificationService.showOrderStatusNotification(orderId, message)
    }

    fun updateProfile(address: String, payment: String) {
        repository.updateProfile(address, payment)
    }

    fun submitReview(targetId: String, targetType: com.example.domain.TargetType, rating: Int, comment: String) {
        viewModelScope.launch {
            repository.addReview(targetId, targetType, rating, comment)
        }
    }

    fun uploadAvatar(imageBytes: ByteArray, onComplete: (Result<String>) -> Unit = {}) {
        viewModelScope.launch {
            _isUploadingAvatar.value = true
            val result = repository.uploadAvatar(imageBytes)
            _isUploadingAvatar.value = false
            onComplete(result)
        }
    }

    fun uploadRestaurantBanner(restaurantId: String, imageBytes: ByteArray, onComplete: (Result<String>) -> Unit = {}) {
        viewModelScope.launch {
            _isUploadingBanner.value = true
            val result = repository.uploadRestaurantBanner(restaurantId, imageBytes)
            _isUploadingBanner.value = false
            onComplete(result)
        }
    }

    fun addRestaurant(
        name: String,
        deliveryTime: String,
        categories: List<String>,
        imageBytes: ByteArray?,
        onComplete: (Result<Restaurant>) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isUploadingBanner.value = true
            val result = repository.addRestaurantWithBanner(name, deliveryTime, categories, imageBytes)
            _isUploadingBanner.value = false
            onComplete(result)
        }
    }
}

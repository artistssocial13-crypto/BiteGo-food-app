package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class RestaurantDetailRoute(val restaurantId: String, val restaurantName: String)

@Serializable
object CartRoute

@Serializable
object ProfileRoute

@Serializable
object AdminRoute

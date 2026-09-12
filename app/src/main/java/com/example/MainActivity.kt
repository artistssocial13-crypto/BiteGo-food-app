package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import android.os.Build
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.room.Room
import com.example.data.MockSupabaseClient
import com.example.data.local.AppDatabase
import com.example.domain.CraveRepository
import com.example.domain.UserRole
import com.example.ui.navigation.*
import com.example.ui.screens.*
import com.example.ui.theme.CraveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "crave_db").build()
        val sessionManager = com.example.data.local.SessionManager(applicationContext)
        val supabase = MockSupabaseClient(sessionManager)
        val repository = CraveRepository(supabase, db.cartDao())
        val notificationService = com.example.notifications.NotificationService(applicationContext)
        
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppViewModel(repository, notificationService) as T
            }
        }

        enableEdgeToEdge()
        setContent {
            CraveTheme {
                val viewModel: AppViewModel = viewModel(factory = factory)
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val user by viewModel.currentUser.collectAsState()
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission accepted
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    // Check if current route is a bottom nav destination
    val isBottomNavRoute = currentDestination?.route in listOf(
        "com.example.ui.navigation.HomeRoute",
        "com.example.ui.navigation.CartRoute",
        "com.example.ui.navigation.ProfileRoute",
        "com.example.ui.navigation.AdminRoute"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isBottomNavRoute) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentDestination?.hierarchy?.any { it.route == "com.example.ui.navigation.HomeRoute" } == true,
                        onClick = {
                            navController.navigate(HomeRoute) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart") },
                        label = { Text("Cart") },
                        selected = currentDestination?.hierarchy?.any { it.route == "com.example.ui.navigation.CartRoute" } == true,
                        onClick = {
                            navController.navigate(CartRoute) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = currentDestination?.hierarchy?.any { it.route == "com.example.ui.navigation.ProfileRoute" } == true,
                        onClick = {
                            navController.navigate(ProfileRoute) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    if (user?.role == UserRole.ADMIN) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Settings, contentDescription = "Admin") },
                            label = { Text("Admin") },
                            selected = currentDestination?.hierarchy?.any { it.route == "com.example.ui.navigation.AdminRoute" } == true,
                            onClick = {
                                navController.navigate(AdminRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<HomeRoute> {
                val restaurants by viewModel.restaurants.collectAsState()
                HomeScreen(
                    restaurants = restaurants,
                    onRestaurantClick = { id, name ->
                        navController.navigate(RestaurantDetailRoute(id, name))
                    }
                )
            }
            composable<RestaurantDetailRoute> { backStackEntry ->
                val menuItems by viewModel.menuItems.collectAsState()
                val route = backStackEntry.toRoute<RestaurantDetailRoute>()
                
                // Filter menu items by restaurantId
                val restaurantMenu = menuItems.filter { it.restaurantId == route.restaurantId }
                val reviews by viewModel.reviews.collectAsState()
                
                RestaurantDetailScreen(
                    restaurantId = route.restaurantId,
                    restaurantName = route.restaurantName,
                    menuItems = restaurantMenu,
                    reviews = reviews,
                    onBack = { navController.popBackStack() },
                    onAddToCart = { item -> viewModel.addToCart(item) },
                    onSubmitReview = { targetId, targetType, rating, comment -> 
                        viewModel.submitReview(targetId, targetType, rating, comment)
                    }
                )
            }
            composable<CartRoute> {
                val cartItems by viewModel.cartItems.collectAsState()
                CartScreen(
                    cartItems = cartItems,
                    user = user,
                    onRemove = { id -> viewModel.removeFromCart(id) },
                    onValidatePromo = { code -> viewModel.validatePromoCode(code) },
                    onCheckout = { items, total, points, promo, promoDiscount -> 
                        viewModel.placeOrder(items, total, points, promo, promoDiscount) 
                    }
                )
            }
            composable<ProfileRoute> {
                val orders by viewModel.orders.collectAsState()
                ProfileScreen(
                    user = user,
                    orders = orders,
                    onToggleAdmin = { viewModel.toggleAdmin() },
                    onUpdateProfile = { address, payment -> viewModel.updateProfile(address, payment) }
                )
            }
            composable<AdminRoute> {
                val orders by viewModel.orders.collectAsState()
                AdminDashboardScreen(
                    orders = orders,
                    onUpdateOrderStatus = { id, status -> viewModel.updateOrderStatus(id, status) }
                )
            }
        }
    }
}


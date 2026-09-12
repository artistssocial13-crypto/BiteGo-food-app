package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.Order
import com.example.domain.OrderStatus
import com.example.domain.Restaurant
import com.example.ui.theme.Charcoal
import com.example.ui.theme.WarmOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    orders: List<Order>,
    restaurants: List<Restaurant> = emptyList(),
    isUploadingBanner: Boolean = false,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    onUploadBanner: (String, ByteArray) -> Unit = { _, _ -> },
    onAddRestaurant: (String, String, List<String>, ByteArray?) -> Unit = { _, _, _, _ -> }
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddRestaurantDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var selectedRestaurantForBanner by remember { mutableStateOf<String?>(null) }
    val bannerPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val restaurantId = selectedRestaurantForBanner
        if (uri != null && restaurantId != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null && bytes.isNotEmpty()) {
                onUploadBanner(restaurantId, bytes)
            }
        }
        selectedRestaurantForBanner = null
    }

    if (showAddRestaurantDialog) {
        AddRestaurantDialog(
            isUploading = isUploadingBanner,
            onDismiss = { showAddRestaurantDialog = false },
            onConfirm = { name, time, categories, imageBytes ->
                onAddRestaurant(name, time, categories, imageBytes)
                showAddRestaurantDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold, color = WarmOrange) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddRestaurantDialog = true },
                    containerColor = WarmOrange,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Restaurant")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = WarmOrange
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active Orders (${orders.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Banners & Storage (${restaurants.size})") }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text("Active Orders", style = MaterialTheme.typography.titleLarge)
                    }

                    if (orders.isEmpty()) {
                        item { Text("No orders found.", color = Color.Gray) }
                    } else {
                        items(orders) { order ->
                            AdminOrderCard(order = order, onStatusUpdate = { newStatus ->
                                onUpdateOrderStatus(order.id, newStatus)
                            })
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Charcoal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.CloudUpload,
                                        contentDescription = null,
                                        tint = WarmOrange,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Supabase Storage Integration",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = WarmOrange
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Connected bucket: restaurant-banners. Upload custom restaurant banner images hosted directly on Supabase Storage.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                                if (isUploadingBanner) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = WarmOrange,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Uploading banner to Supabase...", style = MaterialTheme.typography.bodySmall, color = WarmOrange)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Restaurants & Banner Hosting", style = MaterialTheme.typography.titleLarge)
                    }

                    if (restaurants.isEmpty()) {
                        item { Text("No restaurants found.", color = Color.Gray) }
                    } else {
                        items(restaurants) { restaurant ->
                            RestaurantBannerCard(
                                restaurant = restaurant,
                                isUploading = isUploadingBanner,
                                onChangeBanner = {
                                    selectedRestaurantForBanner = restaurant.id
                                    bannerPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantBannerCard(
    restaurant: Restaurant,
    isUploading: Boolean,
    onChangeBanner: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = restaurant.imageUrl,
                    contentDescription = restaurant.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.categories.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "⏱ ${restaurant.deliveryTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onChangeBanner,
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmOrange)
                ) {
                    Icon(
                        Icons.Filled.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload New Banner (Supabase Storage)")
                }
            }
        }
    }
}

@Composable
fun AddRestaurantDialog(
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<String>, ByteArray?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var deliveryTime by remember { mutableStateOf("20-30 min") }
    var categoriesText by remember { mutableStateOf("American, Fast Food") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }

    val context = LocalContext.current
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) {
            imageBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = { Text("Add Restaurant & Host Banner") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Restaurant Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deliveryTime,
                    onValueChange = { deliveryTime = it },
                    label = { Text("Delivery Time (e.g. 25-35 min)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = categoriesText,
                    onValueChange = { categoriesText = it },
                    label = { Text("Categories (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Banner Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Button(
                    onClick = {
                        pickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        if (selectedImageUri != null) Icons.Filled.Image else Icons.Filled.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (selectedImageUri != null) "Change Banner Image" else "Select Banner for Supabase Storage",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val catList = categoriesText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    onConfirm(name, deliveryTime, catList, imageBytes)
                },
                enabled = name.isNotBlank() && !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = WarmOrange)
            ) {
                Text(if (isUploading) "Uploading..." else "Create & Host")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUploading) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminOrderCard(order: Order, onStatusUpdate: (OrderStatus) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order ID: ${order.id}", style = MaterialTheme.typography.titleSmall)
                Text("$${"%.2f".format(order.total)}", style = MaterialTheme.typography.titleMedium, color = WarmOrange)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("User: ${order.userId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Update Status:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OrderStatusButton(OrderStatus.PENDING, order.status, onStatusUpdate)
                OrderStatusButton(OrderStatus.PREPARING, order.status, onStatusUpdate)
                OrderStatusButton(OrderStatus.DELIVERED, order.status, onStatusUpdate)
            }
        }
    }
}

@Composable
fun OrderStatusButton(status: OrderStatus, currentStatus: OrderStatus, onClick: (OrderStatus) -> Unit) {
    val isSelected = status == currentStatus
    Button(
        onClick = { onClick(status) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) WarmOrange else MaterialTheme.colorScheme.surfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(status.name, style = MaterialTheme.typography.labelSmall, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
    }
}

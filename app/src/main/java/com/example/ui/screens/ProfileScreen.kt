package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil.compose.AsyncImage
import com.example.domain.LoyaltyTier
import com.example.domain.Order
import com.example.domain.OrderStatus
import com.example.domain.User
import com.example.domain.UserRole
import com.example.ui.theme.Charcoal
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.WarmOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User?,
    orders: List<Order>,
    isUploadingAvatar: Boolean = false,
    onUploadAvatar: (ByteArray) -> Unit = {},
    onToggleAdmin: () -> Unit,
    onUpdateProfile: (String, String) -> Unit = { _, _ -> },
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    var showEditProfile by remember { mutableStateOf(false) }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null && bytes.isNotEmpty()) {
                onUploadAvatar(bytes)
            }
        }
    }
    
    if (showEditProfile && user != null) {
        var addressInput by remember { mutableStateOf(user.deliveryAddress ?: "") }
        var paymentInput by remember { mutableStateOf(user.paymentMethod ?: "") }
        
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Delivery Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = paymentInput,
                        onValueChange = { paymentInput = it },
                        label = { Text("Payment Method") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(addressInput, paymentInput)
                        showEditProfile = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfile = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                if (user != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Charcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clickable {
                                        avatarPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!user.profilePictureUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = user.profilePictureUrl,
                                        contentDescription = "User Avatar",
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, WarmOrange, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(56.dp),
                                            tint = WarmOrange
                                        )
                                    }
                                }

                                if (isUploadingAvatar) {
                                    Box(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(36.dp),
                                            color = WarmOrange
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        avatarPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(WarmOrange)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PhotoCamera,
                                        contentDescription = "Upload Avatar",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = {
                                    avatarPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                enabled = !isUploadingAvatar
                            ) {
                                Icon(
                                    Icons.Filled.CloudUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = WarmOrange
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (isUploadingAvatar) "Uploading to Supabase..." else "Upload Avatar (Supabase Storage)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = WarmOrange
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(user.name, style = MaterialTheme.typography.titleLarge)
                            Text(user.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                Text("Delivery Address", style = MaterialTheme.typography.labelSmall, color = WarmOrange)
                                Text(user.deliveryAddress ?: "Not set", style = MaterialTheme.typography.bodyMedium, color = if (user.deliveryAddress != null) Color.White else Color.Gray)
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text("Payment Method", style = MaterialTheme.typography.labelSmall, color = WarmOrange)
                                Text(user.paymentMethod ?: "Not set", style = MaterialTheme.typography.bodyMedium, color = if (user.paymentMethod != null) Color.White else Color.Gray)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { showEditProfile = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = WarmOrange)
                                ) {
                                    Text("Edit Profile", color = Color.White)
                                }
                                
                                Button(
                                    onClick = onToggleAdmin,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text("Toggle Admin Mode", color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedButton(
                                onClick = onLogout,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                            ) {
                                Text("Logout")
                            }
                        }
                    }
                }
            }
            
            item {
                if (user != null) {
                    LoyaltyCard(user)
                }
            }

            item {
                Text("Order History", style = MaterialTheme.typography.titleLarge)
            }

            val userOrders = orders.filter { it.userId == user?.id }
            if (userOrders.isEmpty()) {
                item {
                    Text("No past orders.", color = Color.Gray)
                }
            } else {
                items(userOrders) { order ->
                    OrderHistoryCard(order)
                }
            }
        }
    }
}

@Composable
fun LoyaltyCard(user: User) {
    val tierColor = when (user.loyaltyTier) {
        LoyaltyTier.BRONZE -> Color(0xFFCD7F32)
        LoyaltyTier.SILVER -> Color(0xFFC0C0C0)
        LoyaltyTier.GOLD -> Color(0xFFFFD700)
        LoyaltyTier.PLATINUM -> Color(0xFFE5E4E2)
    }
    
    val nextTierInfo = when (user.loyaltyTier) {
        LoyaltyTier.BRONZE -> Pair(LoyaltyTier.SILVER, 1000)
        LoyaltyTier.SILVER -> Pair(LoyaltyTier.GOLD, 4000)
        LoyaltyTier.GOLD -> Pair(LoyaltyTier.PLATINUM, 10000)
        LoyaltyTier.PLATINUM -> Pair(LoyaltyTier.PLATINUM, user.lifetimePoints) // Maxed out
    }
    
    val progress = if (user.loyaltyTier == LoyaltyTier.PLATINUM) {
        1f
    } else {
        user.lifetimePoints.toFloat() / nextTierInfo.second.toFloat()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Charcoal),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("BiteGo Rewards", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text("${user.loyaltyPoints} pts available", style = MaterialTheme.typography.bodyMedium, color = WarmOrange)
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = tierColor.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = tierColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(user.loyaltyTier.name, style = MaterialTheme.typography.labelMedium, color = tierColor)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (user.loyaltyTier != LoyaltyTier.PLATINUM) {
                Text(
                    "Earn ${nextTierInfo.second - user.lifetimePoints} more pts for ${nextTierInfo.first.name} tier", 
                    style = MaterialTheme.typography.labelMedium, 
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = tierColor,
                    trackColor = Color.DarkGray,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            } else {
                Text("You've reached the highest tier!", style = MaterialTheme.typography.labelMedium, color = tierColor)
            }
        }
    }
}

@Composable
fun OrderTrackingStepper(currentStatus: OrderStatus) {
    if (currentStatus == OrderStatus.CANCELLED) {
        Text("Order Cancelled", color = ErrorRed, style = MaterialTheme.typography.titleSmall)
        return
    }

    val steps = listOf(
        OrderStatus.PENDING,
        OrderStatus.ACCEPTED,
        OrderStatus.PREPARING,
        OrderStatus.ON_THE_WAY,
        OrderStatus.DELIVERED
    )
    
    val currentIndex = steps.indexOf(currentStatus).takeIf { it >= 0 } ?: 0

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            steps.forEachIndexed { index, step ->
                val isCompleted = index <= currentIndex
                val isLast = index == steps.size - 1

                // Dot
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) WarmOrange else Color.DarkGray)
                )

                // Line
                if (!isLast) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(if (index < currentIndex) WarmOrange else Color.DarkGray)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Status: ${currentStatus.name.replace("_", " ")}",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmOrange,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun OrderHistoryCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order ${order.id.takeLast(6)}", style = MaterialTheme.typography.titleMedium)
                Text("$${"%.2f".format(order.total)}", style = MaterialTheme.typography.titleMedium, color = WarmOrange)
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            OrderTrackingStepper(currentStatus = order.status)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (order.discountAmount > 0) {
                Text("Loyalty Discount: -$${"%.2f".format(order.discountAmount)}", style = MaterialTheme.typography.bodySmall, color = WarmOrange)
            }
            if (order.promoCode != null) {
                Text("Promo (${order.promoCode}): -$${"%.2f".format(order.promoDiscountAmount)}", style = MaterialTheme.typography.bodySmall, color = WarmOrange)
            }
            if (order.pointsEarned > 0) {
                Text("Points Earned: +${order.pointsEarned}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Items: ${order.items.joinToString { it.menuItem.name }}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

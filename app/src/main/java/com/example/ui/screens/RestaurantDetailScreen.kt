package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.MenuItem
import com.example.domain.Review
import com.example.domain.TargetType
import com.example.ui.theme.Charcoal
import com.example.ui.theme.WarmOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    restaurantId: String,
    restaurantName: String,
    menuItems: List<MenuItem>,
    reviews: List<Review>,
    onBack: () -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    onSubmitReview: (String, TargetType, Int, String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Menu", "Reviews")

    val restaurantReviews = reviews.filter { it.targetId == restaurantId && it.targetType == TargetType.RESTAURANT }
    
    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewTargetId by remember { mutableStateOf("") }
    var reviewTargetType by remember { mutableStateOf(TargetType.RESTAURANT) }

    if (showReviewDialog) {
        ReviewDialog(
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment -> 
                onSubmitReview(reviewTargetId, reviewTargetType, rating, comment)
                showReviewDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(restaurantName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
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
                contentColor = WarmOrange,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = WarmOrange
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, color = if (selectedTab == index) WarmOrange else Color.Gray) }
                    )
                }
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(menuItems) { item ->
                        MenuItemCard(
                            menuItem = item,
                            onAddToCart = { onAddToCart(item) },
                            onRateClick = { 
                                reviewTargetId = item.id
                                reviewTargetType = TargetType.MENU_ITEM
                                showReviewDialog = true
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Button(
                            onClick = { 
                                reviewTargetId = restaurantId
                                reviewTargetType = TargetType.RESTAURANT
                                showReviewDialog = true
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Write a Review for $restaurantName")
                        }
                    }
                    if (restaurantReviews.isEmpty()) {
                        item {
                            Text("No reviews yet.", color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
                        }
                    } else {
                        items(restaurantReviews) { review ->
                            ReviewCard(review)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItemCard(menuItem: MenuItem, onAddToCart: () -> Unit, onRateClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(menuItem.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                if (menuItem.rating > 0.0) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${menuItem.rating} (${menuItem.reviewCount})", style = MaterialTheme.typography.labelSmall, color = WarmOrange)
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(menuItem.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                Text("$${menuItem.price}", style = MaterialTheme.typography.titleMedium, color = WarmOrange)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Add to Cart", style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(
                        onClick = onRateClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmOrange),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Rate", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            AsyncImage(
                model = menuItem.imageUrl,
                contentDescription = menuItem.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(review.userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row {
                    repeat(5) { i ->
                        Icon(
                            if (i < review.rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = WarmOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(review.comment, style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
        }
    }
}

@Composable
fun ReviewDialog(onDismiss: () -> Unit, onSubmit: (rating: Int, comment: String) -> Unit) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        title = { Text("Write a Review", color = Color.White) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { i ->
                        val currentRating = i + 1
                        Icon(
                            if (currentRating <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = WarmOrange,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { rating = currentRating }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Your comment (optional)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmOrange,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment) },
                colors = ButtonDefaults.buttonColors(containerColor = WarmOrange)
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

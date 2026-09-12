package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.Restaurant
import com.example.ui.theme.Charcoal
import com.example.ui.theme.WarmOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    restaurants: List<Restaurant>,
    onRestaurantClick: (String, String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("BiteGo", color = WarmOrange, fontWeight = FontWeight.Bold) 
                },
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
                SearchBar()
            }
            
            item {
                Text(
                    text = "Featured Restaurants",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(restaurants.filter { it.featured }) { restaurant ->
                        FeaturedRestaurantCard(
                            restaurant = restaurant,
                            onClick = { onRestaurantClick(restaurant.id, restaurant.name) }
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "All Restaurants",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            items(restaurants) { restaurant ->
                RestaurantListItem(
                    restaurant = restaurant,
                    onClick = { onRestaurantClick(restaurant.id, restaurant.name) }
                )
            }
        }
    }
}

@Composable
fun SearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("Search restaurants, food...") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Charcoal,
            focusedContainerColor = Charcoal,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = WarmOrange
        )
    )
}

@Composable
fun FeaturedRestaurantCard(restaurant: Restaurant, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = restaurant.imageUrl,
                contentDescription = restaurant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${restaurant.rating} (${restaurant.reviewCount})", 
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(restaurant.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(restaurant.categories.joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun RestaurantListItem(restaurant: Restaurant, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = restaurant.imageUrl,
            contentDescription = restaurant.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(restaurant.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(restaurant.categories.joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${restaurant.rating} (${restaurant.reviewCount})", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(16.dp))
                Text("• ${restaurant.deliveryTime}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

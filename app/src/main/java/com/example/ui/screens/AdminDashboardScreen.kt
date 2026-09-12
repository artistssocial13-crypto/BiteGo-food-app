package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.Order
import com.example.domain.OrderStatus
import com.example.ui.theme.Charcoal
import com.example.ui.theme.WarmOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    orders: List<Order>,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold, color = WarmOrange) },
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
    }
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

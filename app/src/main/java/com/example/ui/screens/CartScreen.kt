package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.CartItem
import com.example.domain.PromoCode
import com.example.domain.DiscountType
import com.example.domain.User
import com.example.ui.theme.Charcoal
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.WarmOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    user: User?,
    onRemove: (String) -> Unit,
    onValidatePromo: (String) -> PromoCode?,
    onCheckout: (List<CartItem>, Double, Int, PromoCode?, Double) -> Unit
) {
    val subtotal = cartItems.sumOf { it.menuItem.price * it.quantity }
    var pointsToRedeem by remember { mutableFloatStateOf(0f) }
    
    var promoCodeInput by remember { mutableStateOf("") }
    var appliedPromoCode by remember { mutableStateOf<PromoCode?>(null) }
    var promoError by remember { mutableStateOf<String?>(null) }
    
    val maxPoints = minOf(user?.loyaltyPoints ?: 0, (subtotal * 100).toInt())
    
    // Ensure we don't redeem more points than what we have if subtotal changes
    LaunchedEffect(maxPoints) {
        if (pointsToRedeem > maxPoints) pointsToRedeem = maxPoints.toFloat()
    }
    
    val loyaltyDiscount = (pointsToRedeem.toInt() / 100) * 1.0
    
    val promoDiscount = appliedPromoCode?.let {
        when (it.type) {
            DiscountType.PERCENTAGE -> subtotal * it.value
            DiscountType.FIXED -> minOf(subtotal, it.value)
        }
    } ?: 0.0
    
    val finalTotal = maxOf(0.0, subtotal - loyaltyDiscount - promoDiscount)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Cart", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    color = Charcoal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (maxPoints >= 100) {
                            Text("Redeem Loyalty Points (100 pts = $1.00)", style = MaterialTheme.typography.labelMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Slider(
                                    value = pointsToRedeem,
                                    onValueChange = { pointsToRedeem = it },
                                    valueRange = 0f..maxPoints.toFloat(),
                                    steps = if (maxPoints > 100) (maxPoints / 100) - 1 else 0,
                                    colors = SliderDefaults.colors(
                                        thumbColor = WarmOrange,
                                        activeTrackColor = WarmOrange
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${pointsToRedeem.toInt()} pts", 
                                    style = MaterialTheme.typography.labelMedium,
                                    color = WarmOrange
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                            Text("$${"%.2f".format(subtotal)}", style = MaterialTheme.typography.bodyMedium)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = promoCodeInput,
                                onValueChange = { 
                                    promoCodeInput = it
                                    promoError = null
                                },
                                label = { Text("Promo Code", style = MaterialTheme.typography.bodySmall) },
                                modifier = Modifier.weight(1f).height(60.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WarmOrange,
                                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Gray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (promoCodeInput.isNotBlank()) {
                                        val promo = onValidatePromo(promoCodeInput)
                                        if (promo != null) {
                                            if (subtotal >= promo.minOrderValue) {
                                                appliedPromoCode = promo
                                                promoError = null
                                                promoCodeInput = ""
                                            } else {
                                                promoError = "Min order value is $${promo.minOrderValue}"
                                                appliedPromoCode = null
                                            }
                                        } else {
                                            promoError = "Invalid promo code"
                                            appliedPromoCode = null
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("Apply")
                            }
                        }
                        
                        if (promoError != null) {
                            Text(promoError!!, style = MaterialTheme.typography.labelSmall, color = ErrorRed, modifier = Modifier.padding(top = 4.dp))
                        }
                        
                        if (appliedPromoCode != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Promo (${appliedPromoCode!!.code})", style = MaterialTheme.typography.bodyMedium, color = WarmOrange)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(
                                        onClick = { appliedPromoCode = null },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Text("Remove", style = MaterialTheme.typography.labelSmall, color = ErrorRed)
                                    }
                                }
                                Text("-$${"%.2f".format(promoDiscount)}", style = MaterialTheme.typography.bodyMedium, color = WarmOrange)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (loyaltyDiscount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Loyalty Discount", style = MaterialTheme.typography.bodyMedium, color = WarmOrange)
                                Text("-$${"%.2f".format(loyaltyDiscount)}", style = MaterialTheme.typography.bodyMedium, color = WarmOrange)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleLarge)
                            Text("$${"%.2f".format(finalTotal)}", style = MaterialTheme.typography.titleLarge, color = WarmOrange)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onCheckout(cartItems, subtotal, pointsToRedeem.toInt(), appliedPromoCode, promoDiscount) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Checkout", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Your cart is empty", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cartItems) { item ->
                    CartItemRow(item = item, onRemove = { onRemove(item.menuItem.id) })
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = item.menuItem.imageUrl,
                contentDescription = item.menuItem.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(item.menuItem.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("$${item.menuItem.price} x ${item.quantity}", style = MaterialTheme.typography.bodyMedium, color = WarmOrange)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = ErrorRed)
            }
        }
    }
}

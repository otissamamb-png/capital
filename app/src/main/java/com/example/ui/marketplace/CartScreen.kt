package com.example.ui.marketplace

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItemData
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItemData>,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    val totalAmount = remember(cartItems) {
        cartItems.sumOf { it.price * it.quantity }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Shopping Cart", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("cart_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        TextButton(onClick = onClearCart) {
                            Text("Clear", color = RoseOccupied)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Amount", fontSize = 15.sp, color = Slate600)
                            Text(
                                text = "KSh ${"%,d".format(totalAmount)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = RoyalBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showCheckoutDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("checkout_button")
                        ) {
                            Text("Proceed with Sellers (${cartItems.size} items)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.ShoppingCart,
                title = "Your cart is empty",
                description = "Explore products on the residence marketplace and add them to your cart.",
                actionLabel = "Browse Marketplace",
                onActionClick = onBack,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate50)
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems, key = { it.id.ifEmpty { it.product_id } }) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product_name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Sold by ${item.seller_name}",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "KSh ${"%,d".format(item.price)} × ${item.quantity}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RoyalBlue
                                )
                            }
                            IconButton(onClick = { onRemoveItem(item.id.ifEmpty { item.product_id }) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = RoseOccupied)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text("Checkout & Seller Contact", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Capital Home Residence does not automatically deduct funds. Instead, communicate directly with the student sellers to confirm item pickup at the residence and make direct M-Pesa payments.",
                        fontSize = 13.sp,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Total: KSh ${"%,d".format(totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCheckoutDialog = false
                        onClearCart()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Confirm & Return")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

package com.example.ui.marketplace

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderRecord
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orders: List<OrderRecord>,
    onOrderClick: (OrderRecord) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Pending Payment", "Verification", "Confirmed", "Completed"

    val filtered = remember(orders, selectedFilter) {
        when (selectedFilter) {
            "Pending Payment" -> orders.filter { it.payment_status == "Payment Pending" }
            "Verification" -> orders.filter { it.payment_status == "Reference Submitted" || it.order_status == "Payment Verification" }
            "Confirmed" -> orders.filter { it.payment_status == "Payment Confirmed" || it.order_status == "Processing" }
            "Completed" -> orders.filter { it.order_status == "Completed" || it.order_status == "Ready for Pickup" }
            else -> orders
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders & Purchases", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("orders_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate50)
                .padding(paddingValues)
        ) {
            // Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Pending Payment", "Verification", "Confirmed").forEach { f ->
                    val isSelected = selectedFilter == f
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = f },
                        label = { Text(f, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (filtered.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.ReceiptLong,
                    title = "No orders found",
                    description = "When you purchase products from student sellers, your orders will appear here.",
                    actionLabel = "Browse Marketplace",
                    onActionClick = onBack,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id.ifEmpty { it.order_number } }) { order ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOrderClick(order) }
                                .testTag("order_item_${order.order_number}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Order #${order.order_number}", fontWeight = FontWeight.Bold, color = RoyalBlue, fontSize = 14.sp)
                                    Surface(
                                        color = when (order.payment_status) {
                                            "Payment Confirmed" -> EmeraldLight
                                            "Reference Submitted" -> BlueLight
                                            "Payment Rejected" -> RoseLight
                                            else -> AmberLight
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = order.payment_status,
                                            color = when (order.payment_status) {
                                                "Payment Confirmed" -> EmeraldAvailable
                                                "Reference Submitted" -> RoyalBlue
                                                "Payment Rejected" -> RoseOccupied
                                                else -> AmberPending
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(order.product_name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                Text("Sold by ${order.seller_name} • Qty: ${order.quantity}", fontSize = 12.sp, color = Slate500)

                                if (order.mpesa_reference.isNotEmpty()) {
                                    Text("M-Pesa Ref: ${order.mpesa_reference}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RoyalBlue)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total: KSh ${"%,d".format(order.total_amount)}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Slate900)
                                    Text(
                                        text = if (order.payment_status == "Payment Pending") "Complete Payment →" else "View Details →",
                                        color = RoyalBlue,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

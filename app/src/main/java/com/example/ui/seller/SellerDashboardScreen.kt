package com.example.ui.seller

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import com.example.data.model.MarketplaceProduct
import com.example.data.model.OrderRecord
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    products: List<MarketplaceProduct>,
    orders: List<OrderRecord>,
    onAddProductClick: () -> Unit,
    onVerifyPayment: (orderId: String, confirmed: Boolean, reason: String) -> Unit,
    onUpdateOrderStatus: (orderId: String, newStatus: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    var selectedTab by remember { mutableStateOf("Orders") } // "Orders", "Verification", "My Products"
    var showRejectDialogForOrder by remember { mutableStateOf<OrderRecord?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seller Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("seller_dashboard_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = onAddProductClick,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp).height(36.dp).testTag("seller_add_product_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Product", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            // Metrics Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Orders (${orders.size})", "Verification", "My Products (${products.size})").forEach { tab ->
                    val tabKey = if (tab.startsWith("Orders")) "Orders" else if (tab.startsWith("Verif")) "Verification" else "My Products"
                    val isSelected = selectedTab == tabKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = tabKey },
                        label = { Text(tab, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalBlue,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            when (selectedTab) {
                "Verification" -> {
                    val pendingVerification = orders.filter { it.payment_status == "Reference Submitted" }
                    if (pendingVerification.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.CheckCircle,
                            title = "All payments verified!",
                            description = "No pending M-Pesa references awaiting review.",
                            actionLabel = "Back to Orders",
                            onActionClick = { selectedTab = "Orders" },
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pendingVerification, key = { it.id }) { order ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("verify_order_card_${order.order_number}")
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Order #${order.order_number}", fontWeight = FontWeight.Bold, color = RoyalBlue)
                                            Text(
                                                "KSh ${"%,d".format(order.total_amount)}",
                                                fontWeight = FontWeight.Black,
                                                color = Slate900,
                                                fontSize = 16.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text("Product: ${order.product_name} (Qty ${order.quantity})", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("Buyer: ${order.buyer_name} (${order.buyer_phone})", fontSize = 12.sp, color = Slate600)

                                        Surface(
                                            color = BlueLight,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("M-Pesa Reference Code:", fontSize = 11.sp, color = Slate500)
                                                    Text(order.mpesa_reference, fontSize = 15.sp, fontWeight = FontWeight.Black, color = RoyalBlue)
                                                }
                                                Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = RoyalBlue)
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { showRejectDialogForOrder = order },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseOccupied),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Reject")
                                            }

                                            Button(
                                                onClick = { onVerifyPayment(order.id, true, "") },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAvailable),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Confirm Payment")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Orders" -> {
                    if (orders.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.ReceiptLong,
                            title = "No customer orders yet",
                            description = "When students buy your products via M-Pesa, their orders will appear here.",
                            actionLabel = "Add a Product",
                            onActionClick = onAddProductClick,
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(orders, key = { it.id }) { order ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Order #${order.order_number}", fontWeight = FontWeight.Bold, color = RoyalBlue)
                                            Surface(
                                                color = when (order.order_status) {
                                                    "Completed" -> EmeraldLight
                                                    "Processing" -> BlueLight
                                                    "Ready for Pickup" -> AmberLight
                                                    else -> Slate100
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = order.order_status,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (order.order_status) {
                                                        "Completed" -> EmeraldAvailable
                                                        "Processing" -> RoyalBlue
                                                        "Ready for Pickup" -> AmberPending
                                                        else -> Slate700
                                                    },
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(order.product_name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                        Text("Buyer: ${order.buyer_name} (${order.buyer_phone}) • Qty: ${order.quantity}", fontSize = 12.sp, color = Slate500)
                                        Text("Amount: KSh ${"%,d".format(order.total_amount)} • ${order.payment_status}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RoyalBlue)

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Status progression actions
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (order.order_status == "Processing") {
                                                Button(
                                                    onClick = { onUpdateOrderStatus(order.id, "Ready for Pickup") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = AmberPending),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Ready for Pickup", fontSize = 11.sp)
                                                }
                                            }

                                            if (order.order_status == "Ready for Pickup") {
                                                Button(
                                                    onClick = { onUpdateOrderStatus(order.id, "Completed") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAvailable),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Mark Completed", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "My Products" -> {
                    if (products.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.Storefront,
                            title = "No products listed",
                            description = "As an approved seller, you can publish items instantly without waiting for admin approval.",
                            actionLabel = "Add First Product",
                            onActionClick = onAddProductClick,
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(products, key = { it.id }) { prod ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(prod.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                            Text("KSh ${"%,d".format(prod.price)} • Stock: ${prod.quantity} (${prod.stockStatus})", fontSize = 12.sp, color = RoyalBlue)
                                            Text("Category: ${prod.category} • Status: ${prod.status}", fontSize = 11.sp, color = Slate500)
                                        }

                                        Surface(
                                            color = EmeraldLight,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = EmeraldAvailable,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
    }

    // Rejection dialog
    if (showRejectDialogForOrder != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialogForOrder = null },
            title = { Text("Reject Payment Reference") },
            text = {
                Column {
                    Text("Provide a reason to the buyer (e.g. Reference code not found on M-Pesa statement):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        placeholder = { Text("Reason for rejection") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onVerifyPayment(showRejectDialogForOrder!!.id, false, rejectionReason)
                        showRejectDialogForOrder = null
                        rejectionReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseOccupied)
                ) {
                    Text("Reject Reference")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialogForOrder = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

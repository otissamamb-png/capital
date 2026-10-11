package com.example.ui.marketplace

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderRecord
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SafeImage
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orders: List<OrderRecord>,
    initialOrderId: String? = null,
    onPayOrder: (OrderRecord) -> Unit = {},
    onCancelOrder: (String) -> Unit = {},
    onConfirmReceived: (String) -> Unit = {},
    onChatWithSeller: (sellerId: String, sellerName: String) -> Unit = { _, _ -> },
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // If an initial order ID was provided or user selected an order, track it in detail
    var selectedOrder by remember(orders, initialOrderId) {
        mutableStateOf(orders.find { it.id == initialOrderId })
    }

    if (selectedOrder != null) {
        val currentOrder = orders.find { it.id == selectedOrder!!.id } ?: selectedOrder!!
        OrderDetailTrackingView(
            order = currentOrder,
            onBack = { selectedOrder = null },
            onPayNow = { onPayOrder(currentOrder) },
            onCancelOrder = { onCancelOrder(currentOrder.id) },
            onConfirmReceived = { onConfirmReceived(currentOrder.id) },
            onChatWithSeller = { onChatWithSeller(currentOrder.seller_id, currentOrder.seller_name) },
            modifier = modifier
        )
    } else {
        OrderListView(
            orders = orders,
            onOrderClick = { selectedOrder = it },
            onPayOrder = onPayOrder,
            onCancelOrder = onCancelOrder,
            onBack = onBack,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderListView(
    orders: List<OrderRecord>,
    onOrderClick: (OrderRecord) -> Unit,
    onPayOrder: (OrderRecord) -> Unit,
    onCancelOrder: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Active", "Pending Payment", "Completed", "Cancelled"
    var searchQuery by remember { mutableStateOf("") }
    var orderToCancel by remember { mutableStateOf<OrderRecord?>(null) }

    val filtered = remember(orders, selectedFilter, searchQuery) {
        orders.filter { order ->
            val matchesFilter = when (selectedFilter) {
                "Active" -> order.order_status !in listOf("Completed", "Cancelled")
                "Pending Payment" -> order.payment_status == "Payment Pending"
                "Completed" -> order.order_status == "Completed"
                "Cancelled" -> order.order_status == "Cancelled" || order.payment_status == "Payment Cancelled"
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                order.order_number.contains(searchQuery, ignoreCase = true) ||
                order.product_name.contains(searchQuery, ignoreCase = true) ||
                order.seller_name.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Orders & Tracking", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = "${orders.size} order${if (orders.size == 1) "" else "s"} placed",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                },
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
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by order #, product, or seller...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate500)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("orders_search_input")
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Active", "Pending", "Completed").forEach { f ->
                    val isSelected = when (f) {
                        "Pending" -> selectedFilter == "Pending Payment"
                        else -> selectedFilter == f
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedFilter = if (f == "Pending") "Pending Payment" else f
                        },
                        label = { Text(f, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
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
                    description = if (searchQuery.isNotEmpty()) "No orders match '$searchQuery'." else "When you purchase products from sellers, your orders will appear here.",
                    actionLabel = "Browse Marketplace",
                    onActionClick = onBack,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filtered, key = { it.id.ifEmpty { it.order_number } }) { order ->
                        OrderSummaryCard(
                            order = order,
                            onClick = { onOrderClick(order) },
                            onPayOrder = { onPayOrder(order) },
                            onCancelOrder = { orderToCancel = order }
                        )
                    }
                }
            }
        }
    }

    if (orderToCancel != null) {
        val o = orderToCancel!!
        AlertDialog(
            onDismissRequest = { orderToCancel = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = RoseOccupied,
                    modifier = Modifier.size(44.dp)
                )
            },
            title = { Text("Cancel Order #${o.order_number}?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to cancel this order? This will release the item reservation and notify the seller.",
                    fontSize = 13.sp,
                    color = Slate600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelOrder(o.id)
                        orderToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseOccupied),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Yes, Cancel Order", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { orderToCancel = null }, shape = RoundedCornerShape(10.dp)) {
                    Text("Keep Order")
                }
            }
        )
    }
}

@Composable
private fun OrderSummaryCard(
    order: OrderRecord,
    onClick: () -> Unit,
    onPayOrder: () -> Unit,
    onCancelOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stageIndex = getOrderStageIndex(order)
    val isCancelled = order.order_status == "Cancelled" || order.payment_status == "Payment Cancelled"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("order_item_${order.order_number}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Order number & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Order #${order.order_number}",
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue,
                        fontSize = 14.sp
                    )
                }

                StatusPill(order = order)
            }

            Text(
                text = formatOrderDate(order.created_at),
                fontSize = 11.sp,
                color = Slate500,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Product Item Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate100)
                ) {
                    SafeImage(
                        model = order.product_image,
                        contentDescription = order.product_name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        fallbackIcon = Icons.Default.ShoppingBag
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.product_name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Sold by ${order.seller_name}",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                    Text(
                        text = "Qty: ${order.quantity} • ${order.pickup_or_delivery}",
                        fontSize = 11.sp,
                        color = Slate600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "KSh ${"%,d".format(order.total_amount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mini Progress Tracker
            if (!isCancelled) {
                MiniOrderProgressBar(currentStage = stageIndex)
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Surface(
                    color = RoseLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = RoseOccupied, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Order cancelled • ${order.rejection_reason.ifEmpty { "Transaction cancelled" }}",
                            fontSize = 11.sp,
                            color = RoseOccupied,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            HorizontalDivider(color = Slate100, thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (order.payment_status == "Payment Pending") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onCancelOrder,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseOccupied),
                            border = BorderStroke(1.dp, RoseOccupied.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onPayOrder,
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Pay Now →", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = getStageStatusDescription(stageIndex),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate600
                    )
                }

                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Track Order →",
                        color = RoyalBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniOrderProgressBar(currentStage: Int) {
    val totalStages = 5
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until totalStages) {
                val isPassed = i <= currentStage
                val isCurrent = i == currentStage

                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 14.dp else 10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isPassed -> EmeraldAvailable
                                else -> Slate200
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }

                if (i < totalStages - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(
                                if (i < currentStage) EmeraldAvailable else Slate200
                            )
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Placed", fontSize = 9.sp, color = if (currentStage >= 0) Slate700 else Slate400)
            Text("Payment", fontSize = 9.sp, color = if (currentStage >= 1) Slate700 else Slate400)
            Text("Confirmed", fontSize = 9.sp, color = if (currentStage >= 2) Slate700 else Slate400)
            Text("Ready", fontSize = 9.sp, color = if (currentStage >= 3) Slate700 else Slate400)
            Text("Completed", fontSize = 9.sp, color = if (currentStage >= 4) Slate700 else Slate400)
        }
    }
}

/**
 * Detailed Order Tracking Screen displaying full timeline stepper, pickup instructions,
 * seller contact card, payment info, and buyer resolution actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailTrackingView(
    order: OrderRecord,
    onBack: () -> Unit,
    onPayNow: () -> Unit,
    onCancelOrder: () -> Unit,
    onConfirmReceived: () -> Unit,
    onChatWithSeller: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var copiedCode by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showReceivedDialog by remember { mutableStateOf(false) }

    val stageIndex = getOrderStageIndex(order)
    val isCancelled = order.order_status == "Cancelled" || order.payment_status == "Payment Cancelled"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Order Tracking", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = "#${order.order_number}",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("tracking_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    StatusPill(order = order, modifier = Modifier.padding(end = 12.dp))
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Status Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isCancelled -> RoseLight
                        order.order_status == "Completed" -> EmeraldLight
                        order.order_status == "Ready for Pickup" -> AmberLight
                        else -> BlueLight
                    }
                ),
                modifier = Modifier.fillMaxWidth().testTag("tracking_hero_status")
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCancelled -> RoseOccupied
                                    order.order_status == "Completed" -> EmeraldAvailable
                                    order.order_status == "Ready for Pickup" -> AmberPending
                                    else -> RoyalBlue
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isCancelled -> Icons.Default.Cancel
                                order.order_status == "Completed" -> Icons.Default.CheckCircle
                                order.order_status == "Ready for Pickup" -> Icons.Default.LocationOn
                                stageIndex >= 2 -> Icons.Default.Storefront
                                stageIndex == 1 -> Icons.Default.Search
                                else -> Icons.Default.HourglassTop
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                isCancelled -> "Order Cancelled"
                                order.order_status == "Completed" -> "Order Completed"
                                order.order_status == "Ready for Pickup" -> "Ready for Pickup!"
                                stageIndex == 2 -> "Payment Confirmed • Processing"
                                stageIndex == 1 -> "Payment Verification"
                                else -> "Awaiting M-Pesa Payment"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Slate900
                        )
                        Text(
                            text = when {
                                isCancelled -> order.rejection_reason.ifEmpty { "This order was cancelled." }
                                order.order_status == "Completed" -> "Item received successfully at Capital Home Residence."
                                order.order_status == "Ready for Pickup" -> "Seller has made your package ready for pickup!"
                                stageIndex == 2 -> "Seller verified M-Pesa payment and is packaging your item."
                                stageIndex == 1 -> "M-Pesa code ${order.mpesa_reference} received. Seller verifying funds."
                                else -> "Please submit your M-Pesa transaction reference below."
                            },
                            fontSize = 12.sp,
                            color = Slate700,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 2. Visual Step-by-Step Order Timeline Stepper
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("tracking_stepper_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Order Progress Timeline",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Slate900
                    )
                    Text(
                        text = "Real-time updates as seller handles your purchase",
                        fontSize = 12.sp,
                        color = Slate500,
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                    )

                    val timelineSteps = listOf(
                        TimelineStep(
                            title = "Order Placed",
                            subtitle = "Order #${order.order_number} confirmed and reserved with seller.",
                            isDone = stageIndex >= 0 && !isCancelled,
                            isCurrent = stageIndex == 0 && !isCancelled,
                            icon = Icons.Default.Receipt
                        ),
                        TimelineStep(
                            title = "Payment Verification",
                            subtitle = when {
                                order.payment_status == "Payment Confirmed" -> "M-Pesa payment verified by seller."
                                order.mpesa_reference.isNotEmpty() -> "Code ${order.mpesa_reference} submitted. Awaiting seller confirmation."
                                else -> "Awaiting buyer's M-Pesa payment reference code."
                            },
                            isDone = stageIndex >= 1 && !isCancelled,
                            isCurrent = stageIndex == 1 && !isCancelled,
                            icon = Icons.Default.Payment
                        ),
                        TimelineStep(
                            title = "Payment Confirmed & Preparing",
                            subtitle = "Seller confirmed payment and is preparing the product for handover.",
                            isDone = stageIndex >= 2 && !isCancelled,
                            isCurrent = stageIndex == 2 && !isCancelled,
                            icon = Icons.Default.Inventory
                        ),
                        TimelineStep(
                            title = "Ready for Pickup",
                            subtitle = "Product is packed and waiting at pickup point: ${order.pickup_or_delivery}.",
                            isDone = stageIndex >= 3 && !isCancelled,
                            isCurrent = stageIndex == 3 && !isCancelled,
                            icon = Icons.Default.LocationOn
                        ),
                        TimelineStep(
                            title = "Completed",
                            subtitle = "Buyer received the product and verified satisfaction.",
                            isDone = stageIndex >= 4 && !isCancelled,
                            isCurrent = stageIndex == 4 && !isCancelled,
                            icon = Icons.Default.CheckCircle
                        )
                    )

                    timelineSteps.forEachIndexed { index, step ->
                        TimelineRow(
                            step = step,
                            isLast = index == timelineSteps.size - 1,
                            isCancelled = isCancelled
                        )
                    }

                    if (isCancelled) {
                        TimelineRow(
                            step = TimelineStep(
                                title = "Order Cancelled",
                                subtitle = order.rejection_reason.ifEmpty { "Transaction cancelled by buyer" },
                                isDone = true,
                                isCurrent = true,
                                icon = Icons.Default.Cancel,
                                isError = true
                            ),
                            isLast = true,
                            isCancelled = true
                        )
                    }
                }
            }

            // 3. Product & Order Item Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("tracking_product_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Item Purchased",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate100)
                        ) {
                            SafeImage(
                                model = order.product_image,
                                contentDescription = order.product_name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                fallbackIcon = Icons.Default.ShoppingBag
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = order.product_name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "Unit Price: KSh ${"%,d".format(order.unit_price)}",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                            Text(
                                text = "Quantity: ${order.quantity}",
                                fontSize = 12.sp,
                                color = Slate600
                            )
                            Text(
                                text = "Total: KSh ${"%,d".format(order.total_amount)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = RoyalBlue,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // 4. Pickup & Delivery Details Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("tracking_pickup_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pickup & Handover Location",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate900
                        )
                    }

                    Surface(
                        color = Slate50,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = order.pickup_or_delivery,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Slate900
                            )
                            Text(
                                text = "Please inspect the product thoroughly upon pickup at Capital Home Residence before confirming completion.",
                                fontSize = 11.sp,
                                color = Slate600,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // 5. Seller Details Card with Chat & Call buttons
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("tracking_seller_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Seller Information",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate900
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(RoyalBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = order.seller_name.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = order.seller_name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                            Text(
                                text = "Verified Student Resident",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                            if (order.seller_payment_number.isNotEmpty()) {
                                Text(
                                    text = "Phone: ${order.seller_payment_number}",
                                    fontSize = 12.sp,
                                    color = Slate700
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onChatWithSeller,
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("chat_seller_button")
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chat Seller", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (order.seller_payment_number.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.seller_payment_number}"))
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("call_seller_button")
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call Seller", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 6. Payment Information Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("tracking_payment_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Payment Information",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Payment Method", fontSize = 12.sp, color = Slate500)
                        Text(order.payment_method, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("M-Pesa Reference", fontSize = 12.sp, color = Slate500)
                        if (order.mpesa_reference.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    order.mpesa_reference,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = RoyalBlue
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(order.mpesa_reference))
                                        copiedCode = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = RoyalBlue, modifier = Modifier.size(15.dp))
                                }
                            }
                        } else {
                            Text("Not submitted yet", fontSize = 12.sp, color = AmberPending, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (copiedCode) {
                        Text("M-Pesa code copied to clipboard", fontSize = 10.sp, color = EmeraldAvailable, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Payment Status", fontSize = 12.sp, color = Slate500)
                        Text(order.payment_status, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Slate100)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount Paid", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate700)
                        Text(
                            text = "KSh ${"%,d".format(order.total_amount)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Slate900
                        )
                    }
                }
            }

            // 7. Contextual Actions for Buyer
            if (!isCancelled) {
                when {
                    order.payment_status == "Payment Pending" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onPayNow,
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("tracking_pay_now_button")
                            ) {
                                Text("Complete M-Pesa Payment →", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseOccupied),
                                border = BorderStroke(1.dp, RoseOccupied.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("tracking_cancel_order_button")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp), tint = RoseOccupied)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cancel Order & Payment", fontWeight = FontWeight.Bold, color = RoseOccupied)
                            }
                        }
                    }

                    order.payment_status == "Reference Submitted" && order.order_status != "Completed" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onPayNow,
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("tracking_update_reference_button")
                            ) {
                                Text("View / Update Payment Code", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseOccupied),
                                border = BorderStroke(1.dp, RoseOccupied.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("Cancel Order", fontWeight = FontWeight.Bold, color = RoseOccupied)
                            }
                        }
                    }

                    order.order_status == "Ready for Pickup" -> {
                        Button(
                            onClick = { showReceivedDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAvailable),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("tracking_confirm_received_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("I Have Received My Item (Complete Order)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    order.order_status == "Completed" -> {
                        Surface(
                            color = EmeraldLight,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAvailable)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "✓ Order completed & verified successfully",
                                    color = EmeraldAvailable,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = RoseOccupied,
                    modifier = Modifier.size(44.dp)
                )
            },
            title = { Text("Cancel Order #${order.order_number}?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to cancel this order? The seller will be notified and this payment prompt will not appear again.",
                    fontSize = 13.sp,
                    color = Slate600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onCancelOrder()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseOccupied),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_cancel_in_tracking")
                ) {
                    Text("Yes, Cancel Order", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }, shape = RoundedCornerShape(10.dp)) {
                    Text("Keep Order")
                }
            }
        )
    }

    if (showReceivedDialog) {
        AlertDialog(
            onDismissRequest = { showReceivedDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldAvailable,
                    modifier = Modifier.size(44.dp)
                )
            },
            title = { Text("Confirm Product Received?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Have you inspected and received ${order.product_name} from ${order.seller_name}? This will mark the order as Completed.",
                    fontSize = 13.sp,
                    color = Slate600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReceivedDialog = false
                        onConfirmReceived()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAvailable),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Yes, I Received It", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showReceivedDialog = false }, shape = RoundedCornerShape(10.dp)) {
                    Text("Not Yet")
                }
            }
        )
    }
}

private data class TimelineStep(
    val title: String,
    val subtitle: String,
    val isDone: Boolean,
    val isCurrent: Boolean,
    val icon: ImageVector,
    val isError: Boolean = false
)

@Composable
private fun TimelineRow(
    step: TimelineStep,
    isLast: Boolean,
    isCancelled: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            step.isError -> RoseOccupied
                            step.isDone -> EmeraldAvailable
                            step.isCurrent -> RoyalBlue
                            else -> Slate200
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        step.isError -> Icons.Default.Close
                        step.isDone -> Icons.Default.Check
                        else -> step.icon
                    },
                    contentDescription = null,
                    tint = if (step.isDone || step.isCurrent || step.isError) Color.White else Slate500,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(38.dp)
                        .background(if (step.isDone && !isCancelled) EmeraldAvailable else Slate200)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Text(
                text = step.title,
                fontWeight = if (step.isCurrent || step.isDone) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = when {
                    step.isError -> RoseOccupied
                    step.isCurrent -> RoyalBlue
                    step.isDone -> Slate900
                    else -> Slate500
                }
            )
            Text(
                text = step.subtitle,
                fontSize = 11.sp,
                color = if (step.isCurrent || step.isDone) Slate700 else Slate400,
                lineHeight = 15.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun StatusPill(order: OrderRecord, modifier: Modifier = Modifier) {
    val (label, bg, textCol) = when {
        order.order_status == "Cancelled" || order.payment_status == "Payment Cancelled" ->
            Triple("Cancelled", RoseLight, RoseOccupied)
        order.order_status == "Completed" ->
            Triple("Completed", EmeraldLight, EmeraldAvailable)
        order.order_status == "Ready for Pickup" ->
            Triple("Ready for Pickup", AmberLight, AmberPending)
        order.payment_status == "Payment Confirmed" || order.order_status == "Processing" ->
            Triple("Confirmed", Color(0xFFF3E8FF), Color(0xFF7E22CE))
        order.payment_status == "Reference Submitted" ->
            Triple("Verifying", BlueLight, RoyalBlue)
        else ->
            Triple("Pending Payment", AmberLight, AmberPending)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = label.uppercase(),
            color = textCol,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

private fun getOrderStageIndex(order: OrderRecord): Int {
    if (order.order_status == "Cancelled" || order.payment_status == "Payment Cancelled") return -1
    if (order.order_status == "Completed") return 4
    if (order.order_status == "Ready for Pickup") return 3
    if (order.payment_status == "Payment Confirmed" || order.order_status == "Processing" || order.order_status == "Confirmed") return 2
    if (order.payment_status == "Reference Submitted" || order.order_status == "Payment Verification") return 1
    return 0 // Order Placed / Payment Pending
}

private fun getStageStatusDescription(stageIndex: Int): String {
    return when (stageIndex) {
        4 -> "Completed ✓"
        3 -> "Ready for pickup at residence"
        2 -> "Seller preparing item"
        1 -> "Verifying payment reference"
        0 -> "Payment pending"
        else -> "Cancelled"
    }
}

private fun formatOrderDate(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return "Recent"
    return try {
        SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(timestamp.toDate())
    } catch (e: Exception) {
        "Recent"
    }
}

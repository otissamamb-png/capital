package com.example.ui.profile

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
import com.example.data.model.*
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    rooms: List<RoomItem>,
    sellerApplications: List<SellerApplicationData>,
    products: List<MarketplaceProduct> = emptyList(),
    orders: List<OrderRecord> = emptyList(),
    vacancies: List<VacancyItem>,
    announcements: List<ResidenceAnnouncement>,
    users: List<UserProfile> = emptyList(),
    onReviewSeller: (String, Boolean) -> Unit,
    onToggleProductVisibility: (productId: String, hide: Boolean) -> Unit = { _, _ -> },
    onVerifyPayment: (orderId: String, confirmed: Boolean, reason: String) -> Unit = { _, _, _ -> },
    onToggleRoomStatus: (RoomItem) -> Unit,
    onToggleRoomReadyToBook: (roomId: String, ready: Boolean) -> Unit = { _, _ -> },
    onDeleteUser: (userId: String) -> Unit = {},
    onCreateAnnouncement: (String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    var selectedSection by remember { mutableStateOf("Overview") } // "Overview", "Users", "Sellers", "Products", "Orders", "Rooms", "Announcements"
    var showNewAnnouncementDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<UserProfile?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administration Portal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showNewAnnouncementDialog = true }) {
                        Icon(Icons.Default.AddAlert, contentDescription = "Post Announcement", tint = RoyalBlue)
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
            // Section Switcher
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("Overview", "Users", "Sellers", "Products", "Orders", "Rooms", "Announcements")) { section ->
                    val isSelected = selectedSection == section
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSection = section },
                        label = { Text(section, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalBlue,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("admin_tab_$section")
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedSection) {
                    "Overview" -> {
                        item {
                            Text("Marketplace & Residence Metrics", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MetricCard("Users", "${users.size}", RoyalBlue, Modifier.weight(1f))
                                MetricCard("Active Prods", "${products.count { it.status == "Active" }}", EmeraldAvailable, Modifier.weight(1f))
                                MetricCard("Orders", "${orders.size}", RoyalBlue, Modifier.weight(1f))
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Pending Action Items", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MetricCard("Seller Apps", "${sellerApplications.count { it.status == "Pending" }}", AmberPending, Modifier.weight(1f))
                                MetricCard("M-Pesa Reviews", "${orders.count { it.payment_status == "Reference Submitted" }}", RoyalBlue, Modifier.weight(1f))
                                MetricCard("Available Rooms", "${rooms.count { it.status == "Available" }}", EmeraldAvailable, Modifier.weight(1f))
                            }
                        }
                    }

                    "Users" -> {
                        item {
                            Text("Manage Registered Users (${users.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("View resident accounts and delete obsolete or unverified users.", fontSize = 11.sp, color = Slate500)
                        }

                        if (users.isEmpty()) {
                            item {
                                Text("No registered users found.", color = Slate500, fontSize = 13.sp)
                            }
                        } else {
                            items(users, key = { it.id.ifEmpty { it.auth_user_id } }) { user ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("admin_user_card_${user.id}")
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(user.full_name.ifEmpty { "Resident" }, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Text(user.email, fontSize = 12.sp, color = Slate600)
                                            }
                                            Surface(
                                                color = when (user.role) {
                                                    "admin" -> NavyDark
                                                    "seller" -> EmeraldAvailable
                                                    else -> RoyalBlue
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = user.role.uppercase(),
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Phone: ${user.phone.ifEmpty { "N/A" }} • Room: ${user.room_id.ifEmpty { "Unassigned" }}",
                                                fontSize = 11.sp,
                                                color = Slate500
                                            )

                                            // Root admin cannot be deleted
                                            if (user.role != "admin" && user.email != "okindatechhub@gmail.com") {
                                                Button(
                                                    onClick = { userToDelete = user },
                                                    colors = ButtonDefaults.buttonColors(containerColor = RoseOccupied),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(30.dp).testTag("delete_user_${user.id}")
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Delete User", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Sellers" -> {
                        item {
                            Text("Seller Applications (${sellerApplications.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Approval happens only once. Once approved, sellers publish products directly without individual approval.", fontSize = 11.sp, color = Slate500)
                        }
                        if (sellerApplications.isEmpty()) {
                            item {
                                Text("No seller applications pending.", color = Slate500, fontSize = 13.sp)
                            }
                        } else {
                            items(sellerApplications, key = { it.id.ifEmpty { it.user_id } }) { app ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(app.business_name.ifEmpty { app.seller_name }, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            StatusBadge(status = app.status)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Applicant: ${app.seller_name} (${app.phone})", fontSize = 12.sp, color = Slate600)
                                        Text("Payment: ${app.payment_method} - ${app.payment_number}", fontSize = 12.sp, color = Slate600)
                                        if (app.business_description.isNotEmpty()) {
                                            Text("Details: ${app.business_description}", fontSize = 12.sp, color = Slate500)
                                        }

                                        if (app.status == "Pending") {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedButton(
                                                    onClick = { onReviewSeller(app.user_id, false) },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Reject", color = RoseOccupied)
                                                }
                                                Button(
                                                    onClick = { onReviewSeller(app.user_id, true) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAvailable),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Approve Seller")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Products" -> {
                        item {
                            Text("Marketplace Moderation (${products.size} Products)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Approved sellers publish automatically. As admin you can hide or restore products if needed.", fontSize = 11.sp, color = Slate500)
                        }
                        items(products, key = { it.id }) { prod ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("KSh ${"%,d".format(prod.price)} • Sold by ${prod.seller_name}", fontSize = 12.sp, color = Slate500)
                                        Text("Status: ${prod.status} • Stock: ${prod.quantity}", fontSize = 11.sp, color = RoyalBlue)
                                    }
                                    Button(
                                        onClick = { onToggleProductVisibility(prod.id, prod.status == "Active") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (prod.status == "Active") RoseOccupied else EmeraldAvailable
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(if (prod.status == "Active") "Hide" else "Restore", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    "Orders" -> {
                        item {
                            Text("All Orders & M-Pesa Verifications (${orders.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        items(orders, key = { it.id }) { order ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Order #${order.order_number}", fontWeight = FontWeight.Bold, color = RoyalBlue)
                                        Text("KSh ${"%,d".format(order.total_amount)}", fontWeight = FontWeight.Black)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${order.product_name} (Qty ${order.quantity})", fontSize = 13.sp)
                                    Text("Buyer: ${order.buyer_name} • Seller: ${order.seller_name}", fontSize = 11.sp, color = Slate500)
                                    if (order.mpesa_reference.isNotEmpty()) {
                                        Text("M-Pesa Code: ${order.mpesa_reference}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                    }
                                    Text("Status: ${order.payment_status} / ${order.order_status}", fontSize = 11.sp, color = Slate600)

                                    if (order.payment_status == "Reference Submitted") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { onVerifyPayment(order.id, true, "") },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAvailable),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().height(36.dp)
                                        ) {
                                            Text("Confirm Payment (Admin)", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Rooms" -> {
                        item {
                            Text("Manage Room Availability & Booking Readiness", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("When a room is marked Available, residents can register with this room number only if you explicitly enable 'Ready to Book'.", fontSize = 11.sp, color = Slate500)
                        }
                        items(rooms, key = { it.id.ifEmpty { it.room_number } }) { room ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth().testTag("admin_room_card_${room.room_number}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Room ${room.room_number} (${room.room_type})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text("KSh ${"%,d".format(room.price_per_semester)} / semester • ${room.floor}", fontSize = 12.sp, color = Slate500)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            StatusBadge(status = room.status)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = { onToggleRoomStatus(room) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (room.status == "Available") RoseOccupied else EmeraldAvailable
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.height(30.dp).testTag("toggle_status_room_${room.room_number}")
                                            ) {
                                                Text(
                                                    text = if (room.status == "Available") "Mark Occupied" else "Mark Available",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Slate100)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Room Booking Readiness (Requirement 3: Admin manages ready_to_book so registration is allowed only if admin permits)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Registration Status: ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                                                Surface(
                                                    color = if (room.ready_to_book) EmeraldLight else AmberLight,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = if (room.ready_to_book) "READY TO BOOK" else "NOT READY",
                                                        color = if (room.ready_to_book) EmeraldAvailable else AmberPending,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = if (room.ready_to_book)
                                                    "New residents CAN register with Room ${room.room_number}."
                                                else
                                                    "Registration with Room ${room.room_number} is currently BLOCKED.",
                                                fontSize = 10.sp,
                                                color = if (room.ready_to_book) EmeraldAvailable else Slate500
                                            )
                                        }

                                        Button(
                                            onClick = { onToggleRoomReadyToBook(room.id.ifEmpty { room.room_number }, !room.ready_to_book) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (room.ready_to_book) Slate600 else RoyalBlue
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp).testTag("toggle_ready_to_book_${room.room_number}")
                                        ) {
                                            Icon(
                                                imageVector = if (room.ready_to_book) Icons.Default.Lock else Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (room.ready_to_book) "Revoke Booking" else "Allow Registration",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Announcements" -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Announcements (${announcements.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = { showNewAnnouncementDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                                ) {
                                    Text("New")
                                }
                            }
                        }
                        items(announcements, key = { it.id }) { ann ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(ann.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(ann.content, fontSize = 13.sp, color = Slate600)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewAnnouncementDialog) {
        var annTitle by remember { mutableStateOf("") }
        var annContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewAnnouncementDialog = false },
            title = { Text("Create Official Announcement", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = annTitle,
                        onValueChange = { annTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = annContent,
                        onValueChange = { annContent = it },
                        label = { Text("Announcement Body") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (annTitle.isNotBlank() && annContent.isNotBlank()) {
                            onCreateAnnouncement(annTitle, annContent)
                            showNewAnnouncementDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Publish Announcement")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewAnnouncementDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = RoseOccupied, modifier = Modifier.size(32.dp)) },
            title = { Text("Delete User Account", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to permanently delete the account for ${userToDelete!!.full_name} (${userToDelete!!.email})? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uid = userToDelete!!.id.ifEmpty { userToDelete!!.auth_user_id }
                        onDeleteUser(uid)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseOccupied),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_delete_user_button")
                ) {
                    Text("Delete User", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.Medium)
        }
    }
}

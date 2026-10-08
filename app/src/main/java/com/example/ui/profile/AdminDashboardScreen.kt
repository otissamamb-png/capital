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
    vacancies: List<VacancyItem>,
    announcements: List<ResidenceAnnouncement>,
    onReviewSeller: (String, Boolean) -> Unit,
    onToggleRoomStatus: (RoomItem) -> Unit,
    onCreateAnnouncement: (String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    var selectedSection by remember { mutableStateOf("Overview") } // "Overview", "Sellers", "Rooms", "Announce"
    var showNewAnnouncementDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administration", fontWeight = FontWeight.Bold) },
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Overview", "Sellers", "Rooms", "Announcements").forEach { section ->
                    val isSelected = selectedSection == section
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSection = section },
                        label = { Text(section, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalBlue,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
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
                            Text("Residence Metrics", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MetricCard("Total Rooms", "${rooms.size}", RoyalBlue, Modifier.weight(1f))
                                MetricCard("Available", "${rooms.count { it.status == "Available" }}", EmeraldAvailable, Modifier.weight(1f))
                                MetricCard("Occupied", "${rooms.count { it.status == "Occupied" }}", Slate700, Modifier.weight(1f))
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Pending Actions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MetricCard("Seller Apps", "${sellerApplications.count { it.status == "Pending" }}", AmberPending, Modifier.weight(1f))
                                MetricCard("Vacancies", "${vacancies.size}", RoyalBlue, Modifier.weight(1f))
                            }
                        }
                    }

                    "Sellers" -> {
                        item {
                            Text("Seller Applications (${sellerApplications.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                                                    Text("Approve")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Rooms" -> {
                        item {
                            Text("Manage Room Availability", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        items(rooms.take(20), key = { it.id.ifEmpty { it.room_number } }) { room ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Room ${room.room_number} (${room.room_type})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("KSh ${"%,d".format(room.price_per_semester)} - ${room.floor}", fontSize = 12.sp, color = Slate500)
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
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(
                                                text = if (room.status == "Available") "Mark Occupied" else "Mark Available",
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

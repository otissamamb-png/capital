package com.example.ui.rooms

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.RoomItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.RoomTypeBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(
    rooms: List<RoomItem>,
    onRoomClick: (RoomItem) -> Unit,
    onAnnounceVacancyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("All") } // "All", "Old Rooms", "New Rooms"
    var selectedStatusFilter by remember { mutableStateOf("All") } // "All", "Available", "Occupied", "Pending Move-Out"

    val filteredRooms = remember(rooms, searchQuery, selectedTypeFilter, selectedStatusFilter) {
        rooms.filter { room ->
            val matchesSearch = searchQuery.isEmpty() ||
                    room.room_number.contains(searchQuery, ignoreCase = true) ||
                    room.description.contains(searchQuery, ignoreCase = true)

            val matchesType = when (selectedTypeFilter) {
                "Old Rooms" -> room.room_type.contains("Old", ignoreCase = true)
                "New Rooms" -> room.room_type.contains("New", ignoreCase = true)
                else -> true
            }

            val matchesStatus = when (selectedStatusFilter) {
                "Available" -> room.status.equals("Available", ignoreCase = true)
                "Occupied" -> room.status.equals("Occupied", ignoreCase = true)
                "Pending Move-Out" -> room.status.contains("Pending", ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesType && matchesStatus
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rooms",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Button(
                        onClick = onAnnounceVacancyClick,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("rooms_announce_vacancy_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Announce", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search room number...", fontSize = 14.sp, color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Slate400)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = Slate400)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rooms_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = Slate50,
                        unfocusedContainerColor = Slate50
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Type Filter Chips: All, Old Rooms, New Rooms
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(listOf("All", "Old Rooms", "New Rooms")) { filter ->
                        val isSelected = selectedTypeFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTypeFilter = filter },
                            label = {
                                Text(
                                    text = if (filter == "Old Rooms") "Old Rooms (KSh 15k)" else if (filter == "New Rooms") "New Rooms (KSh 25k)" else "All Rooms",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalBlue,
                                selectedLabelColor = Color.White,
                                containerColor = Slate100,
                                labelColor = Slate700
                            ),
                            border = null,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                // Status Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 2.dp)
                ) {
                    items(listOf("All", "Vacant", "Occupied")) { status ->
                        val isSelected = selectedStatusFilter == status
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStatusFilter = status },
                            label = {
                                Text(
                                    text = if (status == "Vacant") "🔑 Vacant" else if (status == "Occupied") "🏠 Occupied" else "All Status",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (status == "Vacant") EmeraldAvailable else if (status == "Occupied") RoseOccupied else RoyalBlue,
                                selectedLabelColor = Color.White,
                                containerColor = Slate50,
                                labelColor = Slate600
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (filteredRooms.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.MeetingRoom,
                title = "No rooms match your search",
                description = "Try adjusting your search criteria or announce a move-out vacancy.",
                actionLabel = "Announce Vacancy",
                onActionClick = onAnnounceVacancyClick,
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredRooms, key = { it.id.ifEmpty { it.room_number } }) { room ->
                    RoomListCard(
                        room = room,
                        onClick = { onRoomClick(room) }
                    )
                }
            }
        }
    }
}

@Composable
fun RoomListCard(
    room: RoomItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("room_card_${room.room_number}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Slate100)
            ) {
                if (room.cover_image_url.isNotEmpty()) {
                    coil.compose.AsyncImage(
                        model = room.cover_image_url,
                        contentDescription = "Room ${room.room_number}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.room_showcase_interior_1791459685558),
                        contentDescription = "Room ${room.room_number}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    RoomTypeBadge(roomType = room.room_type)
                    StatusBadge(status = room.status)
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Room ${room.room_number}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = room.floor,
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "KSh ${"%,d".format(room.price_per_semester)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        )
                        Text(
                            text = "per semester",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = room.description,
                    fontSize = 13.sp,
                    color = Slate600,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("view_room_button_${room.room_number}")
                ) {
                    Text("View Room", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

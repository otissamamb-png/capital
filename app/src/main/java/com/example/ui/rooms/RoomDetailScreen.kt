package com.example.ui.rooms

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.RoomItem
import com.example.data.model.SavedItemData
import com.example.ui.components.RoomTypeBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    room: RoomItem,
    isSaved: Boolean,
    onToggleSave: (SavedItemData) -> Unit,
    onContactResident: (RoomItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room ${room.room_number}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("room_detail_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onToggleSave(
                                SavedItemData(
                                    item_type = "room",
                                    item_id = room.id.ifEmpty { room.room_number },
                                    title = "Room ${room.room_number}",
                                    subtitle = room.room_type,
                                    price = room.price_per_semester,
                                    image_url = room.image_url
                                )
                            )
                        },
                        modifier = Modifier.testTag("room_detail_favorite")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save Room",
                            tint = if (isSaved) RoseOccupied else Slate700
                        )
                    }
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out Room ${room.room_number} (${room.room_type}) at Capital Home Residence! KSh ${"%,d".format(room.price_per_semester)} per semester."
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Room"))
                        },
                        modifier = Modifier.testTag("room_detail_share")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { onContactResident(room) },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("contact_resident_button")
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (room.status.equals("Available", ignoreCase = true)) "Inquire with Management" else "Contact Resident",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate50)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Room Image Carousel Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.room_showcase_interior_1791459685558),
                    contentDescription = "Room ${room.room_number}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "1/5",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // Room Title & Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Room ${room.room_number}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        RoomTypeBadge(roomType = room.room_type)
                    }
                    StatusBadge(status = room.status)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "KSh ${"%,d".format(room.price_per_semester)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = RoyalBlue
                    )
                    Text(
                        text = " / semester",
                        fontSize = 13.sp,
                        color = Slate500,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Room Information 2x3 Grid
                Text(
                    text = "Room Information",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(12.dp))

                val features = listOf(
                    Pair(Icons.Default.SingleBed, "Single Room"),
                    Pair(Icons.Default.Bathtub, "Shared Bathroom"),
                    Pair(Icons.Default.Wifi, "Wi-Fi Included"),
                    Pair(Icons.Default.Desk, "Study Desk"),
                    Pair(Icons.Default.Checkroom, "Wardrobe"),
                    Pair(Icons.Default.Layers, room.floor)
                )

                for (chunk in features.chunked(2)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (item in chunk) {
                            RoomFeatureCard(
                                icon = item.first,
                                label = item.second,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description
                Text(
                    text = "Description",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (room.description.isNotEmpty()) room.description else "Spacious and well lit room with a study desk, wardrobe and comfortable bed. Perfect for university students living at Capital Home Residence.",
                    fontSize = 14.sp,
                    color = Slate600,
                    lineHeight = 20.sp
                )

                if (room.occupant_name.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(RoyalBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Current Resident",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                                Text(
                                    text = room.occupant_name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun RoomFeatureCard(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RoyalBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Slate800
            )
        }
    }
}

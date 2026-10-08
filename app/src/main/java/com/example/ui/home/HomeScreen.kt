package com.example.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.ui.components.RoomTypeBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    profile: UserProfile?,
    rooms: List<RoomItem>,
    announcements: List<ResidenceAnnouncement>,
    products: List<MarketplaceProduct>,
    unreadNotificationCount: Int,
    onNavigateToRooms: () -> Unit,
    onNavigateToMarketplace: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onAnnounceVacancyClick: () -> Unit,
    onRoomClick: (RoomItem) -> Unit,
    onProductClick: (MarketplaceProduct) -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val availableRooms = rooms.filter { it.status.equals("Available", ignoreCase = true) }
    val occupiedRooms = rooms.filter { it.status.equals("Occupied", ignoreCase = true) }
    val totalRooms = rooms.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Top Greeting Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Good morning, ${profile?.full_name?.split(" ")?.firstOrNull() ?: "Alex"}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Welcome to Capital Home Residence",
                        fontSize = 13.sp,
                        color = Slate500
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Notification Button with badge
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier.testTag("home_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationCount > 0) {
                                    Badge(containerColor = RoseOccupied) {
                                        Text("$unreadNotificationCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Slate700
                            )
                        }
                    }

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(RoyalBlue)
                            .clickable { onProfileClick() }
                            .testTag("home_profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (profile?.full_name?.take(1) ?: "A").uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        // 2. Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .testTag("home_hero_banner")
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.hero_residence_banner_1791459672502),
                        contentDescription = "Capital Home Residence",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        NavyDark.copy(alpha = 0.92f),
                                        NavyDark.copy(alpha = 0.65f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Live. Connect. Buy. Sell.",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Everything you need at\nCapital Home Residence.",
                            color = Slate200,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // 3. Quick Actions 2x2 Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Find a Room",
                        subtitle = "View available rooms",
                        icon = Icons.Default.Search,
                        iconBg = BlueLight,
                        iconTint = RoyalBlue,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_find_room"),
                        onClick = onNavigateToRooms
                    )
                    QuickActionCard(
                        title = "Announce Vacancy",
                        subtitle = "Post a room vacancy",
                        icon = Icons.Default.Campaign,
                        iconBg = AmberLight,
                        iconTint = AmberPending,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_announce_vacancy"),
                        onClick = onAnnounceVacancyClick
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Marketplace",
                        subtitle = "Buy & sell items",
                        icon = Icons.Default.Storefront,
                        iconBg = EmeraldLight,
                        iconTint = EmeraldAvailable,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_marketplace"),
                        onClick = onNavigateToMarketplace
                    )
                    QuickActionCard(
                        title = "Community",
                        subtitle = "Connect with residents",
                        icon = Icons.Default.Forum,
                        iconBg = Color(0xFFF3E8FF),
                        iconTint = Color(0xFF7E22CE),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_community"),
                        onClick = onNavigateToCommunity
                    )
                }
            }
        }

        // 4. Residence Overview Statistics
        item {
            Column {
                Text(
                    text = "Residence Overview",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        count = availableRooms.size.toString(),
                        label = "Available Rooms",
                        color = EmeraldAvailable,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        count = occupiedRooms.size.toString(),
                        label = "Occupied Rooms",
                        color = RoyalBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        count = totalRooms.toString(),
                        label = "Total Rooms",
                        color = Slate700,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 5. Available Rooms Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Rooms",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    TextButton(
                        onClick = onNavigateToRooms,
                        modifier = Modifier.testTag("see_all_rooms_button")
                    ) {
                        Text(
                            text = "See All",
                            color = RoyalBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (availableRooms.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No rooms currently available. Check back soon or announce a vacancy!",
                            color = Slate500,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(availableRooms.take(6)) { room ->
                            HomeRoomCard(
                                room = room,
                                onClick = { onRoomClick(room) }
                            )
                        }
                    }
                }
            }
        }

        // 6. Important Announcements
        if (announcements.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Important Announcements",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    announcements.take(2).forEach { ann ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(BlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = ann.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ann.content,
                                        fontSize = 12.sp,
                                        color = Slate600,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Recent Marketplace Listings
        if (products.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Marketplace Highlights",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        TextButton(onClick = onNavigateToMarketplace) {
                            Text(
                                text = "Explore",
                                color = RoyalBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(products.take(4)) { prod ->
                            HomeProductCard(
                                product = prod,
                                onClick = { onProductClick(prod) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Slate500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatCard(
    count: String,
    label: String,
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
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = Slate500,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HomeRoomCard(
    room: RoomItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .width(220.dp)
            .clickable { onClick() }
            .testTag("home_room_card_${room.room_number}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.room_showcase_interior_1791459685558),
                    contentDescription = "Room ${room.room_number}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                RoomTypeBadge(
                    roomType = room.room_type,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Room ${room.room_number}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "KSh ${"%,d".format(room.price_per_semester)} / sem",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RoyalBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = room.status)
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeProductCard(
    product: MarketplaceProduct,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = RoyalBlue,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "KSh ${"%,d".format(product.price)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalBlue
            )
        }
    }
}

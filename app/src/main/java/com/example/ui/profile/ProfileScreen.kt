package com.example.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.RoomItem
import com.example.data.model.UserProfile
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: UserProfile?,
    myRoom: RoomItem?,
    onUpdateRoomStatus: (String, String) -> Unit, // roomId, newStatus
    onUpdateRoomDetails: (String, String, String) -> Unit, // roomId, status, coverImageUrl
    onNavigateToMyPosts: () -> Unit,
    onNavigateToMyProducts: () -> Unit,
    onNavigateToMyVacancies: () -> Unit,
    onNavigateToSavedItems: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToOrders: () -> Unit = {},
    onNavigateToNotifications: () -> Unit,
    onNavigateToSellerApp: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit,
    onUpdateProfile: (UserProfile) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showEditRoomDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Profile Header Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(RoyalBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (profile?.full_name?.take(1) ?: "A").uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile?.full_name?.ifEmpty { "Alex Kimani" } ?: "Alex Kimani",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = profile?.email?.ifEmpty { "alex@capitalhome.co.ke" } ?: "alex@capitalhome.co.ke",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                        Text(
                            text = profile?.phone?.ifEmpty { "+254 712 345 678" } ?: "+254 712 345 678",
                            fontSize = 12.sp,
                            color = Slate500
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            color = if (profile?.role == "admin") AmberLight else if (profile?.role == "seller") EmeraldLight else BlueLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = (profile?.role ?: "resident").uppercase(),
                                color = if (profile?.role == "admin") AmberPending else if (profile?.role == "seller") EmeraldAvailable else RoyalBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(onClick = { showEditProfileDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Slate500)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // MY ROOM SECTION (Mandated by Instructions)
            // ==========================================
            Text(
                text = "MY ROOM",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            if (myRoom != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("my_room_card")
                ) {
                    Column {
                        // Room Cover Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(Slate100)
                        ) {
                            if (myRoom.cover_image_url.isNotEmpty()) {
                                AsyncImage(
                                    model = myRoom.cover_image_url,
                                    contentDescription = "Room Cover",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.room_showcase_interior_1791459685558),
                                    contentDescription = "Room",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            StatusBadge(
                                status = myRoom.status,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                            )
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Room ${myRoom.room_number}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = myRoom.room_type,
                                        fontSize = 12.sp,
                                        color = Slate500
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "KSh ${"%,d".format(myRoom.price_per_semester)}",
                                        fontSize = 16.sp,
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

                            Spacer(modifier = Modifier.height(14.dp))

                            // Update Room Status & Edit Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val isOccupied = myRoom.status.equals("Occupied", ignoreCase = true)
                                Button(
                                    onClick = {
                                        val newStatus = if (isOccupied) "Vacant" else "Occupied"
                                        onUpdateRoomStatus(myRoom.id, newStatus)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isOccupied) EmeraldAvailable else RoyalBlue
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1.2f).height(44.dp).testTag("toggle_room_status_button")
                                ) {
                                    Text(
                                        text = if (isOccupied) "Mark as Vacant 🔑" else "Mark as Occupied 🏠",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showEditRoomDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(0.8f).height(44.dp)
                                ) {
                                    Text("Edit Room", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No room assigned yet.",
                            color = Slate600,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Room ${profile?.room_id ?: "204"} linked to your resident profile.",
                            color = Slate500,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // My Activity Section
            Text(
                text = "My Activity",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.Article,
                        title = "My Posts",
                        onClick = onNavigateToMyPosts
                    )
                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(start = 52.dp))
                    ProfileMenuRow(
                        icon = Icons.Default.Storefront,
                        title = "My Products",
                        onClick = onNavigateToMyProducts
                    )
                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(start = 52.dp))
                    ProfileMenuRow(
                        icon = Icons.Default.Campaign,
                        title = "My Room Announcements",
                        onClick = onNavigateToMyVacancies
                    )
                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(start = 52.dp))
                    ProfileMenuRow(
                        icon = Icons.Default.Bookmark,
                        title = "Saved Items & Rooms",
                        onClick = onNavigateToSavedItems
                    )
                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(start = 52.dp))
                    ProfileMenuRow(
                        icon = Icons.Default.ShoppingCart,
                        title = "My Shopping Cart",
                        onClick = onNavigateToCart
                    )
                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(start = 52.dp))
                    ProfileMenuRow(
                        icon = Icons.Default.ReceiptLong,
                        title = "My Orders & Purchases",
                        onClick = onNavigateToOrders
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Account & Settings Section
            Text(
                text = "Account & Settings",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        onClick = onNavigateToNotifications
                    )
                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(start = 52.dp))
                    ProfileMenuRow(
                        icon = Icons.Default.Sell,
                        title = if (profile?.role == "seller") "Seller Dashboard" else "Become a Seller",
                        onClick = onNavigateToSellerApp
                    )

                    // Administration: ONLY shown if backend role is strictly 'admin'
                    if (profile?.role == "admin") {
                        HorizontalDivider(color = Slate100, modifier = Modifier.padding(start = 52.dp))
                        ProfileMenuRow(
                            icon = Icons.Default.AdminPanelSettings,
                            title = "Administration Dashboard",
                            textColor = RoyalBlue,
                            iconTint = RoyalBlue,
                            onClick = onNavigateToAdminDashboard
                        )
                    }

                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(start = 52.dp))
                    ProfileMenuRow(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = "Log Out",
                        textColor = RoseOccupied,
                        iconTint = RoseOccupied,
                        onClick = onSignOut
                    )
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(profile?.full_name ?: "") }
        var editPhone by remember { mutableStateOf(profile?.phone ?: "") }
        var editRoom by remember { mutableStateOf(profile?.room_id ?: "") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_name")
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone")
                    )
                    OutlinedTextField(
                        value = editRoom,
                        onValueChange = { editRoom = it },
                        label = { Text("Room Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_room")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = (profile ?: UserProfile()).copy(
                            full_name = editName,
                            phone = editPhone,
                            room_id = editRoom
                        )
                        onUpdateProfile(updated)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Room Dialog (Replace Cover Image & Status)
    if (showEditRoomDialog && myRoom != null) {
        var editStatus by remember { mutableStateOf(myRoom.status) }
        var updatedBase64Image by remember { mutableStateOf(myRoom.cover_image_url) }
        var tempBitmap by remember { mutableStateOf<Bitmap?>(null) }

        val roomImagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    if (originalBitmap != null) {
                        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 800, (800 * originalBitmap.height / originalBitmap.width), true)
                        tempBitmap = scaledBitmap
                        val outputStream = ByteArrayOutputStream()
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                        updatedBase64Image = "data:image/jpeg;base64," + Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                    }
                } catch (_: Exception) {}
            }
        }

        AlertDialog(
            onDismissRequest = { showEditRoomDialog = false },
            title = { Text("Update Room ${myRoom.room_number}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Status", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = editStatus == "Occupied",
                            onClick = { editStatus = "Occupied" },
                            label = { Text("🏠 Occupied") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = editStatus == "Vacant",
                            onClick = { editStatus = "Vacant" },
                            label = { Text("🔑 Vacant") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Room Cover Photo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    if (tempBitmap != null) {
                        Image(
                            bitmap = tempBitmap!!.asImageBitmap(),
                            contentDescription = "New Cover Preview",
                            modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            roomImagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick New Image from Device")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateRoomDetails(myRoom.id, editStatus, updatedBase64Image)
                        showEditRoomDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditRoomDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    textColor: Color = Slate900,
    iconTint: Color = Slate600
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Slate400,
            modifier = Modifier.size(20.dp)
        )
    }
}

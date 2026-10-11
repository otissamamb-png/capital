package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.data.repository.ResidenceRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.chat.ChatConversationScreen
import com.example.ui.chat.ChatListScreen
import com.example.ui.community.CommentsDialog
import com.example.ui.community.CommunityScreen
import com.example.ui.community.CreatePostScreen
import com.example.ui.home.HomeScreen
import com.example.ui.marketplace.AddProductDialog
import com.example.ui.marketplace.CartScreen
import com.example.ui.marketplace.MarketplaceScreen
import com.example.ui.marketplace.OrderTrackingScreen
import com.example.ui.marketplace.PaymentScreen
import com.example.ui.marketplace.ProductDetailScreen
import com.example.ui.notifications.NotificationsScreen
import com.example.ui.profile.AdminDashboardScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.profile.SavedItemsScreen
import com.example.ui.rooms.AnnounceVacancyDialog
import com.example.ui.rooms.RoomDetailScreen
import com.example.ui.rooms.RoomsScreen
import com.example.ui.seller.SellerApplicationScreen
import com.example.ui.seller.SellerDashboardScreen
import com.example.ui.theme.CapitalHomeTheme
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.Slate400
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CapitalHomeTheme {
                CapitalHomeApp()
            }
        }
    }
}

@Composable
fun CapitalHomeApp() {
    val context = LocalContext.current
    val repository = remember { ResidenceRepository.create(context) }
    var currentSessionUser by remember { mutableStateOf(repository.getCurrentSessionUser()) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        com.example.util.NotificationHelper.clearAllNotifications(context)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser != null && currentSessionUser == null) {
                currentSessionUser = SessionUser(
                    id = auth.currentUser!!.uid,
                    email = auth.currentUser!!.email ?: "",
                    name = auth.currentUser!!.displayName ?: "Resident",
                    role = "resident"
                )
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentSessionUser == null) {
        AuthScreen(
            onAuthSuccess = {
                currentSessionUser = repository.getCurrentSessionUser()
            }
        )
    } else {
        MainResidenceContent(
            userId = currentSessionUser!!.id,
            userEmail = currentSessionUser!!.email.ifBlank { "alex@capitalhome.co.ke" },
            userName = currentSessionUser!!.name.ifBlank { "Alex Kimani" },
            userRole = currentSessionUser!!.role,
            onSignOut = {
                repository.signOut()
                currentSessionUser = null
            }
        )
    }
}

@Composable
fun MainResidenceContent(
    userId: String,
    userEmail: String,
    userName: String,
    userRole: String = "resident",
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { ResidenceRepository.create(context) }

    // Seed database on first launch
    LaunchedEffect(Unit) {
        repository.seedInitialDataIfEmpty()
    }

    // State flows from persistent cloud database
    val profile by repository.observeProfile(userId).collectAsStateWithLifecycle(initialValue = null)
    val rooms by repository.observeRooms().collectAsStateWithLifecycle(initialValue = emptyList())
    val vacancies by repository.observeVacancies().collectAsStateWithLifecycle(initialValue = emptyList())
    val products by repository.observeProducts().collectAsStateWithLifecycle(initialValue = emptyList())
    val posts by repository.observePosts().collectAsStateWithLifecycle(initialValue = emptyList())
    val conversations by repository.observeConversations(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    val notifications by repository.observeNotifications(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    val savedItems by repository.observeSavedItems(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    val cartItems by repository.observeCartItems(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    val announcements by repository.observeAnnouncements().collectAsStateWithLifecycle(initialValue = emptyList())
    val sellerApplications by repository.observeSellerApplications().collectAsStateWithLifecycle(initialValue = emptyList())
    val likedPostIds by repository.observeUserReactions(userId).collectAsStateWithLifecycle(initialValue = emptySet())
    val buyerOrders by repository.observeBuyerOrders(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    val sellerOrders by repository.observeSellerOrders(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    val allOrders by repository.observeAllOrders().collectAsStateWithLifecycle(initialValue = emptyList())
    val allUsers by repository.observeAllUsers().collectAsStateWithLifecycle(initialValue = emptyList())

    // Ensure profile is created in database if not present and register FCM token
    LaunchedEffect(userId) {
        com.example.service.CapitalHomeFirebaseMessagingService.registerCurrentToken(userId)
        val existing = repository.getProfileOnce(userId)
        if (existing == null) {
            val session = repository.getCurrentSessionUser()
            val defaultProf = UserProfile(
                auth_user_id = userId,
                full_name = session?.name ?: userName,
                email = session?.email ?: userEmail,
                phone = session?.phone ?: "+254 712 345 678",
                room_id = session?.roomId ?: "",
                role = session?.role ?: userRole
            )
            repository.saveProfile(defaultProf)
        }
    }

    // Navigation Tab state (Default to 0: Home for normal users)
    var selectedTab by remember { mutableStateOf(0) } // 0: Home, 1: Rooms, 2: Marketplace, 3: Posts, 4: Chats, 5: Profile

    // Sub-screen states: Admin automatically redirected to Admin Dashboard on login, normal users to Home
    var showAdminDashboardScreen by remember(userId, userRole) {
        mutableStateOf(userRole == "admin")
    }

    LaunchedEffect(profile?.role) {
        if (profile?.role == "admin" && !showAdminDashboardScreen) {
            showAdminDashboardScreen = true
        }
    }

    // Sub-screen states
    var selectedRoom by remember { mutableStateOf<RoomItem?>(null) }
    var selectedProduct by remember { mutableStateOf<MarketplaceProduct?>(null) }
    var selectedConversation by remember { mutableStateOf<ConversationItem?>(null) }
    var showAnnounceVacancyDialog by remember { mutableStateOf(false) }
    var showCreatePostScreen by remember { mutableStateOf(false) }
    var commentsPost by remember { mutableStateOf<CommunityPost?>(null) }
    var showCartScreen by remember { mutableStateOf(false) }
    var showNotificationsScreen by remember { mutableStateOf(false) }
    var showSavedItemsScreen by remember { mutableStateOf(false) }
    var showSellerAppScreen by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showSellerDashboardScreen by remember { mutableStateOf(false) }
    var showOrdersScreen by remember { mutableStateOf(false) }
    var activePaymentOrder by remember { mutableStateOf<OrderRecord?>(null) }
    var trackingOrderId by remember { mutableStateOf<String?>(null) }

    // Comments for active post dialog
    val activeComments by remember(commentsPost) {
        if (commentsPost != null) repository.observeComments(commentsPost!!.id)
        else kotlinx.coroutines.flow.flowOf(emptyList())
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    // Messages for active conversation
    val activeMessages by remember(selectedConversation) {
        if (selectedConversation != null) repository.observeMessages(selectedConversation!!.id)
        else kotlinx.coroutines.flow.flowOf(emptyList())
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val savedProductIds = remember(savedItems) {
        savedItems.filter { it.item_type == "product" }.map { it.item_id }.toSet()
    }
    val savedRoomIds = remember(savedItems) {
        savedItems.filter { it.item_type == "room" }.map { it.item_id }.toSet()
    }
    val unreadNotifCount = remember(notifications) {
        notifications.count { !it.is_read }
    }

    // Subscreen Router
    when {
        selectedRoom != null -> {
            RoomDetailScreen(
                room = selectedRoom!!,
                isSaved = savedRoomIds.contains(selectedRoom!!.id.ifEmpty { selectedRoom!!.room_number }),
                onToggleSave = { item ->
                    coroutineScope.launch { repository.toggleSaveItem(item) }
                },
                onContactResident = { room ->
                    coroutineScope.launch {
                        val result = repository.startOrGetConversation(
                            targetUserId = room.occupant_id.ifEmpty { "mgmt_capital_home" },
                            targetUserName = if (room.occupant_name.isNotEmpty()) room.occupant_name else "Capital Home Management",
                            type = "Room",
                            roomId = room.id.ifEmpty { room.room_number },
                            roomNumber = room.room_number
                        )
                        result.getOrNull()?.let { convId ->
                            selectedRoom = null
                            selectedConversation = ConversationItem(
                                id = convId,
                                other_user_name = if (room.occupant_name.isNotEmpty()) room.occupant_name else "Residence Management",
                                room_number = room.room_number
                            )
                        }
                    }
                },
                onBack = { selectedRoom = null }
            )
        }

        selectedProduct != null -> {
            ProductDetailScreen(
                product = selectedProduct!!,
                isSaved = savedProductIds.contains(selectedProduct!!.id),
                onToggleSave = { item ->
                    coroutineScope.launch { repository.toggleSaveItem(item) }
                },
                onAddToCart = { cartItem ->
                    coroutineScope.launch { repository.addToCart(cartItem) }
                },
                onBuyNow = { prod, qty ->
                    coroutineScope.launch {
                        val result = repository.createOrder(
                            product = prod,
                            quantity = qty,
                            buyerName = profile?.full_name ?: userName,
                            buyerPhone = profile?.phone ?: "+254 700 000 000",
                            pickupOrDelivery = prod.delivery_option.ifEmpty { "Pickup at Capital Home Residence" }
                        )
                        result.getOrNull()?.let { order ->
                            selectedProduct = null
                            activePaymentOrder = order
                        }
                    }
                },
                onChatWithSeller = { prod ->
                    coroutineScope.launch {
                        val result = repository.startOrGetConversation(
                            targetUserId = prod.seller_id,
                            targetUserName = prod.seller_name,
                            type = "Product",
                            productId = prod.id,
                            productName = prod.name
                        )
                        result.getOrNull()?.let { convId ->
                            selectedProduct = null
                            selectedConversation = ConversationItem(
                                id = convId,
                                other_user_name = prod.seller_name,
                                product_name = prod.name
                            )
                        }
                    }
                },
                onBack = { selectedProduct = null }
            )
        }

        selectedConversation != null -> {
            ChatConversationScreen(
                conversation = selectedConversation!!,
                currentUserId = userId,
                messages = activeMessages,
                onSendMessage = { text, attachment ->
                    coroutineScope.launch {
                        repository.sendMessage(
                            conversationId = selectedConversation!!.id,
                            text = text,
                            senderName = profile?.full_name ?: userName,
                            attachmentUrl = attachment
                        )
                    }
                },
                onBack = { selectedConversation = null }
            )
        }

        showCreatePostScreen -> {
            CreatePostScreen(
                profile = profile,
                onSubmitPost = { post ->
                    coroutineScope.launch {
                        repository.createPost(post)
                        showCreatePostScreen = false
                    }
                },
                onBack = { showCreatePostScreen = false }
            )
        }

        activePaymentOrder != null -> {
            PaymentScreen(
                order = activePaymentOrder!!,
                onSubmitReference = { ref ->
                    coroutineScope.launch {
                        repository.submitMpesaReference(activePaymentOrder!!.id, ref)
                        activePaymentOrder = activePaymentOrder!!.copy(
                            payment_status = "Reference Submitted",
                            order_status = "Payment Verification",
                            mpesa_reference = ref
                        )
                    }
                },
                onCancelPayment = {
                    val orderToCancel = activePaymentOrder
                    activePaymentOrder = null
                    if (orderToCancel != null) {
                        coroutineScope.launch {
                            repository.cancelOrder(orderToCancel.id)
                        }
                    }
                },
                onReturnToMarketplace = {
                    activePaymentOrder = null
                    selectedTab = 2 // Marketplace tab
                },
                onBack = { activePaymentOrder = null }
            )
        }

        showOrdersScreen -> {
            OrderTrackingScreen(
                orders = buyerOrders,
                initialOrderId = trackingOrderId,
                onPayOrder = { order ->
                    activePaymentOrder = order
                },
                onCancelOrder = { orderId ->
                    coroutineScope.launch {
                        repository.cancelOrder(orderId)
                    }
                },
                onConfirmReceived = { orderId ->
                    coroutineScope.launch {
                        repository.updateOrderStatus(orderId, "Completed")
                    }
                },
                onChatWithSeller = { sellerId, sellerName ->
                    coroutineScope.launch {
                        val result = repository.startOrGetConversation(
                            targetUserId = sellerId,
                            targetUserName = sellerName,
                            type = "Direct"
                        )
                        val convId = result.getOrNull() ?: if (userId < sellerId) "conv_${userId}_${sellerId}" else "conv_${sellerId}_${userId}"
                        selectedConversation = ConversationItem(
                            id = convId,
                            member_ids = listOf(userId, sellerId),
                            other_user_id = sellerId,
                            other_user_name = sellerName,
                            updated_at = com.google.firebase.Timestamp.now()
                        )
                        showOrdersScreen = false
                    }
                },
                onBack = {
                    showOrdersScreen = false
                    trackingOrderId = null
                }
            )
        }

        showSellerDashboardScreen -> {
            SellerDashboardScreen(
                products = products.filter { it.seller_id == userId },
                orders = sellerOrders,
                onAddProductClick = { showAddProductDialog = true },
                onVerifyPayment = { orderId, confirmed, reason ->
                    coroutineScope.launch {
                        repository.verifyPayment(orderId, confirmed, reason)
                    }
                },
                onUpdateOrderStatus = { orderId, newStatus ->
                    coroutineScope.launch {
                        repository.updateOrderStatus(orderId, newStatus)
                    }
                },
                onEditProduct = { updated ->
                    coroutineScope.launch {
                        repository.updateProduct(updated)
                    }
                },
                onDeleteProduct = { productId ->
                    coroutineScope.launch {
                        repository.deleteProduct(productId)
                    }
                },
                onBack = { showSellerDashboardScreen = false }
            )
        }

        showCartScreen -> {
            CartScreen(
                cartItems = cartItems,
                onRemoveItem = { id ->
                    coroutineScope.launch { repository.removeFromCart(id) }
                },
                onClearCart = {
                    coroutineScope.launch { repository.clearCart(userId) }
                },
                onCheckoutCart = {
                    val firstItem = cartItems.firstOrNull()
                    if (firstItem != null) {
                        val prod = products.find { it.id == firstItem.product_id }
                        if (prod != null) {
                            coroutineScope.launch {
                                val result = repository.createOrder(
                                    product = prod,
                                    quantity = firstItem.quantity,
                                    buyerName = profile?.full_name ?: userName,
                                    buyerPhone = profile?.phone ?: "+254 700 000 000",
                                    pickupOrDelivery = prod.delivery_option.ifEmpty { "Pickup at Capital Home Residence" }
                                )
                                result.getOrNull()?.let { order ->
                                    repository.removeFromCart(firstItem.id.ifEmpty { firstItem.product_id })
                                    showCartScreen = false
                                    activePaymentOrder = order
                                }
                            }
                        }
                    }
                },
                onBack = { showCartScreen = false }
            )
        }

        showNotificationsScreen -> {
            LaunchedEffect(Unit) {
                repository.markAllNotificationsRead(userId)
            }
            NotificationsScreen(
                notifications = notifications,
                onMarkRead = { id ->
                    coroutineScope.launch { repository.markNotificationRead(id) }
                },
                onTrackOrder = { orderId ->
                    trackingOrderId = orderId
                    showOrdersScreen = true
                    showNotificationsScreen = false
                },
                onBack = { showNotificationsScreen = false }
            )
        }

        showSavedItemsScreen -> {
            SavedItemsScreen(
                savedItems = savedItems,
                onRemoveItem = { item ->
                    coroutineScope.launch { repository.toggleSaveItem(item) }
                },
                onBack = { showSavedItemsScreen = false }
            )
        }

        showSellerAppScreen -> {
            SellerApplicationScreen(
                profile = profile,
                existingApplication = sellerApplications.firstOrNull { it.user_id == userId },
                onSubmitApplication = { app: SellerApplicationData ->
                    coroutineScope.launch { repository.submitSellerApplication(app) }
                },
                onBack = { showSellerAppScreen = false }
            )
        }

        showAdminDashboardScreen -> {
            AdminDashboardScreen(
                rooms = rooms,
                sellerApplications = sellerApplications,
                products = products,
                orders = allOrders,
                vacancies = vacancies,
                announcements = announcements,
                users = allUsers,
                onReviewSeller = { targetUserId, approve ->
                    coroutineScope.launch { repository.reviewSellerApplication(targetUserId, approve) }
                },
                onToggleProductVisibility = { prodId, hide ->
                    coroutineScope.launch {
                        repository.updateProductStatus(prodId, if (hide) "Hidden" else "Active")
                    }
                },
                onVerifyPayment = { orderId, confirmed, reason ->
                    coroutineScope.launch {
                        repository.verifyPayment(orderId, confirmed, reason)
                    }
                },
                onToggleRoomStatus = { room ->
                    coroutineScope.launch {
                        val newStatus = if (room.status == "Available") "Occupied" else "Available"
                        repository.updateRoomStatus(room.id, newStatus)
                    }
                },
                onToggleRoomReadyToBook = { roomId, ready ->
                    coroutineScope.launch {
                        repository.toggleRoomReadyToBook(roomId, ready)
                    }
                },
                onDeleteUser = { targetUserId ->
                    coroutineScope.launch {
                        repository.deleteUser(targetUserId)
                    }
                },
                onCreateAnnouncement = { title, content ->
                    coroutineScope.launch { repository.createAnnouncement(title, content) }
                },
                onBack = { showAdminDashboardScreen = false }
            )
        }

        else -> {
            // Main Bottom Navigation Scaffold
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RoyalBlue,
                                selectedTextColor = RoyalBlue,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_item_home")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.MeetingRoom, contentDescription = "Rooms") },
                            label = { Text("Rooms", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RoyalBlue,
                                selectedTextColor = RoyalBlue,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_item_rooms")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.Storefront, contentDescription = "Marketplace") },
                            label = { Text("Marketplace", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RoyalBlue,
                                selectedTextColor = RoyalBlue,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_item_marketplace")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = { Icon(Icons.Default.DynamicFeed, contentDescription = "Posts") },
                            label = { Text("Posts", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RoyalBlue,
                                selectedTextColor = RoyalBlue,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_item_posts")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        val totalUnread = conversations.sumOf { it.unread_count }
                                        if (totalUnread > 0) {
                                            Badge(containerColor = RoyalBlue) { Text("$totalUnread") }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = "Chats")
                                }
                            },
                            label = { Text("Chats", fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RoyalBlue,
                                selectedTextColor = RoyalBlue,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_item_chats")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 5,
                            onClick = { selectedTab = 5 },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile", fontWeight = if (selectedTab == 5) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RoyalBlue,
                                selectedTextColor = RoyalBlue,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_item_profile")
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                when (selectedTab) {
                    0 -> HomeScreen(
                        profile = profile,
                        rooms = rooms,
                        announcements = announcements,
                        products = products,
                        activeOrders = buyerOrders.filter { it.order_status !in listOf("Completed", "Cancelled") },
                        unreadNotificationCount = unreadNotifCount,
                        onNavigateToRooms = { selectedTab = 1 },
                        onNavigateToMarketplace = { selectedTab = 2 },
                        onNavigateToCommunity = { selectedTab = 3 },
                        onAnnounceVacancyClick = { showAnnounceVacancyDialog = true },
                        onRoomClick = { room -> selectedRoom = room },
                        onProductClick = { prod -> selectedProduct = prod },
                        onNotificationsClick = { showNotificationsScreen = true },
                        onProfileClick = { selectedTab = 5 },
                        onTrackOrderClick = { order ->
                            trackingOrderId = order.id
                            showOrdersScreen = true
                        },
                        onViewAllOrdersClick = { showOrdersScreen = true },
                        modifier = Modifier.padding(innerPadding)
                    )

                    1 -> RoomsScreen(
                        rooms = rooms,
                        onRoomClick = { room -> selectedRoom = room },
                        onAnnounceVacancyClick = { showAnnounceVacancyDialog = true },
                        modifier = Modifier.padding(innerPadding)
                    )

                    2 -> MarketplaceScreen(
                        products = products,
                        cartItemCount = cartItems.size,
                        savedProductIds = savedProductIds,
                        onProductClick = { prod -> selectedProduct = prod },
                        onCartClick = { showCartScreen = true },
                        onOrdersClick = { showOrdersScreen = true },
                        onSellClick = {
                            if (profile?.role == "seller" || profile?.role == "admin") {
                                showSellerDashboardScreen = true
                            } else {
                                showSellerAppScreen = true
                            }
                        },
                        onToggleSave = { item ->
                            coroutineScope.launch { repository.toggleSaveItem(item) }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )

                    3 -> CommunityScreen(
                        profile = profile,
                        posts = posts,
                        likedPostIds = likedPostIds,
                        onCreatePostClick = { showCreatePostScreen = true },
                        onToggleLike = { postId ->
                            coroutineScope.launch { repository.toggleLikePost(postId) }
                        },
                        onOpenComments = { post -> commentsPost = post },
                        onEditPost = { postId, newContent, newType ->
                            coroutineScope.launch { repository.updatePost(postId, newContent, newType) }
                        },
                        onDeletePost = { postId ->
                            coroutineScope.launch { repository.deletePost(postId) }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )

                    4 -> ChatListScreen(
                        conversations = conversations,
                        onConversationClick = { conv -> selectedConversation = conv },
                        modifier = Modifier.padding(innerPadding)
                    )

                    5 -> {
                        val myRoom = rooms.find { it.resident_id == userId || it.id == profile?.room_id || it.room_number == profile?.room_id }
                        ProfileScreen(
                            profile = profile,
                            myRoom = myRoom,
                            onUpdateRoomStatus = { roomId, newStatus ->
                                coroutineScope.launch { repository.updateRoomStatus(roomId, newStatus) }
                            },
                            onUpdateRoomDetails = { roomId, status, coverUrl ->
                                coroutineScope.launch { repository.updateRoomDetails(roomId, status, coverUrl) }
                            },
                            onNavigateToMyPosts = { selectedTab = 3 },
                            onNavigateToMyProducts = { selectedTab = 2 },
                            onNavigateToMyVacancies = { showAnnounceVacancyDialog = true },
                            onNavigateToSavedItems = { showSavedItemsScreen = true },
                            onNavigateToCart = { showCartScreen = true },
                            onNavigateToOrders = { showOrdersScreen = true },
                            onNavigateToNotifications = { showNotificationsScreen = true },
                            onNavigateToSellerApp = {
                                if (profile?.role == "seller") {
                                    showSellerDashboardScreen = true
                                } else {
                                    showSellerAppScreen = true
                                }
                            },
                            onNavigateToAdminDashboard = { showAdminDashboardScreen = true },
                            onUpdateProfile = { updated ->
                                coroutineScope.launch { repository.saveProfile(updated) }
                            },
                            onSignOut = {
                                onSignOut()
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    // Announce Vacancy Dialog
    if (showAnnounceVacancyDialog) {
        AnnounceVacancyDialog(
            profile = profile,
            onDismiss = { showAnnounceVacancyDialog = false },
            onSubmit = { vacancy ->
                coroutineScope.launch {
                    repository.announceVacancy(vacancy)
                    showAnnounceVacancyDialog = false
                }
            }
        )
    }

    // Add Product Dialog (for sellers)
    if (showAddProductDialog) {
        AddProductDialog(
            profile = profile,
            onDismiss = { showAddProductDialog = false },
            onSubmit = { product ->
                coroutineScope.launch {
                    repository.addProduct(product)
                    showAddProductDialog = false
                }
            }
        )
    }

    // Comments Dialog
    if (commentsPost != null) {
        CommentsDialog(
            post = commentsPost!!,
            comments = activeComments,
            onAddComment = { content ->
                coroutineScope.launch {
                    repository.addComment(
                        postId = commentsPost!!.id,
                        content = content,
                        userName = profile?.full_name ?: userName,
                        userPhoto = ""
                    )
                }
            },
            onDismiss = { commentsPost = null }
        )
    }
}

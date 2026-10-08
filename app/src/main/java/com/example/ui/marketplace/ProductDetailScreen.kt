package com.example.ui.marketplace

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CartItemData
import com.example.data.model.MarketplaceProduct
import com.example.data.model.SavedItemData
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: MarketplaceProduct,
    isSaved: Boolean,
    onToggleSave: (SavedItemData) -> Unit,
    onAddToCart: (CartItemData) -> Unit,
    onBuyNow: (MarketplaceProduct, Long) -> Unit,
    onChatWithSeller: (MarketplaceProduct) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onBack() }
    var selectedImageIndex by remember { mutableStateOf(0) }
    var selectedQuantity by remember { mutableStateOf(1L) }
    var showAddedSnackbar by remember { mutableStateOf(false) }

    val allImages = remember(product) {
        if (product.image_urls.isNotEmpty()) product.image_urls
        else if (product.image_url.isNotEmpty()) listOf(product.image_url)
        else emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("product_detail_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onToggleSave(
                                SavedItemData(
                                    item_type = "product",
                                    item_id = product.id,
                                    title = product.name,
                                    subtitle = "KSh ${product.price}",
                                    price = product.price,
                                    image_url = product.image_url
                                )
                            )
                        },
                        modifier = Modifier.testTag("product_detail_favorite")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) RoseOccupied else Slate700
                        )
                    }
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out ${product.name} on Capital Home Residence Marketplace for KSh ${"%,d".format(product.price)}! Contact seller: ${product.seller_phone}"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Product"))
                        },
                        modifier = Modifier.testTag("product_detail_share")
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
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    // Two prominent shopping action buttons right at the bottom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (product.quantity > 0) {
                                    onAddToCart(
                                        CartItemData(
                                            product_id = product.id,
                                            product_name = product.name,
                                            price = product.price,
                                            quantity = selectedQuantity,
                                            image_url = product.image_url,
                                            seller_name = product.seller_name,
                                            seller_id = product.seller_id
                                        )
                                    )
                                    showAddedSnackbar = true
                                }
                            },
                            enabled = product.quantity > 0,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("add_to_cart_button")
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add to Cart", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (product.quantity > 0) {
                                    onBuyNow(product, selectedQuantity)
                                }
                            },
                            enabled = product.quantity > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("buy_now_button")
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buy Now", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
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
            // 1. PRODUCT GALLERY (Swipeable/Clickable thumbnails)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Slate100)
            ) {
                if (allImages.isNotEmpty()) {
                    val currentImg = allImages.getOrNull(selectedImageIndex) ?: allImages.first()
                    AsyncImage(
                        model = currentImg,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(80.dp))
                    }
                }

                // Discount Badge on image
                if (product.discountPercent > 0) {
                    Surface(
                        color = RoseOccupied,
                        shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "${product.discountPercent}% OFF",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Stock Badge on image
                Surface(
                    color = when (product.stockStatus) {
                        "In Stock" -> EmeraldAvailable
                        "Low Stock" -> AmberPending
                        else -> RoseOccupied
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                ) {
                    Text(
                        text = product.stockStatus,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Thumbnail gallery bar
            if (allImages.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    itemsIndexed(allImages) { index, imgUrl ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(if (selectedImageIndex == index) 2.dp else 1.dp, if (selectedImageIndex == index) RoyalBlue else Slate200, RoundedCornerShape(8.dp))
                                .clickable { selectedImageIndex = index }
                        ) {
                            AsyncImage(
                                model = imgUrl,
                                contentDescription = "Thumb $index",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // 2. PRODUCT DETAILS SECTION
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                if (showAddedSnackbar) {
                    Surface(
                        color = EmeraldLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAvailable)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Added to persistent cart!", color = EmeraldAvailable, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Title and Condition
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = product.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = Slate100,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = product.condition,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Rating & Review count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                    Text(
                        text = " ${product.rating} (${product.review_count} reviews) • Category: ${product.category}",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }

                // Pricing with Discount
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "KSh ${"%,d".format(product.price)}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = RoyalBlue
                    )
                    if (product.original_price > product.price) {
                        Text(
                            text = "KSh ${"%,d".format(product.original_price)}",
                            fontSize = 16.sp,
                            color = Slate400,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                        Surface(
                            color = RoseLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "-${product.discountPercent}%",
                                color = RoseOccupied,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Quantity Selector
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Quantity", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${product.quantity} items available in stock", fontSize = 11.sp, color = Slate500)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (selectedQuantity > 1) selectedQuantity-- },
                                enabled = selectedQuantity > 1,
                                modifier = Modifier.size(34.dp).background(Slate100, CircleShape)
                            ) {
                                Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "$selectedQuantity",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                            IconButton(
                                onClick = { if (selectedQuantity < product.quantity) selectedQuantity++ },
                                enabled = selectedQuantity < product.quantity,
                                modifier = Modifier.size(34.dp).background(Slate100, CircleShape)
                            ) {
                                Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Description
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Product Description", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product.description.ifEmpty { "No description provided for this product." },
                            fontSize = 13.sp,
                            color = Slate700,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Specifications Table
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Specifications", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                        Spacer(modifier = Modifier.height(2.dp))

                        if (product.brand.isNotEmpty()) SpecRow("Brand", product.brand)
                        if (product.model.isNotEmpty()) SpecRow("Model", product.model)
                        if (product.color.isNotEmpty()) SpecRow("Color", product.color)
                        if (product.size.isNotEmpty()) SpecRow("Size", product.size)
                        if (product.material.isNotEmpty()) SpecRow("Material", product.material)
                        SpecRow("Condition", product.condition)
                        if (product.additional_specs.isNotEmpty()) SpecRow("Additional Specs", product.additional_specs)
                    }
                }

                // Pickup & Delivery Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pickup & Delivery", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(product.location.ifEmpty { "Capital Home Residence" }, fontSize = 13.sp, color = Slate800)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(product.delivery_option.ifEmpty { "Pickup at Capital Home Residence" }, fontSize = 13.sp, color = Slate800)
                        }
                    }
                }

                // Seller Information & M-Pesa Payment Methods Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().testTag("seller_profile_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Seller Information", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(46.dp).clip(CircleShape).background(RoyalBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (product.seller_name.take(1).ifEmpty { "S" }).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.seller_name.ifEmpty { "Resident Seller" }, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Capital Home Residence Resident", fontSize = 11.sp, color = Slate500)
                            }
                            IconButton(onClick = { onChatWithSeller(product) }) {
                                Icon(Icons.Default.Chat, contentDescription = "Chat", tint = RoyalBlue)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate100)

                        Text("Approved M-Pesa Payment Method", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = BlueLight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${product.payment_method}: ${product.payment_number.ifEmpty { "Provided upon checkout" }}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RoyalBlue
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
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Slate500, modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate800, modifier = Modifier.weight(1.5f))
    }
}

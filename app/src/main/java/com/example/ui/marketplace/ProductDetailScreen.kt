package com.example.ui.marketplace

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onChatWithSeller: (MarketplaceProduct) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onBack() }
    var showAddedSnackbar by remember { mutableStateOf(false) }

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
                            contentDescription = "Save Product",
                            tint = if (isSaved) RoseOccupied else Slate700
                        )
                    }
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out ${product.name} for KSh ${"%,d".format(product.price)} on Capital Home Residence Marketplace! Contact seller: ${product.seller_phone}"
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
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onAddToCart(
                                    CartItemData(
                                        product_id = product.id,
                                        product_name = product.name,
                                        price = product.price,
                                        quantity = 1L,
                                        image_url = product.image_url,
                                        seller_name = product.seller_name,
                                        seller_id = product.seller_id
                                    )
                                )
                                showAddedSnackbar = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("add_to_cart_button")
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add to Cart", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onChatWithSeller(product) },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(50.dp)
                                .testTag("chat_with_seller_button")
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chat with Seller", fontWeight = FontWeight.Bold)
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
            // Product Hero Visual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Slate100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = RoyalBlue,
                    modifier = Modifier.size(90.dp)
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                if (showAddedSnackbar) {
                    Surface(
                        color = EmeraldLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAvailable)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Added to Cart!", color = EmeraldAvailable, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        modifier = Modifier.weight(1f)
                    )
                    StatusBadge(status = product.status)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "KSh ${"%,d".format(product.price)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = RoyalBlue
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Seller Information Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChatWithSeller(product) }
                        .testTag("seller_profile_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(RoyalBlue),
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
                            Text(
                                text = product.seller_name.ifEmpty { "Residence Seller" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = EmeraldAvailable,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Approved Seller",
                                    fontSize = 12.sp,
                                    color = EmeraldAvailable,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Details Card (M-Pesa details)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = EmeraldAvailable,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Seller Payment Information",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Payment Method: ${product.payment_method}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate800
                        )
                        if (product.payment_number.isNotEmpty()) {
                            Text(
                                text = "Number / Till: ${product.payment_number}",
                                fontSize = 13.sp,
                                color = Slate700
                            )
                        }
                        if (product.payment_name.isNotEmpty()) {
                            Text(
                                text = "Account Name: ${product.payment_name}",
                                fontSize = 13.sp,
                                color = Slate700
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Note: You will pay directly to the seller via M-Pesa upon agreement.",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Product Details
                Text(
                    text = "Description",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = product.description.ifEmpty { "High quality item from resident seller." },
                    fontSize = 14.sp,
                    color = Slate600,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Condition", fontSize = 12.sp, color = Slate500)
                        Text(product.condition, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Location", fontSize = 12.sp, color = Slate500)
                        Text(product.location, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Category", fontSize = 12.sp, color = Slate500)
                        Text(product.category, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

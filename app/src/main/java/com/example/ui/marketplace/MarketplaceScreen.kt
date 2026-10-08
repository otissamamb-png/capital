package com.example.ui.marketplace

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItemData
import com.example.data.model.MarketplaceProduct
import com.example.data.model.SavedItemData
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    products: List<MarketplaceProduct>,
    cartItemCount: Int,
    savedProductIds: Set<String>,
    onProductClick: (MarketplaceProduct) -> Unit,
    onCartClick: () -> Unit,
    onSellClick: () -> Unit,
    onToggleSave: (SavedItemData) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf(
        "All", "Electronics", "Fashion", "Food", "Books", "Beauty", "Accessories", "Services", "Household"
    )

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { prod ->
            val matchesSearch = searchQuery.isEmpty() ||
                    prod.name.contains(searchQuery, ignoreCase = true) ||
                    prod.description.contains(searchQuery, ignoreCase = true) ||
                    prod.seller_name.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategory == "All" || prod.category.equals(selectedCategory, ignoreCase = true)

            matchesSearch && matchesCategory
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
                        text = "Marketplace",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Sell Button
                        Button(
                            onClick = onSellClick,
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("marketplace_sell_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sell", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Cart Button with badge
                        IconButton(
                            onClick = onCartClick,
                            modifier = Modifier.testTag("marketplace_cart_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (cartItemCount > 0) {
                                        Badge(containerColor = RoyalBlue) {
                                            Text("$cartItemCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = Slate700
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search products...", fontSize = 14.sp, color = Slate400) },
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
                        .testTag("marketplace_search_input"),
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

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 2.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category,
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
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (filteredProducts.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Storefront,
                title = "No products found",
                description = "Be the first student seller to list an item in this category!",
                actionLabel = "Sell an Item",
                onActionClick = onSellClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate50)
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductGridCard(
                        product = product,
                        isSaved = savedProductIds.contains(product.id),
                        onProductClick = { onProductClick(product) },
                        onToggleSave = {
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
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductGridCard(
    product: MarketplaceProduct,
    isSaved: Boolean,
    onProductClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onProductClick() }
            .testTag("product_card_${product.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Slate100)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (product.category.lowercase()) {
                        "electronics" -> Icons.Default.Headphones
                        "fashion" -> Icons.Default.Checkroom
                        "food" -> Icons.Default.Fastfood
                        "books" -> Icons.Default.MenuBook
                        "beauty" -> Icons.Default.Spa
                        else -> Icons.Default.ShoppingBag
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(50.dp)
                    )
                }

                // Favorite Heart Button
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                        .testTag("save_product_${product.id}")
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save Product",
                        tint = if (isSaved) RoseOccupied else Slate500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "KSh ${"%,d".format(product.price)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = RoyalBlue
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Sold by ${product.seller_name.ifEmpty { "Residence Seller" }}",
                    fontSize = 11.sp,
                    color = Slate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                StatusBadge(status = product.status)
            }
        }
    }
}

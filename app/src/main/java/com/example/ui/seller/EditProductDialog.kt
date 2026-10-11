package com.example.ui.seller

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MarketplaceProduct
import com.example.ui.components.SafeImage
import com.example.ui.theme.*
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: MarketplaceProduct,
    onDismiss: () -> Unit,
    onSave: (MarketplaceProduct) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(product.name) }
    var category by remember { mutableStateOf(product.category) }
    var subcategory by remember { mutableStateOf(product.subcategory) }
    var description by remember { mutableStateOf(product.description) }
    var priceText by remember { mutableStateOf(product.price.toString()) }
    var originalPriceText by remember { mutableStateOf(if (product.original_price > 0L) product.original_price.toString() else "") }
    var quantityText by remember { mutableStateOf(product.quantity.toString()) }
    var condition by remember { mutableStateOf(product.condition) }
    var brand by remember { mutableStateOf(product.brand) }
    var model by remember { mutableStateOf(product.model) }
    var color by remember { mutableStateOf(product.color) }
    var size by remember { mutableStateOf(product.size) }
    var material by remember { mutableStateOf(product.material) }
    var additionalSpecs by remember { mutableStateOf(product.additional_specs) }
    var location by remember { mutableStateOf(product.location) }
    var deliveryOption by remember { mutableStateOf(product.delivery_option) }
    val imageList = remember {
        mutableStateListOf<String>().apply {
            if (product.image_urls.isNotEmpty()) addAll(product.image_urls)
            else if (product.image_url.isNotEmpty()) add(product.image_url)
        }
    }
    var isSubmitting by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap != null) {
                    val maxDim = 500
                    val scale = if (originalBitmap.width > maxDim || originalBitmap.height > maxDim) {
                        maxDim.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
                    } else 1.0f

                    val scaled = Bitmap.createScaledBitmap(
                        originalBitmap,
                        (originalBitmap.width * scale).toInt(),
                        (originalBitmap.height * scale).toInt(),
                        true
                    )
                    val baos = ByteArrayOutputStream()
                    scaled.compress(Bitmap.CompressFormat.JPEG, 60, baos)
                    val base64 = "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                    if (imageList.size < 8) {
                        imageList.add(base64)
                    }
                }
            } catch (e: Exception) {
                validationError = "Failed to load image: ${e.message}"
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = RoyalBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Product", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (validationError != null) {
                    Surface(
                        color = RoseLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = validationError ?: "",
                            color = RoseOccupied,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Product Images
                Text("Product Images * (Up to 8 images, first is cover)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    itemsIndexed(imageList) { index, imgStr ->
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(if (index == 0) 2.dp else 1.dp, if (index == 0) RoyalBlue else Slate200, RoundedCornerShape(12.dp))
                        ) {
                            SafeImage(
                                model = imgStr,
                                contentDescription = "Product image $index",
                                modifier = Modifier.fillMaxSize()
                            )
                            if (index == 0) {
                                Surface(
                                    color = RoyalBlue,
                                    shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 8.dp),
                                    modifier = Modifier.align(Alignment.TopStart)
                                ) {
                                    Text("Cover", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                            IconButton(
                                onClick = { imageList.removeAt(index) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    if (imageList.size < 8) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(2.dp, Slate200, RoundedCornerShape(12.dp))
                                    .background(Slate50)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(24.dp))
                                    Text("+ Image", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Basic Info
                Text("Basic Information", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_product_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Category *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    val categories = listOf("Electronics", "Fashion", "Food", "Books", "Furniture", "Health", "Other")
                    items(categories.size) { idx ->
                        val cat = categories[idx]
                        FilterChip(
                            selected = category.equals(cat, ignoreCase = true),
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detailed Description *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("edit_product_desc_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Pricing & Inventory
                Text("Pricing & Inventory", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Selling Price (KSh) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("edit_product_price_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = originalPriceText,
                        onValueChange = { originalPriceText = it },
                        label = { Text("Original Price (KSh)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Available Quantity *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = condition,
                        onValueChange = { condition = it },
                        label = { Text("Condition (New/Used)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pickup & Delivery
                Text("Pickup & Delivery", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Pickup Location *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deliveryOption,
                    onValueChange = { deliveryOption = it },
                    label = { Text("Delivery Option") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        val price = priceText.toLongOrNull() ?: 0L
                        val origPrice = originalPriceText.toLongOrNull() ?: 0L
                        val qty = quantityText.toLongOrNull() ?: 1L

                        if (name.isBlank() || description.isBlank() || price <= 0L || location.isBlank()) {
                            validationError = "Please fill in all required fields (Name, Category, Description, Price, Pickup Location)."
                            return@Button
                        }
                        if (imageList.isEmpty()) {
                            validationError = "Please keep at least one product image."
                            return@Button
                        }

                        isSubmitting = true
                        validationError = null

                        val updated = product.copy(
                            name = name.trim(),
                            category = category,
                            subcategory = subcategory.trim(),
                            description = description.trim(),
                            price = price,
                            original_price = origPrice,
                            quantity = qty,
                            condition = condition,
                            brand = brand.trim(),
                            model = model.trim(),
                            color = color.trim(),
                            size = size.trim(),
                            material = material.trim(),
                            additional_specs = additionalSpecs.trim(),
                            location = location.trim(),
                            delivery_option = deliveryOption.trim(),
                            image_url = imageList.firstOrNull() ?: "",
                            image_urls = imageList.toList()
                        )
                        onSave(updated)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_edit_product_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

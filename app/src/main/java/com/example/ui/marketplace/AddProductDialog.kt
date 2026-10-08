package com.example.ui.marketplace

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import java.io.ByteArrayOutputStream

@Composable
fun AddProductDialog(
    profile: UserProfile?,
    onDismiss: () -> Unit,
    onSubmit: (MarketplaceProduct) -> Unit
) {
    val context = LocalContext.current

    // Required fields
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Electronics") }
    var subcategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var originalPriceText by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var condition by remember { mutableStateOf("New") }
    var location by remember { mutableStateOf("Capital Home Residence") }
    var deliveryOption by remember { mutableStateOf("Pickup at Capital Home Residence") }

    // Specifications
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var material by remember { mutableStateOf("") }
    var additionalSpecs by remember { mutableStateOf("") }

    // Seller Payment info (Pre-filled from approved seller profile, no need to re-enter each time)
    val paymentMethod = remember { "Send Money" }
    val paymentNumber = remember { profile?.phone ?: "" }
    val paymentName = remember { profile?.full_name ?: "" }

    // Images
    val imageList = remember { mutableStateListOf<String>() }
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
                    val maxDim = 800
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
                    scaled.compress(Bitmap.CompressFormat.JPEG, 75, baos)
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
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = RoyalBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Product", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Surface(
                    color = EmeraldLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 14.dp)
                ) {
                    Text(
                        text = "Approved Seller Mode: Once submitted, your product goes LIVE immediately without waiting for admin approval.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldAvailable,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                if (validationError != null) {
                    Surface(
                        color = RoseLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
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

                // 1. PRODUCT IMAGES (At least 1 required)
                Text("Product Images * (Up to 8 images, first is cover)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    itemsIndexed(imageList) { index, imgStr ->
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(if (index == 0) 2.dp else 1.dp, if (index == 0) RoyalBlue else Slate200, RoundedCornerShape(12.dp))
                        ) {
                            val bmp = remember(imgStr) {
                                try {
                                    val pure = imgStr.substringAfter(",")
                                    val bytes = Base64.decode(pure, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                } catch (_: Exception) { null }
                            }
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Thumb",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
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
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(2.dp, Slate200, RoundedCornerShape(12.dp))
                                    .background(Slate50)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .testTag("add_product_image_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(26.dp))
                                    Text("+ Image", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. PRODUCT BASICS
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_product_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category & Subcategory
                Text("Category *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                val categories = listOf("Electronics", "Phones", "Fashion", "Shoes", "Food", "Books", "Furniture", "Household")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)
                ) {
                    items(categories.size) { i ->
                        val cat = categories[i]
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = subcategory,
                    onValueChange = { subcategory = it },
                    label = { Text("Subcategory (e.g. Laptops, Sneakers, Snacks)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Product Description *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("add_product_desc_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. PRICING & INVENTORY
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Selling Price (KSh) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("add_product_price_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = originalPriceText,
                        onValueChange = { originalPriceText = it },
                        label = { Text("Original Price") },
                        placeholder = { Text("For discount %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Available Quantity *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Condition *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                        val conditions = listOf("New", "Like New", "Used", "Refurbished")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            conditions.take(2).forEach { cond ->
                                FilterChip(
                                    selected = condition == cond,
                                    onClick = { condition = cond },
                                    label = { Text(cond, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. STRUCTURED SPECIFICATIONS
                Text("Specifications (Optional but recommended)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand (e.g. Apple, Nike)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model / Series") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Color") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text("Size / Dimensions") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = material,
                    onValueChange = { material = it },
                    label = { Text("Material (e.g. Cotton, Leather, Aluminum)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = additionalSpecs,
                    onValueChange = { additionalSpecs = it },
                    label = { Text("Additional Specs (RAM, Storage, Warranty, etc.)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 5. PICKUP & DELIVERY
                Text("Pickup & Delivery", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Pickup Location *") },
                    placeholder = { Text("e.g. Capital Home Residence, Block B, 2nd Floor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deliveryOption,
                    onValueChange = { deliveryOption = it },
                    label = { Text("Delivery Option") },
                    placeholder = { Text("e.g. Pickup at Residence or Seller-Arranged Delivery") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // SUBMIT BUTTON
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
                            validationError = "Please add at least one product image."
                            return@Button
                        }

                        isSubmitting = true
                        validationError = null

                        val prod = MarketplaceProduct(
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
                            image_urls = imageList.toList(),
                            seller_name = profile?.full_name ?: "Approved Resident Seller",
                            seller_phone = profile?.phone ?: paymentNumber,
                            payment_method = paymentMethod,
                            payment_number = paymentNumber,
                            payment_name = paymentName,
                            status = "Active" // Immediately Active for approved sellers!
                        )
                        onSubmit(prod)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_product_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Publish Product Immediately", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

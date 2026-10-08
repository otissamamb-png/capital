package com.example.ui.marketplace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MarketplaceProduct
import com.example.data.model.UserProfile
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900

@Composable
fun AddProductDialog(
    profile: UserProfile?,
    onDismiss: () -> Unit,
    onSubmit: (MarketplaceProduct) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Electronics") }
    var condition by remember { mutableStateOf("New") }
    var location by remember { mutableStateOf("Capital Home Residence") }
    var paymentMethod by remember { mutableStateOf("Send Money") }
    var paymentNumber by remember { mutableStateOf(profile?.phone ?: "") }
    var paymentName by remember { mutableStateOf(profile?.full_name ?: "") }
    var isSubmitting by remember { mutableStateOf(false) }

    val categories = listOf("Electronics", "Fashion", "Food", "Books", "Beauty", "Accessories", "Services", "Household")
    val paymentMethods = listOf("Send Money", "Lipa na M-Pesa Pochi", "Lipa na M-Pesa Till", "Lipa M-Pesa Pay Bill")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = RoyalBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("List New Product", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Publish your item to the Capital Home marketplace.",
                    fontSize = 12.sp,
                    color = Slate500,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_product_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price (KSh)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_product_price_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Payment Method", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    paymentMethods.take(2).forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = paymentNumber,
                    onValueChange = { paymentNumber = it },
                    label = { Text("M-Pesa Number / Till") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = paymentName,
                    onValueChange = { paymentName = it },
                    label = { Text("Account / Owner Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val price = priceText.toLongOrNull() ?: 0L
                        if (name.isNotBlank() && price > 0L) {
                            isSubmitting = true
                            val p = MarketplaceProduct(
                                name = name,
                                description = description,
                                price = price,
                                category = category,
                                condition = condition,
                                location = location,
                                seller_name = profile?.full_name ?: "Resident Seller",
                                seller_phone = profile?.phone ?: paymentNumber,
                                payment_method = paymentMethod,
                                payment_number = paymentNumber,
                                payment_name = paymentName,
                                status = "Active"
                            )
                            onSubmit(p)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting && name.isNotBlank() && priceText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_product_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Text("List Product for Sale", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

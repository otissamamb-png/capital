package com.example.ui.marketplace

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderRecord
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    order: OrderRecord,
    onSubmitReference: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val clipboardManager = LocalClipboardManager.current
    var mpesaCode by remember { mutableStateOf(order.mpesa_reference) }
    var copiedNotice by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Your Payment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("payment_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Unfinished order alert banner
            Surface(
                color = AmberLight,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = AmberPending)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Unfinished Payment Restored",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Slate900
                        )
                        Text(
                            text = "Order #${order.order_number} is reserved. Complete payment below without restarting.",
                            fontSize = 11.sp,
                            color = Slate700
                        )
                    }
                }
            }

            // Order Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("payment_order_summary")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Order #${order.order_number}", fontWeight = FontWeight.Bold, color = RoyalBlue, fontSize = 14.sp)
                        Surface(
                            color = when (order.payment_status) {
                                "Payment Confirmed" -> EmeraldLight
                                "Reference Submitted" -> BlueLight
                                "Payment Rejected" -> RoseLight
                                else -> AmberLight
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = order.payment_status.uppercase(),
                                color = when (order.payment_status) {
                                    "Payment Confirmed" -> EmeraldAvailable
                                    "Reference Submitted" -> RoyalBlue
                                    "Payment Rejected" -> RoseOccupied
                                    else -> AmberPending
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = order.product_name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text(text = "Qty: ${order.quantity} • Sold by ${order.seller_name}", fontSize = 12.sp, color = Slate500)
                    Text(text = "Delivery/Pickup: ${order.pickup_or_delivery}", fontSize = 12.sp, color = Slate600)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate100)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Amount to Pay", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                        Text(
                            text = "KSh ${"%,d".format(order.total_amount)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = RoyalBlue
                        )
                    }
                }
            }

            // Seller M-Pesa Details Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("seller_payment_details_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Seller's M-Pesa Payment Details",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Pay directly to the seller using the verified details below:",
                        fontSize = 12.sp,
                        color = Slate500,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Surface(
                        color = Slate50,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Payment Method:", fontSize = 12.sp, color = Slate500)
                                Text(order.payment_method, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Pay to Number / Till:", fontSize = 12.sp, color = Slate500)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        order.seller_payment_number.ifEmpty { "0712345678" },
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = RoyalBlue
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(order.seller_payment_number.ifEmpty { "0712345678" }))
                                            copiedNotice = true
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = RoyalBlue, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            if (order.seller_payment_name.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Account / Recipient:", fontSize = 12.sp, color = Slate500)
                                    Text(order.seller_payment_name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                                }
                            }
                        }
                    }

                    if (copiedNotice) {
                        Text(
                            text = "Copied M-Pesa number to clipboard!",
                            color = EmeraldAvailable,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            // Step-by-Step Instructions
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlueLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("How to Complete Manual Payment", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RoyalBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    val steps = listOf(
                        "1. Temporarily minimize Capital Home Residence.",
                        "2. Open M-Pesa on your mobile phone SIM menu / App.",
                        "3. Select ${order.payment_method} & enter the number above.",
                        "4. Send exactly KSh ${"%,d".format(order.total_amount)}.",
                        "5. Return to this screen & enter the transaction code below."
                    )
                    steps.forEach { step ->
                        Text(text = step, fontSize = 12.sp, color = Slate700, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            // Reference input section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "M-Pesa Transaction Code",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Enter the 10-character code received from M-Pesa (e.g., QK8912PXYZ).",
                        fontSize = 11.sp,
                        color = Slate500,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    OutlinedTextField(
                        value = mpesaCode,
                        onValueChange = { mpesaCode = it.uppercase() },
                        placeholder = { Text("e.g. QK8912PXYZ") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mpesa_reference_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg ?: "",
                            color = RoseOccupied,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (mpesaCode.isBlank()) {
                                errorMsg = "Please enter your M-Pesa transaction code."
                                return@Button
                            }
                            isSubmitting = true
                            errorMsg = null
                            onSubmitReference(mpesaCode.trim().uppercase())
                            isSubmitting = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSubmitting && mpesaCode.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_payment_reference_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Submit Payment Reference", fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Submitting updates the status to 'Reference Submitted'. The seller will manually verify the funds in M-Pesa.",
                        fontSize = 11.sp,
                        color = Slate500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        }
    }
}

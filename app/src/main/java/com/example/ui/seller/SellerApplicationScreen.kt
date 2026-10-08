package com.example.ui.seller

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SellerApplicationData
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerApplicationScreen(
    profile: UserProfile?,
    existingApplication: SellerApplicationData?,
    onSubmitApplication: (SellerApplicationData) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var step by remember { mutableStateOf(1) } // 1: Info, 2: Payment, 3: Review, 4: Success
    var businessName by remember { mutableStateOf(existingApplication?.business_name ?: "") }
    var category by remember { mutableStateOf(existingApplication?.category ?: "Electronics") }
    var description by remember { mutableStateOf(existingApplication?.business_description ?: "") }
    var paymentMethod by remember { mutableStateOf(existingApplication?.payment_method ?: "Send Money") }
    var paymentNumber by remember { mutableStateOf(existingApplication?.payment_number ?: (profile?.phone ?: "")) }
    var paymentName by remember { mutableStateOf(existingApplication?.payment_name ?: (profile?.full_name ?: "")) }
    var isSubmitted by remember { mutableStateOf(existingApplication != null && existingApplication.status == "Pending") }

    val categories = listOf("Electronics", "Fashion", "Food", "Books", "Beauty", "General")
    val paymentMethods = listOf("Send Money", "Lipa na M-Pesa Pochi", "Lipa na M-Pesa Till", "Lipa M-Pesa Pay Bill")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Become a Seller", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("seller_app_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (isSubmitted) {
            // Application Submitted Success Screen (Matching UI showcase)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(BlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Application Submitted",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your application is now under review by residence administrators. We'll notify you once there's an update.",
                    fontSize = 14.sp,
                    color = Slate500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("seller_success_back_home")
                ) {
                    Text("Back to Home", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate50)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = "Sell your products to the Capital Home Residence community.",
                    fontSize = 13.sp,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Stepper Header (1. Information, 2. Payment, 3. Review)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepIndicator(stepNumber = 1, title = "Information", isActive = step == 1, isDone = step > 1)
                    HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (step > 1) RoyalBlue else Slate200)
                    StepIndicator(stepNumber = 2, title = "Payment", isActive = step == 2, isDone = step > 2)
                    HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (step > 2) RoyalBlue else Slate200)
                    StepIndicator(stepNumber = 3, title = "Review", isActive = step == 3, isDone = false)
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (step) {
                    1 -> {
                        // Step 1: Business Information
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text("Business Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = businessName,
                                    onValueChange = { businessName = it },
                                    label = { Text("Business / Shop Name") },
                                    placeholder = { Text("e.g. Brian's Tech Store") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("seller_business_name_input")
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text("Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
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

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("Business Description") },
                                    placeholder = { Text("Tell us about your products or services...") },
                                    minLines = 3,
                                    modifier = Modifier.fillMaxWidth().testTag("seller_description_input")
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = { step = 2 },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = businessName.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("seller_step1_next")
                                ) {
                                    Text("Next: Payment Details", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    2 -> {
                        // Step 2: Payment Details
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text("Payment Methods", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Specify how students will pay you for items.",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                Text("Preferred M-Pesa Method", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                paymentMethods.forEach { method ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        RadioButton(
                                            selected = paymentMethod == method,
                                            onClick = { paymentMethod = method }
                                        )
                                        Text(text = method, fontSize = 13.sp, color = Slate800)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = paymentNumber,
                                    onValueChange = { paymentNumber = it },
                                    label = { Text("M-Pesa Number / Till / Pay Bill") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("seller_payment_number_input")
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = paymentName,
                                    onValueChange = { paymentName = it },
                                    label = { Text("Account / Registered Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("seller_payment_name_input")
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { step = 1 },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Text("Back")
                                    }
                                    Button(
                                        onClick = { step = 3 },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = paymentNumber.isNotBlank(),
                                        modifier = Modifier.weight(1f).height(48.dp).testTag("seller_step2_next")
                                    ) {
                                        Text("Next: Review", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        // Step 3: Review and Submit
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text("Review Application", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                Spacer(modifier = Modifier.height(14.dp))

                                SummaryItem("Business Name", businessName)
                                SummaryItem("Category", category)
                                SummaryItem("Description", description)
                                SummaryItem("Payment Method", paymentMethod)
                                SummaryItem("Payment Number", paymentNumber)
                                SummaryItem("Account Name", paymentName)

                                Spacer(modifier = Modifier.height(24.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { step = 2 },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Text("Back")
                                    }
                                    Button(
                                        onClick = {
                                            val app = SellerApplicationData(
                                                seller_name = profile?.full_name ?: paymentName,
                                                phone = profile?.phone ?: paymentNumber,
                                                email = profile?.email ?: "",
                                                business_name = businessName,
                                                business_description = description,
                                                category = category,
                                                payment_method = paymentMethod,
                                                payment_number = paymentNumber,
                                                payment_name = paymentName,
                                                status = "Pending"
                                            )
                                            onSubmitApplication(app)
                                            isSubmitted = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1.2f).height(48.dp).testTag("seller_submit_button")
                                    ) {
                                        Text("Submit Application", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicator(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isDone: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isActive || isDone) RoyalBlue else Slate200),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$stepNumber",
                color = if (isActive || isDone) Color.White else Slate600,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            color = if (isActive) RoyalBlue else Slate500,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, color = Slate500)
        Text(text = value.ifEmpty { "—" }, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
    }
}

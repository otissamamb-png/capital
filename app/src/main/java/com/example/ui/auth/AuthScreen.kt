package com.example.ui.auth

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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.ResidenceRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { ResidenceRepository.create(context) }

    // Navigation state inside Auth: "welcome", "login", "signup", "admin_login"
    var authMode by remember { mutableStateOf("welcome") }

    // Login Form State
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Admin Login Form State
    var adminEmail by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var adminPasswordVisible by remember { mutableStateOf(false) }

    // Signup Multi-Step Form State
    var signupStep by remember { mutableStateOf(1) } // 1: Personal, 2: Residence, 3: Room Image
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Residence info
    var roomNumber by remember { mutableStateOf("") }
    var roomType by remember { mutableStateOf("New Room") } // "Old Room" or "New Room"
    var roomStatus by remember { mutableStateOf("Occupied") } // "Occupied" or "Vacant"

    // Image upload state
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBase64String by remember { mutableStateOf("") }
    var imageBitmapPreview by remember { mutableStateOf<Bitmap?>(null) }

    // General UI states
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap != null) {
                    val maxDimension = 800
                    val width = originalBitmap.width
                    val height = originalBitmap.height
                    val scale = if (width > maxDimension || height > maxDimension) {
                        maxDimension.toFloat() / maxOf(width, height)
                    } else 1.0f

                    val scaledBitmap = Bitmap.createScaledBitmap(
                        originalBitmap,
                        (width * scale).toInt(),
                        (height * scale).toInt(),
                        true
                    )
                    imageBitmapPreview = scaledBitmap

                    val outputStream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                    val bytes = outputStream.toByteArray()
                    imageBase64String = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
            } catch (e: Exception) {
                errorMessage = "Failed to process image: ${e.message}"
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Branding Header
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(NavyDark),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_capital_home_logo_1791459621552),
                    contentDescription = "Capital Home Residence Logo",
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CAPITAL HOME",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = NavyDark,
                letterSpacing = 1.sp
            )
            Text(
                text = "RESIDENCE",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalBlue,
                letterSpacing = 2.sp
            )

            Surface(
                color = BlueLight,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Text(
                    text = "Live. Connect. Buy. Sell.",
                    color = RoyalBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error Message Box
            if (errorMessage != null) {
                Surface(
                    color = RoseLight,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RoseOccupied, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = RoseOccupied,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            when (authMode) {
                // ==========================================
                // 1. WELCOME SCREEN
                // ==========================================
                "welcome" -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.hero_residence_banner_1791459672502),
                                contentDescription = "Residence",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Welcome",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Join the Capital Home Residence community to manage rooms, buy & sell items, and connect with fellow residents.",
                        fontSize = 14.sp,
                        color = Slate500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = {
                            errorMessage = null
                            authMode = "signup"
                            signupStep = 1
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("welcome_get_started_button")
                    ) {
                        Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            errorMessage = null
                            authMode = "login"
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("welcome_have_account_button")
                    ) {
                        Text("I already have an account", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = RoyalBlue)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Discreet Admin Portal Link
                    TextButton(
                        onClick = {
                            errorMessage = null
                            authMode = "admin_login"
                        },
                        modifier = Modifier.testTag("admin_portal_link")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Administrator Portal", color = Slate500, fontSize = 12.sp)
                        }
                    }
                }

                // ==========================================
                // 2. NORMAL RESIDENT LOGIN
                // ==========================================
                "login" -> {
                    Text(
                        text = "Welcome Back",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Sign in to your Capital Home account",
                        fontSize = 13.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = loginEmail,
                        onValueChange = { loginEmail = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Slate400) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Slate400) },
                        trailingIcon = {
                            IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                Icon(
                                    imageVector = if (loginPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Slate400
                                )
                            }
                        },
                        visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (loginEmail.isBlank() || loginPassword.isBlank()) {
                                errorMessage = "Please enter both email and password."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                val result = repository.signInWithEmailPassword(loginEmail, loginPassword)
                                isLoading = false
                                if (result.isSuccess) {
                                    onAuthSuccess()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Invalid login credentials."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Don't have an account?", fontSize = 13.sp, color = Slate500)
                        TextButton(onClick = {
                            errorMessage = null
                            authMode = "signup"
                            signupStep = 1
                        }) {
                            Text("Create Account", color = RoyalBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(onClick = { authMode = "welcome" }) {
                        Text("Back to Welcome", color = Slate500, fontSize = 12.sp)
                    }
                }

                // ==========================================
                // 3. MULTI-STEP SIGNUP WITH AUTOMATIC ROOM
                // ==========================================
                "signup" -> {
                    // Header with back button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (signupStep > 1) signupStep--
                            else authMode = "welcome"
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = "Create Account",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SignupStepPill(1, "Personal", signupStep == 1, signupStep > 1)
                        HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 6.dp), color = if (signupStep > 1) RoyalBlue else Slate200)
                        SignupStepPill(2, "Residence", signupStep == 2, signupStep > 2)
                        HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 6.dp), color = if (signupStep > 2) RoyalBlue else Slate200)
                        SignupStepPill(3, "Room Photo", signupStep == 3, false)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    when (signupStep) {
                        1 -> {
                            // STEP 1: Personal Information
                            Text(
                                text = "Personal Information",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name") },
                                placeholder = { Text("e.g. Alex Kimani") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_name_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone Number") },
                                placeholder = { Text("+254 712 345 678") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_phone_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_email_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_password_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password") },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_confirm_password_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    if (fullName.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank()) {
                                        errorMessage = "Please fill in all personal details."
                                        return@Button
                                    }
                                    if (password.length < 6) {
                                        errorMessage = "Password must be at least 6 characters."
                                        return@Button
                                    }
                                    if (password != confirmPassword) {
                                        errorMessage = "Passwords do not match."
                                        return@Button
                                    }
                                    errorMessage = null
                                    signupStep = 2
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("signup_step1_continue")
                            ) {
                                Text("Continue: Residence Details", fontWeight = FontWeight.Bold)
                            }
                        }

                        2 -> {
                            // STEP 2: Residence Information
                            Text(
                                text = "Your Residence Information",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Text(
                                text = "Every resident provides their room details to register their residence record.",
                                fontSize = 12.sp,
                                color = Slate500,
                                modifier = Modifier.align(Alignment.Start).padding(top = 2.dp, bottom = 14.dp)
                            )

                            OutlinedTextField(
                                value = roomNumber,
                                onValueChange = { roomNumber = it },
                                label = { Text("Room Number") },
                                placeholder = { Text("e.g. 204 or A12") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_room_number_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Room Type Selection Cards
                            Text("Room Type", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RoomSelectionCard(
                                    title = "Old Room",
                                    price = "KSh 15,000 / sem",
                                    isSelected = roomType == "Old Room",
                                    modifier = Modifier.weight(1f),
                                    onClick = { roomType = "Old Room" }
                                )
                                RoomSelectionCard(
                                    title = "New Room",
                                    price = "KSh 25,000 / sem",
                                    isSelected = roomType == "New Room",
                                    modifier = Modifier.weight(1f),
                                    onClick = { roomType = "New Room" }
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Room Status (Occupied vs Vacant, default Occupied)
                            Text("Room Status", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatusSelectionCard(
                                    emoji = "🏠",
                                    label = "Occupied",
                                    isSelected = roomStatus == "Occupied",
                                    modifier = Modifier.weight(1f),
                                    onClick = { roomStatus = "Occupied" }
                                )
                                StatusSelectionCard(
                                    emoji = "🔑",
                                    label = "Vacant",
                                    isSelected = roomStatus == "Vacant",
                                    modifier = Modifier.weight(1f),
                                    onClick = { roomStatus = "Vacant" }
                                )
                            }

                            Spacer(modifier = Modifier.height(26.dp))

                            Button(
                                onClick = {
                                    if (roomNumber.isBlank()) {
                                        errorMessage = "Please enter your room number."
                                        return@Button
                                    }
                                    isLoading = true
                                    errorMessage = null
                                    coroutineScope.launch {
                                        val isRegistered = repository.isRoomNumberRegistered(roomNumber)
                                        isLoading = false
                                        if (isRegistered) {
                                            errorMessage = "Room already registered. Please verify your room number."
                                        } else {
                                            signupStep = 3
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isLoading && roomNumber.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("signup_step2_continue")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Continue: Room Cover Image", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        3 -> {
                            // STEP 3: Room Cover Image Upload
                            Text(
                                text = "Room Cover Image",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Text(
                                text = "Upload a cover photo for Room $roomNumber at Capital Home Residence.",
                                fontSize = 12.sp,
                                color = Slate500,
                                modifier = Modifier.align(Alignment.Start).padding(top = 2.dp, bottom = 14.dp)
                            )

                            // Image Preview or Upload Box
                            if (imageBitmapPreview != null) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Image(
                                            bitmap = imageBitmapPreview!!.asImageBitmap(),
                                            contentDescription = "Room Cover Preview",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        IconButton(
                                            onClick = {
                                                imageBitmapPreview = null
                                                imageBase64String = ""
                                                selectedImageUri = null
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Replace Room Image")
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(2.dp, Slate200, RoundedCornerShape(16.dp))
                                        .background(Slate50)
                                        .clickable {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                        .testTag("upload_room_image_box"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(44.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("+ Upload Room Image", fontWeight = FontWeight.Bold, color = RoyalBlue, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Select JPG, PNG or WebP from device", color = Slate500, fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Summary Box
                            Surface(
                                color = BlueSurface,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Listing Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalBlue)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Room $roomNumber ($roomType) • Status: $roomStatus", fontSize = 12.sp, color = Slate700)
                                    Text(
                                        text = if (roomType == "New Room") "KSh 25,000 / semester" else "KSh 15,000 / semester",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate900
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    isLoading = true
                                    errorMessage = null
                                    coroutineScope.launch {
                                        val result = repository.registerResidentWithRoom(
                                            fullName = fullName,
                                            phone = phone,
                                            email = email,
                                            password = password,
                                            roomNumber = roomNumber,
                                            roomType = roomType,
                                            roomStatus = roomStatus,
                                            coverImageUrl = imageBase64String
                                        )
                                        isLoading = false
                                        if (result.isSuccess) {
                                            onAuthSuccess()
                                        } else {
                                            errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Registration failed."
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("signup_create_account_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Create Account & Register Room", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = "Your room information will be used to automatically create your residence listing.",
                                fontSize = 11.sp,
                                color = Slate500,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    }
                }

                // ==========================================
                // 4. SECURE ADMINISTRATOR LOGIN PORTAL
                // ==========================================
                "admin_login" -> {
                    Surface(
                        color = NavyDark,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Administrator Portal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Secure administrative access only", color = Slate400, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = adminEmail,
                        onValueChange = { adminEmail = it },
                        label = { Text("Admin Email") },
                        placeholder = { Text("okindatechhub@gmail.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_email_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = { adminPassword = it },
                        label = { Text("Admin Password") },
                        visualTransformation = if (adminPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { adminPasswordVisible = !adminPasswordVisible }) {
                                Icon(if (adminPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (adminEmail.isBlank() || adminPassword.isBlank()) {
                                errorMessage = "Please enter administrator credentials."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                val result = repository.signInAdmin(adminEmail, adminPassword)
                                isLoading = false
                                if (result.isSuccess) {
                                    onAuthSuccess()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Administrative authentication failed."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("admin_login_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Authenticate Administrator", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = {
                        errorMessage = null
                        authMode = "welcome"
                    }) {
                        Text("Return to Student Portal", color = Slate500, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SignupStepPill(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
    isDone: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (isActive || isDone) RoyalBlue else Slate200),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$stepNumber",
                color = if (isActive || isDone) Color.White else Slate600,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isActive) RoyalBlue else Slate500,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun RoomSelectionCard(
    title: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) BlueLight else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) RoyalBlue else Slate200),
        modifier = modifier
            .clickable { onClick() }
            .testTag("room_type_$title")
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isSelected) RoyalBlue else Slate900)
            Spacer(modifier = Modifier.height(2.dp))
            Text(price, fontSize = 11.sp, color = Slate600)
        }
    }
}

@Composable
fun StatusSelectionCard(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) (if (label == "Vacant") EmeraldLight else BlueLight) else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) (if (label == "Vacant") EmeraldAvailable else RoyalBlue) else Slate200),
        modifier = modifier
            .clickable { onClick() }
            .testTag("status_selection_$label")
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSelected) (if (label == "Vacant") EmeraldAvailable else RoyalBlue) else Slate800
            )
        }
    }
}

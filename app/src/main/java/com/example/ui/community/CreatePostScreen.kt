package com.example.ui.community

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommunityPost
import com.example.data.model.UserProfile
import com.example.ui.components.SafeImage
import com.example.ui.theme.*
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    profile: UserProfile?,
    onSubmitPost: (CommunityPost) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var content by remember { mutableStateOf("") }
    var postType by remember { mutableStateOf("General") }
    var isSubmitting by remember { mutableStateOf(false) }
    var attachedBase64Image by remember { mutableStateOf("") }
    var attachedBase64Video by remember { mutableStateOf("") }
    var videoLabel by remember { mutableStateOf("") }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val words = content.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val wordCount = words.size
    val isWordCountExceeded = wordCount > 100

    val postImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null) {
                    if (bytes.size > 5 * 1024 * 1024) {
                        validationError = "Image size exceeds 5MB limit (${bytes.size / (1024*1024)}MB)."
                    } else {
                        validationError = null
                        val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
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
                            previewBitmap = scaled
                            val baos = ByteArrayOutputStream()
                            scaled.compress(Bitmap.CompressFormat.JPEG, 75, baos)
                            attachedBase64Image = "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                        }
                    }
                }
            } catch (e: Exception) {
                validationError = "Failed to load image: ${e.message}"
            }
        }
    }

    val postVideoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null) {
                    if (bytes.size > 5 * 1024 * 1024) {
                        validationError = "Video size exceeds 5MB limit (${bytes.size / (1024*1024)}MB)."
                    } else {
                        validationError = null
                        attachedBase64Video = "data:video/mp4;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                        videoLabel = "Video Attached (${bytes.size / 1024} KB)"
                    }
                }
            } catch (e: Exception) {
                validationError = "Failed to load video: ${e.message}"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("create_post_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            if (content.isNotBlank() && !isWordCountExceeded) {
                                isSubmitting = true
                                val post = CommunityPost(
                                    author_id = profile?.auth_user_id?.ifBlank { profile.id } ?: profile?.id ?: "",
                                    author_name = profile?.full_name ?: "Alex Kimani",
                                    author_photo = profile?.profile_photo ?: "",
                                    content = content.trim(),
                                    post_type = postType,
                                    image_url = attachedBase64Image,
                                    video_url = attachedBase64Video
                                )
                                onSubmitPost(post)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSubmitting && content.isNotBlank() && !isWordCountExceeded,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("post_submit_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Post", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                .background(Color.White)
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            // Author info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(RoyalBlue),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile?.profile_photo?.isNotBlank() == true) {
                        SafeImage(
                            model = profile.profile_photo,
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            fallbackIcon = Icons.Default.Person,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = (profile?.full_name?.take(1) ?: "A").uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = profile?.full_name ?: "Alex Kimani",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Posting to Residence Community",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (validationError != null) {
                Surface(
                    color = RoseLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
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

            // Attached image preview if selected
            if (previewBitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate100)
                ) {
                    Image(
                        bitmap = previewBitmap!!.asImageBitmap(),
                        contentDescription = "Attached post image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = {
                            previewBitmap = null
                            attachedBase64Image = ""
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(30.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Remove attached photo", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Attached video preview if selected
            if (attachedBase64Video.isNotEmpty()) {
                Surface(
                    color = Color(0xFFF3E8FF),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color(0xFF7E22CE), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = videoLabel.ifEmpty { "Video attached (Max 5MB)" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7E22CE),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                attachedBase64Video = ""
                                videoLabel = ""
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Remove video", tint = Color(0xFF7E22CE), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Post Text Area Container with 2px solid black border (Mandated by user prompt)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = {
                        Text("Share something with the residence (Maximum 100 words)...", color = Slate400, fontSize = 14.sp)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("post_content_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            // Word Counter (Maximum of 100 words mandated by user prompt)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isWordCountExceeded) {
                    Text(
                        text = "Exceeded limit: $wordCount/100 words (Max 100 words)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoseOccupied
                    )
                } else {
                    Text(
                        text = "$wordCount / 100 words",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (wordCount > 85) AmberPending else Slate500
                    )
                }

                Text(
                    text = "Max 100 words",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }

            HorizontalDivider(color = Slate100)

            Spacer(modifier = Modifier.height(10.dp))

            // Post type tags & Attachment buttons (Image and Video up to 5MB)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        postImagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (attachedBase64Image.isNotEmpty()) EmeraldLight else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f).testTag("post_attach_photo_button")
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = if (attachedBase64Image.isNotEmpty()) EmeraldAvailable else Slate600, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (attachedBase64Image.isNotEmpty()) "Photo (5MB)" else "+ Photo", fontSize = 11.sp, color = if (attachedBase64Image.isNotEmpty()) EmeraldAvailable else Slate700)
                }

                OutlinedButton(
                    onClick = {
                        postVideoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (attachedBase64Video.isNotEmpty()) Color(0xFFF3E8FF) else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f).testTag("post_attach_video_button")
                ) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = if (attachedBase64Video.isNotEmpty()) Color(0xFF7E22CE) else Slate600, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (attachedBase64Video.isNotEmpty()) "Video (5MB)" else "+ Video", fontSize = 11.sp, color = if (attachedBase64Video.isNotEmpty()) Color(0xFF7E22CE) else Slate700)
                }

                OutlinedButton(
                    onClick = { postType = if (postType == "Announcement") "General" else "Announcement" },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (postType == "Announcement") BlueLight else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Announce", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

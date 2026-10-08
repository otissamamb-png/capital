package com.example.ui.community

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommunityPost
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    profile: UserProfile?,
    onSubmitPost: (CommunityPost) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var content by remember { mutableStateOf("") }
    var postType by remember { mutableStateOf("General") }
    var isSubmitting by remember { mutableStateOf(false) }

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
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            if (content.isNotBlank()) {
                                isSubmitting = true
                                val post = CommunityPost(
                                    author_name = profile?.full_name ?: "Alex Kimani",
                                    content = content,
                                    post_type = postType,
                                    image_url = ""
                                )
                                onSubmitPost(post)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSubmitting && content.isNotBlank(),
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
                    Text(
                        text = (profile?.full_name?.take(1) ?: "A").uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
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

            Spacer(modifier = Modifier.height(18.dp))

            // Post Text Area
            OutlinedTextField(
                value = content,
                onValueChange = {
                    if (it.length <= 500) content = it
                },
                placeholder = {
                    Text("Share something with the residence...", color = Slate400, fontSize = 15.sp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("post_content_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // Character Counter
            Text(
                text = "${content.length}/500",
                fontSize = 12.sp,
                color = Slate400,
                modifier = Modifier.align(Alignment.End).padding(vertical = 4.dp)
            )

            HorizontalDivider(color = Slate100)

            Spacer(modifier = Modifier.height(12.dp))

            // Post type tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { postType = if (postType == "Announcement") "General" else "Announcement" },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (postType == "Announcement") BlueLight else Color.Transparent
                    )
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Announcement", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { postType = if (postType == "Question") "General" else "Question" },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (postType == "Question") BlueLight else Color.Transparent
                    )
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Question", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

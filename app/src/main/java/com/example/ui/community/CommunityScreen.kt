package com.example.ui.community

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CommentItem
import com.example.data.model.CommunityPost
import com.example.data.model.UserProfile
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SafeImage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    profile: UserProfile?,
    posts: List<CommunityPost>,
    likedPostIds: Set<String>,
    onCreatePostClick: () -> Unit,
    onToggleLike: (String) -> Unit,
    onOpenComments: (CommunityPost) -> Unit,
    onEditPost: (postId: String, newContent: String, newType: String) -> Unit = { _, _, _ -> },
    onDeletePost: (postId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("All") }
    var editingPost by remember { mutableStateOf<CommunityPost?>(null) }
    var postToDelete by remember { mutableStateOf<CommunityPost?>(null) }

    val filteredPosts = remember(posts, selectedFilter) {
        if (selectedFilter == "All") posts
        else posts.filter { it.post_type.equals(selectedFilter, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate50)
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. "What's happening at Capital Home?" prompt box
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCreatePostClick() }
                        .testTag("community_create_post_prompt")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(RoyalBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (profile?.full_name?.take(1) ?: "A").uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "What's happening at Capital Home Residence?",
                            fontSize = 13.sp,
                            color = Slate500,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onCreatePostClick,
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp).testTag("create_post_button")
                        ) {
                            Text("Create Post", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Filter chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(listOf("All", "Announcements", "Questions", "General", "Marketplace")) { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalBlue,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Slate700
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            // 3. Posts list
            if (filteredPosts.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Forum,
                        title = "No community posts yet",
                        description = "Be the first to share an update, question, or study group invite with the residence!",
                        actionLabel = "Create Post",
                        onActionClick = onCreatePostClick
                    )
                }
            } else {
                items(filteredPosts, key = { it.id }) { post ->
                    val isMyPost = profile != null && (
                        (profile.auth_user_id.isNotBlank() && post.author_id == profile.auth_user_id) ||
                        (profile.id.isNotBlank() && post.author_id == profile.id)
                    )
                    CommunityPostCard(
                        post = post,
                        isLiked = likedPostIds.contains(post.id),
                        isMyPost = isMyPost,
                        onLikeClick = { onToggleLike(post.id) },
                        onCommentClick = { onOpenComments(post) },
                        onShareClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${post.author_name} posted on Capital Home Residence: \"${post.content}\""
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Post"))
                        },
                        onEditClick = { editingPost = post },
                        onDeleteClick = { postToDelete = post }
                    )
                }
            }
        }

        // Edit Post Dialog (Only for the user's own posts)
        if (editingPost != null) {
            var editedContent by remember(editingPost) { mutableStateOf(editingPost!!.content) }
            var editedType by remember(editingPost) { mutableStateOf(editingPost!!.post_type) }
            val categories = listOf("General", "Announcements", "Questions", "Marketplace")

            AlertDialog(
                onDismissRequest = { editingPost = null },
                title = { Text("Edit Your Post", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories) { cat ->
                                val selected = editedType == cat
                                FilterChip(
                                    selected = selected,
                                    onClick = { editedType = cat },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RoyalBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = editedContent,
                            onValueChange = { editedContent = it },
                            label = { Text("Post Content") },
                            placeholder = { Text("What would you like to update?") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp)
                                .testTag("edit_post_content_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editedContent.isNotBlank()) {
                                onEditPost(editingPost!!.id, editedContent, editedType)
                                editingPost = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_edited_post_button")
                    ) {
                        Text("Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingPost = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Post Confirmation Dialog
        if (postToDelete != null) {
            AlertDialog(
                onDismissRequest = { postToDelete = null },
                icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = RoseOccupied, modifier = Modifier.size(32.dp)) },
                title = { Text("Delete Post", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Are you sure you want to permanently delete this post? This action cannot be undone.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeletePost(postToDelete!!.id)
                            postToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseOccupied),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_delete_post_button")
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { postToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityPost,
    isLiked: Boolean,
    isMyPost: Boolean = false,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth().testTag("post_card_${post.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(RoyalBlue),
                    contentAlignment = Alignment.Center
                ) {
                    if (post.author_photo.isNotBlank()) {
                        SafeImage(
                            model = post.author_photo,
                            contentDescription = post.author_name,
                            contentScale = ContentScale.Crop,
                            fallbackIcon = Icons.Default.Person,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = (post.author_name.take(1).ifEmpty { "R" }).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.author_name.ifEmpty { "Resident" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        if (isMyPost) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = BlueLight,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "You",
                                    color = RoyalBlue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Capital Home Resident",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                if (post.post_type != "General") {
                    Surface(
                        color = BlueLight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = post.post_type,
                            color = RoyalBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Show Edit/Delete menu ONLY for user's own posts
                if (isMyPost) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp).testTag("post_options_${post.id}")
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Post options", tint = Slate500, modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Post") },
                                onClick = {
                                    showMenu = false
                                    onEditClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier.testTag("edit_post_item_${post.id}")
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Post", color = RoseOccupied) },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseOccupied, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier.testTag("delete_post_item_${post.id}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Content
            Text(
                text = post.content,
                fontSize = 14.sp,
                color = Slate800,
                lineHeight = 20.sp
            )

            // Optional Image
            if (post.image_url.isNotEmpty() || post.id == "post_faith") {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                ) {
                    SafeImage(
                        model = if (post.image_url.isNotEmpty()) post.image_url else R.drawable.hero_residence_banner_1791459672502,
                        contentDescription = "Post image",
                        contentScale = ContentScale.Crop,
                        fallbackDrawableRes = R.drawable.hero_residence_banner_1791459672502,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Optional Video
            if (post.video_url.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7E22CE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Video Attachment", fontWeight = FontWeight.Bold, color = Color(0xFF7E22CE), fontSize = 13.sp)
                            Text("Attached video (under 5MB)", color = Slate500, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = Slate100)

            Spacer(modifier = Modifier.height(10.dp))

            // Action Bar (Like, Comment, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onLikeClick() }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .testTag("like_post_${post.id}")
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) RoseOccupied else Slate500,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.likes_count}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLiked) RoseOccupied else Slate700
                    )
                }

                // Comment Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onCommentClick() }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .testTag("comment_post_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = Slate500,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.comments_count}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )
                }

                // Share Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onShareClick() }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Slate500,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share",
                        fontSize = 13.sp,
                        color = Slate700
                    )
                }
            }
        }
    }
}

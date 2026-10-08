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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("All") }

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
                    CommunityPostCard(
                        post = post,
                        isLiked = likedPostIds.contains(post.id),
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
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityPost,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    Text(
                        text = (post.author_name.take(1).ifEmpty { "R" }).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.author_name.ifEmpty { "Resident" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
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
                    Image(
                        painter = painterResource(id = R.drawable.hero_residence_banner_1791459672502),
                        contentDescription = "Post image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
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

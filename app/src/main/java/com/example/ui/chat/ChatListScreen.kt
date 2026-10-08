package com.example.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversationItem
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    conversations: List<ConversationItem>,
    onConversationClick: (ConversationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isEmpty()) conversations
        else conversations.filter {
            it.other_user_name.contains(searchQuery, ignoreCase = true) ||
                    it.last_message.contains(searchQuery, ignoreCase = true) ||
                    it.product_name.contains(searchQuery, ignoreCase = true) ||
                    it.room_number.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Chats",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Search conversations
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search conversations...", fontSize = 14.sp, color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Slate400)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = Slate400)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = Slate50,
                        unfocusedContainerColor = Slate50
                    ),
                    singleLine = true
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (filteredConversations.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Chat,
                title = "No conversations yet",
                description = "Start a chat by viewing an available room or marketplace listing and tapping 'Chat with Seller'!",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredConversations, key = { it.id }) { item ->
                    ConversationRow(
                        item = item,
                        onClick = { onConversationClick(item) }
                    )
                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(start = 72.dp))
                }
            }
        }
    }
}

@Composable
fun ConversationRow(
    item: ConversationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("conversation_row_${item.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(RoyalBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (item.other_user_name.take(1).ifEmpty { "C" }).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.other_user_name.ifEmpty { "Residence Member" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "10:42 AM",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.last_message.ifEmpty { "New conversation" },
                    fontSize = 13.sp,
                    color = Slate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (item.unread_count > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(RoyalBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${item.unread_count}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Conversation
import com.example.data.model.MessageType
import com.example.data.repository.AlChatRepository
import com.example.ui.components.MessageStatusIndicator
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class HomeTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector) {
  CHATS("Chats", Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble),
  CALLS("Calls", Icons.Outlined.Call, Icons.Filled.Call),
  STORIES("Stories", Icons.Outlined.HistoryEdu, Icons.Filled.HistoryEdu),
  SETTINGS("Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
}

enum class ChatFilter(val label: String) {
  ALL("All Chats"),
  UNREAD("Unread"),
  GROUPS("Groups"),
  ARCHIVED("Archived")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  repository: AlChatRepository,
  onNavigateToChat: (String) -> Unit,
  onNavigateToGroupChat: (String) -> Unit,
  onNavigateToSearch: () -> Unit,
  onNavigateToCreateGroup: () -> Unit,
  onNavigateToProfile: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onLogout: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(HomeTab.CHATS) }
  var activeFilter by remember { mutableStateOf(ChatFilter.ALL) }
  var searchQuery by remember { mutableStateOf("") }
  var isSearchActive by remember { mutableStateOf(false) }

  val conversations by repository.conversations.collectAsState()
  val currentUser by repository.currentUser.collectAsState()

  val filteredConversations = remember(conversations, searchQuery, activeFilter) {
    conversations.filter { conv ->
      val matchesSearch = if (searchQuery.isBlank()) true
      else conv.title.contains(searchQuery, ignoreCase = true) || conv.lastMessage.contains(searchQuery, ignoreCase = true)

      val matchesFilter = when (activeFilter) {
        ChatFilter.ALL -> true
        ChatFilter.UNREAD -> conv.unreadCount > 0
        ChatFilter.GROUPS -> conv.isGroup
        ChatFilter.ARCHIVED -> false // Clean empty state for archived
      }
      matchesSearch && matchesFilter
    }
  }

  val directChats = remember(filteredConversations) { filteredConversations.filter { !it.isGroup } }
  val groupChats = remember(filteredConversations) { filteredConversations.filter { it.isGroup } }

  val extendedColors = LocalExtendedChatColors.current

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.background)
          .statusBarsPadding()
      ) {
        // Top Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          if (isSearchActive) {
            TextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text("Search conversations...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
              singleLine = true,
              colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
              ),
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
            )
            IconButton(
              onClick = {
                isSearchActive = false
                searchQuery = ""
              },
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          } else {
            // Brand Name (Logo removed as requested)
            Text(
              text = "Al-Chat",
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
              )
            )

            // Action Icons
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              IconButton(
                onClick = { isSearchActive = true },
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surface)
                  .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
              ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surface)
                  .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                  .clickable(onClick = onNavigateToProfile),
                contentAlignment = Alignment.Center
              ) {
                UserAvatar(
                  photoUrl = currentUser?.photoUrl,
                  name = currentUser?.name ?: "User",
                  size = 38.dp,
                  isOnline = true,
                  shape = CircleShape
                )
              }
            }
          }
        }

        // Horizontal Category Filter Pills
        if (selectedTab == HomeTab.CHATS) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(horizontal = 18.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ChatFilter.values().forEach { filter ->
              val isSelected = activeFilter == filter
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) extendedColors.filterPillActiveBg else MaterialTheme.colorScheme.surface,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.clickable { activeFilter = filter }
              ) {
                Text(
                  text = filter.label,
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) extendedColors.filterPillActiveText else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                  ),
                  modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
        }
      }
    },
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(68.dp)
            .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          HomeTab.values().forEach { tab ->
            val selected = selectedTab == tab
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  if (tab == HomeTab.SETTINGS) {
                    onNavigateToSettings()
                  } else {
                    selectedTab = tab
                  }
                }
                .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (selected) tab.selectedIcon else tab.icon,
                  contentDescription = tab.title,
                  tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = tab.title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                  color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                  letterSpacing = 0.5.sp
                )
              )
            }
          }
        }
      }
    },
    floatingActionButton = {
      if (selectedTab == HomeTab.CHATS) {
        FloatingActionButton(
          onClick = {
            if (activeFilter == ChatFilter.GROUPS) onNavigateToCreateGroup()
            else onNavigateToSearch()
          },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          shape = RoundedCornerShape(16.dp),
          elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
          modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)
        ) {
          Icon(
            imageVector = if (activeFilter == ChatFilter.GROUPS) Icons.Default.GroupAdd else Icons.Default.Add,
            contentDescription = "New Action",
            modifier = Modifier.size(28.dp)
          )
        }
      }
    }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(padding)
    ) {
      when (selectedTab) {
        HomeTab.CHATS -> {
          ConversationListTab(
            conversations = filteredConversations,
            onSelectConversation = { conv ->
              if (conv.isGroup) onNavigateToGroupChat(conv.id)
              else onNavigateToChat(conv.id)
            },
            onStartNewChat = onNavigateToSearch,
            emptyTitle = if (activeFilter == ChatFilter.ARCHIVED) "No Archived Chats" else "No Conversations Yet",
            emptySubtitle = if (activeFilter == ChatFilter.ARCHIVED) "Chats you archive will appear here." else "Start a private, secure chat with any registered user."
          )
        }
        HomeTab.CALLS -> {
          CallsListTab()
        }
        HomeTab.STORIES -> {
          StoriesListTab(onNavigateToSearch)
        }
        HomeTab.SETTINGS -> {
          // Handled via navigation
        }
      }
    }
  }
}

@Composable
private fun ConversationListTab(
  conversations: List<Conversation>,
  onSelectConversation: (Conversation) -> Unit,
  onStartNewChat: () -> Unit,
  emptyTitle: String = "No Conversations Yet",
  emptySubtitle: String = "Start a private, secure chat with any registered user."
) {
  if (conversations.isEmpty()) {
    EmptyStateView(
      icon = Icons.Outlined.ChatBubbleOutline,
      title = emptyTitle,
      description = emptySubtitle,
      actionText = "Start New Chat",
      onAction = onStartNewChat
    )
  } else {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
      items(conversations, key = { it.id }) { conv ->
        ConversationItemCard(
          conversation = conv,
          onClick = { onSelectConversation(conv) }
        )
      }
    }
  }
}

@Composable
private fun ConversationItemCard(
  conversation: Conversation,
  onClick: () -> Unit
) {
  val formattedTime = remember(conversation.lastMessageTime) {
    val cal = Calendar.getInstance()
    val today = cal.get(Calendar.DAY_OF_YEAR)
    cal.timeInMillis = conversation.lastMessageTime
    val msgDay = cal.get(Calendar.DAY_OF_YEAR)

    if (today == msgDay) {
      SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(conversation.lastMessageTime))
    } else if (today - msgDay == 1) {
      "Yesterday"
    } else {
      SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(conversation.lastMessageTime))
    }
  }

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = Color.Transparent,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      UserAvatar(
        photoUrl = conversation.photoUrl,
        name = conversation.title,
        size = 54.dp,
        isOnline = conversation.otherUser?.isOnline ?: false,
        shape = RoundedCornerShape(16.dp)
      )

      Spacer(modifier = Modifier.width(14.dp))

      Column(
        modifier = Modifier
          .weight(1f)
          .padding(bottom = 2.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = conversation.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              fontSize = 16.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
          )
          Text(
            text = formattedTime,
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 12.sp
            )
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            if (conversation.isTyping) {
              Text(
                text = "${conversation.typingUserName ?: "someone"} is typing...",
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Medium,
                  fontSize = 14.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            } else {
              if (conversation.lastSenderId == "user_me") {
                MessageStatusIndicator(
                  status = conversation.lastMessageStatus,
                  modifier = Modifier.padding(end = 4.dp),
                  tint = if (conversation.lastMessageStatus == com.example.data.model.MessageStatus.READ) IndigoReadCheck else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Text(
                text = conversation.lastMessage,
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 14.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          if (conversation.unreadCount > 0) {
            Box(
              modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${conversation.unreadCount}",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp
                )
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CallsListTab() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Call,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
        }
        Column {
          Text(
            text = "Voice & Video Calls",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          )
          Text(
            text = "COMING SOON in next release using WebRTC & Cloud Functions.",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
      text = "Recent Call History",
      style = MaterialTheme.typography.titleSmall.copy(
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      ),
      modifier = Modifier.align(Alignment.Start)
    )

    Spacer(modifier = Modifier.height(12.dp))

    CallHistoryItem(name = "Sarah Chen", time = "Yesterday, 8:42 PM", isVideo = false, isIncoming = true)
    CallHistoryItem(name = "Al-Chat Core Team", time = "Aug 29, 3:15 PM", isVideo = true, isIncoming = false)
    CallHistoryItem(name = "Alex Mercer", time = "Aug 27, 11:20 AM", isVideo = false, isIncoming = true)
  }
}

@Composable
private fun StoriesListTab(onStartNewChat: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PurpleAccent.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.HistoryEdu,
            contentDescription = null,
            tint = PurpleAccent,
            modifier = Modifier.size(24.dp)
          )
        }
        Column {
          Text(
            text = "Status & Stories",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          )
          Text(
            text = "Share disappearing photo and video updates with your contacts.",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
          )
        }
      }
    }
  }
}

@Composable
private fun CallHistoryItem(
  name: String,
  time: String,
  isVideo: Boolean,
  isIncoming: Boolean
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        UserAvatar(photoUrl = null, name = name, size = 44.dp, showOnlineBadge = false)
        Column {
          Text(name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface))
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
              imageVector = if (isIncoming) Icons.Filled.CallReceived else Icons.Filled.CallMade,
              contentDescription = null,
              tint = if (isIncoming) EmeraldOnline else MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(14.dp)
            )
            Text(time, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
          }
        }
      }
      IconButton(onClick = {}) {
        Icon(
          imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
          contentDescription = "Call again",
          tint = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}

@Composable
private fun EmptyStateView(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  description: String,
  actionText: String,
  onAction: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(80.dp)
        .clip(RoundedCornerShape(24.dp))
        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(40.dp)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = description,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center,
      style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = onAction,
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
    ) {
      Text(actionText, fontWeight = FontWeight.Bold)
    }
  }
}


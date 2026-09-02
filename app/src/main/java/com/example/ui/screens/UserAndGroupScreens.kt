package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.repository.AlChatRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSearchScreen(
  repository: AlChatRepository,
  onNavigateBack: () -> Unit,
  onUserSelected: (String) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  val searchResults = remember(searchQuery) {
    repository.searchUsers(searchQuery)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, @username, or email...") },
            singleLine = true,
            colors = TextFieldDefaults.colors(
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Close, contentDescription = "Clear")
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      Text(
        text = if (searchQuery.isBlank()) "SUGGESTED CONTACTS" else "SEARCH RESULTS (${searchResults.size})",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
      )

      if (searchResults.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.SearchOff,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No users found",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Try searching with a different @username or email address.",
              style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
          }
        }
      } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
          items(searchResults, key = { it.id }) { user ->
            UserSearchItemCard(
              user = user,
              onClick = {
                val conv = repository.getOrCreateDirectConversation(user.id)
                onUserSelected(conv.id)
              }
            )
            HorizontalDivider(
              modifier = Modifier.padding(start = 76.dp),
              color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun UserSearchItemCard(
  user: User,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    UserAvatar(
      photoUrl = user.photoUrl,
      name = user.name,
      size = 48.dp,
      isOnline = user.isOnline
    )
    Spacer(modifier = Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = user.name,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "@${user.username}",
          style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
        )
      }
      Text(
        text = user.about,
        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        maxLines = 1
      )
    }
    Icon(
      imageVector = Icons.AutoMirrored.Filled.Chat,
      contentDescription = "Message",
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(22.dp)
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
  repository: AlChatRepository,
  onNavigateBack: () -> Unit,
  onGroupCreated: (String) -> Unit
) {
  val context = LocalContext.current
  var groupName by remember { mutableStateOf("") }
  var groupDescription by remember { mutableStateOf("") }
  var groupPhotoUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=200") }
  var selectedMemberIds by remember { mutableStateOf(setOf<String>()) }
  val allUsers by repository.users.collectAsState()
  val currentUser by repository.currentUser.collectAsState()

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let {
      groupPhotoUrl = it.toString()
    }
  }

  val availableUsers = remember(allUsers, currentUser) {
    allUsers.filter { it.id != (currentUser?.id ?: "") }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Create New Group", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          TextButton(
            onClick = {
              if (groupName.isBlank()) {
                Toast.makeText(context, "Please enter a group name", Toast.LENGTH_SHORT).show()
                return@TextButton
              }
              val res = repository.createGroup(
                name = groupName,
                description = groupDescription,
                photoUrl = groupPhotoUrl,
                memberIds = selectedMemberIds.toList()
              )
              if (res.isSuccess) {
                onGroupCreated(res.getOrNull()!!.id)
              }
            }
          ) {
            Text("Create", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .clickable {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
          contentAlignment = Alignment.Center
        ) {
          UserAvatar(photoUrl = groupPhotoUrl, name = groupName.ifBlank { "Group" }, size = 64.dp, showOnlineBadge = false)
        }

        Column(modifier = Modifier.weight(1f)) {
          OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            label = { Text("Group Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
        value = groupDescription,
        onValueChange = { groupDescription = it },
        label = { Text("Group Description (Optional)") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        maxLines = 2
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "SELECT MEMBERS (${selectedMemberIds.size} SELECTED)",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      )

      Spacer(modifier = Modifier.height(8.dp))

      LazyColumn(modifier = Modifier.weight(1f)) {
        items(availableUsers, key = { it.id }) { user ->
          val isSelected = selectedMemberIds.contains(user.id)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                selectedMemberIds = if (isSelected) {
                  selectedMemberIds - user.id
                } else {
                  selectedMemberIds + user.id
                }
              }
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            UserAvatar(photoUrl = user.photoUrl, name = user.name, size = 44.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(user.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
              Text("@${user.username}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
            Checkbox(
              checked = isSelected,
              onCheckedChange = { checked ->
                selectedMemberIds = if (checked) selectedMemberIds + user.id else selectedMemberIds - user.id
              },
              colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
  groupId: String,
  repository: AlChatRepository,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val group = remember(groupId) { repository.getGroupDetails(groupId) }
  val allUsers by repository.users.collectAsState()
  val currentUser by repository.currentUser.collectAsState()

  val memberUsers = remember(group, allUsers) {
    if (group == null) emptyList()
    else allUsers.filter { group.memberIds.contains(it.id) }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Group Information", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { padding ->
    if (group == null) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Group details not found")
      }
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(16.dp))

        UserAvatar(
          photoUrl = group.photoUrl,
          name = group.name,
          size = 90.dp,
          showOnlineBadge = false
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = group.name,
          style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Text(
          text = "Group · ${group.memberIds.size} participants",
          style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        if (group.description.isNotBlank()) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Text(
              text = group.description,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(12.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "MEMBERS (${memberUsers.size})",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        memberUsers.forEach { member ->
          val isAdmin = group.adminIds.contains(member.id)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            UserAvatar(photoUrl = member.photoUrl, name = member.name, size = 44.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (member.id == currentUser?.id) "${member.name} (You)" else member.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
              )
              Text(
                text = member.about,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                maxLines = 1
              )
            }
            if (isAdmin) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
              ) {
                Text(
                  text = "Admin",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  ),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
          onClick = {
            repository.leaveGroup(groupId)
            Toast.makeText(context, "You left the group", Toast.LENGTH_SHORT).show()
            onNavigateBack()
          },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Exit Group")
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

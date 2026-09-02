package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.model.NotificationSettings
import com.example.data.model.PrivacyOption
import com.example.data.model.PrivacySettings
import com.example.data.model.User
import com.example.data.repository.AlChatRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
  userId: String?,
  repository: AlChatRepository,
  onNavigateBack: () -> Unit,
  onNavigateToEditProfile: () -> Unit
) {
  val context = LocalContext.current
  val currentUser by repository.currentUser.collectAsState()
  val targetUser = remember(userId, currentUser) {
    if (userId == null || userId == currentUser?.id) currentUser
    else repository.getUserById(userId)
  }

  val isOwnProfile = targetUser?.id == currentUser?.id

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(if (isOwnProfile) "My Profile" else "Contact Info", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (isOwnProfile) {
            IconButton(onClick = onNavigateToEditProfile) {
              Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary)
            }
          }
        }
      )
    }
  ) { padding ->
    if (targetUser == null) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("User not found")
      }
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(20.dp))

        UserAvatar(
          photoUrl = targetUser.photoUrl,
          name = targetUser.name,
          size = 100.dp,
          isOnline = targetUser.isOnline
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = targetUser.name,
          style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Text(
          text = "@${targetUser.username}",
          style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Info Cards List
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ProfileInfoRow(
              icon = Icons.Default.Info,
              title = "About",
              value = targetUser.about
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ProfileInfoRow(
              icon = Icons.Default.Email,
              title = "Email Address",
              value = targetUser.email
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ProfileInfoRow(
              icon = Icons.Default.Shield,
              title = "Verification Status",
              value = if (targetUser.isEmailVerified) "Email Verified ✓" else "Pending Verification"
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              clipboard.setPrimaryClip(ClipData.newPlainText("Username", "@${targetUser.username}"))
              Toast.makeText(context, "Username copied to clipboard", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share ID")
          }

          if (isOwnProfile) {
            Button(
              onClick = onNavigateToEditProfile,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Edit Info", color = MaterialTheme.colorScheme.onPrimary)
            }
          }
        }

        Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }
}

@Composable
private fun ProfileInfoRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  value: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
    Column {
      Text(title, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
      Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
  repository: AlChatRepository,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val currentUser by repository.currentUser.collectAsState()

  var name by remember { mutableStateOf(currentUser?.name ?: "") }
  var username by remember { mutableStateOf(currentUser?.username ?: "") }
  var about by remember { mutableStateOf(currentUser?.about ?: "") }
  var photoUrl by remember { mutableStateOf(currentUser?.photoUrl ?: "") }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let {
      photoUrl = it.toString()
      Toast.makeText(context, "Photo selected from phone", Toast.LENGTH_SHORT).show()
    }
  }

  val avatarOptions = listOf(
    "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
    "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200",
    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          TextButton(
            onClick = {
              val res = repository.updateProfile(name, username, about, photoUrl)
              if (res.isSuccess) {
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                onNavigateBack()
              } else {
                Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error updating profile", Toast.LENGTH_SHORT).show()
              }
            }
          ) {
            Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier.clickable {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          contentAlignment = Alignment.BottomEnd
        ) {
          UserAvatar(photoUrl = photoUrl, name = name.ifBlank { "User" }, size = 96.dp, showOnlineBadge = false)
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 3.dp,
            modifier = Modifier.size(32.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Change Profile Picture",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Button(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Upload from Phone", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
          }

          if (photoUrl.isNotBlank()) {
            OutlinedButton(
              onClick = { photoUrl = "" },
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Text("Remove", style = MaterialTheme.typography.bodySmall)
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("Or choose a preset style:", style = MaterialTheme.typography.labelSmall)
        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.padding(top = 8.dp)
        ) {
          avatarOptions.forEach { url ->
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .border(
                  width = if (photoUrl == url) 2.dp else 1.dp,
                  color = if (photoUrl == url) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                  shape = CircleShape
                )
                .clickable { photoUrl = url }
            ) {
              UserAvatar(photoUrl = url, name = "A", size = 44.dp, showOnlineBadge = false)
            }
          }
        }
      }

      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Display Name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      OutlinedTextField(
        value = username,
        onValueChange = { username = it },
        label = { Text("Username") },
        prefix = { Text("@") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      OutlinedTextField(
        value = about,
        onValueChange = { about = it },
        label = { Text("About Bio") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        maxLines = 3
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  repository: AlChatRepository,
  onNavigateBack: () -> Unit,
  onNavigateToProfile: () -> Unit,
  onNavigateToPrivacy: () -> Unit,
  onNavigateToNotifications: () -> Unit,
  onNavigateToAppearance: () -> Unit,
  onLogout: () -> Unit
) {
  val currentUser by repository.currentUser.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Settings", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
    ) {
      // Profile Summary Card
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onNavigateToProfile)
          .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          UserAvatar(
            photoUrl = currentUser?.photoUrl,
            name = currentUser?.name ?: "User",
            size = 54.dp,
            isOnline = true
          )
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = currentUser?.name ?: "User",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "@${currentUser?.username ?: "username"}",
              style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
            )
            Text(
              text = currentUser?.about ?: "Hey there! I am using Al-Chat.",
              style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
              maxLines = 1
            )
          }
          Icon(
            imageVector = Icons.Default.QrCode,
            contentDescription = "QR Code",
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }

      SettingsCategoryHeader(title = "PREFERENCES & SECURITY")

      SettingsMenuRow(
        icon = Icons.Outlined.Lock,
        title = "Privacy",
        subtitle = "Last seen, profile photo, read receipts, online status",
        onClick = onNavigateToPrivacy
      )

      SettingsMenuRow(
        icon = Icons.Outlined.Notifications,
        title = "Notifications",
        subtitle = "Message sounds, group alerts, vibrations",
        onClick = onNavigateToNotifications
      )

      SettingsMenuRow(
        icon = Icons.Outlined.Palette,
        title = "Appearance",
        subtitle = "Dark mode, light mode, system theme",
        onClick = onNavigateToAppearance
      )

      SettingsCategoryHeader(title = "ACCOUNT")

      SettingsMenuRow(
        icon = Icons.AutoMirrored.Filled.ExitToApp,
        title = "Logout",
        subtitle = "Sign out of this Al-Chat account session",
        tint = MaterialTheme.colorScheme.error,
        onClick = {
          repository.logout()
          onLogout()
        }
      )

      Spacer(modifier = Modifier.height(32.dp))

      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Al-Chat for Android",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "Version 1.0.0",
          style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun SettingsCategoryHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.labelSmall.copy(
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
  )
}

@Composable
private fun SettingsMenuRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  tint: Color = MaterialTheme.colorScheme.onSurface,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
    Spacer(modifier = Modifier.width(16.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, color = tint))
      Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
    }
    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
  repository: AlChatRepository,
  onNavigateBack: () -> Unit
) {
  val currentUser by repository.currentUser.collectAsState()
  var settings by remember { mutableStateOf(currentUser?.privacySettings ?: PrivacySettings()) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Privacy Settings", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "WHO CAN SEE MY INFORMATION",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      )

      PrivacyOptionRow(
        title = "Last Seen",
        selected = settings.lastSeenVisibility,
        onOptionSelected = {
          settings = settings.copy(lastSeenVisibility = it)
          repository.updatePrivacySettings(settings)
        }
      )

      PrivacyOptionRow(
        title = "Profile Photo",
        selected = settings.profilePhotoVisibility,
        onOptionSelected = {
          settings = settings.copy(profilePhotoVisibility = it)
          repository.updatePrivacySettings(settings)
        }
      )

      PrivacyOptionRow(
        title = "About Info",
        selected = settings.aboutVisibility,
        onOptionSelected = {
          settings = settings.copy(aboutVisibility = it)
          repository.updatePrivacySettings(settings)
        }
      )

      HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("Read Receipts", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
          Text(
            "If turned off, you won't send or receive read receipts (double blue checkmarks).",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
          )
        }
        Switch(
          checked = settings.readReceipts,
          onCheckedChange = {
            settings = settings.copy(readReceipts = it)
            repository.updatePrivacySettings(settings)
          },
          colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
      }
    }
  }
}

@Composable
private fun PrivacyOptionRow(
  title: String,
  selected: PrivacyOption,
  onOptionSelected: (PrivacyOption) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { expanded = true }
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
    Text(
      text = selected.label,
      style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    )

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      PrivacyOption.values().forEach { option ->
        DropdownMenuItem(
          text = { Text(option.label) },
          onClick = {
            onOptionSelected(option)
            expanded = false
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
  repository: AlChatRepository,
  onNavigateBack: () -> Unit
) {
  val settings by repository.notificationSettings.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Notification Settings", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      SettingToggleRow(
        title = "Message Notifications",
        subtitle = "Show notification alerts for 1-to-1 chats",
        checked = settings.messageNotifications,
        onCheckedChange = { repository.updateNotificationSettings(settings.copy(messageNotifications = it)) }
      )

      SettingToggleRow(
        title = "Group Notifications",
        subtitle = "Show notification alerts for group conversations",
        checked = settings.groupNotifications,
        onCheckedChange = { repository.updateNotificationSettings(settings.copy(groupNotifications = it)) }
      )

      SettingToggleRow(
        title = "In-App Notification Sounds",
        subtitle = "Play gentle chime on incoming messages",
        checked = settings.soundEnabled,
        onCheckedChange = { repository.updateNotificationSettings(settings.copy(soundEnabled = it)) }
      )

      SettingToggleRow(
        title = "Vibration",
        subtitle = "Vibrate device on incoming alerts",
        checked = settings.vibrationEnabled,
        onCheckedChange = { repository.updateNotificationSettings(settings.copy(vibrationEnabled = it)) }
      )
    }
  }
}

@Composable
private fun SettingToggleRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
      Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
  repository: AlChatRepository,
  onNavigateBack: () -> Unit
) {
  val currentTheme by repository.themeMode.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Appearance", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = "THEME SELECTION",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      )

      ThemeOptionItem(
        title = "System Default",
        subtitle = "Follow your Android device system light/dark setting",
        isSelected = currentTheme == "SYSTEM",
        onClick = { repository.setThemeMode("SYSTEM") }
      )

      ThemeOptionItem(
        title = "Light Mode",
        subtitle = "Clean high-contrast indigo and bright canvas",
        isSelected = currentTheme == "LIGHT",
        onClick = { repository.setThemeMode("LIGHT") }
      )

      ThemeOptionItem(
        title = "Dark Mode",
        subtitle = "Deep midnight canvas with indigo accents",
        isSelected = currentTheme == "DARK",
        onClick = { repository.setThemeMode("DARK") }
      )
    }
  }
}

@Composable
private fun ThemeOptionItem(
  title: String,
  subtitle: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
          )
        )
        Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
      }
      RadioButton(
        selected = isSelected,
        onClick = onClick,
        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
      )
    }
  }
}


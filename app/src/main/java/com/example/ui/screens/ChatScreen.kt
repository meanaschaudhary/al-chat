package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.repository.AlChatRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
  conversationId: String,
  repository: AlChatRepository,
  onNavigateBack: () -> Unit,
  onNavigateToUserProfile: (String) -> Unit,
  onNavigateToGroupInfo: (String) -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  val conversation by repository.getConversationById(conversationId).collectAsState(initial = null)
  val messages by repository.getMessages(conversationId).collectAsState(initial = emptyList())
  val currentUser by repository.currentUser.collectAsState()

  var inputText by remember { mutableStateOf("") }
  var replyingToMessage by remember { mutableStateOf<Message?>(null) }
  var showAttachmentSheet by remember { mutableStateOf(false) }
  var selectedMediaViewerUrl by remember { mutableStateOf<String?>(null) }
  var selectedFileViewerMessage by remember { mutableStateOf<Message?>(null) }
  var isRecordingVoice by remember { mutableStateOf(false) }
  var voiceRecordingSeconds by remember { mutableStateOf(0) }

  // Mark conversation as read
  LaunchedEffect(conversationId) {
    repository.markConversationAsRead(conversationId)
  }

  // Scroll to bottom on new messages
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  // Voice recording timer simulation
  LaunchedEffect(isRecordingVoice) {
    if (isRecordingVoice) {
      voiceRecordingSeconds = 0
      while (isRecordingVoice) {
        delay(1000)
        voiceRecordingSeconds += 1
      }
    }
  }

  val partnerName = conversation?.title ?: "Chat"
  val isOnline = conversation?.otherUser?.isOnline ?: false
  val subtitle = remember(conversation) {
    when {
      conversation?.isTyping == true -> "typing..."
      conversation?.isGroup == true -> "${conversation?.participants?.size ?: 0} members"
      isOnline -> "Online"
      conversation?.otherUser?.lastSeen != null -> {
        "Last seen " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(conversation!!.otherUser!!.lastSeen))
      }
      else -> ""
    }
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        TopAppBar(
          title = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable {
                if (conversation?.isGroup == true) {
                  onNavigateToGroupInfo(conversationId)
                } else if (conversation?.otherUser != null) {
                  onNavigateToUserProfile(conversation!!.otherUser!!.id)
                }
              }
            ) {
              UserAvatar(
                photoUrl = conversation?.photoUrl,
                name = partnerName,
                size = 40.dp,
                isOnline = isOnline,
                shape = RoundedCornerShape(12.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = partnerName,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                  ),
                  maxLines = 1
                )
                if (subtitle.isNotBlank()) {
                  Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = if (conversation?.isTyping == true || isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                      fontSize = 12.sp
                    ),
                    maxLines = 1
                  )
                }
              }
            }
          },
          navigationIcon = {
            IconButton(onClick = onNavigateBack) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
          },
          actions = {
            IconButton(
              onClick = {
                Toast.makeText(context, "Voice call integration coming in next version (WebRTC)", Toast.LENGTH_SHORT).show()
              }
            ) {
              Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(
              onClick = {
                Toast.makeText(context, "Video call integration coming in next version (WebRTC)", Toast.LENGTH_SHORT).show()
              }
            ) {
              Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = MaterialTheme.colorScheme.primary)
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )
      }
    },
    bottomBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
          .navigationBarsPadding()
          .imePadding()
      ) {
        // Quoted Reply preview banner
        AnimatedVisibility(visible = replyingToMessage != null) {
          replyingToMessage?.let { replyMsg ->
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Replying to ${replyMsg.senderName}",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  )
                  Text(
                    text = replyMsg.text.ifBlank { "Attachment" },
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    maxLines = 1
                  )
                }
                IconButton(onClick = { replyingToMessage = null }) {
                  Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }
          }
        }

        // Voice Recording Banner
        if (isRecordingVoice) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.Mic, contentDescription = null, tint = RoseError)
              Text(
                text = "Recording: 0:${String.format(Locale.getDefault(), "%02d", voiceRecordingSeconds)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = RoseError)
              )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              TextButton(onClick = { isRecordingVoice = false }) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Button(
                onClick = {
                  val duration = voiceRecordingSeconds.coerceAtLeast(3)
                  isRecordingVoice = false
                  repository.sendMessage(
                    conversationId = conversationId,
                    text = "Voice message ($duration s)",
                    type = MessageType.VOICE,
                    audioDurationSec = duration,
                    replyTo = replyingToMessage
                  )
                  replyingToMessage = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text("Send Audio", fontWeight = FontWeight.Bold)
              }
            }
          }
        } else {
          // Normal Input Field Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            IconButton(
              onClick = { showAttachmentSheet = true },
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
            ) {
              Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = "Attach file",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            }

            Surface(
              shape = RoundedCornerShape(24.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
              modifier = Modifier.weight(1f)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
              ) {
                TextField(
                  value = inputText,
                  onValueChange = { inputText = it },
                  placeholder = { Text("Message...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                  modifier = Modifier.weight(1f),
                  colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                  ),
                  maxLines = 4
                )
              }
            }

            if (inputText.isNotBlank()) {
              IconButton(
                onClick = {
                  val textToSend = inputText.trim()
                  inputText = ""
                  val reply = replyingToMessage
                  replyingToMessage = null
                  repository.sendMessage(
                    conversationId = conversationId,
                    text = textToSend,
                    type = MessageType.TEXT,
                    replyTo = reply
                  )
                },
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary)
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Send,
                  contentDescription = "Send",
                  tint = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
            } else {
              IconButton(
                onClick = { isRecordingVoice = true },
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary)
              ) {
                Icon(
                  imageVector = Icons.Default.Mic,
                  contentDescription = "Record Voice",
                  tint = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.size(22.dp)
                )
              }
            }
          }
        }
      }
    }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background)
    ) {
      if (messages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.padding(32.dp)
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "End-to-End Private Routing",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Messages and calls stay between you and this contact.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }
      } else {
        LazyColumn(
          state = listState,
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
          items(messages, key = { it.id }) { msg ->
            val isOutgoing = msg.senderId == (currentUser?.id ?: "user_me")
            MessageBubble(
              message = msg,
              isOutgoing = isOutgoing,
              showSenderName = conversation?.isGroup == true,
              onReply = { messageToReply -> replyingToMessage = messageToReply },
              onReaction = { targetMsg, emoji ->
                repository.toggleReaction(conversationId, targetMsg.id, emoji)
              },
              onDelete = { messageToDelete ->
                repository.deleteMessage(conversationId, messageToDelete.id)
              },
              onImageClick = { url -> selectedMediaViewerUrl = url },
              onFileClick = { fileMsg -> selectedFileViewerMessage = fileMsg }
            )
          }
        }
      }
    }
  }

  // Attachment Bottom Sheet
  if (showAttachmentSheet) {
    AttachmentBottomSheet(
      onDismiss = { showAttachmentSheet = false },
      onSelectPhoto = {
        // Send a high quality sample photo preview
        repository.sendMessage(
          conversationId = conversationId,
          text = "Shared image via Al-Chat",
          type = MessageType.IMAGE,
          mediaUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=800",
          replyTo = replyingToMessage
        )
        replyingToMessage = null
        Toast.makeText(context, "Photo compressed and sent", Toast.LENGTH_SHORT).show()
      },
      onSelectDoc = {
        repository.sendMessage(
          conversationId = conversationId,
          text = "Project_Architecture_Specification.pdf",
          type = MessageType.FILE,
          fileName = "Project_Architecture_v1.pdf",
          fileSize = "2.4 MB",
          replyTo = replyingToMessage
        )
        replyingToMessage = null
        Toast.makeText(context, "Document shared", Toast.LENGTH_SHORT).show()
      },
      onSelectAudio = {
        repository.sendMessage(
          conversationId = conversationId,
          text = "Audio note",
          type = MessageType.VOICE,
          audioDurationSec = 12,
          replyTo = replyingToMessage
        )
        replyingToMessage = null
        Toast.makeText(context, "Audio note sent", Toast.LENGTH_SHORT).show()
      },
      onSelectCamera = {
        repository.sendMessage(
          conversationId = conversationId,
          text = "Camera snap 📸",
          type = MessageType.IMAGE,
          mediaUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800",
          replyTo = replyingToMessage
        )
        replyingToMessage = null
        Toast.makeText(context, "Camera photo sent", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // Fullscreen Media Viewer Dialog
  selectedMediaViewerUrl?.let { mediaUrl ->
    MediaViewerDialog(
      mediaUrl = mediaUrl,
      onDismiss = { selectedMediaViewerUrl = null }
    )
  }

  // File Viewer Dialog
  selectedFileViewerMessage?.let { fileMsg ->
    FileViewerDialog(
      message = fileMsg,
      onDismiss = { selectedFileViewerMessage = null }
    )
  }
}

@Composable
fun MediaViewerDialog(
  mediaUrl: String,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
    ) {
      AsyncImage(
        model = mediaUrl,
        contentDescription = "Full Preview",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
      )

      // Top Actions Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          IconButton(
            onClick = { Toast.makeText(context, "Image saved to device", Toast.LENGTH_SHORT).show() },
            modifier = Modifier
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.6f))
          ) {
            Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
          }
          IconButton(
            onClick = { Toast.makeText(context, "Sharing photo", Toast.LENGTH_SHORT).show() },
            modifier = Modifier
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.6f))
          ) {
            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
          }
        }
      }
    }
  }
}

@Composable
fun FileViewerDialog(
  message: Message,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier.fillMaxWidth(0.92f)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.InsertDriveFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = message.fileName ?: "Document.pdf",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Size: ${message.fileSize ?: "2.4 MB"}",
          style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = {
            Toast.makeText(context, "Downloading ${message.fileName}...", Toast.LENGTH_SHORT).show()
            onDismiss()
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Download Document", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onDismiss) {
          Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  }
}

package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
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
import coil.compose.AsyncImage
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
  message: Message,
  isOutgoing: Boolean,
  showSenderName: Boolean = false,
  onReply: (Message) -> Unit,
  onReaction: (Message, String) -> Unit,
  onDelete: (Message) -> Unit,
  onImageClick: (String) -> Unit,
  onFileClick: (Message) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showMenuDialog by remember { mutableStateOf(false) }
  val extendedColors = LocalExtendedChatColors.current

  val bubbleShape = if (isOutgoing) {
    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
  } else {
    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
  }

  val backgroundColor = if (isOutgoing) {
    extendedColors.outgoingBubble
  } else {
    extendedColors.incomingBubble
  }

  val borderModifier = if (!isOutgoing) {
    Modifier.border(1.dp, extendedColors.subtleBorder, bubbleShape)
  } else {
    Modifier
  }

  val textColor = if (isOutgoing) extendedColors.outgoingText else extendedColors.incomingText

  val timeString = remember(message.timestamp) {
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 3.dp),
    horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
  ) {
    Box(
      modifier = Modifier
        .widthIn(min = 80.dp, max = 310.dp)
        .clip(bubbleShape)
        .then(borderModifier)
        .background(backgroundColor)
        .combinedClickable(
          onClick = {
            if (message.type == MessageType.IMAGE && message.mediaUrl != null) {
              onImageClick(message.mediaUrl)
            } else if (message.type == MessageType.FILE) {
              onFileClick(message)
            }
          },
          onLongClick = {
            showMenuDialog = true
          }
        )
        .padding(
          start = 12.dp,
          end = 12.dp,
          top = 9.dp,
          bottom = 7.dp
        )
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {

        // Group Sender Name (for incoming group messages)
        if (showSenderName && !isOutgoing) {
          Text(
            text = message.senderName,
            style = MaterialTheme.typography.labelMedium.copy(
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 2.dp)
          )
        }

        // Quoted Reply Preview
        if (!message.replyToText.isNullOrBlank()) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isOutgoing) extendedColors.outgoingReplyBg else extendedColors.incomingReplyBg,
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 6.dp)
          ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
              Box(
                modifier = Modifier
                  .width(4.dp)
                  .fillMaxHeight()
                  .background(if (isOutgoing) IndigoLight else MaterialTheme.colorScheme.primary)
              )
              Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                  text = message.replyToSenderName ?: "Reply",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isOutgoing) IndigoLight else MaterialTheme.colorScheme.primary
                  )
                )
                Text(
                  text = message.replyToText,
                  style = MaterialTheme.typography.bodySmall,
                  maxLines = 1,
                  color = if (isOutgoing) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        // Content based on MessageType
        when (message.type) {
          MessageType.TEXT -> {
            Text(
              text = message.text,
              style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                color = textColor,
                lineHeight = 21.sp
              )
            )
          }

          MessageType.IMAGE -> {
            Column {
              if (!message.mediaUrl.isNullOrBlank()) {
                AsyncImage(
                  model = message.mediaUrl,
                  contentDescription = "Image message",
                  modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .clip(RoundedCornerShape(8.dp)),
                  contentScale = ContentScale.Crop
                )
              }
              if (message.text.isNotBlank()) {
                Text(
                  text = message.text,
                  style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                  modifier = Modifier.padding(top = 6.dp)
                )
              }
            }
          }

          MessageType.VOICE -> {
            VoiceWaveformPlayer(
              durationSec = message.audioDurationSec.coerceAtLeast(3),
              isOutgoing = isOutgoing
            )
          }

          MessageType.FILE -> {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isOutgoing) Color.Black.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                .padding(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "File",
                tint = if (isOutgoing) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = message.fileName ?: "Document.pdf",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                  ),
                  maxLines = 1
                )
                Text(
                  text = message.fileSize ?: "1.4 MB",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isOutgoing) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              }
              Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download",
                tint = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          MessageType.VIDEO -> {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.4f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PlayCircleFilled,
                contentDescription = "Play Video",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
              )
            }
          }
        }

        // Timestamp & Status Indicator
        Row(
          modifier = Modifier
            .align(Alignment.End)
            .padding(top = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = timeString,
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 11.sp,
              color = if (isOutgoing) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
          if (isOutgoing) {
            MessageStatusIndicator(
              status = message.status,
              tint = Color.White.copy(alpha = 0.85f)
            )
          }
        }
      }
    }

    // Reaction Badges at bubble edge
    if (message.reactions.isNotEmpty()) {
      Row(
        modifier = Modifier
          .padding(top = 2.dp, start = if (isOutgoing) 0.dp else 8.dp, end = if (isOutgoing) 8.dp else 0.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        val grouped = message.reactions.values.groupingBy { it }.eachCount()
        grouped.forEach { (emoji, count) ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 1.dp,
            modifier = Modifier.clickable { onReaction(message, emoji) }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Text(text = emoji, fontSize = 12.sp)
              if (count > 1) {
                Text(
                  text = "$count",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
              }
            }
          }
        }
      }
    }
  }

  // Quick Action & Reaction Dialog
  if (showMenuDialog) {
    MessageActionsDialog(
      message = message,
      isOutgoing = isOutgoing,
      onDismiss = { showMenuDialog = false },
      onReaction = { emoji ->
        onReaction(message, emoji)
        showMenuDialog = false
      },
      onReply = {
        onReply(message)
        showMenuDialog = false
      },
      onCopy = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Message", message.text))
        Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
        showMenuDialog = false
      },
      onForward = {
        Toast.makeText(context, "Forward feature ready", Toast.LENGTH_SHORT).show()
        showMenuDialog = false
      },
      onDelete = {
        onDelete(message)
        showMenuDialog = false
      }
    )
  }
}

@Composable
fun MessageActionsDialog(
  message: Message,
  isOutgoing: Boolean,
  onDismiss: () -> Unit,
  onReaction: (String) -> Unit,
  onReply: () -> Unit,
  onCopy: () -> Unit,
  onForward: () -> Unit,
  onDelete: () -> Unit
) {
  val emojis = listOf("👍", "❤️", "😂", "😮", "😢", "🙏", "🔥")

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
      tonalElevation = 6.dp,
      modifier = Modifier.fillMaxWidth(0.92f)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        // Quick Reaction Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          emojis.forEach { emoji ->
            Box(
              modifier = Modifier
                .clip(CircleShape)
                .clickable { onReaction(emoji) }
                .padding(6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(text = emoji, fontSize = 22.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Options
        ActionMenuItem(
          icon = Icons.AutoMirrored.Outlined.Reply,
          title = "Reply",
          tint = MaterialTheme.colorScheme.onSurface,
          onClick = onReply
        )
        if (message.text.isNotBlank()) {
          ActionMenuItem(
            icon = Icons.Outlined.ContentCopy,
            title = "Copy Text",
            tint = MaterialTheme.colorScheme.onSurface,
            onClick = onCopy
          )
        }
        ActionMenuItem(
          icon = Icons.AutoMirrored.Outlined.Forward,
          title = "Forward",
          tint = MaterialTheme.colorScheme.onSurface,
          onClick = onForward
        )
        if (isOutgoing) {
          ActionMenuItem(
            icon = Icons.Outlined.Delete,
            title = "Delete Message",
            tint = RoseError,
            onClick = onDelete
          )
        }
      }
    }
  }
}

@Composable
private fun ActionMenuItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  tint: Color = MaterialTheme.colorScheme.onSurface,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = title,
      tint = tint,
      modifier = Modifier.size(22.dp)
    )
    Text(
      text = title,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Medium,
        color = tint
      )
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentBottomSheet(
  onDismiss: () -> Unit,
  onSelectPhoto: () -> Unit,
  onSelectDoc: () -> Unit,
  onSelectAudio: () -> Unit,
  onSelectCamera: () -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface,
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(vertical = 10.dp)
          .size(width = 36.dp, height = 4.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
      )
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
      Text(
        text = "Share Content",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.padding(bottom = 16.dp)
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        AttachmentOptionButton(
          icon = Icons.Default.PhotoLibrary,
          title = "Gallery",
          backgroundColor = IndigoPrimary,
          onClick = {
            onSelectPhoto()
            onDismiss()
          }
        )
        AttachmentOptionButton(
          icon = Icons.Default.CameraAlt,
          title = "Camera",
          backgroundColor = PurpleAccent,
          onClick = {
            onSelectCamera()
            onDismiss()
          }
        )
        AttachmentOptionButton(
          icon = Icons.Default.Description,
          title = "Document",
          backgroundColor = Color(0xFF6366F1),
          onClick = {
            onSelectDoc()
            onDismiss()
          }
        )
        AttachmentOptionButton(
          icon = Icons.Default.Mic,
          title = "Audio",
          backgroundColor = EmeraldOnline,
          onClick = {
            onSelectAudio()
            onDismiss()
          }
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun AttachmentOptionButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  backgroundColor: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .size(54.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(backgroundColor),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = Color.White,
        modifier = Modifier.size(26.dp)
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    )
  }
}

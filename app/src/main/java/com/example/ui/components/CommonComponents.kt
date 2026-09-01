package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MessageStatus
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserAvatar(
  photoUrl: String?,
  name: String,
  modifier: Modifier = Modifier,
  size: Dp = 48.dp,
  isOnline: Boolean = false,
  showOnlineBadge: Boolean = true,
  shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(size * 0.32f)
) {
  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    if (!photoUrl.isNullOrBlank()) {
      AsyncImage(
        model = photoUrl,
        contentDescription = "Avatar of $name",
        modifier = Modifier
          .fillMaxSize()
          .clip(shape)
          .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), shape),
        contentScale = ContentScale.Crop
      )
    } else {
      val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .ifBlank { "AC" }
        .uppercase()

      // Deterministic palette gradient
      val gradientColors = remember(name) {
        val hash = name.hashCode().let { if (it < 0) -it else it }
        when (hash % 4) {
          0 -> listOf(Color(0xFF334155), Color(0xFF475569))
          1 -> listOf(Color(0xFF1E293B), Color(0xFF334155))
          2 -> listOf(Color(0xFF312E81), Color(0xFF4338CA))
          else -> listOf(Color(0xFF1F2937), Color(0xFF374151))
        }
      }

      Box(
        modifier = Modifier
          .fillMaxSize()
          .clip(shape)
          .background(
            Brush.linearGradient(colors = gradientColors)
          )
          .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), shape),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = initials,
          style = MaterialTheme.typography.titleMedium.copy(
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.35f).sp
          )
        )
      }
    }

    if (showOnlineBadge && isOnline) {
      Box(
        modifier = Modifier
          .size(size * 0.28f)
          .align(Alignment.BottomEnd)
          .clip(CircleShape)
          .background(EmeraldOnline)
          .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
      )
    }
  }
}

@Composable
fun MessageStatusIndicator(
  status: MessageStatus,
  modifier: Modifier = Modifier,
  tint: Color = Color.Unspecified
) {
  when (status) {
    MessageStatus.SENDING -> {
      Icon(
        imageVector = Icons.Default.Schedule,
        contentDescription = "Sending",
        modifier = modifier.size(14.dp),
        tint = if (tint != Color.Unspecified) tint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
      )
    }
    MessageStatus.SENT -> {
      Icon(
        imageVector = Icons.Default.Check,
        contentDescription = "Sent",
        modifier = modifier.size(14.dp),
        tint = if (tint != Color.Unspecified) tint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
      )
    }
    MessageStatus.DELIVERED -> {
      Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          modifier = Modifier.size(14.dp),
          tint = if (tint != Color.Unspecified) tint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Delivered",
          modifier = Modifier.size(14.dp),
          tint = if (tint != Color.Unspecified) tint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
      }
    }
    MessageStatus.READ -> {
      Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          modifier = Modifier.size(14.dp),
          tint = BlueReadCheck
        )
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Read",
          modifier = Modifier.size(14.dp),
          tint = BlueReadCheck
        )
      }
    }
  }
}

@Composable
fun DateSeparatorBadge(
  timestamp: Long,
  modifier: Modifier = Modifier
) {
  val label = remember(timestamp) {
    val cal = Calendar.getInstance()
    val today = cal.get(Calendar.DAY_OF_YEAR)
    val todayYear = cal.get(Calendar.YEAR)

    cal.timeInMillis = timestamp
    val msgDay = cal.get(Calendar.DAY_OF_YEAR)
    val msgYear = cal.get(Calendar.YEAR)

    when {
      todayYear == msgYear && today == msgDay -> "Today"
      todayYear == msgYear && today - msgDay == 1 -> "Yesterday"
      else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
      contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
      )
    }
  }
}

@Composable
fun TypingIndicator(
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "typing")
  
  val dot1Alpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot1"
  )
  val dot2Alpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, delayMillis = 200, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot2"
  )
  val dot3Alpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, delayMillis = 400, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot3"
  )

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .padding(horizontal = 12.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary.copy(alpha = dot1Alpha))
    )
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary.copy(alpha = dot2Alpha))
    )
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary.copy(alpha = dot3Alpha))
    )
  }
}

@Composable
fun VoiceWaveformPlayer(
  durationSec: Int,
  modifier: Modifier = Modifier,
  isOutgoing: Boolean = false
) {
  var isPlaying by remember { mutableStateOf(false) }
  var progress by remember { mutableStateOf(0f) }

  val barHeights = remember {
    listOf(0.4f, 0.7f, 0.9f, 0.5f, 0.8f, 1.0f, 0.6f, 0.4f, 0.7f, 0.85f, 0.5f, 0.3f, 0.6f, 0.75f, 0.4f)
  }

  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    IconButton(
      onClick = { isPlaying = !isPlaying },
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(if (isOutgoing) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
      Icon(
        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
        contentDescription = if (isPlaying) "Pause" else "Play",
        tint = if (isOutgoing) Color.White else MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(20.dp)
      )
    }

    Row(
      modifier = Modifier
        .weight(1f)
        .height(28.dp),
      horizontalArrangement = Arrangement.spacedBy(3.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      barHeights.forEachIndexed { index, heightMultiplier ->
        val active = (index.toFloat() / barHeights.size) <= (if (isPlaying) 0.65f else 0.3f)
        Box(
          modifier = Modifier
            .weight(1f)
            .height(24.dp * heightMultiplier)
            .clip(RoundedCornerShape(2.dp))
            .background(
              if (isOutgoing) {
                if (active) Color.White else Color.White.copy(alpha = 0.4f)
              } else {
                if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
              }
            )
        )
      }
    }

    Text(
      text = String.format(Locale.getDefault(), "0:%02d", durationSec),
      style = MaterialTheme.typography.labelSmall,
      color = if (isOutgoing) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

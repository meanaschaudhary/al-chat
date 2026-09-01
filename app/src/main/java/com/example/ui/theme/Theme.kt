package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class ExtendedChatColors(
  val outgoingBubble: Color,
  val incomingBubble: Color,
  val outgoingText: Color,
  val incomingText: Color,
  val outgoingReplyBg: Color,
  val incomingReplyBg: Color,
  val filterPillActiveBg: Color,
  val filterPillActiveText: Color,
  val subtleBorder: Color
)

val LocalExtendedChatColors = staticCompositionLocalOf {
  ExtendedChatColors(
    outgoingBubble = Color(0xFF4338CA),
    incomingBubble = Color(0xFF1C1D21),
    outgoingText = Color.White,
    incomingText = Color.White,
    outgoingReplyBg = Color(0x40000000),
    incomingReplyBg = Color(0xFF2A2B2F),
    filterPillActiveBg = Color(0xFFA5B4FC),
    filterPillActiveText = Color(0xFF0F1012),
    subtleBorder = Color(0xFF2A2B2F)
  )
}

val DarkExtendedColors = ExtendedChatColors(
  outgoingBubble = Color(0xFF4338CA),
  incomingBubble = Color(0xFF1C1D21),
  outgoingText = Color.White,
  incomingText = Color(0xFFFFFFFF),
  outgoingReplyBg = Color(0x40000000),
  incomingReplyBg = Color(0xFF2A2B2F),
  filterPillActiveBg = Color(0xFFA5B4FC),
  filterPillActiveText = Color(0xFF0F1012),
  subtleBorder = Color(0xFF2A2B2F)
)

val LightExtendedColors = ExtendedChatColors(
  outgoingBubble = Color(0xFF4F46E5),
  incomingBubble = Color(0xFFFFFFFF),
  outgoingText = Color.White,
  incomingText = Color(0xFF0F172A),
  outgoingReplyBg = Color(0x25000000),
  incomingReplyBg = Color(0xFFF1F5F9),
  filterPillActiveBg = Color(0xFF4F46E5),
  filterPillActiveText = Color(0xFFFFFFFF),
  subtleBorder = Color(0xFFE2E8F0)
)

private val AlChatDarkColorScheme = darkColorScheme(
  primary = IndigoPrimary,
  onPrimary = Color(0xFF0F1012),
  primaryContainer = IndigoContainer,
  onPrimaryContainer = IndigoLight,
  secondary = PurpleAccent,
  onSecondary = Color(0xFF0F1012),
  secondaryContainer = SophisticatedSurfaceElevated,
  onSecondaryContainer = Color.White,
  tertiary = EmeraldOnline,
  onTertiary = Color(0xFF0F1012),
  background = Color(0xFF0F1012),
  onBackground = Color(0xFFFFFFFF),
  surface = Color(0xFF1C1D21),
  onSurface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xFF2A2B2F),
  onSurfaceVariant = Color(0xFF94A3B8),
  outline = Color(0xFF2A2B2F),
  outlineVariant = Color(0xFF3A3B40),
  error = RoseError,
  onError = Color.White
)

private val AlChatLightColorScheme = lightColorScheme(
  primary = Color(0xFF4F46E5),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFEEF2FF),
  onPrimaryContainer = Color(0xFF3730A3),
  secondary = Color(0xFF9333EA),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFF3E8FF),
  onSecondaryContainer = Color(0xFF581C87),
  tertiary = EmeraldOnline,
  onTertiary = Color.White,
  background = Color(0xFFF8FAFC),
  onBackground = Color(0xFF0F172A),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF0F172A),
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = Color(0xFF64748B),
  outline = Color(0xFFE2E8F0),
  outlineVariant = Color(0xFFCBD5E1),
  error = RoseError,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> AlChatDarkColorScheme
    else -> AlChatLightColorScheme
  }

  val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

  CompositionLocalProvider(
    LocalExtendedChatColors provides extendedColors
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}

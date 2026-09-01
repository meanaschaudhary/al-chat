package com.example.data.model

data class NotificationSettings(
  val messageNotifications: Boolean = true,
  val groupNotifications: Boolean = true,
  val soundEnabled: Boolean = true,
  val vibrationEnabled: Boolean = true,
  val previewMessage: Boolean = true
)

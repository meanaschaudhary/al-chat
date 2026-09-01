package com.example.data.model

enum class MessageType {
  TEXT,
  IMAGE,
  VOICE,
  FILE,
  VIDEO
}

enum class MessageStatus {
  SENDING,
  SENT,
  DELIVERED,
  READ
}

data class Message(
  val id: String = "",
  val conversationId: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val receiverId: String = "",
  val text: String = "",
  val type: MessageType = MessageType.TEXT,
  val mediaUrl: String? = null,
  val fileName: String? = null,
  val fileSize: String? = null,
  val audioDurationSec: Int = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val status: MessageStatus = MessageStatus.SENT,
  val reactions: Map<String, String> = emptyMap(), // userId -> emoji
  val replyToMessageId: String? = null,
  val replyToText: String? = null,
  val replyToSenderName: String? = null,
  val isDeleted: Boolean = false
)

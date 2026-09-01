package com.example.data.model

data class Conversation(
  val id: String = "",
  val isGroup: Boolean = false,
  val title: String = "",
  val photoUrl: String = "",
  val participants: List<String> = emptyList(),
  val lastMessage: String = "",
  val lastMessageType: MessageType = MessageType.TEXT,
  val lastMessageTime: Long = System.currentTimeMillis(),
  val lastSenderId: String = "",
  val lastMessageStatus: MessageStatus = MessageStatus.READ,
  val unreadCount: Int = 0,
  val isTyping: Boolean = false,
  val typingUserName: String? = null,
  val otherUser: User? = null
)

data class Group(
  val id: String = "",
  val name: String = "",
  val description: String = "",
  val photoUrl: String = "",
  val createdBy: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val memberIds: List<String> = emptyList(),
  val adminIds: List<String> = emptyList()
)

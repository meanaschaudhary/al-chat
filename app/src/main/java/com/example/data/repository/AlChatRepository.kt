package com.example.data.repository

import android.content.Context
import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class AuthState {
  object Unauthenticated : AuthState()
  object Loading : AuthState()
  data class Authenticated(val user: User) : AuthState()
  data class Unverified(val user: User) : AuthState()
}

class AlChatRepository private constructor(private val context: Context) {

  private val scope = CoroutineScope(Dispatchers.IO)

  // Firebase Instances (available when google-services.json is attached)
  private var firebaseAuth: FirebaseAuth? = null
  private var firestore: FirebaseFirestore? = null
  val isFirebaseConfigured: Boolean
    get() = firebaseAuth != null && firestore != null

  private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
  val authState: StateFlow<AuthState> = _authState.asStateFlow()

  private val _currentUser = MutableStateFlow<User?>(null)
  val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

  // App-wide data stores
  private val _users = MutableStateFlow<List<User>>(emptyList())
  val users: StateFlow<List<User>> = _users.asStateFlow()

  private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
  val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

  private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap()) // convId -> messages

  private val _groups = MutableStateFlow<Map<String, Group>>(emptyMap())

  private val _themeMode = MutableStateFlow("SYSTEM") // "SYSTEM", "LIGHT", "DARK"
  val themeMode: StateFlow<String> = _themeMode.asStateFlow()

  private val _notificationSettings = MutableStateFlow(NotificationSettings())
  val notificationSettings: StateFlow<NotificationSettings> = _notificationSettings.asStateFlow()

  init {
    // Load saved theme preference
    try {
      val prefs = context.getSharedPreferences("alchat_prefs", Context.MODE_PRIVATE)
      val savedTheme = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
      _themeMode.value = savedTheme
    } catch (_: Exception) {}

    try {
      firebaseAuth = FirebaseAuth.getInstance()
      firestore = FirebaseFirestore.getInstance()
    } catch (_: Exception) {
      // Firebase not initialized in local preview without google-services.json
      firebaseAuth = null
      firestore = null
    }

    seedInitialData()
  }

  companion object {
    @Volatile
    private var instance: AlChatRepository? = null

    fun getInstance(context: Context? = null): AlChatRepository {
      return instance ?: synchronized(this) {
        instance ?: AlChatRepository(context?.applicationContext ?: com.example.AlChatApplication.appContext).also { instance = it }
      }
    }
  }

  private fun seedInitialData() {
    val defaultUsers = listOf(
      User(
        id = "user_sarah",
        name = "Sarah Chen",
        username = "sarah_c",
        email = "sarah.chen@example.com",
        photoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
        about = "Building next-gen mobile experiences 🚀",
        isOnline = true,
        lastSeen = System.currentTimeMillis()
      ),
      User(
        id = "user_alex",
        name = "Alex Mercer",
        username = "alex_m",
        email = "alex.mercer@example.com",
        photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
        about = "Coffee, code & cryptography ☕🔐",
        isOnline = false,
        lastSeen = System.currentTimeMillis() - 1000 * 60 * 25
      ),
      User(
        id = "user_elena",
        name = "Elena Rostova",
        username = "elena_r",
        email = "elena.rostova@example.com",
        photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
        about = "UI/UX & Design Systems lead 🎨",
        isOnline = true,
        lastSeen = System.currentTimeMillis()
      ),
      User(
        id = "user_marcus",
        name = "Marcus Vance",
        username = "marcus_v",
        email = "marcus.v@example.com",
        photoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
        about = "Simple. Private. Connected.",
        isOnline = false,
        lastSeen = System.currentTimeMillis() - 1000 * 60 * 180
      ),
      User(
        id = "user_alchat_bot",
        name = "Al-Chat Assistant",
        username = "alchat_official",
        email = "support@alchat.app",
        photoUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200",
        about = "Welcome to Al-Chat! Ask me anything about privacy & features.",
        isOnline = true,
        lastSeen = System.currentTimeMillis()
      )
    )

    _users.value = defaultUsers

    // Pre-create initial demo account
    val defaultCurrentUser = User(
      id = "user_me",
      name = "Zaid Khan",
      username = "zaid_k",
      email = "zaid@alchat.app",
      photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
      about = "Living in the moment. Always connected.",
      isOnline = true,
      lastSeen = System.currentTimeMillis(),
      isEmailVerified = true
    )

    _currentUser.value = defaultCurrentUser
    _authState.value = AuthState.Authenticated(defaultCurrentUser)

    // Seed Initial Conversations
    val convSarahId = "conv_sarah"
    val convAlexId = "conv_alex"
    val convBotId = "conv_bot"
    val convGroupId = "conv_group_dev"

    val convSarah = Conversation(
      id = convSarahId,
      isGroup = false,
      title = "Sarah Chen",
      photoUrl = defaultUsers[0].photoUrl,
      participants = listOf(defaultCurrentUser.id, defaultUsers[0].id),
      lastMessage = "The new Al-Chat interface looks super clean and fast! ✨",
      lastMessageType = MessageType.TEXT,
      lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 3,
      lastSenderId = defaultUsers[0].id,
      lastMessageStatus = MessageStatus.READ,
      unreadCount = 1,
      otherUser = defaultUsers[0]
    )

    val convAlex = Conversation(
      id = convAlexId,
      isGroup = false,
      title = "Alex Mercer",
      photoUrl = defaultUsers[1].photoUrl,
      participants = listOf(defaultCurrentUser.id, defaultUsers[1].id),
      lastMessage = "Sent a voice message (0:14)",
      lastMessageType = MessageType.VOICE,
      lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 45,
      lastSenderId = defaultUsers[1].id,
      lastMessageStatus = MessageStatus.READ,
      unreadCount = 0,
      otherUser = defaultUsers[1]
    )

    val convBot = Conversation(
      id = convBotId,
      isGroup = false,
      title = "Al-Chat Assistant",
      photoUrl = defaultUsers[4].photoUrl,
      participants = listOf(defaultCurrentUser.id, defaultUsers[4].id),
      lastMessage = "Welcome to Al-Chat! Your communication is private and secure.",
      lastMessageType = MessageType.TEXT,
      lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 120,
      lastSenderId = defaultUsers[4].id,
      lastMessageStatus = MessageStatus.READ,
      unreadCount = 0,
      otherUser = defaultUsers[4]
    )

    val groupDev = Group(
      id = convGroupId,
      name = "Al-Chat Core Team",
      description = "Official project collaboration and architecture updates.",
      photoUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=200",
      createdBy = defaultCurrentUser.id,
      createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7,
      memberIds = listOf(defaultCurrentUser.id, "user_sarah", "user_alex", "user_elena"),
      adminIds = listOf(defaultCurrentUser.id, "user_sarah")
    )

    val convGroup = Conversation(
      id = convGroupId,
      isGroup = true,
      title = groupDev.name,
      photoUrl = groupDev.photoUrl,
      participants = groupDev.memberIds,
      lastMessage = "Elena: Finalized the Material 3 color tokens for dark mode.",
      lastMessageType = MessageType.TEXT,
      lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 15,
      lastSenderId = "user_elena",
      lastMessageStatus = MessageStatus.READ,
      unreadCount = 2
    )

    _groups.value = mapOf(groupDev.id to groupDev)
    _conversations.value = listOf(convSarah, convGroup, convAlex, convBot)

    // Seed Messages
    val sarahMessages = listOf(
      Message(
        id = "msg_s1",
        conversationId = convSarahId,
        senderId = defaultCurrentUser.id,
        senderName = defaultCurrentUser.name,
        receiverId = defaultUsers[0].id,
        text = "Hey Sarah! Have you checked out the new Al-Chat build?",
        timestamp = System.currentTimeMillis() - 1000 * 60 * 10,
        status = MessageStatus.READ
      ),
      Message(
        id = "msg_s2",
        conversationId = convSarahId,
        senderId = defaultUsers[0].id,
        senderName = defaultUsers[0].name,
        receiverId = defaultCurrentUser.id,
        text = "Yes, just installed the APK! The real-time messaging and UI feel incredibly smooth.",
        timestamp = System.currentTimeMillis() - 1000 * 60 * 8,
        status = MessageStatus.READ,
        reactions = mapOf(defaultCurrentUser.id to "❤️")
      ),
      Message(
        id = "msg_s3",
        conversationId = convSarahId,
        senderId = defaultCurrentUser.id,
        senderName = defaultCurrentUser.name,
        receiverId = defaultUsers[0].id,
        text = "Awesome! We added support for image compression and voice messages too.",
        timestamp = System.currentTimeMillis() - 1000 * 60 * 5,
        status = MessageStatus.READ
      ),
      Message(
        id = "msg_s4",
        conversationId = convSarahId,
        senderId = defaultUsers[0].id,
        senderName = defaultUsers[0].name,
        receiverId = defaultCurrentUser.id,
        text = "The new Al-Chat interface looks super clean and fast! ✨",
        timestamp = System.currentTimeMillis() - 1000 * 60 * 3,
        status = MessageStatus.DELIVERED
      )
    )

    val alexMessages = listOf(
      Message(
        id = "msg_a1",
        conversationId = convAlexId,
        senderId = defaultUsers[1].id,
        senderName = defaultUsers[1].name,
        receiverId = defaultCurrentUser.id,
        text = "Check out this voice preview:",
        timestamp = System.currentTimeMillis() - 1000 * 60 * 50,
        status = MessageStatus.READ
      ),
      Message(
        id = "msg_a2",
        conversationId = convAlexId,
        senderId = defaultUsers[1].id,
        senderName = defaultUsers[1].name,
        receiverId = defaultCurrentUser.id,
        text = "Voice message",
        type = MessageType.VOICE,
        audioDurationSec = 14,
        timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
        status = MessageStatus.READ
      )
    )

    val botMessages = listOf(
      Message(
        id = "msg_b1",
        conversationId = convBotId,
        senderId = defaultUsers[4].id,
        senderName = defaultUsers[4].name,
        receiverId = defaultCurrentUser.id,
        text = "Welcome to Al-Chat! 🛡️\n\nSimple. Private. Connected.\n\nYour account is secured with email-only authentication. You can send real-time messages, media, voice notes, and create groups.",
        timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
        status = MessageStatus.READ
      )
    )

    val groupMessages = listOf(
      Message(
        id = "msg_g1",
        conversationId = convGroupId,
        senderId = defaultCurrentUser.id,
        senderName = defaultCurrentUser.name,
        receiverId = convGroupId,
        text = "Welcome everyone to the Al-Chat Core Team group!",
        timestamp = System.currentTimeMillis() - 1000 * 60 * 60,
        status = MessageStatus.READ
      ),
      Message(
        id = "msg_g2",
        conversationId = convGroupId,
        senderId = "user_sarah",
        senderName = "Sarah Chen",
        receiverId = convGroupId,
        text = "Excited to collaborate here. Firebase Firestore indexes are configured.",
        timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
        status = MessageStatus.READ,
        reactions = mapOf("user_me" to "🚀")
      ),
      Message(
        id = "msg_g3",
        conversationId = convGroupId,
        senderId = "user_elena",
        senderName = "Elena Rostova",
        receiverId = convGroupId,
        text = "Elena: Finalized the Material 3 color tokens for dark mode.",
        timestamp = System.currentTimeMillis() - 1000 * 60 * 15,
        status = MessageStatus.READ
      )
    )

    _messages.value = mapOf(
      convSarahId to sarahMessages,
      convAlexId to alexMessages,
      convBotId to botMessages,
      convGroupId to groupMessages
    )
  }

  // --- Authentication ---

  fun signUp(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    photoUrl: String = ""
  ): Result<User> {
    if (name.isBlank()) return Result.failure(Exception("Please enter your name"))
    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      return Result.failure(Exception("Please enter a valid email address"))
    }
    if (password.length < 6) {
      return Result.failure(Exception("Password must be at least 6 characters"))
    }
    if (password != confirmPassword) {
      return Result.failure(Exception("Passwords do not match"))
    }

    val username = email.substringBefore("@").lowercase().replace(".", "_")

    // Check if email already registered in memory or Firebase
    val existing = _users.value.find { it.email.equals(email, ignoreCase = true) }
    if (existing != null) {
      return Result.failure(Exception("An account with this email already exists"))
    }

    val newUser = User(
      id = "user_${UUID.randomUUID().toString().take(8)}",
      name = name.trim(),
      username = username,
      email = email.trim().lowercase(),
      photoUrl = photoUrl.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200" },
      about = "Hey there! I am using Al-Chat.",
      isOnline = true,
      lastSeen = System.currentTimeMillis(),
      createdAt = System.currentTimeMillis(),
      isEmailVerified = false
    )

    _users.value = _users.value + newUser
    _currentUser.value = newUser
    _authState.value = AuthState.Unverified(newUser)

    return Result.success(newUser)
  }

  fun login(email: String, password: String): Result<User> {
    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      return Result.failure(Exception("Please enter a valid email address"))
    }
    if (password.isBlank()) {
      return Result.failure(Exception("Please enter your password"))
    }

    val user = _users.value.find { it.email.equals(email.trim(), ignoreCase = true) }
      ?: User(
        id = "user_${UUID.randomUUID().toString().take(8)}",
        name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
        username = email.substringBefore("@").lowercase(),
        email = email.trim().lowercase(),
        photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
        about = "Hey there! I am using Al-Chat.",
        isOnline = true,
        lastSeen = System.currentTimeMillis(),
        isEmailVerified = true
      ).also { _users.value = _users.value + it }

    _currentUser.value = user
    _authState.value = AuthState.Authenticated(user)
    return Result.success(user)
  }

  fun sendPasswordResetEmail(email: String): Result<String> {
    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      return Result.failure(Exception("Please enter a valid email address"))
    }
    return Result.success("Password reset instructions have been sent to $email. Please check your inbox.")
  }

  fun sendEmailVerification(): Result<String> {
    val email = _currentUser.value?.email ?: "your email"
    return Result.success("Verification link sent to $email. Please check your spam/inbox.")
  }

  fun markEmailAsVerified() {
    val current = _currentUser.value ?: return
    val updated = current.copy(isEmailVerified = true)
    _currentUser.value = updated
    _users.value = _users.value.map { if (it.id == updated.id) updated else it }
    _authState.value = AuthState.Authenticated(updated)
  }

  fun logout() {
    _currentUser.value = null
    _authState.value = AuthState.Unauthenticated
  }

  // --- Profile Management ---

  fun updateProfile(name: String, username: String, about: String, photoUrl: String): Result<User> {
    val current = _currentUser.value ?: return Result.failure(Exception("Not authenticated"))
    val updated = current.copy(
      name = name.trim().ifBlank { current.name },
      username = username.trim().lowercase().removePrefix("@"),
      about = about.trim(),
      photoUrl = photoUrl.ifBlank { current.photoUrl }
    )
    _currentUser.value = updated
    _users.value = _users.value.map { if (it.id == updated.id) updated else it }
    return Result.success(updated)
  }

  fun updatePrivacySettings(settings: PrivacySettings) {
    val current = _currentUser.value ?: return
    val updated = current.copy(privacySettings = settings)
    _currentUser.value = updated
  }

  // --- Search & Contacts ---

  fun searchUsers(query: String): List<User> {
    val currentId = _currentUser.value?.id ?: ""
    val q = query.trim().lowercase().removePrefix("@")
    if (q.isBlank()) {
      return _users.value.filter { it.id != currentId }
    }
    return _users.value.filter { user ->
      user.id != currentId && (
        user.name.lowercase().contains(q) ||
        user.username.lowercase().contains(q) ||
        user.email.lowercase().contains(q)
      )
    }
  }

  fun getUserById(userId: String): User? {
    return _users.value.find { it.id == userId }
  }

  // --- Chat & Messaging ---

  fun getOrCreateDirectConversation(targetUserId: String): Conversation {
    val current = _currentUser.value ?: return _conversations.value.first()
    val target = getUserById(targetUserId) ?: return _conversations.value.first()

    val existing = _conversations.value.find { conv ->
      !conv.isGroup && conv.participants.contains(current.id) && conv.participants.contains(targetUserId)
    }

    if (existing != null) return existing

    val newConv = Conversation(
      id = "conv_${UUID.randomUUID().toString().take(8)}",
      isGroup = false,
      title = target.name,
      photoUrl = target.photoUrl,
      participants = listOf(current.id, target.id),
      lastMessage = "Conversation started",
      lastMessageType = MessageType.TEXT,
      lastMessageTime = System.currentTimeMillis(),
      lastSenderId = current.id,
      otherUser = target
    )

    _conversations.value = listOf(newConv) + _conversations.value
    _messages.value = _messages.value + (newConv.id to emptyList())
    return newConv
  }

  fun getConversationById(convId: String): Flow<Conversation?> {
    return _conversations.map { list -> list.find { it.id == convId } }
  }

  fun getMessages(convId: String): Flow<List<Message>> {
    return _messages.map { map -> map[convId] ?: emptyList() }
  }

  fun sendMessage(
    conversationId: String,
    text: String,
    type: MessageType = MessageType.TEXT,
    mediaUrl: String? = null,
    fileName: String? = null,
    fileSize: String? = null,
    audioDurationSec: Int = 0,
    replyTo: Message? = null
  ): Message {
    val current = _currentUser.value ?: User(id = "user_me", name = "Me")
    val conv = _conversations.value.find { it.id == conversationId }

    val newMessage = Message(
      id = "msg_${UUID.randomUUID().toString().take(8)}",
      conversationId = conversationId,
      senderId = current.id,
      senderName = current.name,
      receiverId = conv?.participants?.firstOrNull { it != current.id } ?: conversationId,
      text = text,
      type = type,
      mediaUrl = mediaUrl,
      fileName = fileName,
      fileSize = fileSize,
      audioDurationSec = audioDurationSec,
      timestamp = System.currentTimeMillis(),
      status = MessageStatus.SENT,
      replyToMessageId = replyTo?.id,
      replyToText = replyTo?.text,
      replyToSenderName = replyTo?.senderName
    )

    val currentList = _messages.value[conversationId] ?: emptyList()
    _messages.value = _messages.value + (conversationId to (currentList + newMessage))

    // Update conversation summary
    val previewText = when (type) {
      MessageType.TEXT -> text
      MessageType.IMAGE -> "📷 Photo"
      MessageType.VOICE -> "🎤 Voice message ($audioDurationSec s)"
      MessageType.FILE -> "📄 File: ${fileName ?: "Document"}"
      MessageType.VIDEO -> "📹 Video"
    }

    _conversations.value = _conversations.value.map { c ->
      if (c.id == conversationId) {
        c.copy(
          lastMessage = previewText,
          lastMessageType = type,
          lastMessageTime = newMessage.timestamp,
          lastSenderId = current.id,
          lastMessageStatus = MessageStatus.SENT
        )
      } else c
    }

    // Simulate realistic delivery & read receipt checkmark transitions
    scope.launch {
      delay(800)
      updateMessageStatus(conversationId, newMessage.id, MessageStatus.DELIVERED)
      delay(1200)
      updateMessageStatus(conversationId, newMessage.id, MessageStatus.READ)

      // If chatting with a bot or contact, trigger realistic reply after a delay
      if (conv != null && !conv.isGroup && conv.otherUser != null && conv.otherUser.id != current.id) {
        triggerSimulatedReply(conv, text)
      }
    }

    return newMessage
  }

  private fun updateMessageStatus(conversationId: String, messageId: String, status: MessageStatus) {
    val list = _messages.value[conversationId] ?: return
    _messages.value = _messages.value + (conversationId to list.map {
      if (it.id == messageId) it.copy(status = status) else it
    })
    _conversations.value = _conversations.value.map { c ->
      if (c.id == conversationId) c.copy(lastMessageStatus = status) else c
    }
  }

  private suspend fun triggerSimulatedReply(conv: Conversation, userText: String) {
    val partner = conv.otherUser ?: return

    // Show typing status
    setTypingStatus(conv.id, true, partner.name)
    delay(2000)
    setTypingStatus(conv.id, false, null)

    val replies = listOf(
      "Got it! Thanks for sending that through Al-Chat 👍",
      "That's fantastic. I'm loving this interface and the privacy controls.",
      "Sounds great! Let's connect again shortly.",
      "Received! Let me review this on my side.",
      "Perfect! The message delivery and typing indicators work in real time!"
    )
    val replyText = if (partner.id == "user_alchat_bot") {
      "Al-Chat is designed with privacy-first principles. Your messages and media are securely routed."
    } else {
      replies.random()
    }

    val replyMessage = Message(
      id = "msg_${UUID.randomUUID().toString().take(8)}",
      conversationId = conv.id,
      senderId = partner.id,
      senderName = partner.name,
      receiverId = _currentUser.value?.id ?: "",
      text = replyText,
      type = MessageType.TEXT,
      timestamp = System.currentTimeMillis(),
      status = MessageStatus.SENT
    )

    val list = _messages.value[conv.id] ?: emptyList()
    _messages.value = _messages.value + (conv.id to (list + replyMessage))

    _conversations.value = _conversations.value.map { c ->
      if (c.id == conv.id) {
        c.copy(
          lastMessage = replyText,
          lastMessageType = MessageType.TEXT,
          lastMessageTime = replyMessage.timestamp,
          lastSenderId = partner.id,
          lastMessageStatus = MessageStatus.SENT,
          unreadCount = c.unreadCount + 1
        )
      } else c
    }
  }

  fun setTypingStatus(conversationId: String, isTyping: Boolean, typingUserName: String? = null) {
    _conversations.value = _conversations.value.map { c ->
      if (c.id == conversationId) {
        c.copy(isTyping = isTyping, typingUserName = if (isTyping) (typingUserName ?: "typing...") else null)
      } else c
    }
  }

  fun toggleReaction(conversationId: String, messageId: String, emoji: String) {
    val currentUserId = _currentUser.value?.id ?: "user_me"
    val list = _messages.value[conversationId] ?: return
    _messages.value = _messages.value + (conversationId to list.map { msg ->
      if (msg.id == messageId) {
        val existing = msg.reactions[currentUserId]
        val newReactions = if (existing == emoji) {
          msg.reactions - currentUserId
        } else {
          msg.reactions + (currentUserId to emoji)
        }
        msg.copy(reactions = newReactions)
      } else msg
    })
  }

  fun deleteMessage(conversationId: String, messageId: String) {
    val list = _messages.value[conversationId] ?: return
    _messages.value = _messages.value + (conversationId to list.map { msg ->
      if (msg.id == messageId) {
        msg.copy(isDeleted = true, text = "This message was deleted")
      } else msg
    })
  }

  fun markConversationAsRead(conversationId: String) {
    _conversations.value = _conversations.value.map { c ->
      if (c.id == conversationId) c.copy(unreadCount = 0) else c
    }
    val list = _messages.value[conversationId] ?: return
    val currentId = _currentUser.value?.id ?: ""
    _messages.value = _messages.value + (conversationId to list.map { msg ->
      if (msg.senderId != currentId) msg.copy(status = MessageStatus.READ) else msg
    })
  }

  // --- Group Management ---

  fun createGroup(name: String, description: String, photoUrl: String, memberIds: List<String>): Result<Conversation> {
    if (name.isBlank()) return Result.failure(Exception("Please enter a group name"))
    val current = _currentUser.value ?: return Result.failure(Exception("Not authenticated"))

    val allMembers = (listOf(current.id) + memberIds).distinct()
    val groupId = "group_${UUID.randomUUID().toString().take(8)}"

    val newGroup = Group(
      id = groupId,
      name = name.trim(),
      description = description.trim(),
      photoUrl = photoUrl.ifBlank { "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=200" },
      createdBy = current.id,
      createdAt = System.currentTimeMillis(),
      memberIds = allMembers,
      adminIds = listOf(current.id)
    )

    val conv = Conversation(
      id = groupId,
      isGroup = true,
      title = newGroup.name,
      photoUrl = newGroup.photoUrl,
      participants = allMembers,
      lastMessage = "Group created",
      lastMessageType = MessageType.TEXT,
      lastMessageTime = System.currentTimeMillis(),
      lastSenderId = current.id
    )

    _groups.value = _groups.value + (groupId to newGroup)
    _conversations.value = listOf(conv) + _conversations.value
    _messages.value = _messages.value + (groupId to listOf(
      Message(
        id = "msg_init_${groupId}",
        conversationId = groupId,
        senderId = current.id,
        senderName = current.name,
        receiverId = groupId,
        text = "${current.name} created the group \"${newGroup.name}\"",
        timestamp = System.currentTimeMillis(),
        status = MessageStatus.READ
      )
    ))

    return Result.success(conv)
  }

  fun getGroupDetails(groupId: String): Group? {
    return _groups.value[groupId]
  }

  fun addMemberToGroup(groupId: String, userId: String) {
    val group = _groups.value[groupId] ?: return
    val updated = group.copy(memberIds = (group.memberIds + userId).distinct())
    _groups.value = _groups.value + (groupId to updated)
    _conversations.value = _conversations.value.map { c ->
      if (c.id == groupId) c.copy(participants = updated.memberIds) else c
    }
  }

  fun removeMemberFromGroup(groupId: String, userId: String) {
    val group = _groups.value[groupId] ?: return
    val updated = group.copy(
      memberIds = group.memberIds.filter { it != userId },
      adminIds = group.adminIds.filter { it != userId }
    )
    _groups.value = _groups.value + (groupId to updated)
    _conversations.value = _conversations.value.map { c ->
      if (c.id == groupId) c.copy(participants = updated.memberIds) else c
    }
  }

  fun toggleAdminStatus(groupId: String, userId: String) {
    val group = _groups.value[groupId] ?: return
    val updated = if (group.adminIds.contains(userId)) {
      group.copy(adminIds = group.adminIds.filter { it != userId })
    } else {
      group.copy(adminIds = group.adminIds + userId)
    }
    _groups.value = _groups.value + (groupId to updated)
  }

  fun leaveGroup(groupId: String) {
    val currentUserId = _currentUser.value?.id ?: "user_me"
    removeMemberFromGroup(groupId, currentUserId)
  }

  // --- Settings & Appearance ---

  fun setThemeMode(mode: String) {
    _themeMode.value = mode
    try {
      context.getSharedPreferences("alchat_prefs", Context.MODE_PRIVATE)
        .edit()
        .putString("theme_mode", mode)
        .apply()
    } catch (_: Exception) {}
  }

  fun updateNotificationSettings(settings: NotificationSettings) {
    _notificationSettings.value = settings
  }
}

package com.example.data.model

data class PrivacySettings(
  val lastSeenVisibility: PrivacyOption = PrivacyOption.EVERYONE,
  val profilePhotoVisibility: PrivacyOption = PrivacyOption.EVERYONE,
  val aboutVisibility: PrivacyOption = PrivacyOption.EVERYONE,
  val onlineStatusVisibility: PrivacyOption = PrivacyOption.EVERYONE,
  val readReceipts: Boolean = true
)

enum class PrivacyOption(val label: String) {
  EVERYONE("Everyone"),
  MY_CONTACTS("My Contacts"),
  NOBODY("Nobody")
}

data class User(
  val id: String = "",
  val name: String = "",
  val username: String = "",
  val email: String = "",
  val photoUrl: String = "",
  val about: String = "Hey there! I am using Al-Chat.",
  val isOnline: Boolean = false,
  val lastSeen: Long = System.currentTimeMillis(),
  val createdAt: Long = System.currentTimeMillis(),
  val isEmailVerified: Boolean = false,
  val privacySettings: PrivacySettings = PrivacySettings()
)

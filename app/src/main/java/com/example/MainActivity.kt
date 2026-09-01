package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.AlChatRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AlChatApp()
    }
  }
}

@Composable
fun AlChatApp() {
  val repository = remember { AlChatRepository.getInstance() }
  val themeMode by repository.themeMode.collectAsState()
  val currentUser by repository.currentUser.collectAsState()

  val isDarkTheme = when (themeMode) {
    "DARK" -> true
    "LIGHT" -> false
    else -> isSystemInDarkTheme()
  }

  MyApplicationTheme(darkTheme = isDarkTheme) {
    val navController = rememberNavController()

    NavHost(
      navController = navController,
      startDestination = "splash"
    ) {
      composable("splash") {
        SplashScreen(
          onNavigateNext = {
            if (currentUser != null) {
              navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
              }
            } else {
              navController.navigate("welcome") {
                popUpTo("splash") { inclusive = true }
              }
            }
          }
        )
      }

      composable("welcome") {
        WelcomeScreen(
          onNavigateToLogin = { navController.navigate("login") },
          onNavigateToRegister = { navController.navigate("register") }
        )
      }

      composable("login") {
        LoginScreen(
          repository = repository,
          onNavigateToRegister = { navController.navigate("register") },
          onNavigateToForgotPassword = { navController.navigate("forgot_password") },
          onLoginSuccess = {
            navController.navigate("home") {
              popUpTo("welcome") { inclusive = true }
            }
          }
        )
      }

      composable("register") {
        RegisterScreen(
          repository = repository,
          onNavigateToLogin = { navController.navigate("login") },
          onRegisterSuccess = {
            navController.navigate("verify_email") {
              popUpTo("welcome") { inclusive = true }
            }
          }
        )
      }

      composable("forgot_password") {
        ForgotPasswordScreen(
          repository = repository,
          onNavigateBack = { navController.popBackStack() }
        )
      }

      composable("verify_email") {
        EmailVerificationScreen(
          repository = repository,
          onVerificationComplete = {
            navController.navigate("home") {
              popUpTo("welcome") { inclusive = true }
            }
          }
        )
      }

      composable("home") {
        HomeScreen(
          repository = repository,
          onNavigateToChat = { convId -> navController.navigate("chat/$convId") },
          onNavigateToGroupChat = { groupId -> navController.navigate("chat/$groupId") },
          onNavigateToSearch = { navController.navigate("user_search") },
          onNavigateToCreateGroup = { navController.navigate("create_group") },
          onNavigateToProfile = { navController.navigate("user_profile/me") },
          onNavigateToSettings = { navController.navigate("settings") },
          onLogout = {
            navController.navigate("welcome") {
              popUpTo("home") { inclusive = true }
            }
          }
        )
      }

      composable(
        route = "chat/{conversationId}",
        arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
      ) { backStackEntry ->
        val convId = backStackEntry.arguments?.getString("conversationId") ?: ""
        ChatScreen(
          conversationId = convId,
          repository = repository,
          onNavigateBack = { navController.popBackStack() },
          onNavigateToUserProfile = { targetUserId -> navController.navigate("user_profile/$targetUserId") },
          onNavigateToGroupInfo = { targetGroupId -> navController.navigate("group_info/$targetGroupId") }
        )
      }

      composable("user_search") {
        UserSearchScreen(
          repository = repository,
          onNavigateBack = { navController.popBackStack() },
          onUserSelected = { convId ->
            navController.navigate("chat/$convId") {
              popUpTo("user_search") { inclusive = true }
            }
          }
        )
      }

      composable("create_group") {
        CreateGroupScreen(
          repository = repository,
          onNavigateBack = { navController.popBackStack() },
          onGroupCreated = { groupId ->
            navController.navigate("chat/$groupId") {
              popUpTo("create_group") { inclusive = true }
            }
          }
        )
      }

      composable(
        route = "group_info/{groupId}",
        arguments = listOf(navArgument("groupId") { type = NavType.StringType })
      ) { backStackEntry ->
        val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
        GroupInfoScreen(
          groupId = groupId,
          repository = repository,
          onNavigateBack = { navController.popBackStack() }
        )
      }

      composable(
        route = "user_profile/{userId}",
        arguments = listOf(navArgument("userId") { type = NavType.StringType })
      ) { backStackEntry ->
        val userId = backStackEntry.arguments?.getString("userId")
        val effectiveId = if (userId == "me") null else userId
        UserProfileScreen(
          userId = effectiveId,
          repository = repository,
          onNavigateBack = { navController.popBackStack() },
          onNavigateToEditProfile = { navController.navigate("edit_profile") }
        )
      }

      composable("edit_profile") {
        EditProfileScreen(
          repository = repository,
          onNavigateBack = { navController.popBackStack() }
        )
      }

      composable("settings") {
        SettingsScreen(
          repository = repository,
          onNavigateBack = { navController.popBackStack() },
          onNavigateToProfile = { navController.navigate("user_profile/me") },
          onNavigateToPrivacy = { navController.navigate("privacy_settings") },
          onNavigateToNotifications = { navController.navigate("notification_settings") },
          onNavigateToAppearance = { navController.navigate("appearance_settings") },
          onLogout = {
            navController.navigate("welcome") {
              popUpTo("home") { inclusive = true }
            }
          }
        )
      }

      composable("privacy_settings") {
        PrivacySettingsScreen(
          repository = repository,
          onNavigateBack = { navController.popBackStack() }
        )
      }

      composable("notification_settings") {
        NotificationSettingsScreen(
          repository = repository,
          onNavigateBack = { navController.popBackStack() }
        )
      }

      composable("appearance_settings") {
        AppearanceSettingsScreen(
          repository = repository,
          onNavigateBack = { navController.popBackStack() }
        )
      }
    }
  }
}

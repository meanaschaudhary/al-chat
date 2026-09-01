package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.AlChatRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onNavigateNext: () -> Unit
) {
  LaunchedEffect(Unit) {
    delay(400)
    onNavigateNext()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(100.dp)
          .clip(RoundedCornerShape(24.dp))
          .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.app_logo),
          contentDescription = "Al-Chat Logo",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Al-Chat",
        style = MaterialTheme.typography.displayLarge.copy(
          color = MaterialTheme.colorScheme.onBackground,
          fontWeight = FontWeight.Bold
        )
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Simple. Private. Connected.",
        style = MaterialTheme.typography.titleMedium.copy(
          color = MaterialTheme.colorScheme.primary,
          letterSpacing = 1.sp
        )
      )
    }

    // Bottom Branding indicator
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "POWERED BY FIREBASE ARCHITECTURE",
        style = MaterialTheme.typography.labelSmall.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          letterSpacing = 1.5.sp
        )
      )
    }
  }
}

@Composable
fun WelcomeScreen(
  onNavigateToLogin: () -> Unit,
  onNavigateToRegister: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Spacer(modifier = Modifier.height(20.dp))

      // Hero Branding Block
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          modifier = Modifier
            .size(110.dp)
            .clip(RoundedCornerShape(28.dp))
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(28.dp)),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Al-Chat Logo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = "Al-Chat",
          style = MaterialTheme.typography.displayMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Simple. Private. Connected.",
          style = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Feature Highlights
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surface,
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            FeatureItem(
              icon = Icons.Default.Email,
              title = "Email-Only Authentication",
              desc = "No phone numbers required. Complete privacy control."
            )
            FeatureItem(
              icon = Icons.Default.Bolt,
              title = "Real-Time Cloud Sync",
              desc = "Instant messaging, live typing, and read receipts."
            )
            FeatureItem(
              icon = Icons.Default.Lock,
              title = "Cloud Firestore & Storage",
              desc = "Fast media sharing with secure database rules."
            )
          }
        }
      }

      // Buttons Action Section
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 32.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Button(
          onClick = onNavigateToRegister,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
        ) {
          Text(
            text = "Create Account",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }

        OutlinedButton(
          onClick = onNavigateToLogin,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
        ) {
          Text(
            text = "Login",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold
            )
          )
        }

        Text(
          text = "By continuing, you agree to Al-Chat Terms & Privacy Policy",
          style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
        )
      }
    }
  }
}

@Composable
private fun FeatureItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  desc: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(20.dp)
      )
    }
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
      )
      Text(
        text = desc,
        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
  repository: AlChatRepository,
  onNavigateToRegister: () -> Unit,
  onNavigateToForgotPassword: () -> Unit,
  onLoginSuccess: () -> Unit
) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  val focusManager = LocalFocusManager.current

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Login to Al-Chat", fontWeight = FontWeight.Bold) },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(24.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "Welcome Back",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
      )

      Text(
        text = "Sign in securely with your email address to access your private conversations.",
        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
      )

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = email,
        onValueChange = {
          email = it
          errorMessage = null
        },
        label = { Text("Email Address") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      OutlinedTextField(
        value = password,
        onValueChange = {
          password = it
          errorMessage = null
        },
        label = { Text("Password") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
          IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
              imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
              contentDescription = "Toggle password visibility"
            )
          }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        TextButton(onClick = onNavigateToForgotPassword) {
          Text("Forgot Password?", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
      }

      AnimatedVisibility(visible = errorMessage != null) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.errorContainer,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = errorMessage ?: "",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onErrorContainer),
            modifier = Modifier.padding(12.dp)
          )
        }
      }

      Button(
        onClick = {
          focusManager.clearFocus()
          isLoading = true
          val res = repository.login(email, password)
          isLoading = false
          if (res.isSuccess) {
            onLoginSuccess()
          } else {
            errorMessage = res.exceptionOrNull()?.message ?: "Login failed"
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        enabled = !isLoading
      ) {
        if (isLoading) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
        } else {
          Text("Login", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary))
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Don't have an account?", style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onNavigateToRegister) {
          Text("Create Account", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
  repository: AlChatRepository,
  onNavigateToLogin: () -> Unit,
  onRegisterSuccess: () -> Unit
) {
  var name by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var photoUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200") }
  var passwordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  val avatarOptions = listOf(
    "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
    "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200",
    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200"
  )

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let {
      photoUrl = it.toString()
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Create Account", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateToLogin) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(24.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text(
        text = "Join Al-Chat",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
      )

      Text(
        text = "Pick an avatar or upload from your phone to complete your profile.",
        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
      )

      // Avatar Selector Row
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier.clickable {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          contentAlignment = Alignment.BottomEnd
        ) {
          UserAvatar(
            photoUrl = photoUrl,
            name = name.ifBlank { "Al-Chat" },
            size = 80.dp,
            showOnlineBadge = false
          )
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Upload Photo",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(15.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        TextButton(
          onClick = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          }
        ) {
          Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Upload from Phone", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("Or pick a preset avatar style:", style = MaterialTheme.typography.labelSmall)
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.padding(top = 6.dp)
        ) {
          avatarOptions.forEach { url ->
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(
                  width = if (photoUrl == url) 2.dp else 1.dp,
                  color = if (photoUrl == url) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                  shape = CircleShape
                )
                .clickable { photoUrl = url }
            ) {
              UserAvatar(photoUrl = url, name = "A", size = 40.dp, showOnlineBadge = false)
            }
          }
        }
      }

      OutlinedTextField(
        value = name,
        onValueChange = { name = it; errorMessage = null },
        label = { Text("Full Name") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      OutlinedTextField(
        value = email,
        onValueChange = { email = it; errorMessage = null },
        label = { Text("Email Address") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      OutlinedTextField(
        value = password,
        onValueChange = { password = it; errorMessage = null },
        label = { Text("Password (min 6 characters)") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
          IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
              imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
              contentDescription = null
            )
          }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      OutlinedTextField(
        value = confirmPassword,
        onValueChange = { confirmPassword = it; errorMessage = null },
        label = { Text("Confirm Password") },
        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      AnimatedVisibility(visible = errorMessage != null) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.errorContainer,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = errorMessage ?: "",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onErrorContainer),
            modifier = Modifier.padding(12.dp)
          )
        }
      }

      Button(
        onClick = {
          isLoading = true
          val res = repository.signUp(name, email, password, confirmPassword, photoUrl)
          isLoading = false
          if (res.isSuccess) {
            onRegisterSuccess()
          } else {
            errorMessage = res.exceptionOrNull()?.message ?: "Registration failed"
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        enabled = !isLoading
      ) {
        if (isLoading) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
        } else {
          Text("Create Account", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary))
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Already have an account?", style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onNavigateToLogin) {
          Text("Login", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
  repository: AlChatRepository,
  onNavigateBack: () -> Unit
) {
  var email by remember { mutableStateOf("") }
  var message by remember { mutableStateOf<String?>(null) }
  var isSuccess by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "Forgot Your Password?",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
      )

      Text(
        text = "Enter your registered email address and we'll send you instructions to reset your password.",
        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
      )

      OutlinedTextField(
        value = email,
        onValueChange = { email = it; message = null },
        label = { Text("Email Address") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      if (message != null) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = message ?: "",
            style = MaterialTheme.typography.bodySmall.copy(
              color = if (isSuccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = Modifier.padding(12.dp)
          )
        }
      }

      Button(
        onClick = {
          val res = repository.sendPasswordResetEmail(email)
          if (res.isSuccess) {
            isSuccess = true
            message = res.getOrNull()
          } else {
            isSuccess = false
            message = res.exceptionOrNull()?.message
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text("Send Reset Link", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
  repository: AlChatRepository,
  onVerificationComplete: () -> Unit
) {
  val context = LocalContext.current
  val user by repository.currentUser.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(title = { Text("Verify Email", fontWeight = FontWeight.Bold) })
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(90.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.MarkEmailRead,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(48.dp)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "Verify Your Email",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "We have sent a verification email to:\n${user?.email ?: "your email"}",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
      )

      Spacer(modifier = Modifier.height(28.dp))

      Button(
        onClick = {
          repository.markEmailAsVerified()
          Toast.makeText(context, "Email verified successfully!", Toast.LENGTH_SHORT).show()
          onVerificationComplete()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text("I Have Verified My Email", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedButton(
        onClick = {
          val res = repository.sendEmailVerification()
          Toast.makeText(context, res.getOrNull() ?: "Verification email sent", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Resend Verification Email", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

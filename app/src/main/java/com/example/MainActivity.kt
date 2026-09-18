package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppWorkspaceMode
import com.example.model.UserRole
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainMarketScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WorkspacePromptScreen
import com.example.ui.theme.NaseebLalMarketTheme
import com.example.util.NotificationHelper
import com.example.viewmodel.MarketViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize notification channel for real-time market updates
    NotificationHelper.createNotificationChannel(applicationContext)

    setContent {
      NaseebLalMarketTheme {
        // Request POST_NOTIFICATIONS runtime permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
          ) { /* permission result handled */ }

          LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.POST_NOTIFICATIONS
              ) != PackageManager.PERMISSION_GRANTED
            ) {
              permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
          }
        }

        Surface(modifier = Modifier.fillMaxSize()) {
          val viewModel: MarketViewModel = viewModel()
          val currentUser by viewModel.currentUser.collectAsState()
          val hasSelectedWorkspace by viewModel.hasSelectedWorkspace.collectAsState()
          var showSplash by remember { mutableStateOf(true) }

          Crossfade(
            targetState = showSplash,
            animationSpec = tween(durationMillis = 400),
            label = "SplashCrossfade"
          ) { isSplash ->
            if (isSplash) {
              SplashScreen(
                onSplashFinished = {
                  showSplash = false
                }
              )
            } else {
              val user = currentUser
              if (user == null) {
                LoginScreen(
                  onLogin = { username, password, role ->
                    viewModel.login(username, password, role)
                  }
                )
              } else {
                // If user has rights to personal data (Admin or Sub-Admin with permission)
                // and hasn't chosen a workspace yet in this session, show prompt
                val canChooseWorkspace = user.isAdmin || user.canManagePersonalTenants
                if (canChooseWorkspace && !hasSelectedWorkspace) {
                  WorkspacePromptScreen(
                    user = user,
                    onSelectWorkspace = { mode ->
                      viewModel.selectWorkspace(mode)
                    },
                    onLogout = {
                      viewModel.logout()
                    }
                  )
                } else {
                  MainMarketScreen(
                    viewModel = viewModel,
                    currentUser = user
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}


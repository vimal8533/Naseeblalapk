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
import android.content.Intent
import android.net.Uri
import com.example.model.AppWorkspaceMode
import com.example.model.RentRecord
import com.example.model.UserRole
import com.example.ui.components.TenantEchoPortalDialog
import com.example.ui.dialogs.TenantPortalLookupDialog
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainMarketScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WorkspacePromptScreen
import com.example.ui.theme.NaseebLalMarketTheme
import com.example.util.NotificationHelper
import com.example.viewmodel.MarketViewModel

class MainActivity : ComponentActivity() {
  private var deepLinkEchoSlug by mutableStateOf<String?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    extractDeepLink(intent)

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
          val allRents by viewModel.rents.collectAsState()
          val allTenants by viewModel.tenants.collectAsState()
          val echoRecords by viewModel.tenantEchoRecords.collectAsState()

          var showSplash by remember { mutableStateOf(true) }
          var activeTenantEchoRent by remember { mutableStateOf<RentRecord?>(null) }
          var showTenantLookupDialog by remember { mutableStateOf(false) }

          // Auto-open portal dialog when launched via deep link
          LaunchedEffect(deepLinkEchoSlug, allRents) {
            val slug = deepLinkEchoSlug?.trim()
            if (!slug.isNullOrBlank()) {
              val matchedRent = allRents.find { rent ->
                val expectedSlug = "${rent.tenantId}_${rent.month}_${rent.year}".replace(" ", "_")
                expectedSlug.equals(slug, ignoreCase = true) || rent.id == slug
              } ?: allRents.find { rent ->
                slug.contains(rent.tenantId, ignoreCase = true) ||
                (slug.contains(rent.shopNumber, ignoreCase = true) && slug.contains(rent.month, ignoreCase = true))
              }
              if (matchedRent != null) {
                activeTenantEchoRent = matchedRent
                showSplash = false // Skip splash on direct deep link
              }
            }
          }

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

          // Standalone Tenant Portal Dialog (Deep Link or Self-Service Lookup)
          if (activeTenantEchoRent != null) {
            val rent = activeTenantEchoRent!!
            val tenant = allTenants.find { it.id == rent.tenantId }
            val echoKey = "${rent.month}_${rent.year}_${rent.tenantId}".replace(" ", "_")
            val echoRecord = echoRecords[echoKey]

            TenantEchoPortalDialog(
              rent = rent,
              tenant = tenant,
              echoRecord = echoRecord,
              onDismiss = {
                activeTenantEchoRent = null
                deepLinkEchoSlug = null
              },
              onSubmitPromiseDate = { promisedDate, note ->
                viewModel.submitTenantPromiseDate(rent.id, promisedDate, note)
              },
              onSubmitClaimPaid = { refNote ->
                viewModel.submitTenantClaimPaid(rent.id, refNote)
              },
              onAdminVerifyPayment = null // Tenant view mode - no admin actions
            )
          }

          // Tenant Self-Service Unit Lookup Dialog
          if (showTenantLookupDialog) {
            TenantPortalLookupDialog(
              rents = allRents,
              onDismiss = { showTenantLookupDialog = false },
              onSelectRent = { selectedRent ->
                showTenantLookupDialog = false
                activeTenantEchoRent = selectedRent
              }
            )
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    extractDeepLink(intent)
  }

  private fun extractDeepLink(intent: Intent?) {
    val uri: Uri? = intent?.data
    if (uri != null) {
      val echoParam = uri.getQueryParameter("echo")
      val slug = if (!echoParam.isNullOrBlank()) {
        echoParam
      } else {
        val lastSegment = uri.lastPathSegment
        if (lastSegment != null && lastSegment != "portal") lastSegment else null
      }
      if (!slug.isNullOrBlank()) {
        deepLinkEchoSlug = slug
      }
    }
  }
}


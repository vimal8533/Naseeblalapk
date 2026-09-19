package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SyncStatus
import com.example.model.AppWorkspaceMode
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.ui.components.MarketLogoMedallion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPending
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    user: UserSession?,
    syncStatus: SyncStatus,
    lastSyncedAt: Long,
    totalShops: Int,
    totalTenants: Int,
    workspaceMode: AppWorkspaceMode = AppWorkspaceMode.PUBLIC_MARKET,
    onToggleWorkspace: () -> Unit = {},
    onForceRefresh: () -> Unit,
    onLogout: () -> Unit,
    onUpdateAdminProfile: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditAdminDialog by remember { mutableStateOf(false) }
    var editAdminName by remember(user) { mutableStateOf(user?.displayName ?: "Vimal Kumar") }
    var editAdminPhone by remember(user) { mutableStateOf(user?.phone ?: "9876543210") }

    val lastSyncStr = remember(lastSyncedAt) {
        if (lastSyncedAt > 0) {
            val sdf = SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.getDefault())
            sdf.format(Date(lastSyncedAt))
        } else "Just now"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Market Logo & User Identity
        MarketLogoMedallion(size = 90.dp, showSubtext = false)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = user?.displayName ?: "User",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Role Badge
        val isSuperAdmin = user?.role == UserRole.ADMIN
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSuperAdmin) GoldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 1.dp,
                    color = if (isSuperAdmin) GoldAccent else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isSuperAdmin) Icons.Filled.Shield else Icons.Filled.Person,
                    contentDescription = null,
                    tint = if (isSuperAdmin) GoldAccent else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSuperAdmin) "Master Admin (Owner)" else "Sub-Admin",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isSuperAdmin) GoldAccent else MaterialTheme.colorScheme.primary
                )
            }
        }

        Text(
            text = "Username: ${user?.username ?: ""}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Contact / Reminder Phone Number
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Phone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Reminder Contact: ${user?.phone?.ifBlank { if (isSuperAdmin) "9876543210" else "None" } ?: ""}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isSuperAdmin) {
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        editAdminName = user?.displayName ?: "Vimal Kumar"
                        editAdminPhone = user?.phone?.ifBlank { "9876543210" } ?: "9876543210"
                        showEditAdminDialog = true
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Contact",
                        tint = GoldAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Workspace Mode Indicator & Switcher (if user has personal property rights)
        val canAccessPersonal = user != null && (user.isAdmin || user.canManagePersonalTenants)
        if (canAccessPersonal) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (workspaceMode == AppWorkspaceMode.PRIVATE_PERSONAL)
                        Color(0xFF0F766E).copy(alpha = 0.1f)
                    else
                        NavyPrimary.copy(alpha = 0.08f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (workspaceMode == AppWorkspaceMode.PRIVATE_PERSONAL) Color(0xFF0F766E).copy(alpha = 0.35f) else GoldAccent.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (workspaceMode == AppWorkspaceMode.PRIVATE_PERSONAL)
                                        Color(0xFF0F766E)
                                    else
                                        NavyDark
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (workspaceMode == AppWorkspaceMode.PRIVATE_PERSONAL)
                                    Icons.Filled.Apartment
                                else
                                    Icons.Filled.Store,
                                contentDescription = null,
                                tint = if (workspaceMode == AppWorkspaceMode.PRIVATE_PERSONAL) Color.White else GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (workspaceMode == AppWorkspaceMode.PRIVATE_PERSONAL)
                                    "Private / Personal Flat Mode"
                                else
                                    "Public Market Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (workspaceMode == AppWorkspaceMode.PRIVATE_PERSONAL)
                                    Color(0xFF0F766E)
                                else
                                    NavyPrimary
                            )
                            Text(
                                text = if (workspaceMode == AppWorkspaceMode.PRIVATE_PERSONAL)
                                    "Showing only your private properties & tenants"
                                else
                                    "Showing Naseeb Lal Market commercial shops",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onToggleWorkspace,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SwapHoriz,
                            contentDescription = "Switch Mode",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Switch",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Realtime Cloud Sync Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CloudDone,
                            contentDescription = null,
                            tint = StatusPaid,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSuperAdmin) "Firebase Cloud Sync" else "Live Data Synchronization",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusPaid.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusPaid
                        )
                    }
                }

                if (isSuperAdmin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Database URL:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "https://nasseblalmarkt-default-rtdb.firebaseio.com/",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Last Synced: $lastSyncStr",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onForceRefresh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("force_sync_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isSuperAdmin) "Sync Database Now" else "Sync Now", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Market Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Naseeb Lal Market Overview",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Total Shops:", fontSize = 13.sp)
                    Text(text = "$totalShops", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Total Tenants:", fontSize = 13.sp)
                    Text(text = "$totalTenants", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Multi-device Sync:", fontSize = 13.sp)
                    Text(text = "Instant / Realtime", fontWeight = FontWeight.Bold, color = StatusPaid, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button"),
            colors = ButtonDefaults.buttonColors(containerColor = StatusPending),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ExitToApp,
                contentDescription = "Logout",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "LOGOUT",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout Confirmation") },
            text = { Text("Are you sure you want to logout from Naseeb Lal Market?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusPending)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditAdminDialog) {
        AlertDialog(
            onDismissRequest = { showEditAdminDialog = false },
            title = {
                Text("Edit Admin Reminder Contact", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Set the sender name and phone number that will appear on rent reminders sent by you.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editAdminName,
                        onValueChange = { editAdminName = it },
                        label = { Text("Admin Name") },
                        placeholder = { Text("Vimal Kumar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editAdminPhone,
                        onValueChange = { editAdminPhone = it },
                        label = { Text("Contact Mobile Number") },
                        placeholder = { Text("9876543210") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditAdminDialog = false
                        onUpdateAdminProfile?.invoke(editAdminName.trim(), editAdminPhone.trim())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Save Details")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditAdminDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

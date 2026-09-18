package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActivityLog
import com.example.model.SubAdminUser
import com.example.ui.components.ActivityLogDialog
import com.example.ui.components.AddSubAdminDialog
import com.example.ui.components.ChangeSubAdminPasswordDialog
import com.example.ui.components.EditSubAdminDialog
import com.example.ui.components.SubAdminsSkeleton
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPending
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubAdminsScreen(
    subAdmins: List<SubAdminUser>,
    activityLogs: List<ActivityLog> = emptyList(),
    onAddSubAdmin: (username: String, password: String, name: String, phone: String, canManagePersonalTenants: Boolean) -> Result<Unit>,
    onUpdateSubAdmin: (subAdminId: String, name: String, phone: String, canManagePersonalTenants: Boolean) -> Result<Unit> = { _, _, _, _ -> Result.success(Unit) },
    onChangePassword: (subAdminId: String, newPass: String) -> Result<Unit>,
    onDeleteSubAdmin: (subAdminId: String) -> Result<Unit>,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var subAdminToEdit by remember { mutableStateOf<SubAdminUser?>(null) }
    var subAdminToDelete by remember { mutableStateOf<SubAdminUser?>(null) }
    var subAdminToChangePass by remember { mutableStateOf<SubAdminUser?>(null) }
    var showLogsDialog by remember { mutableStateOf(false) }
    var logsUserFilter by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            SubAdminsSkeleton()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
            // Header Info & Audit Controls (Compact & Organized)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Security,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Staff & Access Control",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${activityLogs.size} edits/actions logged",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                logsUserFilter = null
                                showLogsDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("view_all_edits_button")
                        ) {
                            Icon(imageVector = Icons.Filled.History, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("All Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Sub-Admins count & Add button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sub-Admins (${subAdmins.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_subadmin_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add New", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // List of Sub-Admins
            if (subAdmins.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No sub-admins added yet.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Add Sub-Admin")
                            }
                        }
                    }
                }
            } else {
                items(subAdmins, key = { it.id }) { subAdmin ->
                    SubAdminCardItem(
                        subAdmin = subAdmin,
                        activityLogs = activityLogs,
                        onViewLogs = {
                            logsUserFilter = subAdmin.name
                            showLogsDialog = true
                        },
                        onEdit = { subAdminToEdit = subAdmin },
                        onChangePassword = { subAdminToChangePass = subAdmin },
                        onDelete = { subAdminToDelete = subAdmin },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
        }

        // FAB to add sub-admin
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = GoldAccent,
            contentColor = NavyDark,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_subadmin_fab")
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Sub Admin")
        }
    }

    // Add Sub-Admin Dialog
    if (showAddDialog) {
        AddSubAdminDialog(
            onDismiss = { showAddDialog = false },
            onSave = onAddSubAdmin
        )
    }

    // Edit Sub-Admin Dialog
    subAdminToEdit?.let { sa ->
        EditSubAdminDialog(
            subAdmin = sa,
            onDismiss = { subAdminToEdit = null },
            onSave = { subAdminId, name, phone, canPersonal ->
                val res = onUpdateSubAdmin(subAdminId, name, phone, canPersonal)
                if (res.isSuccess) {
                    subAdminToEdit = null
                }
                res
            }
        )
    }

    // Change Password Dialog
    subAdminToChangePass?.let { sa ->
        ChangeSubAdminPasswordDialog(
            subAdmin = sa,
            onDismiss = { subAdminToChangePass = null },
            onSaveNewPassword = { subAdminId, newPass ->
                val res = onChangePassword(subAdminId, newPass)
                if (res.isSuccess) {
                    subAdminToChangePass = null
                }
                res
            }
        )
    }

    // Activity Log Viewer Dialog
    if (showLogsDialog) {
        ActivityLogDialog(
            logs = activityLogs,
            initialUserFilter = logsUserFilter,
            onDismiss = { showLogsDialog = false }
        )
    }

    // Delete confirmation dialog
    subAdminToDelete?.let { sa ->
        AlertDialog(
            onDismissRequest = { subAdminToDelete = null },
            title = { Text("Remove Sub-Admin") },
            text = {
                Text("Are you sure you want to remove '${sa.name} (${sa.username})'? They will no longer be able to log in.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSubAdmin(sa.id)
                        subAdminToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusPending)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { subAdminToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SubAdminCardItem(
    subAdmin: SubAdminUser,
    activityLogs: List<ActivityLog>,
    onViewLogs: () -> Unit,
    onEdit: () -> Unit,
    onChangePassword: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val createdDateStr = remember(subAdmin.createdAt) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        sdf.format(Date(subAdmin.createdAt))
    }

    // Online status: Active within last 2 minutes
    val isOnline = remember(subAdmin.lastActiveAt) {
        subAdmin.lastActiveAt > 0L && (System.currentTimeMillis() - subAdmin.lastActiveAt) < 2 * 60 * 1000
    }

    val lastSeenText = remember(subAdmin.lastActiveAt) {
        if (subAdmin.lastActiveAt <= 0L) "Offline"
        else {
            val diffMs = System.currentTimeMillis() - subAdmin.lastActiveAt
            val diffMins = diffMs / 60000
            if (diffMins < 60) "Active ${diffMins}m ago"
            else {
                val hours = diffMins / 60
                if (hours < 24) "Active ${hours}h ago"
                else SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(subAdmin.lastActiveAt))
            }
        }
    }

    // Logs filtered for this user
    val userLogs = remember(activityLogs, subAdmin) {
        activityLogs.filter { log ->
            log.userName.equals(subAdmin.name, ignoreCase = true) ||
            log.userName.equals(subAdmin.username, ignoreCase = true) ||
            log.userName.contains(subAdmin.name, ignoreCase = true)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("subadmin_card_${subAdmin.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Main Top Row: Avatar + Info + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Compact Avatar with Status Dot
                Box(modifier = Modifier.size(38.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Online / Offline Status Dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) Color(0xFF16A34A) else Color(0xFF9CA3AF))
                            .border(1.5.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Name, Username & Phone Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = subAdmin.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (isOnline) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .border(0.5.dp, Color(0xFF86EFAC), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF16A34A))
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Online",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        } else {
                            Text(
                                text = "• $lastSeenText",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "@${subAdmin.username}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (subAdmin.phone.isNotBlank()) {
                            Text(
                                text = " • ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Icon(
                                imageVector = Icons.Filled.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = subAdmin.phone,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    if (subAdmin.canManagePersonalTenants) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F766E).copy(alpha = 0.12f))
                                .border(0.5.dp, Color(0xFF0F766E).copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .clickable { onEdit() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.HomeWork,
                                contentDescription = null,
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Personal Flats: Allowed",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F766E)
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .clickable { onEdit() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.HomeWork,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Personal Flats: Off",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(9.dp)
                            )
                        }
                    }
                }

                // Actions: Edit & Delete (Compact)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("edit_subadmin_${subAdmin.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Sub-Admin",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_subadmin_${subAdmin.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = StatusPending,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Organized Strip: Password & Logs Trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Password with inline Change button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pass: ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = subAdmin.password,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(
                        onClick = onChangePassword,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(
                            text = "Change",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Activity / Edits Trigger Pill
                Button(
                    onClick = onViewLogs,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(26.dp)
                        .testTag("view_user_logs_${subAdmin.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (userLogs.isNotEmpty()) "${userLogs.size} Edits" else "0 Edits",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

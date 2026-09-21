package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SyncStatus
import com.example.model.AppWorkspaceMode
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPending

@Composable
fun MarketTopBar(
    user: UserSession?,
    syncStatus: SyncStatus,
    onRefresh: () -> Unit,
    workspaceMode: AppWorkspaceMode = AppWorkspaceMode.PUBLIC_MARKET,
    onToggleWorkspace: (() -> Unit)? = null,
    unreadNoticeCount: Int = 0,
    onOpenNotifications: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isPersonalMode = workspaceMode == AppWorkspaceMode.PRIVATE_PERSONAL
    val canSwitch = user != null && (user.isAdmin || user.canManagePersonalTenants) && onToggleWorkspace != null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    if (isPersonalMode) {
                        listOf(Color(0xFF042F2E), Color(0xFF0F172A))
                    } else {
                        listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                    }
                )
            )
            .statusBarsPadding()
            .border(
                width = 0.5.dp,
                color = if (isPersonalMode) Color(0xFF0F766E).copy(alpha = 0.6f) else Color(0xFF334155).copy(alpha = 0.6f),
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: App Logo Badge + Title
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarketLogoMedallion(
                    size = 38.dp,
                    showSubtext = false
                )
                Spacer(modifier = Modifier.width(9.dp))
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = if (isPersonalMode) "Private Workspace" else "Naseeb Lal Market",
                        color = Color.White,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.2.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Role Chip
                        val isSuperAdmin = user?.role == UserRole.ADMIN
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSuperAdmin) GoldAccent.copy(alpha = 0.2f) else Color(0xFF334155))
                                .border(
                                    width = 0.5.dp,
                                    color = if (isSuperAdmin) GoldAccent.copy(alpha = 0.6f) else Color(0xFF475569),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSuperAdmin) {
                                    Icon(
                                        imageVector = Icons.Filled.Shield,
                                        contentDescription = "Admin",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = if (isSuperAdmin) "Master Admin" else "Sub-Admin",
                                    color = if (isSuperAdmin) GoldAccent else Color(0xFFE2E8F0),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (!user?.displayName.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            // Logged-in user name
                            Text(
                                text = user?.displayName ?: "",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Mode Switcher (if allowed) + Cloud Sync indicator & Refresh Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Mode Switcher Pill (Clickable button to easily toggle between Public Market and Private Flats)
                if (canSwitch) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isPersonalMode) Color(0xFF0F766E) else Color(0xFF1E3A5F)
                            )
                            .border(
                                1.dp,
                                if (isPersonalMode) Color(0xFF2DD4BF) else GoldAccent.copy(alpha = 0.7f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onToggleWorkspace?.invoke() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("toggle_workspace_mode_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPersonalMode) Icons.Filled.Apartment else Icons.Filled.Store,
                                contentDescription = null,
                                tint = if (isPersonalMode) Color.White else GoldAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPersonalMode) "Private" else "Public",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = "Switch",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // Live Cloud Status Pill
                val (syncColor, syncText) = when (syncStatus) {
                    SyncStatus.SYNCED -> Pair(StatusPaid, "Live")
                    SyncStatus.SYNCING -> Pair(GoldAccent, "Syncing...")
                    SyncStatus.OFFLINE -> Pair(Color(0xFF94A3B8), "Offline")
                    SyncStatus.ERROR -> Pair(StatusPending, "Retry")
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .clickable { onRefresh() }
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(syncColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = syncText,
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Notice Board / Announcements Bell Button
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (unreadNoticeCount > 0) GoldAccent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f))
                        .border(
                            0.5.dp,
                            if (unreadNoticeCount > 0) GoldAccent else Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onOpenNotifications?.invoke() }
                        .testTag("notice_board_topbar_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notice Board",
                        tint = if (unreadNoticeCount > 0) GoldAccent else Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )

                    if (unreadNoticeCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        )
                    }
                }

                // Explicit bounds refresh button
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onRefresh)
                        .testTag("refresh_sync_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Sync Cloud",
                        tint = GoldAccent,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

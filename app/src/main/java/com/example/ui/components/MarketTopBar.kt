package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SyncStatus
import com.example.model.AppWorkspaceMode
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.ui.theme.GoldAccent
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

    // Background gradient depending on Public Market or Private Flats
    val bgBrush = if (isPersonalMode) {
        Brush.horizontalGradient(listOf(Color(0xFF042F2E), Color(0xFF0F172A)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF0A1128), Color(0xFF1E293B)))
    }

    val borderColor = if (isPersonalMode) {
        Color(0xFF14B8A6).copy(alpha = 0.35f)
    } else {
        Color(0xFF334155).copy(alpha = 0.6f)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgBrush)
                .statusBarsPadding()
                .border(width = 0.5.dp, color = borderColor, shape = RoundedCornerShape(0.dp))
                .padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ==================== LEFT: Logo + Title + Role ====================
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App Emblem Logo with subtle border
                    Image(
                        painter = painterResource(id = R.drawable.market_logo),
                        contentDescription = "Naseeb Lal Market Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, GoldAccent.copy(alpha = 0.5f), CircleShape)
                    )

                    Spacer(modifier = Modifier.width(9.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Title
                        Text(
                            text = if (isPersonalMode) "Private Flats" else "Naseeb Lal Market",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.2.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Subtitle: Role Chip + User Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val isSuperAdmin = user?.role == UserRole.ADMIN
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSuperAdmin) GoldAccent.copy(alpha = 0.18f) else Color(0xFF334155))
                                    .border(
                                        width = 0.5.dp,
                                        color = if (isSuperAdmin) GoldAccent.copy(alpha = 0.5f) else Color(0xFF475569),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSuperAdmin) {
                                        Icon(
                                            imageVector = Icons.Filled.Shield,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(9.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = if (isSuperAdmin) "Admin" else "Sub-Admin",
                                        color = if (isSuperAdmin) GoldAccent else Color(0xFFCBD5E1),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (!user?.displayName.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "• ${user?.displayName}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // ==================== RIGHT: Actions Row ====================
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. Workspace Switcher (Market vs Flats) - Compact & Elegant
                    if (canSwitch) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isPersonalMode) Color(0xFF0F766E).copy(alpha = 0.3f) else Color(0xFF1E3A5F).copy(alpha = 0.5f)
                                )
                                .border(
                                    1.dp,
                                    if (isPersonalMode) Color(0xFF2DD4BF) else GoldAccent.copy(alpha = 0.6f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = { onToggleWorkspace() }
                                )
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("toggle_workspace_mode_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPersonalMode) Icons.Filled.Apartment else Icons.Filled.Store,
                                    contentDescription = null,
                                    tint = if (isPersonalMode) Color(0xFF2DD4BF) else GoldAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPersonalMode) "Flats" else "Market",
                                    color = Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 2. Notice Bell Button with Badge
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (unreadNoticeCount > 0) GoldAccent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f)
                            )
                            .border(
                                0.5.dp,
                                if (unreadNoticeCount > 0) GoldAccent else Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { onOpenNotifications?.invoke() }
                            )
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
                                    .padding(3.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                        }
                    }

                    // 3. Unified Live Sync & Refresh Action Pill
                    val (syncColor, syncLabel) = when (syncStatus) {
                        SyncStatus.SYNCED -> Pair(StatusPaid, "Live")
                        SyncStatus.SYNCING -> Pair(GoldAccent, "Sync...")
                        SyncStatus.OFFLINE -> Pair(Color(0xFF94A3B8), "Offline")
                        SyncStatus.ERROR -> Pair(StatusPending, "Retry")
                    }

                    val infiniteTransition = rememberInfiniteTransition(label = "syncRotation")
                    val rotationAngle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onRefresh
                            )
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("refresh_sync_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (syncStatus == SyncStatus.SYNCING) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Syncing",
                                    tint = GoldAccent,
                                    modifier = Modifier
                                        .size(13.dp)
                                        .rotate(rotationAngle)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(syncColor)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = syncLabel,
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ActivityLog
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPending
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityLogDialog(
    logs: List<ActivityLog>,
    initialUserFilter: String? = null,
    onDismiss: () -> Unit
) {
    var selectedUserFilter by remember { mutableStateOf(initialUserFilter ?: "ALL") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    // Distinct users from logs
    val availableUsers = remember(logs) {
        listOf("ALL") + logs.map { it.userName }.distinct().sorted()
    }

    val filteredLogs = remember(logs, selectedUserFilter, selectedCategoryFilter) {
        logs.filter { log ->
            val matchesUser = selectedUserFilter == "ALL" || log.userName.equals(selectedUserFilter, ignoreCase = true)
            val matchesCategory = when (selectedCategoryFilter) {
                "SHOPS" -> log.actionType.contains("SHOP", ignoreCase = true)
                "TENANTS" -> log.actionType.contains("TENANT", ignoreCase = true)
                "RENTS" -> log.actionType.contains("RENT", ignoreCase = true) || log.actionType.contains("COLLECT", ignoreCase = true)
                "SUBADMINS" -> log.actionType.contains("SUB_ADMIN", ignoreCase = true)
                else -> true
            }
            matchesUser && matchesCategory
        }.sortedByDescending { it.timestamp }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("activity_log_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyPrimary)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.History,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (initialUserFilter != null) "$initialUserFilter's Edits & Activity" else "User Activity & Edit Logs",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${filteredLogs.size} edits recorded",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Filter Rows
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    // Category Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val categories = listOf(
                            "ALL" to "All Categories",
                            "TENANTS" to "Tenants",
                            "SHOPS" to "Shops",
                            "RENTS" to "Rent Collection",
                            "SUBADMINS" to "Sub-Admins"
                        )
                        items(categories) { (key, label) ->
                            val isSelected = selectedCategoryFilter == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryFilter = key },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // User filter chips if "ALL" initial
                    if (availableUsers.size > 2 && initialUserFilter == null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(availableUsers) { user ->
                                val isSelected = selectedUserFilter == user
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedUserFilter = user },
                                    label = {
                                        Text(
                                            if (user == "ALL") "All Users" else "@$user",
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldAccent,
                                        selectedLabelColor = NavyDark
                                    )
                                )
                            }
                        }
                    }
                }

                // Logs List
                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No edits or activity recorded yet.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Any shop edits, tenant updates, or rent collections by sub-users will show up here.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredLogs, key = { it.id }) { log ->
                            ActivityLogCard(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityLogCard(log: ActivityLog) {
    val dateStr = remember(log.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    val (icon, iconBg, iconTint) = remember(log.actionType) {
        when {
            log.actionType.contains("RENT", ignoreCase = true) || log.actionType.contains("COLLECT", ignoreCase = true) ->
                Triple(Icons.Filled.Payments, StatusPaidBg, StatusPaid)
            log.actionType.contains("SHOP", ignoreCase = true) ->
                Triple(Icons.Filled.Store, NavyPrimary.copy(alpha = 0.1f), NavyPrimary)
            log.actionType.contains("TENANT", ignoreCase = true) ->
                Triple(Icons.Filled.People, GoldAccent.copy(alpha = 0.2f), Color(0xFFB45309))
            log.actionType.contains("SUB_ADMIN", ignoreCase = true) ->
                Triple(Icons.Filled.Security, Color(0xFFEDE9FE), Color(0xFF7C3AED))
            else ->
                Triple(Icons.Filled.Edit, Color.LightGray.copy(alpha = 0.3f), Color.DarkGray)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("activity_item_${log.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Action Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Header: User Name + Role Badge + Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = log.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Role badge
                        val isAdminRole = log.userRole.contains("ADMIN", ignoreCase = true) && !log.userRole.contains("SUB", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isAdminRole) GoldAccent.copy(alpha = 0.25f) else Color(0xFFE0E7FF))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (isAdminRole) "Master Admin" else "Sub-Admin",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAdminRole) NavyDark else Color(0xFF3730A3)
                            )
                        }
                    }

                    Text(
                        text = dateStr,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = log.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Details (if any)
                if (log.details.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = log.details,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

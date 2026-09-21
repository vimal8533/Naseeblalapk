package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.MarketNotice
import com.example.model.PmcTaxClearanceRecord
import com.example.model.UserSession
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPending
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationPanelDialog(
    notices: List<MarketNotice>,
    currentUser: UserSession?,
    isPmcTaxDue: Boolean = false,
    currentPmcClearance: PmcTaxClearanceRecord? = null,
    currentFinancialYear: String = "2026-2027",
    onDismiss: () -> Unit,
    onPostNotice: (title: String, message: String, category: String, priority: String, dueDate: String) -> Unit,
    onDeleteNotice: (noticeId: String) -> Unit,
    onMarkPmcTaxPaid: (amount: Double, receiptNumber: String, paidDate: String, paymentMode: String, notes: String) -> Unit = { _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showMarkPmcPaidDialog by remember { mutableStateOf(false) }
    var noticeToDelete by remember { mutableStateOf<MarketNotice?>(null) }

    val filteredNotices = remember(notices, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") {
            notices
        } else {
            notices.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, GoldAccent.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .testTag("notification_panel_dialog"),
            color = NavyDark,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent.copy(alpha = 0.15f))
                                    .border(1.dp, GoldAccent.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Campaign,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Notice Board",
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(GoldAccent.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${notices.size}",
                                            color = GoldAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "PMC Tax, Bijli Bill, Kiraya & Suchna",
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF334155), thickness = 0.8.dp)

                // Action Bar: "+ Post Notice" + Category Filter chips
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Post Notice Button
                    Button(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("post_notice_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = NavyDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ Naya Notice / Suchna Jodein",
                            color = NavyDark,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ANNUAL PMC HOLDING TAX BANNER (1 April - 30 Sept)
                    if (isPmcTaxDue) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.2.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(GoldAccent.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.AccountBalance,
                                                contentDescription = null,
                                                tint = GoldAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "ANNUAL PMC TAX DUE",
                                                color = GoldAccent,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "FY $currentFinancialYear (1 Apr - 30 Sept Window)",
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                                            .border(0.5.dp, Color(0xFFEF4444), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "🚨 DUE NOW",
                                            color = Color(0xFFFCA5A5),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Naseeb Lal Market ka annual PMC holding tax jama karne ka samay shuru hai. Agar tax jama ho gaya hai toh kripya turant amount aur receipt darj karein.",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { showMarkPmcPaidDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "✓ Mark as Paid with Amount",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    } else if (currentPmcClearance?.isPaid == true) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(0.8.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "✅ FY $currentFinancialYear PMC Tax Paid",
                                            color = Color(0xFF34D399),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "₹${currentPmcClearance.amountPaid.toInt()} on ${currentPmcClearance.paidDate} by ${currentPmcClearance.paidBy}${if (currentPmcClearance.receiptNumber.isNotBlank()) " • Receipt: ${currentPmcClearance.receiptNumber}" else ""}",
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = { showMarkPmcPaidDialog = true },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Edit", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Category Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val categories = listOf(
                            "ALL" to "Sabhi",
                            "TAX" to "🏛️ PMC Tax",
                            "ELECTRICITY" to "⚡ Bijli Bill",
                            "RENT" to "💰 Kiraya",
                            "MAINTENANCE" to "🛠️ Maintenance",
                            "GENERAL" to "📢 Aam Suchna"
                        )
                        items(categories) { (key, label) ->
                            val isSelected = selectedCategoryFilter.equals(key, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) GoldAccent.copy(alpha = 0.25f) else Color(0xFF1E293B)
                                    )
                                    .border(
                                        width = if (isSelected) 1.dp else 0.5.dp,
                                        color = if (isSelected) GoldAccent else Color(0xFF475569),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedCategoryFilter = key }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.5f), thickness = 0.5.dp)

                // Notice List or Empty State
                if (filteredNotices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (selectedCategoryFilter == "ALL") "Koi Notice Nahi Hai" else "Is Category me koi notice nahi hai",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Naya notice post karne ke liye upar diye gaye button par click karein.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredNotices, key = { it.id }) { notice ->
                            NoticeItemCard(
                                notice = notice,
                                currentUser = currentUser,
                                onShare = {
                                    shareNoticeOnWhatsApp(context, notice)
                                },
                                onDelete = {
                                    noticeToDelete = notice
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for deleting a notice
    if (noticeToDelete != null) {
        AlertDialog(
            onDismissRequest = { noticeToDelete = null },
            title = {
                Text("Notice Delete Karein?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Kya aap waqai is notice ko delete karna chahte hain? Yeh sabhi users ke panel se hat jayega.",
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        noticeToDelete?.let { onDeleteNotice(it.id) }
                        noticeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noticeToDelete = null }) {
                    Text("Radd Karein", color = Color.White.copy(alpha = 0.7f))
                }
            },
            containerColor = NavyDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialog for creating a new notice
    if (showCreateDialog) {
        CreateNoticeDialog(
            onDismiss = { showCreateDialog = false },
            onPost = { title, message, category, priority, dueDate ->
                onPostNotice(title, message, category, priority, dueDate)
                showCreateDialog = false
                Toast.makeText(context, "Notice safalta-purvak post ho gaya!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog for marking PMC tax as paid
    if (showMarkPmcPaidDialog) {
        MarkPmcTaxPaidDialog(
            financialYear = currentFinancialYear,
            initialClearance = currentPmcClearance,
            currentUser = currentUser,
            onDismiss = { showMarkPmcPaidDialog = false },
            onSubmit = { amount, receiptNumber, paidDate, paymentMode, notes ->
                onMarkPmcTaxPaid(amount, receiptNumber, paidDate, paymentMode, notes)
                showMarkPmcPaidDialog = false
                Toast.makeText(context, "PMC Tax payment safalta-purvak darj ho gaya!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun NoticeItemCard(
    notice: MarketNotice,
    currentUser: UserSession?,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val canDelete = currentUser?.isAdmin == true || currentUser?.displayName.equals(notice.authorName, ignoreCase = true)
    val formattedDate = remember(notice.createdAt) {
        try {
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(notice.createdAt))
        } catch (_: Exception) {
            ""
        }
    }

    val (badgeBg, badgeBorder, badgeTextColor) = when (notice.category.uppercase()) {
        "TAX", "PMC", "PMC_TAX" -> Triple(GoldAccent.copy(alpha = 0.18f), GoldAccent.copy(alpha = 0.6f), GoldAccent)
        "ELECTRICITY" -> Triple(Color(0xFF06B6D4).copy(alpha = 0.18f), Color(0xFF06B6D4).copy(alpha = 0.6f), Color(0xFF22D3EE))
        "RENT" -> Triple(Color(0xFF10B981).copy(alpha = 0.18f), Color(0xFF10B981).copy(alpha = 0.6f), Color(0xFF34D399))
        "MAINTENANCE" -> Triple(Color(0xFF8B5CF6).copy(alpha = 0.18f), Color(0xFF8B5CF6).copy(alpha = 0.6f), Color(0xFFA78BFA))
        else -> Triple(Color(0xFF64748B).copy(alpha = 0.18f), Color(0xFF64748B).copy(alpha = 0.6f), Color(0xFFCBD5E1))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (notice.isUrgent) 1.2.dp else 0.6.dp,
                color = if (notice.isUrgent) Color(0xFFEF4444) else Color(0xFF334155),
                shape = RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Category Badge + Priority + Delete icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg)
                            .border(0.5.dp, badgeBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = notice.categoryDisplay,
                            color = badgeTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Urgent tag if priority is URGENT
                    if (notice.isUrgent) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                                .border(0.5.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "🚨 URGENT",
                                color = Color(0xFFFCA5A5),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // WhatsApp Share Icon
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Delete Icon
                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notice Title
            Text(
                text = notice.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp
            )

            // Due Date if applicable
            if (notice.dueDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F172A))
                        .border(0.5.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Event,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Antim Tithi (Due): ${notice.dueDate}",
                        color = GoldAccent,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notice Message Body
            Text(
                text = notice.message,
                color = Color(0xFFE2E8F0),
                fontSize = 13.sp,
                lineHeight = 18.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.6f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // Footer: Author & Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Likhit: ${notice.authorName} (${if (notice.authorRole == "ADMIN") "Master Admin" else "Sub-Admin"})",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = formattedDate,
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun CreateNoticeDialog(
    onDismiss: () -> Unit,
    onPost: (title: String, message: String, category: String, priority: String, dueDate: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("TAX") }
    var priority by remember { mutableStateOf("NORMAL") }
    var dueDate by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
            color = NavyDark,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Dialog Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Naya Notice Jodein",
                        color = Color.White,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Selection
                Text(
                    text = "Category Chunein:",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                val presetCategories = listOf(
                    Triple("TAX", "🏛️ PMC Tax", "Varshik PMC Holding Tax jama karne hetu notice"),
                    Triple("ELECTRICITY", "⚡ Bijli Bill", "Meter reading aur bijli bill bhugtan hetu suchna"),
                    Triple("RENT", "💰 Kiraya Notice", "Masik ya varshik kiraya jama karne ka anurodh"),
                    Triple("GENERAL", "📢 Aam Suchna", "Market samay, chhutti ya zaroori guidelines")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetCategories.forEach { (catKey, catLabel, defaultMsg) ->
                        val isSelected = category == catKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GoldAccent.copy(alpha = 0.25f) else Color(0xFF1E293B))
                                .border(
                                    width = if (isSelected) 1.dp else 0.5.dp,
                                    color = if (isSelected) GoldAccent else Color(0xFF475569),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    category = catKey
                                    if (title.isBlank()) {
                                        title = when (catKey) {
                                            "TAX" -> "Annual PMC Holding Tax (₹1,000) Jama Karein"
                                            "ELECTRICITY" -> "Bijli Meter Reading & Bill Bhugtan"
                                            "RENT" -> "Kiraya Bhugtan Hetu Suchna"
                                            else -> "Zaroori Suchna - Naseeb Lal Market"
                                        }
                                    }
                                    if (message.isBlank()) {
                                        message = defaultMsg
                                    }
                                }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = catLabel,
                                color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.8f),
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notice ka Title / Vishay") },
                    placeholder = { Text("e.g. Annual PMC Tax Jama Karein") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = GoldAccent,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Due Date Input (Optional)
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Antim Tithi / Due Date (Optional)") },
                    placeholder = { Text("e.g. 31st March ya 10th of this month") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = GoldAccent,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Message Body Input
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Poori Jaankari / Message") },
                    placeholder = { Text("Yahan notice ki poori jaankari likhein...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = GoldAccent,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Priority Toggle (Normal vs Urgent)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Priority:",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val isNormal = priority == "NORMAL"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isNormal) Color(0xFF334155) else Color.Transparent)
                                .border(0.5.dp, if (isNormal) GoldAccent else Color(0xFF475569), RoundedCornerShape(8.dp))
                                .clickable { priority = "NORMAL" }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Samanya (Normal)",
                                color = if (isNormal) GoldAccent else Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }

                        val isUrgent = priority == "URGENT"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isUrgent) Color(0xFFEF4444).copy(alpha = 0.25f) else Color.Transparent)
                                .border(0.5.dp, if (isUrgent) Color(0xFFEF4444) else Color(0xFF475569), RoundedCornerShape(8.dp))
                                .clickable { priority = "URGENT" }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "🚨 Urgent Alert",
                                color = if (isUrgent) Color(0xFFFCA5A5) else Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = if (isUrgent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF475569))
                    ) {
                        Text("Radd Karein", color = Color.White.copy(alpha = 0.8f))
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                return@Button
                            }
                            onPost(title, message, category, priority, dueDate)
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Publish Karein 🚀", color = NavyDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun shareNoticeOnWhatsApp(context: Context, notice: MarketNotice) {
    val shareText = buildString {
        appendLine("📢 *NASEEB LAL MARKET - NOTICE BOARD*")
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("📌 *${notice.title}*")
        appendLine("🏷️ *Vibhag*: ${notice.categoryDisplay}")
        if (notice.dueDate.isNotBlank()) {
            appendLine("📅 *Antim Tithi (Due Date)*: ${notice.dueDate}")
        }
        appendLine()
        appendLine(notice.message)
        appendLine()
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("✍️ *Aadeshak*: ${notice.authorName} (${if (notice.authorRole == "ADMIN") "Master Admin" else "Sub-Admin"})")
        appendLine("Naseeb Lal Market Management")
    }

    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Notice Share Karein")
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Share karne me samasya aayi: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun MarkPmcTaxPaidDialog(
    financialYear: String,
    initialClearance: PmcTaxClearanceRecord?,
    currentUser: UserSession?,
    onDismiss: () -> Unit,
    onSubmit: (amount: Double, receiptNumber: String, paidDate: String, paymentMode: String, notes: String) -> Unit
) {
    var amountText by remember {
        mutableStateOf(
            if (initialClearance != null && initialClearance.amountPaid > 0)
                initialClearance.amountPaid.toInt().toString()
            else "15000"
        )
    }
    var receiptNumber by remember { mutableStateOf(initialClearance?.receiptNumber ?: "") }
    var paidDate by remember {
        mutableStateOf(
            if (!initialClearance?.paidDate.isNullOrBlank())
                initialClearance!!.paidDate
            else
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        )
    }
    var paymentMode by remember { mutableStateOf(initialClearance?.paymentMode?.ifBlank { "CASH" } ?: "CASH") }
    var notes by remember { mutableStateOf(initialClearance?.notes ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = NavyDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountBalance,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PMC Tax Mark as Paid",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Financial Year: $financialYear",
                                color = GoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Tax payment darj hote hi yeh alert sabhi Master & Sub-Admins ke phone se turant dismiss ho jayega aur green clearance record save ho jayega.",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Text Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Total Tax Amount Paid (₹) *", color = Color.White.copy(alpha = 0.8f)) },
                    placeholder = { Text("e.g. 15000", color = Color.White.copy(alpha = 0.4f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Receipt / Challan Number
                OutlinedTextField(
                    value = receiptNumber,
                    onValueChange = { receiptNumber = it },
                    label = { Text("PMC Receipt / Challan No. (Aicchik)", color = Color.White.copy(alpha = 0.8f)) },
                    placeholder = { Text("e.g. PMC/2026/8841", color = Color.White.copy(alpha = 0.4f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Paid Date
                OutlinedTextField(
                    value = paidDate,
                    onValueChange = { paidDate = it },
                    label = { Text("Payment Date", color = Color.White.copy(alpha = 0.8f)) },
                    placeholder = { Text("DD MMM YYYY", color = Color.White.copy(alpha = 0.4f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Payment Mode Selector
                Text("Payment Mode:", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("CASH" to "💵 Cash", "ONLINE" to "📱 Online", "CHEQUE" to "📑 Cheque").forEach { (mode, label) ->
                        val isSel = paymentMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) GoldPrimary.copy(alpha = 0.25f) else Color(0xFF1E293B))
                                .border(if (isSel) 1.dp else 0.5.dp, if (isSel) GoldPrimary else Color(0xFF475569), RoundedCornerShape(8.dp))
                                .clickable { paymentMode = mode }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) GoldAccent else Color.White.copy(alpha = 0.8f),
                                fontSize = 11.5.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Radd Karein", color = Color.White.copy(alpha = 0.8f))
                    }

                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            onSubmit(amt, receiptNumber, paidDate, paymentMode, notes)
                        },
                        enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Clear", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

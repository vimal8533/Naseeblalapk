package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.model.RentRecord
import com.example.model.Tenant
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPaidBorder
import com.example.ui.theme.StatusPartial
import com.example.ui.theme.StatusPartialBg
import com.example.ui.theme.StatusPartialBorder
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusPendingBorder
import java.util.Locale

@Composable
fun DeletedTenantsArchiveDialog(
    archivedTenants: List<Tenant>,
    allHistoricalRents: List<RentRecord>,
    initialMonth: String,
    initialYear: Int,
    onRestoreTenant: (tenantId: String) -> Unit,
    onPermanentlyDeleteTenant: (tenantId: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var selectedYear by remember { mutableIntStateOf(initialYear) }
    var searchQuery by remember { mutableStateOf("") }
    var tenantToPermanentDelete by remember { mutableStateOf<Tenant?>(null) }
    var tenantToRestore by remember { mutableStateOf<Tenant?>(null) }

    val monthsList = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val yearsList = listOf(2027, 2026, 2025, 2024, 2023, 2022)

    var yearDropdownExpanded by remember { mutableStateOf(false) }

    // Filter archived tenants by search
    val filteredTenants = remember(archivedTenants, searchQuery) {
        if (searchQuery.isBlank()) {
            archivedTenants
        } else {
            val q = searchQuery.trim().lowercase(Locale.getDefault())
            archivedTenants.filter {
                it.name.lowercase(Locale.getDefault()).contains(q) ||
                it.shopNumber.lowercase(Locale.getDefault()).contains(q) ||
                it.phone.contains(q) ||
                it.businessName.lowercase(Locale.getDefault()).contains(q)
            }
        }
    }

    // Historical rents for the selected month and year belonging to archived tenants
    val archivedTenantIds = remember(archivedTenants) { archivedTenants.map { it.id }.toSet() }
    val monthArchivedRents = remember(allHistoricalRents, archivedTenantIds, selectedMonth, selectedYear) {
        allHistoricalRents.filter {
            it.tenantId in archivedTenantIds &&
            it.month.equals(selectedMonth, ignoreCase = true) &&
            it.year == selectedYear
        }
    }

    val totalExpectedInMonth = remember(monthArchivedRents) {
        monthArchivedRents.sumOf { it.amountDue }
    }
    val totalCollectedInMonth = remember(monthArchivedRents) {
        monthArchivedRents.sumOf { it.amountPaid }
    }
    val totalPendingInMonth = remember(monthArchivedRents) {
        monthArchivedRents.sumOf { it.pendingAmount }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(22.dp))
                .testTag("deleted_tenants_archive_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // 1. Header with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF2F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.HistoryEdu,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Deleted / Exited Tenants Hisaab",
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                            Text(
                                text = "Past records & ledger are safely preserved here",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Month & Year Selector Controls
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, CardBorderLight)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📅 Select Hisaab Month & Year:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )

                            // Year Picker Dropdown Chip
                            Box {
                                OutlinedButton(
                                    onClick = { yearDropdownExpanded = true },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, CardBorderLight),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(
                                        text = "Year $selectedYear",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyDark
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = NavyDark
                                    )
                                }

                                DropdownMenu(
                                    expanded = yearDropdownExpanded,
                                    onDismissRequest = { yearDropdownExpanded = false }
                                ) {
                                    yearsList.forEach { y ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = y.toString(),
                                                    fontWeight = if (y == selectedYear) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                selectedYear = y
                                                yearDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Horizontal Month Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            items(monthsList) { m ->
                                val isSelected = m.equals(selectedMonth, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) NavyDark else MaterialTheme.colorScheme.surface)
                                        .border(
                                            1.dp,
                                            if (isSelected) NavyDark else CardBorderLight,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { selectedMonth = m }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = m.take(3),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Month Financial Summary for Archived/Deleted Tenants
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Total Archived Count
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Exited Tenants", fontSize = 9.5.sp, color = Color(0xFF475569))
                            Text("${archivedTenants.size}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                    }

                    // Expected in this Month
                    Card(
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Expected", fontSize = 9.5.sp, color = Color(0xFF1D4ED8))
                            Text("₹${formatAmount(totalExpectedInMonth)}", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                        }
                    }

                    // Collected in this Month
                    Card(
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = StatusPaidBg),
                        border = BorderStroke(1.dp, StatusPaidBorder)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Collected", fontSize = 9.5.sp, color = StatusPaid)
                            Text("₹${formatAmount(totalCollectedInMonth)}", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = StatusPaid)
                        }
                    }

                    // Pending / Dues in this Month
                    Card(
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = StatusPendingBg),
                        border = BorderStroke(1.dp, StatusPendingBorder)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Pending Dues", fontSize = 9.5.sp, color = StatusPending)
                            Text("₹${formatAmount(totalPendingInMonth)}", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = StatusPending)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search deleted tenant name, shop, phone...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 5. Tenant List
                if (filteredTenants.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(46.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (archivedTenants.isEmpty()) "Abhi tak koi tenant delete/archive nahi kiya gaya hai." else "No matches found for '$searchQuery'",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Jab koi kirayedar dukaan/flat chhodega ya delete hoga, uska poora hisaab bina active screen ko disturb kiye yahan safe rahega.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredTenants, key = { it.id }) { tenant ->
                            // Find rent record for the selected month/year for this tenant
                            val rentForMonth = monthArchivedRents.firstOrNull { it.tenantId == tenant.id }
                            // All rent records ever recorded for this tenant
                            val allRentsForTenant = remember(allHistoricalRents, tenant.id) {
                                allHistoricalRents.filter { it.tenantId == tenant.id }
                            }

                            ArchivedTenantCard(
                                tenant = tenant,
                                rentForSelectedMonth = rentForMonth,
                                allRents = allRentsForTenant,
                                selectedMonth = selectedMonth,
                                selectedYear = selectedYear,
                                onCall = {
                                    if (tenant.phone.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${tenant.phone}"))
                                        context.startActivity(intent)
                                    }
                                },
                                onWhatsApp = {
                                    if (tenant.phone.isNotBlank()) {
                                        val cleanPhone = tenant.phone.replace(Regex("[^0-9]"), "")
                                        val waUrl = "https://wa.me/91$cleanPhone"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                                        context.startActivity(intent)
                                    }
                                },
                                onRestore = { tenantToRestore = tenant },
                                onPermanentDelete = { tenantToPermanentDelete = tenant }
                            )
                        }
                    }
                }
            }
        }
    }

    // Restore Confirmation Dialog
    tenantToRestore?.let { t ->
        AlertDialog(
            onDismissRequest = { tenantToRestore = null },
            title = { Text("Restore Tenant to Active?") },
            text = {
                Text("Kya aap '${t.name}' ko wapas Active tenants list me restore karna chahte hain? Inka puraana hisaab aur details barkarar rahenge.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreTenant(t.id)
                        tenantToRestore = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text("Restore Active")
                }
            },
            dismissButton = {
                TextButton(onClick = { tenantToRestore = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Permanent Delete Confirmation Dialog (Purge Dummy Data)
    tenantToPermanentDelete?.let { t ->
        AlertDialog(
            onDismissRequest = { tenantToPermanentDelete = null },
            title = { Text("Permanently Delete Dummy Data?", color = Color(0xFFDC2626)) },
            text = {
                Text("Dhyan dein: Agar ye dummy/test data tha to isse '${t.name}' aur unke sabhi purane rent records database se poori tarah mitt jayenge. Ye action wapas nahi ho sakta.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPermanentlyDeleteTenant(t.id)
                        tenantToPermanentDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Permanently Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { tenantToPermanentDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ArchivedTenantCard(
    tenant: Tenant,
    rentForSelectedMonth: RentRecord?,
    allRents: List<RentRecord>,
    selectedMonth: String,
    selectedYear: Int,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit
) {
    var expandedHistoricalLedger by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Name, Shop, Exit Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tenant.name.ifBlank { "Unnamed Tenant" },
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        if (tenant.businessName.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${tenant.businessName})",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        if (tenant.shopNumber.isNotBlank()) {
                            Text(
                                text = "🏪 Shop: ${tenant.shopNumber}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyPrimary
                            )
                        }
                        if (tenant.exitDate.isNotBlank()) {
                            Text(
                                text = "• Left: ${tenant.exitDate}",
                                fontSize = 11.sp,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }

                // Exited / Archived Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "EXITED",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contact & Financial Highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (tenant.phone.isNotBlank()) {
                        Text(
                            text = "📞 ${tenant.phone}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Quick call icon
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Call",
                            tint = StatusPaid,
                            modifier = Modifier
                                .size(15.dp)
                                .clickable { onCall() }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "💬",
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { onWhatsApp() }
                        )
                    } else {
                        Text(
                            text = "No Phone Recorded",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                if (tenant.advanceDeposit > 0.0) {
                    Text(
                        text = "Deposit: ₹${formatAmount(tenant.advanceDeposit)}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F766E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🎯 SELECTED MONTH HISAAB BOX
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (rentForSelectedMonth != null) Color(0xFFF8FAFC) else Color(0xFFF1F5F9).copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, if (rentForSelectedMonth != null) Color(0xFFE2E8F0) else Color(0xFFE2E8F0).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 $selectedMonth $selectedYear Hisaab:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )

                        if (rentForSelectedMonth != null) {
                            val (badgeBg, badgeBorder, badgeText, statusLabel) = when {
                                rentForSelectedMonth.isPaid -> Quad(StatusPaidBg, StatusPaidBorder, StatusPaid, "PAID")
                                rentForSelectedMonth.isPartial -> Quad(StatusPartialBg, StatusPartialBorder, StatusPartial, "PARTIAL")
                                else -> Quad(StatusPendingBg, StatusPendingBorder, StatusPending, "PENDING")
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeBg)
                                    .border(1.dp, badgeBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = statusLabel,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeText
                                )
                            }
                        } else {
                            Text(
                                text = "No Bill in $selectedMonth",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    if (rentForSelectedMonth != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Billed: ₹${formatAmount(rentForSelectedMonth.amountDue)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Paid: ₹${formatAmount(rentForSelectedMonth.amountPaid)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (rentForSelectedMonth.amountPaid > 0) StatusPaid else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Balance: ₹${formatAmount(rentForSelectedMonth.pendingAmount)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rentForSelectedMonth.pendingAmount > 0) StatusPending else StatusPaid
                            )
                        }

                        if (rentForSelectedMonth.paidDate.isNotBlank()) {
                            Text(
                                text = "Paid on: ${rentForSelectedMonth.paidDate} (${rentForSelectedMonth.paymentMode})",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Is mahine me is kirayedar ka koi rent generate nahi hua tha.",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expandable All Historical Ledger
            if (allRents.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { expandedHistoricalLedger = !expandedHistoricalLedger }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 View All Past Months Ledger (${allRents.size} records)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyPrimary
                    )
                    Icon(
                        imageVector = if (expandedHistoricalLedger) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(visible = expandedHistoricalLedger) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        allRents.forEach { r ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${r.month} ${r.year}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Due: ₹${formatAmount(r.amountDue)} | Paid: ₹${formatAmount(r.amountPaid)}",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (r.isPaid) "PAID" else if (r.isPartial) "PARTIAL" else "PENDING",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (r.isPaid) StatusPaid else if (r.isPartial) StatusPartial else StatusPending
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp, color = CardBorderLight)
            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons: Restore vs Permanent Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Permanent Delete button (to purge test dummy data)
                TextButton(
                    onClick = onPermanentDelete,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = "Permanently Delete",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Permanent Delete (Dummy)",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFDC2626)
                    )
                }

                // Restore Button
                OutlinedButton(
                    onClick = onRestore,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF16A34A)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF0FDF4))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Restore,
                        contentDescription = "Restore",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Restore Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun formatAmount(amount: Double): String {
    return String.format(Locale.getDefault(), "%,.0f", amount)
}

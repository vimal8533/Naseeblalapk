package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Apartment
import com.example.model.Shop
import com.example.model.Tenant
import com.example.ui.components.AddTenantDialog
import com.example.ui.components.TenantsSkeleton
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPaidBorder
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusPendingBorder
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.clickable

@Composable
fun TenantsScreen(
    tenants: List<Tenant>,
    shops: List<Shop> = emptyList(),
    canManagePersonalTenants: Boolean = false,
    currentWorkspaceMode: com.example.model.AppWorkspaceMode = com.example.model.AppWorkspaceMode.PUBLIC_MARKET,
    onAddOrUpdateTenant: (
        name: String,
        businessName: String,
        phone: String,
        shopNumber: String,
        numberOfShops: Int,
        deposit: Double,
        rent: Double,
        previousDues: Double,
        incrementYears: Int,
        incrementPercent: Double,
        billingCycle: String,
        joiningDate: String,
        idProof: String,
        notes: String,
        existingId: String?,
        isPersonal: Boolean,
        electricityBill: Double
    ) -> Unit,
    onDeleteTenant: (tenantId: String) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPersonalWorkspace = currentWorkspaceMode == com.example.model.AppWorkspaceMode.PRIVATE_PERSONAL
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "SINGLE", "MULTI"
    var showAddDialog by remember { mutableStateOf(false) }
    var tenantToEdit by remember { mutableStateOf<Tenant?>(null) }
    var tenantToDelete by remember { mutableStateOf<Tenant?>(null) }

    val filteredTenants = remember(tenants, searchQuery, selectedFilter) {
        val base = when (selectedFilter) {
            "MULTI" -> tenants.filter { it.shopCount > 1 }
            "SINGLE" -> tenants.filter { it.shopCount <= 1 }
            else -> tenants
        }
        if (searchQuery.isBlank()) {
            base
        } else {
            val query = searchQuery.trim()
            base.filter { t ->
                t.name.contains(query, ignoreCase = true) ||
                t.businessName.contains(query, ignoreCase = true) ||
                t.phone.contains(query, ignoreCase = true) ||
                t.shopNumber.contains(query, ignoreCase = true)
            }
        }
    }

    val totalAssignedShops = remember(filteredTenants) {
        filteredTenants.sumOf { it.shopCount }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            TenantsSkeleton()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
            // Search Bar
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(if (isPersonalWorkspace) "Search flat tenants (Name, Phone, Flat #)..." else "Search tenants (Name, Phone, Shop #)...")
                        },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenants_search_input")
                    )
                }
            }

            // Quick Filter Chips (All, Single Shop/Flat, Multi-Shop/Unit)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val multiCount = tenants.count { it.shopCount > 1 }
                    val singleCount = tenants.count { it.shopCount <= 1 }

                    val singleLabel = if (isPersonalWorkspace) "Single Flat ($singleCount)" else "Single Shop ($singleCount)"
                    val singleIcon = if (isPersonalWorkspace) Icons.Filled.Apartment else Icons.Filled.Store
                    val multiLabel = if (isPersonalWorkspace) "Multi-Flat ($multiCount)" else "Multi-Shop ($multiCount)"
                    val multiIcon = if (isPersonalWorkspace) Icons.Filled.Apartment else Icons.Filled.Store

                    listOf(
                        Triple("ALL", "All (${tenants.size})", Icons.Filled.People),
                        Triple("SINGLE", singleLabel, singleIcon),
                        Triple("MULTI", multiLabel, multiIcon)
                    ).forEach { (filterKey, label, icon) ->
                        val isSelected = selectedFilter == filterKey
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) (if (isPersonalWorkspace) Color(0xFF0F766E) else NavyDark) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) (if (isPersonalWorkspace) Color(0xFF2DD4BF) else GoldAccent) else CardBorderLight,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedFilter = filterKey }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) (if (isPersonalWorkspace) Color.White else GoldAccent) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Summary Stats Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPersonalWorkspace) "Active Flat Tenants: ${filteredTenants.size}" else "Active Tenants: ${filteredTenants.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPersonalWorkspace) Color(0xFF0F766E) else NavyPrimary
                    )
                    Text(
                        text = if (isPersonalWorkspace) "Total Assigned Flats: $totalAssignedShops" else "Total Assigned Shops: $totalAssignedShops",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tenants List
            if (filteredTenants.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (isPersonalWorkspace) Icons.Filled.Apartment else Icons.Filled.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isPersonalWorkspace) "No flat tenants found." else "No tenants found.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPersonalWorkspace) Color(0xFF0F766E) else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(if (isPersonalWorkspace) "Add New Flat Tenant" else "Add New Tenant")
                            }
                        }
                    }
                }
            } else {
                items(filteredTenants, key = { it.id }) { tenant ->
                    TenantCardItem(
                        tenant = tenant,
                        onCall = {
                            if (tenant.phone.isNotBlank()) {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${tenant.phone}"))
                                context.startActivity(dialIntent)
                            }
                        },
                        onWhatsApp = {
                            if (tenant.phone.isNotBlank()) {
                                val cleanPhone = tenant.phone.filter { it.isDigit() }.let { raw ->
                                    when {
                                        raw.length == 10 -> "91$raw"
                                        raw.startsWith("0") && raw.length == 11 -> "91${raw.substring(1)}"
                                        raw.startsWith("91") && raw.length == 12 -> raw
                                        else -> raw
                                    }
                                }
                                val text = if (tenant.isPersonal) {
                                    "Hello ${tenant.name}, greetings regarding your Flat/Unit ${tenant.shopNumber} rent and electricity bill details."
                                } else {
                                    "Hello ${tenant.name}, greetings from Naseeb Lal Market management regarding your Shop ${tenant.shopNumber}."
                                }
                                try {
                                    val waIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(text)}")
                                    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                    context.startActivity(waIntent)
                                } catch (e: Exception) {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, text)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Contact via WhatsApp"))
                                }
                            }
                        },
                        onEdit = { tenantToEdit = tenant },
                        onDelete = { tenantToDelete = tenant },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
        }

        // FAB to add tenant
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = if (isPersonalWorkspace) Color(0xFF0F766E) else GoldAccent,
            contentColor = if (isPersonalWorkspace) Color.White else NavyDark,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_tenant_fab")
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Tenant")
        }
    }

    // Add Tenant Dialog
    if (showAddDialog) {
        AddTenantDialog(
            initialTenant = null,
            availableShops = shops,
            canManagePersonalTenants = canManagePersonalTenants,
            initialWorkspaceMode = currentWorkspaceMode,
            onDismiss = { showAddDialog = false },
            onSave = { name, business, phone, shopNum, numShops, deposit, rent, previousDues, incYears, incPct, cycle, date, idProof, notes, isPersonal, elecBill ->
                onAddOrUpdateTenant(name, business, phone, shopNum, numShops, deposit, rent, previousDues, incYears, incPct, cycle, date, idProof, notes, null, isPersonal, elecBill)
                showAddDialog = false
            }
        )
    }

    // Edit Tenant Dialog
    tenantToEdit?.let { t ->
        AddTenantDialog(
            initialTenant = t,
            availableShops = shops,
            canManagePersonalTenants = canManagePersonalTenants,
            initialWorkspaceMode = currentWorkspaceMode,
            onDismiss = { tenantToEdit = null },
            onSave = { name, business, phone, shopNum, numShops, deposit, rent, previousDues, incYears, incPct, cycle, date, idProof, notes, isPersonal, elecBill ->
                onAddOrUpdateTenant(name, business, phone, shopNum, numShops, deposit, rent, previousDues, incYears, incPct, cycle, date, idProof, notes, t.id, isPersonal, elecBill)
                tenantToEdit = null
            }
        )
    }

    // Delete confirmation dialog
    tenantToDelete?.let { t ->
        AlertDialog(
            onDismissRequest = { tenantToDelete = null },
            title = { Text("Remove Tenant") },
            text = {
                Text("Are you sure you want to remove '${t.name}'?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTenant(t.id)
                        tenantToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusPending)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { tenantToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TenantCardItem(
    tenant: Tenant,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val individualShops = remember(tenant.shopNumber, tenant.isPersonal) {
        if (tenant.shopNumber.isBlank()) emptyList<String>()
        else tenant.shopNumber
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { raw ->
                if (tenant.isPersonal) {
                    if (raw.startsWith("Flat", ignoreCase = true) || raw.startsWith("Unit", ignoreCase = true)) raw
                    else "Flat $raw"
                } else {
                    if (raw.startsWith("Shop", ignoreCase = true)) raw else "Shop $raw"
                }
            }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tenant_card_${tenant.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Avatar + Shop/Business Name (Top) & Tenant Name + Phone (Below) (Left) and Shop Count + Cycle Badges (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    val avatarLetter = (if (tenant.businessName.isNotBlank()) tenant.businessName else tenant.name)
                        .take(1).uppercase()
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NavyDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = avatarLetter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = GoldAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        // Shop / Business Name on TOP (Prominent, wrapped)
                        val mainTitle = if (tenant.businessName.isNotBlank()) tenant.businessName else tenant.name
                        Text(
                            text = mainTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            lineHeight = 19.sp,
                            color = NavyDark,
                            softWrap = true
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // Tenant Name and Phone underneath
                        val subInfo = buildString {
                            if (tenant.businessName.isNotBlank()) {
                                append(tenant.name)
                            }
                            if (tenant.phone.isNotBlank()) {
                                if (isNotEmpty()) append(" • ")
                                append(tenant.phone)
                            }
                        }
                        if (subInfo.isNotBlank()) {
                            Text(
                                text = subInfo,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                softWrap = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Badges Row: Count + Cycle + (Optional Personal)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (tenant.isPersonal) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F766E))
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Apartment,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Personal",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Total Shops/Flats Count Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (tenant.isPersonal) Color(0xFF0F766E) else NavyDark)
                            .border(0.5.dp, if (tenant.isPersonal) Color(0xFF2DD4BF).copy(alpha = 0.5f) else GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (tenant.isPersonal) Icons.Filled.Apartment else Icons.Filled.Store,
                                contentDescription = null,
                                tint = if (tenant.isPersonal) Color.White else GoldAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val unitLabel = if (tenant.isPersonal) {
                                if (tenant.shopCount > 1) "${tenant.shopCount} Flats" else "1 Flat"
                            } else {
                                if (tenant.shopCount > 1) "${tenant.shopCount} Shops" else "1 Shop"
                            }
                            Text(
                                text = unitLabel,
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Billing Cycle Tag
                    val cycleLabel = when (tenant.cycleMonths) {
                        12 -> "Yearly"
                        6 -> "6 Months"
                        3 -> "3 Months"
                        else -> "Monthly"
                    }
                    val (cycleBg, cycleTextColor) = when (tenant.cycleMonths) {
                        12 -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
                        6 -> Pair(Color(0xFFEDE9FE), Color(0xFF6D28D9))
                        3 -> Pair(Color(0xFFE0F2FE), Color(0xFF0369A1))
                        else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
                    }
                    val cycleBorderColor = when (tenant.cycleMonths) {
                        12 -> Color(0xFFFDE68A)
                        6 -> Color(0xFFDDD6FE)
                        3 -> Color(0xFFBAE6FD)
                        else -> Color(0xFFE2E8F0)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(cycleBg)
                            .border(0.5.dp, cycleBorderColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cycleLabel,
                            color = cycleTextColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==================== ASSIGNED UNITS / SHOPS LIST (FLOW ROW CHIPS) ====================
            if (individualShops.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(0.5.dp, CardBorderLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    val assignedHeader = if (tenant.isPersonal) {
                        if (individualShops.size > 1) "ASSIGNED FLATS (${individualShops.size}):" else "ASSIGNED FLAT:"
                    } else {
                        if (individualShops.size > 1) "ASSIGNED SHOPS (${individualShops.size}):" else "ASSIGNED SHOP:"
                    }
                    Text(
                        text = assignedHeader,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        individualShops.forEach { unitName ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White)
                                    .border(
                                        0.5.dp,
                                        if (tenant.isPersonal) Color(0xFF0F766E).copy(alpha = 0.35f) else NavyDark.copy(alpha = 0.25f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (tenant.isPersonal) Icons.Filled.Apartment else Icons.Filled.Store,
                                        contentDescription = null,
                                        tint = if (tenant.isPersonal) Color(0xFF0F766E) else NavyPrimary,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = unitName,
                                        color = if (tenant.isPersonal) Color(0xFF0F766E) else NavyDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ==================== PREVIOUS DUES BADGE (IF ANY) ====================
            if (tenant.previousDues > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusPendingBg)
                        .border(0.5.dp, StatusPendingBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.HistoryEdu,
                            contentDescription = null,
                            tint = StatusPending,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Previous Dues (Opening Balance)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusPending
                        )
                    }
                    Text(
                        text = "₹${tenant.previousDues.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = StatusPending
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // ==================== RENT & DEPOSIT MICRO-LEDGER ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(0.5.dp, CardBorderLight, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    val rentLabel = when (tenant.cycleMonths) {
                        12 -> "YEARLY RENT"
                        6 -> "6-MONTH RENT"
                        3 -> "3-MONTH RENT"
                        else -> "MONTHLY RENT"
                    }
                    val rentDisplay = when (tenant.cycleMonths) {
                        12 -> "₹${(tenant.monthlyRent * 12).toInt()}/yr"
                        6 -> "₹${(tenant.monthlyRent * 6).toInt()}/6m"
                        3 -> "₹${(tenant.monthlyRent * 3).toInt()}/3m"
                        else -> "₹${tenant.monthlyRent.toInt()}/mo"
                    }
                    Text(
                        text = rentLabel,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = rentDisplay,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp,
                        color = NavyPrimary
                    )
                    if (tenant.shopCount > 1) {
                        Text(
                            text = "(₹${(tenant.monthlyRent / tenant.shopCount).toInt()}/shop/mo)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (tenant.cycleMonths > 1) {
                        Text(
                            text = "(₹${tenant.monthlyRent.toInt()}/mo)",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column {
                    Text(
                        text = "ADVANCE DEPOSIT",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${tenant.advanceDeposit.toInt()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = StatusPaid
                    )
                    if (tenant.shopCount > 1 && tenant.advanceDeposit > 0) {
                        Text(
                            text = "(₹${(tenant.advanceDeposit / tenant.shopCount).toInt()}/shop)",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "JOINED DATE",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tenant.joiningDate.ifBlank { "Active" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = NavyDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ==================== ANNUAL PMC TAX & INCREMENT STRIP (COMMERCIAL) OR ELECTRICITY & RENT BREAKUP (PERSONAL) ====================
            if (tenant.isPersonal) {
                // Personal flat strip: Electricity bill and total combined monthly due
                if (tenant.electricityBill > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(0.5.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ElectricMeter,
                                contentDescription = "Electricity Bill",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Monthly Electricity: ₹${tenant.electricityBill.toInt()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Total Flat Monthly: ₹${(tenant.monthlyRent + tenant.electricityBill).toInt()} (Rent + Light)",
                                    fontSize = 9.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFD97706))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+₹${tenant.electricityBill.toInt()}/mo",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Annual PMC Tax info
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(0.5.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AccountBalance,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "PMC: ₹${tenant.annualPmcTaxAmount.toInt()}/yr",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "₹1k × ${tenant.shopCount} shop${if (tenant.shopCount > 1) "s" else ""}",
                                    fontSize = 8.5.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }

                    // Increment rule
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavyDark.copy(alpha = 0.05f))
                            .border(0.5.dp, NavyDark.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.TrendingUp,
                                contentDescription = "Increment",
                                tint = NavyPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "Inc: ${tenant.incrementRuleText}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark
                                )
                                if (tenant.monthlyRent > 0) {
                                    Text(
                                        text = "Next: ₹${tenant.nextIncrementRent.toInt()}/mo",
                                        fontSize = 8.5.sp,
                                        color = NavyPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (tenant.idProof.isNotBlank() || tenant.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (tenant.idProof.isNotBlank()) {
                        Text(text = "ID: ${tenant.idProof}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (tenant.notes.isNotBlank()) {
                        Text(text = tenant.notes, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (tenant.lastModifiedBy.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Last edited by: ${tenant.lastModifiedBy}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions: Call, WhatsApp, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Call Button
                    OutlinedButton(
                        onClick = onCall,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, StatusPaidBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = StatusPaidBg.copy(alpha = 0.4f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("call_tenant_${tenant.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Call",
                            tint = StatusPaid,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (tenant.phone.isNotBlank()) tenant.phone else "Call",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusPaid
                        )
                    }

                    // WhatsApp Button
                    OutlinedButton(
                        onClick = onWhatsApp,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF0FDF4)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("wa_tenant_${tenant.id}")
                    ) {
                        Text(
                            text = "💬 WhatsApp",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF16A34A) // WhatsApp Green
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CardBorderLight),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("edit_tenant_${tenant.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = NavyPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = NavyPrimary)
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, StatusPendingBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = StatusPendingBg.copy(alpha = 0.3f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("delete_tenant_${tenant.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = StatusPending,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Delete", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = StatusPending)
                    }
                }
            }
        }
    }
}

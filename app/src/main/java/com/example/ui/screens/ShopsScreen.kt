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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.text.style.TextOverflow
import com.example.model.Shop
import com.example.ui.components.AddShopDialog
import com.example.ui.components.ShopsSkeleton
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

@Composable
fun ShopsScreen(
    shops: List<Shop>,
    canManagePersonalTenants: Boolean = false,
    currentWorkspaceMode: com.example.model.AppWorkspaceMode = com.example.model.AppWorkspaceMode.PUBLIC_MARKET,
    onAddOrUpdateShop: (
        shopNumber: String,
        floor: String,
        sizeSqFt: String,
        baseRent: Double,
        maintenanceCharge: Double,
        meter: String,
        notes: String,
        existingId: String?,
        isPersonal: Boolean
    ) -> Unit,
    onDeleteShop: (shopId: String) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("ALL") } // ALL, OCCUPIED, VACANT
    var showAddDialog by remember { mutableStateOf(false) }
    var shopToEdit by remember { mutableStateOf<Shop?>(null) }
    var shopToDelete by remember { mutableStateOf<Shop?>(null) }

    val filteredShops = remember(shops, searchQuery, filterStatus) {
        val query = searchQuery.trim()
        shops.filter { shop ->
            val matchesQuery = query.isBlank() ||
                    shop.shopNumber.contains(query, ignoreCase = true) ||
                    shop.floor.contains(query, ignoreCase = true) ||
                    (shop.tenantName ?: "").contains(query, ignoreCase = true)
            val matchesStatus = when (filterStatus) {
                "OCCUPIED" -> shop.isOccupied
                "VACANT" -> !shop.isOccupied
                else -> true
            }
            matchesQuery && matchesStatus
        }
    }

    val occupiedCount = remember(shops) { shops.count { it.isOccupied } }
    val vacantCount = remember(shops) { shops.count { !it.isOccupied } }
    val filters = remember(shops.size, occupiedCount, vacantCount) {
        listOf(
            "ALL" to "All Shops (${shops.size})",
            "OCCUPIED" to "Occupied ($occupiedCount)",
            "VACANT" to "Vacant ($vacantCount)"
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            ShopsSkeleton()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
            // 1. Search Bar
            item {
                Box(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search shops (Shop #, Floor, Tenant)...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shops_search_input")
                    )
                }
            }

            // 2. Filter chips
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters, key = { it.first }) { (key, label) ->
                        val isSelected = filterStatus == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { filterStatus = key }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 3. Shops List
            if (filteredShops.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No shops found.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Add New Shop")
                            }
                        }
                    }
                }
            } else {
                items(filteredShops, key = { it.id }) { shop ->
                    ShopCardItem(
                        shop = shop,
                        onEdit = { shopToEdit = shop },
                        onDelete = { shopToDelete = shop },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
        }

        // FAB to add shop
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = GoldAccent,
            contentColor = NavyDark,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_shop_fab")
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Shop")
        }
    }

    // Add Shop Dialog
    if (showAddDialog) {
        AddShopDialog(
            initialShop = null,
            canManagePersonalTenants = canManagePersonalTenants,
            initialWorkspaceMode = currentWorkspaceMode,
            onDismiss = { showAddDialog = false },
            onSave = { number, floor, size, rent, maint, meter, notes, isPersonal ->
                onAddOrUpdateShop(number, floor, size, rent, maint, meter, notes, null, isPersonal)
                showAddDialog = false
            }
        )
    }

    // Edit Shop Dialog
    shopToEdit?.let { shop ->
        AddShopDialog(
            initialShop = shop,
            canManagePersonalTenants = canManagePersonalTenants,
            initialWorkspaceMode = currentWorkspaceMode,
            onDismiss = { shopToEdit = null },
            onSave = { number, floor, size, rent, maint, meter, notes, isPersonal ->
                onAddOrUpdateShop(number, floor, size, rent, maint, meter, notes, shop.id, isPersonal)
                shopToEdit = null
            }
        )
    }

    // Delete confirmation dialog
    shopToDelete?.let { shop ->
        AlertDialog(
            onDismissRequest = { shopToDelete = null },
            title = { Text("Delete Shop") },
            text = {
                Text("Are you sure you want to remove '${shop.shopNumber}' from Naseeb Lal Market?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteShop(shop.id)
                        shopToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusPending)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { shopToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShopCardItem(
    shop: Shop,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOccupied = shop.isOccupied

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shop_card_${shop.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Shop Number, Unit Name & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (shop.isFlat) Color(0xFF0F766E) else NavyDark)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = shop.shopNumber,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = if (shop.isFlat) Color.White else GoldAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val shopDisplayName = if (shop.isFlat) {
                        if (shop.shopNumber.startsWith("Flat", ignoreCase = true) || shop.shopNumber.startsWith("Unit", ignoreCase = true) || shop.shopNumber.startsWith("Room", ignoreCase = true)) shop.shopNumber else "Flat ${shop.shopNumber}"
                    } else {
                        if (shop.shopNumber.startsWith("Shop", ignoreCase = true)) shop.shopNumber else "Shop ${shop.shopNumber}"
                    }
                    Text(
                        text = shopDisplayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavyDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Badges (FlowRow to prevent line overflow)
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (shop.isPersonal) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F766E).copy(alpha = 0.12f))
                                .border(0.5.dp, Color(0xFF0F766E).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Personal",
                                color = Color(0xFF0F766E),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (shop.isFlat) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F766E).copy(alpha = 0.12f))
                                .border(0.5.dp, Color(0xFF0F766E).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Flat",
                                color = Color(0xFF0F766E),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Occupancy Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isOccupied) StatusPaidBg else StatusPendingBg)
                            .border(
                                width = 0.5.dp,
                                color = if (isOccupied) StatusPaidBorder else StatusPendingBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isOccupied) "OCCUPIED ✓" else "VACANT",
                            color = if (isOccupied) StatusPaid else StatusPending,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Row: Floor, Size, Rent micro-ledger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .border(0.5.dp, CardBorderLight, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "FLOOR",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = shop.floor.ifBlank { "Ground" }, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                }

                Column {
                    Text(
                        text = "CARPET AREA",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = shop.sizeSqFt.ifBlank { "-" }, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "MONTHLY RENT",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${shop.totalMonthlyRent.toInt()}/mo",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp,
                        color = NavyPrimary
                    )
                }
            }

            // Tenant information if occupied
            if (shop.isOccupied && !shop.tenantName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusPaidBg)
                        .border(0.5.dp, StatusPaidBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Tenant",
                        tint = StatusPaid,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tenant: ${shop.tenantName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyDark
                    )
                }
            }

            if (shop.electricityMeter.isNotBlank() || shop.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (shop.electricityMeter.isNotBlank()) {
                        Text(
                            text = "Meter: ${shop.electricityMeter}",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    if (shop.electricityMeter.isNotBlank() && shop.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (shop.notes.isNotBlank()) {
                        Text(
                            text = shop.notes,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1.5f, fill = false)
                        )
                    }
                }
            }

            if (shop.lastModifiedBy.isNotBlank()) {
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
                        text = "Last edited by: ${shop.lastModifiedBy}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Edit and Delete Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CardBorderLight),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("edit_shop_${shop.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Shop",
                        tint = NavyPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = NavyPrimary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, StatusPendingBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = StatusPendingBg.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("delete_shop_${shop.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Shop",
                        tint = StatusPending,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = StatusPending)
                }
            }
        }
    }
}

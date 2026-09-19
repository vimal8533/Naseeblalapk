package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SyncStatus
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.ui.components.MarketTopBar
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.viewmodel.MarketViewModel

sealed class MarketTab(val title: String, val icon: ImageVector, val tag: String) {
    object Dashboard : MarketTab("Rent Tracker", Icons.Filled.Dashboard, "tab_dashboard")
    object Shops : MarketTab("Shops", Icons.Filled.Store, "tab_shops")
    object Tenants : MarketTab("Tenants", Icons.Filled.People, "tab_tenants")
    object SubAdmins : MarketTab("Sub Admin", Icons.Filled.Shield, "tab_subadmins")
    object Profile : MarketTab("Profile-Sync", Icons.Filled.CloudSync, "tab_profile")
}

@Composable
fun MainMarketScreen(
    viewModel: MarketViewModel,
    currentUser: UserSession,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf<MarketTab>(MarketTab.Dashboard) }

    val shops by viewModel.shops.collectAsState()
    val tenants by viewModel.tenants.collectAsState()
    val archivedTenants by viewModel.archivedTenants.collectAsState()
    val rents by viewModel.filteredRents.collectAsState()
    val allRents by viewModel.rents.collectAsState()
    val allHistoricalRents by viewModel.allHistoricalRents.collectAsState()
    val subAdmins by viewModel.subAdmins.collectAsState()
    val activityLogs by viewModel.activityLogs.collectAsState()
    val monthlyReminders by viewModel.monthlyReminders.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val lastSyncedAt by viewModel.lastSyncedAt.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val rentStatusFilter by viewModel.rentStatusFilter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val workspaceMode by viewModel.workspaceMode.collectAsState()

    // Tab items: If super admin, include SubAdmins tab. If sub-admin, do NOT include SubAdmins tab!
    val availableTabs = remember(currentUser.role) {
        if (currentUser.isAdmin) {
            listOf(
                MarketTab.Dashboard,
                MarketTab.Shops,
                MarketTab.Tenants,
                MarketTab.SubAdmins,
                MarketTab.Profile
            )
        } else {
            listOf(
                MarketTab.Dashboard,
                MarketTab.Shops,
                MarketTab.Tenants,
                MarketTab.Profile
            )
        }
    }

    Scaffold(
        topBar = {
            MarketTopBar(
                user = currentUser,
                syncStatus = syncStatus,
                onRefresh = { viewModel.forceRefresh() },
                workspaceMode = workspaceMode,
                onToggleWorkspace = { viewModel.toggleWorkspace() }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("market_bottom_navigation"),
                color = NavyDark,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFF2C3E6B).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(62.dp)
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    availableTabs.forEach { tab ->
                        val selected = currentTab == tab
                        val isProfile = tab == MarketTab.Profile

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { currentTab = tab }
                                )
                                .testTag(tab.tag)
                                .padding(vertical = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Active pill background behind the icon
                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (selected) GoldAccent.copy(alpha = 0.18f) else Color.Transparent
                                    )
                                    .padding(horizontal = 14.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (selected) GoldAccent else Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )

                                if (isProfile) {
                                    val dotColor = when (syncStatus) {
                                        SyncStatus.SYNCING -> Color(0xFFF59E0B)
                                        SyncStatus.ERROR -> Color(0xFFEF4444)
                                        SyncStatus.OFFLINE -> Color(0xFF94A3B8)
                                        SyncStatus.SYNCED -> Color(0xFF10B981)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) GoldAccent else Color.White.copy(alpha = 0.65f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MarketTab.Dashboard -> {
                    DashboardScreen(
                        rents = rents,
                        allRents = allRents,
                        shops = shops,
                        tenants = tenants,
                        archivedTenants = archivedTenants,
                        allHistoricalRents = allHistoricalRents,
                        onRestoreTenant = { viewModel.restoreTenant(it) },
                        onPermanentlyDeleteTenant = { viewModel.permanentlyDeleteTenant(it) },
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        searchQuery = searchQuery,
                        statusFilter = rentStatusFilter,
                        monthlyReminders = monthlyReminders,
                        currentUser = currentUser,
                        workspaceMode = workspaceMode,
                        onMonthChanged = { viewModel.setMonth(it) },
                        onYearChanged = { viewModel.setYear(it) },
                        onSearchChanged = { viewModel.setSearchQuery(it) },
                        onFilterChanged = { viewModel.setRentStatusFilter(it) },
                        onCollectRentPayment = { rentId, amount, mode, note, paidDate ->
                            viewModel.recordRentPayment(rentId, amount, mode, note, paidDate)
                        },
                        onCorrectRentPayment = { rentId, newAmount, mode, reason, paidDate ->
                            viewModel.correctRentPayment(rentId, newAmount, mode, reason, paidDate)
                        },
                        onGenerateMonthCycle = { month, year ->
                            viewModel.generateRentRecordsForMonth(month, year)
                        },
                        onSendBatchReminders = { month, year, senderName, phone, simSlot, apiKey, onProgress, onComplete ->
                            viewModel.sendBatchRentReminders(
                                month = month,
                                year = year,
                                senderName = senderName,
                                senderContactPhone = phone,
                                simSlotSubscriptionId = simSlot,
                                fast2SmsApiKey = apiKey,
                                onProgress = onProgress,
                                onComplete = onComplete
                            )
                        },
                        onResetReminderStatus = { month, year ->
                            viewModel.resetMonthlyReminderStatus(month, year)
                        },
                        onRecordManualRemindersSent = { month, year, count, channel ->
                            viewModel.recordManualRemindersSent(month, year, count, channel)
                        },
                        onUpdateElectricityMeter = { rentId, prev, current, rate ->
                            viewModel.updateElectricityMeterReading(rentId, prev, current, rate)
                        },
                        isLoading = isLoading
                    )
                }
                MarketTab.Shops -> {
                    val canManagePersonal = currentUser.isAdmin || currentUser.canManagePersonalTenants
                    ShopsScreen(
                        shops = shops,
                        canManagePersonalTenants = canManagePersonal,
                        currentWorkspaceMode = workspaceMode,
                        onAddOrUpdateShop = { number, floor, size, rent, maint, meter, notes, existingId, isPersonal ->
                            viewModel.addOrUpdateShop(number, floor, size, rent, maint, meter, notes, existingId, isPersonal)
                        },
                        onDeleteShop = { viewModel.deleteShop(it) },
                        isLoading = isLoading
                    )
                }
                MarketTab.Tenants -> {
                    val canManagePersonal = currentUser.isAdmin || currentUser.canManagePersonalTenants
                    TenantsScreen(
                        tenants = tenants,
                        shops = shops,
                        archivedTenants = archivedTenants,
                        allHistoricalRents = allHistoricalRents,
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        canManagePersonalTenants = canManagePersonal,
                        currentWorkspaceMode = workspaceMode,
                        onAddOrUpdateTenant = { name, business, phone, shopNumber, numberOfShops, deposit, rent, previousDues, incYears, incPct, cycle, date, idProof, notes, existingId, isPersonal, elecBill ->
                            viewModel.addOrUpdateTenant(
                                name = name,
                                businessName = business,
                                phone = phone,
                                shopNumber = shopNumber,
                                numberOfShops = numberOfShops,
                                advanceDeposit = deposit,
                                monthlyRent = rent,
                                previousDues = previousDues,
                                incrementYears = incYears,
                                incrementPercent = incPct,
                                billingCycle = cycle,
                                joiningDate = date,
                                idProof = idProof,
                                notes = notes,
                                existingId = existingId,
                                isPersonal = isPersonal,
                                ownerSubAdminId = "",
                                electricityBill = elecBill
                            )
                        },
                        onDeleteTenant = { viewModel.deleteTenant(it) },
                        onRestoreTenant = { viewModel.restoreTenant(it) },
                        onPermanentlyDeleteTenant = { viewModel.permanentlyDeleteTenant(it) },
                        isLoading = isLoading
                    )
                }
                MarketTab.SubAdmins -> {
                    SubAdminsScreen(
                        subAdmins = subAdmins,
                        activityLogs = activityLogs,
                        onAddSubAdmin = { user, pass, name, phone, canPersonal ->
                            viewModel.addSubAdmin(user, pass, name, phone, canPersonal)
                        },
                        onUpdateSubAdmin = { subAdminId, name, phone, canPersonal ->
                            viewModel.updateSubAdmin(subAdminId, name, phone, canPersonal)
                        },
                        onChangePassword = { subAdminId, newPass ->
                            viewModel.changeSubAdminPassword(subAdminId, newPass)
                        },
                        onDeleteSubAdmin = { viewModel.deleteSubAdmin(it) },
                        isLoading = isLoading
                    )
                }
                MarketTab.Profile -> {
                    ProfileScreen(
                        user = currentUser,
                        syncStatus = syncStatus,
                        lastSyncedAt = lastSyncedAt,
                        totalShops = shops.size,
                        totalTenants = tenants.size,
                        workspaceMode = workspaceMode,
                        onToggleWorkspace = { viewModel.toggleWorkspace() },
                        onForceRefresh = { viewModel.forceRefresh() },
                        onUpdateAdminProfile = { name, phone -> viewModel.updateAdminProfile(name, phone) },
                        onLogout = { viewModel.logout() }
                    )
                }
            }
        }
    }
}

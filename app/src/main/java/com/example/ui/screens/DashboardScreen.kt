package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppWorkspaceMode
import com.example.model.MonthlyReminderRecord
import com.example.model.RentRecord
import com.example.model.Shop
import com.example.model.Tenant
import com.example.model.UserSession
import com.example.ui.components.BatchMeterReadingDialog
import com.example.ui.components.BatchSmsReminderDialog
import com.example.ui.components.ElectricityMeterDialog
import com.example.ui.components.CollectRentDialog
import com.example.ui.components.CorrectPaymentDialog
import com.example.ui.components.RentTrackerSkeleton
import com.example.ui.components.showCalendarDatePicker
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyLight
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
import com.example.ui.util.MonthlyReportExcelGenerator
import com.example.ui.util.ReceiptPdfGenerator
import com.example.ui.util.ShareUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    rents: List<RentRecord>,
    allRents: List<RentRecord> = rents,
    shops: List<Shop>,
    tenants: List<Tenant> = emptyList(),
    selectedMonth: String,
    selectedYear: Int,
    searchQuery: String,
    statusFilter: String,
    monthlyReminders: Map<String, MonthlyReminderRecord> = emptyMap(),
    currentUser: UserSession? = null,
    workspaceMode: AppWorkspaceMode = AppWorkspaceMode.PUBLIC_MARKET,
    archivedTenants: List<Tenant> = emptyList(),
    allHistoricalRents: List<RentRecord> = emptyList(),
    onRestoreTenant: (tenantId: String) -> Unit = {},
    onPermanentlyDeleteTenant: (tenantId: String) -> Unit = {},
    onMonthChanged: (String) -> Unit,
    onYearChanged: (Int) -> Unit,
    onSearchChanged: (String) -> Unit,
    onFilterChanged: (String) -> Unit,
    onCollectRentPayment: (rentId: String, amountPaid: Double, mode: String, notes: String, paidDate: String) -> Unit,
    onCorrectRentPayment: (rentId: String, newTotalPaid: Double, mode: String, reason: String, paidDate: String) -> Unit = { _, _, _, _, _ -> },
    onGenerateMonthCycle: (month: String, year: Int) -> Unit,
    onSendBatchReminders: (month: String, year: Int, senderName: String, senderPhone: String, subscriptionId: Int?, fast2SmsApiKey: String?, onProgress: (Int, Int) -> Unit, onComplete: (Int, Int, String?) -> Unit) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onResetReminderStatus: (month: String, year: Int) -> Unit = { _, _ -> },
    onRecordManualRemindersSent: (month: String, year: Int, count: Int, channel: String) -> Unit = { _, _, _, _ -> },
    onUpdateElectricityMeter: (rentId: String, prev: Double, current: Double, rate: Double) -> Unit = { _, _, _, _ -> },
    onSaveBatchMeterReadings: (entries: List<Triple<String, Triple<Double, Double, Double>, Double>>) -> Unit = { _ -> },
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var rentToCollect by remember { mutableStateOf<RentRecord?>(null) }
    var rentToCorrect by remember { mutableStateOf<RentRecord?>(null) }
    var rentForElectricityMeter by remember { mutableStateOf<RentRecord?>(null) }
    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var showBatchSmsDialog by remember { mutableStateOf(false) }
    var showBatchMeterDialog by remember { mutableStateOf(false) }
    var showDuesBreakdownDialog by remember { mutableStateOf(false) }

    val isPersonalWorkspace = workspaceMode == AppWorkspaceMode.PRIVATE_PERSONAL
    val reminderKey = "${selectedYear}_${selectedMonth}${if (isPersonalWorkspace) "_personal" else ""}"
    val reminderRecord = monthlyReminders.entries.firstOrNull {
        it.key.equals(reminderKey, ignoreCase = true)
    }?.value ?: (if (!isPersonalWorkspace) monthlyReminders.entries.firstOrNull {
        it.value.month.equals(selectedMonth, ignoreCase = true) && it.value.year == selectedYear && !it.key.contains("personal")
    }?.value else null)
    val isReminderAlreadySent = reminderRecord != null && reminderRecord.isSent

    val pendingRentsForMonth = remember(rents, selectedMonth, selectedYear) {
        rents.filter { rent ->
            rent.month.equals(selectedMonth, ignoreCase = true) &&
            rent.year == selectedYear &&
            (rent.isPending || rent.isPartial) &&
            rent.pendingAmount > 0
        }
    }

    val monthsList = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // Memoized Tenant Lookup Map for O(1) instantaneous access during scroll
    val tenantMap = remember(tenants) { tenants.associateBy { it.id } }

    val subMeteredRentsForMonth = remember(rents, tenants, selectedMonth, selectedYear) {
        rents.filter { rent ->
            val tenant = tenantMap[rent.tenantId]
            val isFlatUnit = rent.isFlat || (tenant?.isFlat == true)

            rent.month.equals(selectedMonth, ignoreCase = true) &&
            rent.year == selectedYear &&
            (isFlatUnit || rent.electricityBill > 0 || rent.currentMeterReading > 0)
        }
    }

    // Dues data structure for tracking total arrears + current month dues per tenant
    data class TenantDuesSummary(
        val tenantId: String,
        val tenantName: String,
        val businessName: String,
        val phone: String,
        val shopNumbers: String,
        val openingDues: Double,
        val currentMonthDues: Double,
        val currentMonthRent: RentRecord?,
        val totalDues: Double
    )

    // Compute comprehensive tenant-wise dues breakdown
    val tenantDuesList = remember(tenants, rents, allRents, selectedMonth, selectedYear) {
        tenants.mapNotNull { tenant ->
            val tenantOpeningDues = tenant.previousDues.coerceAtLeast(0.0)
            val currentMonthRent = rents.firstOrNull { it.tenantId == tenant.id }
            val currentMonthDue = currentMonthRent?.pendingAmount ?: 0.0

            val totalDue = tenantOpeningDues + currentMonthDue
            if (totalDue > 0.0) {
                TenantDuesSummary(
                    tenantId = tenant.id,
                    tenantName = tenant.name,
                    businessName = tenant.businessName,
                    phone = tenant.phone,
                    shopNumbers = tenant.shopNumber.ifBlank { currentMonthRent?.shopNumber ?: "" },
                    openingDues = tenantOpeningDues,
                    currentMonthDues = currentMonthDue,
                    currentMonthRent = currentMonthRent,
                    totalDues = totalDue
                )
            } else null
        }.sortedByDescending { it.totalDues }
    }

    val totalDuesAmount = remember(tenantDuesList) { tenantDuesList.sumOf { it.totalDues } }
    val totalDuesTenantsCount = remember(tenantDuesList) { tenantDuesList.size }

    // Memoized Analytics Calculations to avoid recomputing on scroll/keystrokes
    val totalExpected = remember(rents) { rents.sumOf { it.amountDue } }
    val totalCollected = remember(rents) { rents.sumOf { it.amountPaid } }
    val totalPending = remember(rents) { rents.sumOf { it.pendingAmount } }
    val pendingCount = remember(rents) { rents.count { it.isPending || it.isPartial } }
    val paidCount = remember(rents) { rents.count { it.isPaid } }
    val partialCount = remember(rents) { rents.count { it.isPartial } }
    val pendingOnlyCount = remember(rents) { rents.count { it.isPending } }

    val occupiedCount = remember(shops) { shops.count { it.isOccupied } }
    val totalShops = shops.size

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            RentTrackerSkeleton()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
            // 1. Unified Rent Tracker & Actions Header (Clean, Modern M3 Layout)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CardBorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Top Row: Month Picker Chip + Calendar Picker + Excel Export
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Month & Year Selector
                            Box(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .border(1.dp, CardBorderLight, RoundedCornerShape(10.dp))
                                        .clickable { monthDropdownExpanded = true }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.CalendarToday,
                                            contentDescription = null,
                                            tint = NavyPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$selectedMonth $selectedYear",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = NavyPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "▼", fontSize = 9.sp, color = NavyLight)
                                }

                                DropdownMenu(
                                    expanded = monthDropdownExpanded,
                                    onDismissRequest = { monthDropdownExpanded = false }
                                ) {
                                    monthsList.forEach { m ->
                                        DropdownMenuItem(
                                            text = { Text(m, fontWeight = if (m == selectedMonth) FontWeight.Bold else FontWeight.Normal) },
                                            onClick = {
                                                onMonthChanged(m)
                                                monthDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Calendar Picker Icon Button
                            IconButton(
                                onClick = {
                                    showCalendarDatePicker(
                                        context = context,
                                        initialDateStr = "01 $selectedMonth $selectedYear"
                                    ) { _, cal ->
                                        val mName = SimpleDateFormat("MMMM", Locale.ENGLISH).format(cal.time)
                                        val yNum = cal.get(Calendar.YEAR)
                                        onMonthChanged(mName)
                                        onYearChanged(yNum)
                                    }
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, CardBorderLight, RoundedCornerShape(10.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EditCalendar,
                                    contentDescription = "Pick Date from Calendar",
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Excel Report Button (Compact & Clean)
                            Button(
                                onClick = {
                                    MonthlyReportExcelGenerator.exportAndShareMonthlyReport(
                                        context = context,
                                        month = selectedMonth,
                                        year = selectedYear,
                                        rents = rents,
                                        shops = shops,
                                        tenants = tenants
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("export_monthly_excel_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Excel", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Bottom Row: Contextual Cycle / Reminder Strip
                        Spacer(modifier = Modifier.height(12.dp))

                        if (rents.none { it.month == selectedMonth && it.year == selectedYear }) {
                            // Cycle Missing: One-Click Auto-Generate
                            OutlinedButton(
                                onClick = { onGenerateMonthCycle(selectedMonth, selectedYear) },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.3f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generate_rent_cycle_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Auto-Generate $selectedMonth Rent Cycle",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                        } else if (isReminderAlreadySent) {
                            // Reminders already sent: Clean, peaceful green status bar
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reminder_already_sent_card"),
                                shape = RoundedCornerShape(10.dp),
                                color = StatusPaidBg,
                                border = BorderStroke(1.dp, StatusPaidBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = StatusPaid,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "$selectedMonth Reminders Sent (${reminderRecord?.recipientsCount ?: 0} Tenants)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = StatusPaid
                                            )
                                            if (pendingRentsForMonth.isNotEmpty()) {
                                                Text(
                                                    text = "${pendingRentsForMonth.size} tenant(s) still unpaid",
                                                    fontSize = 10.5.sp,
                                                    color = Color(0xFF166534)
                                                )
                                            }
                                        }
                                    }

                                    if (pendingRentsForMonth.isNotEmpty()) {
                                        OutlinedButton(
                                            onClick = { onResetReminderStatus(selectedMonth, selectedYear) },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, StatusPaid.copy(alpha = 0.5f)),
                                            modifier = Modifier.testTag("reset_reminder_status_btn")
                                        ) {
                                            Text("↺ Reset", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = StatusPaid)
                                        }
                                    }
                                }
                            }
                        } else if (pendingRentsForMonth.isNotEmpty()) {
                            // Pending Reminders: Sleek, high-contrast action strip
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("batch_sms_reminder_card"),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(NavyPrimary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.NotificationsActive,
                                                contentDescription = null,
                                                tint = NavyPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "${pendingRentsForMonth.size} Reminders Pending",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = NavyPrimary
                                            )
                                            Text(
                                                text = "₹${pendingRentsForMonth.sumOf { it.pendingAmount }.toInt()} due for $selectedMonth",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { showBatchSmsDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("send_batch_sms_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Sms,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Send Reminders",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (subMeteredRentsForMonth.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("batch_meter_reading_card"),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0F766E).copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, Color(0xFF0F766E).copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFEF3C7)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.ElectricMeter,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Monthly Meter Readings",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.5.sp,
                                                    color = Color(0xFF0F766E)
                                                )
                                                val enteredCount = subMeteredRentsForMonth.count { it.currentMeterReading > 0.0 }
                                                Text(
                                                    text = if (enteredCount == subMeteredRentsForMonth.size) "All ${subMeteredRentsForMonth.size} flats recorded ✓" else "$enteredCount of ${subMeteredRentsForMonth.size} recorded for $selectedMonth",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = { showBatchMeterDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("enter_batch_readings_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.ElectricMeter,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "Enter Readings",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Overview Stat Cards
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    // Highlight Banner: Total Market Dues (Clickable to view tenant-wise dues breakdown)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDuesBreakdownDialog = true }
                            .testTag("total_dues_banner_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                        border = BorderStroke(1.2.dp, Color(0xFFFECDD3)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE11D48).copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = "Total Dues",
                                        tint = Color(0xFFE11D48),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "TOTAL MARKET DUES",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF9F1239),
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFE11D48))
                                                .padding(horizontal = 6.dp, vertical = 1.5.dp)
                                        ) {
                                            Text(
                                                text = "$totalDuesTenantsCount Tenants",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "₹${formatAmount(totalDuesAmount)}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF9F1239),
                                        letterSpacing = (-0.5).sp
                                    )
                                    Text(
                                        text = "Tap to view tenant breakdown & details",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.5.sp,
                                        color = Color(0xFFBE123C)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE11D48).copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = "View Details",
                                    tint = Color(0xFFE11D48),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Expected
                        StatCard(
                            title = "Total Expected",
                            amount = "₹${formatAmount(totalExpected)}",
                            subtitle = "${rents.size} Records",
                            icon = Icons.Filled.Receipt,
                            containerColor = MaterialTheme.colorScheme.surface,
                            accentColor = NavyPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        // Collected
                        StatCard(
                            title = "Total Collected",
                            amount = "₹${formatAmount(totalCollected)}",
                            subtitle = "$paidCount Fully Paid",
                            icon = Icons.Filled.CheckCircle,
                            containerColor = StatusPaidBg,
                            accentColor = StatusPaid,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Pending (Prominent Alert Card)
                        StatCard(
                            title = "Month Pending",
                            amount = "₹${formatAmount(totalPending)}",
                            subtitle = "$pendingCount Due in $selectedMonth",
                            icon = Icons.Filled.HourglassBottom,
                            containerColor = StatusPendingBg,
                            accentColor = StatusPending,
                            modifier = Modifier.weight(1f)
                        )

                        // Market / Flat Occupancy
                        val isPersonalWorkspace = workspaceMode == AppWorkspaceMode.PRIVATE_PERSONAL
                        StatCard(
                            title = if (isPersonalWorkspace) "Flat Occupancy" else "Shop Occupancy",
                            amount = "$occupiedCount / $totalShops",
                            subtitle = "${totalShops - occupiedCount} Vacant",
                            icon = if (isPersonalWorkspace) Icons.Filled.Apartment else Icons.Filled.Store,
                            containerColor = MaterialTheme.colorScheme.surface,
                            accentColor = if (isPersonalWorkspace) Color(0xFF0F766E) else GoldDark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Search Bar
            item {
                val isPersonalWorkspace = workspaceMode == AppWorkspaceMode.PRIVATE_PERSONAL
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChanged,
                        placeholder = { Text(if (isPersonalWorkspace) "Search flat number or tenant name..." else "Search shop number or tenant name...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rent_search_input")
                    )
                }
            }

            // 4. Status Filter Chips
            item {
                val filters = remember(rents.size, pendingOnlyCount, partialCount, paidCount) {
                    listOf(
                        "ALL" to "All (${rents.size})",
                        "PENDING" to "Pending ($pendingOnlyCount)",
                        "PARTIAL" to "Partial ($partialCount)",
                        "PAID" to "Paid ($paidCount)"
                    )
                }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters, key = { it.first }) { (key, label) ->
                        val isSelected = statusFilter == key
                        val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        val chipText = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(chipBg)
                                .clickable { onFilterChanged(key) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                color = chipText,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 5. Rent Records List
            if (rents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No rent records found for this month.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onGenerateMonthCycle(selectedMonth, selectedYear) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Generate Rents for $selectedMonth")
                            }
                        }
                    }
                }
            } else {
                items(rents, key = { it.id }) { rent ->
                    val tenant = tenantMap[rent.tenantId]
                    RentItemCard(
                        rent = rent,
                        businessName = tenant?.businessName ?: "",
                        tenantPhone = tenant?.phone ?: "",
                        billingCycleLabel = if ((tenant?.cycleMonths ?: 1) > 1) (tenant?.billingCycleDisplay ?: "") else "",
                        onCollect = { rentToCollect = rent },
                        onEditPayment = { rentToCorrect = rent },
                        onEditElectricityMeter = { rentForElectricityMeter = rent },
                        onShare = { ShareUtils.shareRentReceipt(context, rent) },
                        onWhatsAppReminder = { ShareUtils.sendWhatsAppReminder(context, rent, tenant?.phone) },
                        onDownloadReceipt = { ReceiptPdfGenerator.generateAndDownloadReceipt(context, rent) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
    }

    // Dialog for collecting rent
    rentToCollect?.let { r ->
        CollectRentDialog(
            rent = r,
            onDismiss = { rentToCollect = null },
            onConfirmPayment = { amount, mode, note, paidDate ->
                onCollectRentPayment(r.id, amount, mode, note, paidDate)
                rentToCollect = null
            },
            onSwitchToCorrection = {
                rentToCorrect = r
                rentToCollect = null
            }
        )
    }

    // Dialog for 1-Click Batch SMS Reminders
    if (showBatchSmsDialog) {
        BatchSmsReminderDialog(
            month = selectedMonth,
            year = selectedYear,
            pendingRents = pendingRentsForMonth,
            tenants = tenants,
            currentUser = currentUser,
            onDismiss = { showBatchSmsDialog = false },
            onRecordManualRemindersSent = { count, channel ->
                onRecordManualRemindersSent(selectedMonth, selectedYear, count, channel)
            },
            onConfirmSend = { senderName, senderPhone, simSlot, apiKey, onProgress, onComplete ->
                onSendBatchReminders(selectedMonth, selectedYear, senderName, senderPhone, simSlot, apiKey, onProgress, onComplete)
            }
        )
    }

    // Dialog for Batch Meter Readings
    if (showBatchMeterDialog) {
        BatchMeterReadingDialog(
            month = selectedMonth,
            year = selectedYear,
            subMeteredRents = subMeteredRentsForMonth,
            tenants = tenants,
            onDismiss = { showBatchMeterDialog = false },
            onSaveBatch = { entries ->
                onSaveBatchMeterReadings(entries)
                showBatchMeterDialog = false
            }
        )
    }

    // Dialog for Electricity Meter Reading
    rentForElectricityMeter?.let { r ->
        ElectricityMeterDialog(
            rent = r,
            tenant = tenantMap[r.tenantId],
            onDismiss = { rentForElectricityMeter = null },
            onSave = { prev, current, rate ->
                onUpdateElectricityMeter(r.id, prev, current, rate)
                rentForElectricityMeter = null
            }
        )
    }

    // Dialog for correcting / editing payment
    rentToCorrect?.let { r ->
        CorrectPaymentDialog(
            rent = r,
            onDismiss = { rentToCorrect = null },
            onConfirmCorrection = { newAmount, mode, reason, paidDate ->
                onCorrectRentPayment(r.id, newAmount, mode, reason, paidDate)
                rentToCorrect = null
            }
        )
    }

    // Comprehensive Tenant-wise Dues Breakdown Dialog
    if (showDuesBreakdownDialog) {
        Dialog(onDismissRequest = { showDuesBreakdownDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Market Dues Breakdown",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = NavyDark
                            )
                            Text(
                                text = "Tenant-wise outstanding dues",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showDuesBreakdownDialog = false }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Aggregate Summary Pill
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                        border = BorderStroke(1.dp, Color(0xFFFECDD3))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL OUTSTANDING",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF9F1239),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "₹${formatAmount(totalDuesAmount)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF9F1239)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE11D48))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "$totalDuesTenantsCount Tenants Due",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (tenantDuesList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusPaid,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "All Clear! No pending dues.",
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(tenantDuesList, key = { it.tenantId }) { dueItem ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    border = BorderStroke(0.8.dp, CardBorderLight)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Top: Business / Shop Name (Bold & prominent, wrapped)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = if (dueItem.businessName.isNotBlank()) dueItem.businessName else "Shop ${dueItem.shopNumbers}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                lineHeight = 18.sp,
                                                color = NavyDark,
                                                modifier = Modifier.weight(1f),
                                                softWrap = true
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "₹${formatAmount(dueItem.totalDues)}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = Color(0xFFE11D48)
                                            )
                                        }

                                        // Below: Tenant Name and Phone (Wrapped)
                                        Text(
                                            text = buildString {
                                                append(dueItem.tenantName)
                                                if (dueItem.phone.isNotBlank()) {
                                                    append(" • ${dueItem.phone}")
                                                }
                                            },
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            softWrap = true
                                        )

                                        // Details: Opening Arrears + Current Month Dues
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (dueItem.openingDues > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFFFEF3C7))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "Prior Arrears: ₹${formatAmount(dueItem.openingDues)}",
                                                        fontSize = 10.5.sp,
                                                        color = Color(0xFF92400E),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                            if (dueItem.currentMonthDues > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFFFEE2E2))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "$selectedMonth: ₹${formatAmount(dueItem.currentMonthDues)}",
                                                        fontSize = 10.5.sp,
                                                        color = Color(0xFF991B1B),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }

                                        // Quick Action Button: Collect Rent if this month's record exists
                                        dueItem.currentMonthRent?.let { rentRec ->
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    showDuesBreakdownDialog = false
                                                    rentToCollect = rentRec
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                                                contentPadding = PaddingValues(vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "Collect ₹${formatAmount(rentRec.pendingAmount)}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showDuesBreakdownDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RentItemCard(
    rent: RentRecord,
    businessName: String = "",
    tenantPhone: String = "",
    billingCycleLabel: String = "",
    onCollect: () -> Unit,
    onEditPayment: (() -> Unit)? = null,
    onEditElectricityMeter: (() -> Unit)? = null,
    onShare: () -> Unit,
    onWhatsAppReminder: () -> Unit,
    onDownloadReceipt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBg, statusBorder, statusLabel) = when {
        rent.isPaid -> Quadruple(StatusPaid, StatusPaidBg, StatusPaidBorder, "PAID ✓")
        rent.isPartial -> Quadruple(StatusPartial, StatusPartialBg, StatusPartialBorder, "PARTIAL (₹${rent.pendingAmount.toInt()} DUE)")
        else -> Quadruple(StatusPending, StatusPendingBg, StatusPendingBorder, "PENDING (₹${rent.pendingAmount.toInt()} DUE)")
    }

    val isFlatProperty = remember(rent.isPersonal, rent.shopNumber, rent.isFlat) {
        rent.isFlat
    }

    val individualShops = remember(rent.shopNumber, rent.isPersonal, isFlatProperty) {
        if (rent.shopNumber.isBlank()) emptyList<String>()
        else rent.shopNumber
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { raw ->
                if (isFlatProperty) {
                    if (raw.startsWith("Flat", ignoreCase = true) || raw.startsWith("Unit", ignoreCase = true) || raw.startsWith("Room", ignoreCase = true)) raw
                    else "Flat $raw"
                } else {
                    if (raw.startsWith("Shop", ignoreCase = true)) raw else "Shop $raw"
                }
            }
    }
    val shopCount = individualShops.size.coerceAtLeast(1)

    // Primary display title: Shop/Business Name at top. If business name is blank, fallback to Flat/Shop number(s) or Tenant Name
    val topShopName = remember(businessName, rent.shopNumber, rent.tenantName, rent.isPersonal, isFlatProperty) {
        when {
            businessName.isNotBlank() -> businessName
            rent.shopNumber.isNotBlank() -> if (isFlatProperty) (if (rent.shopNumber.startsWith("Flat", ignoreCase = true) || rent.shopNumber.startsWith("Unit", ignoreCase = true)) rent.shopNumber else "Flat ${rent.shopNumber}") else "Shop ${rent.shopNumber}"
            else -> rent.tenantName
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rent_item_${rent.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Avatar + Shop Name at TOP, Tenant Name & Phone below (Left) and Badges (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isFlatProperty) Color(0xFF0F766E) else NavyDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isFlatProperty -> Icons.Filled.Apartment
                                businessName.isNotBlank() -> Icons.Filled.Store
                                else -> Icons.Filled.Person
                            },
                            contentDescription = null,
                            tint = if (isFlatProperty) Color.White else GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(9.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        // 1. TOP: Shop/Flat Name / Business Name (Bold, wrapped cleanly for full readability)
                        Text(
                            text = topShopName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            lineHeight = 19.sp,
                            color = if (isFlatProperty) Color(0xFF0F766E) else NavyDark,
                            softWrap = true
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // 2. BELOW: Tenant Name + Phone Number (Wrapped cleanly)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = buildString {
                                    append(rent.tenantName)
                                    if (tenantPhone.isNotBlank()) {
                                        append(" • $tenantPhone")
                                    }
                                },
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                softWrap = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Badges Row: Personal + Shop Count + Cycle + Status + Edit Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (rent.isPersonal) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F766E).copy(alpha = 0.12f))
                                .border(0.5.dp, Color(0xFF0F766E).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Personal",
                                color = Color(0xFF0F766E),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (isFlatProperty) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F766E).copy(alpha = 0.12f))
                                .border(0.5.dp, Color(0xFF0F766E).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Flat",
                                color = Color(0xFF0F766E),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (shopCount > 1) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isFlatProperty) Color(0xFF0F766E) else NavyDark)
                                .border(0.5.dp, if (isFlatProperty) Color(0xFF2DD4BF).copy(alpha = 0.5f) else GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isFlatProperty) Icons.Filled.Apartment else Icons.Filled.Store,
                                    contentDescription = null,
                                    tint = if (isFlatProperty) Color.White else GoldAccent,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                val countLabel = if (isFlatProperty) "$shopCount Flats" else "$shopCount Shops"
                                Text(
                                    text = countLabel,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (billingCycleLabel.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF3C7))
                                .border(0.5.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = billingCycleLabel,
                                color = Color(0xFFB45309),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusBg)
                            .border(0.5.dp, statusBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (rent.amountPaid > 0 && onEditPayment != null) {
                        IconButton(
                            onClick = onEditPayment,
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("edit_payment_icon_${rent.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit / Correct Payment",
                                tint = NavyPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // ==================== ASSIGNED FLATS / SHOPS CHIPS (FLOW ROW) ====================
            if (individualShops.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    individualShops.forEach { unitName ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isFlatProperty) Color(0xFF0F766E).copy(alpha = 0.08f) else NavyDark.copy(alpha = 0.07f))
                                .border(
                                    0.5.dp,
                                    if (isFlatProperty) Color(0xFF0F766E).copy(alpha = 0.3f) else NavyDark.copy(alpha = 0.2f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 7.dp, vertical = 2.5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isFlatProperty) Icons.Filled.Apartment else Icons.Filled.Store,
                                    contentDescription = null,
                                    tint = if (isFlatProperty) Color(0xFF0F766E) else NavyPrimary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = unitName,
                                    color = if (isFlatProperty) Color(0xFF0F766E) else NavyDark,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ==================== ELECTRICITY BILL BADGE (FOR FLATS & METERED UNITS) ====================
            // ==================== ELECTRICITY BILL / METER READING ====================
            if (isFlatProperty || rent.hasMeterReading || rent.electricityBill > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                if (rent.hasMeterReading || rent.electricityBill > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(10.dp))
                            .clickable(enabled = onEditElectricityMeter != null) { onEditElectricityMeter?.invoke() }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD97706).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ElectricMeter,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (rent.hasMeterReading) "Electricity (Meter Reading)" else "Electricity Bill Added",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                        if (onEditElectricityMeter != null) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Filled.Edit,
                                                contentDescription = "Edit Meter",
                                                tint = Color(0xFFB45309),
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                    if (rent.hasMeterReading) {
                                        Text(
                                            text = "Meter: ${rent.prevMeterReading.toInt()} → ${rent.currentMeterReading.toInt()} (${rent.unitsConsumed.toInt()} u @ ₹${rent.electricityRatePerUnit.toInt()})",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFB45309)
                                        )
                                    } else {
                                        val flatBaseRent = (rent.amountDue - rent.electricityBill).coerceAtLeast(0.0)
                                        Text(
                                            text = "Flat: ₹${formatAmount(flatBaseRent)} + Light: ₹${formatAmount(rent.electricityBill)}",
                                            fontSize = 9.sp,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+₹${formatAmount(rent.electricityBill)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Electricity",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                } else {
                    // No meter reading yet entered
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F766E).copy(alpha = 0.06f))
                            .border(1.dp, Color(0xFF0F766E).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .clickable(enabled = onEditElectricityMeter != null) { onEditElectricityMeter?.invoke() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.ElectricMeter,
                                    contentDescription = null,
                                    tint = Color(0xFF0F766E),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Electricity Meter Reading",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F766E)
                                    )
                                    Text(
                                        text = "Tap to enter meter reading ((Current - Prev) × Rate)",
                                        fontSize = 8.5.sp,
                                        color = Color(0xFF0F766E).copy(alpha = 0.8f)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F766E))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "+ Enter Reading",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // ==================== PMC TAX BADGE (IF APPLIED - COMMERCIAL ONLY) ====================
            if (!isFlatProperty && (rent.hasPmcTax || rent.pmcTax > 0)) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldLight.copy(alpha = 0.5f))
                        .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = GoldDark,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Annual PMC Municipal Tax (Anniversary)",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldDark
                                )
                                if (shopCount > 1) {
                                    Text(
                                        text = "₹1,000 × $shopCount shops included",
                                        fontSize = 8.5.sp,
                                        color = GoldDark.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "+₹${formatAmount(rent.pmcTax)} (Annual)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Amount breakdown micro-ledger
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
                    val billHeader = if (billingCycleLabel.isNotBlank()) "${billingCycleLabel.uppercase()} BILL" else "TOTAL BILL"
                    Text(
                        text = billHeader,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "₹${formatAmount(rent.amountDue)}", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = NavyDark)
                    if ((isFlatProperty || rent.electricityBill > 0) && rent.electricityBill > 0) {
                        val flatRent = (rent.amountDue - rent.electricityBill).coerceAtLeast(0.0)
                        Text(
                            text = "Rent: ₹${formatAmount(flatRent)} + Light: ₹${formatAmount(rent.electricityBill)}",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (shopCount > 1) {
                        val baseRent = if (rent.hasPmcTax) (rent.amountDue - rent.pmcTax).coerceAtLeast(0.0) else rent.amountDue
                        val unitWord = if (isFlatProperty) "flat" else "shop"
                        Text(
                            text = "(₹${formatAmount(baseRent / shopCount)}/$unitWord)",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (rent.hasPmcTax || rent.pmcTax > 0) {
                        Text(
                            text = "Base: ₹${formatAmount((rent.amountDue - rent.pmcTax).coerceAtLeast(0.0))} + PMC ₹${formatAmount(rent.pmcTax)}",
                            fontSize = 8.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column {
                    Text(
                        text = "COLLECTED",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${formatAmount(rent.amountPaid)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = if (rent.amountPaid > 0) StatusPaid else NavyDark
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BALANCE DUE",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        color = if (rent.pendingAmount > 0) StatusPending else StatusPaid
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${formatAmount(rent.pendingAmount)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (rent.pendingAmount > 0) StatusPending else StatusPaid
                    )
                }
            }

            if (rent.notes.isNotBlank() || rent.paidDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (rent.paidDate.isNotBlank()) {
                        Text(
                            text = "Paid on: ${rent.paidDate} (${rent.paymentMode})",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (rent.notes.isNotBlank()) {
                        Text(
                            text = rent.notes,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row 1: Primary Action & WhatsApp Reminder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Collect Button (or Edit Payment if paid)
                Button(
                    onClick = if (rent.isPaid && onEditPayment != null) onEditPayment else onCollect,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("collect_rent_btn_${rent.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (rent.isPaid) MaterialTheme.colorScheme.surfaceVariant else NavyPrimary,
                        contentColor = if (rent.isPaid) NavyPrimary else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (rent.isPaid) Icons.Filled.Edit else Icons.Filled.Paid,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (rent.isPaid) "Edit Payment" else "Collect Rent",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // WhatsApp Reminder Button (for pending/partial) or Download PDF Button (for paid)
                if (rent.pendingAmount > 0) {
                    Button(
                        onClick = onWhatsAppReminder,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("wa_reminder_btn_${rent.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF15803D), // WhatsApp Green
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "💬 Reminder",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onDownloadReceipt,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("download_pdf_btn_${rent.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Download PDF",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons Row 2: Secondary Share & Download options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (rent.pendingAmount > 0) {
                    // Download PDF for Pending
                    OutlinedButton(
                        onClick = onDownloadReceipt,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("download_pdf_pending_${rent.id}"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CardBorderLight),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Download PDF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = NavyPrimary
                        )
                    }
                }

                if ((isFlatProperty || rent.hasMeterReading || rent.electricityBill > 0) && onEditElectricityMeter != null) {
                    OutlinedButton(
                        onClick = onEditElectricityMeter,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("edit_meter_btn_${rent.id}"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFB45309)
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ElectricMeter,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (rent.hasMeterReading) "Meter" else "+ Meter",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                // Share Receipt
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("share_receipt_btn_${rent.id}"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CardBorderLight),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = NavyPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Share Receipt",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = NavyPrimary
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun StatCard(
    title: String,
    amount: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = if (onClick != null) {
            modifier.clickable { onClick() }
        } else {
            modifier
        },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, CardBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(13.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = amount,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (accentColor == NavyPrimary) NavyDark else accentColor,
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        String.format("%,d", amount.toLong())
    } else {
        String.format("%,.2f", amount)
    }
}

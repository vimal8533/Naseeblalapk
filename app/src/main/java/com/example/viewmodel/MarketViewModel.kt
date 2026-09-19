package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirebaseRealtimeSync
import com.example.data.SyncStatus
import com.example.model.ActivityLog
import com.example.model.AppWorkspaceMode
import com.example.model.MonthlyReminderRecord
import com.example.model.RentRecord
import com.example.model.Shop
import com.example.model.SubAdminUser
import com.example.model.Tenant
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.util.BillingCycleHelper
import com.example.util.PmcTaxHelper
import com.example.util.SmsReminderHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MarketViewModel(application: Application) : AndroidViewModel(application) {

    private val sync = FirebaseRealtimeSync(application.applicationContext)
    private val authPrefs = application.getSharedPreferences("nlm_auth_prefs", Context.MODE_PRIVATE)

    // User session state
    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    // Filters and search
    private val _selectedMonth = MutableStateFlow("September")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(2026)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _rentStatusFilter = MutableStateFlow("ALL") // ALL, PENDING, PARTIAL, PAID
    val rentStatusFilter: StateFlow<String> = _rentStatusFilter.asStateFlow()

    // Workspace Mode: Public Market (Commercial) vs Private Personal (Flats/Rooms)
    private val _workspaceMode = MutableStateFlow(AppWorkspaceMode.PUBLIC_MARKET)
    val workspaceMode: StateFlow<AppWorkspaceMode> = _workspaceMode.asStateFlow()

    // Has user selected their workspace on this launch?
    private val _hasSelectedWorkspace = MutableStateFlow(false)
    val hasSelectedWorkspace: StateFlow<Boolean> = _hasSelectedWorkspace.asStateFlow()

    // Raw Data streams from Firebase sync
    private val rawShops: StateFlow<List<Shop>> = sync.shops
    private val rawTenants: StateFlow<List<Tenant>> = sync.tenants
    private val rawRents: StateFlow<List<RentRecord>> = sync.rents
    val subAdmins: StateFlow<List<SubAdminUser>> = sync.subAdmins
    val activityLogs: StateFlow<List<ActivityLog>> = sync.activityLogs
    val monthlyReminders: StateFlow<Map<String, MonthlyReminderRecord>> = sync.monthlyReminders
    val syncStatus: StateFlow<SyncStatus> = sync.syncStatus
    val lastSyncedAt: StateFlow<Long> = sync.lastSyncedAt
    val isLoading: StateFlow<Boolean> = sync.isInitialLoading

    private var presenceJob: Job? = null

    // Filtered data streams respecting:
    // 1. Current Workspace Mode (PUBLIC_MARKET vs PRIVATE_PERSONAL)
    // 2. User role & permissions:
    //    - In PUBLIC_MARKET: shows all commercial shops/tenants/rents (isPersonal == false)
    //    - In PRIVATE_PERSONAL: shows personal flats/tenants/rents (isPersonal == true).
    //      For Master Admin: all personal properties. For Sub-Admin: their own personal properties.
    val shops: StateFlow<List<Shop>> = combine(rawShops, currentUser, workspaceMode) { allShops, user, mode ->
        when (mode) {
            AppWorkspaceMode.PUBLIC_MARKET -> {
                allShops.filter { !it.isPersonal }
            }
            AppWorkspaceMode.PRIVATE_PERSONAL -> {
                if (user == null || user.isAdmin) {
                    allShops.filter { it.isPersonal }
                } else {
                    allShops.filter { it.isPersonal && it.ownerSubAdminId == user.subAdminId }
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tenants: StateFlow<List<Tenant>> = combine(rawTenants, currentUser, workspaceMode) { allTenants, user, mode ->
        val filtered = when (mode) {
            AppWorkspaceMode.PUBLIC_MARKET -> {
                allTenants.filter { !it.isPersonal }
            }
            AppWorkspaceMode.PRIVATE_PERSONAL -> {
                if (user == null || user.isAdmin) {
                    allTenants.filter { it.isPersonal }
                } else {
                    allTenants.filter { it.isPersonal && it.ownerSubAdminId == user.subAdminId }
                }
            }
        }
        filtered.filter { it.isActive }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedTenants: StateFlow<List<Tenant>> = combine(rawTenants, currentUser, workspaceMode) { allTenants, user, mode ->
        val filtered = when (mode) {
            AppWorkspaceMode.PUBLIC_MARKET -> {
                allTenants.filter { !it.isPersonal }
            }
            AppWorkspaceMode.PRIVATE_PERSONAL -> {
                if (user == null || user.isAdmin) {
                    allTenants.filter { it.isPersonal }
                } else {
                    allTenants.filter { it.isPersonal && it.ownerSubAdminId == user.subAdminId }
                }
            }
        }
        filtered.filter { !it.isActive }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active rents strictly filtered for currently active tenants (prevents deleted tenants from inflating totals)
    val rents: StateFlow<List<RentRecord>> = combine(rawRents, rawTenants, currentUser, workspaceMode) { allRents, allTenants, user, mode ->
        val activeTenantIds = allTenants.filter { it.isActive }.map { it.id }.toSet()
        val modeRents = when (mode) {
            AppWorkspaceMode.PUBLIC_MARKET -> {
                allRents.filter { !it.isPersonal }
            }
            AppWorkspaceMode.PRIVATE_PERSONAL -> {
                if (user == null || user.isAdmin) {
                    allRents.filter { it.isPersonal }
                } else {
                    allRents.filter { it.isPersonal && it.ownerSubAdminId == user.subAdminId }
                }
            }
        }
        modeRents.filter { it.tenantId in activeTenantIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All historical rents (including deleted/archived tenants) for archive audit & ledger
    val allHistoricalRents: StateFlow<List<RentRecord>> = combine(rawRents, currentUser, workspaceMode) { allRents, user, mode ->
        when (mode) {
            AppWorkspaceMode.PUBLIC_MARKET -> {
                allRents.filter { !it.isPersonal }
            }
            AppWorkspaceMode.PRIVATE_PERSONAL -> {
                if (user == null || user.isAdmin) {
                    allRents.filter { it.isPersonal }
                } else {
                    allRents.filter { it.isPersonal && it.ownerSubAdminId == user.subAdminId }
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered rents based on search, month, year, and status filter
    val filteredRents: StateFlow<List<RentRecord>> = combine(
        rents,
        selectedMonth,
        selectedYear,
        searchQuery,
        rentStatusFilter
    ) { visibleRents, month, year, query, filter ->
        visibleRents.filter { rent ->
            val matchesMonthYear = rent.month.equals(month, ignoreCase = true) && rent.year == year
            val matchesQuery = query.isBlank() ||
                    rent.shopNumber.contains(query, ignoreCase = true) ||
                    rent.tenantName.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                "PENDING" -> rent.isPending
                "PARTIAL" -> rent.isPartial
                "PAID" -> rent.isPaid
                else -> true
            }
            matchesMonthYear && matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        restoreSession()
        observeSessionValidity()
    }

    private fun restoreSession() {
        val loggedIn = authPrefs.getBoolean("is_logged_in", false)
        if (loggedIn) {
            val username = authPrefs.getString("username", "") ?: ""
            val roleStr = authPrefs.getString("role", "") ?: ""
            val savedAdminName = authPrefs.getString("admin_name", "Vimal Kumar") ?: "Vimal Kumar"
            val savedAdminPhone = authPrefs.getString("admin_phone", "9876543210") ?: "9876543210"
            var displayName = authPrefs.getString("display_name", "") ?: ""
            var phone = authPrefs.getString("phone", "") ?: ""
            val subAdminId = authPrefs.getString("sub_admin_id", "") ?: ""
            val sessionId = authPrefs.getString("session_id", "") ?: ""
            val passwordSnapshot = authPrefs.getString("password_snapshot", "") ?: ""
            val canManagePersonalTenants = authPrefs.getBoolean("can_manage_personal_tenants", false)

            val role = if (roleStr == UserRole.ADMIN.name) UserRole.ADMIN else UserRole.SUB_ADMIN
            if (role == UserRole.ADMIN) {
                if (displayName.isBlank() || displayName.contains("(Master Admin)")) {
                    displayName = savedAdminName
                }
                if (phone.isBlank()) {
                    phone = savedAdminPhone
                }
            }
            val session = UserSession(
                username = username,
                role = role,
                displayName = displayName,
                phone = phone,
                subAdminId = subAdminId,
                sessionId = sessionId,
                passwordSnapshot = passwordSnapshot,
                canManagePersonalTenants = canManagePersonalTenants
            )
            _currentUser.value = session
            if (session.isSubAdmin && subAdminId.isNotBlank()) {
                startPresenceTracking(subAdminId)
            }
        }
    }

    private fun observeSessionValidity() {
        viewModelScope.launch {
            subAdmins.collect { currentSubAdmins ->
                val user = _currentUser.value
                // Only monitor if currently logged in as a Sub-Admin
                if (user != null && user.isSubAdmin && user.subAdminId.isNotBlank()) {
                    val matchingRecord = currentSubAdmins.find { it.id == user.subAdminId }
                    if (matchingRecord == null) {
                        // Deleted by Master Admin -> force logout immediately
                        logout()
                    } else if (!matchingRecord.isActive) {
                        // Deactivated by Master Admin -> force logout immediately
                        logout()
                    } else if (matchingRecord.activeSessionId.isNotBlank() &&
                        user.sessionId.isNotBlank() &&
                        matchingRecord.activeSessionId != user.sessionId
                    ) {
                        // Logged in from another device -> force logout old session immediately
                        logout()
                    } else if (user.passwordSnapshot.isNotBlank() &&
                        matchingRecord.password != user.passwordSnapshot
                    ) {
                        // Password changed by Master Admin -> force logout immediately
                        logout()
                    } else if (user.canManagePersonalTenants != matchingRecord.canManagePersonalTenants) {
                        // Permission updated dynamically by Master Admin -> update session
                        val updated = user.copy(canManagePersonalTenants = matchingRecord.canManagePersonalTenants)
                        saveSession(updated)
                        _currentUser.value = updated
                    }
                }
            }
        }
    }

    fun login(usernameInput: String, passwordInput: String, role: UserRole = UserRole.ADMIN): Result<UserSession> {
        val trimmedUser = usernameInput.trim()
        val trimmedPass = passwordInput.trim()

        if (trimmedUser.isBlank() || trimmedPass.isBlank()) {
            return Result.failure(Exception("Please enter both username and password."))
        }

        if (role == UserRole.ADMIN) {
            // 1. Check Master Admin credentials
            if (trimmedUser == "v1i2m3a4l" && trimmedPass == "Vivani@1928") {
                val savedAdminName = authPrefs.getString("admin_name", "Vimal Kumar") ?: "Vimal Kumar"
                val savedAdminPhone = authPrefs.getString("admin_phone", "9876543210") ?: "9876543210"
                val adminSession = UserSession(
                    username = "v1i2m3a4l",
                    role = UserRole.ADMIN,
                    displayName = savedAdminName,
                    phone = savedAdminPhone,
                    subAdminId = ""
                )
                saveSession(adminSession)
                _currentUser.value = adminSession
                return Result.success(adminSession)
            }

            // Check if this username belongs to a Sub-Admin
            val isSubAdminUser = subAdmins.value.any { it.username.equals(trimmedUser, ignoreCase = true) }
            if (isSubAdminUser) {
                return Result.failure(Exception("This is a Sub-Admin account. Please select the 'Sub Admin' option to sign in."))
            }

            return Result.failure(Exception("Invalid Master Admin username or password!"))
        } else {
            // 2. Sub-Admin login check
            if (trimmedUser.equals("v1i2m3a4l", ignoreCase = true)) {
                return Result.failure(Exception("This is the Master Admin account. Please select the 'Master Admin' option to sign in."))
            }

            val matchedSubAdmin = subAdmins.value.firstOrNull {
                it.username.equals(trimmedUser, ignoreCase = true) && it.password == trimmedPass
            }

            if (matchedSubAdmin != null) {
                if (!matchedSubAdmin.isActive) {
                    return Result.failure(Exception("This Sub-Admin account is deactivated. Please contact Master Admin."))
                }

                val newSessionId = UUID.randomUUID().toString()
                val updatedSubAdmin = matchedSubAdmin.copy(
                    activeSessionId = newSessionId,
                    lastActiveAt = System.currentTimeMillis()
                )

                // Push new session ID to Firebase immediately so previous sessions get logged out
                viewModelScope.launch {
                    sync.saveSubAdmin(updatedSubAdmin)
                }

                val subAdminSession = UserSession(
                    username = matchedSubAdmin.username,
                    role = UserRole.SUB_ADMIN,
                    displayName = matchedSubAdmin.name.ifBlank { matchedSubAdmin.username },
                    phone = matchedSubAdmin.phone,
                    subAdminId = matchedSubAdmin.id,
                    sessionId = newSessionId,
                    passwordSnapshot = matchedSubAdmin.password,
                    canManagePersonalTenants = matchedSubAdmin.canManagePersonalTenants
                )
                saveSession(subAdminSession)
                _currentUser.value = subAdminSession
                startPresenceTracking(matchedSubAdmin.id)
                return Result.success(subAdminSession)
            }

            return Result.failure(Exception("Invalid Sub-Admin username or password! Please check credentials given by Master Admin."))
        }
    }

    private fun startPresenceTracking(subAdminId: String) {
        presenceJob?.cancel()
        presenceJob = viewModelScope.launch {
            while (isActive) {
                sync.updateSubAdminPresence(subAdminId)
                delay(30_000) // Update heartbeat every 30 seconds
            }
        }
    }

    private fun saveSession(session: UserSession) {
        authPrefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("username", session.username)
            .putString("role", session.role.name)
            .putString("display_name", session.displayName)
            .putString("phone", session.phone)
            .putString("sub_admin_id", session.subAdminId)
            .putString("session_id", session.sessionId)
            .putString("password_snapshot", session.passwordSnapshot)
            .putBoolean("can_manage_personal_tenants", session.canManagePersonalTenants)
            .apply()
    }

    fun updateAdminProfile(name: String, phone: String) {
        val trimmedName = name.trim().ifBlank { "Vimal Kumar" }
        val trimmedPhone = phone.trim()
        authPrefs.edit()
            .putString("admin_name", trimmedName)
            .putString("admin_phone", trimmedPhone)
            .apply()
        val current = _currentUser.value
        if (current != null && current.isAdmin) {
            val updated = current.copy(displayName = trimmedName, phone = trimmedPhone)
            _currentUser.value = updated
            saveSession(updated)
        }
    }

    fun logout() {
        presenceJob?.cancel()
        presenceJob = null
        authPrefs.edit().clear().apply()
        _currentUser.value = null
        _hasSelectedWorkspace.value = false
        _workspaceMode.value = AppWorkspaceMode.PUBLIC_MARKET
    }

    fun selectWorkspace(mode: AppWorkspaceMode) {
        _workspaceMode.value = mode
        _hasSelectedWorkspace.value = true
    }

    fun toggleWorkspace() {
        val next = if (_workspaceMode.value == AppWorkspaceMode.PUBLIC_MARKET) {
            AppWorkspaceMode.PRIVATE_PERSONAL
        } else {
            AppWorkspaceMode.PUBLIC_MARKET
        }
        _workspaceMode.value = next
        _hasSelectedWorkspace.value = true
    }

    fun clearAllData() {
        viewModelScope.launch {
            sync.clearAllData()
        }
    }

    fun setMonth(month: String) {
        _selectedMonth.value = month
    }

    fun setYear(year: Int) {
        _selectedYear.value = year
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setRentStatusFilter(filter: String) {
        _rentStatusFilter.value = filter
    }

    // --- Shop Operations ---

    fun addOrUpdateShop(
        shopNumber: String,
        floor: String,
        sizeSqFt: String,
        baseRent: Double,
        maintenanceCharge: Double,
        electricityMeter: String,
        notes: String,
        existingId: String? = null,
        isPersonal: Boolean = false,
        ownerSubAdminId: String = ""
    ) {
        viewModelScope.launch {
            val id = existingId ?: "shop_${shopNumber.filter { it.isLetterOrDigit() }.lowercase()}_${System.currentTimeMillis() % 10000}"
            val existing = rawShops.value.find { it.id == id }
            val modifierName = currentUser.value?.let {
                if (it.isAdmin) "Admin (${it.displayName})" else it.displayName
            } ?: "Admin"

            val effectiveIsPersonal = if (existing != null) existing.isPersonal else isPersonal
            val effectiveOwnerId = if (existing != null) {
                existing.ownerSubAdminId
            } else if (isPersonal) {
                ownerSubAdminId.ifBlank { currentUser.value?.subAdminId ?: "" }
            } else {
                ""
            }

            val shop = Shop(
                id = id,
                shopNumber = shopNumber.trim(),
                floor = floor.trim(),
                sizeSqFt = sizeSqFt.trim(),
                baseRent = baseRent,
                maintenanceCharge = maintenanceCharge,
                tenantId = existing?.tenantId,
                tenantName = existing?.tenantName,
                status = existing?.status ?: "VACANT",
                electricityMeter = electricityMeter.trim(),
                notes = notes.trim(),
                isPersonal = effectiveIsPersonal,
                ownerSubAdminId = effectiveOwnerId,
                lastModifiedBy = modifierName,
                updatedAt = System.currentTimeMillis()
            )
            sync.saveShop(shop)

            val isNew = existing == null
            sync.logActivity(ActivityLog(
                id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                timestamp = System.currentTimeMillis(),
                userName = currentUser.value?.displayName ?: "User",
                userRole = currentUser.value?.role?.name ?: "ADMIN",
                actionType = if (isNew) "ADD_SHOP" else "EDIT_SHOP",
                title = if (isNew) "Created ${if (effectiveIsPersonal) "Flat" else "Shop"} ${shop.shopNumber}" else "Edited ${if (effectiveIsPersonal) "Flat" else "Shop"} ${shop.shopNumber}",
                details = "Rent: ₹${baseRent.toInt()}, Floor: ${floor.ifBlank { "Ground" }}${if (effectiveIsPersonal) " [Personal]" else ""}"
            ))
        }
    }

    fun deleteShop(shopId: String) {
        viewModelScope.launch {
            val s = shops.value.find { it.id == shopId }
            sync.removeShop(shopId)
            sync.logActivity(ActivityLog(
                id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                timestamp = System.currentTimeMillis(),
                userName = currentUser.value?.displayName ?: "User",
                userRole = currentUser.value?.role?.name ?: "ADMIN",
                actionType = "DELETE_SHOP",
                title = "Deleted Shop ${s?.shopNumber ?: shopId}",
                details = "Floor: ${s?.floor ?: "Unknown"}"
            ))
        }
    }

    // --- Tenant Operations ---

    fun addOrUpdateTenant(
        name: String,
        businessName: String,
        phone: String,
        shopNumber: String,
        numberOfShops: Int,
        advanceDeposit: Double,
        monthlyRent: Double,
        previousDues: Double = 0.0,
        incrementYears: Int = 1,
        incrementPercent: Double = 5.0,
        billingCycle: String = "MONTHLY",
        joiningDate: String,
        idProof: String,
        notes: String,
        existingId: String? = null,
        isPersonal: Boolean = false,
        ownerSubAdminId: String = "",
        electricityBill: Double = 0.0
    ) {
        viewModelScope.launch {
            val id = existingId ?: "tenant_${System.currentTimeMillis()}"
            val cleanShopNumber = shopNumber.trim()
            val validNumberOfShops = numberOfShops.coerceAtLeast(1)

            val existingTenant = rawTenants.value.find { it.id == id }
            val effectiveIsPersonal = if (existingTenant != null) existingTenant.isPersonal else isPersonal
            val effectiveElectricityBill = if (effectiveIsPersonal) electricityBill.coerceAtLeast(0.0) else 0.0
            val effectiveOwnerId = if (existingTenant != null) {
                existingTenant.ownerSubAdminId
            } else if (isPersonal) {
                ownerSubAdminId.ifBlank { currentUser.value?.subAdminId ?: "" }
            } else {
                ""
            }

            // Annual PMC Tax rule: ₹1,000 per shop billed ONLY ONCE A YEAR in anniversary month (Market shops only, 0 for personal flat)
            val isAnniversaryMonth = PmcTaxHelper.isPmcTaxDueInMonth(joiningDate, selectedMonth.value, selectedYear.value)
            val pmcTax = if (!effectiveIsPersonal && isAnniversaryMonth) PmcTaxHelper.getPmcTaxForShops(validNumberOfShops) else 0.0

            val isNewTenant = existingId == null
            val modifierName = currentUser.value?.let {
                if (it.isAdmin) "Admin (${it.displayName})" else it.displayName
            } ?: "Admin"

            val tenant = Tenant(
                id = id,
                name = name.trim(),
                businessName = businessName.trim(),
                phone = phone.trim(),
                shopId = "",
                shopNumber = cleanShopNumber,
                assignedShopIds = emptyList(),
                numberOfShops = validNumberOfShops,
                advanceDeposit = advanceDeposit,
                monthlyRent = monthlyRent,
                previousDues = previousDues,
                incrementYears = incrementYears.coerceAtLeast(1),
                incrementPercent = incrementPercent.coerceAtLeast(0.0),
                billingCycle = billingCycle.ifBlank { "MONTHLY" },
                joiningDate = joiningDate.trim(),
                idProof = idProof.trim(),
                isActive = true,
                notes = notes.trim(),
                electricityBill = effectiveElectricityBill,
                isPersonal = effectiveIsPersonal,
                ownerSubAdminId = effectiveOwnerId,
                lastModifiedBy = modifierName,
                updatedAt = System.currentTimeMillis()
            )
            sync.saveTenant(tenant)

            // Calculate cycle rent: if cycle > 1, charge cycleRent (monthlyRent * cycleMonths) in due month
            val isDue = BillingCycleHelper.isRentDueInMonth(joiningDate, tenant.cycleMonths, selectedMonth.value, selectedYear.value)
            val cycleRent = tenant.monthlyRent * tenant.cycleMonths
            // For personal flat: totalAmountDue includes monthly electricity bill
            val totalAmountDue = if (isDue) (cycleRent + pmcTax + effectiveElectricityBill) else effectiveElectricityBill
            val cycleNote = when {
                tenant.cycleMonths > 1 && isDue -> "${tenant.billingCycleDisplay} Cycle Due (${tenant.cycleMonths} Months)"
                tenant.cycleMonths > 1 && !isDue -> "Covered under ${tenant.billingCycleDisplay} Cycle"
                else -> ""
            }
            val initialStatus = if (totalAmountDue > 0.0) "PENDING" else "PAID"

            val detailsText = buildString {
                append("Rent: ₹${monthlyRent.toInt()}/mo (${tenant.billingCycleDisplay})")
                if (effectiveElectricityBill > 0.0) {
                    append(", Electricity: ₹${effectiveElectricityBill.toInt()}/mo")
                }
                append(", Property: ${cleanShopNumber.ifBlank { "Unassigned" }}")
                if (effectiveIsPersonal) append(" [Personal Flat]")
            }

            sync.logActivity(ActivityLog(
                id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                timestamp = System.currentTimeMillis(),
                userName = currentUser.value?.displayName ?: "User",
                userRole = currentUser.value?.role?.name ?: "ADMIN",
                actionType = if (isNewTenant) "ADD_TENANT" else "EDIT_TENANT",
                title = if (isNewTenant) "Added Tenant: $name" else "Edited Tenant: $name",
                details = detailsText
            ))

            // Auto generate/update rent record for current selected month if not present
            val existingRent = rawRents.value.find {
                it.tenantId == id && it.month.equals(selectedMonth.value, ignoreCase = true) && it.year == selectedYear.value
            }
            if (existingRent == null) {
                val newRent = RentRecord(
                    id = "rent_${selectedYear.value}_${selectedMonth.value}_${id}",
                    shopId = "",
                    shopNumber = cleanShopNumber,
                    tenantId = id,
                    tenantName = name.trim(),
                    month = selectedMonth.value,
                    year = selectedYear.value,
                    amountDue = totalAmountDue,
                    amountPaid = 0.0,
                    pmcTax = pmcTax,
                    electricityBill = effectiveElectricityBill,
                    status = initialStatus,
                    dueDate = "10th ${selectedMonth.value}",
                    paidDate = "",
                    paymentMode = "CASH",
                    notes = cycleNote,
                    isPersonal = effectiveIsPersonal,
                    ownerSubAdminId = effectiveOwnerId,
                    receiptNumber = "NLM-${selectedYear.value}-${UUID.randomUUID().toString().take(4).uppercase()}"
                )
                sync.saveRentRecord(newRent)
            } else {
                sync.saveRentRecord(existingRent.copy(
                    shopNumber = cleanShopNumber,
                    tenantName = name.trim(),
                    amountDue = if (existingRent.amountPaid > 0.0) existingRent.amountDue else totalAmountDue,
                    pmcTax = pmcTax,
                    electricityBill = effectiveElectricityBill,
                    status = if (existingRent.amountPaid > 0.0) existingRent.status else initialStatus,
                    notes = if (existingRent.notes.isBlank()) cycleNote else existingRent.notes,
                    isPersonal = effectiveIsPersonal,
                    ownerSubAdminId = effectiveOwnerId
                ))
            }
        }
    }

    fun addOrUpdateTenant(
        name: String,
        businessName: String,
        phone: String,
        selectedShopIds: List<String>,
        advanceDeposit: Double,
        monthlyRent: Double,
        previousDues: Double = 0.0,
        incrementYears: Int = 1,
        incrementPercent: Double = 5.0,
        billingCycle: String = "MONTHLY",
        joiningDate: String,
        idProof: String,
        notes: String,
        existingId: String? = null
    ) {
        val assignedShops = shops.value.filter { it.id in selectedShopIds }
        val shopNumbers = assignedShops.map { it.shopNumber }.filter { it.isNotBlank() }
        val shopNumberStr = if (shopNumbers.isNotEmpty()) shopNumbers.joinToString(", ") else ""
        addOrUpdateTenant(
            name, businessName, phone, shopNumberStr, selectedShopIds.size.coerceAtLeast(1),
            advanceDeposit, monthlyRent, previousDues, incrementYears, incrementPercent,
            billingCycle, joiningDate, idProof, notes, existingId
        )
    }

    fun deleteTenant(tenantId: String) {
        archiveTenant(tenantId, "Vacated / Deleted")
    }

    fun archiveTenant(tenantId: String, reason: String = "Vacated / Deleted") {
        viewModelScope.launch {
            val t = rawTenants.value.find { it.id == tenantId } ?: return@launch
            val exitFormatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val archived = t.copy(
                isActive = false,
                exitDate = exitFormatted,
                exitReason = reason,
                lastModifiedBy = currentUser.value?.displayName ?: "Admin",
                updatedAt = System.currentTimeMillis()
            )
            sync.saveTenant(archived)
            // Also free up any shops that were occupied by this tenant
            val occupiedShops = shops.value.filter { it.tenantId == tenantId }
            for (shop in occupiedShops) {
                val updatedShop = shop.copy(
                    tenantId = null,
                    tenantName = null,
                    status = "VACANT",
                    lastModifiedBy = currentUser.value?.displayName ?: "Admin",
                    updatedAt = System.currentTimeMillis()
                )
                sync.saveShop(updatedShop)
            }
            sync.logActivity(ActivityLog(
                id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                timestamp = System.currentTimeMillis(),
                userName = currentUser.value?.displayName ?: "User",
                userRole = currentUser.value?.role?.name ?: "ADMIN",
                actionType = "ARCHIVE_TENANT",
                title = "Archived Tenant: ${t.name}",
                details = "Moved to Deleted/Exited Archive. Historical rent records preserved."
            ))
        }
    }

    fun restoreTenant(tenantId: String) {
        viewModelScope.launch {
            val t = rawTenants.value.find { it.id == tenantId } ?: return@launch
            val restored = t.copy(
                isActive = true,
                exitDate = "",
                exitReason = "",
                lastModifiedBy = currentUser.value?.displayName ?: "Admin",
                updatedAt = System.currentTimeMillis()
            )
            sync.saveTenant(restored)
            sync.logActivity(ActivityLog(
                id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                timestamp = System.currentTimeMillis(),
                userName = currentUser.value?.displayName ?: "User",
                userRole = currentUser.value?.role?.name ?: "ADMIN",
                actionType = "RESTORE_TENANT",
                title = "Restored Tenant: ${t.name}",
                details = "Tenant restored to Active list."
            ))
        }
    }

    fun permanentlyDeleteTenant(tenantId: String) {
        viewModelScope.launch {
            val t = rawTenants.value.find { it.id == tenantId }
            sync.removeTenant(tenantId)
            sync.removeRentRecordsForTenant(tenantId)
            // Also free up any shops that were occupied by this tenant
            val occupiedShops = shops.value.filter { it.tenantId == tenantId }
            for (shop in occupiedShops) {
                val updatedShop = shop.copy(
                    tenantId = null,
                    tenantName = null,
                    status = "VACANT"
                )
                sync.saveShop(updatedShop)
            }
            sync.logActivity(ActivityLog(
                id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                timestamp = System.currentTimeMillis(),
                userName = currentUser.value?.displayName ?: "User",
                userRole = currentUser.value?.role?.name ?: "ADMIN",
                actionType = "PERMANENT_DELETE_TENANT",
                title = "Permanently Deleted: ${t?.name ?: tenantId}",
                details = "Tenant and all linked dummy records permanently removed."
            ))
        }
    }

    fun cleanOrphanRents() {
        viewModelScope.launch {
            val allTenantIds = rawTenants.value.map { it.id }.toSet()
            val orphanRents = rawRents.value.filter { it.tenantId !in allTenantIds }
            for (orphan in orphanRents) {
                sync.removeRentRecord(orphan.id)
            }
        }
    }

    // --- Rent Payment Recording ---

    fun recordRentPayment(
        rentRecordId: String,
        additionalPayment: Double,
        paymentMode: String,
        notes: String,
        paidDate: String? = null
    ) {
        viewModelScope.launch {
            val record = rents.value.find { it.id == rentRecordId } ?: return@launch
            val newAmountPaid = record.amountPaid + additionalPayment
            val status = when {
                newAmountPaid >= record.amountDue -> "PAID"
                newAmountPaid > 0 -> "PARTIAL"
                else -> "PENDING"
            }
            val effectiveDate = if (!paidDate.isNullOrBlank()) {
                paidDate.trim()
            } else {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            }

            val collector = currentUser.value?.let {
                if (it.isAdmin) "Admin (${it.username})" else "Sub-Admin (${it.displayName})"
            } ?: "Naseeb Lal Market"

            val receipt = if (record.receiptNumber.isNotBlank()) record.receiptNumber
            else "NLM-${record.year}-${UUID.randomUUID().toString().take(4).uppercase()}"

            val updatedRecord = record.copy(
                amountPaid = newAmountPaid,
                status = status,
                paidDate = effectiveDate,
                paymentMode = paymentMode,
                collectedBy = collector,
                receiptNumber = receipt,
                notes = if (notes.isNotBlank()) "${record.notes} | $notes".trimStart(' ', '|') else record.notes,
                updatedAt = System.currentTimeMillis()
            )
            sync.saveRentRecord(updatedRecord)

            sync.logActivity(ActivityLog(
                id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                timestamp = System.currentTimeMillis(),
                userName = currentUser.value?.displayName ?: "User",
                userRole = currentUser.value?.role?.name ?: "ADMIN",
                actionType = "COLLECT_RENT",
                title = "Collected Rent: ₹${additionalPayment.toInt()} (${record.shopNumber})",
                details = "Tenant: ${record.tenantName}, Month: ${record.month} ${record.year}, Mode: $paymentMode, Status: $status"
            ))
        }
    }

    fun correctRentPayment(
        rentRecordId: String,
        newTotalPaid: Double,
        paymentMode: String,
        reason: String,
        paidDate: String? = null
    ) {
        viewModelScope.launch {
            val record = rents.value.find { it.id == rentRecordId } ?: return@launch
            val previousPaid = record.amountPaid
            val clampedPaid = newTotalPaid.coerceAtLeast(0.0)
            val newStatus = when {
                clampedPaid >= record.amountDue && record.amountDue > 0.0 -> "PAID"
                clampedPaid > 0.0 -> "PARTIAL"
                else -> "PENDING"
            }
            val effectiveDate = if (!paidDate.isNullOrBlank()) {
                paidDate.trim()
            } else if (clampedPaid > 0.0) {
                record.paidDate.ifBlank {
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                }
            } else {
                ""
            }

            val editor = currentUser.value?.let {
                if (it.isAdmin) "Admin (${it.username})" else "Sub-Admin (${it.displayName})"
            } ?: "User"

            val correctionNote = "Corrected: ₹${previousPaid.toInt()} ➔ ₹${clampedPaid.toInt()} [Reason: $reason] by $editor"
            val updatedNotes = if (record.notes.isBlank()) correctionNote else "${record.notes} | $correctionNote"

            val updatedRecord = record.copy(
                amountPaid = clampedPaid,
                status = newStatus,
                paidDate = effectiveDate,
                paymentMode = if (clampedPaid > 0.0) paymentMode else record.paymentMode,
                notes = updatedNotes,
                updatedAt = System.currentTimeMillis()
            )
            sync.saveRentRecord(updatedRecord)

            sync.logActivity(ActivityLog(
                id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                timestamp = System.currentTimeMillis(),
                userName = currentUser.value?.displayName ?: "User",
                userRole = currentUser.value?.role?.name ?: "ADMIN",
                actionType = "PAYMENT_CORRECTION",
                title = "Payment Corrected: ₹${previousPaid.toInt()} ➔ ₹${clampedPaid.toInt()} (${record.shopNumber})",
                details = "Tenant: ${record.tenantName}, Month: ${record.month} ${record.year} | Reason: $reason | Editor: $editor"
            ))
        }
    }

    fun generateRentRecordsForMonth(month: String, year: Int) {
        viewModelScope.launch {
            for (tenant in rawTenants.value.filter { it.isActive && (it.shopIdList.isNotEmpty() || it.shopNumber.isNotBlank()) }) {
                val rentId = "rent_${year}_${month}_${tenant.id}"
                val exists = rawRents.value.any {
                    it.id == rentId || (it.tenantId == tenant.id && it.month.equals(month, ignoreCase = true) && it.year == year)
                }
                if (!exists) {
                    val isAnniversaryMonth = PmcTaxHelper.isPmcTaxDueInMonth(tenant.joiningDate, month, year)
                    val pmcTax = if (!tenant.isPersonal && isAnniversaryMonth) PmcTaxHelper.getPmcTaxForShops(tenant.shopCount) else 0.0
                    val electricityBill = if (tenant.isPersonal) tenant.electricityBill else 0.0

                    // Find previous meter reading for personal flat
                    val prevReading = if (tenant.isPersonal) {
                        val prevRentWithReading = rawRents.value
                            .filter { it.tenantId == tenant.id && it.currentMeterReading > 0.0 }
                            .maxByOrNull { it.year * 100 + BillingCycleHelper.monthToOrder(it.month) }
                        prevRentWithReading?.currentMeterReading ?: tenant.lastMeterReading
                    } else 0.0

                    val ratePerUnit = if (tenant.isPersonal) {
                        if (tenant.electricityRatePerUnit > 0.0) tenant.electricityRatePerUnit else 10.0
                    } else 0.0

                    val isDue = BillingCycleHelper.isRentDueInMonth(tenant.joiningDate, tenant.cycleMonths, month, year)

                    if (isDue) {
                        // In due month: multiply base rent by cycle months + electricity bill (if personal flat)
                        val cycleRent = tenant.monthlyRent * tenant.cycleMonths
                        val totalDue = cycleRent + pmcTax + electricityBill
                        val cycleNote = if (tenant.cycleMonths > 1) "${tenant.billingCycleDisplay} Cycle Due (${tenant.cycleMonths} Months)" else ""

                        val rent = RentRecord(
                            id = rentId,
                            shopId = tenant.shopIdList.firstOrNull() ?: tenant.shopId,
                            shopNumber = tenant.shopNumber,
                            tenantId = tenant.id,
                            tenantName = tenant.name,
                            month = month,
                            year = year,
                            amountDue = totalDue,
                            amountPaid = 0.0,
                            pmcTax = pmcTax,
                            electricityBill = electricityBill,
                            prevMeterReading = prevReading,
                            currentMeterReading = 0.0,
                            electricityRatePerUnit = ratePerUnit,
                            status = if (totalDue > 0.0) "PENDING" else "PAID",
                            dueDate = "10th $month",
                            notes = cycleNote,
                            isPersonal = tenant.isPersonal,
                            ownerSubAdminId = tenant.ownerSubAdminId,
                            receiptNumber = "NLM-$year-${UUID.randomUUID().toString().take(4).uppercase()}"
                        )
                        sync.saveRentRecord(rent)
                    } else {
                        // Intermediate month in 3M / 6M / Yearly cycle: base rent covered, but monthly electricity bill may apply for flat!
                        val intermediateDue = electricityBill
                        val rent = RentRecord(
                            id = rentId,
                            shopId = tenant.shopIdList.firstOrNull() ?: tenant.shopId,
                            shopNumber = tenant.shopNumber,
                            tenantId = tenant.id,
                            tenantName = tenant.name,
                            month = month,
                            year = year,
                            amountDue = intermediateDue,
                            amountPaid = 0.0,
                            pmcTax = 0.0,
                            electricityBill = electricityBill,
                            prevMeterReading = prevReading,
                            currentMeterReading = 0.0,
                            electricityRatePerUnit = ratePerUnit,
                            status = if (intermediateDue > 0.0) "PENDING" else "PAID",
                            dueDate = "10th $month",
                            notes = if (intermediateDue > 0.0) "Electricity Due (Rent covered in ${tenant.billingCycleDisplay} cycle)" else "Covered under ${tenant.billingCycleDisplay} Cycle",
                            isPersonal = tenant.isPersonal,
                            ownerSubAdminId = tenant.ownerSubAdminId,
                            receiptNumber = "NLM-$year-${UUID.randomUUID().toString().take(4).uppercase()}"
                        )
                        sync.saveRentRecord(rent)
                    }
                }
            }
        }
    }

    // --- Sub-Admin Management (SUPER ADMIN ONLY) ---

    fun addSubAdmin(username: String, password: String, name: String, phone: String, canManagePersonalTenants: Boolean = false): Result<Unit> {
        if (_currentUser.value?.isAdmin != true) {
            return Result.failure(Exception("Only Master Admin can create Sub-Admins!"))
        }
        val trimmedUser = username.trim()
        val trimmedPass = password.trim()
        if (trimmedUser.isBlank() || trimmedPass.isBlank()) {
            return Result.failure(Exception("Username and Password cannot be empty."))
        }
        if (trimmedUser.equals("v1i2m3a4l", ignoreCase = true)) {
            return Result.failure(Exception("Master Admin username cannot be reused."))
        }
        if (subAdmins.value.any { it.username.equals(trimmedUser, ignoreCase = true) }) {
            return Result.failure(Exception("This username already exists."))
        }

        viewModelScope.launch {
            val subAdmin = SubAdminUser(
                id = "subadmin_${System.currentTimeMillis()}",
                username = trimmedUser,
                password = trimmedPass,
                name = name.trim().ifBlank { trimmedUser },
                phone = phone.trim(),
                canManagePersonalTenants = canManagePersonalTenants,
                isActive = true,
                createdAt = System.currentTimeMillis(),
                lastActiveAt = 0L
            )
            sync.saveSubAdmin(subAdmin)
            sync.logActivity(ActivityLog(
                id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                timestamp = System.currentTimeMillis(),
                userName = currentUser.value?.displayName ?: "Master Admin",
                userRole = "ADMIN",
                actionType = "CREATE_SUB_ADMIN",
                title = "Created Sub-Admin: ${subAdmin.name}",
                details = "Username: @$trimmedUser, Phone: ${phone.ifBlank { "N/A" }}, Personal Allowed: $canManagePersonalTenants"
            ))
        }
        return Result.success(Unit)
    }

    fun updateSubAdmin(
        subAdminId: String,
        name: String,
        phone: String,
        canManagePersonalTenants: Boolean
    ): Result<Unit> {
        if (_currentUser.value?.isAdmin != true) {
            return Result.failure(Exception("Only Master Admin can edit Sub-Admins!"))
        }
        val sa = subAdmins.value.find { it.id == subAdminId }
            ?: return Result.failure(Exception("Sub-Admin not found."))

        viewModelScope.launch {
            val updated = sa.copy(
                name = name.trim().ifBlank { sa.name },
                phone = phone.trim(),
                canManagePersonalTenants = canManagePersonalTenants
            )
            sync.saveSubAdmin(updated)
            if (_currentUser.value?.subAdminId == subAdminId) {
                _currentUser.value = _currentUser.value?.copy(
                    displayName = updated.name,
                    canManagePersonalTenants = canManagePersonalTenants
                )
            }
            sync.logActivity(ActivityLog(
                id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                timestamp = System.currentTimeMillis(),
                userName = currentUser.value?.displayName ?: "Master Admin",
                userRole = "ADMIN",
                actionType = "EDIT_SUB_ADMIN",
                title = "Updated Sub-Admin: ${updated.name}",
                details = "Username: @${sa.username}, Personal Flats Allowed: $canManagePersonalTenants"
            ))
        }
        return Result.success(Unit)
    }

    fun changeSubAdminPassword(subAdminId: String, newPass: String): Result<Unit> {
        if (_currentUser.value?.isAdmin != true) {
            return Result.failure(Exception("Only Master Admin can change Sub-Admin password!"))
        }
        val trimmedPass = newPass.trim()
        if (trimmedPass.isBlank()) {
            return Result.failure(Exception("Password cannot be empty."))
        }
        if (trimmedPass.length < 4) {
            return Result.failure(Exception("Password must be at least 4 characters."))
        }

        val sa = subAdmins.value.find { it.id == subAdminId }
            ?: return Result.failure(Exception("Sub-Admin not found."))

        viewModelScope.launch {
            // Generating a new session ID ensures any currently logged in device will immediately mismatch and logout
            val invalidatedSessionId = "invalidated_${UUID.randomUUID()}"
            val updated = sa.copy(
                password = trimmedPass,
                activeSessionId = invalidatedSessionId
            )
            sync.saveSubAdmin(updated)
            sync.logActivity(ActivityLog(
                id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                timestamp = System.currentTimeMillis(),
                userName = currentUser.value?.displayName ?: "Master Admin",
                userRole = "ADMIN",
                actionType = "CHANGE_PASSWORD_SUB_ADMIN",
                title = "Changed Password for Sub-Admin: ${sa.name}",
                details = "Username: @${sa.username}. All active sessions invalidated."
            ))
        }
        return Result.success(Unit)
    }

    fun deleteSubAdmin(subAdminId: String): Result<Unit> {
        if (_currentUser.value?.isAdmin != true) {
            return Result.failure(Exception("Only Master Admin can delete Sub-Admins!"))
        }
        viewModelScope.launch {
            val sa = subAdmins.value.find { it.id == subAdminId }
            sync.removeSubAdmin(subAdminId)
            sync.logActivity(ActivityLog(
                id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                timestamp = System.currentTimeMillis(),
                userName = currentUser.value?.displayName ?: "Master Admin",
                userRole = "ADMIN",
                actionType = "DELETE_SUB_ADMIN",
                title = "Deleted Sub-Admin: ${sa?.name ?: subAdminId}",
                details = "Username: @${sa?.username ?: "N/A"}"
            ))
        }
        return Result.success(Unit)
    }

    fun forceRefresh() {
        viewModelScope.launch {
            sync.forceRefresh()
        }
    }

    fun sendBatchRentReminders(
        month: String,
        year: Int,
        senderName: String = "",
        senderContactPhone: String,
        simSlotSubscriptionId: Int? = null,
        fast2SmsApiKey: String? = null,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> },
        onComplete: (successCount: Int, failedCount: Int, errorMessage: String?) -> Unit = { _, _, _ -> }
    ) {
        viewModelScope.launch {
            val pendingRecords = rents.value.filter { rent ->
                rent.month.equals(month, ignoreCase = true) &&
                rent.year == year &&
                (rent.isPending || rent.isPartial) &&
                rent.pendingAmount > 0
            }

            if (pendingRecords.isEmpty()) {
                onComplete(0, 0, null)
                return@launch
            }

            var successCount = 0
            var failedCount = 0
            var lastErrorMessage: String? = null
            val total = pendingRecords.size

            val currentUserName = currentUser.value?.let {
                if (it.isAdmin) it.displayName.replace("(Master Admin)", "").trim().ifBlank { "Vimal Kumar" } else it.displayName.ifBlank { "Manager" }
            } ?: "Vimal Kumar"
            val effectiveSenderName = senderName.trim().ifBlank { currentUserName }
            val apiKey = fast2SmsApiKey?.trim()?.ifBlank { null }
                ?: SmsReminderHelper.getFast2SmsApiKey(getApplication()).ifBlank { null }

            withContext(Dispatchers.IO) {
                for ((index, rent) in pendingRecords.withIndex()) {
                    val tenant = tenants.value.find { it.id == rent.tenantId }
                    val phone = tenant?.phone ?: ""
                    if (phone.isNotBlank()) {
                        val message = SmsReminderHelper.formatReminderMessage(
                            tenantName = rent.tenantName,
                            shopNumber = rent.shopNumber,
                            month = rent.month,
                            year = rent.year,
                            pendingAmount = rent.pendingAmount,
                            senderName = effectiveSenderName,
                            senderContactPhone = senderContactPhone,
                            cycleMonths = tenant?.cycleMonths ?: 1,
                            isPersonal = rent.isPersonal,
                            electricityBill = rent.electricityBill,
                            flatBaseRent = (rent.amountDue - rent.electricityBill).coerceAtLeast(0.0)
                        )

                        val result = if (!apiKey.isNullOrBlank()) {
                            SmsReminderHelper.sendViaFast2SmsRetrofit(
                                apiKey = apiKey,
                                rawPhoneNumber = phone,
                                message = message
                            )
                        } else {
                            val sent = SmsReminderHelper.sendSms(
                                context = getApplication(),
                                rawPhoneNumber = phone,
                                message = message,
                                subscriptionId = simSlotSubscriptionId
                            )
                            Pair(sent, if (sent) "SMS sent" else "Device SIM SMS failed")
                        }

                        if (result.first) {
                            successCount++
                        } else {
                            failedCount++
                            if (lastErrorMessage == null) {
                                lastErrorMessage = result.second
                            }
                        }
                    } else {
                        failedCount++
                        if (lastErrorMessage == null) {
                            lastErrorMessage = "Missing phone number for ${rent.tenantName}"
                        }
                    }
                    withContext(Dispatchers.Main) {
                        onProgress(index + 1, total)
                    }
                    delay(250)
                }
            }

            // Only mark monthly reminder as sent if at least 1 SMS was actually delivered
            if (successCount > 0) {
                val isPersonalWorkspace = workspaceMode.value == AppWorkspaceMode.PRIVATE_PERSONAL
                val reminderId = "${year}_${month}${if (isPersonalWorkspace) "_personal" else ""}"
                val reminderRecord = MonthlyReminderRecord(
                    id = reminderId,
                    month = month,
                    year = year,
                    sentBy = effectiveSenderName,
                    senderPhone = senderContactPhone,
                    sentAt = System.currentTimeMillis(),
                    recipientsCount = successCount,
                    isSent = true
                )
                sync.saveMonthlyReminder(reminderRecord)

                // Add Activity Log
                sync.logActivity(
                    ActivityLog(
                        id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                        timestamp = System.currentTimeMillis(),
                        userName = effectiveSenderName,
                        userRole = currentUser.value?.role?.name ?: "ADMIN",
                        actionType = "SEND_BATCH_SMS_REMINDERS",
                        title = "${if (isPersonalWorkspace) "Flat Rent & Electricity" else "Rent"} SMS Sent for $month $year",
                        details = "Sent by $effectiveSenderName to $successCount tenants (Contact: ${senderContactPhone.ifBlank { "N/A" }})"
                    )
                )
            }

            withContext(Dispatchers.Main) {
                onComplete(successCount, failedCount, lastErrorMessage)
            }
        }
    }

    fun resetMonthlyReminderStatus(month: String, year: Int) {
        viewModelScope.launch {
            val isPersonalWorkspace = workspaceMode.value == AppWorkspaceMode.PRIVATE_PERSONAL
            val reminderId = "${year}_${month}${if (isPersonalWorkspace) "_personal" else ""}"
            sync.removeMonthlyReminder(reminderId)

            val matchedEntries = monthlyReminders.value.entries.filter {
                it.key.equals(reminderId, ignoreCase = true) ||
                (it.value.month.equals(month, ignoreCase = true) && it.value.year == year && it.key.contains("personal") == isPersonalWorkspace)
            }
            for (entry in matchedEntries) {
                sync.removeMonthlyReminder(entry.key)
            }

            val currentUserName = currentUser.value?.let {
                if (it.isAdmin) "Admin (${it.displayName})" else it.displayName
            } ?: "Admin"
            sync.logActivity(
                ActivityLog(
                    id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                    timestamp = System.currentTimeMillis(),
                    userName = currentUserName,
                    userRole = currentUser.value?.role?.name ?: "ADMIN",
                    actionType = "RESET_SMS_REMINDERS",
                    title = "Reminder status reset for $month $year",
                    details = "Reminder lock reset by $currentUserName to allow re-sending reminders"
                )
            )
        }
    }

    fun recordManualRemindersSent(
        month: String,
        year: Int,
        recipientsCount: Int,
        channel: String = "SMS/WhatsApp"
    ) {
        viewModelScope.launch {
            if (recipientsCount <= 0) return@launch
            val effectiveSenderName = currentUser.value?.let {
                if (it.isAdmin) "Admin (${it.displayName})" else it.displayName
            } ?: "Admin"
            val isPersonalWorkspace = workspaceMode.value == AppWorkspaceMode.PRIVATE_PERSONAL
            val reminderId = "${year}_${month}${if (isPersonalWorkspace) "_personal" else ""}"
            val reminderRecord = MonthlyReminderRecord(
                id = reminderId,
                month = month,
                year = year,
                sentBy = effectiveSenderName,
                senderPhone = currentUser.value?.phone ?: "",
                sentAt = System.currentTimeMillis(),
                recipientsCount = recipientsCount,
                isSent = true
            )
            sync.saveMonthlyReminder(reminderRecord)
            sync.logActivity(
                ActivityLog(
                    id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                    timestamp = System.currentTimeMillis(),
                    userName = effectiveSenderName,
                    userRole = currentUser.value?.role?.name ?: "ADMIN",
                    actionType = "MANUAL_REMINDERS_SENT",
                    title = "$channel Reminders Sent for $month $year",
                    details = "Dispatched to $recipientsCount tenants via $channel by $effectiveSenderName"
                )
            )
        }
    }

    fun updateElectricityMeterReading(
        rentId: String,
        prevReading: Double,
        currentReading: Double,
        ratePerUnit: Double
    ) {
        viewModelScope.launch {
            val existingRent = rawRents.value.find { it.id == rentId } ?: return@launch
            val validPrev = prevReading.coerceAtLeast(0.0)
            val validCurrent = currentReading.coerceAtLeast(validPrev)
            val validRate = ratePerUnit.coerceAtLeast(0.0)
            val units = (validCurrent - validPrev).coerceAtLeast(0.0)
            val elecBill = units * validRate

            val baseRent = (existingRent.amountDue - existingRent.electricityBill).coerceAtLeast(0.0)
            val newAmountDue = baseRent + elecBill
            val newStatus = when {
                newAmountDue <= 0.0 -> "PAID"
                existingRent.amountPaid >= newAmountDue -> "PAID"
                existingRent.amountPaid > 0.0 -> "PARTIAL"
                else -> "PENDING"
            }

            val updatedRent = existingRent.copy(
                prevMeterReading = validPrev,
                currentMeterReading = validCurrent,
                electricityRatePerUnit = validRate,
                electricityBill = elecBill,
                amountDue = newAmountDue,
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
            sync.saveRentRecord(updatedRent)

            // Also update Tenant's last recorded meter reading and rate
            val tenant = rawTenants.value.find { it.id == existingRent.tenantId }
            if (tenant != null) {
                val updatedTenant = tenant.copy(
                    lastMeterReading = validCurrent,
                    electricityRatePerUnit = validRate,
                    electricityBill = elecBill,
                    updatedAt = System.currentTimeMillis()
                )
                sync.saveTenant(updatedTenant)
            }

            // Log activity
            val user = currentUser.value?.displayName ?: "Admin"
            sync.logActivity(
                ActivityLog(
                    id = "act_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
                    timestamp = System.currentTimeMillis(),
                    userName = user,
                    userRole = currentUser.value?.role?.name ?: "ADMIN",
                    actionType = "UPDATE_METER_READING",
                    title = "Meter Reading: ${existingRent.tenantName} (${existingRent.shopNumber})",
                    details = "Reading: ${validPrev.toInt()} → ${validCurrent.toInt()} (${units.toInt()} units @ ₹${validRate.toInt()}/u) = ₹${elecBill.toInt()} electricity bill",
                    tenantId = existingRent.tenantId,
                    shopId = existingRent.shopId
                )
            )
        }
    }
}

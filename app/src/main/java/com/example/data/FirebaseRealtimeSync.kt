package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.model.ActivityLog
import com.example.model.AppUpdateInfo
import com.example.model.MarketNotice
import com.example.model.MonthlyReminderRecord
import com.example.model.PmcTaxClearanceRecord
import com.example.model.RentRecord
import com.example.model.Shop
import com.example.model.SubAdminUser
import com.example.model.Tenant
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

enum class SyncStatus {
    SYNCED,
    SYNCING,
    OFFLINE,
    ERROR
}

class FirebaseRealtimeSync(private val context: Context) {

    private val tag = "FirebaseRealtimeSync"
    private val baseUrl = "https://nasseblalmarkt-default-rtdb.firebaseio.com"

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // infinite for SSE stream
        .writeTimeout(25, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val writeClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val prefs = context.getSharedPreferences("naseeb_lal_market_cache", Context.MODE_PRIVATE)

    fun getLocalDeviceId(): String {
        var devId = prefs.getString("local_device_id", null)
        if (devId.isNullOrBlank()) {
            devId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString("local_device_id", devId).apply()
        }
        return devId
    }

    private val _syncStatus = MutableStateFlow(SyncStatus.SYNCING)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

    private val _lastSyncedAt = MutableStateFlow(System.currentTimeMillis())
    val lastSyncedAt: StateFlow<Long> = _lastSyncedAt.asStateFlow()

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops.asStateFlow()

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants.asStateFlow()

    private val _rents = MutableStateFlow<List<RentRecord>>(emptyList())
    val rents: StateFlow<List<RentRecord>> = _rents.asStateFlow()

    private val _subAdmins = MutableStateFlow<List<SubAdminUser>>(emptyList())
    val subAdmins: StateFlow<List<SubAdminUser>> = _subAdmins.asStateFlow()

    private val _activityLogs = MutableStateFlow<List<ActivityLog>>(emptyList())
    val activityLogs: StateFlow<List<ActivityLog>> = _activityLogs.asStateFlow()

    private val _monthlyReminders = MutableStateFlow<Map<String, MonthlyReminderRecord>>(emptyMap())
    val monthlyReminders: StateFlow<Map<String, MonthlyReminderRecord>> = _monthlyReminders.asStateFlow()

    private val _marketNotices = MutableStateFlow<List<MarketNotice>>(emptyList())
    val marketNotices: StateFlow<List<MarketNotice>> = _marketNotices.asStateFlow()

    private val _pmcTaxClearances = MutableStateFlow<Map<String, PmcTaxClearanceRecord>>(emptyMap())
    val pmcTaxClearances: StateFlow<Map<String, PmcTaxClearanceRecord>> = _pmcTaxClearances.asStateFlow()

    private val _appUpdateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val appUpdateInfo: StateFlow<AppUpdateInfo?> = _appUpdateInfo.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var sseJob: Job? = null
    private var periodicJob: Job? = null

    init {
        // 1. Immediately load whatever is in local device cache
        loadFromCache()
        if (_shops.value.isNotEmpty() || _rents.value.isNotEmpty()) {
            scope.launch {
                delay(600) // Brief shimmer for smooth, polished feel
                _isInitialLoading.value = false
            }
        }

        // 2. Register real-time connectivity callback for automatic reconnect
        registerNetworkCallback()

        // 3. Start live cloud synchronization
        startRealtimeSync()
    }

    private fun registerNetworkCallback() {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            cm?.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.i(tag, "Network connection established, triggering live sync...")
                    scope.launch {
                        fetchInitialSnapshot()
                        startSseListener()
                    }
                }

                override fun onLost(network: Network) {
                    Log.w(tag, "Network disconnected")
                    _syncStatus.value = SyncStatus.OFFLINE
                }
            })
        } catch (e: Exception) {
            Log.e(tag, "registerNetworkCallback failed: ${e.message}")
        }
    }

    private fun startRealtimeSync() {
        scope.launch {
            // First fetch complete snapshot from Firebase
            fetchInitialSnapshot()

            // Start SSE stream for instant real-time live events across devices
            startSseListener()

            // Periodic backup polling sync to guarantee multi-device freshness
            startPeriodicSync()
        }
    }

    suspend fun forceRefresh(): Unit = withContext(Dispatchers.IO) {
        _isInitialLoading.value = true
        _syncStatus.value = SyncStatus.SYNCING
        fetchInitialSnapshot()
        startSseListener()
        delay(400)
        _isInitialLoading.value = false
    }

    suspend fun fetchInitialSnapshot(): Unit = withContext(Dispatchers.IO) {
        _syncStatus.value = SyncStatus.SYNCING
        try {
            val request = Request.Builder()
                .url("$baseUrl/.json")
                .get()
                .build()

            writeClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (bodyString != null && bodyString != "null" && bodyString.isNotBlank()) {
                        parseFullDatabase(bodyString)
                        saveToCache(bodyString)
                    }
                    _syncStatus.value = SyncStatus.SYNCED
                    _lastSyncedAt.value = System.currentTimeMillis()
                } else {
                    _syncStatus.value = SyncStatus.ERROR
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to fetch initial snapshot: ${e.message}")
            _syncStatus.value = SyncStatus.OFFLINE
        } finally {
            _isInitialLoading.value = false
        }
    }

    private fun startSseListener() {
        sseJob?.cancel()
        sseJob = scope.launch {
            while (isActive) {
                try {
                    val request = Request.Builder()
                        .url("$baseUrl/.json")
                        .addHeader("Accept", "text/event-stream")
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            if (_syncStatus.value != SyncStatus.SYNCED) {
                                _syncStatus.value = SyncStatus.OFFLINE
                            }
                            delay(4000)
                            return@use
                        }

                        // SSE connection open and active
                        _syncStatus.value = SyncStatus.SYNCED
                        _lastSyncedAt.value = System.currentTimeMillis()

                        val source = response.body?.byteStream() ?: return@use
                        val reader = BufferedReader(InputStreamReader(source))
                        var currentEvent: String? = null

                        while (isActive) {
                            val line = reader.readLine() ?: break
                            if (line.startsWith("event: ")) {
                                currentEvent = line.substring(7).trim()
                            } else if (line.startsWith("data: ")) {
                                val dataString = line.substring(6).trim()
                                if (dataString != "null" && dataString.isNotBlank()) {
                                    handleSseData(currentEvent, dataString)
                                    _syncStatus.value = SyncStatus.SYNCED
                                    _lastSyncedAt.value = System.currentTimeMillis()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(tag, "SSE stream reconnecting: ${e.message}")
                    delay(3000)
                }
            }
        }
    }

    private fun handleSseData(event: String?, dataString: String) {
        try {
            val json = JSONObject(dataString)
            val path = json.optString("path", "/")
            val data = json.opt("data")

            when {
                path == "/" && data is JSONObject -> {
                    parseFullDatabase(data.toString())
                    saveToCache(data.toString())
                }
                path.startsWith("/shops/") && data is JSONObject -> {
                    val shopId = path.removePrefix("/shops/")
                    val shop = parseShop(shopId, data)
                    val current = _shops.value.toMutableList()
                    val idx = current.indexOfFirst { it.id == shop.id }
                    if (idx >= 0) current[idx] = shop else current.add(shop)
                    current.sortBy { it.shopNumber }
                    _shops.value = current
                    persistCurrentStateToCache()
                }
                path.startsWith("/shops/") && (data == null || data == JSONObject.NULL) -> {
                    val shopId = path.removePrefix("/shops/")
                    _shops.value = _shops.value.filterNot { it.id == shopId }
                    persistCurrentStateToCache()
                }
                path.startsWith("/tenants/") && data is JSONObject -> {
                    val tenantId = path.removePrefix("/tenants/")
                    val tenant = parseTenant(tenantId, data)
                    val current = _tenants.value.toMutableList()
                    val idx = current.indexOfFirst { it.id == tenant.id }
                    if (idx >= 0) current[idx] = tenant else current.add(tenant)
                    current.sortBy { it.name }
                    _tenants.value = current
                    persistCurrentStateToCache()
                }
                path.startsWith("/tenants/") && (data == null || data == JSONObject.NULL) -> {
                    val tenantId = path.removePrefix("/tenants/")
                    _tenants.value = _tenants.value.filterNot { it.id == tenantId }
                    val orphanRents = _rents.value.filter { it.tenantId == tenantId }
                    if (orphanRents.isNotEmpty()) {
                        _rents.value = _rents.value.filterNot { it.tenantId == tenantId }
                        scope.launch {
                            for (orphan in orphanRents) {
                                sendDeleteRequest("rents/${orphan.id}")
                            }
                        }
                    }
                    persistCurrentStateToCache()
                }
                path.startsWith("/rents/") && data is JSONObject -> {
                    val rentId = path.removePrefix("/rents/")
                    val rent = parseRent(rentId, data)
                    val current = _rents.value.toMutableList()
                    val idx = current.indexOfFirst { it.id == rent.id }
                    if (idx >= 0) current[idx] = rent else current.add(rent)
                    current.sortByDescending { it.year * 100 + monthToOrder(it.month) }
                    _rents.value = current
                    persistCurrentStateToCache()
                }
                path.startsWith("/rents/") && (data == null || data == JSONObject.NULL) -> {
                    val rentId = path.removePrefix("/rents/")
                    _rents.value = _rents.value.filterNot { it.id == rentId }
                    persistCurrentStateToCache()
                }
                path.startsWith("/subadmins/") && data is JSONObject -> {
                    val subAdminId = path.removePrefix("/subadmins/")
                    val subAdmin = parseSubAdmin(subAdminId, data)
                    val current = _subAdmins.value.toMutableList()
                    val idx = current.indexOfFirst { it.id == subAdmin.id }
                    if (idx >= 0) current[idx] = subAdmin else current.add(subAdmin)
                    _subAdmins.value = current
                    persistCurrentStateToCache()
                }
                path.startsWith("/subadmins/") && path.endsWith("/lastActiveAt") && (data is Number || data is String) -> {
                    val subAdminId = path.removePrefix("/subadmins/").removeSuffix("/lastActiveAt")
                    val time = if (data is Number) data.toLong() else data.toString().toLongOrNull() ?: 0L
                    val current = _subAdmins.value.toMutableList()
                    val idx = current.indexOfFirst { it.id == subAdminId }
                    if (idx >= 0) {
                        current[idx] = current[idx].copy(lastActiveAt = time)
                        _subAdmins.value = current
                    }
                }
                path.startsWith("/subadmins/") && (data == null || data == JSONObject.NULL) -> {
                    val subAdminId = path.removePrefix("/subadmins/")
                    _subAdmins.value = _subAdmins.value.filterNot { it.id == subAdminId }
                    persistCurrentStateToCache()
                }
                path.startsWith("/activity_logs/") && data is JSONObject -> {
                    val logId = path.removePrefix("/activity_logs/")
                    val actLog = parseActivityLog(logId, data)
                    val current = _activityLogs.value.toMutableList()
                    val isNew = current.none { it.id == actLog.id }
                    current.removeAll { it.id == actLog.id }
                    current.add(0, actLog)
                    _activityLogs.value = current.sortedByDescending { it.timestamp }.take(100)
                    persistCurrentStateToCache()

                    // Trigger instant push alert on all other connected phones
                    val myDevId = getLocalDeviceId()
                    val isFromAnotherDevice = actLog.authorDeviceId.isNotBlank() && actLog.authorDeviceId != myDevId
                    val isRecent = (System.currentTimeMillis() - actLog.timestamp) < 120_000 // within last 2 minutes

                    if (isNew && (isFromAnotherDevice || actLog.authorDeviceId.isBlank()) && isRecent) {
                        val alertTitle = "🔔 " + (if (actLog.userName.isNotBlank()) "${actLog.userName}: " else "") + actLog.title
                        val alertMsg = if (actLog.details.isNotBlank()) actLog.details else "Market data was updated in real-time."
                        NotificationHelper.showUpdateNotification(
                            context = context,
                            title = alertTitle,
                            message = alertMsg,
                            notificationId = (actLog.id.hashCode() and 0x7FFFFFFF)
                        )
                    }
                }
                path.startsWith("/monthly_reminders/") && data is JSONObject -> {
                    val remId = path.removePrefix("/monthly_reminders/")
                    val rem = parseMonthlyReminder(remId, data)
                    val current = _monthlyReminders.value.toMutableMap()
                    current[rem.id] = rem
                    _monthlyReminders.value = current
                    persistCurrentStateToCache()
                }
                path.startsWith("/monthly_reminders/") && (data == null || data == JSONObject.NULL) -> {
                    val remId = path.removePrefix("/monthly_reminders/")
                    val current = _monthlyReminders.value.toMutableMap()
                    current.remove(remId)
                    _monthlyReminders.value = current
                    persistCurrentStateToCache()
                }
                path.startsWith("/market_notices/") && data is JSONObject -> {
                    val noticeId = path.removePrefix("/market_notices/")
                    val notice = parseMarketNotice(noticeId, data)
                    val current = _marketNotices.value.toMutableList()
                    val isNew = current.none { it.id == notice.id }
                    current.removeAll { it.id == notice.id }
                    current.add(0, notice)
                    _marketNotices.value = current.sortedWith(
                        compareByDescending<MarketNotice> { it.isPinned }
                            .thenByDescending { it.createdAt }
                    )
                    persistCurrentStateToCache()

                    val myDevId = getLocalDeviceId()
                    val isFromAnotherDevice = notice.authorDeviceId.isNotBlank() && notice.authorDeviceId != myDevId
                    val isRecent = (System.currentTimeMillis() - notice.createdAt) < 180_000

                    if (isNew && (isFromAnotherDevice || notice.authorDeviceId.isBlank()) && isRecent) {
                        val alertTitle = "📢 " + notice.title
                        val alertMsg = if (notice.message.isNotBlank()) notice.message else "Naya market notice aaya hai."
                        NotificationHelper.showUpdateNotification(
                            context = context,
                            title = alertTitle,
                            message = alertMsg,
                            notificationId = (notice.id.hashCode() and 0x7FFFFFFF)
                        )
                    }
                }
                path.startsWith("/market_notices/") && (data == null || data == JSONObject.NULL) -> {
                    val noticeId = path.removePrefix("/market_notices/")
                    _marketNotices.value = _marketNotices.value.filterNot { it.id == noticeId }
                    persistCurrentStateToCache()
                }
                path.startsWith("/pmc_tax_clearances/") && data is JSONObject -> {
                    val fy = path.removePrefix("/pmc_tax_clearances/")
                    val record = parsePmcTaxClearance(fy, data)
                    val map = _pmcTaxClearances.value.toMutableMap()
                    map[record.financialYear] = record
                    _pmcTaxClearances.value = map
                    persistCurrentStateToCache()
                }
                path.startsWith("/pmc_tax_clearances/") && (data == null || data == JSONObject.NULL) -> {
                    val fy = path.removePrefix("/pmc_tax_clearances/")
                    val map = _pmcTaxClearances.value.toMutableMap()
                    map.remove(fy)
                    _pmcTaxClearances.value = map
                    persistCurrentStateToCache()
                }
                path.startsWith("/app_version_info") && data is JSONObject -> {
                    _appUpdateInfo.value = parseAppUpdateInfo(data)
                }
                else -> {
                    // Fallback to full snapshot refresh
                    scope.launch { fetchInitialSnapshot() }
                }
            }
        } catch (e: Exception) {
            try {
                parseFullDatabase(dataString)
                saveToCache(dataString)
            } catch (_: Exception) {
            }
        }
    }

    private fun startPeriodicSync() {
        periodicJob?.cancel()
        periodicJob = scope.launch {
            while (isActive) {
                // If currently OFFLINE or SYNCING, retry quickly every 4 seconds.
                // If already SYNCED, perform background heartbeat poll every 8 seconds.
                val interval = if (_syncStatus.value == SyncStatus.SYNCED) 8000L else 4000L
                delay(interval)
                try {
                    val request = Request.Builder()
                        .url("$baseUrl/.json")
                        .get()
                        .build()
                    writeClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val body = response.body?.string()
                            if (body != null && body != "null" && body.isNotBlank()) {
                                parseFullDatabase(body)
                                saveToCache(body)
                            }
                            _syncStatus.value = SyncStatus.SYNCED
                            _lastSyncedAt.value = System.currentTimeMillis()
                        } else if (_syncStatus.value != SyncStatus.SYNCED) {
                            _syncStatus.value = SyncStatus.OFFLINE
                        }
                    }
                } catch (e: Exception) {
                    if (_syncStatus.value != SyncStatus.OFFLINE) {
                        _syncStatus.value = SyncStatus.OFFLINE
                    }
                }
            }
        }
    }

    private fun parseFullDatabase(rawJson: String) {
        try {
            val root = JSONObject(rawJson)

            // 1. Shops
            val shopsObj = root.optJSONObject("shops")
            val loadedShops = mutableListOf<Shop>()
            if (shopsObj != null) {
                val keys = shopsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val s = shopsObj.optJSONObject(key)
                    if (s != null) {
                        loadedShops.add(parseShop(key, s))
                    }
                }
            }
            loadedShops.sortBy { it.shopNumber }
            _shops.value = loadedShops

            // 2. Tenants
            val tenantsObj = root.optJSONObject("tenants")
            val loadedTenants = mutableListOf<Tenant>()
            if (tenantsObj != null) {
                val keys = tenantsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val t = tenantsObj.optJSONObject(key)
                    if (t != null) {
                        loadedTenants.add(parseTenant(key, t))
                    }
                }
            }
            loadedTenants.sortBy { it.name }
            _tenants.value = loadedTenants

            // 3. Rents
            val rentsObj = root.optJSONObject("rents")
            val loadedRents = mutableListOf<RentRecord>()
            if (rentsObj != null) {
                val keys = rentsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val r = rentsObj.optJSONObject(key)
                    if (r != null) {
                        loadedRents.add(parseRent(key, r))
                    }
                }
            }
            // Auto-clean orphan rents whose tenant no longer exists in database
            val validTenantIds = loadedTenants.map { it.id }.toSet()
            val (validRents, orphanRents) = loadedRents.partition { it.tenantId in validTenantIds }
            _rents.value = validRents.sortedByDescending { it.year * 100 + monthToOrder(it.month) }

            if (orphanRents.isNotEmpty()) {
                scope.launch {
                    for (orphan in orphanRents) {
                        sendDeleteRequest("rents/${orphan.id}")
                    }
                    persistCurrentStateToCache()
                }
            }

            // 4. Sub-admins
            val subAdminObj = root.optJSONObject("subadmins")
            val loadedSubAdmins = mutableListOf<SubAdminUser>()
            if (subAdminObj != null) {
                val keys = subAdminObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val sa = subAdminObj.optJSONObject(key)
                    if (sa != null) {
                        loadedSubAdmins.add(parseSubAdmin(key, sa))
                    }
                }
            }
            _subAdmins.value = loadedSubAdmins

            // 5. Activity Logs
            val actObj = root.optJSONObject("activity_logs")
            val loadedLogs = mutableListOf<ActivityLog>()
            if (actObj != null) {
                val keys = actObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val al = actObj.optJSONObject(key)
                    if (al != null) {
                        loadedLogs.add(parseActivityLog(key, al))
                    }
                }
            }
            _activityLogs.value = loadedLogs.sortedByDescending { it.timestamp }.take(100)

            // 6. Monthly Reminders
            val remObj = root.optJSONObject("monthly_reminders")
            val loadedReminders = mutableMapOf<String, MonthlyReminderRecord>()
            if (remObj != null) {
                val keys = remObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val r = remObj.optJSONObject(key)
                    if (r != null) {
                        loadedReminders[key] = parseMonthlyReminder(key, r)
                    }
                }
            }
            _monthlyReminders.value = loadedReminders

            // 7. Market Notices / Announcements
            val noticesObj = root.optJSONObject("market_notices")
            val loadedNotices = mutableListOf<MarketNotice>()
            if (noticesObj != null) {
                val keys = noticesObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val no = noticesObj.optJSONObject(key)
                    if (no != null) {
                        loadedNotices.add(parseMarketNotice(key, no))
                    }
                }
            }
            _marketNotices.value = loadedNotices.sortedWith(
                compareByDescending<MarketNotice> { it.isPinned }
                    .thenByDescending { it.createdAt }
            )

            // 8. Annual PMC Tax Clearances
            val clearancesObj = root.optJSONObject("pmc_tax_clearances")
            val loadedClearances = mutableMapOf<String, PmcTaxClearanceRecord>()
            if (clearancesObj != null) {
                val keys = clearancesObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val cObj = clearancesObj.optJSONObject(key)
                    if (cObj != null) {
                        loadedClearances[key] = parsePmcTaxClearance(key, cObj)
                    }
                }
            }
            _pmcTaxClearances.value = loadedClearances

            // 9. App Version Update Info
            val versionObj = root.optJSONObject("app_version_info")
            if (versionObj != null) {
                _appUpdateInfo.value = parseAppUpdateInfo(versionObj)
            }

        } catch (e: Exception) {
            Log.e(tag, "parseFullDatabase error: ${e.message}")
        }
    }

    private fun parseShop(key: String, obj: JSONObject): Shop {
        return Shop(
            id = obj.optString("id", key),
            shopNumber = obj.optString("shopNumber", "Shop $key"),
            floor = obj.optString("floor", "Ground Floor"),
            sizeSqFt = obj.optString("sizeSqFt", "200 sq.ft"),
            baseRent = obj.optDouble("baseRent", 0.0),
            maintenanceCharge = obj.optDouble("maintenanceCharge", 0.0),
            tenantId = if (obj.has("tenantId") && !obj.isNull("tenantId") && obj.optString("tenantId").isNotBlank()) obj.optString("tenantId") else null,
            tenantName = if (obj.has("tenantName") && !obj.isNull("tenantName") && obj.optString("tenantName").isNotBlank()) obj.optString("tenantName") else null,
            status = obj.optString("status", "VACANT"),
            electricityMeter = obj.optString("electricityMeter", ""),
            notes = obj.optString("notes", ""),
            incrementYears = obj.optInt("incrementYears", 1).coerceAtLeast(1),
            incrementPercent = obj.optDouble("incrementPercent", 5.0).coerceAtLeast(0.0),
            isPersonal = obj.optBoolean("isPersonal", false),
            propertyType = obj.optString("propertyType", "SHOP"),
            ownerSubAdminId = obj.optString("ownerSubAdminId", ""),
            lastModifiedBy = obj.optString("lastModifiedBy", ""),
            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    private fun parseTenant(key: String, obj: JSONObject): Tenant {
        val assignedShopsArray = obj.optJSONArray("assignedShopIds")
        val assignedIds = mutableListOf<String>()
        if (assignedShopsArray != null) {
            for (i in 0 until assignedShopsArray.length()) {
                val sId = assignedShopsArray.optString(i)
                if (sId.isNotBlank()) assignedIds.add(sId)
            }
        }
        val shopIdVal = obj.optString("shopId", "")
        val numShops = obj.optInt("numberOfShops", if (assignedIds.isNotEmpty()) assignedIds.size else if (shopIdVal.isNotBlank()) 1 else 0)

        return Tenant(
            id = obj.optString("id", key),
            name = obj.optString("name", "Unknown"),
            businessName = obj.optString("businessName", ""),
            phone = obj.optString("phone", ""),
            shopId = shopIdVal,
            shopNumber = obj.optString("shopNumber", ""),
            assignedShopIds = assignedIds,
            numberOfShops = numShops,
            advanceDeposit = obj.optDouble("advanceDeposit", 0.0),
            monthlyRent = obj.optDouble("monthlyRent", 0.0),
            previousDues = obj.optDouble("previousDues", 0.0),
            incrementYears = obj.optInt("incrementYears", 1).coerceAtLeast(1),
            incrementPercent = obj.optDouble("incrementPercent", 5.0).coerceAtLeast(0.0),
            billingCycle = obj.optString("billingCycle", "MONTHLY").ifBlank { "MONTHLY" },
            joiningDate = obj.optString("joiningDate", "01 Jan 2026"),
            idProof = obj.optString("idProof", ""),
            isActive = obj.optBoolean("isActive", true),
            notes = obj.optString("notes", ""),
            electricityBill = obj.optDouble("electricityBill", 0.0),
            lastMeterReading = obj.optDouble("lastMeterReading", 0.0),
            electricityRatePerUnit = obj.optDouble("electricityRatePerUnit", 10.0),
            isPersonal = obj.optBoolean("isPersonal", false),
            ownerSubAdminId = obj.optString("ownerSubAdminId", ""),
            lastModifiedBy = obj.optString("lastModifiedBy", ""),
            exitDate = obj.optString("exitDate", ""),
            exitReason = obj.optString("exitReason", ""),
            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    private fun parseRent(key: String, obj: JSONObject): RentRecord {
        return RentRecord(
            id = obj.optString("id", key),
            shopId = obj.optString("shopId", ""),
            shopNumber = obj.optString("shopNumber", ""),
            tenantId = obj.optString("tenantId", ""),
            tenantName = obj.optString("tenantName", ""),
            month = obj.optString("month", "September"),
            year = obj.optInt("year", 2026),
            amountDue = obj.optDouble("amountDue", 0.0),
            amountPaid = obj.optDouble("amountPaid", 0.0),
            pmcTax = obj.optDouble("pmcTax", 0.0),
            electricityBill = obj.optDouble("electricityBill", 0.0),
            prevMeterReading = obj.optDouble("prevMeterReading", 0.0),
            currentMeterReading = obj.optDouble("currentMeterReading", 0.0),
            electricityRatePerUnit = obj.optDouble("electricityRatePerUnit", 0.0),
            status = obj.optString("status", "PENDING"),
            dueDate = obj.optString("dueDate", "10th"),
            paidDate = obj.optString("paidDate", ""),
            paymentMode = obj.optString("paymentMode", "CASH"),
            collectedBy = obj.optString("collectedBy", ""),
            receiptNumber = obj.optString("receiptNumber", ""),
            notes = obj.optString("notes", ""),
            isPersonal = obj.optBoolean("isPersonal", false),
            ownerSubAdminId = obj.optString("ownerSubAdminId", ""),
            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    private fun parseSubAdmin(key: String, obj: JSONObject): SubAdminUser {
        return SubAdminUser(
            id = obj.optString("id", key),
            username = obj.optString("username", ""),
            password = obj.optString("password", ""),
            name = obj.optString("name", "Sub Admin"),
            phone = obj.optString("phone", ""),
            isActive = obj.optBoolean("isActive", true),
            canManagePersonalTenants = obj.optBoolean("canManagePersonalTenants", false),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            lastActiveAt = obj.optLong("lastActiveAt", 0L),
            activeSessionId = obj.optString("activeSessionId", "")
        )
    }

    private fun parseActivityLog(key: String, obj: JSONObject): ActivityLog {
        return ActivityLog(
            id = obj.optString("id", key),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
            userName = obj.optString("userName", "Unknown"),
            userRole = obj.optString("userRole", "SUB_ADMIN"),
            actionType = obj.optString("actionType", ""),
            title = obj.optString("title", ""),
            details = obj.optString("details", ""),
            authorDeviceId = obj.optString("authorDeviceId", ""),
            tenantId = obj.optString("tenantId", ""),
            shopId = obj.optString("shopId", "")
        )
    }

    private fun parseMonthlyReminder(key: String, obj: JSONObject): MonthlyReminderRecord {
        return MonthlyReminderRecord(
            id = obj.optString("id", key),
            month = obj.optString("month", ""),
            year = obj.optInt("year", 2026),
            sentBy = obj.optString("sentBy", ""),
            senderPhone = obj.optString("senderPhone", ""),
            sentAt = obj.optLong("sentAt", 0L),
            recipientsCount = obj.optInt("recipientsCount", 0),
            isSent = obj.optBoolean("isSent", true)
        )
    }

    private fun parseMarketNotice(key: String, obj: JSONObject): MarketNotice {
        return MarketNotice(
            id = obj.optString("id", key),
            title = obj.optString("title", ""),
            message = obj.optString("message", ""),
            category = obj.optString("category", "TAX"),
            priority = obj.optString("priority", "NORMAL"),
            authorName = obj.optString("authorName", "Admin"),
            authorRole = obj.optString("authorRole", "ADMIN"),
            authorDeviceId = obj.optString("authorDeviceId", ""),
            dueDate = obj.optString("dueDate", ""),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            isPinned = obj.optBoolean("isPinned", false),
            targetAudience = obj.optString("targetAudience", "ALL")
        )
    }

    fun saveNotice(notice: MarketNotice, onComplete: ((Boolean) -> Unit)? = null) {
        scope.launch {
            val json = JSONObject().apply {
                put("id", notice.id)
                put("title", notice.title)
                put("message", notice.message)
                put("category", notice.category)
                put("priority", notice.priority)
                put("authorName", notice.authorName)
                put("authorRole", notice.authorRole)
                put("authorDeviceId", notice.authorDeviceId)
                put("dueDate", notice.dueDate)
                put("createdAt", notice.createdAt)
                put("isPinned", notice.isPinned)
                put("targetAudience", notice.targetAudience)
            }
            val success = sendPutRequest("market_notices/${notice.id}", json.toString())
            if (success) {
                val list = _marketNotices.value.filterNot { it.id == notice.id }.toMutableList()
                list.add(0, notice)
                _marketNotices.value = list.sortedWith(
                    compareByDescending<MarketNotice> { it.isPinned }
                        .thenByDescending { it.createdAt }
                )
                persistCurrentStateToCache()
            }
            withContext(Dispatchers.Main) {
                onComplete?.invoke(success)
            }
        }
    }

    fun deleteNotice(noticeId: String, onComplete: ((Boolean) -> Unit)? = null) {
        scope.launch {
            val success = sendDeleteRequest("market_notices/$noticeId")
            if (success) {
                _marketNotices.value = _marketNotices.value.filterNot { it.id == noticeId }
                persistCurrentStateToCache()
            }
            withContext(Dispatchers.Main) {
                onComplete?.invoke(success)
            }
        }
    }

    private fun parsePmcTaxClearance(key: String, obj: JSONObject): PmcTaxClearanceRecord {
        return PmcTaxClearanceRecord(
            financialYear = obj.optString("financialYear", key),
            isPaid = obj.optBoolean("isPaid", false),
            amountPaid = obj.optDouble("amountPaid", 0.0),
            receiptNumber = obj.optString("receiptNumber", ""),
            paidDate = obj.optString("paidDate", ""),
            paidBy = obj.optString("paidBy", ""),
            paidByRole = obj.optString("paidByRole", "ADMIN"),
            paymentMode = obj.optString("paymentMode", "CASH"),
            notes = obj.optString("notes", ""),
            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    fun savePmcTaxClearance(record: PmcTaxClearanceRecord, onComplete: ((Boolean) -> Unit)? = null) {
        scope.launch {
            val json = JSONObject().apply {
                put("financialYear", record.financialYear)
                put("isPaid", record.isPaid)
                put("amountPaid", record.amountPaid)
                put("receiptNumber", record.receiptNumber)
                put("paidDate", record.paidDate)
                put("paidBy", record.paidBy)
                put("paidByRole", record.paidByRole)
                put("paymentMode", record.paymentMode)
                put("notes", record.notes)
                put("updatedAt", record.updatedAt)
            }
            val success = sendPutRequest("pmc_tax_clearances/${record.financialYear}", json.toString())
            if (success) {
                val map = _pmcTaxClearances.value.toMutableMap()
                map[record.financialYear] = record
                _pmcTaxClearances.value = map
                persistCurrentStateToCache()
            }
            withContext(Dispatchers.Main) {
                onComplete?.invoke(success)
            }
        }
    }

    private fun parseAppUpdateInfo(obj: JSONObject): AppUpdateInfo {
        return AppUpdateInfo(
            latestVersionCode = obj.optInt("latestVersionCode", 2),
            latestVersionName = obj.optString("latestVersionName", "2.0"),
            updateTitle = obj.optString("updateTitle", "New Update Available!"),
            updateMessage = obj.optString("updateMessage", ""),
            downloadUrl = obj.optString("downloadUrl", ""),
            isMandatory = obj.optBoolean("isMandatory", false),
            releasedDate = obj.optString("releasedDate", "")
        )
    }

    fun publishAppUpdateInfo(info: AppUpdateInfo, onComplete: ((Boolean) -> Unit)? = null) {
        scope.launch {
            val json = JSONObject().apply {
                put("latestVersionCode", info.latestVersionCode)
                put("latestVersionName", info.latestVersionName)
                put("updateTitle", info.updateTitle)
                put("updateMessage", info.updateMessage)
                put("downloadUrl", info.downloadUrl)
                put("isMandatory", info.isMandatory)
                put("releasedDate", info.releasedDate)
            }
            val success = sendPutRequest("app_version_info", json.toString())
            if (success) {
                _appUpdateInfo.value = info
            }
            withContext(Dispatchers.Main) {
                onComplete?.invoke(success)
            }
        }
    }

    fun fetchLatestAppUpdateInfo(onResult: ((AppUpdateInfo?) -> Unit)? = null) {
        scope.launch {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/app_version_info.json")
                    .get()
                    .build()
                writeClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null && body != "null" && body.isNotBlank()) {
                            val parsed = parseAppUpdateInfo(JSONObject(body))
                            _appUpdateInfo.value = parsed
                            withContext(Dispatchers.Main) {
                                onResult?.invoke(parsed)
                            }
                            return@use
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "fetchLatestAppUpdateInfo error: ${e.message}")
            }
            withContext(Dispatchers.Main) {
                onResult?.invoke(_appUpdateInfo.value)
            }
        }
    }

    private fun monthToOrder(month: String): Int {
        return when (month.lowercase()) {
            "january", "jan" -> 1
            "february", "feb" -> 2
            "march", "mar" -> 3
            "april", "apr" -> 4
            "may" -> 5
            "june", "jun" -> 6
            "july", "jul" -> 7
            "august", "aug" -> 8
            "september", "sep" -> 9
            "october", "oct" -> 10
            "november", "nov" -> 11
            "december", "dec" -> 12
            else -> 0
        }
    }

    // --- CRUD Operations for Cloud Realtime Sync ---

    suspend fun saveShop(shop: Shop): Boolean = withContext(Dispatchers.IO) {
        val current = _shops.value.toMutableList()
        val index = current.indexOfFirst { it.id == shop.id }
        if (index >= 0) {
            current[index] = shop
        } else {
            current.add(shop)
        }
        current.sortBy { it.shopNumber }
        _shops.value = current
        persistCurrentStateToCache()

        val json = JSONObject().apply {
            put("id", shop.id)
            put("shopNumber", shop.shopNumber)
            put("floor", shop.floor)
            put("sizeSqFt", shop.sizeSqFt)
            put("baseRent", shop.baseRent)
            put("maintenanceCharge", shop.maintenanceCharge)
            put("tenantId", shop.tenantId ?: JSONObject.NULL)
            put("tenantName", shop.tenantName ?: JSONObject.NULL)
            put("status", shop.status)
            put("electricityMeter", shop.electricityMeter)
            put("notes", shop.notes)
            put("incrementYears", shop.incrementYears)
            put("incrementPercent", shop.incrementPercent)
            put("isPersonal", shop.isPersonal)
            put("propertyType", shop.propertyType)
            put("ownerSubAdminId", shop.ownerSubAdminId)
            put("lastModifiedBy", shop.lastModifiedBy)
            put("updatedAt", shop.updatedAt)
        }

        sendPutRequest("shops/${shop.id}", json.toString())
    }

    suspend fun removeShop(shopId: String): Boolean = withContext(Dispatchers.IO) {
        _shops.value = _shops.value.filterNot { it.id == shopId }
        persistCurrentStateToCache()
        sendDeleteRequest("shops/$shopId")
    }

    suspend fun saveTenant(tenant: Tenant): Boolean = withContext(Dispatchers.IO) {
        val current = _tenants.value.toMutableList()
        val index = current.indexOfFirst { it.id == tenant.id }
        if (index >= 0) {
            current[index] = tenant
        } else {
            current.add(tenant)
        }
        _tenants.value = current.sortedBy { it.name }
        persistCurrentStateToCache()

        val json = JSONObject().apply {
            put("id", tenant.id)
            put("name", tenant.name)
            put("businessName", tenant.businessName)
            put("phone", tenant.phone)
            put("shopId", tenant.shopId)
            put("shopNumber", tenant.shopNumber)
            put("assignedShopIds", JSONArray(tenant.shopIdList))
            put("numberOfShops", tenant.shopCount)
            put("advanceDeposit", tenant.advanceDeposit)
            put("monthlyRent", tenant.monthlyRent)
            put("previousDues", tenant.previousDues)
            put("incrementYears", tenant.incrementYears)
            put("incrementPercent", tenant.incrementPercent)
            put("billingCycle", tenant.billingCycle)
            put("joiningDate", tenant.joiningDate)
            put("idProof", tenant.idProof)
            put("isActive", tenant.isActive)
            put("notes", tenant.notes)
            put("electricityBill", tenant.electricityBill)
            put("lastMeterReading", tenant.lastMeterReading)
            put("electricityRatePerUnit", tenant.electricityRatePerUnit)
            put("isPersonal", tenant.isPersonal)
            put("ownerSubAdminId", tenant.ownerSubAdminId)
            put("exitDate", tenant.exitDate)
            put("exitReason", tenant.exitReason)
            put("lastModifiedBy", tenant.lastModifiedBy)
            put("updatedAt", tenant.updatedAt)
        }

        sendPutRequest("tenants/${tenant.id}", json.toString())
    }

    suspend fun removeTenant(tenantId: String): Boolean = withContext(Dispatchers.IO) {
        _tenants.value = _tenants.value.filterNot { it.id == tenantId }
        persistCurrentStateToCache()
        sendDeleteRequest("tenants/$tenantId")
    }

    suspend fun saveRentRecord(rent: RentRecord): Boolean = withContext(Dispatchers.IO) {
        val current = _rents.value.toMutableList()
        val index = current.indexOfFirst { it.id == rent.id }
        if (index >= 0) {
            current[index] = rent
        } else {
            current.add(rent)
        }
        _rents.value = current.sortedByDescending { it.year * 100 + monthToOrder(it.month) }
        persistCurrentStateToCache()

        val json = JSONObject().apply {
            put("id", rent.id)
            put("shopId", rent.shopId)
            put("shopNumber", rent.shopNumber)
            put("tenantId", rent.tenantId)
            put("tenantName", rent.tenantName)
            put("month", rent.month)
            put("year", rent.year)
            put("amountDue", rent.amountDue)
            put("amountPaid", rent.amountPaid)
            put("pmcTax", rent.pmcTax)
            put("electricityBill", rent.electricityBill)
            put("prevMeterReading", rent.prevMeterReading)
            put("currentMeterReading", rent.currentMeterReading)
            put("electricityRatePerUnit", rent.electricityRatePerUnit)
            put("status", rent.status)
            put("dueDate", rent.dueDate)
            put("paidDate", rent.paidDate)
            put("paymentMode", rent.paymentMode)
            put("collectedBy", rent.collectedBy)
            put("receiptNumber", rent.receiptNumber)
            put("notes", rent.notes)
            put("isPersonal", rent.isPersonal)
            put("ownerSubAdminId", rent.ownerSubAdminId)
            put("updatedAt", rent.updatedAt)
        }

        sendPutRequest("rents/${rent.id}", json.toString())
    }

    suspend fun removeRentRecord(rentId: String): Boolean = withContext(Dispatchers.IO) {
        _rents.value = _rents.value.filterNot { it.id == rentId }
        persistCurrentStateToCache()
        sendDeleteRequest("rents/$rentId")
    }

    suspend fun removeRentRecordsForTenant(tenantId: String): Boolean = withContext(Dispatchers.IO) {
        val toRemove = _rents.value.filter { it.tenantId == tenantId }
        if (toRemove.isEmpty()) return@withContext true
        _rents.value = _rents.value.filterNot { it.tenantId == tenantId }
        persistCurrentStateToCache()
        var allSuccess = true
        for (r in toRemove) {
            val ok = sendDeleteRequest("rents/${r.id}")
            if (!ok) allSuccess = false
        }
        allSuccess
    }

    suspend fun saveSubAdmin(subAdmin: SubAdminUser): Boolean = withContext(Dispatchers.IO) {
        val current = _subAdmins.value.toMutableList()
        val index = current.indexOfFirst { it.id == subAdmin.id }
        if (index >= 0) {
            current[index] = subAdmin
        } else {
            current.add(subAdmin)
        }
        _subAdmins.value = current
        persistCurrentStateToCache()

        val json = JSONObject().apply {
            put("id", subAdmin.id)
            put("username", subAdmin.username)
            put("password", subAdmin.password)
            put("name", subAdmin.name)
            put("phone", subAdmin.phone)
            put("isActive", subAdmin.isActive)
            put("canManagePersonalTenants", subAdmin.canManagePersonalTenants)
            put("createdAt", subAdmin.createdAt)
            put("lastActiveAt", subAdmin.lastActiveAt)
            put("activeSessionId", subAdmin.activeSessionId)
        }

        sendPutRequest("subadmins/${subAdmin.id}", json.toString())
    }

    suspend fun updateSubAdminPresence(subAdminId: String): Boolean = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val current = _subAdmins.value.toMutableList()
        val idx = current.indexOfFirst { it.id == subAdminId }
        if (idx >= 0) {
            current[idx] = current[idx].copy(lastActiveAt = now)
            _subAdmins.value = current
        }
        sendPutRequest("subadmins/$subAdminId/lastActiveAt", now.toString())
    }

    suspend fun logActivity(log: ActivityLog): Boolean = withContext(Dispatchers.IO) {
        val current = _activityLogs.value.toMutableList()
        current.removeAll { it.id == log.id }
        current.add(0, log)
        _activityLogs.value = current.sortedByDescending { it.timestamp }.take(100)
        persistCurrentStateToCache()

        val authorId = log.authorDeviceId.ifBlank { getLocalDeviceId() }
        val json = JSONObject().apply {
            put("id", log.id)
            put("timestamp", log.timestamp)
            put("userName", log.userName)
            put("userRole", log.userRole)
            put("actionType", log.actionType)
            put("title", log.title)
            put("details", log.details)
            put("authorDeviceId", authorId)
            if (log.tenantId.isNotBlank()) put("tenantId", log.tenantId)
            if (log.shopId.isNotBlank()) put("shopId", log.shopId)
        }
        sendPutRequest("activity_logs/${log.id}", json.toString())
    }

    suspend fun removeSubAdmin(subAdminId: String): Boolean = withContext(Dispatchers.IO) {
        _subAdmins.value = _subAdmins.value.filterNot { it.id == subAdminId }
        persistCurrentStateToCache()
        sendDeleteRequest("subadmins/$subAdminId")
    }

    suspend fun saveMonthlyReminder(record: MonthlyReminderRecord): Boolean = withContext(Dispatchers.IO) {
        val current = _monthlyReminders.value.toMutableMap()
        current[record.id] = record
        _monthlyReminders.value = current
        persistCurrentStateToCache()

        val json = JSONObject().apply {
            put("id", record.id)
            put("month", record.month)
            put("year", record.year)
            put("sentBy", record.sentBy)
            put("senderPhone", record.senderPhone)
            put("sentAt", record.sentAt)
            put("recipientsCount", record.recipientsCount)
            put("isSent", record.isSent)
        }
        sendPutRequest("monthly_reminders/${record.id}", json.toString())
    }

    suspend fun removeMonthlyReminder(reminderId: String): Boolean = withContext(Dispatchers.IO) {
        val current = _monthlyReminders.value.toMutableMap()
        current.remove(reminderId)
        _monthlyReminders.value = current
        persistCurrentStateToCache()
        sendDeleteRequest("monthly_reminders/$reminderId")
    }

    private fun sendPutRequest(path: String, jsonBody: String): Boolean {
        return try {
            val body = jsonBody.toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("$baseUrl/$path.json")
                .put(body)
                .build()

            writeClient.newCall(request).execute().use { response ->
                val success = response.isSuccessful
                if (success) {
                    _lastSyncedAt.value = System.currentTimeMillis()
                    _syncStatus.value = SyncStatus.SYNCED
                }
                success
            }
        } catch (e: Exception) {
            Log.e(tag, "sendPutRequest failed for $path: ${e.message}")
            _syncStatus.value = SyncStatus.OFFLINE
            false
        }
    }

    private fun sendDeleteRequest(path: String): Boolean {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/$path.json")
                .delete()
                .build()

            writeClient.newCall(request).execute().use { response ->
                val success = response.isSuccessful
                if (success) {
                    _lastSyncedAt.value = System.currentTimeMillis()
                    _syncStatus.value = SyncStatus.SYNCED
                }
                success
            }
        } catch (e: Exception) {
            Log.e(tag, "sendDeleteRequest failed for $path: ${e.message}")
            _syncStatus.value = SyncStatus.OFFLINE
            false
        }
    }

    // --- Offline Cache Management ---

    private fun saveToCache(rawJson: String) {
        prefs.edit().putString("cached_db", rawJson).apply()
    }

    private fun loadFromCache() {
        val cached = prefs.getString("cached_db", null)
        if (!cached.isNullOrBlank()) {
            parseFullDatabase(cached)
        }
    }

    private fun persistCurrentStateToCache() {
        try {
            val root = JSONObject()

            val shopsObj = JSONObject()
            for (shop in _shops.value) {
                shopsObj.put(shop.id, JSONObject().apply {
                    put("id", shop.id)
                    put("shopNumber", shop.shopNumber)
                    put("floor", shop.floor)
                    put("sizeSqFt", shop.sizeSqFt)
                    put("baseRent", shop.baseRent)
                    put("maintenanceCharge", shop.maintenanceCharge)
                    put("tenantId", shop.tenantId ?: JSONObject.NULL)
                    put("tenantName", shop.tenantName ?: JSONObject.NULL)
                    put("status", shop.status)
                    put("electricityMeter", shop.electricityMeter)
                    put("notes", shop.notes)
                    put("lastModifiedBy", shop.lastModifiedBy)
                    put("updatedAt", shop.updatedAt)
                })
            }
            root.put("shops", shopsObj)

            val tenantsObj = JSONObject()
            for (tenant in _tenants.value) {
                tenantsObj.put(tenant.id, JSONObject().apply {
                    put("id", tenant.id)
                    put("name", tenant.name)
                    put("businessName", tenant.businessName)
                    put("phone", tenant.phone)
                    put("shopId", tenant.shopId)
                    put("shopNumber", tenant.shopNumber)
                    put("assignedShopIds", JSONArray(tenant.shopIdList))
                    put("numberOfShops", tenant.shopCount)
                    put("advanceDeposit", tenant.advanceDeposit)
                    put("monthlyRent", tenant.monthlyRent)
                    put("previousDues", tenant.previousDues)
                    put("incrementYears", tenant.incrementYears)
                    put("incrementPercent", tenant.incrementPercent)
                    put("billingCycle", tenant.billingCycle)
                    put("joiningDate", tenant.joiningDate)
                    put("idProof", tenant.idProof)
                    put("isActive", tenant.isActive)
                    put("notes", tenant.notes)
                    put("exitDate", tenant.exitDate)
                    put("exitReason", tenant.exitReason)
                    put("lastModifiedBy", tenant.lastModifiedBy)
                    put("updatedAt", tenant.updatedAt)
                })
            }
            root.put("tenants", tenantsObj)

            val rentsObj = JSONObject()
            for (rent in _rents.value) {
                rentsObj.put(rent.id, JSONObject().apply {
                    put("id", rent.id)
                    put("shopId", rent.shopId)
                    put("shopNumber", rent.shopNumber)
                    put("tenantId", rent.tenantId)
                    put("tenantName", rent.tenantName)
                    put("month", rent.month)
                    put("year", rent.year)
                    put("amountDue", rent.amountDue)
                    put("amountPaid", rent.amountPaid)
                    put("pmcTax", rent.pmcTax)
                    put("status", rent.status)
                    put("dueDate", rent.dueDate)
                    put("paidDate", rent.paidDate)
                    put("paymentMode", rent.paymentMode)
                    put("collectedBy", rent.collectedBy)
                    put("receiptNumber", rent.receiptNumber)
                    put("notes", rent.notes)
                    put("updatedAt", rent.updatedAt)
                })
            }
            root.put("rents", rentsObj)

            val subAdminsObj = JSONObject()
            for (subAdmin in _subAdmins.value) {
                subAdminsObj.put(subAdmin.id, JSONObject().apply {
                    put("id", subAdmin.id)
                    put("username", subAdmin.username)
                    put("password", subAdmin.password)
                    put("name", subAdmin.name)
                    put("phone", subAdmin.phone)
                    put("isActive", subAdmin.isActive)
                    put("createdAt", subAdmin.createdAt)
                    put("lastActiveAt", subAdmin.lastActiveAt)
                    put("activeSessionId", subAdmin.activeSessionId)
                })
            }
            root.put("subadmins", subAdminsObj)

            val activityLogsObj = JSONObject()
            for (log in _activityLogs.value) {
                activityLogsObj.put(log.id, JSONObject().apply {
                    put("id", log.id)
                    put("timestamp", log.timestamp)
                    put("userName", log.userName)
                    put("userRole", log.userRole)
                    put("actionType", log.actionType)
                    put("title", log.title)
                    put("details", log.details)
                    put("authorDeviceId", log.authorDeviceId)
                })
            }
            root.put("activity_logs", activityLogsObj)

            val monthlyRemObj = JSONObject()
            for ((key, rem) in _monthlyReminders.value) {
                monthlyRemObj.put(key, JSONObject().apply {
                    put("id", rem.id)
                    put("month", rem.month)
                    put("year", rem.year)
                    put("sentBy", rem.sentBy)
                    put("senderPhone", rem.senderPhone)
                    put("sentAt", rem.sentAt)
                    put("recipientsCount", rem.recipientsCount)
                    put("isSent", rem.isSent)
                })
            }
            root.put("monthly_reminders", monthlyRemObj)

            val noticesObj = JSONObject()
            for (notice in _marketNotices.value) {
                noticesObj.put(notice.id, JSONObject().apply {
                    put("id", notice.id)
                    put("title", notice.title)
                    put("message", notice.message)
                    put("category", notice.category)
                    put("priority", notice.priority)
                    put("authorName", notice.authorName)
                    put("authorRole", notice.authorRole)
                    put("authorDeviceId", notice.authorDeviceId)
                    put("dueDate", notice.dueDate)
                    put("createdAt", notice.createdAt)
                    put("isPinned", notice.isPinned)
                    put("targetAudience", notice.targetAudience)
                })
            }
            root.put("market_notices", noticesObj)

            val clearancesObj = JSONObject()
            for ((key, clearance) in _pmcTaxClearances.value) {
                clearancesObj.put(key, JSONObject().apply {
                    put("financialYear", clearance.financialYear)
                    put("isPaid", clearance.isPaid)
                    put("amountPaid", clearance.amountPaid)
                    put("receiptNumber", clearance.receiptNumber)
                    put("paidDate", clearance.paidDate)
                    put("paidBy", clearance.paidBy)
                    put("paidByRole", clearance.paidByRole)
                    put("paymentMode", clearance.paymentMode)
                    put("notes", clearance.notes)
                    put("updatedAt", clearance.updatedAt)
                })
            }
            root.put("pmc_tax_clearances", clearancesObj)

            saveToCache(root.toString())
        } catch (e: Exception) {
            Log.e(tag, "persistCurrentStateToCache error: ${e.message}")
        }
    }

    suspend fun clearAllData(): Unit = withContext(Dispatchers.IO) {
        Log.i(tag, "Explicit data wipe invoked...")
        _shops.value = emptyList()
        _tenants.value = emptyList()
        _rents.value = emptyList()
        _subAdmins.value = emptyList()

        sendDeleteRequest("shops")
        sendDeleteRequest("tenants")
        sendDeleteRequest("rents")
        sendDeleteRequest("subadmins")

        sendPutRequest("meta", JSONObject().apply {
            put("marketName", "Naseeb Lal Market")
            put("lastUpdated", System.currentTimeMillis())
        }.toString())

        prefs.edit().remove("cached_db").apply()
        _syncStatus.value = SyncStatus.SYNCED
        _lastSyncedAt.value = System.currentTimeMillis()
    }
}

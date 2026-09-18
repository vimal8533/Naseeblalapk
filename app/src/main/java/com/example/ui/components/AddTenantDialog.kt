package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Shop
import com.example.model.Tenant
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary
import com.example.util.PmcTaxHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddTenantDialog(
    initialTenant: Tenant? = null,
    availableShops: List<Shop> = emptyList(),
    canManagePersonalTenants: Boolean = false,
    initialWorkspaceMode: com.example.model.AppWorkspaceMode = com.example.model.AppWorkspaceMode.PUBLIC_MARKET,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        businessName: String,
        phone: String,
        shopNumber: String,
        numberOfShops: Int,
        advanceDeposit: Double,
        monthlyRent: Double,
        previousDues: Double,
        incrementYears: Int,
        incrementPercent: Double,
        billingCycle: String,
        joiningDate: String,
        idProof: String,
        notes: String,
        isPersonal: Boolean,
        electricityBill: Double
    ) -> Unit
) {
    var isPersonal by remember {
        mutableStateOf(
            initialTenant?.isPersonal ?: (initialWorkspaceMode == com.example.model.AppWorkspaceMode.PRIVATE_PERSONAL)
        )
    }
    var name by remember { mutableStateOf(initialTenant?.name ?: "") }
    var businessName by remember { mutableStateOf(initialTenant?.businessName ?: "") }
    var phone by remember { mutableStateOf(initialTenant?.phone ?: "") }
    var billingCycle by remember {
        mutableStateOf(initialTenant?.billingCycle?.ifBlank { "MONTHLY" } ?: "MONTHLY")
    }

    // Manual shop assignment: Number of shops and shop identity
    var numberOfShops by remember {
        mutableIntStateOf(initialTenant?.shopCount ?: 1)
    }
    var shopNumber by remember {
        mutableStateOf(initialTenant?.shopNumber ?: "")
    }

    // Mutually agreed monthly rent and deposit
    var monthlyRent by remember {
        mutableStateOf(
            if (initialTenant != null && initialTenant.monthlyRent > 0)
                initialTenant.monthlyRent.toInt().toString()
            else ""
        )
    }

    // Monthly Electricity Bill (For Personal Flat / Unit)
    var electricityBill by remember {
        mutableStateOf(
            if (initialTenant != null && initialTenant.electricityBill > 0)
                initialTenant.electricityBill.toInt().toString()
            else ""
        )
    }

    var advanceDeposit by remember {
        mutableStateOf(
            if (initialTenant != null && initialTenant.advanceDeposit > 0)
                initialTenant.advanceDeposit.toInt().toString()
            else "20000"
        )
    }

    // Previous Dues / Opening Balance (बकाया राशि)
    var previousDues by remember {
        mutableStateOf(
            if (initialTenant != null && initialTenant.previousDues > 0)
                initialTenant.previousDues.toInt().toString()
            else "0"
        )
    }

    // Rent Increment Policy: Default 1 year, 5%
    var incrementYears by remember { mutableIntStateOf(initialTenant?.incrementYears ?: 1) }
    var incrementPercent by remember {
        val initialPct = initialTenant?.incrementPercent ?: 5.0
        mutableStateOf(if (initialPct % 1.0 == 0.0) initialPct.toInt().toString() else initialPct.toString())
    }

    var joiningDate by remember {
        val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        mutableStateOf(initialTenant?.joiningDate?.ifBlank { today } ?: today)
    }

    var idProof by remember { mutableStateOf(initialTenant?.idProof ?: "") }
    var notes by remember { mutableStateOf(initialTenant?.notes ?: "") }
    var hasError by remember { mutableStateOf(false) }

    val validShopsCount = numberOfShops.coerceAtLeast(1)
    val annualPmcTax = PmcTaxHelper.getPmcTaxForShops(validShopsCount)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTenant == null) "Add New Tenant" else "Edit Tenant Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==================== PROPERTY TYPE SELECTOR (COMMERCIAL VS PERSONAL) ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 🏪 Commercial Market Shop Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                isPersonal = false
                                hasError = false
                            }
                            .testTag("select_commercial_type"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!isPersonal) NavyPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (!isPersonal) 2.dp else 1.dp,
                            color = if (!isPersonal) NavyPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (!isPersonal) NavyPrimary else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Store,
                                    contentDescription = null,
                                    tint = if (!isPersonal) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Commercial Shop",
                                fontSize = 12.sp,
                                fontWeight = if (!isPersonal) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isPersonal) NavyPrimary else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Market / Dukan",
                                fontSize = 10.sp,
                                color = if (!isPersonal) NavyPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // 🏢 Personal Flat / Residential Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                isPersonal = true
                                numberOfShops = 1
                                hasError = false
                            }
                            .testTag("select_personal_type"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPersonal) Color(0xFF0F766E).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isPersonal) 2.dp else 1.dp,
                            color = if (isPersonal) Color(0xFF0F766E) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isPersonal) Color(0xFF0F766E)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Apartment,
                                    contentDescription = null,
                                    tint = if (isPersonal) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Personal Flat",
                                fontSize = 12.sp,
                                fontWeight = if (isPersonal) FontWeight.Bold else FontWeight.Medium,
                                color = if (isPersonal) Color(0xFF0F766E) else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Residential / Unit",
                                fontSize = 10.sp,
                                color = if (isPersonal) Color(0xFF0F766E).copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isPersonal) {
                    // ==================== COMMERCIAL MARKET SHOP FORM ====================
                    // Full Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            hasError = false
                        },
                        label = { Text("Tenant Full Name *") },
                        placeholder = { Text("e.g. Ramesh Kumar") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        isError = hasError && name.isBlank(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Shop / Business Name
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Shop / Business Name") },
                        placeholder = { Text("e.g. Kumar General Store") },
                        leadingIcon = { Icon(Icons.Filled.CorporateFare, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_business_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Phone Number
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            hasError = false
                        },
                        label = { Text("Phone Number *") },
                        placeholder = { Text("10-digit mobile number") },
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = hasError && phone.isBlank(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_phone_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // MANUAL SHOP ASSIGNMENT CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Number of Shops Held",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Stepper (- / +)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconButton(
                                            onClick = { if (numberOfShops > 1) numberOfShops-- },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Remove,
                                                contentDescription = "Decrease",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NavyPrimary)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "$numberOfShops Shop${if (numberOfShops > 1) "s" else ""}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconButton(
                                            onClick = { numberOfShops++ },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = "Increase",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Shop Number(s)
                            OutlinedTextField(
                                value = shopNumber,
                                onValueChange = {
                                    shopNumber = it
                                    hasError = false
                                },
                                label = { Text("Shop Number(s) *") },
                                placeholder = { Text(if (numberOfShops > 1) "e.g. Shop 101, Shop 102" else "e.g. Shop 105") },
                                leadingIcon = { Icon(Icons.Filled.Store, contentDescription = null) },
                                isError = hasError && shopNumber.isBlank(),
                                supportingText = {
                                    Text(
                                        if (numberOfShops > 1) "Enter all $numberOfShops shop numbers separated by comma"
                                        else "Enter shop number assigned to this tenant"
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tenant_shop_input")
                            )

                            // Market shop inventory suggestions
                            val marketShops = remember(availableShops) {
                                availableShops.filter { !it.isPersonal }
                            }
                            if (marketShops.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Quick select from market inventory:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    marketShops.take(4).forEach { shop ->
                                        val isSelected = shopNumber.contains(shop.shopNumber)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isSelected) NavyPrimary
                                                    else MaterialTheme.colorScheme.surface
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) NavyPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .clickable {
                                                    shopNumber = if (isSelected) {
                                                        shopNumber.replace(shop.shopNumber, "").replace(", ,", ",").trim(',', ' ')
                                                    } else {
                                                        if (shopNumber.isBlank()) shop.shopNumber
                                                        else "$shopNumber, ${shop.shopNumber}"
                                                    }
                                                    hasError = false
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = shop.shopNumber,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Agreed Monthly Rent (Clean Single Column to avoid squeeze)
                    OutlinedTextField(
                        value = monthlyRent,
                        onValueChange = {
                            monthlyRent = it
                            hasError = false
                        },
                        label = { Text("Agreed Monthly Rent (₹) *") },
                        placeholder = { Text("e.g. 7000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = hasError && (monthlyRent.toDoubleOrNull() ?: 0.0) <= 0.0,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_rent_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Advance Deposit
                    OutlinedTextField(
                        value = advanceDeposit,
                        onValueChange = { advanceDeposit = it },
                        label = { Text("Advance Security Deposit (₹)") },
                        placeholder = { Text("e.g. 20000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_deposit_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Previous Dues / Opening Balance
                    OutlinedTextField(
                        value = previousDues,
                        onValueChange = { previousDues = it },
                        label = { Text("Previous Dues / Opening Balance (₹)") },
                        placeholder = { Text("0") },
                        leadingIcon = { Icon(Icons.Filled.HistoryEdu, contentDescription = null) },
                        supportingText = {
                            Text("Unpaid balance from before adding tenant to this app (Default: 0)")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_previous_dues_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rent Increment Policy Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Rent Increment Policy",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Period Stepper
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconButton(
                                            onClick = { if (incrementYears > 1) incrementYears-- },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Remove,
                                                contentDescription = "Decrease Years",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NavyPrimary)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Every $incrementYears yr${if (incrementYears > 1) "s" else ""}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconButton(
                                            onClick = { incrementYears++ },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = "Increase Years",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Increment Percentage Input
                            OutlinedTextField(
                                value = incrementPercent,
                                onValueChange = { incrementPercent = it },
                                label = { Text("Increment Rate (%)") },
                                placeholder = { Text("5") },
                                leadingIcon = { Icon(Icons.Filled.Percent, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tenant_increment_percent_input")
                            )

                            val currentRent = monthlyRent.toDoubleOrNull() ?: 0.0
                            val currentPct = incrementPercent.toDoubleOrNull() ?: 5.0
                            if (currentRent > 0.0 && currentPct > 0.0) {
                                val nextProjectedRent = currentRent * (1.0 + (currentPct / 100.0))
                                val increaseAmt = nextProjectedRent - currentRent
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Preview: After $incrementYears year${if (incrementYears > 1) "s" else ""}, rent will be ₹${nextProjectedRent.toInt()} (+₹${increaseAmt.toInt()} / month)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rent Billing Cycle
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyPrimary.copy(alpha = 0.04f)),
                        border = BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Rent Billing Cycle (किराया चक्र)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = NavyPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            val cycles = listOf(
                                "MONTHLY" to "Monthly (1M)",
                                "QUARTERLY" to "3 Months",
                                "HALF_YEARLY" to "6 Months",
                                "YEARLY" to "1 Year"
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                cycles.forEach { (key, label) ->
                                    val isSelected = billingCycle.equals(key, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) NavyPrimary else MaterialTheme.colorScheme.surface)
                                            .border(
                                                1.dp,
                                                if (isSelected) NavyPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { billingCycle = key }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 10.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Joining Date (Single Column, full width)
                    DatePickerField(
                        label = "Joining Date (Calendar)",
                        selectedDate = joiningDate,
                        onDateSelected = { newDate ->
                            joiningDate = newDate
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "tenant_joining_date_input"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ID Proof (Single Column, full width)
                    OutlinedTextField(
                        value = idProof,
                        onValueChange = { idProof = it },
                        label = { Text("Aadhaar / ID Proof Number") },
                        placeholder = { Text("e.g. 12-digit Aadhaar / Voter ID") },
                        leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_id_proof_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Annual PMC Tax Policy Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccountBalance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Annual Municipal PMC Tax: ₹1,000 / Shop",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "PMC Tax is charged ONLY ONCE A YEAR (₹${annualPmcTax.toInt()} for ${validShopsCount} shop${if (validShopsCount > 1) "s" else ""}) upon completing 1 full year from joining date ($joiningDate). It will NOT be added to regular monthly rents.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Agreement Details (Optional)") },
                        leadingIcon = { Icon(Icons.Filled.Notes, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_notes_input")
                    )
                } else {
                    // ==================== PERSONAL FLAT / RESIDENTIAL FORM ====================
                    // Clean single-column layout without heavy market fields
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E).copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, Color(0xFF0F766E).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Apartment,
                                contentDescription = null,
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Residential Flat / Room Tenant",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF0F766E)
                                )
                                Text(
                                    text = "Private to Admin. Market taxes, shop count & increments do not apply.",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Full Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            hasError = false
                        },
                        label = { Text("Tenant Full Name *") },
                        placeholder = { Text("e.g. Rahul Sharma") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        isError = hasError && name.isBlank(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Phone
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            hasError = false
                        },
                        label = { Text("Phone Number *") },
                        placeholder = { Text("10-digit mobile number") },
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = hasError && phone.isBlank(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_phone_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Flat / Room / Unit Number
                    OutlinedTextField(
                        value = shopNumber,
                        onValueChange = {
                            shopNumber = it
                            hasError = false
                        },
                        label = { Text("Flat / Unit / Room Number *") },
                        placeholder = { Text("e.g. Flat 302, Unit 2B, Ground Floor") },
                        leadingIcon = { Icon(Icons.Filled.Apartment, contentDescription = null) },
                        isError = hasError && shopNumber.isBlank(),
                        supportingText = {
                            Text("Enter flat or room number assigned to this tenant")
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_shop_input")
                    )

                    // Quick select from personal properties inventory if any
                    val personalProperties = remember(availableShops) {
                        availableShops.filter { it.isPersonal }
                    }
                    if (personalProperties.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Quick select from personal inventory:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            personalProperties.take(4).forEach { prop ->
                                val isSelected = shopNumber == prop.shopNumber
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) Color(0xFF0F766E)
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(0xFF0F766E) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            shopNumber = prop.shopNumber
                                            hasError = false
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = prop.shopNumber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Monthly Rent
                    OutlinedTextField(
                        value = monthlyRent,
                        onValueChange = {
                            monthlyRent = it
                            hasError = false
                        },
                        label = { Text("Monthly Rent (₹) *") },
                        placeholder = { Text("e.g. 12000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = hasError && (monthlyRent.toDoubleOrNull() ?: 0.0) <= 0.0,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_rent_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Monthly Electricity Bill (Flat / Unit specific)
                    OutlinedTextField(
                        value = electricityBill,
                        onValueChange = { electricityBill = it },
                        label = { Text("Monthly Electricity Bill (₹)") },
                        placeholder = { Text("e.g. 1500 (Optional, added to monthly bill)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.ElectricMeter,
                                contentDescription = "Electricity Bill",
                                tint = Color(0xFFD97706)
                            )
                        },
                        supportingText = {
                            val elecVal = electricityBill.toDoubleOrNull() ?: 0.0
                            val rVal = monthlyRent.toDoubleOrNull() ?: 0.0
                            if (elecVal > 0.0 && rVal > 0.0) {
                                Text(
                                    "Total Monthly Due: ₹${(rVal + elecVal).toInt()} (Rent: ₹${rVal.toInt()} + Electricity: ₹${elecVal.toInt()})",
                                    color = Color(0xFF0F766E),
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text("Monthly electricity charges in ₹ for this flat")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_electricity_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Security Deposit
                    OutlinedTextField(
                        value = advanceDeposit,
                        onValueChange = { advanceDeposit = it },
                        label = { Text("Security / Advance Deposit (₹)") },
                        placeholder = { Text("e.g. 24000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_deposit_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Previous Dues / Opening Balance
                    OutlinedTextField(
                        value = previousDues,
                        onValueChange = { previousDues = it },
                        label = { Text("Previous Dues / Opening Balance (₹)") },
                        placeholder = { Text("0") },
                        leadingIcon = { Icon(Icons.Filled.HistoryEdu, contentDescription = null) },
                        supportingText = {
                            Text("Previous unpaid dues, if any (Default: 0)")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_previous_dues_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rent Billing Cycle
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E).copy(alpha = 0.04f)),
                        border = BorderStroke(1.dp, Color(0xFF0F766E).copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color(0xFF0F766E),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Rent Cycle (किराया चक्र)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF0F766E)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            val cycles = listOf(
                                "MONTHLY" to "Monthly (1M)",
                                "QUARTERLY" to "3 Months",
                                "HALF_YEARLY" to "6 Months",
                                "YEARLY" to "1 Year"
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                cycles.forEach { (key, label) ->
                                    val isSelected = billingCycle.equals(key, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFF0F766E) else MaterialTheme.colorScheme.surface)
                                            .border(
                                                1.dp,
                                                if (isSelected) Color(0xFF0F766E) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { billingCycle = key }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 10.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Move-in / Joining Date
                    DatePickerField(
                        label = "Move-in / Joining Date",
                        selectedDate = joiningDate,
                        onDateSelected = { newDate ->
                            joiningDate = newDate
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "tenant_joining_date_input"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Aadhaar / ID Proof
                    OutlinedTextField(
                        value = idProof,
                        onValueChange = { idProof = it },
                        label = { Text("Aadhaar / ID Proof Number") },
                        placeholder = { Text("e.g. 12-digit Aadhaar") },
                        leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_id_proof_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Agreement Notes / Remarks (Optional)") },
                        leadingIcon = { Icon(Icons.Filled.Notes, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tenant_notes_input")
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Save Button
                Button(
                    onClick = {
                        val rentVal = monthlyRent.toDoubleOrNull() ?: 0.0
                        if (name.isBlank() || phone.isBlank() || shopNumber.isBlank() || rentVal <= 0.0) {
                            hasError = true
                        } else {
                            val pct = if (isPersonal) 0.0 else (incrementPercent.toDoubleOrNull() ?: 5.0)
                            val dues = previousDues.toDoubleOrNull() ?: 0.0
                            val shopsCount = if (isPersonal) 1 else numberOfShops.coerceAtLeast(1)
                            val incYears = if (isPersonal) 1 else incrementYears.coerceAtLeast(1)
                            val bName = if (isPersonal) "" else businessName.trim()
                            val elecVal = if (isPersonal) (electricityBill.toDoubleOrNull() ?: 0.0) else 0.0
                            onSave(
                                name.trim(),
                                bName,
                                phone.trim(),
                                shopNumber.trim(),
                                shopsCount,
                                advanceDeposit.toDoubleOrNull() ?: 0.0,
                                rentVal,
                                dues.coerceAtLeast(0.0),
                                incYears,
                                pct.coerceAtLeast(0.0),
                                billingCycle,
                                joiningDate.trim(),
                                idProof.trim(),
                                notes.trim(),
                                isPersonal,
                                elecVal
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_tenant_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPersonal) Color(0xFF0F766E) else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (initialTenant == null) {
                            if (isPersonal) "SAVE PERSONAL TENANT" else "SAVE TENANT"
                        } else {
                            if (isPersonal) "UPDATE PERSONAL TENANT" else "UPDATE TENANT"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

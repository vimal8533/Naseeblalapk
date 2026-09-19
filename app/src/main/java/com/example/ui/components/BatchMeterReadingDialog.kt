package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.RentRecord
import com.example.model.Tenant
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusPaid

/**
 * Data item representing a flat's meter reading state in the batch dialog
 */
data class BatchMeterEntry(
    val rent: RentRecord,
    val tenant: Tenant?,
    var prevReadingStr: String,
    var currentReadingStr: String,
    var ratePerUnitStr: String
)

/**
 * Professional, clutter-free Batch Meter Reading Entry Dialog.
 *
 * Dedicated for Sub-metered Flats (isPersonal == true and sub-metered).
 * Allows landlords to review previous readings, enter current readings,
 * see units consumed & electricity bill calculated automatically,
 * and save all at once in 1 click.
 */
@Composable
fun BatchMeterReadingDialog(
    month: String,
    year: Int,
    subMeteredRents: List<RentRecord>,
    tenants: List<Tenant>,
    onDismiss: () -> Unit,
    onSaveBatch: (entries: List<Triple<String, Triple<Double, Double, Double>, Double>>) -> Unit
    // Triple<rentId, Triple<prev, current, rate>, billAmount>
) {
    val tenantMap = remember(tenants) { tenants.associateBy { it.id } }

    // Map of rentId -> Pair(currentReadingStr, rateStr)
    val currentReadingsState = remember(subMeteredRents) {
        mutableStateMapOf<String, String>().apply {
            subMeteredRents.forEach { r ->
                if (r.currentMeterReading > 0.0) {
                    put(r.id, if (r.currentMeterReading % 1.0 == 0.0) r.currentMeterReading.toInt().toString() else r.currentMeterReading.toString())
                } else {
                    put(r.id, "")
                }
            }
        }
    }

    val prevReadingsState = remember(subMeteredRents, tenants) {
        mutableStateMapOf<String, String>().apply {
            subMeteredRents.forEach { r ->
                val tenant = tenantMap[r.tenantId]
                val prev = when {
                    r.prevMeterReading > 0.0 -> r.prevMeterReading
                    tenant != null && tenant.lastMeterReading > 0.0 -> tenant.lastMeterReading
                    else -> 0.0
                }
                put(r.id, if (prev % 1.0 == 0.0) prev.toInt().toString() else prev.toString())
            }
        }
    }

    // Default rate per unit
    var defaultRateStr by remember {
        val sampleRate = subMeteredRents.firstOrNull { it.electricityRatePerUnit > 0.0 }?.electricityRatePerUnit
            ?: tenants.firstOrNull { it.electricityRatePerUnit > 0.0 }?.electricityRatePerUnit
            ?: 10.0
        mutableStateOf(if (sampleRate % 1.0 == 0.0) sampleRate.toInt().toString() else sampleRate.toString())
    }

    // Rate state per item
    val ratesState = remember(subMeteredRents, tenants, defaultRateStr) {
        mutableStateMapOf<String, String>().apply {
            subMeteredRents.forEach { r ->
                val tenant = tenantMap[r.tenantId]
                val rate = when {
                    r.electricityRatePerUnit > 0.0 -> r.electricityRatePerUnit
                    tenant != null && tenant.electricityRatePerUnit > 0.0 -> tenant.electricityRatePerUnit
                    else -> defaultRateStr.toDoubleOrNull() ?: 10.0
                }
                put(r.id, if (rate % 1.0 == 0.0) rate.toInt().toString() else rate.toString())
            }
        }
    }

    // Count how many have valid readings
    val completedCount = subMeteredRents.count { r ->
        val prev = prevReadingsState[r.id]?.toDoubleOrNull() ?: 0.0
        val curr = currentReadingsState[r.id]?.toDoubleOrNull() ?: -1.0
        curr >= prev && curr > 0.0
    }

    // Total electricity bill calculated across all valid entries
    val totalBatchElectricityBill = subMeteredRents.sumOf { r ->
        val prev = prevReadingsState[r.id]?.toDoubleOrNull() ?: 0.0
        val curr = currentReadingsState[r.id]?.toDoubleOrNull() ?: 0.0
        val rate = ratesState[r.id]?.toDoubleOrNull() ?: 10.0
        if (curr >= prev && curr > 0.0) {
            (curr - prev) * rate
        } else {
            0.0
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("batch_meter_reading_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ElectricMeter,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Monthly Meter Readings",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                            Text(
                                text = "$month $year • Sub-metered Flats (${subMeteredRents.size})",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Summary & Quick Rate Row
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F766E).copy(alpha = 0.07f),
                    border = BorderStroke(1.dp, Color(0xFF0F766E).copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = if (completedCount == subMeteredRents.size) StatusPaid else Color(0xFF0F766E),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$completedCount of ${subMeteredRents.size} Flats Recorded",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F766E)
                                )
                            }
                            if (totalBatchElectricityBill > 0) {
                                Text(
                                    text = "Total Electricity Calculated: ₹${totalBatchElectricityBill.toInt()}",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF0F766E),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Apply Common Rate Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Rate: ₹", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Box(
                                modifier = Modifier
                                    .width(52.dp)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFF0F766E).copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.text.BasicTextField(
                                    value = defaultRateStr,
                                    onValueChange = { newVal ->
                                        defaultRateStr = newVal
                                        val validRate = newVal.toDoubleOrNull()
                                        if (validRate != null && validRate > 0) {
                                            subMeteredRents.forEach { r ->
                                                ratesState[r.id] = newVal
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F766E),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("batch_common_rate_input")
                                )
                            }
                            Text("/unit", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable List of Sub-metered Flats
                if (subMeteredRents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No sub-metered flats found for $month $year.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(subMeteredRents, key = { it.id }) { rent ->
                            val tenant = tenantMap[rent.tenantId]
                            val prevStr = prevReadingsState[rent.id] ?: "0"
                            val currStr = currentReadingsState[rent.id] ?: ""
                            val rateStr = ratesState[rent.id] ?: defaultRateStr

                            val prevVal = prevStr.toDoubleOrNull() ?: 0.0
                            val currVal = currStr.toDoubleOrNull() ?: 0.0
                            val rateVal = rateStr.toDoubleOrNull() ?: 10.0

                            val isEntered = currStr.isNotBlank() && currVal >= prevVal
                            val units = if (isEntered) (currVal - prevVal).coerceAtLeast(0.0) else 0.0
                            val bill = units * rateVal
                            val isInvalid = currStr.isNotBlank() && currVal < prevVal

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("batch_meter_card_${rent.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isEntered) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = when {
                                        isInvalid -> Color(0xFFEF4444)
                                        isEntered -> Color(0xFF86EFAC)
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    }
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    // Row 1: Flat Number & Tenant Name + Bill Preview
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF0F766E).copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (rent.shopNumber.startsWith("Flat", ignoreCase = true)) rent.shopNumber else "Flat ${rent.shopNumber}",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F766E)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = rent.tenantName.ifBlank { "Tenant" },
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.5.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (isEntered && bill > 0) {
                                            Surface(
                                                color = Color(0xFFD97706).copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "+₹${bill.toInt()} (${units.toInt()} u)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFB45309),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        } else if (isEntered) {
                                            Text("0 units", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Row 2: Inputs (Previous Reading -> Current Reading -> Rate)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Previous Reading Box (Editable if landlord needs to correct baseline)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Prev Reading",
                                                fontSize = 9.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(38.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.White)
                                                    .border(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                androidx.compose.foundation.text.BasicTextField(
                                                    value = prevStr,
                                                    onValueChange = { prevReadingsState[rent.id] = it },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    textStyle = androidx.compose.ui.text.TextStyle(
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    ),
                                                    modifier = Modifier.fillMaxWidth().testTag("prev_reading_${rent.id}")
                                                )
                                            }
                                        }

                                        // Arrow
                                        Text("➜", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 14.dp))

                                        // Current Reading Box (Primary input for monthly meter run)
                                        Column(modifier = Modifier.weight(1.2f)) {
                                            Text(
                                                text = "Current Reading *",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isInvalid) Color(0xFFEF4444) else NavyPrimary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(38.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.White)
                                                    .border(
                                                        width = if (isInvalid) 1.5.dp else 1.dp,
                                                        color = when {
                                                            isInvalid -> Color(0xFFEF4444)
                                                            isEntered -> Color(0xFF16A34A)
                                                            else -> NavyPrimary.copy(alpha = 0.5f)
                                                        },
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 8.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                androidx.compose.foundation.text.BasicTextField(
                                                    value = currStr,
                                                    onValueChange = { currentReadingsState[rent.id] = it },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    textStyle = androidx.compose.ui.text.TextStyle(
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isInvalid) Color(0xFFEF4444) else NavyDark
                                                    ),
                                                    decorationBox = { innerTextField ->
                                                        if (currStr.isEmpty()) {
                                                            Text(
                                                                "e.g. ${(prevVal + 50).toInt()}",
                                                                fontSize = 11.5.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                            )
                                                        }
                                                        innerTextField()
                                                    },
                                                    modifier = Modifier.fillMaxWidth().testTag("curr_reading_${rent.id}")
                                                )
                                            }
                                        }

                                        // Rate Box
                                        Column(modifier = Modifier.weight(0.9f)) {
                                            Text(
                                                text = "₹/Unit",
                                                fontSize = 9.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(38.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.White)
                                                    .border(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                androidx.compose.foundation.text.BasicTextField(
                                                    value = rateStr,
                                                    onValueChange = { ratesState[rent.id] = it },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    textStyle = androidx.compose.ui.text.TextStyle(
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    ),
                                                    modifier = Modifier.fillMaxWidth().testTag("rate_${rent.id}")
                                                )
                                            }
                                        }
                                    }

                                    if (isInvalid) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "⚠️ Current reading must be ≥ previous ($prevStr)",
                                            fontSize = 9.5.sp,
                                            color = Color(0xFFEF4444),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val entriesToSave = mutableListOf<Triple<String, Triple<Double, Double, Double>, Double>>()
                            subMeteredRents.forEach { r ->
                                val prev = prevReadingsState[r.id]?.toDoubleOrNull() ?: 0.0
                                val curr = currentReadingsState[r.id]?.toDoubleOrNull()
                                val rate = ratesState[r.id]?.toDoubleOrNull() ?: 10.0
                                if (curr != null && curr >= prev && curr > 0.0) {
                                    val bill = (curr - prev) * rate
                                    entriesToSave.add(Triple(r.id, Triple(prev, curr, rate), bill))
                                }
                            }
                            onSaveBatch(entriesToSave)
                        },
                        enabled = completedCount > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp)
                            .testTag("save_batch_meter_readings_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (completedCount > 0) "Save $completedCount Flats ✓" else "Enter Readings",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

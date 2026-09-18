package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.RentRecord
import com.example.model.Tenant
import java.util.Locale

@Composable
fun ElectricityMeterDialog(
    rent: RentRecord,
    tenant: Tenant?,
    onDismiss: () -> Unit,
    onSave: (prevReading: Double, currentReading: Double, ratePerUnit: Double) -> Unit
) {
    val scrollState = rememberScrollState()

    // Initialize previous reading
    val initialPrev = remember(rent, tenant) {
        when {
            rent.prevMeterReading > 0.0 -> rent.prevMeterReading
            rent.currentMeterReading > 0.0 -> rent.prevMeterReading
            tenant != null && tenant.lastMeterReading > 0.0 -> tenant.lastMeterReading
            else -> 0.0
        }
    }

    // Initialize current reading
    val initialCurrent = remember(rent) {
        if (rent.currentMeterReading > 0.0) rent.currentMeterReading.toString() else ""
    }

    // Initialize rate per unit
    val initialRate = remember(rent, tenant) {
        when {
            rent.electricityRatePerUnit > 0.0 -> rent.electricityRatePerUnit
            tenant != null && tenant.electricityRatePerUnit > 0.0 -> tenant.electricityRatePerUnit
            else -> 10.0
        }
    }

    var prevReadingStr by remember {
        mutableStateOf(if (initialPrev % 1.0 == 0.0) initialPrev.toInt().toString() else initialPrev.toString())
    }
    var currentReadingStr by remember { mutableStateOf(initialCurrent) }
    var ratePerUnitStr by remember {
        mutableStateOf(if (initialRate % 1.0 == 0.0) initialRate.toInt().toString() else initialRate.toString())
    }

    val prevVal = prevReadingStr.toDoubleOrNull() ?: 0.0
    val currentVal = currentReadingStr.toDoubleOrNull() ?: 0.0
    val rateVal = ratePerUnitStr.toDoubleOrNull() ?: 10.0

    val unitsConsumed = (currentVal - prevVal).coerceAtLeast(0.0)
    val electricityBill = unitsConsumed * rateVal

    // Base Flat Rent (excluding previously saved electricity bill)
    val flatBaseRent = (rent.amountDue - rent.electricityBill).coerceAtLeast(0.0)
    val newTotalMonthlyDue = flatBaseRent + electricityBill

    val isCurrentLessThanPrev = currentReadingStr.isNotBlank() && currentVal < prevVal
    val isValid = currentReadingStr.isNotBlank() && currentVal >= prevVal && rateVal >= 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("electricity_meter_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Header
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
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Electricity Meter Reading",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val flatDisplay = if (rent.shopNumber.startsWith("Flat", ignoreCase = true)) rent.shopNumber else "Flat ${rent.shopNumber}"
                            Text(
                                text = "$flatDisplay • ${rent.month} ${rent.year}",
                                fontSize = 12.sp,
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

                Spacer(modifier = Modifier.height(14.dp))

                // Formula Info Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F766E).copy(alpha = 0.08f))
                        .border(1.dp, Color(0xFF0F766E).copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡ Calculation Rule: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F766E)
                        )
                        Text(
                            text = "(Current - Previous) × Rate per Unit",
                            fontSize = 11.sp,
                            color = Color(0xFF0F766E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Row 1: Previous Reading & Rate per Unit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = prevReadingStr,
                        onValueChange = { prevReadingStr = it },
                        label = { Text("Previous Reading") },
                        placeholder = { Text("e.g. 1200") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("prev_reading_input")
                    )

                    OutlinedTextField(
                        value = ratePerUnitStr,
                        onValueChange = { ratePerUnitStr = it },
                        label = { Text("Rate / Unit (₹)") },
                        placeholder = { Text("e.g. 10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rate_per_unit_input")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row 2: Current Reading (Highlighted)
                OutlinedTextField(
                    value = currentReadingStr,
                    onValueChange = { currentReadingStr = it },
                    label = { Text("Current Reading (New) *") },
                    placeholder = { Text("e.g. 1350") },
                    isError = isCurrentLessThanPrev,
                    supportingText = {
                        if (isCurrentLessThanPrev) {
                            Text(
                                text = "Current reading cannot be lower than previous (${prevVal.toInt()})",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("Enter meter units recorded for this month")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("current_reading_input")
                )

                if (isCurrentLessThanPrev) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Please ensure current meter reading is greater than or equal to previous reading.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time Calculation Summary Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.05f))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "BILL CALCULATION BREAKDOWN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Units Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Units Consumed (${currentVal.toInt()} - ${prevVal.toInt()}):",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${unitsConsumed.toInt()} Units",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Rate Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Rate per Unit:",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "₹${if (rateVal % 1.0 == 0.0) rateVal.toInt().toString() else rateVal.toString()}/unit",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Electricity Bill Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Electricity Bill:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%,d", electricityBill.toInt())}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFE2E8F0))
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Flat Base Rent
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Flat Base Rent:",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%,d", flatBaseRent.toInt())}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // New Total Monthly Due
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Monthly Due:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F766E)
                            )
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%,d", newTotalMonthlyDue.toInt())}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F766E)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (isValid) {
                                onSave(prevVal, currentVal, rateVal)
                                onDismiss()
                            }
                        },
                        enabled = isValid,
                        modifier = Modifier
                            .weight(1.4f)
                            .height(46.dp)
                            .testTag("save_electricity_meter_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F766E)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Save Bill (₹${electricityBill.toInt()})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

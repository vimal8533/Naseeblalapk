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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.RentRecord
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPartial
import com.example.ui.theme.StatusPending
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog to edit / correct a previously recorded payment amount.
 * Requires a mandatory reason for audit trail & activity logging.
 */
@Composable
fun CorrectPaymentDialog(
    rent: RentRecord,
    onDismiss: () -> Unit,
    onConfirmCorrection: (newTotalPaid: Double, paymentMode: String, reason: String, paidDate: String) -> Unit
) {
    var amountInput by remember {
        mutableStateOf(rent.amountPaid.toInt().toString())
    }
    var selectedMode by remember {
        mutableStateOf(if (rent.paymentMode.isNotBlank()) rent.paymentMode else "UPI / ONLINE")
    }
    var paymentDate by remember {
        val defaultDate = rent.paidDate.ifBlank {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        }
        mutableStateOf(defaultDate)
    }
    var reasonInput by remember { mutableStateOf("") }
    var touchedReason by remember { mutableStateOf(false) }

    val paymentModes = listOf("CASH", "UPI / ONLINE", "CHEQUE", "BANK_TRANSFER")
    val quickReasons = listOf(
        "Mistyped extra zero",
        "Wrong amount fed",
        "Wrong shop selected",
        "Cash recount adjustment",
        "Payment cancelled / bounced"
    )

    val currentNewAmount = amountInput.toDoubleOrNull()
    val isAmountValid = currentNewAmount != null && currentNewAmount >= 0.0
    val isReasonValid = reasonInput.trim().isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NavyPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Correct Payment",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${rent.shopNumber} • ${rent.tenantName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Recorded Summary Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total Rent Due:", style = MaterialTheme.typography.bodySmall)
                            Text(text = "₹${rent.amountDue.toInt()}", fontWeight = FontWeight.SemiBold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Currently Recorded Paid:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "₹${rent.amountPaid.toInt()}",
                                color = StatusPaid,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Period:", style = MaterialTheme.typography.bodySmall)
                            Text(text = "${rent.month} ${rent.year}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input: New Correct Total Paid Amount
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Corrected Total Amount Paid (₹)") },
                    placeholder = { Text("Enter correct total paid") },
                    leadingIcon = {
                        Text(
                            text = "₹",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                        )
                    },
                    isError = !isAmountValid,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("correct_payment_amount_input")
                )

                // Real-time Preview of resulting status & balance
                if (isAmountValid && currentNewAmount != null) {
                    val previewBalance = (rent.amountDue - currentNewAmount).coerceAtLeast(0.0)
                    val (previewStatus, previewColor) = when {
                        currentNewAmount >= rent.amountDue && rent.amountDue > 0.0 -> Pair("PAID", StatusPaid)
                        currentNewAmount > 0.0 -> Pair("PARTIAL", StatusPartial)
                        else -> Pair("PENDING", StatusPending)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(previewColor.copy(alpha = 0.1f))
                            .border(1.dp, previewColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "New Status: $previewStatus",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = previewColor
                            )
                            Text(
                                text = "Remaining Due: ₹${previewBalance.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Mode Selection
                Text(
                    text = "Payment Mode:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    paymentModes.forEach { mode ->
                        val isSelected = selectedMode == mode
                        val label = when (mode) {
                            "CASH" -> "Cash"
                            "UPI / ONLINE" -> "UPI"
                            "CHEQUE" -> "Cheque"
                            else -> "Bank"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedMode = mode }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Date
                DatePickerField(
                    label = "Payment Date",
                    selectedDate = paymentDate,
                    onDateSelected = { paymentDate = it },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "correct_payment_date_picker"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mandatory Reason Field
                OutlinedTextField(
                    value = reasonInput,
                    onValueChange = {
                        reasonInput = it
                        touchedReason = true
                    },
                    label = { Text("Reason for Correction *") },
                    placeholder = { Text("e.g. Mistyped extra zero") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = if (touchedReason && !isReasonValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    isError = touchedReason && !isReasonValid,
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("correct_payment_reason_input")
                )

                if (touchedReason && !isReasonValid) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reason is mandatory for audit & log record.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick-tap reason chips
                Text(
                    text = "Quick Reasons:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    quickReasons.chunked(2).forEach { rowReasons ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowReasons.forEach { reason ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                        .clickable {
                                            reasonInput = reason
                                            touchedReason = true
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = reason,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                            if (rowReasons.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            touchedReason = true
                            if (isAmountValid && isReasonValid && currentNewAmount != null) {
                                onConfirmCorrection(
                                    currentNewAmount,
                                    selectedMode,
                                    reasonInput.trim(),
                                    paymentDate
                                )
                            }
                        },
                        enabled = isAmountValid && isReasonValid,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("confirm_correction_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Save Correction",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.model.RentRecord
import com.example.model.Tenant
import com.example.model.UserSession
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPending
import com.example.util.SimInfo
import com.example.util.SmsReminderHelper

@Composable
fun BatchSmsReminderDialog(
    month: String,
    year: Int,
    pendingRents: List<RentRecord>,
    tenants: List<Tenant> = emptyList(),
    currentUser: UserSession?,
    onDismiss: () -> Unit,
    onConfirmSend: (senderName: String, senderContactPhone: String, subscriptionId: Int?, onProgress: (Int, Int) -> Unit, onComplete: (Int, Int) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isPersonalWorkspace = pendingRents.any { it.isPersonal }

    var senderName by remember {
        mutableStateOf(
            if (isPersonalWorkspace) {
                currentUser?.displayName?.ifBlank { "Property Owner" } ?: "Property Owner"
            } else {
                currentUser?.displayName?.ifBlank { "Naseeb Lal Market" } ?: "Naseeb Lal Market"
            }
        )
    }

    var senderContactPhone by remember {
        mutableStateOf(currentUser?.phone?.ifBlank { "9876543210" } ?: "9876543210")
    }

    var availableSims by remember { mutableStateOf<List<SimInfo>>(emptyList()) }
    var selectedSimIndex by remember { mutableIntStateOf(0) }

    var isSending by remember { mutableStateOf(false) }
    var sentProgress by remember { mutableIntStateOf(0) }
    var totalToSend by remember { mutableIntStateOf(pendingRents.size) }

    // Load available SIMs
    LaunchedEffect(Unit) {
        availableSims = SmsReminderHelper.getAvailableSims(context)
    }

    // Permission launcher for SMS
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isSending = true
            val chosenSubscriptionId = availableSims.getOrNull(selectedSimIndex)?.subscriptionId
            onConfirmSend(
                senderName.trim(),
                senderContactPhone.trim(),
                chosenSubscriptionId,
                { current, total ->
                    sentProgress = current
                    totalToSend = total
                },
                { success, failed ->
                    isSending = false
                    Toast.makeText(
                        context,
                        "✅ $success SMS sent successfully (${failed} skipped/failed)",
                        Toast.LENGTH_LONG
                    ).show()
                    onDismiss()
                }
            )
        } else {
            Toast.makeText(
                context,
                "SMS permission is needed to dispatch reminders from SIM.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val sampleRent = pendingRents.firstOrNull() ?: RentRecord(
        tenantName = if (isPersonalWorkspace) "Rahul Sharma" else "Ramesh Kumar",
        shopNumber = if (isPersonalWorkspace) "Flat 101" else "Shop G-01",
        amountDue = if (isPersonalWorkspace) 9500.0 else 8500.0,
        electricityBill = if (isPersonalWorkspace) 1500.0 else 0.0,
        isPersonal = isPersonalWorkspace,
        amountPaid = 0.0
    )

    val sampleTenant = remember(sampleRent, tenants) {
        tenants.find { it.id == sampleRent.tenantId }
    }
    val sampleCycleMonths = sampleTenant?.cycleMonths ?: 1

    val previewMessage = SmsReminderHelper.formatReminderMessage(
        tenantName = sampleRent.tenantName,
        shopNumber = sampleRent.shopNumber,
        month = month,
        year = year,
        pendingAmount = if (sampleRent.pendingAmount > 0) sampleRent.pendingAmount else (if (isPersonalWorkspace) 9500.0 else 8500.0),
        senderName = senderName,
        senderContactPhone = senderContactPhone,
        cycleMonths = sampleCycleMonths,
        isPersonal = isPersonalWorkspace,
        electricityBill = sampleRent.electricityBill,
        flatBaseRent = (sampleRent.amountDue - sampleRent.electricityBill).coerceAtLeast(0.0)
    )

    Dialog(
        onDismissRequest = { if (!isSending) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("batch_sms_reminder_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val themeTint = if (isPersonalWorkspace) Color(0xFF0F766E) else NavyPrimary
                            Icon(
                                imageVector = Icons.Filled.Sms,
                                contentDescription = null,
                                tint = themeTint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isPersonalWorkspace) "Send Flat Rent & Electricity SMS" else "Send Rent SMS Reminders",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$month $year • 1-Click SIM SMS",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!isSending) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pending Recipients",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${pendingRents.size} Tenants",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = StatusPending
                        )
                    }

                    val totalDueAmount = pendingRents.sumOf { it.pendingAmount }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Balance Due",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${if (totalDueAmount % 1.0 == 0.0) String.format("%,d", totalDueAmount.toLong()) else String.format("%,.2f", totalDueAmount)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Multi-month billing cycle banner (if any)
                val multiCycleCount = remember(pendingRents, tenants) {
                    pendingRents.count { rent ->
                        val t = tenants.find { it.id == rent.tenantId }
                        (t?.cycleMonths ?: 1) > 1
                    }
                }
                if (multiCycleCount > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldAccent.copy(alpha = 0.15f))
                            .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "⚡ Is 1-Click SMS me $multiCycleCount Tenant(s) ki 3-Month / 6-Month / Yearly cycle due hai aur unka cycle amount automatic jud gaya hai.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyDark,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sender Name Field
                Text(
                    text = "Aapka Naam (Shown in SMS)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SMS ke aakhiri me 'Sampark karein' ke sath aapka naam likha jayega:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = senderName,
                    onValueChange = { senderName = it },
                    placeholder = { Text("e.g. Sub-Admin Rahul") },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = NavyPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isSending,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sender_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Sender Contact Number Field (Custom for each sub-admin/admin)
                Text(
                    text = "Your Contact Number (Shown in SMS)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tenant ko message me yeh contact number dikhega jisse wo aap se baat kar sakein:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = senderContactPhone,
                    onValueChange = { senderContactPhone = it },
                    placeholder = { Text("e.g. 9876543210") },
                    leadingIcon = {
                        Icon(Icons.Filled.Phone, contentDescription = null, tint = NavyPrimary)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isSending,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sender_contact_phone_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // SIM Slot Selection (if device has multiple SIMs)
                if (availableSims.size > 1) {
                    Text(
                        text = "Choose SIM for Dispatching SMS:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableSims.forEachIndexed { index, sim ->
                            val isSelected = selectedSimIndex == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) NavyPrimary.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isSelected) NavyPrimary else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable(enabled = !isSending) { selectedSimIndex = index }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.SimCard,
                                        contentDescription = null,
                                        tint = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = sim.displayName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.5.sp,
                                            color = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Slot ${sim.slotIndex + 1}",
                                            fontSize = 9.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Live SMS Preview Card
                Text(
                    text = "Live Message Preview (Sample Tenant):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💬 SMS Format",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "100% FREE SIM SMS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = previewMessage,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp,
                            color = Color(0xFF1E293B),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = StatusPaid,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Strict policy: No UPI ID or payment link included.",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Indicator during dispatch
                if (isSending) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavyPrimary.copy(alpha = 0.08f))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp),
                                color = NavyPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sending SMS $sentProgress of $totalToSend...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = NavyPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val progressFraction = if (totalToSend > 0) sentProgress.toFloat() / totalToSend.toFloat() else 0f
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = NavyPrimary,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSending,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                                isSending = true
                                val chosenSubscriptionId = availableSims.getOrNull(selectedSimIndex)?.subscriptionId
                                onConfirmSend(
                                    senderName.trim(),
                                    senderContactPhone.trim(),
                                    chosenSubscriptionId,
                                    { current, total ->
                                        sentProgress = current
                                        totalToSend = total
                                    },
                                    { success, failed ->
                                        isSending = false
                                        Toast.makeText(
                                            context,
                                            "✅ $success SMS sent successfully (${failed} skipped/failed)",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        onDismiss()
                                    }
                                )
                            } else {
                                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                            }
                        },
                        enabled = !isSending && pendingRents.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                            .testTag("confirm_send_batch_sms_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Send All SMS (1-Click)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

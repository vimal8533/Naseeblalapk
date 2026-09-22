package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.RentRecord
import com.example.model.Tenant
import com.example.model.TenantEchoRecord
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPaidBorder
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusPendingBorder
import com.example.ui.util.ReceiptPdfGenerator
import com.example.ui.util.ShareUtils
import java.text.NumberFormat
import java.util.Locale

/**
 * TenantEchoPortalDialog:
 * Complete Interactive Tenant Echo & Self-Service Portal.
 *
 * Implements all user requirements:
 * 1. Unique monthly link preview per tenant (e.g. naseeblalmarket.app/portal?t=...&m=...&y=...)
 * 2. Strict policy: NO online payment gateway. Contact authorized person only.
 * 3. Tenant Action: "Set Promise Date & Note" -> Auto-feeds into Firebase & App in real-time.
 * 4. Tenant Action: "I Have Paid" -> Tenant submits payment claim with reference note ->
 *    Screen immediately shows: "Wait for some time. Management will verify the payment, then you will get the payment receipt."
 * 5. Admin verification: When marked FULL PAID in app, receipt unlocks automatically on the link.
 *    If partial paid, NO receipt is shown.
 * 6. Quick WhatsApp share with pre-formatted message carrying this exact unique link.
 */
@Composable
fun TenantEchoPortalDialog(
    rent: RentRecord,
    tenant: Tenant?,
    echoRecord: TenantEchoRecord?,
    onDismiss: () -> Unit,
    onSubmitPromiseDate: (promisedDate: String, note: String) -> Unit,
    onSubmitClaimPaid: (referenceNote: String) -> Unit,
    onAdminVerifyPayment: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val inrFormat = remember { NumberFormat.getNumberInstance(Locale("en", "IN")) }

    // Unique link representation for this tenant & month
    val uniqueLink = remember(rent.id, rent.tenantId, rent.month, rent.year) {
        val slug = "${rent.tenantId}_${rent.month}_${rent.year}".replace(" ", "_")
        "https://naseeblalmarket.web.app/portal?echo=$slug"
    }

    var showPromiseInputSection by remember { mutableStateOf(false) }
    var showClaimPaidSection by remember { mutableStateOf(false) }

    var promiseDateInput by remember { mutableStateOf(rent.promisedDate.ifBlank { echoRecord?.promisedDate ?: "" }) }
    var promiseNoteInput by remember { mutableStateOf(rent.promisedNote.ifBlank { echoRecord?.promisedNote ?: "" }) }
    var claimNoteInput by remember { mutableStateOf(rent.tenantClaimedNote.ifBlank { echoRecord?.claimedPaidNote ?: "" }) }

    val hasClaimedPaid = rent.tenantClaimedPaid || (echoRecord?.claimedPaid == true)
    val isFullPaid = rent.isPaid
    val isPartial = rent.isPartial

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                .testTag("tenant_echo_portal_dialog"),
            color = Color(0xFF0F172A),
            shadowElevation = 20.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0B192C), Color(0xFF1E293B))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent.copy(alpha = 0.2f))
                                    .border(1.dp, GoldAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Tenant Echo Portal",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF2563EB))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "2-Way Feedback",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "${rent.tenantName} • Unit: ${rent.shopNumber} • ${rent.month} ${rent.year}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("close_echo_portal_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. UNIQUE TENANT MONTHLY LINK CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.OpenInNew,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tenant Unique Monthly Link",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF0F766E).copy(alpha = 0.2f))
                                        .border(0.5.dp, Color(0xFF2DD4BF), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Private Per-Tenant",
                                        color = Color(0xFF2DD4BF),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Link pill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = uniqueLink,
                                    color = Color(0xFF93C5FD),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 2
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Tenant Link", uniqueLink))
                                        Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Link", fontSize = 11.5.sp)
                                }

                                Button(
                                    onClick = {
                                        ShareUtils.sendWhatsAppReminder(context, rent, tenant?.phone)
                                    },
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Send on WhatsApp", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // 2. STRICT SECURITY NOTICE BANNER (NO ONLINE PAYMENT)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF451A03)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Strict Payment Policy (No Online Payment)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFFDE68A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Is application me koi online payment gateway integrate nahi hai. Sabhi bhugtan kewal authorized market management/collector ko physically ya direct verify hone ke baad hi mane jayenge.",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFFEF3C7).copy(alpha = 0.9f),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    // 3. CURRENT BILL & RENT SUMMARY
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "BILLING DETAILS (${rent.month} ${rent.year})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                val statusBadge = when {
                                    isFullPaid -> "FULL PAID ✓" to StatusPaid
                                    isPartial -> "PARTIALLY PAID" to Color(0xFFF59E0B)
                                    else -> "PENDING" to StatusPending
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statusBadge.second.copy(alpha = 0.2f))
                                        .border(0.5.dp, statusBadge.second, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = statusBadge.first,
                                        color = statusBadge.second,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Total Due", fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.6f))
                                    Text(
                                        text = "₹${inrFormat.format(rent.amountDue.toInt())}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Column {
                                    Text(text = "Amount Paid", fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.6f))
                                    Text(
                                        text = "₹${inrFormat.format(rent.amountPaid.toInt())}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusPaid
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Pending Balance", fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.6f))
                                    Text(
                                        text = "₹${inrFormat.format(rent.pendingAmount.toInt())}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (rent.pendingAmount > 0) Color(0xFFEF4444) else StatusPaid
                                    )
                                }
                            }
                        }
                    }

                    // 4. TENANT ACTIONS & STATUS LOGIC
                    // Condition A: If Tenant has already pressed "I Have Paid" and is waiting for verification
                    if (hasClaimedPaid && !isFullPaid) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF60A5FA))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.HourglassTop,
                                        contentDescription = null,
                                        tint = Color(0xFF93C5FD),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Under Verification by Management",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F172A))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "⏳ \"Wait for some time. Management will verify the payment, then you will get the payment receipt.\"",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFFDE68A),
                                            lineHeight = 17.sp
                                        )
                                        val note = rent.tenantClaimedNote.ifBlank { echoRecord?.claimedPaidNote ?: "" }
                                        if (note.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Tenant Note: $note",
                                                fontSize = 10.5.sp,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }

                                if (onAdminVerifyPayment != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = onAdminVerifyPayment,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Verified,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Admin: Verify & Mark Full Paid", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Condition B: If Full Paid, DIGITAL RECEIPT IS UNLOCKED!
                    if (isFullPaid) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Payment Verified & Receipt Unlocked",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Receipt #: ${rent.receiptNumber.ifBlank { "NLM-${rent.year}" }}",
                                                fontSize = 11.sp,
                                                color = Color(0xFFA7F3D0)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            ReceiptPdfGenerator.generateAndDownloadReceipt(context, rent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Download Receipt PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            ShareUtils.shareRentReceipt(context, rent)
                                        },
                                        modifier = Modifier.weight(0.9f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Share Receipt", fontSize = 11.5.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Condition C: If Partial or Pending, strictly NO RECEIPT banner
                    if (!isFullPaid) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF475569))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isPartial) "Receipt Policy: Partial payment par receipt generate nahi hoti. Pura baki jama hone par hi final receipt unlock hogi."
                                    else "Receipt Policy: Final official payment receipt kewal full payment complete aur verify hone par unlock hogi.",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    // 5. PROMISE DATE STATUS DISPLAY (IF SET BY TENANT)
                    val activePromisedDate = rent.promisedDate.ifBlank { echoRecord?.promisedDate ?: "" }
                    if (activePromisedDate.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1065)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFFC084FC),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tenant's Promised Payment Date",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFE9D5FF)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "📅 Expected Date: $activePromisedDate",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                val pNote = rent.promisedNote.ifBlank { echoRecord?.promisedNote ?: "" }
                                if (pNote.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Note: $pNote",
                                        fontSize = 11.sp,
                                        color = Color(0xFFE9D5FF).copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }

                    // 6. INTERACTIVE TENANT ACTIONS (Simulate / Record Tenant Response)
                    if (!isFullPaid) {
                        Text(
                            text = "TENANT ACTIONS (ECHO FEEDBACK)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 0.5.sp
                        )

                        // Action 1: Set / Update Promised Payment Date
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.CalendarToday,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (activePromisedDate.isNotBlank()) "Update Promise Date" else "Set Promise Date",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }

                                    TextButton(
                                        onClick = { showPromiseInputSection = !showPromiseInputSection }
                                    ) {
                                        Text(
                                            text = if (showPromiseInputSection) "Cancel" else "Enter Date",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = showPromiseInputSection) {
                                    Column(
                                        modifier = Modifier.padding(top = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = promiseDateInput,
                                            onValueChange = { promiseDateInput = it },
                                            label = { Text("Promised Date (e.g. 15 Oct 2026)") },
                                            placeholder = { Text("15 Oct 2026") },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("tenant_promise_date_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF38BDF8),
                                                unfocusedBorderColor = Color(0xFF475569),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )

                                        OutlinedTextField(
                                            value = promiseNoteInput,
                                            onValueChange = { promiseNoteInput = it },
                                            label = { Text("Note / Reason (Optional)") },
                                            placeholder = { Text("e.g. Salary aane ke baad payment hoga") },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("tenant_promise_note_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF38BDF8),
                                                unfocusedBorderColor = Color(0xFF475569),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )

                                        Button(
                                            onClick = {
                                                if (promiseDateInput.isNotBlank()) {
                                                    onSubmitPromiseDate(promiseDateInput.trim(), promiseNoteInput.trim())
                                                    showPromiseInputSection = false
                                                    Toast.makeText(context, "Promise date synced to all admins!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("submit_promise_date_btn"),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Submit Promise Date (Auto-feed to App)", fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        // Action 2: Tenant presses "I Have Paid"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Payments,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Claim \"I Have Paid\"",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }

                                    TextButton(
                                        onClick = { showClaimPaidSection = !showClaimPaidSection }
                                    ) {
                                        Text(
                                            text = if (showClaimPaidSection) "Cancel" else "Claim Paid",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = showClaimPaidSection) {
                                    Column(
                                        modifier = Modifier.padding(top = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = claimNoteInput,
                                            onValueChange = { claimNoteInput = it },
                                            label = { Text("Payment Details / UTR / Cash given to") },
                                            placeholder = { Text("e.g. Paid in cash to Sub-Admin on 5th") },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("tenant_claim_paid_note_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF10B981),
                                                unfocusedBorderColor = Color(0xFF475569),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )

                                        Button(
                                            onClick = {
                                                onSubmitClaimPaid(claimNoteInput.trim())
                                                showClaimPaidSection = false
                                                Toast.makeText(context, "Payment claim sent to management!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("submit_claim_paid_btn"),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Press \"I Have Paid\" & Notify Admins", fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)

                // Bottom Close button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close Portal", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

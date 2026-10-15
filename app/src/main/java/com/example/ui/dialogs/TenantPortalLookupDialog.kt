package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.RentRecord
import com.example.model.Tenant
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.util.ShareUtils

/**
 * Dedicated Hub for Tenant Echo Portals.
 * Provides shop-wise and flat-wise dedicated links with Open, Copy, and WhatsApp Share actions.
 */
@Composable
fun TenantPortalLookupDialog(
    rents: List<RentRecord>,
    tenants: List<Tenant> = emptyList(),
    selectedMonth: String = "",
    selectedYear: Int = 2026,
    onDismiss: () -> Unit,
    onSelectRent: (RentRecord) -> Unit
) {
    val context = LocalContext.current
    var searchInput by remember { mutableStateOf("") }

    // Aggregate unique shop units with their tenant & rent data
    val unitItems = remember(rents, tenants, selectedMonth, selectedYear) {
        val list = mutableListOf<RentRecord>()
        val seenTenantIds = mutableSetOf<String>()

        // 1. From rents
        rents.forEach { r ->
            if (r.tenantId.isNotBlank() && seenTenantIds.add(r.tenantId)) {
                list.add(r)
            }
        }

        // 2. Add tenants that might not have a rent record in this specific month filter
        tenants.forEach { t ->
            if (t.id.isNotBlank() && seenTenantIds.add(t.id)) {
                list.add(
                    RentRecord(
                        id = "rent_${selectedYear}_${selectedMonth}_${t.id}",
                        tenantId = t.id,
                        shopNumber = t.shopNumber,
                        tenantName = t.name,
                        month = selectedMonth.ifBlank { "Current" },
                        year = selectedYear,
                        amountDue = t.monthlyRent,
                        isPersonal = t.isPersonal
                    )
                )
            }
        }

        list.sortedWith(
            compareBy(
                { !it.shopNumber.any { ch -> ch.isDigit() } },
                { it.shopNumber.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 999 },
                { it.shopNumber }
            )
        )
    }

    val filteredUnits = remember(unitItems, searchInput) {
        if (searchInput.isBlank()) unitItems
        else {
            val q = searchInput.trim().lowercase()
            unitItems.filter { r ->
                r.shopNumber.lowercase().contains(q) ||
                r.tenantName.lowercase().contains(q) ||
                (r.shopNumber.filter { it.isDigit() }.isNotEmpty() && r.shopNumber.filter { it.isDigit() } == q.filter { it.isDigit() })
            }
        }
    }

    // Quick chips from top units
    val topChips = remember(unitItems) {
        unitItems.take(8)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 16.dp)
                .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1324)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GoldAccent.copy(alpha = 0.15f))
                                .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tenant Echo Dedicated Links",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "दुकान अनुसार डिजिटल पोर्टल लिंक Hub",
                                color = GoldAccent,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Shop No. ya Kirayedaar ka naam search karein...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = GoldAccent
                        )
                    },
                    trailingIcon = {
                        if (searchInput.isNotEmpty()) {
                            IconButton(onClick = { searchInput = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.6f))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Color(0xFF1E2D4A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = GoldAccent,
                        focusedContainerColor = Color(0xFF070C18),
                        unfocusedContainerColor = Color(0xFF070C18).copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                if (topChips.isNotEmpty() && searchInput.isBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(topChips) { u ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF16233B))
                                    .border(1.dp, Color(0xFF263B60), RoundedCornerShape(8.dp))
                                    .clickable { searchInput = u.shopNumber }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${if (u.isPersonal) "Flat" else "Shop"} ${u.shopNumber}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Units List Header
                Text(
                    text = "Total ${filteredUnits.size} Units / Dedicated Links:",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredUnits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Koi unit nahi mila '$searchInput' ke liye",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredUnits, key = { "${it.tenantId}_${it.shopNumber}" }) { rentItem ->
                            val tenantObj = tenants.find { it.id == rentItem.tenantId }
                            val directUrl = ShareUtils.generateTenantEchoPortalUrl(rentItem)
                            val unitTitle = "${if (rentItem.isPersonal) "Flat" else "Shop"} ${rentItem.shopNumber.ifBlank { "Unassigned" }}"
                            val tenantTitle = rentItem.tenantName.ifBlank { tenantObj?.name ?: "Kirayedaar" }
                            val phone = tenantObj?.phone?.trim() ?: ""

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF131D31),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D4A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Top Row: Shop & Tenant Name
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(GoldAccent.copy(alpha = 0.15f))
                                                    .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = unitTitle,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GoldAccent
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = tenantTitle,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.5.sp,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (phone.isNotBlank()) {
                                            Text(
                                                text = "📞 $phone",
                                                fontSize = 10.5.sp,
                                                color = Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Portal URL snippet box
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF070C18))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = directUrl,
                                            fontSize = 10.sp,
                                            color = Color(0xFF93C5FD),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action Buttons Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // 1. Open Portal Button
                                        Button(
                                            onClick = {
                                                onSelectRent(rentItem)
                                            },
                                            modifier = Modifier.weight(1.2f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = GoldAccent,
                                                contentColor = NavyDark
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Open Portal",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }

                                        // 2. Copy Link Button
                                        OutlinedButton(
                                            onClick = {
                                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                cm?.setPrimaryClip(ClipData.newPlainText("Portal Link", directUrl))
                                                Toast.makeText(context, "$unitTitle ka link copy ho gaya! ✓", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.5f)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF93C5FD)),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }

                                        // 3. WhatsApp Share Button
                                        Button(
                                            onClick = {
                                                val waText = "Namaste $tenantTitle ji, aapki $unitTitle ka Naseeb Lal Market portal link:\n$directUrl\n\nIs link se aap apna hisaab, bill aur payment status dekh sakte hain."
                                                val cleanPhone = phone.filter { it.isDigit() }.let { raw ->
                                                    when {
                                                        raw.length == 10 -> "91$raw"
                                                        raw.startsWith("0") && raw.length == 11 -> "91${raw.substring(1)}"
                                                        raw.startsWith("91") && raw.length == 12 -> raw
                                                        else -> raw
                                                    }
                                                }
                                                try {
                                                    if (cleanPhone.isNotBlank()) {
                                                        val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(waText)}"
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        })
                                                    } else {
                                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                            type = "text/plain"
                                                            putExtra(Intent.EXTRA_TEXT, waText)
                                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        }
                                                        context.startActivity(Intent.createChooser(sendIntent, "Share Portal Link"))
                                                    }
                                                } catch (_: Exception) {
                                                    Toast.makeText(context, "WhatsApp nahi khul saka", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Send,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

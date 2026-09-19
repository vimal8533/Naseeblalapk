package com.example.ui.components

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
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Shop

@Composable
fun AddShopDialog(
    initialShop: Shop? = null,
    canManagePersonalTenants: Boolean = false,
    initialWorkspaceMode: com.example.model.AppWorkspaceMode = com.example.model.AppWorkspaceMode.PUBLIC_MARKET,
    onDismiss: () -> Unit,
    onSave: (
        shopNumber: String,
        floor: String,
        sizeSqFt: String,
        baseRent: Double,
        maintenanceCharge: Double,
        electricityMeter: String,
        notes: String,
        isPersonal: Boolean
    ) -> Unit
) {
    var isPersonal by remember {
        mutableStateOf(
            initialShop?.isPersonal ?: (initialWorkspaceMode == com.example.model.AppWorkspaceMode.PRIVATE_PERSONAL)
        )
    }
    var isFlatType by remember {
        mutableStateOf(
            initialShop?.propertyType?.equals("FLAT", ignoreCase = true) == true ||
            initialShop?.shopNumber?.startsWith("Flat", ignoreCase = true) == true ||
            (initialShop == null && initialWorkspaceMode == com.example.model.AppWorkspaceMode.PRIVATE_PERSONAL)
        )
    }
    var shopNumber by remember { mutableStateOf(initialShop?.shopNumber ?: "") }
    var floor by remember { mutableStateOf(initialShop?.floor ?: "Ground Floor") }
    var sizeSqFt by remember { mutableStateOf(initialShop?.sizeSqFt ?: "200 sq.ft") }
    var baseRent by remember { mutableStateOf(if (initialShop != null && initialShop.baseRent > 0) initialShop.baseRent.toInt().toString() else "") }
    var maintenanceCharge by remember { mutableStateOf(if (initialShop != null && initialShop.maintenanceCharge > 0) initialShop.maintenanceCharge.toInt().toString() else "500") }
    var electricityMeter by remember { mutableStateOf(initialShop?.electricityMeter ?: "") }
    var notes by remember { mutableStateOf(initialShop?.notes ?: "") }

    var hasError by remember { mutableStateOf(false) }

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
                        text = if (initialShop == null) {
                            if (isFlatType) "Add New Flat" else "Add New Shop"
                        } else {
                            if (isFlatType) "Edit Flat Details" else "Edit Shop Details"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Workspace Indicator Banner (Automatic based on active mode)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPersonal) Color(0xFF0F766E).copy(alpha = 0.08f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isPersonal) Color(0xFF0F766E).copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isPersonal) Color(0xFF0F766E) else MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPersonal) Icons.Filled.Apartment else Icons.Filled.Store,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isPersonal) "Personal Workspace" else "Naseeb Lal Market",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isPersonal) Color(0xFF0F766E) else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (isPersonal) "Will be saved under Personal Property" else "Will be saved under Naseeb Lal Market",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Allow workspace switch if editing or authorized admin
                        if (canManagePersonalTenants || initialShop != null) {
                            FilterChip(
                                selected = isPersonal,
                                onClick = { isPersonal = !isPersonal },
                                label = {
                                    Text(
                                        text = if (isPersonal) "Switch to Market" else "Switch to Personal",
                                        fontSize = 10.sp
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Unit Type Selector (Shop vs Flat) - Does NOT alter workspace!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 🏬 Commercial Shop Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                isFlatType = false
                                hasError = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!isFlatType) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (!isFlatType) 2.dp else 1.dp,
                            color = if (!isFlatType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
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
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (!isFlatType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Store,
                                    contentDescription = null,
                                    tint = if (!isFlatType) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Shop (Dukan)",
                                fontSize = 11.5.sp,
                                fontWeight = if (!isFlatType) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isFlatType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // 🏢 Flat / Unit Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                isFlatType = true
                                hasError = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFlatType) Color(0xFF0F766E).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isFlatType) 2.dp else 1.dp,
                            color = if (isFlatType) Color(0xFF0F766E) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
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
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isFlatType) Color(0xFF0F766E) else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Apartment,
                                    contentDescription = null,
                                    tint = if (isFlatType) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Flat (Apartment)",
                                fontSize = 11.5.sp,
                                fontWeight = if (isFlatType) FontWeight.Bold else FontWeight.Medium,
                                color = if (isFlatType) Color(0xFF0F766E) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = shopNumber,
                    onValueChange = {
                        shopNumber = it
                        hasError = false
                    },
                    label = { Text(if (isFlatType) "Flat / Unit Number *" else "Shop Number *") },
                    placeholder = { Text(if (isFlatType) "e.g. Flat 101, Flat 2B" else "e.g. Shop 105, G-12") },
                    leadingIcon = { Icon(if (isFlatType) Icons.Filled.Apartment else Icons.Filled.Store, contentDescription = null) },
                    isError = hasError && shopNumber.isBlank(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shop_number_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = floor,
                        onValueChange = { floor = it },
                        label = { Text("Floor") },
                        placeholder = { Text("Ground, 1st...") },
                        leadingIcon = { Icon(Icons.Filled.Layers, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("shop_floor_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = sizeSqFt,
                        onValueChange = { sizeSqFt = it },
                        label = { Text("Size") },
                        placeholder = { Text("250 sq.ft") },
                        leadingIcon = { Icon(Icons.Filled.SquareFoot, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("shop_size_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = baseRent,
                        onValueChange = {
                            baseRent = it
                            hasError = false
                        },
                        label = { Text("Base Rent (₹) *") },
                        placeholder = { Text("7000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = hasError && (baseRent.toDoubleOrNull() ?: 0.0) <= 0.0,
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("shop_rent_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = maintenanceCharge,
                        onValueChange = { maintenanceCharge = it },
                        label = { Text("Maint. (₹)") },
                        placeholder = { Text("500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("shop_maint_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = electricityMeter,
                    onValueChange = { electricityMeter = it },
                    label = { Text("Electricity Meter # (Optional)") },
                    placeholder = { Text("e.g. NLM-MTR-105") },
                    leadingIcon = { Icon(Icons.Filled.ElectricMeter, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shop_meter_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Remarks / Notes (Optional)") },
                    placeholder = { Text("e.g. Corner shop near stairs") },
                    leadingIcon = { Icon(Icons.Filled.Notes, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shop_notes_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val rentDouble = baseRent.toDoubleOrNull() ?: 0.0
                        if (shopNumber.isBlank() || rentDouble <= 0.0) {
                            hasError = true
                        } else {
                            onSave(
                                shopNumber.trim(),
                                floor.trim(),
                                sizeSqFt.trim(),
                                rentDouble,
                                maintenanceCharge.toDoubleOrNull() ?: 0.0,
                                electricityMeter.trim(),
                                notes.trim(),
                                isPersonal
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_shop_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPersonal) Color(0xFF0F766E) else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (initialShop == null) {
                            if (isFlatType) "SAVE FLAT" else "SAVE SHOP"
                        } else {
                            if (isFlatType) "UPDATE FLAT" else "UPDATE SHOP"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

package com.example.model

data class Shop(
    val id: String = "",
    val shopNumber: String = "",
    val floor: String = "Ground Floor",
    val sizeSqFt: String = "200 sq.ft",
    val baseRent: Double = 0.0,
    val maintenanceCharge: Double = 0.0,
    val tenantId: String? = null,
    val tenantName: String? = null,
    val status: String = "VACANT", // OCCUPIED, VACANT, MAINTENANCE
    val electricityMeter: String = "",
    val notes: String = "",
    val incrementYears: Int = 1, // Default: 1 year
    val incrementPercent: Double = 5.0, // Default: 5%
    val isPersonal: Boolean = false,
    val propertyType: String = "SHOP", // "SHOP", "FLAT"
    val ownerSubAdminId: String = "",
    val lastModifiedBy: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalMonthlyRent: Double get() = baseRent + maintenanceCharge
    val isOccupied: Boolean get() = status.equals("OCCUPIED", ignoreCase = true)

    val isFlat: Boolean
        get() = isPersonal ||
                propertyType.equals("FLAT", ignoreCase = true) ||
                shopNumber.startsWith("Flat", ignoreCase = true) ||
                shopNumber.startsWith("Unit", ignoreCase = true) ||
                shopNumber.startsWith("Room", ignoreCase = true)

    // Calculate next projected rent after increment
    val nextIncrementRent: Double
        get() = baseRent * (1.0 + (incrementPercent / 100.0))

    val incrementRuleText: String
        get() = "${if (incrementPercent % 1.0 == 0.0) incrementPercent.toInt().toString() else incrementPercent.toString()}% every $incrementYears yr${if (incrementYears > 1) "s" else ""}"
}

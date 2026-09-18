package com.example.model

import com.example.util.PmcTaxHelper

data class Tenant(
    val id: String = "",
    val name: String = "",
    val businessName: String = "",
    val phone: String = "",
    val shopId: String = "",
    val shopNumber: String = "",
    val assignedShopIds: List<String> = emptyList(),
    val numberOfShops: Int = 1,
    val advanceDeposit: Double = 0.0,
    val monthlyRent: Double = 0.0,
    val previousDues: Double = 0.0, // Opening balance / dues prior to app
    val incrementYears: Int = 1, // Default: 1 year
    val incrementPercent: Double = 5.0, // Default: 5%
    val billingCycle: String = "MONTHLY", // MONTHLY, QUARTERLY, HALF_YEARLY, YEARLY
    val joiningDate: String = "",
    val idProof: String = "",
    val isActive: Boolean = true,
    val notes: String = "",
    val electricityBill: Double = 0.0, // Monthly electricity bill in rupees for personal flat/unit
    val lastMeterReading: Double = 0.0, // Last recorded electricity meter reading
    val electricityRatePerUnit: Double = 10.0, // Default electricity charge per unit in rupees (e.g. ₹10)
    val isPersonal: Boolean = false,
    val ownerSubAdminId: String = "",
    val lastModifiedBy: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val shopIdList: List<String>
        get() = when {
            assignedShopIds.isNotEmpty() -> assignedShopIds
            shopId.isNotBlank() -> shopId.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            else -> emptyList()
        }

    val shopCount: Int
        get() = numberOfShops.coerceAtLeast(shopIdList.size.coerceAtLeast(if (shopNumber.isNotBlank()) 1 else 0))

    // PMC Tax rule: Annual fee of ₹1,000 per shop, applied ONCE a year upon 1-year anniversary
    val hasCompletedOneYear: Boolean
        get() = PmcTaxHelper.hasCompletedOneYear(joiningDate)

    val annualPmcTaxAmount: Double
        get() = PmcTaxHelper.getPmcTaxForShops(shopCount)

    // Backward-compatible accessor for PMC tax amount
    val pmcTaxAmount: Double
        get() = annualPmcTaxAmount

    // Pure monthly rent (PMC tax is NOT added to monthly rent each month; billed once a year)
    val effectiveMonthlyRent: Double
        get() = monthlyRent

    // Rent Increment Policy calculations
    val nextIncrementRent: Double
        get() = monthlyRent * (1.0 + (incrementPercent / 100.0))

    val incrementRuleText: String
        get() = "${if (incrementPercent % 1.0 == 0.0) incrementPercent.toInt().toString() else incrementPercent.toString()}% every $incrementYears yr${if (incrementYears > 1) "s" else ""}"

    val cycleMonths: Int
        get() = when (billingCycle.uppercase()) {
            "QUARTERLY", "3_MONTHS", "3 MONTHS" -> 3
            "HALF_YEARLY", "6_MONTHS", "6 MONTHS" -> 6
            "YEARLY", "12_MONTHS", "12 MONTHS", "ANNUAL" -> 12
            else -> 1
        }

    val billingCycleDisplay: String
        get() = when (cycleMonths) {
            3 -> "3 Months"
            6 -> "6 Months"
            12 -> "Yearly"
            else -> "Monthly"
        }
}

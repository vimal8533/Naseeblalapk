package com.example.model

data class RentRecord(
    val id: String = "",
    val shopId: String = "",
    val shopNumber: String = "",
    val tenantId: String = "",
    val tenantName: String = "",
    val month: String = "", // e.g. "September"
    val year: Int = 2026,
    val amountDue: Double = 0.0,
    val amountPaid: Double = 0.0,
    val pmcTax: Double = 0.0,
    val electricityBill: Double = 0.0, // Monthly electricity bill in rupees for personal flat
    val prevMeterReading: Double = 0.0, // Previous electricity meter reading (units)
    val currentMeterReading: Double = 0.0, // Current electricity meter reading (units)
    val electricityRatePerUnit: Double = 0.0, // Electricity charge per unit in rupees
    val status: String = "PENDING", // PAID, PARTIAL, PENDING
    val dueDate: String = "10th",
    val paidDate: String = "",
    val paymentMode: String = "CASH", // CASH, UPI / ONLINE, CHEQUE, BANK_TRANSFER
    val collectedBy: String = "",
    val receiptNumber: String = "",
    val notes: String = "",
    val isPersonal: Boolean = false,
    val ownerSubAdminId: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val pendingAmount: Double
        get() = (amountDue - amountPaid).coerceAtLeast(0.0)

    val isPaid: Boolean
        get() = status.equals("PAID", ignoreCase = true) || (amountPaid >= amountDue && amountDue > 0.0)

    val isPartial: Boolean
        get() = status.equals("PARTIAL", ignoreCase = true) || (amountPaid > 0.0 && amountPaid < amountDue)

    val isPending: Boolean
        get() = !isPaid && !isPartial

    val hasPmcTax: Boolean
        get() = pmcTax > 0.0

    val hasElectricityBill: Boolean
        get() = electricityBill > 0.0

    val unitsConsumed: Double
        get() = (currentMeterReading - prevMeterReading).coerceAtLeast(0.0)

    val hasMeterReading: Boolean
        get() = currentMeterReading > 0.0
}

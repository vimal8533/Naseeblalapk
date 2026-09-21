package com.example.model

data class PmcTaxClearanceRecord(
    val financialYear: String = "", // e.g. "2026-2027"
    val isPaid: Boolean = false,
    val amountPaid: Double = 0.0,
    val receiptNumber: String = "",
    val paidDate: String = "",
    val paidBy: String = "",
    val paidByRole: String = "ADMIN", // "ADMIN" or "SUB_ADMIN"
    val paymentMode: String = "CASH", // CASH, ONLINE, CHEQUE
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

package com.example.model

data class MonthlyReminderRecord(
    val id: String = "", // e.g. "2026_October"
    val month: String = "",
    val year: Int = 2026,
    val sentBy: String = "",
    val senderPhone: String = "",
    val sentAt: Long = 0L,
    val recipientsCount: Int = 0,
    val isSent: Boolean = true
)

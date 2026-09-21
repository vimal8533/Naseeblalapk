package com.example.model

data class MarketNotice(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val category: String = "TAX", // TAX, ELECTRICITY, RENT, MAINTENANCE, GENERAL
    val priority: String = "NORMAL", // NORMAL, IMPORTANT, URGENT
    val authorName: String = "",
    val authorRole: String = "ADMIN",
    val authorDeviceId: String = "",
    val dueDate: String = "", // e.g. "31 March 2027" or "10th October"
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val targetAudience: String = "ALL" // ALL, SUB_ADMINS, TENANTS
) {
    val categoryDisplay: String
        get() = when (category.uppercase()) {
            "TAX", "PMC", "PMC_TAX" -> "🏛️ Tax / PMC"
            "ELECTRICITY", "POWER" -> "⚡ Bijli / Meter"
            "RENT", "RENT_REMINDER" -> "💰 Kiraya Notice"
            "MAINTENANCE" -> "🛠️ Maintenance"
            "URGENT" -> "🚨 Urgent Alert"
            else -> "📢 Aam Suchna"
        }

    val isUrgent: Boolean
        get() = priority.equals("URGENT", ignoreCase = true) || category.equals("URGENT", ignoreCase = true)

    val isTaxNotice: Boolean
        get() = category.uppercase() in listOf("TAX", "PMC", "PMC_TAX")
}

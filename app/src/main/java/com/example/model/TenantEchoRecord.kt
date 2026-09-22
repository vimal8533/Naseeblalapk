package com.example.model

/**
 * Represents a monthly interactive status record generated for each tenant.
 * Stored in Firebase at: /tenant_echo/{month_year}/{tenantId}
 * e.g. /tenant_echo/October_2026/tenant_123
 */
data class TenantEchoRecord(
    val id: String = "", // e.g. "October_2026_tenant_123"
    val tenantId: String = "",
    val tenantName: String = "",
    val tenantPhone: String = "",
    val shopNumber: String = "",
    val month: String = "",
    val year: Int = 2026,
    val amountDue: Double = 0.0,
    val amountPaid: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val isPersonal: Boolean = false,
    val isFullPaid: Boolean = false,
    // Tenant interaction fields:
    val promisedDate: String = "", // e.g. "12/10/2026"
    val promisedNote: String = "", // e.g. "Salary aane par denge"
    val promisedAt: Long = 0L,
    val claimedPaid: Boolean = false, // Tenant clicked "I Have Paid"
    val claimedPaidNote: String = "", // Reference/UTR or details
    val claimedPaidAt: Long = 0L,
    val isVerifiedByAdmin: Boolean = false,
    val verifiedBy: String = "",
    val receiptNumber: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val portalSlug: String
        get() = "${tenantId}_${month}_${year}".replace(" ", "_")
}

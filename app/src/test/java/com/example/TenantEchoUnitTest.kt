package com.example

import com.example.model.RentRecord
import com.example.model.TenantEchoRecord
import com.example.ui.util.ShareUtils
import org.junit.Assert.*
import org.junit.Test

class TenantEchoUnitTest {

    @Test
    fun testEchoPortalUniqueLinkGeneration() {
        val rent = RentRecord(
            id = "September_2026_t123",
            tenantId = "t123",
            tenantName = "Ramesh Kumar",
            shopNumber = "12",
            month = "September",
            year = 2026,
            amountDue = 4500.0,
            amountPaid = 0.0,
            status = "PENDING"
        )

        val uniqueLink = ShareUtils.generateTenantEchoPortalUrl(rent)
        assertNotNull(uniqueLink)
        assertTrue("URL must contain portal slug", uniqueLink.contains("portal?echo="))
        assertTrue("URL must contain tenantId", uniqueLink.contains("t123"))
        assertTrue("URL must contain month and year", uniqueLink.contains("September_2026"))
    }

    @Test
    fun testPromiseDateEchoRecordCreation() {
        val echoRecord = TenantEchoRecord(
            id = "September_2026_t123",
            tenantId = "t123",
            tenantName = "Ramesh Kumar",
            month = "September",
            year = 2026,
            amountDue = 4500.0,
            promisedDate = "25 Sep 2026",
            promisedNote = "Salary aane par cash dunga",
            promisedAt = 1727000000000L
        )

        assertEquals("25 Sep 2026", echoRecord.promisedDate)
        assertEquals("Salary aane par cash dunga", echoRecord.promisedNote)
        assertFalse("Should not be claimed paid initially", echoRecord.claimedPaid)
        assertFalse("Should not be verified paid initially", echoRecord.isVerifiedByAdmin)
    }

    @Test
    fun testTenantClaimPaidAndAdminVerification() {
        var echoRecord = TenantEchoRecord(
            id = "September_2026_t123",
            tenantId = "t123",
            tenantName = "Ramesh Kumar",
            month = "September",
            year = 2026,
            amountDue = 4500.0,
            promisedDate = "25 Sep 2026",
            claimedPaid = true,
            claimedPaidNote = "Cash given to Munna Ji on shop",
            claimedPaidAt = 1727005000000L
        )

        assertTrue(echoRecord.claimedPaid)
        assertEquals("Cash given to Munna Ji on shop", echoRecord.claimedPaidNote)
        assertFalse(echoRecord.isVerifiedByAdmin)

        // Admin verifies the payment
        echoRecord = echoRecord.copy(
            isVerifiedByAdmin = true,
            verifiedBy = "Super Admin"
        )

        assertTrue(echoRecord.isVerifiedByAdmin)
        assertEquals("Super Admin", echoRecord.verifiedBy)
    }

    @Test
    fun testShareMessageIncludesPortalLinkAndNoOnlinePayment() {
        val rent = RentRecord(
            id = "September_2026_t123",
            tenantId = "t123",
            tenantName = "Ramesh Kumar",
            shopNumber = "12",
            month = "September",
            year = 2026,
            amountDue = 4500.0,
            amountPaid = 0.0,
            status = "PENDING"
        )

        val message = ShareUtils.formatRentReminderMessage(rent)
        assertTrue("Message should include Tenant Echo link", message.contains("naseeblalmarket.web.app/portal?echo="))
        assertFalse("Strictly manual payment: no online gateway links allowed", message.contains("razorpay") || message.contains("stripe"))
    }
}

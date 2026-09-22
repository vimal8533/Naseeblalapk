package com.example

import com.example.model.RentRecord
import com.example.model.Tenant
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
        assertTrue("URL must contain market tenant echo prefix", uniqueLink.contains("tenant_echo") || uniqueLink.contains("portal"))
        assertTrue("URL must contain rentId", uniqueLink.contains("September_2026_t123"))
        assertTrue("URL must contain tenantId", uniqueLink.contains("t123"))
    }

    @Test
    fun testPromiseDateEchoRecordCreation() {
        val echoRecord = TenantEchoRecord(
            rentId = "September_2026_t123",
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
        assertFalse("Should not be claimed paid initially", echoRecord.hasTenantClaimedPaid)
        assertFalse("Should not be verified paid initially", echoRecord.isVerifiedPaid)
    }

    @Test
    fun testTenantClaimPaidAndAdminVerification() {
        var echoRecord = TenantEchoRecord(
            rentId = "September_2026_t123",
            tenantId = "t123",
            tenantName = "Ramesh Kumar",
            month = "September",
            year = 2026,
            amountDue = 4500.0,
            promisedDate = "25 Sep 2026",
            hasTenantClaimedPaid = true,
            claimedPaidNote = "Cash given to Munna Ji on shop",
            claimedPaidAt = 1727005000000L
        )

        assertTrue(echoRecord.hasTenantClaimedPaid)
        assertEquals("Cash given to Munna Ji on shop", echoRecord.claimedPaidNote)
        assertFalse(echoRecord.isVerifiedPaid)

        // Admin verifies the payment
        echoRecord = echoRecord.copy(
            isVerifiedPaid = true,
            verifiedAt = 1727010000000L,
            verifiedBy = "Super Admin"
        )

        assertTrue(echoRecord.isVerifiedPaid)
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
        assertTrue("Message should include Tenant Echo link", message.contains("naseeb-lal-market.web.app/portal/echo") || message.contains("tenant_echo"))
        assertFalse("Strictly manual payment: no online gateway links allowed", message.contains("razorpay") || message.contains("stripe"))
    }
}

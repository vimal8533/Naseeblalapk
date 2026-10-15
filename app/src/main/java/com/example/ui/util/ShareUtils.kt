package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.RentRecord
import com.example.model.Tenant

object ShareUtils {

    fun generateTenantEchoPortalUrl(rent: RentRecord): String {
        val echoSlug = "${rent.tenantId}_${rent.month}_${rent.year}".replace(" ", "_")
        return "https://naseeblalmarket.web.app/portal?echo=$echoSlug"
    }

    /**
     * Checks if a tenant's promised payment date is active (today or in future).
     * If date has already passed, returns false (overdue).
     */
    fun isPromiseDateActive(dateStr: String): Boolean {
        if (dateStr.isBlank()) return false
        val clean = dateStr.trim()
        val formats = listOf(
            "dd MMM yyyy",
            "d MMM yyyy",
            "dd MMMM yyyy",
            "d MMMM yyyy",
            "dd/MM/yyyy",
            "d/M/yyyy",
            "dd-MM-yyyy",
            "d-M-yyyy",
            "yyyy-MM-dd"
        )
        val todayCal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        for (fmt in formats) {
            try {
                val sdf = java.text.SimpleDateFormat(fmt, java.util.Locale.ENGLISH)
                sdf.isLenient = false
                val parsed = sdf.parse(clean)
                if (parsed != null) {
                    return parsed.time >= todayCal.timeInMillis
                }
            } catch (_: Exception) {}
        }
        return true
    }

    fun formatRentReminderMessage(rent: RentRecord, tenant: Tenant? = null): String {
        val portalLink = generateTenantEchoPortalUrl(rent)
        val unitLabel = if (rent.isPersonal) "Flat No. ${rent.shopNumber}" else "Shop No. ${rent.shopNumber}"
        val periodLabel = if (rent.month.contains("yearly", ignoreCase = true) || rent.month.contains("varshik", ignoreCase = true)) {
            "Varshik (Yearly)"
        } else {
            "${rent.month} ${rent.year}"
        }
        val formattedPending = formatAmount(rent.pendingAmount)
        val priorArrears = tenant?.previousDues?.coerceAtLeast(0.0) ?: 0.0
        val baseRent = (rent.amountDue - rent.electricityBill - rent.pmcTax).coerceAtLeast(0.0)

        return buildString {
            if (rent.isPartial || rent.amountPaid > 0.0) {
                // Detailed breakdown for PARTIAL Dues:
                appendLine("Namaste ${rent.tenantName} ji,")
                appendLine("Aapki $unitLabel ka $periodLabel hisaab me aashik bhugtan (Partial Payment) darj hua hai:")
                appendLine()
                appendLine("📋 Mad-Vaar Dues Vivran (Kis Chiz Ka Baki Hai):")
                if (baseRent > 0.0) {
                    appendLine("• Dukaan/Flat Kiraya: Rs ${formatAmount(baseRent)}")
                }
                if (rent.electricityBill > 0.0) {
                    val unitStr = if (rent.unitsConsumed > 0.0) " (${formatAmount(rent.unitsConsumed)} units)" else ""
                    appendLine("• Bijli Bill$unitStr: Rs ${formatAmount(rent.electricityBill)}")
                }
                if (rent.pmcTax > 0.0) {
                    appendLine("• PMC Property Tax: Rs ${formatAmount(rent.pmcTax)}")
                }
                if (priorArrears > 0.0) {
                    appendLine("• Pichhla Purana Baki (Prior Arrears): Rs ${formatAmount(priorArrears)}")
                }
                appendLine("---------------------------")
                val totalWithArrears = rent.amountDue + priorArrears
                appendLine("Kul Rakam (Total Bill): Rs ${formatAmount(totalWithArrears)}")
                appendLine("Aapke dwara Jama (Paid): Rs ${formatAmount(rent.amountPaid)}")
                val netRemaining = rent.pendingAmount + priorArrears
                appendLine("👉 Shesh Dues (Remaining Balance): Rs ${formatAmount(netRemaining)}")
                if (rent.notes.isNotBlank()) {
                    appendLine("ℹ️ Note: ${rent.notes}")
                }
                appendLine()
                appendLine("Kripya shesh baki rashi samay par jama karein.")
            } else {
                val hasMultipleItems = rent.electricityBill > 0.0 || rent.pmcTax > 0.0 || priorArrears > 0.0
                if (hasMultipleItems) {
                    appendLine("Namaste ${rent.tenantName} ji,")
                    appendLine("Aapki $unitLabel ka $periodLabel hisaab due hai:")
                    appendLine()
                    appendLine("📋 Dues Vivran (Kis Chiz Ka Baki Hai):")
                    if (baseRent > 0.0) {
                        appendLine("• Dukaan/Flat Kiraya: Rs ${formatAmount(baseRent)}")
                    }
                    if (rent.electricityBill > 0.0) {
                        val unitStr = if (rent.unitsConsumed > 0.0) " (${formatAmount(rent.unitsConsumed)} units)" else ""
                        appendLine("• Bijli Bill$unitStr: Rs ${formatAmount(rent.electricityBill)}")
                    }
                    if (rent.pmcTax > 0.0) {
                        appendLine("• PMC Property Tax: Rs ${formatAmount(rent.pmcTax)}")
                    }
                    if (priorArrears > 0.0) {
                        appendLine("• Pichhla Purana Baki: Rs ${formatAmount(priorArrears)}")
                    }
                    appendLine("---------------------------")
                    val netTotal = rent.pendingAmount + priorArrears
                    appendLine("👉 Kul Dues (Total Amount Due): Rs ${formatAmount(netTotal)}")
                    if (rent.notes.isNotBlank()) {
                        appendLine("ℹ️ Note: ${rent.notes}")
                    }
                    appendLine()
                    appendLine("Kripya samay par jama karein.")
                } else {
                    appendLine("Namaste ${rent.tenantName} ji, aapki $unitLabel ka $periodLabel kiraya Rs $formattedPending due hai. Kripya samay par jama karein.")
                }
            }
            appendLine()
            appendLine("Shukriya - Naseeb Lal Market (Sampark: Vimal Kumar, Mob: 7654138539)")
            appendLine()
            appendLine("🔗 Apna Payment Status & Portal Link:")
            appendLine(portalLink)
            appendLine()
            appendLine("(Upar link par click karke aap payment ki expected date de sakte hain, ya payment jama kar diya ho toh verify karwa ke receipt le sakte hain)")
        }.trim()
    }

    fun formatArrearsOnlyReminderMessage(tenant: Tenant): String {
        val priorAmount = formatAmount(tenant.previousDues)
        val unitLabel = if (tenant.isPersonal) "Flat No. ${tenant.shopNumber}" else "Shop No. ${tenant.shopNumber}"
        return buildString {
            appendLine("Namaste ${tenant.name} ji,")
            appendLine("Aapki $unitLabel ka pichhla purana baki (Prior Arrears) Rs $priorAmount due hai.")
            if (tenant.notes.isNotBlank()) {
                appendLine("ℹ️ Note: ${tenant.notes}")
            }
            appendLine()
            appendLine("Kripya ye baki rashi samay par jama karein.")
            appendLine()
            appendLine("Shukriya - Naseeb Lal Market (Sampark: Vimal Kumar, Mob: 7654138539)")
        }.trim()
    }

    /**
     * Sends a polite and clear WhatsApp rent payment reminder to the tenant.
     * Targets tenant's phone number directly if available, otherwise opens chooser.
     */
    fun sendWhatsAppReminder(context: Context, rent: RentRecord, tenantPhone: String? = null, tenant: Tenant? = null) {
        val reminderText = formatRentReminderMessage(rent, tenant)

        val cleanPhone = tenantPhone?.filter { it.isDigit() }?.let { raw ->
            when {
                raw.length == 10 -> "91$raw"
                raw.startsWith("0") && raw.length == 11 -> "91${raw.substring(1)}"
                raw.startsWith("91") && raw.length == 12 -> raw
                else -> raw
            }
        }

        try {
            if (!cleanPhone.isNullOrBlank()) {
                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${safeUrlEncode(reminderText)}"
                val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(waIntent)
            } else {
                openGenericShare(context, reminderText, "Send Rent Reminder (WhatsApp / SMS)")
            }
        } catch (e: Exception) {
            openGenericShare(context, reminderText, "Send Rent Reminder (WhatsApp / SMS)")
        }
    }

    fun sendArrearsWhatsAppReminder(context: Context, tenant: Tenant) {
        val reminderText = formatArrearsOnlyReminderMessage(tenant)
        val cleanPhone = tenant.phone.filter { it.isDigit() }.let { raw ->
            when {
                raw.length == 10 -> "91$raw"
                raw.startsWith("0") && raw.length == 11 -> "91${raw.substring(1)}"
                raw.startsWith("91") && raw.length == 12 -> raw
                else -> raw
            }
        }

        try {
            if (cleanPhone.isNotBlank()) {
                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${safeUrlEncode(reminderText)}"
                val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(waIntent)
            } else {
                openGenericShare(context, reminderText, "Send Dues Reminder (WhatsApp / SMS)")
            }
        } catch (_: Exception) {
            openGenericShare(context, reminderText, "Send Dues Reminder (WhatsApp / SMS)")
        }
    }

    /**
     * Sends the reminder directly to the tenant's Phone SMS Inbox.
     * Works on any phone even without WhatsApp or internet.
     */
    fun sendSmsReminder(context: Context, rent: RentRecord, tenantPhone: String? = null, tenant: Tenant? = null) {
        val reminderText = formatRentReminderMessage(rent, tenant)
        val cleanPhone = tenantPhone?.filter { it.isDigit() }?.let { raw ->
            when {
                raw.length == 10 -> raw
                raw.startsWith("0") && raw.length == 11 -> raw.substring(1)
                raw.startsWith("91") && raw.length == 12 -> raw.substring(2)
                else -> raw
            }
        }

        try {
            val smsUri = if (!cleanPhone.isNullOrBlank()) {
                Uri.parse("smsto:$cleanPhone")
            } else {
                Uri.parse("smsto:")
            }
            val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                putExtra("sms_body", reminderText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
        } catch (e: Exception) {
            openGenericShare(context, reminderText, "Send SMS Rent Reminder")
        }
    }

    fun shareRentReceipt(context: Context, rent: RentRecord) {
        val statusEmoji = when {
            rent.isPaid -> "✅ PAID"
            rent.isPartial -> "⚠️ PARTIALLY PAID"
            else -> "⏳ PENDING"
        }

        val receiptText = buildString {
            if (rent.isPersonal) {
                appendLine("🏠 *RESIDENTIAL FLAT RECEIPT*")
                appendLine("📋 *Official Rent & Utility Receipt*")
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("🏠 *Flat / Unit No:* ${rent.shopNumber}")
                appendLine("👤 *Tenant:* ${rent.tenantName}")
                appendLine("📅 *Month:* ${rent.month} ${rent.year}")
                appendLine("━━━━━━━━━━━━━━━━━━")
                val flatBaseRent = (rent.amountDue - rent.electricityBill).coerceAtLeast(0.0)
                if (rent.electricityBill > 0) {
                    appendLine("🏠 *Flat Rent:* ₹${formatAmount(flatBaseRent)}")
                    appendLine("⚡ *Electricity Bill:* ₹${formatAmount(rent.electricityBill)}")
                }
                appendLine("💰 *Total Due:* ₹${formatAmount(rent.amountDue)}")
                appendLine("💵 *Amount Paid:* ₹${formatAmount(rent.amountPaid)}")
                appendLine("⏳ *Pending Balance:* ₹${formatAmount(rent.pendingAmount)}")
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("📌 *Status:* $statusEmoji")
                if (rent.paidDate.isNotBlank()) {
                    appendLine("📅 *Payment Date:* ${rent.paidDate}")
                    appendLine("💳 *Mode:* ${rent.paymentMode}")
                }
                if (rent.receiptNumber.isNotBlank()) {
                    appendLine("🧾 *Receipt #:* ${rent.receiptNumber}")
                }
                if (rent.collectedBy.isNotBlank()) {
                    appendLine("👤 *Collected By:* ${rent.collectedBy}")
                }
                if (rent.notes.isNotBlank()) {
                    appendLine("📝 *Note:* ${rent.notes}")
                }
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("Thank you for your payment.")
            } else {
                val baseRent = (rent.amountDue - rent.pmcTax).coerceAtLeast(0.0)
                appendLine("🏪 *NASEEB LAL MARKET*")
                appendLine("📋 *Official Rent Receipt*")
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("🏠 *Shop No:* ${rent.shopNumber}")
                appendLine("👤 *Tenant:* ${rent.tenantName}")
                appendLine("📅 *Month:* ${rent.month} ${rent.year}")
                appendLine("━━━━━━━━━━━━━━━━━━")
                if (rent.pmcTax > 0) {
                    appendLine("🏠 *Base Rent:* ₹${formatAmount(baseRent)}")
                    appendLine("🏷️ *PMC Municipal Tax (1+ Year):* ₹${formatAmount(rent.pmcTax)}")
                }
                appendLine("💰 *Total Rent Due:* ₹${formatAmount(rent.amountDue)}")
                appendLine("💵 *Amount Paid:* ₹${formatAmount(rent.amountPaid)}")
                appendLine("⏳ *Pending Balance:* ₹${formatAmount(rent.pendingAmount)}")
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("📌 *Status:* $statusEmoji")
                if (rent.paidDate.isNotBlank()) {
                    appendLine("📅 *Payment Date:* ${rent.paidDate}")
                    appendLine("💳 *Mode:* ${rent.paymentMode}")
                }
                if (rent.receiptNumber.isNotBlank()) {
                    appendLine("🧾 *Receipt #:* ${rent.receiptNumber}")
                }
                if (rent.collectedBy.isNotBlank()) {
                    appendLine("👤 *Collected By:* ${rent.collectedBy}")
                }
                if (rent.notes.isNotBlank()) {
                    appendLine("📝 *Note:* ${rent.notes}")
                }
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("Thank you for your business.")
                appendLine("📍 *Naseeb Lal Market Management*")
            }
        }

        openGenericShare(context, receiptText, "Share Rent Receipt via WhatsApp / SMS")
    }

    fun sendWhatsAppReceipt(context: Context, rent: RentRecord, tenantPhone: String? = null) {
        val statusEmoji = when {
            rent.isPaid -> "✅ PAID"
            rent.isPartial -> "⚠️ PARTIALLY PAID"
            else -> "⏳ PENDING"
        }

        val receiptText = buildString {
            if (rent.isPersonal) {
                appendLine("🏠 *RESIDENTIAL FLAT RECEIPT*")
                appendLine("📋 *Official Rent & Utility Receipt*")
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("🏠 *Flat / Unit No:* ${rent.shopNumber}")
                appendLine("👤 *Tenant:* ${rent.tenantName}")
                appendLine("📅 *Month:* ${rent.month} ${rent.year}")
                appendLine("━━━━━━━━━━━━━━━━━━")
                val flatBaseRent = (rent.amountDue - rent.electricityBill).coerceAtLeast(0.0)
                if (rent.electricityBill > 0) {
                    appendLine("🏠 *Flat Rent:* ₹${formatAmount(flatBaseRent)}")
                    appendLine("⚡ *Electricity Bill:* ₹${formatAmount(rent.electricityBill)}")
                }
                appendLine("💰 *Total Due:* ₹${formatAmount(rent.amountDue)}")
                appendLine("💵 *Amount Paid:* ₹${formatAmount(rent.amountPaid)}")
                appendLine("⏳ *Pending Balance:* ₹${formatAmount(rent.pendingAmount)}")
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("📌 *Status:* $statusEmoji")
                if (rent.paidDate.isNotBlank()) {
                    appendLine("📅 *Payment Date:* ${rent.paidDate}")
                    appendLine("💳 *Mode:* ${rent.paymentMode}")
                }
                if (rent.receiptNumber.isNotBlank()) {
                    appendLine("🧾 *Receipt #:* ${rent.receiptNumber}")
                }
                if (rent.collectedBy.isNotBlank()) {
                    appendLine("👤 *Collected By:* ${rent.collectedBy}")
                }
                if (rent.notes.isNotBlank()) {
                    appendLine("📝 *Note:* ${rent.notes}")
                }
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("Thank you for your payment.")
            } else {
                val baseRent = (rent.amountDue - rent.pmcTax).coerceAtLeast(0.0)
                appendLine("🏪 *NASEEB LAL MARKET*")
                appendLine("📋 *Official Rent Receipt*")
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("🏠 *Shop No:* ${rent.shopNumber}")
                appendLine("👤 *Tenant:* ${rent.tenantName}")
                appendLine("📅 *Month:* ${rent.month} ${rent.year}")
                appendLine("━━━━━━━━━━━━━━━━━━")
                if (rent.pmcTax > 0) {
                    appendLine("🏠 *Base Rent:* ₹${formatAmount(baseRent)}")
                    appendLine("🏷️ *PMC Municipal Tax (1+ Year):* ₹${formatAmount(rent.pmcTax)}")
                }
                appendLine("💰 *Total Rent Due:* ₹${formatAmount(rent.amountDue)}")
                appendLine("💵 *Amount Paid:* ₹${formatAmount(rent.amountPaid)}")
                appendLine("⏳ *Pending Balance:* ₹${formatAmount(rent.pendingAmount)}")
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("📌 *Status:* $statusEmoji")
                if (rent.paidDate.isNotBlank()) {
                    appendLine("📅 *Payment Date:* ${rent.paidDate}")
                    appendLine("💳 *Mode:* ${rent.paymentMode}")
                }
                if (rent.receiptNumber.isNotBlank()) {
                    appendLine("🧾 *Receipt #:* ${rent.receiptNumber}")
                }
                if (rent.collectedBy.isNotBlank()) {
                    appendLine("👤 *Collected By:* ${rent.collectedBy}")
                }
                if (rent.notes.isNotBlank()) {
                    appendLine("📝 *Note:* ${rent.notes}")
                }
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("Thank you for your business.")
                appendLine("📍 *Naseeb Lal Market Management*")
            }
        }

        val cleanPhone = tenantPhone?.filter { it.isDigit() }?.let { raw ->
            when {
                raw.length == 10 -> "91$raw"
                raw.startsWith("0") && raw.length == 11 -> "91${raw.substring(1)}"
                raw.startsWith("91") && raw.length == 12 -> raw
                else -> raw
            }
        }

        try {
            if (!cleanPhone.isNullOrBlank()) {
                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(receiptText)}"
                val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(waIntent)
            } else {
                openGenericShare(context, receiptText, "Share Rent Receipt via WhatsApp")
            }
        } catch (e: Exception) {
            openGenericShare(context, receiptText, "Share Rent Receipt via WhatsApp")
        }
    }

    private fun openGenericShare(context: Context, text: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val shareIntent = Intent.createChooser(sendIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }

    private fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            String.format("%,d", amount.toLong())
        } else {
            String.format("%,.2f", amount)
        }
    }

    private fun safeUrlEncode(value: String): String {
        return try {
            java.net.URLEncoder.encode(value, "UTF-8")
        } catch (_: Exception) {
            value.replace(" ", "+")
        }
    }
}


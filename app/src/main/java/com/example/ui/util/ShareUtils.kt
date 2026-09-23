package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.RentRecord

object ShareUtils {

    fun generateTenantEchoPortalUrl(rent: RentRecord): String {
        val echoSlug = "${rent.tenantId}_${rent.month}_${rent.year}".replace(" ", "_")
        return "https://nasseblalmarkt.web.app/?echo=$echoSlug"
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

    fun formatRentReminderMessage(rent: RentRecord): String {
        val portalLink = generateTenantEchoPortalUrl(rent)
        val unitLabel = if (rent.isPersonal) "Flat No. ${rent.shopNumber}" else "Shop No. ${rent.shopNumber}"
        val periodLabel = if (rent.month.contains("yearly", ignoreCase = true) || rent.month.contains("varshik", ignoreCase = true)) {
            "Varshik (Yearly)"
        } else {
            "${rent.month} ${rent.year}"
        }
        val formattedAmount = formatAmount(rent.pendingAmount)

        return buildString {
            appendLine("Namaste ${rent.tenantName} ji, aapki $unitLabel ka $periodLabel kiraya Rs $formattedAmount due hai. Kripya samay par jama karein. Shukriya - Naseeb Lal Market (Sampark: Vimal Kumar, Mob: 7654138539)")
            appendLine()
            appendLine("🔗 Apna Payment Status & Portal Link:")
            appendLine(portalLink)
            appendLine()
            appendLine("(Upar link par click karke aap payment ki expected date de sakte hain, ya payment jama kar diya ho toh verify karwa ke receipt le sakte hain)")
        }.trim()
    }

    /**
     * Sends a polite and clear WhatsApp rent payment reminder to the tenant.
     * Targets tenant's phone number directly if available, otherwise opens chooser.
     */
    fun sendWhatsAppReminder(context: Context, rent: RentRecord, tenantPhone: String? = null) {
        val reminderText = formatRentReminderMessage(rent)

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

    /**
     * Sends the reminder directly to the tenant's Phone SMS Inbox.
     * Works on any phone even without WhatsApp or internet.
     */
    fun sendSmsReminder(context: Context, rent: RentRecord, tenantPhone: String? = null) {
        val reminderText = formatRentReminderMessage(rent)
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


package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.RentRecord

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

    fun formatRentReminderMessage(rent: RentRecord): String {
        return buildString {
            if (rent.isPersonal) {
                appendLine("🏠 *RESIDENTIAL / FLAT MANAGEMENT*")
                appendLine("📢 *RENT & BILL PAYMENT REMINDER*")
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("Dear *${rent.tenantName}*,")
                appendLine()
                appendLine("This is a friendly reminder regarding your dues for *Flat/Unit ${rent.shopNumber}*.")
                appendLine()
                appendLine("📅 *Month:* ${rent.month} ${rent.year}")
                val flatBaseRent = (rent.amountDue - rent.electricityBill).coerceAtLeast(0.0)
                if (rent.electricityBill > 0) {
                    appendLine("🏠 *Flat Rent:* ₹${formatAmount(flatBaseRent)}")
                    appendLine("⚡ *Electricity Bill:* ₹${formatAmount(rent.electricityBill)}")
                    appendLine("💰 *Total Amount Due:* ₹${formatAmount(rent.amountDue)}")
                } else {
                    appendLine("💰 *Total Amount Due:* ₹${formatAmount(rent.amountDue)}")
                }
                if (rent.amountPaid > 0) {
                    appendLine("💵 *Amount Paid so far:* ₹${formatAmount(rent.amountPaid)}")
                }
                appendLine("⏳ *PENDING BALANCE:* ₹${formatAmount(rent.pendingAmount)}")
                if (rent.dueDate.isNotBlank()) {
                    appendLine("⏰ *Due Date:* ${rent.dueDate}")
                }
                if (rent.promisedDate.isNotBlank() && !isPromiseDateActive(rent.promisedDate)) {
                    appendLine("⚠️ *Promise Passed:* Aapne ${rent.promisedDate} tak bhugtan ka vada kiya tha jo beet chuka hai.")
                }
                appendLine("━━━━━━━━━━━━━━━━━━")
                val portalLink = generateTenantEchoPortalUrl(rent)
                val appDeepLink = "naseeblalmarket://portal?echo=${rent.tenantId}_${rent.month}_${rent.year}".replace(" ", "_")
                val encodedUnit = safeUrlEncode(if (rent.isPersonal) "Flat ${rent.shopNumber}" else "Shop ${rent.shopNumber}")
                val encodedMonth = safeUrlEncode("${rent.month} ${rent.year}")
                val encodedBal = safeUrlEncode("Rs. ${formatAmount(rent.pendingAmount)}")

                appendLine("🔗 *Tenant Self-Service Portal:*")
                appendLine("👉 $portalLink")
                appendLine("*(App me direct kholne ke liye upar link tap karein)*")
                appendLine()
                appendLine("⚡ *Instant 1-Click WhatsApp Reply:*")
                appendLine("📅 *Vada / Promise Date dene ke liye tap karein:*")
                appendLine("👉 https://wa.me/?text=Namaste+Management,+I+promise+to+pay+rent+for+$encodedUnit+($encodedMonth)+by+Date:_________+Note:_________")
                appendLine()
                appendLine("💳 *Payment jama karne / UTR bhejne ke liye tap karein:*")
                appendLine("👉 https://wa.me/?text=Namaste+Management,+I+have+paid+rent+for+$encodedUnit+($encodedMonth).+Amount:+$encodedBal.+Mode:+[Cash/Online].+Ref/UTR+No:_________")
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("Kindly clear the pending balance at your earliest convenience.")
                appendLine("⚠️ *Important Notice (Anti-Fraud Policy):*")
                appendLine("• Koi bhi online payment gateway ya automatic payment link nahi diya gaya hai.")
                appendLine("• Payment direct authorized Management se contact karke hi jama karein.")
                appendLine("• Online transfer kiya ho to Portal link par jakar apna UTR/Ref No. enter karein taaki fraud-check verify ho sake.")
                appendLine("If already paid, click 'I Have Paid' on the link and enter your UTR number.")
                appendLine()
                appendLine("📍 *Residential Management*")
            } else {
                val baseRent = (rent.amountDue - rent.pmcTax).coerceAtLeast(0.0)
                appendLine("🏪 *NASEEB LAL MARKET*")
                appendLine("📢 *RENT PAYMENT REMINDER*")
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("Dear *${rent.tenantName}*,")
                appendLine()
                appendLine("This is a friendly reminder regarding your rent for *Shop ${rent.shopNumber}*.")
                appendLine()
                appendLine("📅 *Month:* ${rent.month} ${rent.year}")
                if (rent.pmcTax > 0) {
                    appendLine("🏠 *Monthly Rent:* ₹${formatAmount(baseRent)}")
                    appendLine("🏷️ *PMC Tax (1+ Year Completed):* ₹${formatAmount(rent.pmcTax)}")
                    appendLine("💰 *Total Amount Due:* ₹${formatAmount(rent.amountDue)}")
                } else {
                    appendLine("💰 *Total Amount Due:* ₹${formatAmount(rent.amountDue)}")
                }
                if (rent.amountPaid > 0) {
                    appendLine("💵 *Amount Paid so far:* ₹${formatAmount(rent.amountPaid)}")
                }
                appendLine("⏳ *PENDING BALANCE:* ₹${formatAmount(rent.pendingAmount)}")
                if (rent.dueDate.isNotBlank()) {
                    appendLine("⏰ *Due Date:* ${rent.dueDate}")
                }
                if (rent.promisedDate.isNotBlank() && !isPromiseDateActive(rent.promisedDate)) {
                    appendLine("⚠️ *Promise Passed:* Aapne ${rent.promisedDate} tak bhugtan ka vada kiya tha jo beet chuka hai.")
                }
                appendLine("━━━━━━━━━━━━━━━━━━")
                val portalLink = generateTenantEchoPortalUrl(rent)
                val appDeepLink = "naseeblalmarket://portal?echo=${rent.tenantId}_${rent.month}_${rent.year}".replace(" ", "_")
                val encodedUnit = safeUrlEncode("Shop ${rent.shopNumber}")
                val encodedMonth = safeUrlEncode("${rent.month} ${rent.year}")
                val encodedBal = safeUrlEncode("Rs. ${formatAmount(rent.pendingAmount)}")

                appendLine("🔗 *Tenant Self-Service Portal:*")
                appendLine("👉 $portalLink")
                appendLine("*(App me direct kholne ke liye upar link tap karein)*")
                appendLine()
                appendLine("⚡ *Instant 1-Click WhatsApp Reply:*")
                appendLine("📅 *Vada / Promise Date dene ke liye tap karein:*")
                appendLine("👉 https://wa.me/?text=Namaste+Market+Management,+I+promise+to+pay+rent+for+$encodedUnit+($encodedMonth)+by+Date:_________+Note:_________")
                appendLine()
                appendLine("💳 *Payment jama karne / UTR bhejne ke liye tap karein:*")
                appendLine("👉 https://wa.me/?text=Namaste+Market+Management,+I+have+paid+rent+for+$encodedUnit+($encodedMonth).+Amount:+$encodedBal.+Payment+Mode:+[Cash/Online].+Ref/UTR+No:_________")
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("Kindly clear the pending balance at your earliest convenience.")
                appendLine("⚠️ *Important Notice (Anti-Fraud Policy):*")
                appendLine("• Kisi bhi payment link ya UPI ID par payment na karein.")
                appendLine("• Payment direct authorized Market Management se contact karke hi karein.")
                appendLine("• Bhugtan ke baad portal link par 'I Have Paid' dabayein aur valid UTR/Ref No. dalein.")
                appendLine("Management UTR verify karke hi final digital receipt unlock karegi.")
                appendLine()
                appendLine("📍 *Naseeb Lal Market Management*")
            }
        }
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
                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(reminderText)}"
                val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(waIntent)
            } else {
                openGenericShare(context, reminderText, "Send Rent Reminder via WhatsApp")
            }
        } catch (e: Exception) {
            // Fallback to standard share if WhatsApp URI fails
            openGenericShare(context, reminderText, "Send Rent Reminder via WhatsApp")
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


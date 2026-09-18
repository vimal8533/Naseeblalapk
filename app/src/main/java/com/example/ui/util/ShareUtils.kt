package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.RentRecord

object ShareUtils {

    /**
     * Sends a polite and clear WhatsApp rent payment reminder to the tenant.
     * Targets tenant's phone number directly if available, otherwise opens chooser.
     */
    fun sendWhatsAppReminder(context: Context, rent: RentRecord, tenantPhone: String? = null) {
        val reminderText = buildString {
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
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("Kindly clear the pending balance at your earliest convenience.")
                appendLine("If already paid, kindly share the payment reference. Thank you!")
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
                appendLine("━━━━━━━━━━━━━━━━━━")
                appendLine("Kindly clear the pending balance at your earliest convenience.")
                appendLine("If already paid, kindly share the payment reference. Thank you!")
                appendLine()
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
}


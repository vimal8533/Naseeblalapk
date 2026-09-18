package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.app.ActivityCompat

data class SimInfo(
    val subscriptionId: Int,
    val displayName: String,
    val carrierName: String,
    val slotIndex: Int
)

object SmsReminderHelper {

    private const val TAG = "SmsReminderHelper"

    /**
     * Builds a personalized, polite, and clear Hindi/Hinglish SMS for the tenant.
     * CRITICAL: Strictly NO UPI ID or payment links are included.
     */
    fun formatReminderMessage(
        tenantName: String,
        shopNumber: String,
        month: String,
        year: Int,
        pendingAmount: Double,
        senderName: String = "",
        senderContactPhone: String = "",
        cycleMonths: Int = 1,
        isPersonal: Boolean = false,
        electricityBill: Double = 0.0,
        flatBaseRent: Double = 0.0
    ): String {
        val cleanName = tenantName.trim().ifBlank { "Kirayedaar" }
        val cleanShop = shopNumber.trim().ifBlank { if (isPersonal) "Flat" else "Dukaan" }
        val amountStr = if (pendingAmount % 1.0 == 0.0) {
            String.format("%,d", pendingAmount.toLong())
        } else {
            String.format("%,.2f", pendingAmount)
        }

        val trimmedName = senderName.trim()
        val trimmedPhone = senderContactPhone.trim()
        val contactDetails = when {
            trimmedName.isNotBlank() && trimmedPhone.isNotBlank() -> "$trimmedName, Mob: $trimmedPhone"
            trimmedPhone.isNotBlank() -> "Mob: $trimmedPhone"
            trimmedName.isNotBlank() -> trimmedName
            else -> ""
        }
        val contactSuffix = if (contactDetails.isNotBlank()) {
            " (Sampark: $contactDetails)"
        } else {
            ""
        }

        val cyclePhrase = when (cycleMonths) {
            3 -> "3 Mahine ka"
            6 -> "6 Mahine ka"
            12 -> "Varshik (Yearly)"
            else -> "$month $year ka"
        }

        return if (isPersonal) {
            val flatLabel = if (cleanShop.startsWith("Flat", ignoreCase = true)) cleanShop else "Flat $cleanShop"
            val breakdownStr = if (electricityBill > 0.0 && flatBaseRent > 0.0) {
                val rentStr = if (flatBaseRent % 1.0 == 0.0) String.format("%,d", flatBaseRent.toLong()) else String.format("%,.2f", flatBaseRent)
                val elecStr = if (electricityBill % 1.0 == 0.0) String.format("%,d", electricityBill.toLong()) else String.format("%,.2f", electricityBill)
                "kiraya Rs $rentStr + Bijli Bill Rs $elecStr (Kul Rs $amountStr)"
            } else {
                "kiraya Rs $amountStr"
            }
            val signOff = if (trimmedName.isNotBlank()) "Shukriya - $trimmedName" else "Shukriya"
            "Namaste $cleanName ji, aapke $flatLabel ka $cyclePhrase $breakdownStr due hai. Kripya samay par jama karein. $signOff$contactSuffix"
        } else {
            "Namaste $cleanName ji, aapki Shop No. $cleanShop ka $cyclePhrase kiraya Rs $amountStr due hai. Kripya samay par jama karein. Shukriya - Naseeb Lal Market$contactSuffix"
        }
    }

    /**
     * Inspects device for active SIM cards (e.g. SIM 1 / SIM 2)
     */
    fun getAvailableSims(context: Context): List<SimInfo> {
        val simList = mutableListOf<SimInfo>()
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                val sm = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                val subs = sm?.activeSubscriptionInfoList
                subs?.forEachIndexed { index, sub ->
                    val name = sub.displayName?.toString()?.ifBlank { "SIM ${index + 1}" } ?: "SIM ${index + 1}"
                    val carrier = sub.carrierName?.toString()?.ifBlank { "Cellular" } ?: "Cellular"
                    simList.add(
                        SimInfo(
                            subscriptionId = sub.subscriptionId,
                            displayName = "$name ($carrier)",
                            carrierName = carrier,
                            slotIndex = sub.simSlotIndex
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching SIM subscriptions: ${e.message}")
        }

        if (simList.isEmpty()) {
            simList.add(
                SimInfo(
                    subscriptionId = -1,
                    displayName = "Default Phone SIM",
                    carrierName = "Active SIM",
                    slotIndex = 0
                )
            )
        }
        return simList
    }

    /**
     * Dispatches an SMS using standard Android SmsManager
     */
    fun sendSms(
        context: Context,
        rawPhoneNumber: String,
        message: String,
        subscriptionId: Int? = null
    ): Boolean {
        val cleanNumber = sanitizePhoneNumber(rawPhoneNumber)
        if (cleanNumber.isBlank() || cleanNumber.length < 10) {
            Log.w(TAG, "Invalid phone number: $rawPhoneNumber")
            return false
        }

        return try {
            val smsManager: SmsManager = when {
                subscriptionId != null && subscriptionId != -1 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.getSystemService(SmsManager::class.java).createForSubscriptionId(subscriptionId)
                    } else {
                        @Suppress("DEPRECATION")
                        SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    context.getSystemService(SmsManager::class.java)
                }
                else -> {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
            }

            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(cleanNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(cleanNumber, null, message, null, null)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $cleanNumber: ${e.message}")
            false
        }
    }

    private fun sanitizePhoneNumber(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.length == 10 -> digits
            digits.length == 11 && digits.startsWith("0") -> digits.substring(1)
            digits.length == 12 && digits.startsWith("91") -> digits.substring(2)
            else -> digits
        }
    }
}
